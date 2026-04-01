package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.isEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.isNotEmpty;
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
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.viewmodel.CategoriaViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFragment extends Fragment {

    private static final BigDecimal VALOR_ARROBA_REFERENCIA = new BigDecimal("310");
    private static final BigDecimal PERCENTUAL_RENDIMENTO_PADRAO = new BigDecimal("30");
    private static final String CHAVE_RESUMO_BEZERRO = "resumo_bezerro";
    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String CHAVE_RESUMO_COM_FRETE = "resumo_com_frete";

    @Inject BezerroResumoMapper bezerroResumoMapper;
    @Inject FreteResumoMapper freteResumoMapper;

    private FragmentPrecificacaoBinding binding;
    private TextWatcher camposEntradaWatcher;
    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private TransporteViewModel transporteViewModel;
    private CategoriaAdapter categoriaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrecificacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializarViewModels();
        inicializarFragmentosFilhos();
        inicializarAdapterCategorias();
        configurarListeners();
        configurarResultadoFrete();
        configurarObservadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void inicializarViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        bezerroViewModel = provider.get(PrecificacaoBezerroViewModel.class);
        freteViewModel = provider.get(PrecificacaoFreteViewModel.class);
        categoriaViewModel = provider.get(CategoriaViewModel.class);
        transporteViewModel = provider.get(TransporteViewModel.class);
    }

    private void inicializarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valor_frete, criarResumoFrete())
                .replace(R.id.layout_container_valor_com_frete, criarResumoComFrete())
                .replace(R.id.layout_container_valor_total_final, new ResultadoFragment())
                .commit();
    }

    private void inicializarAdapterCategorias() {
        categoriaAdapter = new CategoriaAdapter(categoriaViewModel::selecionar);
        binding.listaCategorias.setAdapter(categoriaAdapter);
    }

    private void configurarListeners() {
        camposEntradaWatcher = SimpleTextWatcher(this::aoEntradasAlteradas);
        binding.entradaTextoPesoAnimal.addTextChangedListener(camposEntradaWatcher);
        binding.entradaTextoValorFrete.addTextChangedListener(camposEntradaWatcher);
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(camposEntradaWatcher);

        binding.botaoActionPrecificacaoFrete.setOnClickListener(v -> navegarParaSelecaoFrete());
    }

    private void configurarResultadoFrete() {
        getParentFragmentManager().setFragmentResultListener(
                PrecificacaoFreteFragment.CHAVE_RESULTADO_FRETE,
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    String valorStr = bundle.getString(PrecificacaoFreteFragment.EXTRA_VALOR_FRETE);
                    if (valorStr == null) return;
                    aoFreteSelecionado(new BigDecimal(valorStr));
                }
        );
    }

    private void configurarObservadores() {
        observarCategorias();
        observarFrete();
        observarBezerro();
    }

    private void observarCategorias() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(), this::recomendarTransportesParaCategoria);
    }

    private void observarFrete() {
        freteViewModel.getState().observe(getViewLifecycleOwner(), estadoFrete ->
                obterResumoViewModel(CHAVE_RESUMO_FRETE).setState(isEmpty(estadoFrete) ? null : freteResumoMapper.mapper(estadoFrete)));
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::aoIncidenciaAtualizada);
        obterResumoViewModel(CHAVE_RESUMO_FRETE).getState().observe(getViewLifecycleOwner(), estadoResumoFrete ->
                setVisible(isNotEmpty(estadoResumoFrete), binding.layoutContainerValorFrete));
    }

    private void observarBezerro() {
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), estadoBezerro ->
                obterResumoViewModel(CHAVE_RESUMO_BEZERRO).setState(isEmpty(estadoBezerro) ? null : bezerroResumoMapper.mapper(estadoBezerro)));
        bezerroViewModel.getStateComFrete().observe(getViewLifecycleOwner(), estadoBezerroComFrete ->
                obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).setState(isEmpty(estadoBezerroComFrete) ? null : bezerroResumoMapper.mapper(estadoBezerroComFrete)));
        obterResumoViewModel(CHAVE_RESUMO_BEZERRO).getState().observe(getViewLifecycleOwner(), this::aoResumoBezerroAtualizado);
        obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).getState().observe(getViewLifecycleOwner(), this::aoResumoComFreteAtualizado);
    }

    private void aoEntradasAlteradas() {
        if (!isResumed()) return;
        recomendarTransportesParaCategoria(categoriaViewModel.getCategoriaSelecionada().getValue());

        if (formularioIncompleto()) {
            bezerroViewModel.limpar();
            freteViewModel.limpar();
            obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).setState(null);
            return;
        }

        bezerroViewModel.calcularNegociacao(obterPesoUnitario(), VALOR_ARROBA_REFERENCIA, PERCENTUAL_RENDIMENTO_PADRAO, obterQuantidade());
        freteViewModel.calcularIncidencia(obterValorFrete(), obterQuantidade() * obterPesoUnitario().intValue());
    }

    private void aoFreteSelecionado(BigDecimal valorFrete) {
        if (isEmpty(valorFrete)) return;
        atualizarCampoFreteSeAlterado(formatCurrency(valorFrete));
        if (!formularioIncompleto()) {
            bezerroViewModel.calcularNegociacao(obterPesoUnitario(), VALOR_ARROBA_REFERENCIA, PERCENTUAL_RENDIMENTO_PADRAO, obterQuantidade());
            freteViewModel.calcularIncidencia(obterValorFrete(), obterQuantidade() * obterPesoUnitario().intValue());
        }
    }

    private void atualizarCampoFreteSeAlterado(String novoValor) {
        if (novoValor.equals(binding.entradaTextoValorFrete.getText().toString())) return;
        binding.entradaTextoValorFrete.removeTextChangedListener(camposEntradaWatcher);
        setText(binding.entradaTextoValorFrete, novoValor);
        binding.entradaTextoValorFrete.addTextChangedListener(camposEntradaWatcher);
    }

    private void aoIncidenciaAtualizada(BigDecimal incidenciaFrete) {
        if (formularioIncompleto() || isEmpty(incidenciaFrete) || incidenciaFrete.compareTo(BigDecimal.ZERO) == 0) {
            obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).setState(null);
            return;
        }
        bezerroViewModel.calcularNegociacaoComDescontoDoFrete(obterPesoUnitario(), VALOR_ARROBA_REFERENCIA, PERCENTUAL_RENDIMENTO_PADRAO, obterQuantidade(), incidenciaFrete);
    }

    private void aoResumoBezerroAtualizado(ResumoValoresUiState estadoResumo) {
        if (obterValorFrete().compareTo(BigDecimal.ZERO) == 0) {
            substituirFragmentoNoContainer(R.id.layout_container_valor_com_frete, criarResumoBezerro());
            setVisible(isNotEmpty(estadoResumo), binding.layoutContainerValorComFrete);
        }
        setVisible(isNotEmpty(estadoResumo), binding.layoutContainerValorTotalFinal);
    }

    private void aoResumoComFreteAtualizado(ResumoValoresUiState estadoResumo) {
        if (obterValorFrete().compareTo(BigDecimal.ZERO) > 0) {
            substituirFragmentoNoContainer(R.id.layout_container_valor_com_frete, criarResumoComFrete());
            setVisible(isNotEmpty(estadoResumo), binding.layoutContainerValorComFrete);
        }
    }

    private void recomendarTransportesParaCategoria(CategoriaUiState categoria) {
        if (isEmpty(categoria) || isEmpty(obterQuantidade())) return;
        transporteViewModel.recomendar(categoria.getId(), obterQuantidade());
    }

    private void navegarParaSelecaoFrete() {
        if (isEmpty(categoriaViewModel.getCategoriaSelecionada().getValue())) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_field_category));
            return;
        }
        if (formularioIncompleto()) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_invalid_input));
            return;
        }

        int pesoTotal = obterQuantidade() * obterPesoUnitario().intValue();
        NavHostFragment.findNavController(this).navigate(PrecificacaoFragmentDirections.actionPrecificacaoFragmentToPrecificacaoFreteFragment(pesoTotal));
    }

    private void substituirFragmentoNoContainer(int containerId, Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }

    private ResumoValoresFragment criarResumoFrete() {
        return ResumoValoresFragment.newInstance(CHAVE_RESUMO_FRETE, getString(R.string.titulo_resumo_frete), getString(R.string.card_label_total_value), getString(R.string.card_label_unit_value_frete));
    }

    private ResumoValoresFragment criarResumoComFrete() {
        return ResumoValoresFragment.newInstance(CHAVE_RESUMO_COM_FRETE, getString(R.string.titulo_resumo_com_frete), getString(R.string.rotulo_valor_final_por_cabeca), getString(R.string.rotulo_valor_final_por_kg));
    }

    private ResumoValoresFragment criarResumoBezerro() {
        return ResumoValoresFragment.newInstance(CHAVE_RESUMO_BEZERRO, getString(R.string.titulo_resumo_bezerro), getString(R.string.card_label_total_value), getString(R.string.card_label_unit_value_frete));
    }

    private BigDecimal obterPesoUnitario() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private BigDecimal obterValorFrete() {
        BigDecimal valor = getBigDecimal(binding.entradaTextoValorFrete);
        return isEmpty(valor) ? BigDecimal.ZERO : valor;
    }

    private int obterQuantidade() {
        return getInt(binding.entradaTextoQuantidadeAnimais);
    }

    private boolean formularioIncompleto() {
        return isEmpty(obterPesoUnitario()) || isEmpty(obterQuantidade());
    }

    private ResumoValoresViewModel obterResumoViewModel(String chave) {
        return new ViewModelProvider(requireActivity()).get(chave, ResumoValoresViewModel.class);
    }
}