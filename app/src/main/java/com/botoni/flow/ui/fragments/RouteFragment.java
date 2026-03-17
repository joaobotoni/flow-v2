package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentRouteBinding;
import com.botoni.flow.ui.state.RouteUiState;
import com.botoni.flow.ui.viewmodel.RouteViewModel;

import java.util.Arrays;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RouteFragment extends Fragment {
    private FragmentRouteBinding binding;
    private RouteViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRouteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        initObservers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
    }

    private void initObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::bind);
    }

    private void bind(RouteUiState state) {
        if (!state.isVisible()) return;
        bindOrigin(state.getPoints().get(0));
        bindDestination(state.getPoints().get(1));
        bindDistance(state.getDistance());
    }

    private void bindOrigin(String originPoint) {
        String[] origin = parse(originPoint);
        binding.textoCidadeOrigem.setText(origin[0]);
        binding.textoEstadoOrigem.setText(origin[1]);
    }

    private void bindDestination(String destinationPoint) {
        String[] destination = parse(destinationPoint);
        binding.textoCidadeDestino.setText(destination[0]);
        binding.textoEstadoDestino.setText(destination[1]);
    }

    private void bindDistance(double distance) {
        binding.textoValorDistancia.setText(String.format(Locale.getDefault(), "%.2f", distance));
    }

    private String[] parse(String point) {
        return Arrays.stream(point.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }
}