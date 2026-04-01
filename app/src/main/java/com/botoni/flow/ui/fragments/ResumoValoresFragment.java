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
        ResumoValoresFragment novoFragment = criarNovoFragment();
        Bundle argumentos = construirArgumentos(chave, titulo, rotuloPrincipal, rotuloSecundario);
        novoFragment.setArguments(argumentos);
        return novoFragment;
    }

    private static ResumoValoresFragment criarNovoFragment() {
        return new ResumoValoresFragment();
    }

    private static Bundle construirArgumentos(String chave, String titulo, String rotuloPrincipal, String rotuloSecundario) {
        Bundle argumentos = novoBundle();
        adicionarChaveAoBundle(argumentos, chave);
        adicionarTituloAoBundle(argumentos, titulo);
        adicionarRotuloPrincipalAoBundle(argumentos, rotuloPrincipal);
        adicionarRotuloSecundarioAoBundle(argumentos, rotuloSecundario);
        return argumentos;
    }

    private static Bundle novoBundle() {
        return new Bundle();
    }

    private static void adicionarChaveAoBundle(Bundle bundle, String chave) {
        bundle.putString(ARG_CHAVE, chave);
    }

    private static void adicionarTituloAoBundle(Bundle bundle, String titulo) {
        bundle.putString(ARG_TITULO, titulo);
    }

    private static void adicionarRotuloPrincipalAoBundle(Bundle bundle, String rotuloPrincipal) {
        bundle.putString(ARG_ROTULO_PRINCIPAL, rotuloPrincipal);
    }

    private static void adicionarRotuloSecundarioAoBundle(Bundle bundle, String rotuloSecundario) {
        bundle.putString(ARG_ROTULO_SECUNDARIO, rotuloSecundario);
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
        String chaveViewModel = extrairChaveDoArgumento();
        if (temChaveValida(chaveViewModel)) {
            criarViewModelComChave(chaveViewModel);
        }
    }

    private String extrairChaveDoArgumento() {
        return requireArguments().getString(ARG_CHAVE);
    }

    private boolean temChaveValida(String chave) {
        return chave != null;
    }

    private void criarViewModelComChave(String chave) {
        ViewModelProvider providerDeViewModels = new ViewModelProvider(requireActivity());
        viewModel = providerDeViewModels.get(chave, ResumoValoresViewModel.class);
    }

    private void aplicarRotulos() {
        preencherTituloSecao();
        preencherRotuloPrincipal();
        preencherRotuloSecundario();
    }

    private void preencherTituloSecao() {
        String titulo = obterTituloDoArgumento();
        binding.textoTituloSecao.setText(titulo);
    }

    private String obterTituloDoArgumento() {
        return requireArguments().getString(ARG_TITULO);
    }

    private void preencherRotuloPrincipal() {
        String rotuloPrincipal = obterRotuloPrincipalDoArgumento();
        binding.textoRotuloPrincipal.setText(rotuloPrincipal);
    }

    private String obterRotuloPrincipalDoArgumento() {
        return requireArguments().getString(ARG_ROTULO_PRINCIPAL);
    }

    private void preencherRotuloSecundario() {
        String rotuloSecundario = obterRotuloSecundarioDoArgumento();
        binding.textoRotuloSecundario.setText(rotuloSecundario);
    }

    private String obterRotuloSecundarioDoArgumento() {
        return requireArguments().getString(ARG_ROTULO_SECUNDARIO);
    }

    private void configurarObservadores() {
        observarMudancasEstado();
    }

    private void observarMudancasEstado() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::aoEstadoAtualizado);
    }

    private void aoEstadoAtualizado(ResumoValoresUiState state) {
        if (naoTemEstadoValido(state)) return;
        preencherValoresDaTela(state);
    }

    private boolean naoTemEstadoValido(ResumoValoresUiState state) {
        return state == null;
    }

    private void preencherValoresDaTela(ResumoValoresUiState state) {
        preencherValorPrincipal(state);
        preencherValorSecundario(state);
    }

    private void preencherValorPrincipal(ResumoValoresUiState state) {
        String valorFormatado = formatCurrency(state.getValorPrincipal());
        binding.textoValorPrincipal.setText(valorFormatado);
    }

    private void preencherValorSecundario(ResumoValoresUiState state) {
        String valorFormatado = formatCurrency(state.getValorSecundario());
        binding.textoValorSecundario.setText(valorFormatado);
    }
}