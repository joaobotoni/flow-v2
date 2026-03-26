package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.LocalizacaoRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.BuscaLocalizacaoUiState;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BuscaViewModel extends ViewModel {

    private final LocalizacaoRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<BuscaLocalizacaoUiState> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public BuscaViewModel(LocalizacaoRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public LiveData<BuscaLocalizacaoUiState> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void buscar(String consulta, double latitude, double longitude) {
        taskHelper.execute(
                () -> {
                    String codigo = repositorio.paisDeCoordenadas(latitude, longitude).orElseThrow(() ->
                            new RuntimeException("Código do pais não encontrado"));
                    return new BuscaLocalizacaoUiState(repositorio.enderecosPorTexto(consulta, codigo), false);
                },
                state::postValue,
                error::postValue
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}