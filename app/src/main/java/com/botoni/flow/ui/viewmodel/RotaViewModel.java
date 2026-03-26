package com.botoni.flow.ui.viewmodel;

import android.location.Address;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.Rota;
import com.botoni.flow.data.repositories.LocalizacaoRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.RotaUiState;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RotaViewModel extends ViewModel {

    private final LocalizacaoRepository repositorio;
    private final TaskHelper taskHelper;
    private static final String DESTINO_QUERY = "Cuiabá";

    private final MutableLiveData<RotaUiState> state = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public RotaViewModel(TaskHelper taskHelper, LocalizacaoRepository repositorio) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public LiveData<RotaUiState> getState() {
        return state;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void selecionar(Address origem) {
        taskHelper.execute(
                () -> calcularRota(origem),
                state::postValue,
                error::postValue
        );
    }

    private RotaUiState calcularRota(Address origem) throws Exception {
        Address destino = repositorio.enderecoPorNome(DESTINO_QUERY).orElseThrow();
        Rota resposta = repositorio.calcularRota(origem, destino);
        return new RotaUiState(
                resposta.getCidadeOrigem(),
                resposta.getEstadoOrigem(),
                resposta.getCidadeDestino(),
                resposta.getEstadoDestino(),
                resposta.getDistancia()
        );
    }

    public void limpar() {
        state.setValue(null);
    }
}