package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.CategoriaFreteRepository;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.CategoryUiState;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {
    private final TaskHelper taskExecutor;
    private final CategoriaFreteRepository freightRepo;
    private final MutableLiveData<CategoryUiState> uiState = new MutableLiveData<>(new CategoryUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();

    @Inject
    public CategoryViewModel(TaskHelper taskExecutor, CategoriaFreteRepository freightRepo) {
        this.taskExecutor = taskExecutor;
        this.freightRepo = freightRepo;
        load();
    }

    public LiveData<CategoryUiState> getUiState() {
        return uiState;
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    public void select(CategoriaFrete categoria) {
        taskExecutor.execute(
                () -> setState(getCategories(), categoria),
                uiState::setValue,
                errorEvent::setValue);
    }

    public CategoriaFrete getSelected() {
        CategoryUiState current = uiState.getValue();
        return current != null ? current.getSelected() : null;
    }

    private void load() {
        taskExecutor.execute(
                () -> setState(freightRepo.getAll(), null),
                uiState::setValue,
                errorEvent::setValue);
    }

    public void reset() {
        taskExecutor.execute(
                () -> setState(getCategories(), null),
                uiState::setValue,
                errorEvent::setValue);
    }

    private CategoryUiState setState(List<CategoriaFrete> categories, CategoriaFrete selected) {
        return new CategoryUiState(categories, selected);
    }

    private List<CategoriaFrete> getCategories() {
        CategoryUiState current = uiState.getValue();
        return current != null && current.getCategories() != null
                ? current.getCategories()
                : Collections.emptyList();
    }
}