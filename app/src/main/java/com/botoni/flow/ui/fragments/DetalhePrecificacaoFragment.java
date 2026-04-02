package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentDetalhePrecificacaoBinding;
import com.botoni.flow.ui.adapters.DetalhePrecificacaoAdapter;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;
import com.botoni.flow.ui.viewmodel.DetalhePrecificacaoViewModel;
import com.botoni.flow.ui.viewmodel.ResultadoViewModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetalhePrecificacaoFragment extends Fragment {
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal AGIO = new BigDecimal("30");
    private static final String CHAVE_RESULTADO_DETALHE = "resultado_detalhe";
    private FragmentDetalhePrecificacaoBinding binding;
    private DetalhePrecificacaoAdapter adapter;
    private DetalhePrecificacaoViewModel viewModel;
    private ResultadoViewModel resultadoViewModel;
    private int quantidadeTotal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarCallbackVoltar();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalhePrecificacaoBinding.inflate(inflater, container, false);
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
        iniciarAdapterDetalhes();
        iniciarFragmentosEstaticos();
        registrarEventos();
        configurarObservadores();
    }

    private void instanciarViewModels() {
        quantidadeTotal = obterQuantidadeTotal();
        viewModel = new ViewModelProvider(this).get(DetalhePrecificacaoViewModel.class);
        resultadoViewModel = new ViewModelProvider(requireActivity())
                .get(CHAVE_RESULTADO_DETALHE, ResultadoViewModel.class);
    }

    private void iniciarAdapterDetalhes() {
        adapter = new DetalhePrecificacaoAdapter(getOnDetalheActionListener());
        binding.listaPrecoBezerros.setAdapter(adapter);
    }

    private void iniciarFragmentosEstaticos() {
        substituirFragmento(R.id.container_valor_final, criarFragmentoResultado());
    }

    private void registrarEventos() {
        configurarClickBotaoAdicionar();
    }

    private void configurarClickBotaoAdicionar() {
        binding.containerEntrada.setEndIconOnClickListener(v -> aoClicarAdicionar());
    }

    private void configurarObservadores() {
        observarListaDetalhes();
        observarValorTotal();
        observarItemParaEditar();
    }

    private void observarListaDetalhes() {
        viewModel.getLista().observe(getViewLifecycleOwner(), this::atualizarListaEProgresso);
    }

    private void observarValorTotal() {
        viewModel.getValorTotal().observe(getViewLifecycleOwner(), resultadoViewModel::setState);
    }

    private void observarItemParaEditar() {
        viewModel.getItemParaEditar().observe(getViewLifecycleOwner(), this::preencherCampoPeso);
    }

    private void aoClicarAdicionar() {
        if (pesoCampoVazio()) return;
        viewModel.adicionarItem(lerPeso(), ARROBA, AGIO);
        limparCampoPeso();
    }

    private void atualizarListaEProgresso(List<DetalhePrecoBezerroUiState> lista) {
        adapter.submitList(new ArrayList<>(lista));
        atualizarProgresso(lista.size());
    }

    private void atualizarProgresso(int atual) {
        atualizarTextoContagem(atual, quantidadeTotal);
        atualizarBarraProgresso(atual, quantidadeTotal);
    }

    private void atualizarTextoContagem(int atual, int total) {
        binding.textoContagemBezerros.setText(getString(R.string.bezerros_contagem, atual, total));
    }

    private void atualizarBarraProgresso(int atual, int total) {
        binding.barraProgresso.setProgressCompat(calcularPercentualProgresso(atual, total), true);
    }

    private DetalhePrecificacaoAdapter.OnDetalheActionListener getOnDetalheActionListener() {
        return new DetalhePrecificacaoAdapter.OnDetalheActionListener() {
            @Override public void onEdit(DetalhePrecoBezerroUiState detalhe) { viewModel.editarItem(detalhe); }
            @Override public void onRemove(int id) { viewModel.removerItem(id); }
        };
    }

    private void preencherCampoPeso(DetalhePrecoBezerroUiState detalhe) {
        if (detalhe != null) {
            setText(binding.entradaPesoBezerro, detalhe.getPeso().toPlainString());
        }
    }

    private void limparCampoPeso() {
        binding.entradaPesoBezerro.setText("");
    }

    private void substituirFragmento(int containerId, Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(containerId, fragment).commit();
    }

    private ResultadoFragment criarFragmentoResultado() {
        return ResultadoFragment.newInstance(CHAVE_RESULTADO_DETALHE);
    }

    private BigDecimal lerPeso() {
        return getBigDecimal(binding.entradaPesoBezerro);
    }

    private boolean pesoCampoVazio() {
        return anyEmpty(lerPeso());
    }

    private int obterQuantidadeTotal() {
        return DetalhePrecificacaoFragmentArgs.fromBundle(requireArguments()).getQuantidadeBezerros();
    }

    private int calcularPercentualProgresso(int atual, int total) {
        if (total == 0) return 0;
        return (atual * 100) / total;
    }

    private void voltar() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void registrarCallbackVoltar() {
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                voltar();
            }
        });
    }
}
