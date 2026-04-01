package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.data.repositories.FreteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.mappers.domain.PrecificacaoFreteMapper;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PrecificacaoFreteViewModel extends ViewModel {
    private final FreteRepository repositorio;
    private final TaskHelper taskHelper;
    private final PrecificacaoFreteMapper precificacaoFreteMapper;
    private final MutableLiveData<PrecificacaoFreteUiState> state = new MutableLiveData<>();
    private final MutableLiveData<BigDecimal> incidencia = new MutableLiveData<>();
    private final MutableLiveData<Double> distancia = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public PrecificacaoFreteViewModel(
            TaskHelper taskHelper,
            FreteRepository repositorio,
            PrecificacaoFreteMapper precificacaoFreteMapper
    ) {
        this.taskHelper = taskHelper;
        this.repositorio = repositorio;
        this.precificacaoFreteMapper = precificacaoFreteMapper;
    }

    public LiveData<PrecificacaoFreteUiState> getState() {
        return state;
    }

    public LiveData<BigDecimal> getIncidencia() {
        return incidencia;
    }

    public LiveData<Double> getDistancia() {
        return distancia;
    }

    public LiveData<Throwable> getError() {
        return error;
    }
    public void setDistancia(double value) {
        distancia.setValue(value);
    }

    public void calcularFrete(List<Transporte> transportes, double distancia, int cargaTotal) {
        taskHelper.execute(
                () -> precificacaoFreteMapper.mapFrom(repositorio.calcularFrete(transportes, distancia, cargaTotal)),
                state::postValue,
                error::postValue
        );
    }

    public void calcularIncidencia(BigDecimal valorDoFrete, int totalCarga) {
        taskHelper.execute(
                () -> repositorio.calcularIncidenciaFretePorAnimal(valorDoFrete, totalCarga),
                resultadoIncidencia -> {
                    incidencia.postValue(resultadoIncidencia);
                    state.postValue(new PrecificacaoFreteUiState(valorDoFrete, resultadoIncidencia));
                },
                error::postValue
        );
    }
    public void limpar() {
        state.setValue(null);
    }
}