package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentRotaBinding;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.viewmodel.RotaViewModel;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RotaFragment extends Fragment {

    private FragmentRotaBinding binding;
    private RotaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RotaViewModel.class);
        configurarObservadores();
    }
    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::bind);
    }
    private void bind(RotaUiState state) {
        if (state == null) return;
        binding.textoCidadeOrigem.setText(state.cidadeOrigem);
        binding.textoEstadoOrigem.setText(state.estadoOrigem);
        binding.textoCidadeDestino.setText(state.cidadeDestino);
        binding.textoEstadoDestino.setText(state.estadoDestino);
        binding.textoValorDistancia.setText(String.format(Locale.getDefault(), "%.2f km", state.distancia));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}