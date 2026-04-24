package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.botoni.flow.ui.state.ResumoValoresUiState;

import dagger.hilt.android.lifecycle.HiltViewModel;

public class ResumoValoresViewModel extends ViewModel {
    private final MutableLiveData<ResumoValoresUiState> state = new MutableLiveData<>();

    public void setState(ResumoValoresUiState novoState) {
        state.setValue(novoState);
    }

    public LiveData<ResumoValoresUiState> getState() {
        return state;
    }

    public void limpar() {
        state.setValue(null);
    }
}
