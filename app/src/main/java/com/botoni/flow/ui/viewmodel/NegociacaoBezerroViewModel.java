package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.NegociacaoPorFaixaRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.ResultadoBezerroUiState;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NegociacaoBezerroViewModel extends ViewModel {

    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal PERCENT = new BigDecimal("30");

    private final NegociacaoPorFaixaRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<ResultadoBezerroUiState> uiState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public NegociacaoBezerroViewModel(NegociacaoPorFaixaRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public void calcular(BigDecimal peso, Integer quantidade) {
        taskHelper.execute(() -> montar(peso, quantidade), state -> {
            uiState.postValue(state);
            visivel.postValue(state != null);
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(null);
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<ResultadoBezerroUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    private ResultadoBezerroUiState montar(BigDecimal peso, Integer quantidade) {
        BigDecimal valorPorKg = repositorio.calcularValorPorKg(peso, ARROBA, PERCENT);
        BigDecimal valorPorCabeca = repositorio.calcularValorTotalBezerro(peso, ARROBA, PERCENT);
        BigDecimal valorTotal = repositorio.calcularValorTotalLote(valorPorCabeca, quantidade);
        return new ResultadoBezerroUiState(valorPorKg, valorPorCabeca, valorTotal);
    }
}