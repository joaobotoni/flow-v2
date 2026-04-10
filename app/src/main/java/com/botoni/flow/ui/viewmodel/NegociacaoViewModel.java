package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;

import java.math.BigDecimal;

public class NegociacaoViewModel extends ViewModel {
    private final MutableLiveData<PrecificacaoBezerroUiState> override = new MutableLiveData<>();
    private final MutableLiveData<BigDecimal> totalOriginal = new MutableLiveData<>();

    public LiveData<PrecificacaoBezerroUiState> getOverride() {
        return override;
    }

    public void setOverride(PrecificacaoBezerroUiState estado) {
        override.setValue(estado);
    }

    public void limparOverride() {
        override.setValue(null);
    }

    public LiveData<BigDecimal> getTotalOriginal() {
        return totalOriginal;
    }

    public void setTotalOriginal(BigDecimal valor) {
        totalOriginal.setValue(valor);
    }
}
