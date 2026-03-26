package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.data.repositories.PrecificacaoBezerroRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PrecificacaoBezerroViewModel extends ViewModel {

    private final PrecificacaoBezerroRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<PrecificacaoBezerroUiState> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public PrecificacaoBezerroViewModel(PrecificacaoBezerroRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public LiveData<PrecificacaoBezerroUiState> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void calcularNegociacaoBezerroComDescontoDoFrete(BigDecimal peso, BigDecimal arroba, BigDecimal percent, Integer quantidade, BigDecimal valorIncidenteFrete) {
        taskHelper.execute(
                () -> {
                    PrecificacaoBezerro result = repositorio.calcularNegociacaoBezerroComDescontoDoFrete(peso, arroba, percent, quantidade, valorIncidenteFrete);
                    return new PrecificacaoBezerroUiState(result.getValorPorKg(), result.getValorPorCabeca(), result.getValorTotal());
                },
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}