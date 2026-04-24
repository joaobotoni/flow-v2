package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.ViewHelper.orElse;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.botoni.flow.data.models.Configuration;
import com.botoni.flow.data.repositories.GespecRepository;
import com.botoni.flow.databinding.FragmentMainBinding;
import com.botoni.flow.ui.helpers.TaskHelper;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Inject
    TaskHelper taskHelper;
    @Inject
    GespecRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupListeners() {
        binding.botaoEnviar.setOnClickListener(v -> request(buildConfig()));
    }

    private void request(Configuration config) {
        requestUsuario(config);
        requestAcesso(config);
    }

    private void requestUsuario(Configuration config) {
        taskHelper.execute(
                () -> repository.syncUsuario(config),
                this::onSuccess,
                this::onError
        );
    }

    private void requestAcesso(Configuration config) {
        taskHelper.execute(
                () -> repository.syncAcessoUsuario(config),
                this::onSuccess,
                this::onError
        );
    }

    private Configuration buildConfig() {
        return Configuration.builder()
                .host(readIp())
                .port(readHost())
                .site(readSite())
                .username(readUsername())
                .applicationId(deviceId())
                .applicationName(deviceName())
                .build();
    }

    @SuppressLint("HardwareIds")
    private String deviceId() {
        return Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    private String deviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    private void onSuccess(String body) {
        showSnackBar(requireView(),
                String.format(Locale.getDefault(), "Deu bom.\nResponse: %s", body));

        Log.d("TESTE", String.format(Locale.getDefault(), "Deu bom.\nResponse: %s", body));
    }

    private void onError(Throwable throwable) {
        showSnackBar(requireView(),
                String.format(Locale.getDefault(), "Erro na comunicação: %s", throwable.getMessage()));
    }

    private String readHost() {
        return orElse(requireText(binding.entradaTextoHost), "");
    }

    private String readIp() {
        return orElse(requireText(binding.entradaTextoIp), "");
    }

    private String readSite() {
        return orElse(requireText(binding.entradaTextoSite), "");
    }

    private String readUsername() {
        return orElse(requireText(binding.entradaTextoUsuario), "");
    }
}