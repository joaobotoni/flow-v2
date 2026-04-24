package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.ViewHelper.orElse;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentSucessBinding;
import com.botoni.flow.ui.helpers.FileHelper;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.reports.PdfDetalhePrecificacaoBuilder;
import com.botoni.flow.ui.reports.PdfPrecificacaoBuilder;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.DetalhePrecificacaoViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResultadoViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SucessoFragment extends Fragment {

    private static final String CHAVE_RESULTADO_DETALHE = "resultado_detalhe";
    private static final String CHAVE_RESUMO_BEZERRO = "resumo_bezerro";
    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String CHAVE_RESUMO_COM_FRETE = "resumo_com_frete";
    private static final String CHAVE_RESULTADO_FINAL = "resultado_final";
    private static final String CHAVE_VIEWMODEL_SIMULACAO = "viewmodel_simulacao_frete";
    private FragmentSucessBinding binding;

    private DetalhePrecificacaoViewModel viewModel;
    private ResultadoViewModel resultadoViewModel;
    private ResumoValoresViewModel resumoNegociacaoViewModel;
    private ResumoValoresViewModel resumoFreteViewModel;
    private ResumoValoresViewModel resumoComFreteNegociacaoViewModel;
    private ResultadoViewModel resultadoNegociacaoViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private RotaViewModel rotaViewModel;
    private PrecificacaoBezerroViewModel bezerroViewModel;

    private int quantidadeTotal;
    private String pesoMedio;
    private String valorTotalFrete;
    private boolean isOrigemDetalhe;
    @Inject
    TaskHelper taskHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSucessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configurarEventos();
        configurarEstadoInicial();
        iniciarAnimacaoEntrada();
    }

    private void configurarEstadoInicial() {
        extrairArgumentos();
        inicializarViewModels();
    }

    private void extrairArgumentos() {
        SucessoFragmentArgs args = SucessoFragmentArgs.fromBundle(requireArguments());
        quantidadeTotal = args.getQuantidade();
        pesoMedio = args.getPesoMedio();
        valorTotalFrete = args.getValorTotalFrete();
        isOrigemDetalhe = args.getOrigemDetalhe();
    }

    private void inicializarViewModels() {
        ViewModelProvider activityProvider = new ViewModelProvider(requireActivity());
        viewModel = activityProvider.get(DetalhePrecificacaoViewModel.class);
        resultadoViewModel = activityProvider.get(CHAVE_RESULTADO_DETALHE, ResultadoViewModel.class);
        resumoNegociacaoViewModel = activityProvider.get(CHAVE_RESUMO_BEZERRO, ResumoValoresViewModel.class);
        resumoFreteViewModel = activityProvider.get(CHAVE_RESUMO_FRETE, ResumoValoresViewModel.class);
        resumoComFreteNegociacaoViewModel = activityProvider.get(CHAVE_RESUMO_COM_FRETE, ResumoValoresViewModel.class);
        resultadoNegociacaoViewModel = activityProvider.get(CHAVE_RESULTADO_FINAL, ResultadoViewModel.class);
        freteViewModel = activityProvider.get(CHAVE_VIEWMODEL_SIMULACAO, PrecificacaoFreteViewModel.class);
        rotaViewModel = activityProvider.get(RotaViewModel.class);
        bezerroViewModel = activityProvider.get(PrecificacaoBezerroViewModel.class);
    }

    private void onContinuarClicado() {
        limparViewModels();
        navegarParaTelaPrincipal();
    }

    private void onFinalizarClicado() {
        if (isOrigemDetalhe && !listaValida()) return;
        if (isOrigemDetalhe && !listaCompleta()) {
            showSnackBar(requireView(), getString(R.string.quantidade_incompleta));
            return;
        }
        gerarECompartilharPdf();
    }

    private void limparViewModels() {
        viewModel.limpar();
        resultadoViewModel.limpar();
        resumoNegociacaoViewModel.limpar();
        resumoFreteViewModel.limpar();
        resumoComFreteNegociacaoViewModel.limpar();
        resultadoNegociacaoViewModel.limpar();
        freteViewModel.limpar();
        rotaViewModel.limpar();
        bezerroViewModel.limpar();
    }

    private void navegarParaTelaPrincipal() {
        NavHostFragment.findNavController(this).popBackStack(R.id.precificacaoFragment, false);
    }

    private void gerarECompartilharPdf() {
        taskHelper.execute(this::construirPdfs, this::compartilharPdfs, this::tratarErroPdf);
    }

    private void compartilharPdfs(List<File> pdfs) {
        if (isAdded()) {
            FileHelper.compartilharMultiplos(requireActivity(), pdfs, "application/pdf", getString(R.string.compartilhar_relatorio));
        }
    }

    private void tratarErroPdf(Exception error) {
        if (isAdded()) showSnackBar(binding.getRoot(), getString(R.string.erro_gerar_pdf));
    }

    private List<File> construirPdfs() throws IOException {
        List<File> pdfs = new ArrayList<>();
        pdfs.add(builderPrecificacaoPdf());
        if (isOrigemDetalhe) {
            pdfs.add(builderDetalhePrecificacaoPdf());
        }
        return pdfs;
    }

    private File builderPrecificacaoPdf() throws IOException {
        return PdfPrecificacaoBuilder.gerarCapaNegociacao(
                requireContext(),
                quantidadeTotal,
                lerPesoMedio(),
                lerValorPorKgBezerro(),
                lerValorPorCabecaBezerro(),
                lerValorPorKgComFrete(),
                lerValorPorCabecaComFrete(),
                orElse(lerTotalNegociacao(), BigDecimal.ZERO),
                lerIncidenciaFrete(),
                lerValorTotalFrete(),
                lerDistanciaRota(),
                rotaViewModel.getState().getValue()
        );
    }

    private File builderDetalhePrecificacaoPdf() throws IOException {
        return PdfDetalhePrecificacaoBuilder.gerarRelatorioPrecificacao(
                requireContext(),
                capturarListaAtual(),
                capturarTotalAtual()
        );
    }


    private List<DetalhePrecoBezerroUiState> capturarListaAtual() {
        return new ArrayList<>(orElse(viewModel.getState().getValue(), Collections.emptyList()));
    }

    private BigDecimal capturarTotalAtual() {
        return orElse(viewModel.getTotal().getValue(), BigDecimal.ZERO);
    }

    private BigDecimal lerPesoMedio() {
        return new BigDecimal(pesoMedio);
    }

    private BigDecimal lerValorTotalFrete() {
        return new BigDecimal(valorTotalFrete);
    }

    private BigDecimal lerIncidenciaFrete() {
        return orElse(freteViewModel.getIncidencia().getValue(), BigDecimal.ZERO);
    }

    private double lerDistanciaRota() {
        return orElse(freteViewModel.getDistancia().getValue(), 0.0);
    }

    private BigDecimal lerValorPorCabecaBezerro() {
        return lerValorPrincipalDeResumo((orElse(resumoNegociacaoViewModel.getState().getValue(),
                new ResumoValoresUiState(new BigDecimal("0.0"), new BigDecimal("0.0")))));
    }

    private BigDecimal lerValorPorKgBezerro() {
        return lerValorSecundarioDeResumo((orElse(resumoNegociacaoViewModel.getState().getValue(),
                new ResumoValoresUiState(new BigDecimal("0.0"), new BigDecimal("0.0")))));
    }

    private BigDecimal lerValorPorCabecaComFrete() {
        return lerValorPrincipalDeResumo(orElse(resumoComFreteNegociacaoViewModel.getState().getValue(),
                new ResumoValoresUiState(new BigDecimal("0.0"), new BigDecimal("0.0"))));
    }

    private BigDecimal lerValorPorKgComFrete() {
        return lerValorSecundarioDeResumo(orElse(resumoComFreteNegociacaoViewModel.getState().getValue(),
                new ResumoValoresUiState(new BigDecimal("0.0"), new BigDecimal("0.0"))));
    }

    private BigDecimal lerTotalNegociacao() {
        return resultadoNegociacaoViewModel.getState().getValue();
    }

    private BigDecimal lerValorPrincipalDeResumo(ResumoValoresUiState resumoValoresUiState) {
        return orElse(resumoValoresUiState.getValorPrincipal(), BigDecimal.ZERO);
    }

    private BigDecimal lerValorSecundarioDeResumo(ResumoValoresUiState resumoValoresUiState) {
        return orElse(resumoValoresUiState.getValorSecundario(), BigDecimal.ZERO);
    }

    private boolean listaValida() {
        List<DetalhePrecoBezerroUiState> lista = viewModel.getState().getValue();
        return lista != null && !lista.isEmpty();
    }

    private boolean listaCompleta() {
        return viewModel.size() == quantidadeTotal;
    }

    private void iniciarAnimacaoEntrada() {
        binding.cardIconeSucess.setScaleX(0f);
        binding.cardIconeSucess.setScaleY(0f);
        binding.haloMaior.setScaleX(0f);
        binding.haloMaior.setScaleY(0f);
        binding.haloMenor.setScaleX(0f);
        binding.haloMenor.setScaleY(0f);

        binding.textoTituloSucess.setAlpha(0f);
        binding.textoTituloSucess.setTranslationY(40f);
        binding.textoDescricaoSucess.setAlpha(0f);
        binding.textoDescricaoSucess.setTranslationY(40f);

        binding.cardIconeSucess.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .withEndAction(this::iniciarLoopsInfinitos)
                .start();

        binding.haloMaior.animate().scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(100).start();
        binding.haloMenor.animate().scaleX(1f).scaleY(1f).setDuration(700).setStartDelay(50).start();

        binding.textoTituloSucess.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.textoDescricaoSucess.animate()
                .alpha(0.7f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(450)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void iniciarLoopsInfinitos() {
        aplicarEfeitoPulso(binding.cardIconeSucess, 1.05f, 1200, 0);
        aplicarEfeitoPulso(binding.haloMenor, 1.08f, 1500, 150);
        aplicarEfeitoPulso(binding.haloMaior, 1.12f, 2000, 300);
    }

    private void aplicarEfeitoPulso(View view, float escalaFinal, int duracao, int delay) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, escalaFinal);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, escalaFinal);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(duracao);
        set.setStartDelay(delay);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    private void configurarEventos() {
        binding.botaoConcluir.setOnClickListener(v -> onContinuarClicado());
        binding.botaoGerarRelatorioEFinalizar.setOnClickListener(v -> onFinalizarClicado());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}