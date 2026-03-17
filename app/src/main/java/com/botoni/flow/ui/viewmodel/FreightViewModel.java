package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.FreteRepository;
import com.botoni.flow.data.repositories.local.TransportRepository;
import com.botoni.flow.domain.entities.Transport;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.FreightUiState;


import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FreightViewModel extends ViewModel {
    private final TaskHelper taskExecutor;
    private final FreteRepository repository;
    private final MutableLiveData<FreightUiState> uiState = new MutableLiveData<>(new FreightUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();
    @Inject
    public FreightViewModel(TaskHelper taskExecutor, FreteRepository repository) {
        this.taskExecutor = taskExecutor;
        this.repository = repository;
    }

    public LiveData<FreightUiState> getUiState() {
        return uiState;
    }

     public void calculate(List<Transport> transports, double distance, int count){
         taskExecutor.execute(
                 () -> setState(transports,  distance, count),
                 uiState::setValue,
                 errorEvent::setValue
         );
     }
    private FreightUiState setState(List<Transport> transports, double distance, int count) {
        BigDecimal total = repository.calcularTotal(transports, distance);
        BigDecimal perAnimal = repository.calcularPorCabeca(transports, distance, count);
        return new FreightUiState(total, perAnimal, true);
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    public void reset() {
        uiState.setValue(new FreightUiState());
    }
}
