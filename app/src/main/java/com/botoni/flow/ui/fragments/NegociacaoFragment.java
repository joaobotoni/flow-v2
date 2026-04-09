package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.NumberHelper.formatInteger;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.isEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.isNotEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.orElse;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;
import static com.botoni.flow.ui.helpers.ViewHelper.setVisible;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentNegociacaoBinding;
import com.botoni.flow.ui.mappers.presentation.BezerroResumoMapper;
import com.botoni.flow.ui.mappers.presentation.FreteResumoMapper;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResultadoViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NegociacaoFragment extends Fragment {
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal AGIO = new BigDecimal("30");
    private static final String CHAVE_RESUMO_BEZERRO = "resumo_bezerro";
    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String CHAVE_RESUMO_COM_FRETE = "resumo_com_frete";
    private static final String CHAVE_RESULTADO_FINAL = "resultado_final";

    @Inject BezerroResumoMapper bezerroResumoMapper;
    @Inject FreteResumoMapper freteResumoMapper;

    private FragmentNegociacaoBinding binding;
    private TextWatcher entradaWatcher;
    private TextWatcher valorPorCabWatcher;
    private TextWatcher valorPorKgWatcher;

    private BigDecimal pesoUnitario;
    private int quantidade;
    private BigDecimal totalOriginal;
    private BigDecimal incidenciaFrete = BigDecimal.ZERO;

    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private ResumoValoresViewModel resumoBezerroViewModel;
    private ResumoValoresViewModel resumoFreteViewModel;
    private ResumoValoresViewModel resumoComFreteViewModel;
    private ResultadoViewModel resultadoFinalViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNegociacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciarSetup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void iniciarSetup() {
        instanciarViewModels();
        extrairArgumentosDeNavegacao();
        configurarComponentesIniciais();
        registrarEventos();
        configurarObservadores();
    }

    private void instanciarViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        bezerroViewModel = provider.get(PrecificacaoBezerroViewModel.class);
        freteViewModel = provider.get(PrecificacaoFreteViewModel.class);
        resumoBezerroViewModel = provider.get(CHAVE_RESUMO_BEZERRO, ResumoValoresViewModel.class);
        resumoFreteViewModel = provider.get(CHAVE_RESUMO_FRETE, ResumoValoresViewModel.class);
        resumoComFreteViewModel = provider.get(CHAVE_RESUMO_COM_FRETE, ResumoValoresViewModel.class);
        resultadoFinalViewModel = provider.get(CHAVE_RESULTADO_FINAL, ResultadoViewModel.class);
    }

    private void extrairArgumentosDeNavegacao() {
        NegociacaoFragmentArgs args = NegociacaoFragmentArgs.fromBundle(requireArguments());
        pesoUnitario = new BigDecimal(args.getPesoMedio());
        quantidade = args.getQuantidadeBezerros();
        atribuirTextoPesoMedio(formatCurrency(pesoUnitario));
        atribuirTextoQuantidade(formatInteger(quantidade));
        capturarTotalOriginal();
    }

    private void capturarTotalOriginal() {
    }

    private void configurarComponentesIniciais() {
        iniciarFragmentosEstaticos();
        preencherInputsComValoresDaPrecificacao();
    }

    private void iniciarFragmentosEstaticos() {
        substituirFragmento(R.id.layout_container_valor_bezerro, criarFragmentoResumoBezerroSemFrete());
        substituirFragmento(R.id.layout_container_valor_frete, criarFragmentoResumoFrete());
        substituirFragmento(R.id.layout_container_valor_bezerro_com_frete, criarFragmentoResumoBezerroComFrete());
        substituirFragmento(R.id.layout_container_valor_total_final, criarFragmentoResultadoFinal());
    }

    private void registrarEventos() {
        configurarTextWatcherInputs();
        configurarClickBotaoFinalizar();
    }

    private void configurarTextWatcherInputs() {
        entradaWatcher = SimpleTextWatcher(this::aoModificarValorFrete);
        valorPorCabWatcher = SimpleTextWatcher(this::aoModificarValorPorCab);
        valorPorKgWatcher = SimpleTextWatcher(this::aoModificarValorPorKg);
        binding.entradaTextoValorFrete.addTextChangedListener(entradaWatcher);
        binding.entradaTextoValorPorCab.addTextChangedListener(valorPorCabWatcher);
        binding.entradaTextoValorPorKg.addTextChangedListener(valorPorKgWatcher);
    }

    private void configurarClickBotaoFinalizar() {
        binding.botaoFinalizar.setOnClickListener(v -> executarNavegacaoDetalhes());
    }

    private void configurarObservadores() {
        observarCalculosFrete();
        observarCalculosBezerro();
        observarResumosUi();
        observarResultadoFinal();
    }

    private void observarCalculosFrete() {
        freteViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarEstadoResumoFrete);
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::processarIncidenciaFrete);
    }

    private void observarCalculosBezerro() {
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), estado -> {
            atualizarEstadoResumoComFrete(estado);
            atualizarEstadoResultadoFinal(estado);
        });

        bezerroViewModel.getStateComFrete().observe(getViewLifecycleOwner(), this::atualizarEstadoResumoBezerro);
    }

    private void preencherInputsComValoresDaPrecificacao() {
        preencherValorFrete();
        preencherValorPorCab();
        preencherValorPorKg();
    }

    private void preencherValorFrete() {
        ResumoValoresUiState frete = resumoFreteViewModel.getState().getValue();
        if (isEmpty(frete) || isEmpty(frete.getValorPrincipal())) return;
        setText(binding.entradaTextoValorFrete, formatCurrency(frete.getValorPrincipal()));
    }

    private void preencherValorPorCab() {
        ResumoValoresUiState bezerro = resumoBezerroViewModel.getState().getValue();
        if (isEmpty(bezerro) || isEmpty(bezerro.getValorPrincipal())) return;
        setText(binding.entradaTextoValorPorCab, formatCurrency(bezerro.getValorPrincipal()));
    }

    private void preencherValorPorKg() {
        ResumoValoresUiState bezerro = resumoBezerroViewModel.getState().getValue();
        if (isEmpty(bezerro) || isEmpty(bezerro.getValorSecundario())) return;
        setText(binding.entradaTextoValorPorKg, formatCurrency(bezerro.getValorSecundario()));
    }

    private void observarResumosUi() {
        resumoFreteViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarVisibilidadeContainerFrete);
        resumoBezerroViewModel.getState().observe(getViewLifecycleOwner(), this::processarAtualizacaoResumoBezerroSemFrete);
        resumoComFreteViewModel.getState().observe(getViewLifecycleOwner(), this::processarAtualizacaoResumoComFrete);
    }

    private void observarResultadoFinal() {
        resultadoFinalViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarVariacao);
    }

    private void aoModificarValorFrete() {
        if (!isResumed()) return;
        processarFluxoCalculosComFrete();
    }

    private void aoModificarValorPorCab() {
        if (!isResumed()) return;
        if (isValorPorCabPreenchido()) {
            processarOverrideValorPorCab();
        } else {
            limparCampoValorPorKg();
            restaurarCalculosBaseBezerro();
        }
    }

    private void aoModificarValorPorKg() {
        if (!isResumed()) return;
        if (isValorPorKgPreenchido()) {
            processarOverrideValorPorKg();
        } else {
            limparCampoValorPorCab();
            restaurarCalculosBaseBezerro();
        }
    }

    private void processarFluxoCalculosComFrete() {
        if (dadosIncompletosParaCalculo()) {
            limparResultadosFrete();
            return;
        }
        executarCalculosPrimarios();
    }

    private void executarCalculosPrimarios() {
        calcularValorBaseBezerro();
        calcularValorBaseFrete();
    }

    private void processarOverrideValorPorCab() {
        BigDecimal valorPorCab = lerValorPorCab();
        BigDecimal valorPorKg = calcularKgPorCabeca(valorPorCab);
        BigDecimal valorTotal = calcularTotalPorCab(valorPorCab);

        aplicarOverrideResumos(valorPorCab, valorPorKg, valorTotal);
        atualizarCampoValorPorKgSilenciosamente(valorPorKg);
    }

    private void processarOverrideValorPorKg() {
        BigDecimal valorPorKg = lerValorPorKg();
        BigDecimal valorPorCab = calcularCabecaPorKg(valorPorKg);
        BigDecimal valorTotal = calcularTotalPorCab(valorPorCab);

        aplicarOverrideResumos(valorPorCab, valorPorKg, valorTotal);
        atualizarCampoValorPorCabSilenciosamente(valorPorCab);
    }
    private void aplicarOverrideResumos(BigDecimal valorPorCab, BigDecimal valorPorKg, BigDecimal valorTotal) {
        resumoBezerroViewModel.setState(new ResumoValoresUiState(valorPorCab, valorPorKg));

        BigDecimal valorPorKgComFrete = valorPorKg.add(incidenciaFrete);
        BigDecimal valorPorCabComFrete = calcularCabecaPorKg(valorPorKgComFrete);
        BigDecimal valorTotalComFrete = calcularTotalPorCab(valorPorCabComFrete);

        resumoComFreteViewModel.setState(new ResumoValoresUiState(valorPorCabComFrete, valorPorKgComFrete));
        resultadoFinalViewModel.setState(valorTotalComFrete);
    }

    private void restaurarCalculosBaseBezerro() {
        calcularValorBaseBezerro();
        calcularValorBaseFrete();
    }

    private void atualizarEstadoResumoFrete(PrecificacaoFreteUiState estado) {
        resumoFreteViewModel.setState(isEmpty(estado) ? null : freteResumoMapper.mapper(estado));
    }

    private void processarIncidenciaFrete(BigDecimal incidencia) {
        if (incidenciaDeveSerIgnorada(incidencia)) {
            incidenciaFrete = BigDecimal.ZERO;
            limparResumoComFreteUiState();
            calcularBezerroComDescontoFrete(BigDecimal.ZERO);
            return;
        }
        incidenciaFrete = incidencia;
        calcularBezerroComDescontoFrete(incidencia);
    }

    private void calcularBezerroComDescontoFrete(BigDecimal incidencia) {
        bezerroViewModel.calcularNegociacao(pesoUnitario, ARROBA, AGIO, quantidade, incidencia);
    }

    private void atualizarEstadoResumoBezerro(PrecificacaoBezerroUiState estado) {
        resumoBezerroViewModel.setState(isEmpty(estado) ? null : bezerroResumoMapper.mapper(estado));
    }

    private void atualizarEstadoResumoComFrete(PrecificacaoBezerroUiState estado) {
        resumoComFreteViewModel.setState(isEmpty(estado) ? null : bezerroResumoMapper.mapper(estado));
    }

    private void atualizarEstadoResultadoFinal(PrecificacaoBezerroUiState estado) {
        resultadoFinalViewModel.setState(isEmpty(estado) ? null : estado.getValorTotal());
    }

    private void processarAtualizacaoResumoBezerroSemFrete(ResumoValoresUiState resumo) {
        atualizarVisibilidadeContainerSemFrete(resumo);
        atualizarVisibilidadeContainerResultado(resumo);
    }

    private void processarAtualizacaoResumoComFrete(ResumoValoresUiState resumo) {
        if (isFreteDeclarado()) {
            aplicarLayoutCenarioComFrete(resumo);
        } else {
            aplicarLayoutCenarioSemFrete();
        }
    }

    private void aplicarLayoutCenarioComFrete(ResumoValoresUiState resumo) {
        atualizarVisibilidadeContainerComFrete(resumo);
    }

    private void aplicarLayoutCenarioSemFrete() {
        setVisible(false, binding.layoutContainerValorBezerroComFrete);
    }

    private void atualizarVisibilidadeContainerFrete(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorFrete);
    }

    private void atualizarVisibilidadeContainerComFrete(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorBezerroComFrete);
    }

    private void atualizarVisibilidadeContainerSemFrete(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorBezerro);
    }

    private void atualizarVisibilidadeContainerResultado(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorTotalFinal);
    }

    private void atualizarVariacao(BigDecimal totalAtual) {
        if (isEmpty(totalOriginal) && isNotEmpty(totalAtual)) {
            totalOriginal = totalAtual;
        }
        if (isEmpty(totalOriginal) || isEmpty(totalAtual)) return;
        BigDecimal variacao = calcularVariacaoPercentual(totalOriginal, totalAtual);
        setText(binding.textoValorVariacao, formatCurrency(variacao));
    }

    private BigDecimal calcularVariacaoPercentual(BigDecimal original, BigDecimal atual) {
        return atual.subtract(original)
                .divide(original, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void executarNavegacaoDetalhes() {
        NavHostFragment.findNavController(this).navigate(
                NegociacaoFragmentDirections.actionNegociacaoFragmentToDetalhePrecificacaoFragment(quantidade));
    }

    private ResumoValoresFragment criarFragmentoResumoBezerroSemFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_BEZERRO,
                getString(R.string.titulo_resumo_bezerro),
                getString(R.string.rotulo_valor_final_por_cabeca),
                getString(R.string.rotulo_valor_final_por_kg));
    }

    private ResumoValoresFragment criarFragmentoResumoFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_FRETE,
                getString(R.string.titulo_resumo_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete));
    }

    private ResumoValoresFragment criarFragmentoResumoBezerroComFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_COM_FRETE,
                getString(R.string.titulo_resumo_com_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete));
    }

    private ResultadoFragment criarFragmentoResultadoFinal() {
        return ResultadoFragment.newInstance(CHAVE_RESULTADO_FINAL);
    }

    private void substituirFragmento(int containerId, Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
    }

    private void removerWatcherValorPorCab() {
        binding.entradaTextoValorPorCab.removeTextChangedListener(valorPorCabWatcher);
    }

    private void adicionarWatcherValorPorCab() {
        binding.entradaTextoValorPorCab.addTextChangedListener(valorPorCabWatcher);
    }

    private void removerWatcherValorPorKg() {
        binding.entradaTextoValorPorKg.removeTextChangedListener(valorPorKgWatcher);
    }

    private void adicionarWatcherValorPorKg() {
        binding.entradaTextoValorPorKg.addTextChangedListener(valorPorKgWatcher);
    }

    private void atualizarCampoValorPorCabSilenciosamente(BigDecimal valor) {
        removerWatcherValorPorCab();
        setText(binding.entradaTextoValorPorCab, formatCurrency(valor));
        adicionarWatcherValorPorCab();
    }

    private void atualizarCampoValorPorKgSilenciosamente(BigDecimal valor) {
        removerWatcherValorPorKg();
        setText(binding.entradaTextoValorPorKg, formatCurrency(valor));
        adicionarWatcherValorPorKg();
    }

    private void limparCampoValorPorCab() {
        removerWatcherValorPorCab();
        setText(binding.entradaTextoValorPorCab, "");
        adicionarWatcherValorPorCab();
    }

    private void limparCampoValorPorKg() {
        removerWatcherValorPorKg();
        setText(binding.entradaTextoValorPorKg, "");
        adicionarWatcherValorPorKg();
    }

    private BigDecimal lerValorFrete() {
        return orElse(getBigDecimal(binding.entradaTextoValorFrete), BigDecimal.ZERO);
    }

    private BigDecimal lerValorPorCab() {
        return getBigDecimal(binding.entradaTextoValorPorCab);
    }

    private BigDecimal lerValorPorKg() {
        return getBigDecimal(binding.entradaTextoValorPorKg);
    }

    private int calcularPesoTotalLote() {
        return quantidade * pesoUnitario.intValue();
    }

    private BigDecimal calcularTotalPorCab(BigDecimal valorPorCab) {
        return valorPorCab.multiply(BigDecimal.valueOf(quantidade));
    }

    private BigDecimal calcularCabecaPorKg(BigDecimal valorPorKg) {
        return valorPorKg.multiply(pesoUnitario);
    }

    private BigDecimal calcularKgPorCabeca(BigDecimal valorPorCab) {
        if (isEmpty(pesoUnitario) || pesoUnitario.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorPorCab.divide(pesoUnitario, 2, RoundingMode.HALF_UP);
    }

    private void calcularValorBaseBezerro() {
        bezerroViewModel.calcularNegociacaoComFrete(pesoUnitario, ARROBA, AGIO, quantidade);
    }

    private void calcularValorBaseFrete() {
        freteViewModel.calcularIncidencia(lerValorFrete(), calcularPesoTotalLote());
    }

    private void limparResumoComFreteUiState() {
        resumoComFreteViewModel.setState(null);
    }

    private void limparResultadosFrete() {
        freteViewModel.limpar();
        limparResumoComFreteUiState();
    }

    private boolean isFreteDeclarado() {
        return isNotEmpty(lerValorFrete());
    }

    private boolean isValorPorCabPreenchido() {
        return isNotEmpty(lerValorPorCab());
    }

    private boolean isValorPorKgPreenchido() {
        return isNotEmpty(lerValorPorKg());
    }

    private boolean dadosIncompletosParaCalculo() {
        return anyEmpty(pesoUnitario, quantidade);
    }

    private boolean incidenciaDeveSerIgnorada(BigDecimal incidencia) {
        return anyEmpty(incidencia, pesoUnitario, quantidade);
    }

    private void atribuirTextoPesoMedio(String texto) {
        setText(binding.textoValorPesoMedio, texto);
    }

    private void atribuirTextoQuantidade(String texto) {
        setText(binding.textoValorQuantidadeCabecas, texto);
    }
}