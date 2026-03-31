package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SearchTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;

import android.Manifest;
import android.app.Dialog;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentBuscaBinding;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.state.BuscaLocalizacaoUiState;
import com.botoni.flow.ui.viewmodel.BuscaViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BuscaLocalizacaoFragment extends BottomSheetDialogFragment {
    private FragmentBuscaBinding binding;
    private BuscaViewModel buscaViewModel;
    private RotaViewModel rotaViewModel;
    private FusedLocationProviderClient fusedClient;
    private LocationAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBuscaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciarViewModels();
        iniciarRecyclerView();
        configurarInput();
        configurarObservadores();
    }

    @Override
    public void onStart() {
        super.onStart();
        configurarBottomSheet();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void iniciarViewModels() {
        buscaViewModel = new ViewModelProvider(this).get(BuscaViewModel.class);
        rotaViewModel = new ViewModelProvider(requireActivity()).get(RotaViewModel.class);
    }

    private void iniciarRecyclerView() {
        adapter = new LocationAdapter(this::onLocalizacaoSelecionada);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void configurarInput() {
        binding.textInputEditText.addTextChangedListener(SearchTextWatcher(this::buscar));
    }

    private void configurarObservadores() {
        buscaViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarListaLocalizacoes);
        buscaViewModel.getError().observe(getViewLifecycleOwner(), this::tratarErroBusca);
        rotaViewModel.getError().observe(getViewLifecycleOwner(), this::tratarErroRota);
    }

    private void atualizarListaLocalizacoes(@Nullable BuscaLocalizacaoUiState state) {
        adapter.submitList(state != null ? state.getLocalizacoes() : null);
    }

    private void tratarErroBusca(@Nullable Throwable erro) {
        if (erro == null) return;
        adapter.submitList(null);
        showSnackBar(requireView(), getString(R.string.error_search_address));
    }

    private void tratarErroRota(@Nullable Throwable erro) {
        if (erro == null) return;
        showSnackBar(requireView(), getString(R.string.error_search_address));
    }

    private void onLocalizacaoSelecionada(Address origem) {
        rotaViewModel.selecionar(origem);
        dismiss();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void buscar() {
        String texto = requireText(binding.textInputEditText);
        if (texto.isEmpty()) return;
        fusedClient.getLastLocation().addOnSuccessListener(this::executarBuscaComLocalizacao);
    }

    private void executarBuscaComLocalizacao(@Nullable Location location) {
        double lat = location != null ? location.getLatitude() : 0;
        double lng = location != null ? location.getLongitude() : 0;
        buscaViewModel.buscar(requireText(binding.textInputEditText), lat, lng);
    }

    private void configurarBottomSheet() {
        FrameLayout sheet = requireDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.5f));
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}