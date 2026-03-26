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

import com.botoni.flow.databinding.FragmentResultBinding;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResultadoFragment extends Fragment {
    private FragmentResultBinding binding;
    private PrecificacaoBezerroViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PrecificacaoBezerroViewModel.class);

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

    private void bind(PrecificacaoBezerroUiState state) {
        if (state == null) return;
        binding.textoValorPorCabeca.setText(formatCurrency(state.valorPorCabeca));
        binding.textoValorPorKg.setText(formatCurrency(state.valorPorKg));
        binding.textoValorTotal.setText(formatCurrency(state.valorTotal));
    }
}