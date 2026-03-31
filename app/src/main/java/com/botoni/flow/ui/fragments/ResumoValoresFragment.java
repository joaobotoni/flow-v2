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
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResumoValoresFragment extends Fragment {
    private static final String ARG_CHAVE = "chave";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_ROTULO_PRINCIPAL = "rotuloPrincipal";
    private static final String ARG_ROTULO_SECUNDARIO = "rotuloSecundario";
    private FragmentResumoValoresBinding binding;
    private ResumoValoresViewModel viewModel;

    public static ResumoValoresFragment newInstance(String chave, String titulo,
                                                    String rotuloPrincipal, String rotuloSecundario) {
        ResumoValoresFragment fragment = new ResumoValoresFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAVE, chave);
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_ROTULO_PRINCIPAL, rotuloPrincipal);
        args.putString(ARG_ROTULO_SECUNDARIO, rotuloSecundario);
        fragment.setArguments(args);
        return fragment;
    }

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
        inicializarViewModel();
        aplicarRotulos();
        configurarObservadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void inicializarViewModel() {
        String chave = requireArguments().getString(ARG_CHAVE);
        if (chave != null) {
            viewModel = new ViewModelProvider(requireActivity()).get(chave, ResumoValoresViewModel.class);
        }
    }

    private void aplicarRotulos() {
        Bundle args = requireArguments();
        binding.textoTituloSecao.setText(args.getString(ARG_TITULO));
        binding.textoRotuloPrincipal.setText(args.getString(ARG_ROTULO_PRINCIPAL));
        binding.textoRotuloSecundario.setText(args.getString(ARG_ROTULO_SECUNDARIO));
    }

    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::bind);
    }

    private void bind(ResumoValoresUiState state) {
        if (state == null) return;
        binding.textoValorPrincipal.setText(formatCurrency(state.getValorPrincipal()));
        binding.textoValorSecundario.setText(formatCurrency(state.getValorSecundario()));
    }
}