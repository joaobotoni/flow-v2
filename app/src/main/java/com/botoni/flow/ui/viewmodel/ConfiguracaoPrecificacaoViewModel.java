package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.ui.state.ConfiguracaoPrecificacaoUiState;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ConfiguracaoPrecificacaoViewModel extends ViewModel {
    private final MutableLiveData<ConfiguracaoPrecificacaoUiState> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public ConfiguracaoPrecificacaoViewModel() { }

    public LiveData<ConfiguracaoPrecificacaoUiState> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void atualizarConfiguracao(BigDecimal arrobaBoiGordo, BigDecimal agioBoiGordo, BigDecimal pesoRefBoiGordo,
                                      BigDecimal arrobaVacaGorda, BigDecimal agioVacaGorda, BigDecimal pesoRefVacaGorda) {
        state.setValue(new ConfiguracaoPrecificacaoUiState(
                arrobaBoiGordo, agioBoiGordo, pesoRefBoiGordo,
                arrobaVacaGorda, agioVacaGorda, pesoRefVacaGorda));
    }

    public void limpar() {
        state.setValue(null);
    }
}
