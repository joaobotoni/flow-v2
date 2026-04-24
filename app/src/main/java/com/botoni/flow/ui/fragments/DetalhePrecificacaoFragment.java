package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
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
import com.botoni.flow.ui.helpers.ViewHelper;
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
    private static final BigDecimal PESO_BASE = new BigDecimal("180");
    private static final String CHAVE_RESULTADO_DETALHE = "resultado_detalhe";
    private FragmentDetalhePrecificacaoBinding binding;
    private DetalhePrecificacaoAdapter adapter;
    private DetalhePrecificacaoViewModel viewModel;
    private ResultadoViewModel resultadoViewModel;
    private int quantidadeTotal;
    private String pesoMedio;
    private String valorTotalFrete;

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
        configurarEstadoInicial();
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

    private void configurarEstadoInicial() {
        extrairArgumentos();
        inicializarViewModels();
    }

    private void extrairArgumentos() {
        DetalhePrecificacaoFragmentArgs args = DetalhePrecificacaoFragmentArgs.fromBundle(requireArguments());
        quantidadeTotal = args.getQuantidadeBezerros();
        pesoMedio = args.getPesoMedio();
        valorTotalFrete = args.getValorTotalFrete();
    }

    private void inicializarViewModels() {
        ViewModelProvider activityProvider = new ViewModelProvider(requireActivity());
        viewModel = activityProvider.get(DetalhePrecificacaoViewModel.class);
        resultadoViewModel = activityProvider.get(CHAVE_RESULTADO_DETALHE, ResultadoViewModel.class);
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
        binding.botaoVoltar.setOnClickListener(v -> executarNavegacaoVoltar());
        binding.botaoContinuar.setOnClickListener(v -> onFinalizarClicado());
    }

    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::atualizarLista);
        viewModel.getTotal().observe(getViewLifecycleOwner(), resultadoViewModel::setState);
    }

    private void onAdicionarClicado() {
        if (campoPesoVazio()) return;
        if (listaCheia()) {
            showSnackBar(requireView(), getString(R.string.limite_bezerros_atingido));
            return;
        }
        viewModel.adicionarItem(lerPeso(), ARROBA, AGIO, PESO_BASE);
        limparCampoPeso();
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

    private boolean listaCheia() {
        return viewModel.size() >= quantidadeTotal;
    }

    private void atualizarLista(List<DetalhePrecoBezerroUiState> lista) {
        adapter.submitList(new ArrayList<>(lista));
        setText(binding.textoContagemBezerros, requireContext(), R.string.bezerros_contagem, lista.size(), quantidadeTotal);
        binding.barraProgresso.setProgressCompat(calcularProgresso(lista.size()), true);
    }

    private int calcularProgresso(int atual) {
        return quantidadeTotal == 0 ? 0 : (atual * 100) / quantidadeTotal;
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
        dialog.setOnConfirmListener(novoPeso -> viewModel.atualizarItem(detalhe.getId(), novoPeso, ARROBA, AGIO, PESO_BASE));
        dialog.show(getChildFragmentManager(), null);
    }

    private void onFinalizarClicado() {
        if (!listaValida()) return;
        if (!listaCompleta()) {
            showSnackBar(requireView(), getString(R.string.quantidade_incompleta));
            return;
        }
        navegarParaSucessoFragment();
    }

    private boolean listaValida() {
        List<DetalhePrecoBezerroUiState> lista = viewModel.getState().getValue();
        return lista != null && !lista.isEmpty();
    }

    private boolean listaCompleta() {
        return viewModel.size() == quantidadeTotal;
    }

    private void navegarParaSucessoFragment() {
        NavHostFragment.findNavController(this).navigate(
                DetalhePrecificacaoFragmentDirections
                        .actionDetalhePrecificacaoFragmentToSucessoFragment(quantidadeTotal, pesoMedio)
                        .setValorTotalFrete(valorTotalFrete)
                        .setOrigemDetalhe(true));
    }

    private void registrarCallbackVoltar() {
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        executarNavegacaoVoltar();
                    }
                });
    }

    private void executarNavegacaoVoltar() {
        NavHostFragment.findNavController(this).popBackStack();
    }
}
