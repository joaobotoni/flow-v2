package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.register;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.clearText;
import static com.botoni.flow.ui.helpers.ViewHelper.getDouble;
import static com.botoni.flow.ui.helpers.ViewHelper.isEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.isNotEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;
import static com.botoni.flow.ui.helpers.ViewHelper.setVisible;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentPrecificacaoFreteBinding;
import com.botoni.flow.ui.mappers.domain.TransporteMapper;
import com.botoni.flow.ui.mappers.presentation.FreteResumoMapper;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFreteFragment extends Fragment {

    private static final String CHAVE_RESUMO_SIMULACAO_FRETE = "resumo_simulacao_frete";
    private static final String CHAVE_VIEWMODEL_SIMULACAO = "viewmodel_simulacao_frete";
    static final String CHAVE_RESULTADO_FRETE = "resultado_selecao_frete";
    static final String EXTRA_VALOR_FRETE = "valor_frete";
    private static final String[] PERMISSOES_LOCALIZACAO = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Inject
    FreteResumoMapper freteResumoMapper;
    @Inject
    TransporteMapper transporteMapper;

    private FragmentPrecificacaoFreteBinding binding;
    private TextWatcher distanciaWatcher;
    private ActivityResultLauncher<String[]> permissaoLauncher;

    private PrecificacaoFreteViewModel simulacaoFreteViewModel;
    private RotaViewModel rotaViewModel;
    private TransporteViewModel transporteViewModel;
    private ResumoValoresViewModel resumoSimulacaoFreteViewModel;

    private int cargaTotalDoLote;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarLauncherDePermissao();
        registrarCallbackVoltar();
    }

    @Override
    public void onStart() {
        super.onStart();
        solicitarPermissoesDeLocalizacaoSeNecessario();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrecificacaoFreteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lerArgumentosDeNavegacao();
        vincularViewModels();
        montarFragmentosFilhos();
        registrarListeners();
        observarEstadoDasTelas();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restaurarDistanciaSalvaSePossivel();
        sincronizarVisibilidadeComEstadoAtual();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void registrarLauncherDePermissao() {
        permissaoLauncher = register(this, (concedida, result) -> {
            if (!concedida) tratarPermissaoNegada();
        });
    }

    private void registrarCallbackVoltar() {
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(PrecificacaoFreteFragment.this).popBackStack();
            }
        });
    }

    private void solicitarPermissoesDeLocalizacaoSeNecessario() {
        if (!hasPermissions(requireContext(), PERMISSOES_LOCALIZACAO)) {
            request(requireContext(), permissaoLauncher, PERMISSOES_LOCALIZACAO);
        }
    }

    private void lerArgumentosDeNavegacao() {
        cargaTotalDoLote = PrecificacaoFreteFragmentArgs.fromBundle(requireArguments()).getCargaTotal();
    }

    private void vincularViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        rotaViewModel = provider.get(RotaViewModel.class);
        transporteViewModel = provider.get(TransporteViewModel.class);
        simulacaoFreteViewModel = provider.get(CHAVE_VIEWMODEL_SIMULACAO, PrecificacaoFreteViewModel.class);
        resumoSimulacaoFreteViewModel = provider.get(CHAVE_RESUMO_SIMULACAO_FRETE, ResumoValoresViewModel.class);
    }

    private void montarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_rota, new RotaFragment())
                .replace(R.id.layout_container_transporte, new TransporteFragment())
                .replace(R.id.layout_container_frete, criarFragmentoResumoSimulacaoFrete())
                .commit();
    }

    private ResumoValoresFragment criarFragmentoResumoSimulacaoFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_SIMULACAO_FRETE,
                getString(R.string.titulo_resumo_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete)
        );
    }

    private void registrarListeners() {
        binding.cartaoExplorarRota.setOnClickListener(v -> abrirBuscaDeLocalizacao());
        binding.botaoVoltar.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.botaoContinuar.setOnClickListener(v -> confirmarSelecaoFrete());

        distanciaWatcher = SimpleTextWatcher(this::aoDistanciaManualAlterada);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void observarEstadoDasTelas() {
        observarRota();
        observarTransporte();
        observarFreteCalculado();
    }

    private void observarRota() {
        rotaViewModel.getState().observe(getViewLifecycleOwner(), this::aoRotaObtida);
    }

    private void observarTransporte() {
        transporteViewModel.getState().observe(getViewLifecycleOwner(), transportes -> {
            sincronizarVisibilidadeComEstadoAtual();
            calcularFrete();
        });
    }

    private void observarFreteCalculado() {
        observarEstadoSimulacaoFrete();
        observarVisibilidadeResumoFrete();
    }

    private void observarEstadoSimulacaoFrete() {
        simulacaoFreteViewModel.getState().observe(getViewLifecycleOwner(), this::aoFreteCalculado);
    }

    private void observarVisibilidadeResumoFrete() {
        resumoSimulacaoFreteViewModel.getState().observe(getViewLifecycleOwner(), resumo ->
                setVisible(isNotEmpty(resumo), binding.layoutContainerFrete));
    }

    private void restaurarDistanciaSalvaSePossivel() {
        if (isNotEmpty(lerDistanciaManual())) return;
        Double distanciaSalva = simulacaoFreteViewModel.getDistancia().getValue();
        if (isNotEmpty(distanciaSalva) && distanciaSalva > 0) {
            setText(binding.entradaTextoDistancia, String.valueOf(distanciaSalva));
        }
    }

    private void sincronizarVisibilidadeComEstadoAtual() {
        boolean temRota = isNotEmpty(rotaViewModel.getState().getValue());
        boolean temDistanciaManual = isNotEmpty(lerDistanciaManual());
        setVisible(temRota && !temDistanciaManual, binding.layoutContainerRota);
        setVisible(temRota || temDistanciaManual, binding.layoutContainerTransporte);
    }

    private void aoRotaObtida(RotaUiState rota) {
        if (rotaConflitaComDistanciaManual(rota)) {
            rotaViewModel.limpar();
            return;
        }
        if (isNotEmpty(rota)) {
            limparDistanciaManualSemDisparar();
        }
        sincronizarVisibilidadeComEstadoAtual();
        calcularFrete();
    }

    private void aoDistanciaManualAlterada() {
        double distancia = lerDistanciaManual();
        simulacaoFreteViewModel.setDistancia(distancia);
        if (isNotEmpty(distancia)) rotaViewModel.limpar();
        sincronizarVisibilidadeComEstadoAtual();
        calcularFrete();
    }

    private void aoFreteCalculado(PrecificacaoFreteUiState estadoFrete) {
        resumoSimulacaoFreteViewModel.setState(isEmpty(estadoFrete) ? null : freteResumoMapper.mapper(estadoFrete));
    }

    private void confirmarSelecaoFrete() {
        PrecificacaoFreteUiState estado = simulacaoFreteViewModel.getState().getValue();
        if (isEmpty(estado)) return;
        BigDecimal valorTotal = estado.getValorTotal();
        if (isEmpty(valorTotal)) return;

        Bundle resultado = new Bundle();
        resultado.putString(EXTRA_VALOR_FRETE, valorTotal.toPlainString());
        getParentFragmentManager().setFragmentResult(CHAVE_RESULTADO_FRETE, resultado);
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void calcularFrete() {
        double distanciaAtiva = resolverDistanciaAtiva();
        if (isEmpty(distanciaAtiva)) {
            resumoSimulacaoFreteViewModel.setState(null);
            simulacaoFreteViewModel.limpar();
            return;
        }
        simulacaoFreteViewModel.calcularFrete(
                transporteMapper.mapTo(transporteViewModel.getState().getValue()),
                distanciaAtiva,
                cargaTotalDoLote);
    }

    private double resolverDistanciaAtiva() {
        double distanciaManual = lerDistanciaManual();
        if (isNotEmpty(distanciaManual)) return distanciaManual;
        RotaUiState rota = rotaViewModel.getState().getValue();
        return isNotEmpty(rota) ? rota.getDistancia() : 0.0;
    }

    private boolean rotaConflitaComDistanciaManual(RotaUiState rota) {
        return isNotEmpty(rota) && isNotEmpty(lerDistanciaManual());
    }

    private void limparDistanciaManualSemDisparar() {
        if (isEmpty(lerDistanciaManual())) return;
        binding.entradaTextoDistancia.removeTextChangedListener(distanciaWatcher);
        clearText(binding.entradaTextoDistancia);
        simulacaoFreteViewModel.setDistancia(0);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void tratarPermissaoNegada() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (d, w) -> abrirConfiguracoesDaPermissao(),
                (d, w) -> requireActivity().finish());
    }

    private void abrirConfiguracoesDaPermissao() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }

    private void abrirBuscaDeLocalizacao() {
        new BuscaLocalizacaoFragment().show(getChildFragmentManager(), null);
    }

    private double lerDistanciaManual() {
        return getDouble(binding.entradaTextoDistancia);
    }
}