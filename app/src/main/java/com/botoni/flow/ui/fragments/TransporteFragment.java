package com.botoni.flow.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentTransporteBinding;
import com.botoni.flow.ui.adapters.TransporteAdapter;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransporteFragment extends Fragment {

    private FragmentTransporteBinding binding;
    private TransporteViewModel viewModel;
    private TransporteAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransporteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TransporteViewModel.class);
        configurarAdapter();
        configurarObservadores();
    }

    private void configurarAdapter() {
        adapter = new TransporteAdapter();
        binding.listTransport.setAdapter(adapter);
    }

    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) {
                adapter.submitList(state);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}