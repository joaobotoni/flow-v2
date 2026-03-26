package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showDialog;
import static com.botoni.flow.ui.helpers.PermissionHelper.hasPermissions;
import static com.botoni.flow.ui.helpers.PermissionHelper.register;
import static com.botoni.flow.ui.helpers.PermissionHelper.request;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SimpleTextWatcher;

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
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentPrecificacaoFreteBinding;

public class PrecificacaoFreteFragment extends Fragment {
    private FragmentPrecificacaoFreteBinding binding;
    private ActivityResultLauncher<String[]> permissaoLauncher;
    private static final String[] PERMISSOES_LOCALIZACAO = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissaoLauncher = register(this, (granted, result) -> {
            if (!granted) onPermissaoNegada();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrecificacaoFreteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciarFragmentosFilhos();
        configurarViews();
        configurarWatchers();
        configurarNavegacao();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void iniciarFragmentosFilhos() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.layout_container_rota, new RotaFragment())
                .replace(R.id.layout_container_transporte, new TransporteFragment())
                .replace(R.id.layout_container_frete, new FreteFragment())
                .commit();
    }

    private void configurarViews() {
        binding.cartaoExplorarRota.setOnClickListener(v -> showBottomSheet(getChildFragmentManager()));
    }

    private void configurarWatchers() {
        binding.entradaTextoDistancia.addTextChangedListener(SimpleTextWatcher(() -> {
        }));
    }

    private void configurarNavegacao() {
        binding.botaoVoltar.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_precificacaoFreteFragment_to_precificacaoFragment));
    }

    public static void showBottomSheet(FragmentManager fragmentManager) {
        new BuscaLocalizacaoFragment().show(fragmentManager, null);
    }

    private void onPermissaoNegada() {
        showDialog(
                requireContext(),
                getString(R.string.dialog_title_permission_location),
                getString(R.string.dialog_message_permission_location),
                (dialog, which) -> abrirConfiguracoes(),
                (dialog, which) -> requireActivity().finish());
    }

    private void abrirConfiguracoes() {
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
    }
}