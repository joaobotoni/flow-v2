package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;

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
import com.botoni.flow.ui.helpers.FileHelper;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.helpers.ViewHelper;
import com.botoni.flow.ui.reports.PdfDetalhePrecificacaoBuilder;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;
import com.botoni.flow.ui.viewmodel.DetalhePrecificacaoViewModel;
import com.botoni.flow.ui.viewmodel.ResultadoViewModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetalhePrecificacaoFragment extends Fragment {
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal AGIO = new BigDecimal("30");
    private static final String CHAVE_RESULTADO_DETALHE = "resultado_detalhe";
    @Inject
    TaskHelper taskHelper;
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
        inicializarViewModels();
        inicializarAdapter();
        inicializarFragmentosEstaticos();
        configurarEventos();
        configurarObservadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void inicializarViewModels() {
        quantidadeTotal = DetalhePrecificacaoFragmentArgs.fromBundle(requireArguments()).getQuantidadeBezerros();
        viewModel = new ViewModelProvider(this).get(DetalhePrecificacaoViewModel.class);
        resultadoViewModel = new ViewModelProvider(requireActivity()).get(CHAVE_RESULTADO_DETALHE, ResultadoViewModel.class);
    }

    private void inicializarAdapter() {
        adapter = new DetalhePrecificacaoAdapter(criarListenerDeAcoes());
        binding.listaPrecoBezerros.setAdapter(adapter);
    }

    private void inicializarFragmentosEstaticos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_valor_final, ResultadoFragment.newInstance(CHAVE_RESULTADO_DETALHE))
                .commit();
    }

    private void configurarEventos() {
        binding.containerEntrada.setEndIconOnClickListener(v -> onAdicionarClicado());
        binding.botaoFinalizar.setOnClickListener(v -> onFinalizarClicado());
    }

    private void configurarObservadores() {
        viewModel.getLista().observe(getViewLifecycleOwner(), this::atualizarLista);
        viewModel.getValorTotal().observe(getViewLifecycleOwner(), resultadoViewModel::setState);
    }

    private void onAdicionarClicado() {
        if (campoPesoVazio()) return;
        viewModel.adicionarItem(lerPeso(), ARROBA, AGIO);
        limparCampoPeso();
    }

    private void onFinalizarClicado() {
        if (!listaValida()) return;
        gerarECompartilharPdf(capturarListaAtual(), capturarTotalAtual());
    }

    private void atualizarLista(List<DetalhePrecoBezerroUiState> lista) {
        adapter.submitList(new ArrayList<>(lista));
        binding.textoContagemBezerros.setText(getString(R.string.bezerros_contagem, lista.size(), quantidadeTotal));
        binding.barraProgresso.setProgressCompat(calcularProgresso(lista.size()), true);
    }

    private void gerarECompartilharPdf(List<DetalhePrecoBezerroUiState> lista, BigDecimal total) {
        taskHelper.execute(
                () -> PdfDetalhePrecificacaoBuilder.gerarRelatorioPrecificacao(requireContext(), lista, total),
                pdf -> {
                    if (isAdded())
                        FileHelper.compartilhar(requireActivity(), pdf, "application/pdf", getString(R.string.compartilhar_relatorio));
                },
                error -> {
                    if (isAdded())
                        showSnackBar(binding.getRoot(), getString(R.string.erro_gerar_pdf));
                }
        );
    }

    private DetalhePrecificacaoAdapter.OnDetalheActionListener criarListenerDeAcoes() {
        return new DetalhePrecificacaoAdapter.OnDetalheActionListener() {
            @Override
            public void onEdit(DetalhePrecoBezerroUiState detalhe) {
                abrirDialogEdicao(detalhe);
            }

            @Override
            public void onRemove(int id) {
                viewModel.removerItem(id);
            }
        };
    }

    private void abrirDialogEdicao(DetalhePrecoBezerroUiState detalhe) {
        DialogEdicaoPesoFragment dialog = DialogEdicaoPesoFragment.newInstance(detalhe);
        dialog.setOnConfirmListener(novoPeso ->
                viewModel.atualizarItem(detalhe.getId(), novoPeso, ARROBA, AGIO));
        dialog.show(getChildFragmentManager(), null);
    }

    private void registrarCallbackVoltar() {
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(DetalhePrecificacaoFragment.this).popBackStack();
                    }
                });
    }

    private boolean listaValida() {
        List<DetalhePrecoBezerroUiState> lista = viewModel.getLista().getValue();
        return lista != null && !lista.isEmpty();
    }

    private List<DetalhePrecoBezerroUiState> capturarListaAtual() {
        return new ArrayList<>(viewModel.getLista().getValue());
    }

    private BigDecimal capturarTotalAtual() {
        BigDecimal total = viewModel.getValorTotal().getValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    private int calcularProgresso(int atual) {
        return quantidadeTotal == 0 ? 0 : (atual * 100) / quantidadeTotal;
    }

    private BigDecimal lerPeso() {
        return getBigDecimal(binding.entradaPesoBezerro);
    }

    private boolean campoPesoVazio() {
        return anyEmpty(lerPeso());
    }

    private void limparCampoPeso() {
        ViewHelper.clearText(binding.entradaPesoBezerro);
    }
}