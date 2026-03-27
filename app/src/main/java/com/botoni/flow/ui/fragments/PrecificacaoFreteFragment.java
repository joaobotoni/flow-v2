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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.databinding.FragmentPrecificacaoFreteBinding;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.state.TransporteUiState;
import com.botoni.flow.ui.viewmodel.PrecificacaoFreteViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PrecificacaoFreteFragment extends Fragment {

    private static final String[] PERMISSOES_LOCALIZACAO = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FragmentPrecificacaoFreteBinding binding;
    private PrecificacaoFreteViewModel precificacaoViewModel;
    private RotaViewModel rotaViewModel;
    private TransporteViewModel transporteViewModel;
    private ActivityResultLauncher<String[]> permissaoLauncher;
    private TextWatcher distanciaWatcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializarPermissaoLauncher();
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
        inicializarInteracoes();
        inicializarObservadores();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restaurarDistanciaSalva();
        atualizarInterface();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void inicializarPermissaoLauncher() {
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
        transporteViewModel = provider.get(TransporteViewModel.class);
        precificacaoViewModel = provider.get(PrecificacaoFreteViewModel.class);
        rotaViewModel = provider.get(RotaViewModel.class);
    }

    private void inicializarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_rota, new RotaFragment())
                .replace(R.id.layout_container_transporte, new TransporteFragment())
                .replace(R.id.layout_container_frete, new FreteFragment())
                .commit();
    }

    private void inicializarInteracoes() {
        binding.cartaoExplorarRota.setOnClickListener(v -> abrirBuscaLocalizacao());
        binding.botaoVoltar.setOnClickListener(v -> navegarParaTras());
        binding.botaoContinuar.setOnClickListener(v -> confirmarSelecaoFrete());
        distanciaWatcher = SimpleTextWatcher(this::aoDistanciaAlterada);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void inicializarObservadores() {
        rotaViewModel.getState().observe(getViewLifecycleOwner(), this::aoRotaAlterada);
        transporteViewModel.getState().observe(getViewLifecycleOwner(), state -> atualizarInterface());
    }

    private void aoRotaAlterada(RotaUiState rota) {
        if (isNotEmpty(rota)) limparDistanciaManual();
        atualizarInterface();
    }

    private void aoDistanciaAlterada() {
        double distancia = getDouble(binding.entradaTextoDistancia);
        precificacaoViewModel.setDistanciaManual(distancia);
        if (isNotEmpty(distancia)) rotaViewModel.limpar();
        atualizarInterface();
    }

    private void atualizarInterface() {
        RotaUiState rota = rotaViewModel.getState().getValue();
        List<TransporteUiState> transportes = transporteViewModel.getState().getValue();
        aplicarVisibilidade(rota);
        processarCalculoFrete(rota, transportes);
    }

    private void aplicarVisibilidade(RotaUiState rota) {
        boolean temRota = isNotEmpty(rota);
        boolean temDistanciaManual = isNotEmpty(getDouble(binding.entradaTextoDistancia));
        boolean mostrarFluxoTransporte = temRota || temDistanciaManual;
        setVisible(temRota && !temDistanciaManual, binding.layoutContainerRota);
        setVisible(mostrarFluxoTransporte, binding.layoutContainerTransporte, binding.layoutContainerFrete);
    }

    private void processarCalculoFrete(RotaUiState rota, List<TransporteUiState> transportes) {
        PrecificacaoFreteFragmentArgs args = lerArgumentos();
        if (anyEmpty(args, transportes)) return;
        double distancia = resolverDistancia(rota);
        if (isEmpty(distancia)) return;
        precificacaoViewModel.calcularFrete(mapearTransportes(transportes), distancia, args.getCargaTotal());
    }

    private double resolverDistancia(RotaUiState rota) {
        double manual = getDouble(binding.entradaTextoDistancia);
        if (isNotEmpty(manual)) return manual;
        return isNotEmpty(rota) ? rota.getDistancia() : 0.0;
    }

    private List<Transporte> mapearTransportes(List<TransporteUiState> uiStates) {
        return uiStates.stream()
                .map(t -> new Transporte(
                        t.getId(),
                        t.getNomeVeiculo(),
                        t.getQuantidade(),
                        t.getCapacidade(),
                        t.getOcupacao()))
                .collect(Collectors.toList());
    }

    private void confirmarSelecaoFrete() {
        PrecificacaoFreteUiState state = precificacaoViewModel.getState().getValue();
        if (anyEmpty(state, state != null ? state.getValorTotal() : null)) return;
        precificacaoViewModel.selecionarFrete(state.getValorTotal());
        navegarParaTras();
    }

    private void restaurarDistanciaSalva() {
        if (isNotEmpty(getDouble(binding.entradaTextoDistancia))) return;
        Double salva = precificacaoViewModel.getDistanciaManual().getValue();
        if (isNotEmpty(salva)) setText(binding.entradaTextoDistancia, String.valueOf(salva));
    }

    private void limparDistanciaManual() {
        if (isEmpty(getDouble(binding.entradaTextoDistancia))) return;
        binding.entradaTextoDistancia.removeTextChangedListener(distanciaWatcher);
        clearText(binding.entradaTextoDistancia);
        precificacaoViewModel.setDistanciaManual(0);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private PrecificacaoFreteFragmentArgs lerArgumentos() {
        if (getArguments() == null) return null;
        return PrecificacaoFreteFragmentArgs.fromBundle(getArguments());
    }

    private void abrirBuscaLocalizacao() {
        new BuscaLocalizacaoFragment().show(getChildFragmentManager(), null);
    }

    private void navegarParaTras() {
        NavHostFragment.findNavController(this).popBackStack();
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
}