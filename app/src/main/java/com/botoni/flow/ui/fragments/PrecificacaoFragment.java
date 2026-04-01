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
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal AGIO = new BigDecimal("30");
    private static final String CHAVE_RESUMO_BEZERRO = "resumo_bezerro";
    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String CHAVE_RESUMO_COM_FRETE = "resumo_com_frete";
    @Inject
    BezerroResumoMapper bezerroResumoMapper;
    @Inject
    FreteResumoMapper freteResumoMapper;

    private FragmentPrecificacaoBinding binding;
    private TextWatcher entradaWatcher;

    private PrecificacaoBezerroViewModel bezerroViewModel;
    private PrecificacaoFreteViewModel freteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private TransporteViewModel transporteViewModel;
    private ResumoValoresViewModel resumoBezerroViewModel;
    private ResumoValoresViewModel resumoFreteViewModel;
    private ResumoValoresViewModel resumoComFreteViewModel;

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
        vincularViewModels();
        montarFragmentosFilhos();
        configurarListaDeCategorias();
        registrarListeners();
        registrarResultadoSelecaoFrete();
        observarEstadoDasTelas();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void vincularViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        bezerroViewModel = provider.get(PrecificacaoBezerroViewModel.class);
        freteViewModel = provider.get(PrecificacaoFreteViewModel.class);
        categoriaViewModel = provider.get(CategoriaViewModel.class);
        transporteViewModel = provider.get(TransporteViewModel.class);
        resumoBezerroViewModel = provider.get(CHAVE_RESUMO_BEZERRO, ResumoValoresViewModel.class);
        resumoFreteViewModel = provider.get(CHAVE_RESUMO_FRETE, ResumoValoresViewModel.class);
        resumoComFreteViewModel = provider.get(CHAVE_RESUMO_COM_FRETE, ResumoValoresViewModel.class);
    }

    private void montarFragmentosFilhos() {
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
                getString(R.string.card_label_unit_value_frete)
        );
    }

    private ResumoValoresFragment criarFragmentoResumoComFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_COM_FRETE,
                getString(R.string.titulo_resumo_com_frete),
                getString(R.string.rotulo_valor_final_por_cabeca),
                getString(R.string.rotulo_valor_final_por_kg)
        );
    }

    private void configurarListaDeCategorias() {
        categoriaAdapter = new CategoriaAdapter(categoriaViewModel::selecionar);
        binding.listaCategorias.setAdapter(categoriaAdapter);
    }

    private void registrarListeners() {
        entradaWatcher = SimpleTextWatcher(this::aoEntradaAlterada);
        binding.entradaTextoPesoAnimal.addTextChangedListener(entradaWatcher);
        binding.entradaTextoValorFrete.addTextChangedListener(entradaWatcher);
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(entradaWatcher);
        binding.botaoActionPrecificacaoFrete.setOnClickListener(v -> navegarParaSimulacaoFrete());
    }

    private void registrarResultadoSelecaoFrete() {
        getParentFragmentManager().setFragmentResultListener(
                PrecificacaoFreteFragment.CHAVE_RESULTADO_FRETE,
                getViewLifecycleOwner(),
                (chave, bundle) -> aoFreteSimuladoRecebido(bundle));
    }

    private void observarEstadoDasTelas() {
        observarCategorias();
        observarIncidenciaFrete();
        observarNegociacaoBezerro();
    }

    private void observarCategorias() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(), this::aoCategoriaSelecionada);
    }

    private void observarIncidenciaFrete() {
        freteViewModel.getState().observe(getViewLifecycleOwner(), estado ->
                resumoFreteViewModel.setState(isEmpty(estado) ? null : freteResumoMapper.mapper(estado)));
        freteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::aoIncidenciaFreteCalculada);
        resumoFreteViewModel.getState().observe(getViewLifecycleOwner(), resumo ->
                setVisible(isNotEmpty(resumo), binding.layoutContainerValorFrete));
    }

    private void observarNegociacaoBezerro() {
        observarCalculoBezerro();
        observarCalculoBezerroComFrete();
        observarResumoBezerro();
        observarResumoComFrete();
    }

    private void observarCalculoBezerro() {
        bezerroViewModel.getState().observe(getViewLifecycleOwner(), estado ->
                resumoBezerroViewModel.setState(isEmpty(estado) ? null : bezerroResumoMapper.mapper(estado)));
    }

    private void observarCalculoBezerroComFrete() {
        bezerroViewModel.getStateComFrete().observe(getViewLifecycleOwner(), estado ->
                resumoComFreteViewModel.setState(isEmpty(estado) ? null : bezerroResumoMapper.mapper(estado)));
    }

    private void observarResumoBezerro() {
        resumoBezerroViewModel.getState().observe(getViewLifecycleOwner(), this::aoResumoBezerroAtualizado);
    }

    private void observarResumoComFrete() {
        resumoComFreteViewModel.getState().observe(getViewLifecycleOwner(), this::aoResumoComFreteAtualizado);
    }

    private void aoEntradaAlterada() {
        if (!isResumed()) return;
        aoCategoriaSelecionada(categoriaViewModel.getCategoriaSelecionada().getValue());

        if (pesoOuQuantidadeAusentes()) {
            limparResultadosDeCalculo();
            return;
        }

        calcularNegociacaoBezerro();
        calcularIncidenciaFrete();
    }

    private void aoFreteSimuladoRecebido(Bundle bundle) {
        String valorStr = bundle.getString(PrecificacaoFreteFragment.EXTRA_VALOR_FRETE);
        if (valorStr == null) return;

        BigDecimal valorFrete = new BigDecimal(valorStr);
        preencherCampoFreteSeAlterado(formatCurrency(valorFrete));

        if (!pesoOuQuantidadeAusentes()) {
            calcularNegociacaoBezerro();
            calcularIncidenciaFrete();
        }
    }

    private void aoCategoriaSelecionada(CategoriaUiState categoria) {
        if (isEmpty(categoria) || isEmpty(lerQuantidade())) return;
        transporteViewModel.recomendar(categoria.getId(), lerQuantidade());
    }

    private void aoIncidenciaFreteCalculada(BigDecimal incidencia) {
        if (pesoOuQuantidadeAusentes() || isEmpty(incidencia) || incidencia.compareTo(BigDecimal.ZERO) == 0) {
            resumoComFreteViewModel.setState(null);
            return;
        }
        bezerroViewModel.calcularNegociacaoComDescontoDoFrete(lerPesoUnitario(), ARROBA, AGIO, lerQuantidade(), incidencia);
    }

    private void aoResumoBezerroAtualizado(ResumoValoresUiState resumo) {
        if (semFreteManual()) {
            exibirResumoBezerroNoContainerComFrete();
            setVisible(isNotEmpty(resumo), binding.layoutContainerValorComFrete);
        }
        setVisible(isNotEmpty(resumo), binding.layoutContainerValorTotalFinal);
    }

    private void aoResumoComFreteAtualizado(ResumoValoresUiState resumo) {
        if (comFreteManual()) {
            exibirResumoComFreteNoContainerComFrete();
            setVisible(isNotEmpty(resumo), binding.layoutContainerValorComFrete);
        }
    }

    private void navegarParaSimulacaoFrete() {
        if (isEmpty(categoriaViewModel.getCategoriaSelecionada().getValue())) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_field_category));
            return;
        }
        if (pesoOuQuantidadeAusentes()) {
            AlertHelper.showSnackBar(requireView(), getString(R.string.error_invalid_input));
            return;
        }
        int pesoTotalDoLote = lerQuantidade() * lerPesoUnitario().intValue();
        NavHostFragment.findNavController(this).navigate(
                PrecificacaoFragmentDirections.actionPrecificacaoFragmentToPrecificacaoFreteFragment(pesoTotalDoLote));
    }

    private void calcularNegociacaoBezerro() {
        bezerroViewModel.calcularNegociacao(lerPesoUnitario(), ARROBA, AGIO, lerQuantidade());
    }

    private void calcularIncidenciaFrete() {
        int pesoTotalDoLote = lerQuantidade() * lerPesoUnitario().intValue();
        freteViewModel.calcularIncidencia(lerValorFrete(), pesoTotalDoLote);
    }

    private void limparResultadosDeCalculo() {
        bezerroViewModel.limpar();
        freteViewModel.limpar();
        resumoComFreteViewModel.setState(null);
    }

    private void exibirResumoBezerroNoContainerComFrete() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valor_com_frete, ResumoValoresFragment.newInstance(
                        CHAVE_RESUMO_BEZERRO,
                        getString(R.string.titulo_resumo_bezerro),
                        getString(R.string.card_label_total_value),
                        getString(R.string.card_label_unit_value_frete)))
                .commit();
    }

    private void exibirResumoComFreteNoContainerComFrete() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valor_com_frete, ResumoValoresFragment.newInstance(
                        CHAVE_RESUMO_COM_FRETE,
                        getString(R.string.titulo_resumo_com_frete),
                        getString(R.string.rotulo_valor_final_por_cabeca),
                        getString(R.string.rotulo_valor_final_por_kg)))
                .commit();
    }

    private void preencherCampoFreteSeAlterado(String novoValor) {
        boolean jaPreenchido = novoValor.equals(binding.entradaTextoValorFrete.getText().toString());
        if (jaPreenchido) return;
        binding.entradaTextoValorFrete.removeTextChangedListener(entradaWatcher);
        setText(binding.entradaTextoValorFrete, novoValor);
        binding.entradaTextoValorFrete.addTextChangedListener(entradaWatcher);
    }

    private BigDecimal lerPesoUnitario() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private BigDecimal lerValorFrete() {
        BigDecimal valor = getBigDecimal(binding.entradaTextoValorFrete);
        return isEmpty(valor) ? BigDecimal.ZERO : valor;
    }

    private int lerQuantidade() {
        return getInt(binding.entradaTextoQuantidadeAnimais);
    }

    private boolean pesoOuQuantidadeAusentes() {
        return isEmpty(lerPesoUnitario()) || isEmpty(lerQuantidade());
    }

    private boolean semFreteManual() {
        return lerValorFrete().compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean comFreteManual() {
        return lerValorFrete().compareTo(BigDecimal.ZERO) > 0;
    }
}