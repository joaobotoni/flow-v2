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
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
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
    @Inject
    BezerroResumoMapper bezerroResumoMapper;
    @Inject
    FreteResumoMapper freteResumoMapper;

    private FragmentPrecificacaoBinding binding;
    private TextWatcher camposEntradaWatcher;
    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private TransporteViewModel transporteViewModel;
    private CategoriaAdapter categoriaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                .replace(R.id.layout_container_valor_frete, criarFragmentoResumoFrete())
                .replace(R.id.layout_container_valor_com_frete, criarFragmentoResumoComFrete())
                .replace(R.id.layout_container_valor_total_final, new ResultadoFragment())
                .commit();
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

    private void configurarObservadores() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(), this::aoCategoriaSelecionada);
        freteViewModel.getFreteSelecionado().observe(getViewLifecycleOwner(), this::aoFreteSelecionado);
        freteViewModel.getState().observe(getViewLifecycleOwner(), this::aoStateFreteAtualizado);
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::aoIncidenciaAtualizada);
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), this::aoStateBezerroAtualizado);
        bezerroViewModel.getStateComFrete().observe(getViewLifecycleOwner(), this::aoStateBezerroComFreteAtualizado);
        obterResumoViewModel(CHAVE_RESUMO_FRETE).getState().observe(getViewLifecycleOwner(), this::aoResumoFreteAtualizado);
        obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).getState().observe(getViewLifecycleOwner(), this::aoResumoComFreteAtualizado);
    }

    private void aoEntradasAlteradas() {
        if (!isResumed()) return;
        recomendarTransportesParaCategoria(categoriaViewModel.getCategoriaSelecionada().getValue());

        if (formularioIncompleto()) {
            limparResultados();
            return;
        }

        calcularNegociacaoBezerro();
        calcularIncidenciaFrete();
    }

    private void aoCategoriaSelecionada(CategoriaUiState categoria) {
        recomendarTransportesParaCategoria(categoria);
    }

    private void aoFreteSelecionado(BigDecimal valorFrete) {
        if (isEmpty(valorFrete)) return;
        binding.entradaTextoValorFrete.removeTextChangedListener(camposEntradaWatcher);
        setText(binding.entradaTextoValorFrete, formatCurrency(valorFrete));
        binding.entradaTextoValorFrete.addTextChangedListener(camposEntradaWatcher);
        if (!formularioIncompleto()) {
            calcularNegociacaoBezerro();
            calcularIncidenciaFrete();
        }
    }

    private void aoStateBezerroAtualizado(PrecificacaoBezerroUiState state) {
        publicarResumoBezerro(isEmpty(state) ? null : bezerroResumoMapper.mapper(state));
    }

    private void aoStateBezerroComFreteAtualizado(PrecificacaoBezerroUiState state) {
        publicarResumoComFrete(isEmpty(state) ? null : bezerroResumoMapper.mapper(state));
    }

    private void aoStateFreteAtualizado(PrecificacaoFreteUiState state) {
        publicarResumoFrete(isEmpty(state) ? null : freteResumoMapper.mapper(state));
    }

    private void aoIncidenciaAtualizada(BigDecimal incidenciaFretePorAnimal) {
        calcularBezerroComDescontoFrete(incidenciaFretePorAnimal);
    }

    private void aoResumoFreteAtualizado(ResumoValoresUiState state) {
        setVisible(isNotEmpty(state), binding.layoutContainerValorFrete);
    }

    private void aoResumoComFreteAtualizado(ResumoValoresUiState state) {
        setVisible(isNotEmpty(state), binding.layoutContainerValorComFrete);
        setVisible(isNotEmpty(state), binding.layoutContainerValorTotalFinal);
    }

    private void calcularNegociacaoBezerro() {
        bezerroViewModel.calcularNegociacao(
                obterPesoUnitarioPorAnimal(),
                VALOR_ARROBA_REFERENCIA,
                PERCENTUAL_RENDIMENTO_PADRAO,
                obterQuantidadeAnimais());
    }

    private void calcularIncidenciaFrete() {
        freteViewModel.calcularIncidencia(obterValorFrete(), obterPesoTotalDoLote());
    }

    private void calcularBezerroComDescontoFrete(BigDecimal incidenciaFretePorAnimal) {
        if (formularioIncompleto() || isEmpty(incidenciaFretePorAnimal)) {
            publicarResumoComFrete(null);
            return;
        }
        bezerroViewModel.calcularNegociacaoComDescontoDoFrete(
                obterPesoUnitarioPorAnimal(),
                VALOR_ARROBA_REFERENCIA,
                PERCENTUAL_RENDIMENTO_PADRAO,
                obterQuantidadeAnimais(),
                incidenciaFretePorAnimal);
    }

    private void recomendarTransportesParaCategoria(CategoriaUiState categoria) {
        if (isEmpty(categoria) || isEmpty(obterQuantidadeAnimais())) return;
        transporteViewModel.recomendar(categoria.getId(), obterQuantidadeAnimais());
    }

    private void limparResultados() {
        bezerroViewModel.limpar();
        freteViewModel.limpar();
        publicarResumoComFrete(null);
    }

    private void navegarParaSelecaoFrete() {
        CategoriaUiState categoria = categoriaViewModel.getCategoriaSelecionada().getValue();
        if (isEmpty(categoria)) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_field_category));
            return;
        }
        if (isEmpty(obterPesoUnitarioPorAnimal()) || isEmpty(obterQuantidadeAnimais())) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_invalid_input));
            return;
        }
        var direcao = PrecificacaoFragmentDirections
                .actionPrecificacaoFragmentToPrecificacaoFreteFragment(obterPesoTotalDoLote());
        NavHostFragment.findNavController(this).navigate(direcao);
    }

    private void publicarResumoBezerro(ResumoValoresUiState resumo) {
        obterResumoViewModel(CHAVE_RESUMO_BEZERRO).setState(resumo);
    }

    private void publicarResumoFrete(ResumoValoresUiState resumo) {
        obterResumoViewModel(CHAVE_RESUMO_FRETE).setState(resumo);
    }

    private void publicarResumoComFrete(ResumoValoresUiState resumo) {
        obterResumoViewModel(CHAVE_RESUMO_COM_FRETE).setState(resumo);
    }

    private BigDecimal obterPesoUnitarioPorAnimal() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private BigDecimal obterValorFrete() {
        return getBigDecimal(binding.entradaTextoValorFrete);
    }

    private int obterQuantidadeAnimais() {
        return getInt(binding.entradaTextoQuantidadeAnimais);
    }

    private int obterPesoTotalDoLote() {
        return obterQuantidadeAnimais() * obterPesoUnitarioPorAnimal().intValue();
    }

    private boolean formularioIncompleto() {
        return isEmpty(obterPesoUnitarioPorAnimal()) || isEmpty(obterQuantidadeAnimais()) || isEmpty(obterValorFrete());
    }

    private ResumoValoresViewModel obterResumoViewModel(String chave) {
        return new ViewModelProvider(requireActivity()).get(chave, ResumoValoresViewModel.class);
    }
}