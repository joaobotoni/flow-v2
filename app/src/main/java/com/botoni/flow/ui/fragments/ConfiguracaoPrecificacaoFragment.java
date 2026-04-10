package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.isEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.os.Bundle;
import android.text.TextWatcher;
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
import com.botoni.flow.databinding.FragmentConfiguracaoPrecificacaoBinding;
import com.botoni.flow.ui.state.ConfiguracaoPrecificacaoUiState;
import com.botoni.flow.ui.viewmodel.ConfiguracaoPrecificacaoViewModel;

import java.math.BigDecimal;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ConfiguracaoPrecificacaoFragment extends Fragment {

    static final String CHAVE_RESULTADO_CONFIGURACAO = "resultado_configuracao_precificacao";
    static final String EXTRA_ARROBA_BOI_GORDO = "arroba_boi_gordo";
    static final String EXTRA_AGIO_BOI_GORDO = "agio_boi_gordo";
    static final String EXTRA_PESO_REF_BOI_GORDO = "peso_ref_boi_gordo";
    static final String EXTRA_ARROBA_VACA_GORDA = "arroba_vaca_gorda";
    static final String EXTRA_AGIO_VACA_GORDA = "agio_vaca_gorda";
    static final String EXTRA_PESO_REF_VACA_GORDA = "peso_ref_vaca_gorda";

    private FragmentConfiguracaoPrecificacaoBinding binding;
    private TextWatcher arrobaBoiGordoWatcher;
    private TextWatcher agioBoiGordoWatcher;
    private TextWatcher pesoRefBoiGordoWatcher;
    private TextWatcher arrobaVacaGordaWatcher;
    private TextWatcher agioVacaGordaWatcher;
    private TextWatcher pesoRefVacaGordaWatcher;
    private ConfiguracaoPrecificacaoViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarComportamentoBotaoVoltar();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfiguracaoPrecificacaoBinding.inflate(inflater, container, false);
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
        registrarEventos();
        configurarObservadores();
    }

    private void instanciarViewModels() {
        viewModel = new ViewModelProvider(this).get(ConfiguracaoPrecificacaoViewModel.class);
    }

    private void registrarEventos() {
        configurarTextWatchers();
        configurarEventosDeClique();
    }

    private void configurarTextWatchers() {
        criarWatchersBoiGordo();
        criarWatchersVacaGorda();
        adicionarTodosWatchers();
    }

    private void criarWatchersBoiGordo() {
        arrobaBoiGordoWatcher = SimpleTextWatcher(this::aoModificarCampo);
        agioBoiGordoWatcher = SimpleTextWatcher(this::aoModificarCampo);
        pesoRefBoiGordoWatcher = SimpleTextWatcher(this::aoModificarCampo);
    }

    private void criarWatchersVacaGorda() {
        arrobaVacaGordaWatcher = SimpleTextWatcher(this::aoModificarCampo);
        agioVacaGordaWatcher = SimpleTextWatcher(this::aoModificarCampo);
        pesoRefVacaGordaWatcher = SimpleTextWatcher(this::aoModificarCampo);
    }

    private void configurarEventosDeClique() {
        binding.botaoContinuar.setOnClickListener(v -> processarConfirmacao());
    }

    private void configurarObservadores() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::atualizarInterface);
    }

    private void atualizarInterface(ConfiguracaoPrecificacaoUiState estado) {
        if (isEmpty(estado)) return;
        preencherCamposSilenciosamente();
    }

    private void preencherCamposSilenciosamente() {
        removerTodosWatchers();
        adicionarTodosWatchers();
    }

    private void aoModificarCampo() {
        if (!isResumed()) return;
        salvarEstadoNoViewModel();
    }

    private void salvarEstadoNoViewModel() {
        viewModel.atualizarConfiguracao(
                lerArrobaBoiGordo(),
                lerAgioBoiGordo(),
                lerPesoRefBoiGordo(),
                lerArrobaVacaGorda(),
                lerAgioVacaGorda(),
                lerPesoRefVacaGorda());
    }

    private void processarConfirmacao() {
        if (camposIncompletos()) {
            exibirMensagemCamposObrigatorios();
            return;
        }
        enviarResultadoParaTelaAnterior();
        executarNavegacaoVoltar();
    }

    private boolean camposIncompletos() {
        return camposBoiGordoIncompletos() || camposVacaGordaIncompletos();
    }

    private boolean camposBoiGordoIncompletos() {
        return anyEmpty(lerArrobaBoiGordo(), lerAgioBoiGordo(), lerPesoRefBoiGordo());
    }

    private boolean camposVacaGordaIncompletos() {
        return anyEmpty(lerArrobaVacaGorda(), lerAgioVacaGorda(), lerPesoRefVacaGorda());
    }

    private void exibirMensagemCamposObrigatorios() {
        showSnackBar(requireView(), getString(R.string.subtitle_required));
    }

    private void enviarResultadoParaTelaAnterior() {
        Bundle resultado = criarBundleDeResultado();
        getParentFragmentManager().setFragmentResult(CHAVE_RESULTADO_CONFIGURACAO, resultado);
    }

    private Bundle criarBundleDeResultado() {
        Bundle bundle = new Bundle();
        adicionarDadosBoiGordo(bundle);
        adicionarDadosVacaGorda(bundle);
        return bundle;
    }

    private void adicionarDadosBoiGordo(Bundle bundle) {
        bundle.putString(EXTRA_ARROBA_BOI_GORDO, lerArrobaBoiGordo().toPlainString());
        bundle.putString(EXTRA_AGIO_BOI_GORDO, lerAgioBoiGordo().toPlainString());
        bundle.putString(EXTRA_PESO_REF_BOI_GORDO, lerPesoRefBoiGordo().toPlainString());
    }

    private void adicionarDadosVacaGorda(Bundle bundle) {
        bundle.putString(EXTRA_ARROBA_VACA_GORDA, lerArrobaVacaGorda().toPlainString());
        bundle.putString(EXTRA_AGIO_VACA_GORDA, lerAgioVacaGorda().toPlainString());
        bundle.putString(EXTRA_PESO_REF_VACA_GORDA, lerPesoRefVacaGorda().toPlainString());
    }

    private void configurarComportamentoBotaoVoltar() {
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

    private void adicionarTodosWatchers() {
        adicionarWatchersBoiGordo();
        adicionarWatchersVacaGorda();
    }

    private void adicionarWatchersBoiGordo() {
        binding.entradaTextoPrecoArroba.addTextChangedListener(arrobaBoiGordoWatcher);
        binding.entradaTextoAgioBoiGordo.addTextChangedListener(agioBoiGordoWatcher);
        binding.entradaTextoPesoReferenciaBoi.addTextChangedListener(pesoRefBoiGordoWatcher);
    }

    private void adicionarWatchersVacaGorda() {
        binding.entradaTextoPrecoArrobaVaca.addTextChangedListener(arrobaVacaGordaWatcher);
        binding.entradaTextoAgioVacaGorda.addTextChangedListener(agioVacaGordaWatcher);
        binding.entradaTextoPesoReferenciaVaca.addTextChangedListener(pesoRefVacaGordaWatcher);
    }

    private void removerTodosWatchers() {
        removerWatchersBoiGordo();
        removerWatchersVacaGorda();
    }

    private void removerWatchersBoiGordo() {
        binding.entradaTextoPrecoArroba.removeTextChangedListener(arrobaBoiGordoWatcher);
        binding.entradaTextoAgioBoiGordo.removeTextChangedListener(agioBoiGordoWatcher);
        binding.entradaTextoPesoReferenciaBoi.removeTextChangedListener(pesoRefBoiGordoWatcher);
    }

    private void removerWatchersVacaGorda() {
        binding.entradaTextoPrecoArrobaVaca.removeTextChangedListener(arrobaVacaGordaWatcher);
        binding.entradaTextoAgioVacaGorda.removeTextChangedListener(agioVacaGordaWatcher);
        binding.entradaTextoPesoReferenciaVaca.removeTextChangedListener(pesoRefVacaGordaWatcher);
    }

    private BigDecimal lerArrobaBoiGordo() {
        return getBigDecimal(binding.entradaTextoPrecoArroba);
    }

    private BigDecimal lerAgioBoiGordo() {
        return getBigDecimal(binding.entradaTextoAgioBoiGordo);
    }

    private BigDecimal lerPesoRefBoiGordo() {
        return getBigDecimal(binding.entradaTextoPesoReferenciaBoi);
    }

    private BigDecimal lerArrobaVacaGorda() {
        return getBigDecimal(binding.entradaTextoPrecoArrobaVaca);
    }

    private BigDecimal lerAgioVacaGorda() {
        return getBigDecimal(binding.entradaTextoAgioVacaGorda);
    }

    private BigDecimal lerPesoRefVacaGorda() {
        return getBigDecimal(binding.entradaTextoPesoReferenciaVaca);
    }
}
