package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SearchTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentSearchBinding;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.libs.BottomSheetFragment;
import com.botoni.flow.ui.viewmodel.BuscaViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BuscaLocalizacaoFragmento extends BottomSheetFragment {

    private static final String CHAVE_TECLADO = "state_keyboard_visible";

    private FragmentSearchBinding binding;
    private LocationAdapter adapter;
    private BuscaViewModel buscaViewModel;
    private RotaViewModel rotaViewModel;
    private boolean tecladoVisivel = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            tecladoVisivel = savedInstanceState.getBoolean(CHAVE_TECLADO, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buscaViewModel = new ViewModelProvider(requireActivity()).get(BuscaViewModel.class);
        rotaViewModel = new ViewModelProvider(requireActivity()).get(RotaViewModel.class);
        iniciarRecycler();
        iniciarWatcher();
        observarBusca();
        observarRota();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CHAVE_TECLADO, tecladoVisivel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    @Override
    protected View getBackgroundView() {
        return binding.getRoot();
    }

    @NonNull
    @Override
    protected View getBottomSheetView() {
        return binding.bottom;
    }

    @Override
    protected void onBind(@NonNull BottomSheetBehavior<View> behavior) {
        configurarBehavior(behavior);
        observarTeclado(behavior);
    }

    @Override
    protected void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN ||
                (newState == BottomSheetBehavior.STATE_DRAGGING && tecladoVisivel)) {
            esconderTeclado();
        }
    }

    @Override
    protected void onSlide(@NonNull View bottomSheet, float slideOffset) {
        atualizarAlpha(slideOffset);
        if (slideOffset < 0.5f && tecladoVisivel) esconderTeclado();
    }

    private void iniciarRecycler() {
        adapter = new LocationAdapter(rotaViewModel::selecionar);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void iniciarWatcher() {
        binding.textInputEditText.addTextChangedListener(SearchTextWatcher(this::onConsultaAlterada));
    }

    private void observarBusca() {
        buscaViewModel.getUiState().observe(getViewLifecycleOwner(), state ->
                adapter.submitList(state != null ? state.localizacoes : null));

        buscaViewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro == null) return;
            adapter.submitList(null);
            showSnackBar(requireView(), getString(R.string.error_search_address));
        });
    }

    private void observarRota() {
        rotaViewModel.getVisivel().observe(getViewLifecycleOwner(),
                visivel -> {
                    if (visivel) dismiss();
                });

        rotaViewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro == null) return;
            showSnackBar(requireView(), getString(R.string.error_search_address));
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void onConsultaAlterada() {
        String consulta = requireText(binding.textInputEditText);
        adapter.submitList(null);
        if (consulta.isEmpty()) buscaViewModel.limpar();
        else buscaViewModel.buscar(consulta);
    }

    private void onTecladoAlterado(boolean visivel, BottomSheetBehavior<View> behavior) {
        if (visivel == tecladoVisivel) return;
        tecladoVisivel = visivel;
        if (visivel && behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private void configurarBehavior(@NonNull BottomSheetBehavior<View> behavior) {
        int altura = getResources().getDisplayMetrics().heightPixels;
        behavior.setHideable(true);
        behavior.setFitToContents(false);
        behavior.setPeekHeight((int) (altura * 0.4f));
        behavior.setHalfExpandedRatio(0.8f);
        behavior.setExpandedOffset((int) (altura * 0.2f));
    }

    private void observarTeclado(@NonNull BottomSheetBehavior<View> behavior) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            onTecladoAlterado(insets.isVisible(WindowInsetsCompat.Type.ime()), behavior);
            return ViewCompat.onApplyWindowInsets(v, insets);
        });
    }

    private void atualizarAlpha(float slideOffset) {
        float alpha = Math.max(0f, Math.min(1f, slideOffset));
        binding.getRoot().setBackgroundColor(Color.argb((int) (alpha * 150), 0, 0, 0));
    }

    private void esconderTeclado() {
        WindowCompat.getInsetsController(requireActivity().getWindow(), binding.getRoot())
                .hide(WindowInsetsCompat.Type.ime());
    }
}