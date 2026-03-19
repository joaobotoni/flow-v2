package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.FreteRepository;
import com.botoni.flow.data.model.Transporte;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.FreteUiState;
import com.botoni.flow.ui.state.TransporteUiState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FreteViewModel extends ViewModel {

    private final FreteRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<FreteUiState> uiState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public FreteViewModel(FreteRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public void calcular(List<TransporteUiState> transportes, double distancia, int total) {
        taskHelper.execute(() -> montar(transportes, distancia, total), state -> {
            uiState.postValue(state);
            visivel.postValue(state != null);
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(null);
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<FreteUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    private FreteUiState montar(List<TransporteUiState> transportes, double distancia, int total) {
        List<Transporte> modelos = converter(transportes);
        BigDecimal valorTotal = repositorio.calcularTotal(modelos, distancia);
        BigDecimal valorPorAnimal = repositorio.calcularPorCabeca(modelos, distancia, total);
        return new FreteUiState(valorTotal, valorPorAnimal);
    }

    private List<Transporte> converter(List<TransporteUiState> transportes) {
        List<Transporte> lista = new ArrayList<>();
        for (TransporteUiState t : transportes) {
            lista.add(new Transporte(t.id, t.nomeVeiculo, t.quantidade, t.capacidade, t.percentual));
        }
        return lista;
    }
}