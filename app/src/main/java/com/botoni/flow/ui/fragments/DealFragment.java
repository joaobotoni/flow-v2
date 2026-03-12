package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.getTexto;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentDealBinding;
import com.botoni.flow.ui.adapters.CategoryAdapter;
import com.botoni.flow.ui.helpers.PermissionHelper;
import com.botoni.flow.ui.state.DealUiState;
import com.botoni.flow.ui.viewmodel.DealViewModel;
import com.botoni.flow.ui.viewmodel.RouteViewModel;

import java.math.BigDecimal;
import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DealFragment extends Fragment {
    private static final String KEY_SEARCH_SHEET = "SearchBottomSheetFragment";
    private static final String KEY_DEAL = "DealFragment";

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FragmentDealBinding binding;
    private DealViewModel dealViewModel;
    private RouteViewModel routeViewModel;
    private CategoryAdapter categoryAdapter;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPermissionLauncher();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermissionsIfNeeded();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModels();
        initChildFragments(savedInstanceState);
        initViews();
        initObservers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModels() {
        dealViewModel = new ViewModelProvider(requireActivity()).get(DealViewModel.class);
        routeViewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
    }

    private void initChildFragments(@Nullable Bundle savedInstanceState) {
        relaySearchSheetResult();
        if (savedInstanceState == null) attachRouteFragment();
    }

    private void initViews() {
        initCategoryAdapter();
        initInputWatchers();
        binding.botaoAbrirBottomSheet.setOnClickListener(v -> openSearchSheet());
    }

    private void initObservers() {
        observeCategories();
        observeFreightVisibility();
        observeDealState();
        observeErrors();
    }

    private void initCategoryAdapter() {
        categoryAdapter = new CategoryAdapter(dealViewModel::select);
        binding.listaCategorias.setAdapter(categoryAdapter);
    }

    private void initInputWatchers() {
        binding.entradaTextoPesoAnimal.addTextChangedListener(SimpleTextWatcher(this::onInputChanged));
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(SimpleTextWatcher(this::onInputChanged));
    }

    private void observeCategories() {
        dealViewModel.getCategories().observe(getViewLifecycleOwner(), categoryAdapter::submitList);
    }

    private void observeErrors() {
        dealViewModel.getErrorEvent().observe(getViewLifecycleOwner(),
                error -> showSnackBar(binding.getRoot(), getString(R.string.error_generic)));
    }

    private void observeDealState() {
        dealViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            setResultsVisible(state.isCalculated());
            if (state.isCalculated()) bindResults(state);
        });
    }

    private void observeFreightVisibility() {
        MediatorLiveData<Boolean> freightVisible = new MediatorLiveData<>();
        freightVisible.addSource(dealViewModel.getUiState(), ignored -> freightVisible.setValue(shouldShowFreight()));
        freightVisible.addSource(routeViewModel.getUiState(), ignored -> freightVisible.setValue(shouldShowFreight()));
        freightVisible.observe(getViewLifecycleOwner(), show ->
                binding.layoutContainerFrete.setVisibility(Boolean.TRUE.equals(show) ? View.VISIBLE : View.GONE));
    }

    private void bindResults(DealUiState state) {
        binding.textoValorPorCabeca.setText(formatCurrency(state.getValorPorCabeca()));
        binding.textoValorPorKg.setText(formatCurrency(state.getValorPorKg()));
        binding.textoValorTotal.setText(formatCurrency(state.getValorTotal()));
    }

    private void setResultsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.layoutContainerResultados.setVisibility(visibility);
        binding.layoutContainerBotoes.setVisibility(visibility);
    }

    private void relaySearchSheetResult() {
        getParentFragmentManager().setFragmentResultListener(
                KEY_SEARCH_SHEET,
                getViewLifecycleOwner(),
                (key, result) -> getChildFragmentManager().setFragmentResult(KEY_DEAL, result)
        );
    }

    private void attachRouteFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_frete, new RouteFragment())
                .commit();
    }

    private void openSearchSheet() {
        SearchBottonSheetFragment sheet = new SearchBottonSheetFragment();
        sheet.show(getParentFragmentManager(), sheet.getTag());
    }

    private void onInputChanged() {
        String weight = getTexto(binding.entradaTextoPesoAnimal);
        String quantity = getTexto(binding.entradaTextoQuantidadeAnimais);

        if (allFilled(weight, quantity)) {
            BigDecimal weightValue = getBigDecimal(binding.entradaTextoPesoAnimal);
            Integer quantityValue = getInt(binding.entradaTextoQuantidadeAnimais);
            dealViewModel.calculate(weightValue, quantityValue);
        } else {
            dealViewModel.reset();
        }
    }

    private boolean allFilled(String... fields) {
        return Arrays.stream(fields).noneMatch(String::isEmpty);
    }

    private void registerPermissionLauncher() {
        permissionLauncher = PermissionHelper.register(this, (granted, result) -> {
            if (!granted) onLocationPermissionDenied();
        });
    }

    private void requestLocationPermissionsIfNeeded() {
        if (!hasPermissions(requireContext(), LOCATION_PERMISSIONS)) {
            request(requireContext(), permissionLauncher, LOCATION_PERMISSIONS);
        }
    }

    private void onLocationPermissionDenied() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (dialog, which) -> openAppSettings(),
                (dialog, which) -> { if (isAdded()) requireActivity().finish(); });
    }

    private void openAppSettings() {
        if (!isAdded()) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }

    private boolean shouldShowFreight() {
        boolean isCalculated = dealViewModel.getUiState().getValue() != null
                && dealViewModel.getUiState().getValue().isCalculated();
        boolean hasRoute = routeViewModel.getUiState().getValue() != null
                && routeViewModel.getUiState().getValue().isFreightVisible();
        return isCalculated && hasRoute;
    }
}