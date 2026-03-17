package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.noneMatch;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;
import static com.botoni.flow.ui.helpers.ViewHelper.setVisible;

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
import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.databinding.FragmentDealBinding;
import com.botoni.flow.domain.entities.Transport;
import com.botoni.flow.ui.adapters.CategoryAdapter;
import com.botoni.flow.ui.helpers.PermissionHelper;
import com.botoni.flow.ui.state.CategoryUiState;
import com.botoni.flow.ui.state.FreightUiState;
import com.botoni.flow.ui.state.RouteUiState;
import com.botoni.flow.ui.state.TransportUiState;
import com.botoni.flow.ui.viewmodel.CalfResultViewModel;
import com.botoni.flow.ui.viewmodel.CategoryViewModel;
import com.botoni.flow.ui.viewmodel.FreightViewModel;
import com.botoni.flow.ui.viewmodel.RouteViewModel;
import com.botoni.flow.ui.viewmodel.TransportViewModel;

import java.math.BigDecimal;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DealFragment extends Fragment {
    private FragmentDealBinding binding;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private ActivityResultLauncher<String[]> permissionLauncher;
    private CalfResultViewModel calfResultViewModel;
    private RouteViewModel routeViewModel;
    private FreightViewModel freightViewModel;
    private TransportViewModel transportViewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;

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
        initViewModel();
        initViews();
        initObservers();
        initChildrenFragments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViews() {
        initInputWatchers();
        initAdapter();
        binding.botaoAbrirBottomSheet.setOnClickListener(v -> openSearchSheet());
    }

    private void initAdapter() {
        categoryAdapter = new CategoryAdapter(categoryViewModel::select);
        binding.listaCategorias.setAdapter(categoryAdapter);
    }

    private void initChildrenFragments() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valores, new CalfResultFragment())
                .replace(R.id.layout_container_rota, new RouteFragment())
                .replace(R.id.layout_container_frete, new FreightFragment())
                .replace(R.id.layout_container_transporte, new TransportFragment())
                .commit();
    }

    private void initInputWatchers() {
        binding.entradaTextoPesoAnimal.addTextChangedListener(
                SimpleTextWatcher(this::onInputChangedCalc));

        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(
                SimpleTextWatcher(() -> {
                    onInputChangedCalc();
                    onInputChangedTransport(categoryViewModel.getSelected());
                    onInputChangedFreight();
                }));
    }

    private void initViewModel() {
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        calfResultViewModel = new ViewModelProvider(requireActivity()).get(CalfResultViewModel.class);
        routeViewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
        freightViewModel = new ViewModelProvider(requireActivity()).get(FreightViewModel.class);
        transportViewModel = new ViewModelProvider(requireActivity()).get(TransportViewModel.class);
    }

    private void initObservers() {
        observeCalfResult();
        observeRoute();
        observeFreight();
        observeTransport();
        observeCategory();
        observeSelectedCategory();
    }

    private void observeCalfResult() {
        calfResultViewModel.getUiState().observe(getViewLifecycleOwner(),
                state -> setVisible(state.isVisible(), binding.layoutContainerValores, binding.layoutContainerBotoes));
    }

    private void observeRoute() {
        routeViewModel.getUiState().observe(getViewLifecycleOwner(),
                state -> {
                    setVisible(state.isVisible(), binding.layoutContainerRota);
                    onInputChangedFreight();
                });
    }

    private void observeTransport() {
        transportViewModel.getUiState().observe(getViewLifecycleOwner(),
                state -> {
                    setVisible(state.isVisible(), binding.layoutContainerTransporte);
                    onInputChangedFreight();
                });
    }

    private void observeFreight() {
        freightViewModel.getUiState().observe(getViewLifecycleOwner(),
                state -> setVisible(state.isVisible(), binding.layoutContainerFrete));
    }


    private void observeCategory() {
        categoryViewModel.getUiState().observe(getViewLifecycleOwner(), this::bindRecycleView);
    }

    private void observeSelectedCategory() {
        categoryViewModel.getUiState().observe(getViewLifecycleOwner(),
                state -> onInputChangedTransport(state.getSelected()));
    }


    private void bindRecycleView(CategoryUiState state) {
        categoryAdapter.submitList(state.getCategories());
        categoryAdapter.setSelected(state.getSelected());
    }

    private void onInputChangedCalc() {
        String weight = requireText(binding.entradaTextoPesoAnimal);
        String quantity = requireText(binding.entradaTextoQuantidadeAnimais);

        if (noneMatch(weight, quantity)) {
            BigDecimal weightValue = getBigDecimal(binding.entradaTextoPesoAnimal);
            Integer quantityValue = getInt(binding.entradaTextoQuantidadeAnimais);
            calfResultViewModel.calculate(weightValue, quantityValue);
        } else {
            calfResultViewModel.reset();
        }
    }

    private void onInputChangedTransport(CategoriaFrete selectedCategory) {
        String quantity = requireText(binding.entradaTextoQuantidadeAnimais);

        if (noneMatch(quantity) && selectedCategory != null) {
            Integer quantityValue = getInt(binding.entradaTextoQuantidadeAnimais);
            transportViewModel.calculate(selectedCategory.getId(), quantityValue);
        } else {
            transportViewModel.reset();
        }
    }

    private void onInputChangedFreight() {
        RouteUiState routeState = routeViewModel.getUiState().getValue();
        TransportUiState transportState = transportViewModel.getUiState().getValue();
        String quantityStr = requireText(binding.entradaTextoQuantidadeAnimais);

        boolean hasRoute = routeState != null && routeState.isVisible();
        boolean hasTransport = transportState != null && transportState.isVisible() && transportState.getTransports() != null && !transportState.getTransports().isEmpty();
        boolean hasQuantity = noneMatch(quantityStr);

        if (hasRoute && hasTransport && hasQuantity) {
            double distance = routeState.getDistance();
            List<Transport> transports = transportState.getTransports();
            int count = getInt(binding.entradaTextoQuantidadeAnimais);

            freightViewModel.calculate(transports, distance, count);
        } else {
            freightViewModel.reset();
        }
    }


    private void registerPermissionLauncher() {
        permissionLauncher = PermissionHelper.register(this, (granted, result) -> {
            if (!granted) onLocationPermissionDenied();
        });
    }

    private void openSearchSheet() {
        SearchFragment sheet = new SearchFragment();
        sheet.show(getParentFragmentManager(), sheet.getTag());
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
                (dialog, which) -> {
                    if (isAdded()) requireActivity().finish();
                });
    }

    private void openAppSettings() {
        if (!isAdded()) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }
}