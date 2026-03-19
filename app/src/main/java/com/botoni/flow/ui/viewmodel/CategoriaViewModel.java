package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.CategoriaFreteRepository;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.CategoriaUiState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoriaViewModel extends ViewModel {

    private final CategoriaFreteRepository repositorio;
    private final TaskHelper taskHelper;

    private final MutableLiveData<List<CategoriaUiState>> uiState = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> visivel = new MutableLiveData<>(false);
    private final MutableLiveData<Exception> erro = new MutableLiveData<>(null);

    @Inject
    public CategoriaViewModel(CategoriaFreteRepository repositorio, TaskHelper taskHelper) {
        this.repositorio = repositorio;
        this.taskHelper = taskHelper;
        carregar();
    }

    public void selecionar(CategoriaUiState item) {
        taskHelper.execute(() -> marcar(item), lista -> {
            uiState.postValue(lista);
            visivel.postValue(lista != null && !lista.isEmpty());
        }, erro::postValue);
    }

    public void limpar() {
        uiState.postValue(Collections.emptyList());
        visivel.postValue(false);
        erro.postValue(null);
    }

    public LiveData<List<CategoriaUiState>> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getVisivel() {
        return visivel;
    }

    public LiveData<Exception> getErro() {
        return erro;
    }

    private void carregar() {
        taskHelper.execute(this::montar, lista -> {
            uiState.postValue(lista);
            visivel.postValue(lista != null && !lista.isEmpty());
        }, erro::postValue);
    }

    private List<CategoriaUiState> montar() {
        List<CategoriaUiState> lista = new ArrayList<>();
        for (CategoriaFrete categoria : repositorio.getAll()) {
            lista.add(converter(categoria, false));
        }
        return lista;
    }

    private List<CategoriaUiState> marcar(CategoriaUiState selecionada) {
        List<CategoriaUiState> atual = uiState.getValue();
        if (atual == null) return Collections.emptyList();
        List<CategoriaUiState> lista = new ArrayList<>();
        for (CategoriaUiState item : atual) {
            boolean estaSelecionada = selecionada != null && item.id == selecionada.id;
            lista.add(new CategoriaUiState(item.id, item.descricao, estaSelecionada));
        }
        return lista;
    }

    private CategoriaUiState converter(CategoriaFrete categoria, boolean selecionada) {
        return new CategoriaUiState(categoria.getId(), categoria.getDescricao(), selecionada);
    }
}