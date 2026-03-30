package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.botoni.flow.ui.state.ResumoValoresUiState;
public class ResumoValoresViewModel extends ViewModel {
    private final MutableLiveData<ResumoValoresUiState> state = new MutableLiveData<>();

    public void setState(ResumoValoresUiState novoState) {
        state.setValue(novoState);
    }

    public LiveData<ResumoValoresUiState> getState() {
        return state;
    }
}
