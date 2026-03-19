package com.botoni.flow.ui.viewmodel;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.R;
import com.botoni.flow.data.repositories.LocalizacaoRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.RotaUiState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class RotaViewModel extends ViewModel {

    private static final double DESTINO_LAT = -15.574583992567458;
    private static final double DESTINO_LNG = -56.090944897864865;

    private final Context context;
    private final LocalizacaoRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<RotaUiState> uiState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public RotaViewModel(@ApplicationContext Context context, LocalizacaoRepository repositorio, TaskHelper taskHelper) {
        this.context = context;
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
    }

    public void selecionar(Address origem) {
        taskHelper.execute(() -> {
            try {
                return montar(origem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, state -> {
            uiState.postValue(state);
            visivel.postValue(state != null);
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(null);
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<RotaUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    private RotaUiState montar(Address origem) throws IOException {
        Address destino = buscarDestino();
        double distancia = repositorio.parseDistance(repositorio.fetchRoute(origem, destino));
        List<String> pontos = Arrays.asList(formatar(origem), formatar(destino));
        return new RotaUiState(pontos, distancia);
    }

    private Address buscarDestino() throws IOException {
        List<Address> resultados = new Geocoder(context).getFromLocation(DESTINO_LAT, DESTINO_LNG, 1);
        if (resultados == null || resultados.isEmpty()) {
            throw new IOException(context.getString(R.string.erro_endereco_nao_encontrado));
        }
        return resultados.get(0);
    }

    private String formatar(Address endereco) {
        String cidade = endereco.getLocality() != null ? endereco.getLocality() : endereco.getSubAdminArea();
        String estado = endereco.getAdminArea();
        return cidade != null && estado != null
                ? String.format("%s, %s", cidade, estado)
                : endereco.getAddressLine(0);
    }
}