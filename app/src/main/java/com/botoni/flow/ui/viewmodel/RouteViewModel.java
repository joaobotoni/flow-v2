package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.TransportRepository;
import com.botoni.flow.domain.entities.Transport;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.RouteUiState;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RouteViewModel extends ViewModel {
    private final MutableLiveData<RouteUiState> _uiState = new MutableLiveData<>(new RouteUiState());
    private final MutableLiveData<List<Transport>> _transports = new MutableLiveData<>();
    private final MutableLiveData<Exception> _errorEvent = new MutableLiveData<>();
    private final TransportRepository repository;
    private final TaskHelper taskExecutor;

    @Inject
    public RouteViewModel(TransportRepository repository, TaskHelper taskExecutor) {
        this.repository = repository;
        this.taskExecutor = taskExecutor;
    }

    public LiveData<RouteUiState> getUiState() {
        return _uiState;
    }

    public LiveData<List<Transport>> getTransports() {
        return _transports;
    }

    public LiveData<Exception> getErrorEvent() {
        return _errorEvent;
    }

    public void recommend(long category, int quantity) {
        taskExecutor.execute(
                () -> repository.recommend(category, quantity),
                _transports::setValue,
                _errorEvent::setValue
        );
    }

    public void setRoute(List<String> points, double distance) {
        _uiState.setValue(new RouteUiState(points, distance, true));
    }

    public void reset() {
        _uiState.setValue(new RouteUiState());
    }
}