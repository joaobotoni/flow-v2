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
    @Inject FreteResumoMapper freteResumoMapper;
    @Inject TransporteMapper transporteMapper;

    private FragmentPrecificacaoFreteBinding binding;
    private PrecificacaoFreteViewModel precificacaoViewModel;
    private RotaViewModel rotaViewModel;
    private TransporteViewModel transporteViewModel;
    private ActivityResultLauncher<String[]> permissaoLauncher;
    private TextWatcher distanciaWatcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissaoLauncher = register(this, (concedida, result) -> {
            if (!concedida) tratarPermissaoNegada();
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(PrecificacaoFreteFragment.this).popBackStack();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!hasPermissions(requireContext(), PERMISSOES_LOCALIZACAO)) {
            request(requireContext(), permissaoLauncher, PERMISSOES_LOCALIZACAO);
        }
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

    private void inicializarViewModels() {
        ViewModelProvider providerDaActivity = new ViewModelProvider(requireActivity());
        rotaViewModel = providerDaActivity.get(RotaViewModel.class);
        transporteViewModel = providerDaActivity.get(TransporteViewModel.class);
        precificacaoViewModel = providerDaActivity.get(CHAVE_VIEWMODEL_SIMULACAO, PrecificacaoFreteViewModel.class);

    }

    private void inicializarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_rota, new RotaFragment())
                .replace(R.id.layout_container_transporte, new TransporteFragment())
                .replace(R.id.layout_container_frete, criarResumoSimulacaoFrete())
                .commit();
    }

    private void configurarListeners() {
        binding.cartaoExplorarRota.setOnClickListener(v -> new BuscaLocalizacaoFragment().show(getChildFragmentManager(), null));
        binding.botaoVoltar.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.botaoContinuar.setOnClickListener(v -> confirmarSelecaoFrete());

        distanciaWatcher = SimpleTextWatcher(this::aoDistanciaAlterada);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void configurarObservadores() {
        observarRota();
        observarTransporte();
        observarCalculoFrete();
    }

    private void observarRota() {
        rotaViewModel.getState().observe(getViewLifecycleOwner(), rotaAtual -> {
            if (isNotEmpty(rotaAtual) && isNotEmpty(obterDistanciaManualEmKm())) {
                rotaViewModel.limpar();
                return;
            }
            if (isNotEmpty(rotaAtual)) redefinirCampoDistanciaSemDisparar();
            atualizarVisibilidade();
            calcularFrete();
        });
    }

    private void observarTransporte() {
        transporteViewModel.getState().observe(getViewLifecycleOwner(), listaTransportes -> {
            atualizarVisibilidade();
            calcularFrete();
        });
    }

    private void observarCalculoFrete() {
        precificacaoViewModel.getState().observe(getViewLifecycleOwner(), estadoFrete ->
                obterResumoViewModel().setState(isEmpty(estadoFrete) ? null : freteResumoMapper.mapper(estadoFrete)));
        obterResumoViewModel().getState().observe(getViewLifecycleOwner(), estadoResumo ->
                setVisible(isNotEmpty(estadoResumo), binding.layoutContainerFrete));
    }

    private void aoDistanciaAlterada() {
        double distanciaEmKm = obterDistanciaManualEmKm();
        precificacaoViewModel.setDistancia(distanciaEmKm);
        if (isNotEmpty(distanciaEmKm)) rotaViewModel.limpar();
        atualizarVisibilidade();
        calcularFrete();
    }

    private void calcularFrete() {
        double distancia = resolverDistanciaEmKm();
        if (isEmpty(distancia)) {
            obterResumoViewModel().setState(null);
            precificacaoViewModel.limpar();
            return;
        }

        precificacaoViewModel.calcularFrete(
                transporteMapper.mapTo(transporteViewModel.getState().getValue()),
                distancia,
                lerArgumentos().getCargaTotal()
        );
    }

    private double resolverDistanciaEmKm() {
        double distanciaManual = obterDistanciaManualEmKm();
        if (isNotEmpty(distanciaManual)) return distanciaManual;

        RotaUiState rota = rotaViewModel.getState().getValue();
        return isNotEmpty(rota) ? rota.getDistancia() : 0.0;
    }

    private void atualizarVisibilidade() {
        boolean temRota = isNotEmpty(rotaViewModel.getState().getValue());
        boolean temDistanciaManual = isNotEmpty(obterDistanciaManualEmKm());

        setVisible(temRota && !temDistanciaManual, binding.layoutContainerRota);
        setVisible(temRota || temDistanciaManual, binding.layoutContainerTransporte);
    }

    private void confirmarSelecaoFrete() {
        PrecificacaoFreteUiState estado = precificacaoViewModel.getState().getValue();
        if (isEmpty(estado)) return;
        BigDecimal valorTotal = estado.getValorTotal();
        if (isEmpty(valorTotal)) return;

        Bundle resultado = new Bundle();
        resultado.putString(EXTRA_VALOR_FRETE, valorTotal.toPlainString());
        getParentFragmentManager().setFragmentResult(CHAVE_RESULTADO_FRETE, resultado);
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void restaurarDistanciaSalva() {
        if (isNotEmpty(obterDistanciaManualEmKm())) return;
        Double distanciaSalva = precificacaoViewModel.getDistancia().getValue();
        if (isNotEmpty(distanciaSalva) && distanciaSalva > 0) {
            setText(binding.entradaTextoDistancia, String.valueOf(distanciaSalva));
        }
    }

    private void redefinirCampoDistanciaSemDisparar() {
        if (isEmpty(obterDistanciaManualEmKm())) return;
        binding.entradaTextoDistancia.removeTextChangedListener(distanciaWatcher);
        clearText(binding.entradaTextoDistancia);
        precificacaoViewModel.setDistancia(0);
        binding.entradaTextoDistancia.addTextChangedListener(distanciaWatcher);
    }

    private void tratarPermissaoNegada() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(intent);
                },
                (d, w) -> requireActivity().finish()
        );
    }

    private ResumoValoresFragment criarResumoSimulacaoFrete() {
        return ResumoValoresFragment.newInstance(
                CHAVE_RESUMO_SIMULACAO_FRETE,
                getString(R.string.titulo_resumo_frete),
                getString(R.string.card_label_total_value),
                getString(R.string.card_label_unit_value_frete)
        );
    }

    private double obterDistanciaManualEmKm() {
        return getDouble(binding.entradaTextoDistancia);
    }

    private PrecificacaoFreteFragmentArgs lerArgumentos() {
        return PrecificacaoFreteFragmentArgs.fromBundle(requireArguments());
    }

    private ResumoValoresViewModel obterResumoViewModel() {
        return new ViewModelProvider(requireActivity()).get(CHAVE_RESUMO_SIMULACAO_FRETE, ResumoValoresViewModel.class);
    }
}
