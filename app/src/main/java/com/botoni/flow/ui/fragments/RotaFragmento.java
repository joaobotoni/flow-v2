package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentRouteBinding;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.viewmodel.RotaViewModel;

import java.util.Arrays;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RotaFragmento extends Fragment {

    private FragmentRouteBinding binding;
    private RotaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRouteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(RotaViewModel.class);
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

    private void bind(RotaUiState state) {
        if (state == null) return;
        bindPonto(state.pontos.get(0), binding.textoCidadeOrigem, binding.textoEstadoOrigem);
        bindPonto(state.pontos.get(1), binding.textoCidadeDestino, binding.textoEstadoDestino);
        binding.textoValorDistancia.setText(String.format(Locale.getDefault(), "%.2f", state.distancia));
    }

    private void bindPonto(String ponto, TextView cidade, TextView estado) {
        String[] partes = Arrays.stream(ponto.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        cidade.setText(partes[0]);
        estado.setText(partes[1]);
    }
}