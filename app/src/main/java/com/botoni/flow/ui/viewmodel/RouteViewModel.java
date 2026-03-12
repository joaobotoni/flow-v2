package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.ui.state.RouteUiState;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RouteViewModel extends ViewModel {
    private final MutableLiveData<RouteUiState> _uiState = new MutableLiveData<>(new RouteUiState());

    @Inject
    public RouteViewModel() {
    }

    public LiveData<RouteUiState> getUiState() {
        return _uiState;
    }

    public void setRoute(List<String> points, double distance) {
        _uiState.setValue(new RouteUiState(points, distance, true));
    }

    public void reset() {
        _uiState.setValue(new RouteUiState());
    }
}
