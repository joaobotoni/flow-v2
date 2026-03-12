package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.databinding.FragmentRouteBinding;
import com.botoni.flow.ui.adapters.TransportAdapter;
import com.botoni.flow.ui.state.RouteUiState;
import com.botoni.flow.ui.viewmodel.RouteViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RouteFragment extends Fragment {

    private static final String KEY_DEAL = "DealFragment";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_POINTS = "points";
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
        initViews();
        initObservers();
        initResultListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
    }

    private void initViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(new TransportAdapter());
    }

    private void initObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::bindRoute);
    }

    private void initResultListener() {
        getParentFragmentManager().setFragmentResultListener(KEY_DEAL, getViewLifecycleOwner(),
                (key, result) -> viewModel.setRoute(
                        result.getStringArrayList(KEY_POINTS),
                        result.getDouble(KEY_DISTANCE)));
    }

    private void bindRoute(RouteUiState state) {
        if (binding == null || state.getPoints() == null || state.getPoints().size() < 2) return;

        String[] origin = parsePoint(state.getPoints().get(0));
        String[] destination = parsePoint(state.getPoints().get(1));

        binding.textoCidadeOrigem.setText(origin[0]);
        binding.textoEstadoOrigem.setText(origin[1]);
        binding.textoCidadeDestino.setText(destination[0]);
        binding.textoEstadoDestino.setText(destination[1]);
        binding.textoValorDistancia.setText(String.format(Locale.getDefault(), "%.2f", state.getDistance()));
    }

    private String[] parsePoint(String point) {
        return Arrays.stream(point.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }
}