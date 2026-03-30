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
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFragment extends Fragment {
    private static final BigDecimal VALOR_ARROBA_PADRAO = new BigDecimal("310");
    private static final BigDecimal PERCENTUAL_PADRAO = new BigDecimal("30");
    private FragmentPrecificacaoBinding binding;
    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private TransporteViewModel transporteViewModel;
    private CategoriaAdapter categoriaAdapter;
    @Inject
    BezerroResumoMapper bezerroResumoMapper;
    @Inject
    FreteResumoMapper freteResumoMapper;


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
        inicializarWatchers();
        inicializarNavegacao();
        inicializarObservadores();
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
                .replace(R.id.layout_container_valor, new ResumoValoresFragment())
                .replace(R.id.layout_container_valor_frete, new ResumoValoresFragment())
                .replace(R.id.layout_container_valor_com_frete, new ResumoValoresFragment())
                .replace(R.id.layout_container_valor_final, new ResultadoFragment())
                .commit();
    }

    private void inicializarAdapterCategorias() {
        categoriaAdapter = new CategoriaAdapter(categoriaViewModel::selecionar);
        binding.listaCategorias.setAdapter(categoriaAdapter);
    }

    private void inicializarWatchers() {
        var watcher = SimpleTextWatcher(this::aoEntradasAlteradas);
        binding.entradaTextoPesoAnimal.addTextChangedListener(watcher);
        binding.entradaTextoValorFrete.addTextChangedListener(watcher);
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(watcher);
    }

    private void inicializarNavegacao() {
        binding.botaoActionPrecificacaoFrete.setOnClickListener(v -> navegarParaSelecaoFrete());
    }

    private void inicializarObservadores() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(), this::aoCategoriaSelecionada);
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::calcularComFreteBezerro);
        freteViewModel.getFreteSelecionado().observe(getViewLifecycleOwner(), this::aoFreteSelecionado);
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), state -> setVisible(isNotEmpty(state), binding.layoutContainerValor));
    }

    private void aoCategoriaSelecionada(CategoriaUiState categoria) {
        calcularTransporte(categoria);
        calcularComFreteBezerro(freteViewModel.getIncidencia().getValue());
    }

    private void aoEntradasAlteradas() {
        calcularFrete();
        calcularTransporte(categoriaViewModel.getCategoriaSelecionada().getValue());
        calcularComFreteBezerro(freteViewModel.getIncidencia().getValue());
    }

    private void aoFreteSelecionado(BigDecimal valor) {
        if (isEmpty(valor)) return;
        setText(binding.entradaTextoValorFrete, formatCurrency(valor));
        freteViewModel.consumirFreteSelecionado();
    }

    private void calcularFrete() {
        if (isDadosInsuficientes()) {
            freteViewModel.limpar();
            return;
        }
        freteViewModel.calcularIncidencia(obterFrete(), obterPesoTotal());
    }

    private void calcularComFreteBezerro(BigDecimal incidencia) {
        if (isDadosInsuficientes()) {
            bezerroViewModel.limpar();
            return;
        }
        bezerroViewModel.calcularNegociacaoBezerroComDescontoDoFrete(
                obterPesoUnitario(), VALOR_ARROBA_PADRAO, PERCENTUAL_PADRAO, obterQuantidade(), incidencia);
    }

    private void calcularBezerro() {
        if (isDadosInsuficientes()) {
            bezerroViewModel.limpar();
            return;
        }
        bezerroViewModel.calcularNegociacao(
                obterPesoUnitario(), VALOR_ARROBA_PADRAO, PERCENTUAL_PADRAO, obterQuantidade());
    }

    private void calcularTransporte(CategoriaUiState categoria) {
        if (isEmpty(categoria) || isEmpty(obterQuantidade())) return;
        transporteViewModel.recomendar(categoria.getId(), obterQuantidade());
    }

    private boolean isDadosInsuficientes() {
        return isEmpty(obterPesoUnitario()) || isEmpty(obterQuantidade());
    }

    private void navegarParaSelecaoFrete() {
        CategoriaUiState categoria = categoriaViewModel.getCategoriaSelecionada().getValue();
        if (isEmpty(categoria)) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_field_category));
            return;
        }
        var direcao = PrecificacaoFragmentDirections
                .actionPrecificacaoFragmentToPrecificacaoFreteFragment(obterPesoTotal());
        NavHostFragment.findNavController(this).navigate(direcao);
    }

    private BigDecimal obterPesoUnitario() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private BigDecimal obterFrete() {
        return getBigDecimal(binding.entradaTextoValorFrete);
    }

    private int obterQuantidade() {
        return getInt(binding.entradaTextoQuantidadeAnimais);
    }

    private int obterPesoTotal() {
        return obterQuantidade() * obterPesoUnitario().intValue();
    }

    private ResumoValoresUiState mapearValoresBezerro(PrecificacaoBezerroUiState precificacaoBezerroUiState){
        return bezerroResumoMapper.mapper(precificacaoBezerroUiState);
    }

    private ResumoValoresUiState mapearValoresFrete(PrecificacaoFreteUiState freteUiState) {
        return freteResumoMapper.mapper(freteUiState);
    }
}