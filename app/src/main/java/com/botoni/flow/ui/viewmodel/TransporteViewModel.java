package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.TransporteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.mappers.domain.TransporteMapper;
import com.botoni.flow.ui.state.TransporteUiState;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransporteViewModel extends ViewModel {
    private final TransporteRepository repositorio;
    private final TaskHelper taskHelper;
    private final MutableLiveData<List<TransporteUiState>> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();
    private final TransporteMapper transporteMapper;

    @Inject
    public TransporteViewModel(TransporteRepository repositorio, TaskHelper taskHelper, TransporteMapper transporteMapper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
        this.transporteMapper = transporteMapper;
    }

    public LiveData<List<TransporteUiState>> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void recomendar(long categoria, int quantidade) {
        taskHelper.execute(
                () -> transporteMapper
                        .mapFrom(repositorio.recomendarTransportes(categoria, quantidade)),
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}