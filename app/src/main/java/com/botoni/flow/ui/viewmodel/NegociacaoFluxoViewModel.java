package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NegociacaoFluxoViewModel extends ViewModel {

    private final MutableLiveData<Boolean> bezerroCompleto = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> rotaCompleta = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> categoriaOk = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> transporteOk = new MutableLiveData<>(false);

    private final MediatorLiveData<Boolean> exibirRota = new MediatorLiveData<>(false);
    private final MediatorLiveData<Boolean> exibirFrete = new MediatorLiveData<>(false);
    private final MediatorLiveData<Boolean> exibirTransporte = new MediatorLiveData<>(false);

    @Inject
    public NegociacaoFluxoViewModel() {
        observarRota();
        observarFrete();
        observarTransporte();
    }

    public void setBezerroCompleto(boolean valor) {
        bezerroCompleto.setValue(valor);
    }

    public void setRotaCompleta(boolean valor) {
        rotaCompleta.setValue(valor);
    }

    public void setCategoriaOk(boolean valor) {
        categoriaOk.setValue(valor);
    }

    public void setTransporteOk(boolean valor) {
        transporteOk.setValue(valor);
    }

    public LiveData<Boolean> getExibirRota() {
        return exibirRota;
    }

    public LiveData<Boolean> getExibirFrete() {
        return exibirFrete;
    }

    public LiveData<Boolean> getExibirTransporte() {
        return exibirTransporte;
    }

    private void observarRota() {
        exibirRota.addSource(bezerroCompleto, v ->
                exibirRota.setValue(Boolean.TRUE.equals(v)));
    }

    private void observarFrete() {
        exibirFrete.addSource(exibirRota, v -> atualizarFrete());
        exibirFrete.addSource(rotaCompleta, v -> atualizarFrete());
    }

    private void observarTransporte() {
        exibirTransporte.addSource(exibirFrete, v -> atualizarTransporte());
        exibirTransporte.addSource(exibirRota, v -> atualizarTransporte());
        exibirTransporte.addSource(categoriaOk, v -> atualizarTransporte());
        exibirTransporte.addSource(transporteOk, v -> atualizarTransporte());
    }

    private void atualizarFrete() {
        exibirFrete.setValue(
                Boolean.TRUE.equals(exibirRota.getValue()) &&
                        Boolean.TRUE.equals(rotaCompleta.getValue()));
    }

    private void atualizarTransporte() {
        exibirTransporte.setValue(
                Boolean.TRUE.equals(exibirFrete.getValue()) &&
                        Boolean.TRUE.equals(exibirRota.getValue()) &&
                        Boolean.TRUE.equals(categoriaOk.getValue()) &&
                        Boolean.TRUE.equals(transporteOk.getValue()));
    }
}