package com.botoni.flow.ui.fragments;

import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalBezerro;
import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalPorKg;
import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalTodosBezerros;
import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.getTexto;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.R;
import com.botoni.flow.data.repositories.local.CategoriaFreteRepository;
import com.botoni.flow.databinding.FragmentDealBinding;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.viewmodel.DealViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DealFragment extends Fragment {
    private static final String TAG = "DealFragment";
    private static final String STATE_CATEGORIA = "state_categoria";
    private static final String STATE_FRETE_VISIBLE = "state_frete_visible";
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FragmentDealBinding binding;
    private String selectedCategory;
    @Inject
    TaskHelper taskHelper;
    @Inject
    CategoriaFreteRepository categoriaFreteRepository;

    private ActivityResultLauncher<String[]> permissionLauncher;

    private boolean isFreteVisible = false;

    private DealViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DealViewModel.class);
        restoreInstanceState(savedInstanceState);
        registerPermissionLauncher();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!hasPermissions(PERMISSIONS)) {
            requestLocationPermissions();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager childFragmentManager = getChildFragmentManager();
        configureChips();
        setupTextWatchers();
        registerSearchBottomSheetResultListener(childFragmentManager);
        registerRouteResultListener(childFragmentManager);
        attachRouteFragment(childFragmentManager, savedInstanceState);
        binding.layoutContainerFrete.setVisibility(isFreteVisible ? View.VISIBLE : View.GONE);
        binding.botaoAbrirBottomSheet.setOnClickListener(v -> openSearchBottomSheet());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CATEGORIA, selectedCategory);
        outState.putBoolean(STATE_FRETE_VISIBLE, isFreteVisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void restoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        selectedCategory = savedInstanceState.getString(STATE_CATEGORIA);
        isFreteVisible = savedInstanceState.getBoolean(STATE_FRETE_VISIBLE, false);
    }

    private void registerSearchBottomSheetResultListener(FragmentManager childFragmentManager) {
        getParentFragmentManager().setFragmentResultListener(
                "SearchBottonSheetFragment", this, (key, result) ->
                        childFragmentManager.setFragmentResult(TAG, result)
        );
    }

    private void registerRouteResultListener(FragmentManager childFragmentManager) {
        childFragmentManager.setFragmentResultListener("RouteFragment", getViewLifecycleOwner(), (key, result) -> {
            if (result.getBoolean("linked")) {
                isFreteVisible = true;
                binding.layoutContainerFrete.setVisibility(View.VISIBLE);
            }
        });
    }

    private void attachRouteFragment(FragmentManager childFragmentManager, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.layout_container_frete, new RouteFragment())
                    .commit();
        }
    }

    private void resetRouteFragment(FragmentManager childFragmentManager) {
        isFreteVisible = false;
        binding.layoutContainerFrete.setVisibility(View.GONE);
        childFragmentManager.beginTransaction()
                .replace(R.id.layout_container_frete, new RouteFragment())
                .commit();
    }

    private void openSearchBottomSheet() {
        SearchBottonSheetFragment bottomSheetFragment = new SearchBottonSheetFragment();
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
    }

    private void setupTextWatchers() {
        if (binding == null) return;
        TextWatcher calculationWatcher = SimpleTextWatcher(this::calculateAndUpdateResults);
        binding.entradaTextoPesoAnimal.addTextChangedListener(calculationWatcher);
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(calculationWatcher);
    }

    private void configureChips() {
        taskHelper.execute(
                categoriaFreteRepository::getDescricoes,
                categorias -> {
                    if (binding != null && isAdded()) {
                        for (String categoria : categorias) {
                            Chip chip = createChip(categoria);
                            if (chip != null) {
                                binding.grupoChipsCategorias.addView(chip);
                            }
                        }
                    }
                },
                e -> {
                    if (binding != null && isAdded()) {
                        showSnackbar(binding.getRoot(), getString(R.string.error_generic));
                    }
                }
        );
    }

    private Chip createChip(String texto) {
        if (!isAdded()) return null;
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setShapeAppearanceModel(ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, 50f)
                .build());
        chip.setOnClickListener(v -> selectedCategory = texto);
        chip.setChecked(Objects.equals(texto, selectedCategory));
        return chip;
    }

    private void calculateAndUpdateResults() {
        if (binding == null) return;

        boolean allFieldsFilled = areAllFieldsFilled(
                getTexto(binding.entradaTextoPesoAnimal),
                getTexto(binding.entradaTextoQuantidadeAnimais)
        );

        int visibility = allFieldsFilled ? View.VISIBLE : View.GONE;
        binding.layoutContainerResultados.setVisibility(visibility);
        binding.layoutContainerBotoes.setVisibility(visibility);

        if (allFieldsFilled) {
            BigDecimal peso = getBigDecimal(binding.entradaTextoPesoAnimal);
            BigDecimal precoArroba = new BigDecimal("310.0");
            BigDecimal percentual = new BigDecimal("30.0");
            int quantidade = getInt(binding.entradaTextoQuantidadeAnimais);

            BigDecimal valorPorKg = calcularValorTotalPorKg(peso, precoArroba, percentual);
            BigDecimal valorPorCabeca = calcularValorTotalBezerro(peso, precoArroba, percentual);
            BigDecimal valorTotal = calcularValorTotalTodosBezerros(valorPorCabeca, quantidade);

            binding.textoValorPorKg.setText(formatCurrency(valorPorKg));
            binding.textoValorPorCabeca.setText(formatCurrency(valorPorCabeca));
            binding.textoValorTotal.setText(formatCurrency(valorTotal));
        }
    }

    private boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void registerPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (result == null) return;
                    boolean fineLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarseLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (!fineLocationGranted || !coarseLocationGranted) {
                        handleLocationDenied();
                    }
                });
    }

    private void requestLocationPermissions() {
        if (permissionLauncher != null) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void handleLocationDenied() {
        if (!isAdded()) return;
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (dialog, which) -> navigateToAppSettings(),
                (dialog, which) -> {
                    if (getActivity() != null) {
                        requireActivity().finish();
                    }
                }
        );
    }

    private void navigateToAppSettings() {
        if (!isAdded()) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }

    private boolean areAllFieldsFilled(String... fields) {
        if (binding == null) return false;
        return Arrays.stream(fields).noneMatch(String::isEmpty);
    }

    private void showSnackbar(View view, String message) {
        if (view != null && message != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showDialog(Context context, String title, String message,
                            DialogInterface.OnClickListener positiveListener,
                            DialogInterface.OnClickListener negativeListener) {
        if (context == null || !isAdded()) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_ok), positiveListener)
                .setNegativeButton(getString(R.string.action_no), negativeListener)
                .show();
    }
}