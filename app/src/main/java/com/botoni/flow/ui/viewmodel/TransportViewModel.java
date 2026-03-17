package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.TransportRepository;
import com.botoni.flow.domain.entities.Transport;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.TransportUiState;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransportViewModel extends ViewModel {

    private final TaskHelper taskExecutor;
    private final TransportRepository repository;
    private final MutableLiveData<TransportUiState> uiState = new MutableLiveData<>(new TransportUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();

    @Inject
    public TransportViewModel(TransportRepository repository, TaskHelper taskExecutor) {
        this.repository = repository;
        this.taskExecutor = taskExecutor;
    }

    public void calculate(long category, int quantity) {
        taskExecutor.execute(
                () -> setState(category, quantity),
                uiState::setValue,
                errorEvent::setValue
        );
    }
    private TransportUiState setState(long category, int quantity) {
        List<Transport> result = repository.recommend(category, quantity);
        return new TransportUiState(result, true);
    }

    public LiveData<TransportUiState> getUiState() {
        return uiState;
    }
    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    public void reset() {
        uiState.setValue(new TransportUiState());
    }
}