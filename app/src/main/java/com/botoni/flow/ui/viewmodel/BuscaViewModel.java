package com.botoni.flow.ui.viewmodel;

import android.Manifest;

import androidx.annotation.RequiresPermission;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.LocalizacaoRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.BuscaLocalizacaoUiState;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BuscaViewModel extends ViewModel {

    private final LocalizacaoRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<BuscaLocalizacaoUiState> uiState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public BuscaViewModel(LocalizacaoRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void buscar(String consulta) {
        taskHelper.execute(() -> montar(consulta), state -> {
            uiState.postValue(state);
            visivel.postValue(state != null);
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(null);
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<BuscaLocalizacaoUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private BuscaLocalizacaoUiState montar(String consulta) {
        return new BuscaLocalizacaoUiState(repositorio.searchCityAndState(consulta), false);
    }
}