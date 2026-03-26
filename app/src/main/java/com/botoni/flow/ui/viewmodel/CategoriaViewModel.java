package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.CategoriaFreteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.CategoriaUiState;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoriaViewModel extends ViewModel {

    private final CategoriaFreteRepository repositorio;
    private final TaskHelper taskHelper;
    private final MutableLiveData<List<CategoriaUiState>> state = new MutableLiveData<>();
    private final MutableLiveData<CategoriaUiState> categoriaSelecionada = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public CategoriaViewModel(CategoriaFreteRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
        listar();
    }

    public LiveData<List<CategoriaUiState>> getState() {
        return state;
    }

    public LiveData<CategoriaUiState> getCategoriaSelecionada() {
        return categoriaSelecionada;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void selecionar(CategoriaUiState selecionada) {
        if (state.getValue() == null) return;

        List<CategoriaUiState> newList = state.getValue().stream()
                .map(item -> new CategoriaUiState(
                        item.getId(),
                        item.getDescricao(),
                        Objects.equals(item.getId(), selecionada.getId())))
                .collect(Collectors.toList());

        state.setValue(newList);
        categoriaSelecionada.setValue(selecionada);
    }

    private void listar() {
        taskHelper.execute(
                () -> repositorio.getAll().stream()
                        .map(e -> new CategoriaUiState(
                                e.getId(),
                                e.getDescricao(),
                                false))
                        .collect(Collectors.toList()),
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}