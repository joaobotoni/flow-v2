package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.model.Transporte;
import com.botoni.flow.data.repositories.TransporteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.TransporteUiState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransporteViewModel extends ViewModel {

    private final TransporteRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<List<TransporteUiState>> uiState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public TransporteViewModel(TransporteRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public void calcular(long categoria, int quantidade) {
        taskHelper.execute(() -> montar(categoria, quantidade), lista -> {
            uiState.postValue(lista);
            visivel.postValue(lista != null && !lista.isEmpty());
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(null);
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<List<TransporteUiState>> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    private List<TransporteUiState> montar(long categoria, int quantidade) {
        List<TransporteUiState> lista = new ArrayList<>();
        for (Transporte t : repositorio.recomendacao(categoria, quantidade)) {
            lista.add(converter(t));
        }
        return lista;
    }

    private TransporteUiState converter(Transporte t) {
        return new TransporteUiState(t.getId(), t.getNome(), t.getQuantidade(), t.getCapacidade(), t.getPercentual());
    }
}