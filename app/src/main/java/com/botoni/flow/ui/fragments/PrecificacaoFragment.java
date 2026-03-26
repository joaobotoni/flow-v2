package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.noneMatch;
import static com.botoni.flow.ui.helpers.ViewHelper.setVisible;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentPrecificacaoBinding;
import com.botoni.flow.ui.adapters.CategoriaAdapter;
import com.botoni.flow.ui.viewmodel.CategoriaViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;

import java.math.BigDecimal;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFragment extends Fragment {
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal PERCENT = new BigDecimal("30");
    private FragmentPrecificacaoBinding binding;
    private PrecificacaoBezerroViewModel precificacaoBezerroViewModel;
    private PrecificacaoFreteViewModel precificacaoFreteViewModel;
    private CategoriaViewModel categoriaViewModel;
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
        iniciarViewModels();
        iniciarFragmentosFilhos();
        iniciarAdapter();
        configurarObservadores();
        configurarWatchers();
        configurarNavegacao();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void iniciarViewModels() {
        precificacaoBezerroViewModel = new ViewModelProvider(requireActivity()).get(PrecificacaoBezerroViewModel.class);
        precificacaoFreteViewModel = new ViewModelProvider(requireActivity()).get(PrecificacaoFreteViewModel.class);
        categoriaViewModel = new ViewModelProvider(requireActivity()).get(CategoriaViewModel.class);
    }

    private void iniciarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valores, new ResultadoFragment())
                .commit();
    }

    private void iniciarAdapter() {
        categoriaAdapter = new CategoriaAdapter(categoriaViewModel::selecionar);
        binding.listaCategorias.setAdapter(categoriaAdapter);
    }

    private void configurarObservadores() {
        observarListaCategorias();
        observarCategoriaSelecionada();
        observarIncidenciaFrete();
        observarResultadoBezerro();
    }

    private void observarListaCategorias() {
        categoriaViewModel.getState().observe(getViewLifecycleOwner(), categoriaAdapter::submitList);
    }

    private void observarCategoriaSelecionada() {
        categoriaViewModel.getCategoriaSelecionada().observe(getViewLifecycleOwner(),
                categoria -> calcularBezerro(precificacaoFreteViewModel.getIncidencia().getValue()));
    }

    private void observarIncidenciaFrete() {
        precificacaoFreteViewModel.getIncidencia().observe(getViewLifecycleOwner(), this::calcularBezerro);
    }

    private void observarResultadoBezerro() {
        precificacaoBezerroViewModel.getState().observe(getViewLifecycleOwner(),
                state -> setVisible(state != null, binding.layoutContainerValores));
    }

    private void configurarWatchers() {
        binding.entradaTextoPesoAnimal.addTextChangedListener(SimpleTextWatcher(this::onPesoAlterado));
        binding.entradaTextoValorFrete.addTextChangedListener(SimpleTextWatcher(this::onFreteAlterado));
        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(SimpleTextWatcher(this::onQuantidadeAlterada));
    }

    private void onPesoAlterado() {
        calcularFrete();
        calcularBezerro(precificacaoFreteViewModel.getIncidencia().getValue());
    }

    private void onFreteAlterado() {
        calcularFrete();
        calcularBezerro(precificacaoFreteViewModel.getIncidencia().getValue());
    }

    private void onQuantidadeAlterada() {
        calcularFrete();
        calcularBezerro(precificacaoFreteViewModel.getIncidencia().getValue());
    }

    private void calcularFrete() {
        if (!camposFretePreenchidos()) {
            precificacaoFreteViewModel.limpar();
            return;
        }
        precificacaoFreteViewModel.calcularIncidencia(freteAtual(), pesoTotalAtual());
    }

    private void calcularBezerro(BigDecimal incidencia) {
        if (incidencia == null || !camposBezerroPreenchidos()) {
            precificacaoBezerroViewModel.limpar();
            return;
        }
        precificacaoBezerroViewModel.calcularNegociacaoBezerroComDescontoDoFrete(pesoAtual(), ARROBA, PERCENT, quantidadeAtual(), incidencia);
    }

    private boolean camposFretePreenchidos() {
        return noneMatch(binding.entradaTextoPesoAnimal, binding.entradaTextoValorFrete, binding.entradaTextoQuantidadeAnimais)
                && pesoAtual().compareTo(BigDecimal.ZERO) > 0
                && freteAtual().compareTo(BigDecimal.ZERO) > 0
                && quantidadeAtual() > 0;
    }

    private boolean camposBezerroPreenchidos() {
        return noneMatch(binding.entradaTextoPesoAnimal, binding.entradaTextoQuantidadeAnimais)
                && pesoAtual().compareTo(BigDecimal.ZERO) > 0
                && quantidadeAtual() > 0;
    }

    private BigDecimal pesoAtual() {
        return getBigDecimal(binding.entradaTextoPesoAnimal);
    }

    private BigDecimal freteAtual() {
        return getBigDecimal(binding.entradaTextoValorFrete);
    }

    private int quantidadeAtual() {
        return getInt(binding.entradaTextoQuantidadeAnimais);
    }

    private int pesoTotalAtual() {
        return quantidadeAtual() * pesoAtual().intValue();
    }

    private void configurarNavegacao() {
        binding.botaoActionPrecificacaoFrete.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_precificacaoFragment_to_precificacaoFreteFragment);
        });
    }
}