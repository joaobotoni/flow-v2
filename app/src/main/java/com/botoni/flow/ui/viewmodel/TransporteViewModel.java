package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.TransporteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.TransporteUiState;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransporteViewModel extends ViewModel {

    private final TransporteRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<List<TransporteUiState>> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public TransporteViewModel(TransporteRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public LiveData<List<TransporteUiState>> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void recomendar(long categoria, int quantidade) {
        taskHelper.execute(
                () -> repositorio.recomendarTransportes(categoria, quantidade)
                        .stream()
                        .map(t -> new TransporteUiState(
                                t.getId(),
                                t.getNomeVeiculo(),
                                t.getQuantidade(),
                                t.getCapacidade(),
                                t.getOcupacao()))
                        .collect(Collectors.toList()),
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}