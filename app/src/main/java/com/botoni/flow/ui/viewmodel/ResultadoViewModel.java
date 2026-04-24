package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;

public class ResultadoViewModel extends ViewModel {
    private final MutableLiveData<BigDecimal> state = new MutableLiveData<>();

    public void setState(BigDecimal novoState) {
        state.setValue(novoState);
    }

    public LiveData<BigDecimal> getState() {
        return state;
    }

    public void limpar() {
        state.setValue(null);
    }
}