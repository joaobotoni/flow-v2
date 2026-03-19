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
import com.botoni.flow.ui.state.FreteUiState;
import com.botoni.flow.ui.viewmodel.FreteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FreteFragmento extends Fragment {

    private FragmentFreightBinding binding;
    private FreteViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFreightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(FreteViewModel.class);
        viewModel.getVisivel().observe(getViewLifecycleOwner(), this::setVisivel);
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::bind);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setVisivel(boolean visivel) {
        binding.getRoot().setVisibility(visivel ? View.VISIBLE : View.GONE);
    }

    private void bind(FreteUiState state) {
        if (state == null) return;
        binding.textoValorFreteTotal.setText(formatCurrency(state.valorTotal));
        binding.textoValorFretePorAnimal.setText(formatCurrency(state.valorPorAnimal));
    }
}