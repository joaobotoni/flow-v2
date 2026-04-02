package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.isEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.isNotEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.orElse;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;
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
import com.botoni.flow.databinding.FragmentPrecificacaoBinding;
import com.botoni.flow.ui.adapters.CategoriaAdapter;
import com.botoni.flow.ui.helpers.AlertHelper;
import com.botoni.flow.ui.mappers.presentation.BezerroResumoMapper;
import com.botoni.flow.ui.mappers.presentation.FreteResumoMapper;
import com.botoni.flow.ui.state.CategoriaUiState;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.CategoriaViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResultadoViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFragment extends Fragment {

    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal AGIO = new BigDecimal("30");
    private static final String CHAVE_RESUMO_BEZERRO = "resumo_bezerro";
    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String CHAVE_RESUMO_COM_FRETE = "resumo_com_frete";
    private static final String CHAVE_RESULTADO_FINAL = "resultado_final";

    @Inject BezerroResumoMapper bezerroResumoMapper;
    @Inject FreteResumoMapper freteResumoMapper;

    private FragmentPrecificacaoBinding binding;
    private TextWatcher entradaWatcher;
    private CategoriaAdapter categoriaAdapter;

    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private TransporteViewModel transporteViewModel;
    private ResumoValoresViewModel resumoBezerroViewModel;
    private ResumoValoresViewModel resumoFreteViewModel;
    private ResumoValoresViewModel resumoComFreteViewModel;
    private ResultadoViewModel resultadoFinalViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrecificacaoBinding.inflate(inflater, container, false);
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
        configurarComponentesIniciais();
        registrarEventos();
        configurarObservadores();
    }

    private void instanciarViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        bezerroViewModel = provider.get(PrecificacaoBezerroViewModel.class);
        freteViewModel = provider.get(PrecificacaoFreteViewModel.class);
        categoriaViewModel = provider.get(CategoriaViewModel.class);
        transporteViewModel = provider.get(TransporteViewModel.class);
        resumoBezerroViewModel = provider.get(CHAVE_RESUMO_BEZERRO, ResumoValoresViewModel.class);
        resumoFreteViewModel = provider.get(CHAVE_RESUMO_FRETE, ResumoValoresViewModel.class);
        resumoComFreteViewModel = provider.get(CHAVE_RESUMO_COM_FRETE, ResumoValoresViewModel.class);
        resultadoFinalViewModel = provider.get(CHAVE_RESULTADO_FINAL, ResultadoViewModel.class);
    }

    private void configurarComponentesIniciais() {
        iniciarFragmentosEstaticos();
        iniciarAdapterCategorias();
    }

    private void iniciarFragmentosEstaticos() {
        substituirFragmento(R.id.layout_container_valor_frete, criarFragmentoResumoFrete());
        substituirFragmento(R.id.layout_container_valor_bezerro_com_frete, criarFragmentoResumoComFrete());
        substituirFragmento(R.id.layout_container_valor_total_final, criarFragmentoResultadoFinal());
    }

    private void iniciarAdapterCategorias() {
        categoriaAdapter = new CategoriaAdapter(this::aoSelecionarCategoriaNaLista);
        binding.listaCategorias.setAdapter(categoriaAdapter);
    }

    private void registrarEventos() {
        configurarTextWatcherInputs();
        configurarClickBotaoFrete();
        configurarClickBotaoProsseguir();
        configurarListenerResultadoFrete();
    }

    private void configurarTextWatcherInputs() {
        entradaWatcher = SimpleTextWatcher(this::aoModificarEntradaTexto);
        binding.entradaTextoPesoAnimal.addTextChangedListener(entradaWatcher);
        binding.entradaTextoValorFrete.addTextChangedListener(entradaWatcher);
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(entradaWatcher);
    }

    private void configurarClickBotaoFrete() {
        binding.botaoActionPrecificacaoFrete.setOnClickListener(v -> validarENavegarParaFrete());
    }

    private void configurarClickBotaoProsseguir() {
        binding.botaoProsseguir.setOnClickListener(v -> validarENavegarParaDetalhes());
    }

    private void configurarListenerResultadoFrete() {
        getParentFragmentManager().setFragmentResultListener(
                PrecificacaoFreteFragment.CHAVE_RESULTADO_FRETE,
                getViewLifecycleOwner(),
                (chave, bundle) -> aoReceberResultadoFrete(bundle));
    }

    private void configurarObservadores() {
        observarListaCategorias();
        observarCategoriaSelecionada();
        observarCalculosFrete();
        observarCalculosBezerro();
        observarResumosUi();
    }

    private void observarListaCategorias() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
    }

    private void observarCategoriaSelecionada() {
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(), this::processarRecomendacaoTransporte);
    }

    private void observarCalculosFrete() {
        freteViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarEstadoResumoFrete);
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::processarIncidenciaFrete);
    }

    private void observarCalculosBezerro() {
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), estado -> {
            atualizarEstadoResumoBezerro(estado);
            if (isFreteNaoDeclarado()) atualizarEstadoResultadoFinal(estado);
        });

        bezerroViewModel.getStateComFrete().observe(getViewLifecycleOwner(), estado -> {
            atualizarEstadoResumoComFrete(estado);
            if (isFreteDeclarado()) atualizarEstadoResultadoFinal(estado);
        });
    }

    private void observarResumosUi() {
        resumoFreteViewModel.getState().observe(getViewLifecycleOwner(), this::atualizarVisibilidadeContainerFrete);
        resumoBezerroViewModel.getState().observe(getViewLifecycleOwner(), this::processarAtualizacaoResumoBezerro);
        resumoComFreteViewModel.getState().observe(getViewLifecycleOwner(), this::processarAtualizacaoResumoComFrete);
    }

    private void aoModificarEntradaTexto() {
        if (!isResumed()) return;
        notificarMudancaRecomendacaoTransporte();
        processarFluxoCalculos();
    }

    private void notificarMudancaRecomendacaoTransporte() {
        processarRecomendacaoTransporte(categoriaViewModel.getCategoriaSelecionada().getValue());
    }

    private void processarFluxoCalculos() {
        if (dadosIncompletosParaCalculo()) {
            limparTodosResultados();
            return;
        }
        executarCalculosPrimarios();
    }

    private void executarCalculosPrimarios() {
        calcularValorBaseBezerro();
        calcularValorBaseFrete();
    }

    private void aoSelecionarCategoriaNaLista(CategoriaUiState categoria) {
        categoriaViewModel.selecionar(categoria);
    }

    private void processarRecomendacaoTransporte(CategoriaUiState categoria) {
        if (dadosIncompletosParaRecomendacao(categoria)) return;
        solicitarRecomendacaoTransporte(categoria.getId());
    }

    private void solicitarRecomendacaoTransporte(long categoriaId) {
        transporteViewModel.recomendar(categoriaId, lerQuantidade());
    }

    private void atualizarEstadoResumoFrete(PrecificacaoFreteUiState estado) {
        resumoFreteViewModel.setState(isEmpty(estado) ? null : freteResumoMapper.mapper(estado));
    }

    private void processarIncidenciaFrete(BigDecimal incidencia) {
        if (incidenciaDeveSerIgnorada(incidencia)) {
            limparResumoComFreteUiState();
            return;
        }
        calcularBezerroComDescontoFrete(incidencia);
    }

    private void calcularBezerroComDescontoFrete(BigDecimal incidencia) {
        bezerroViewModel.calcularNegociacaoComDescontoDoFrete(lerPesoUnitario(), ARROBA, AGIO, lerQuantidade(), incidencia);
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

    private void processarAtualizacaoResumoBezerro(ResumoValoresUiState resumo) {
        if (isFreteNaoDeclarado()) {
            aplicarLayoutCenarioSemFrete(resumo);
        }
        atualizarVisibilidadeContainerResultado(resumo);
    }

    private void processarAtualizacaoResumoComFrete(ResumoValoresUiState resumo) {
        if (isFreteDeclarado()) {
            aplicarLayoutCenarioComFrete(resumo);
        }
    }

    private void aplicarLayoutCenarioSemFrete(ResumoValoresUiState resumo) {
        substituirFragmento(R.id.layout_container_valor_bezerro_com_frete, criarFragmentoResumoBezerro());
        atualizarVisibilidadeContainerComFrete(resumo);
    }

    private void aplicarLayoutCenarioComFrete(ResumoValoresUiState resumo) {
        substituirFragmento(R.id.layout_container_valor_bezerro_com_frete, criarFragmentoResumoComFrete());
        atualizarVisibilidadeContainerComFrete(resumo);
    }

    private void atualizarVisibilidadeContainerFrete(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorFrete);
    }

    private void atualizarVisibilidadeContainerComFrete(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorBezerroComFrete);
    }

    private void atualizarVisibilidadeContainerResultado(ResumoValoresUiState resumo) {
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorTotalFinal);
    }

    private void aoReceberResultadoFrete(Bundle bundle) {
        String valorFreteStr = extrairValorFrete(bundle);
        if (isNotEmpty(valorFreteStr)) {
            processarNovoValorFrete(valorFreteStr);
        }
    }

    private void processarNovoValorFrete(String valorFreteStr) {
        String valorFormatado = formatCurrency(new BigDecimal(valorFreteStr));
        if (isValorFreteDiferente(valorFormatado)) {
            aplicarNovoValorFreteNaInterface(valorFormatado);
            processarFluxoCalculos();
        }
    }

    private void aplicarNovoValorFreteNaInterface(String novoValor) {
        removerWatcherFrete();
        atribuirTextoFrete(novoValor);
        adicionarWatcherFrete();
    }

    private void validarENavegarParaDetalhes() {
        if (falhaValidacaoCategoria()) return;
        if (falhaValidacaoEntradas()) return;
        executarNavegacaoDetalhes();
    }

    private void executarNavegacaoDetalhes() {
        NavHostFragment.findNavController(this).navigate(
                PrecificacaoFragmentDirections.actionPrecificacaoFragmentToDetalhePrecificacaoFragment(lerQuantidade()));
    }

    private void validarENavegarParaFrete() {
        if (falhaValidacaoCategoria()) return;
        if (falhaValidacaoEntradas()) return;
        executarNavegacaoFrete();
    }

    private boolean falhaValidacaoCategoria() {
        if (isCategoriaNaoSelecionada()) {
            exibirAlertaErro(R.string.error_field_category);
            return true;
        }
        return false;
    }

    private boolean falhaValidacaoEntradas() {
        if (dadosIncompletosParaCalculo()) {
            exibirAlertaErro(R.string.error_invalid_input);
            return true;
        }
        return false;
    }

    private void exibirAlertaErro(int mensagemId) {
        AlertHelper.showSnackBar(requireView(), getString(mensagemId));
    }

    private void executarNavegacaoFrete() {
        NavHostFragment.findNavController(this).navigate(
                PrecificacaoFragmentDirections.actionPrecificacaoFragmentToPrecificacaoFreteFragment(calcularPesoTotalLote()));
    }

    private void calcularValorBaseBezerro() {
        bezerroViewModel.calcularNegociacao(lerPesoUnitario(), ARROBA, AGIO, lerQuantidade());
    }

    private void calcularValorBaseFrete() {
        freteViewModel.calcularIncidencia(lerValorFrete(), calcularPesoTotalLote());
    }

    private void limparTodosResultados() {
        bezerroViewModel.limpar();
        freteViewModel.limpar();
        limparResumoComFreteUiState();
    }

    private void limparResumoComFreteUiState() {
        resumoComFreteViewModel.setState(null);
    }

    private void substituirFragmento(int containerId, Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
    }

    private ResumoValoresFragment criarFragmentoResumoFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_FRETE,
                getString(R.string.titulo_resumo_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete));
    }

    private ResumoValoresFragment criarFragmentoResumoComFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_COM_FRETE,
                getString(R.string.titulo_resumo_com_frete),
                getString(R.string.rotulo_valor_final_por_cabeca),
                getString(R.string.rotulo_valor_final_por_kg));
    }

    private ResumoValoresFragment criarFragmentoResumoBezerro() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_BEZERRO,
                getString(R.string.titulo_resumo_bezerro),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete));
    }

    private ResultadoFragment criarFragmentoResultadoFinal() {
        return ResultadoFragment.newInstance(CHAVE_RESULTADO_FINAL);
    }

    private void removerWatcherFrete() {
        binding.entradaTextoValorFrete.removeTextChangedListener(entradaWatcher);
    }

    private void adicionarWatcherFrete() {
        binding.entradaTextoValorFrete.addTextChangedListener(entradaWatcher);
    }

    private void atribuirTextoFrete(String texto) {
        setText(binding.entradaTextoValorFrete, texto);
    }

    private String extrairValorFrete(Bundle bundle) {
        return bundle.getString(PrecificacaoFreteFragment.EXTRA_VALOR_FRETE);
    }

    private BigDecimal lerPesoUnitario() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private int lerQuantidade() {
        return orElse(getInt(binding.entradaTextoQuantidadeAnimais), 0);
    }

    private BigDecimal lerValorFrete() {
        return orElse(getBigDecimal(binding.entradaTextoValorFrete), BigDecimal.ZERO);
    }

    private int calcularPesoTotalLote() {
        return lerQuantidade() * lerPesoUnitario().intValue();
    }

    private boolean isCategoriaNaoSelecionada() {
        return isEmpty(categoriaViewModel.getCategoriaSelecionada().getValue());
    }

    private boolean dadosIncompletosParaCalculo() {
        return anyEmpty(lerPesoUnitario(), lerQuantidade());
    }

    private boolean dadosIncompletosParaRecomendacao(CategoriaUiState categoria) {
        return anyEmpty(categoria, lerQuantidade());
    }

    private boolean incidenciaDeveSerIgnorada(BigDecimal incidencia) {
        return anyEmpty(incidencia, lerPesoUnitario(), lerQuantidade());
    }

    private boolean isFreteNaoDeclarado() {
        return isEmpty(lerValorFrete());
    }

    private boolean isFreteDeclarado() {
        return isNotEmpty(lerValorFrete());
    }

    private boolean isValorFreteDiferente(String novoValor) {
        return !novoValor.equals(requireText(binding.entradaTextoValorFrete));
    }
}