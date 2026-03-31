package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.register;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.anyEmpty;
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
import com.botoni.flow.ui.state.ResumoValoresUiState;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.state.TransporteUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFreteFragment extends Fragment {

    private static final String CHAVE_RESUMO_FRETE = "resumo_frete";
    private static final String[] PERMISSOES_LOCALIZACAO = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Inject
    FreteResumoMapper freteResumoMapper;
    @Inject
    TransporteMapper transporteMapper;

    private FragmentPrecificacaoFreteBinding binding;
    private PrecificacaoFreteViewModel precificacaoViewModel;
    private RotaViewModel rotaViewModel;
    private TransporteViewModel transporteViewModel;
    private ActivityResultLauncher<String[]> permissaoLauncher;
    private TextWatcher distanciaWatcher;
    private List<TransporteUiState> transportesRecomendados;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarLauncherDePermissao();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                descartarSimulacaoEVoltar();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        solicitarPermissoesSeNecessario();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrecificacaoFreteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializarViewModels();
        inicializarFragmentosFilhos();
        configurarListeners();
        configurarObservadores();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restaurarDistanciaSalva();
        atualizarVisibilidade();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void registrarLauncherDePermissao() {
        permissaoLauncher = register(this, (granted, result) -> {
            if (!granted) tratarPermissaoNegada();
        });
    }

    private void solicitarPermissoesSeNecessario() {
        if (!hasPermissions(requireContext(), PERMISSOES_LOCALIZACAO)) {
            request(requireContext(), permissaoLauncher, PERMISSOES_LOCALIZACAO);
        }
    }

    private void inicializarViewModels() {
        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        precificacaoViewModel = provider.get(PrecificacaoFreteViewModel.class);
        rotaViewModel = provider.get(RotaViewModel.class);
        transporteViewModel = provider.get(TransporteViewModel.class);
    }

    private void inicializarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_rota, new RotaFragment())
                .replace(R.id.layout_container_transporte, new TransporteFragment())
                .replace(R.id.layout_container_frete, criarFragmentoResumoFrete())
                .commit();
    }

    private ResumoValoresFragment criarFragmentoResumoFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_FRETE,
                getString(R.string.titulo_resumo_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete));
    }

    private void configurarListeners() {
        binding.cartaoExplorarRota.setOnClickListener(v -> abrirBuscaLocalizacao());
        binding.botaoVoltar.setOnClickListener(v -> descartarSimulacaoEVoltar());
        binding.botaoContinuar.setOnClickListener(v -> confirmarSelecaoFrete());
        distanciaWatcher = SimpleTextWatcher(this::aoDistanciaAlterada);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void configurarObservadores() {
        rotaViewModel.getState().observe(getViewLifecycleOwner(), this::aoRotaAlterada);
        transporteViewModel.getState().observe(getViewLifecycleOwner(), this::aoTransporteAtualizado);
        precificacaoViewModel.getState().observe(getViewLifecycleOwner(), this::aoStateFreteAtualizado);
        obterResumoViewModel().getState().observe(getViewLifecycleOwner(), this::aoResumoFreteAtualizado);
    }

    private void aoRotaAlterada(RotaUiState rota) {
        if (isNotEmpty(rota)) limparDistanciaManual();
        atualizarVisibilidade();
        calcularFrete();
    }
    private void aoTransporteAtualizado(List<TransporteUiState> transportes) {
        transportesRecomendados = transportes;
        atualizarVisibilidade();
        calcularFrete();
    }

    private void aoDistanciaAlterada() {
        double distanciaEmKm = obterDistanciaManualEmKm();
        precificacaoViewModel.setDistanciaManual(distanciaEmKm);
        if (isNotEmpty(distanciaEmKm)) rotaViewModel.limpar();
        atualizarVisibilidade();
        calcularFrete();
    }

    private void aoStateFreteAtualizado(PrecificacaoFreteUiState state) {
        publicarResumoFrete(isEmpty(state) ? null : freteResumoMapper.mapper(state));
    }

    private void aoResumoFreteAtualizado(ResumoValoresUiState state) {
        setVisible(isNotEmpty(state), binding.layoutContainerFrete);
    }

    private void confirmarSelecaoFrete() {
        BigDecimal valorTotalFrete = obterValorTotalFrete();
        if (isEmpty(valorTotalFrete)) return;
        precificacaoViewModel.selecionarFrete(valorTotalFrete);
        NavHostFragment.findNavController(this).popBackStack();
    }
    private void descartarSimulacaoEVoltar() {
        precificacaoViewModel.limpar();
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void abrirBuscaLocalizacao() {
        new BuscaLocalizacaoFragment().show(getChildFragmentManager(), null);
    }

    private void tratarPermissaoNegada() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (d, w) -> abrirConfiguracoesDoApp(),
                (d, w) -> requireActivity().finish()
        );
    }

    private void abrirConfiguracoesDoApp() {
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
    }


    private void calcularFrete() {
        PrecificacaoFreteFragmentArgs args = lerArgumentos();
        if (anyEmpty(args, transportesRecomendados)) return;
        double distanciaEmKm = resolverDistanciaEmKm();
        if (isEmpty(distanciaEmKm)) return;
        precificacaoViewModel.calcularFrete(
                transporteMapper.mapTo(transportesRecomendados),
                distanciaEmKm,
                args.getCargaTotal());
    }

    private double resolverDistanciaEmKm() {
        double distanciaManual = obterDistanciaManualEmKm();
        if (isNotEmpty(distanciaManual)) return distanciaManual;
        RotaUiState rota = rotaViewModel.getState().getValue();
        return isNotEmpty(rota) ? rota.getDistancia() : 0.0;
    }

    private void atualizarVisibilidade() {
        setVisible(deveExibirRota(), binding.layoutContainerRota);
        setVisible(deveExibirFluxoTransporte(), binding.layoutContainerTransporte);
    }

    private boolean deveExibirRota() {
        return temRota() && !temDistanciaManual();
    }

    private boolean deveExibirFluxoTransporte() {
        return temRota() || temDistanciaManual();
    }

    private boolean temRota() {
        return isNotEmpty(rotaViewModel.getState().getValue());
    }

    private boolean temDistanciaManual() {
        return isNotEmpty(obterDistanciaManualEmKm());
    }


    private void publicarResumoFrete(ResumoValoresUiState resumo) {
        obterResumoViewModel().setState(resumo);
    }

    private void restaurarDistanciaSalva() {
        if (isNotEmpty(obterDistanciaManualEmKm())) return;
        Double distanciaSalva = precificacaoViewModel.getDistanciaManual().getValue();
        if (isNotEmpty(distanciaSalva)) setText(binding.entradaTextoDistancia, String.valueOf(distanciaSalva));
    }

    private void limparDistanciaManual() {
        if (isEmpty(obterDistanciaManualEmKm())) return;
        binding.entradaTextoDistancia.removeTextChangedListener(distanciaWatcher);
        clearText(binding.entradaTextoDistancia);
        precificacaoViewModel.setDistanciaManual(0);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }


    private BigDecimal obterValorTotalFrete() {
        PrecificacaoFreteUiState state = precificacaoViewModel.getState().getValue();
        if (isEmpty(state)) return null;
        return state.getValorTotal();
    }

    private double obterDistanciaManualEmKm() {
        return getDouble(binding.entradaTextoDistancia);
    }

    private PrecificacaoFreteFragmentArgs lerArgumentos() {
        if (getArguments() == null) return null;
        return PrecificacaoFreteFragmentArgs.fromBundle(getArguments());
    }

    private ResumoValoresViewModel obterResumoViewModel() {
        return new ViewModelProvider(requireActivity()).get(CHAVE_RESUMO_FRETE, ResumoValoresViewModel.class);
    }
}