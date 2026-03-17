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

import com.botoni.flow.databinding.FragmentFreightBinding;
import com.botoni.flow.ui.state.FreightUiState;
import com.botoni.flow.ui.viewmodel.FreightViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FreightFragment extends Fragment {
    private FragmentFreightBinding binding;
    private FreightViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFreightBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(FreightViewModel.class);
    }

    private void initObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::bind);
    }

    private void bind(FreightUiState state) {
        if (!state.isVisible()) return;
        binding.textoValorFreteTotal.setText(formatCurrency(state.getValorFreteTotal()));
        binding.textoValorFretePorAnimal.setText(formatCurrency(state.getValorFretePorAnimal()));
    }
}