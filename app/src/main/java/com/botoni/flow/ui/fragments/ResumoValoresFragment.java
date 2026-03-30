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

import com.botoni.flow.databinding.FragmentResumoValoresBinding;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResumoValoresFragment extends Fragment {
    private FragmentResumoValoresBinding binding;
    private ResumoValoresViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResumoValoresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ResumoValoresViewModel.class);
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

    private void bind(ResumoValoresUiState state) {
        if (state == null) return;
        binding.textoValorPrincipal.setText(formatCurrency(state.getValorPrincipal()));
        binding.textoValorSecundario.setText(formatCurrency(state.getValorSecundario()));
    }
}