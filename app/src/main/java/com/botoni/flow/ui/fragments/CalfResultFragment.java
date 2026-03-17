package com.botoni.flow.ui.fragments;
import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentCalfResultBinding;
import com.botoni.flow.ui.state.CalfResultUiState;
import com.botoni.flow.ui.viewmodel.CalfResultViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalfResultFragment extends Fragment {
    private FragmentCalfResultBinding binding;
    private CalfResultViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalfResultBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(CalfResultViewModel.class);
    }

    private void initObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::bind);
    }

    private void bind(CalfResultUiState state) {
        if (!state.isVisible()) return;
        binding.textoValorPorCabeca.setText(formatCurrency(state.getValorPorCabeca()));
        binding.textoValorPorKg.setText(formatCurrency(state.getValorPorKg()));
        binding.textoValorTotal.setText(formatCurrency(state.getValorTotal()));
    }
}