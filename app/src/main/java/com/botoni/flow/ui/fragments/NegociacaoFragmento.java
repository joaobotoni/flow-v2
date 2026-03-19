package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
import static com.botoni.flow.ui.helpers.ViewHelper.getInt;
import static com.botoni.flow.ui.helpers.ViewHelper.noneMatch;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentDealBinding;
import com.botoni.flow.ui.adapters.CategoriaAdapter;
import com.botoni.flow.ui.helpers.PermissionHelper;
import com.botoni.flow.ui.state.CategoriaUiState;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.state.TransporteUiState;
import com.botoni.flow.ui.viewmodel.CategoriaViewModel;
import com.botoni.flow.ui.viewmodel.FreteViewModel;
import com.botoni.flow.ui.viewmodel.NegociacaoBezerroViewModel;
import com.botoni.flow.ui.viewmodel.NegociacaoFluxoViewModel;
import com.botoni.flow.ui.viewmodel.RotaViewModel;
import com.botoni.flow.ui.viewmodel.TransporteViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NegociacaoFragmento extends Fragment {

    private static final String[] PERMISSOES_LOCALIZACAO = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FragmentDealBinding binding;
    private ActivityResultLauncher<String[]> permissaoLauncher;
    private NegociacaoBezerroViewModel bezerroViewModel;
    private RotaViewModel rotaViewModel;
    private FreteViewModel freteViewModel;
    private TransporteViewModel transporteViewModel;
    private CategoriaViewModel categoriaViewModel;
    private NegociacaoFluxoViewModel fluxoViewModel;
    private CategoriaAdapter categoriaAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarPermissao();
    }

    @Override
    public void onStart() {
        super.onStart();
        solicitarPermissaoSeNecessario();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciarViewModels();
        iniciarViews();
        iniciarObservadoresFluxo();
        iniciarObservadoresEntrada();
        iniciarFragmentosFilhos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void iniciarViewModels() {
        bezerroViewModel = new ViewModelProvider(requireActivity()).get(NegociacaoBezerroViewModel.class);
        rotaViewModel = new ViewModelProvider(requireActivity()).get(RotaViewModel.class);
        freteViewModel = new ViewModelProvider(requireActivity()).get(FreteViewModel.class);
        transporteViewModel = new ViewModelProvider(requireActivity()).get(TransporteViewModel.class);
        categoriaViewModel = new ViewModelProvider(requireActivity()).get(CategoriaViewModel.class);
        fluxoViewModel = new ViewModelProvider(requireActivity()).get(NegociacaoFluxoViewModel.class);
    }

    private void iniciarViews() {
        categoriaAdapter = new CategoriaAdapter(categoriaViewModel::selecionar);
        binding.listaCategorias.setAdapter(categoriaAdapter);
        binding.botaoAbrirBottomSheet.setOnClickListener(v -> abrirBusca());
        iniciarWatchers();
    }

    private void iniciarWatchers() {
        binding.entradaTextoPesoAnimal.addTextChangedListener(
                SimpleTextWatcher(this::onCamposBezerroAlterados));

        binding.entradaTextoQuantidadeAnimais.addTextChangedListener(
                SimpleTextWatcher(() -> {
                    onCamposBezerroAlterados();
                    onCamposTransporteAlterados();
                    onCamposFreteAlterados();
                }));
    }

    private void iniciarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_valores, new BezerroResultadoFragmento())
                .replace(R.id.layout_container_rota, new RotaFragmento())
                .replace(R.id.layout_container_frete, new FreteFragmento())
                .replace(R.id.layout_container_transporte, new TransporteFragmento())
                .commit();
    }

    private void iniciarObservadoresFluxo() {
        fluxoViewModel.getExibirRota().observe(getViewLifecycleOwner(),
                v -> binding.layoutContainerRota.setVisibility(v ? View.VISIBLE : View.GONE));

        fluxoViewModel.getExibirFrete().observe(getViewLifecycleOwner(),
                v -> binding.layoutContainerFrete.setVisibility(v ? View.VISIBLE : View.GONE));

        fluxoViewModel.getExibirTransporte().observe(getViewLifecycleOwner(),
                v -> binding.layoutContainerTransporte.setVisibility(v ? View.VISIBLE : View.GONE));
    }

    private void iniciarObservadoresEntrada() {
        bezerroViewModel.getVisivel().observe(getViewLifecycleOwner(),
                v -> {
                    binding.layoutContainerValores.setVisibility(v ? View.VISIBLE : View.GONE);
                    binding.layoutContainerBotoes.setVisibility(v ? View.VISIBLE : View.GONE);
                    fluxoViewModel.setBezerroCompleto(v);
                });

        rotaViewModel.getVisivel().observe(getViewLifecycleOwner(),
                v -> {
                    fluxoViewModel.setRotaCompleta(v);
                    onCamposFreteAlterados();
                });

        categoriaViewModel.getUiState().observe(getViewLifecycleOwner(), categorias -> {
            categoriaAdapter.submitList(categorias);
            boolean temSelecionada = categorias != null &&
                    categorias.stream().anyMatch(c -> c.selecionada);
            fluxoViewModel.setCategoriaOk(temSelecionada);
            onCamposTransporteAlterados();
        });

        transporteViewModel.getVisivel().observe(getViewLifecycleOwner(),
                v -> {
                    fluxoViewModel.setTransporteOk(v);
                    onCamposFreteAlterados();
                });
    }

    private void onCamposBezerroAlterados() {
        String peso = requireText(binding.entradaTextoPesoAnimal);
        String quantidade = requireText(binding.entradaTextoQuantidadeAnimais);
        if (noneMatch(peso, quantidade)) {
            bezerroViewModel.calcular(
                    getBigDecimal(binding.entradaTextoPesoAnimal),
                    getInt(binding.entradaTextoQuantidadeAnimais));
        } else {
            bezerroViewModel.limpar();
        }
    }

    private void onCamposTransporteAlterados() {
        String quantidade = requireText(binding.entradaTextoQuantidadeAnimais);
        List<CategoriaUiState> lista = categoriaViewModel.getUiState().getValue();
        CategoriaUiState cat = lista == null ? null :
                lista.stream().filter(c -> c.selecionada).findFirst().orElse(null);
        if (noneMatch(quantidade) && cat != null) {
            transporteViewModel.calcular(cat.id, getInt(binding.entradaTextoQuantidadeAnimais));
        } else {
            transporteViewModel.limpar();
        }
    }

    private void onCamposFreteAlterados() {
        RotaUiState rota = rotaViewModel.getUiState().getValue();
        List<TransporteUiState> transportes = transporteViewModel.getUiState().getValue();
        String quantidade = requireText(binding.entradaTextoQuantidadeAnimais);
        if (rota != null && transportes != null && !transportes.isEmpty() && noneMatch(quantidade)) {
            freteViewModel.calcular(transportes, rota.distancia, getInt(binding.entradaTextoQuantidadeAnimais));
        } else {
            freteViewModel.limpar();
        }
    }

    private void registrarPermissao() {
        permissaoLauncher = PermissionHelper.register(this, (granted, result) -> {
            if (!granted) onPermissaoNegada();
        });
    }

    private void solicitarPermissaoSeNecessario() {
        if (!hasPermissions(requireContext(), PERMISSOES_LOCALIZACAO)) {
            request(requireContext(), permissaoLauncher, PERMISSOES_LOCALIZACAO);
        }
    }

    private void onPermissaoNegada() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (dialog, which) -> abrirConfiguracoes(),
                (dialog, which) -> {
                    if (isAdded()) requireActivity().finish();
                });
    }

    private void abrirBusca() {
        BuscaLocalizacaoFragmento sheet = new BuscaLocalizacaoFragmento();
        sheet.show(getParentFragmentManager(), sheet.getTag());
    }

    private void abrirConfiguracoes() {
        if (!isAdded()) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }
}