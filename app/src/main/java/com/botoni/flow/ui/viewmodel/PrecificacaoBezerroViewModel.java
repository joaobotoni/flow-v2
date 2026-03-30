package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.data.repositories.PrecificacaoBezerroRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.mappers.domain.PrecificacaoBezerroMapper;
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
    private final PrecificacaoBezerroMapper precificacaoBezerroMapper;

    @Inject
    public PrecificacaoBezerroViewModel(PrecificacaoBezerroRepository repositorio, TaskHelper taskHelper, PrecificacaoBezerroMapper precificacaoBezerroMapper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
        this.precificacaoBezerroMapper = precificacaoBezerroMapper;
    }

    public LiveData<PrecificacaoBezerroUiState> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void calcularNegociacao(BigDecimal peso, BigDecimal arroba, BigDecimal percent, Integer quantidade) {
        taskHelper.execute(
                () -> precificacaoBezerroMapper.
                        mapFrom(repositorio.calcularNegociacaoBezerro(peso, arroba, percent, quantidade)),
                state::postValue,
                error::postValue
        );
    }

    public void calcularNegociacaoBezerroComDescontoDoFrete(BigDecimal peso, BigDecimal arroba, BigDecimal percent, Integer quantidade,
                                                            BigDecimal valorIncidenteFrete) {
        taskHelper.execute(
                () -> precificacaoBezerroMapper
                        .mapFrom(repositorio.calcularNegociacaoBezerroComDescontoDoFrete(peso, arroba, percent, quantidade, valorIncidenteFrete)),
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}