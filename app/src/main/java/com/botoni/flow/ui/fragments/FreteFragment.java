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

import com.botoni.flow.databinding.FragmentFreteBinding;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FreteFragment extends Fragment {

    private FragmentFreteBinding binding;
    private PrecificacaoFreteViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFreteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PrecificacaoFreteViewModel.class);

        configurarObservadores();
    }

    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::bind);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void bind(PrecificacaoFreteUiState state) {
        if (state == null) return;
        binding.textoValorFretePorAnimal.setText(formatCurrency(state.getValorParcial()));
        binding.textoValorFreteTotal.setText(formatCurrency(state.getValorTotal()));
    }
}