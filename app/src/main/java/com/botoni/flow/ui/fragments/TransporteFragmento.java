package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentTransportBinding;
import com.botoni.flow.ui.adapters.TransporteAdapter;
import com.botoni.flow.ui.state.TransporteUiState;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransporteFragmento extends Fragment {

    private FragmentTransportBinding binding;
    private TransporteViewModel      viewModel;
    private TransporteAdapter        adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter   = new TransporteAdapter();
        viewModel = new ViewModelProvider(requireActivity()).get(TransporteViewModel.class);
        binding.listTransport.setAdapter(adapter);
        viewModel.getVisivel().observe(getViewLifecycleOwner(), this::setVisivel);
        viewModel.getUiState().observe(getViewLifecycleOwner(), adapter::submitList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setVisivel(boolean visivel) {
        binding.getRoot().setVisibility(visivel ? View.VISIBLE : View.GONE);
    }
}