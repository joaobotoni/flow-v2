package com.botoni.flow.ui.viewmodel;

import android.Manifest;

import androidx.annotation.RequiresPermission;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.network.LocationRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.SearchUiState;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    private final TaskHelper taskExecutor;
    private final LocationRepository locationRepository;
    private final MutableLiveData<SearchUiState> uiState = new MutableLiveData<>(new SearchUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();
    @Inject
    public SearchViewModel(LocationRepository locationRepository, TaskHelper taskExecutor) {
        this.locationRepository = locationRepository;
        this.taskExecutor = taskExecutor;
    }
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void search(String query) {
        taskExecutor.execute(() -> setState(query), uiState::setValue, errorEvent::setValue);
    }

    public LiveData<SearchUiState> getUiState() {
        return uiState;
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private SearchUiState setState(String query) {
        return new SearchUiState(locationRepository.searchCityAndState(query), false);
    }
    public void reset() {
        uiState.setValue(new SearchUiState());
    }
}