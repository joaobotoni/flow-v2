package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.PrecificacaoFrete;
import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.data.repositories.FreteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PrecificacaoFreteViewModel extends ViewModel {
    private final FreteRepository repositorio;
    private final TaskHelper taskHelper;
    private final MutableLiveData<PrecificacaoFreteUiState> state = new MutableLiveData<>();
    private final MutableLiveData<BigDecimal> incidencia = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public PrecificacaoFreteViewModel(TaskHelper taskHelper, FreteRepository repositorio) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public LiveData<PrecificacaoFreteUiState> getState() {
        return state;
    }

    public LiveData<BigDecimal> getIncidencia() {
        return incidencia;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void calcularFrete(List<Transporte> transportes, double distancia, int cargaTotal) {
        taskHelper.execute(
                () -> {
                    PrecificacaoFrete result = repositorio.calcularFrete(transportes, distancia, cargaTotal);
                    return new PrecificacaoFreteUiState(result.getValorTotal(), result.getValorParcial());
                },
                state::postValue,
                error::postValue
        );
    }

    public void calcularIncidencia(BigDecimal valorDoFrete, int totalCarga) {
        taskHelper.execute(
                () -> repositorio.calcularIncidenciaFretePorAnimal(valorDoFrete, totalCarga),
                incidencia::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}