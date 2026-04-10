package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.PrecificacaoBezerroRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DetalhePrecificacaoViewModel extends ViewModel {
    private final TaskHelper taskHelper;
    private final PrecificacaoBezerroRepository repository;
    private final MutableLiveData<List<DetalhePrecoBezerroUiState>> state = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<BigDecimal> total = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();
    @Inject
    public DetalhePrecificacaoViewModel(PrecificacaoBezerroRepository repository, TaskHelper taskHelper) {
        this.repository = repository;
        this.taskHelper = taskHelper;
    }

    public LiveData<List<DetalhePrecoBezerroUiState>> getState() {
        return state;
    }

    public LiveData<BigDecimal> getTotal() {
        return total;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void adicionarItem(BigDecimal peso, BigDecimal arroba, BigDecimal percent, BigDecimal pesoBase) {
        List<DetalhePrecoBezerroUiState> lista = listaAtual();
        taskHelper.execute(
                () -> adicionarNaLista(lista, peso, arroba, percent, pesoBase),
                this::publicar,
                error::postValue
        );
    }

    public void atualizarItem(int id, BigDecimal peso, BigDecimal arroba, BigDecimal percent, BigDecimal pesoBase) {
        List<DetalhePrecoBezerroUiState> lista = listaAtual();
        taskHelper.execute(
                () -> update(lista, id, calcularItem(id, peso, arroba, percent, pesoBase)),
                this::publicar,
                error::postValue
        );
    }

    public void removerItem(int id) {
        List<DetalhePrecoBezerroUiState> lista = listaAtual();
        taskHelper.execute(
                () -> remove(lista, id),
                this::publicar,
                error::postValue
        );
    }

    private List<DetalhePrecoBezerroUiState> adicionarNaLista(
            List<DetalhePrecoBezerroUiState> lista, BigDecimal peso, BigDecimal arroba, BigDecimal percent, BigDecimal pesoBase) {
        lista.add(calcularItem(lista.size(), peso, arroba, percent, pesoBase));
        return lista;
    }

    private List<DetalhePrecoBezerroUiState> update(
            List<DetalhePrecoBezerroUiState> lista, int id, DetalhePrecoBezerroUiState item) {
        lista.set(id, item);
        return lista;
    }

    private List<DetalhePrecoBezerroUiState> remove(List<DetalhePrecoBezerroUiState> lista, int id) {
        lista.remove(id);
        return reindexar(lista);
    }

    private void publicar(List<DetalhePrecoBezerroUiState> lista) {
        state.postValue(lista);
        total.postValue(
                lista.stream()
                        .map(DetalhePrecoBezerroUiState::getValorTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    private List<DetalhePrecoBezerroUiState> reindexar(List<DetalhePrecoBezerroUiState> lista) {
        List<DetalhePrecoBezerroUiState> reindexada = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            DetalhePrecoBezerroUiState item = lista.get(i);
            reindexada.add(new DetalhePrecoBezerroUiState(i, item.getPeso(), item.getValorTotal(), item.getValorPorKg()));
        }
        return reindexada;
    }

    private DetalhePrecoBezerroUiState calcularItem(int id, BigDecimal peso, BigDecimal arroba, BigDecimal percent, BigDecimal pesoBase) {
        return new DetalhePrecoBezerroUiState(
                id, peso,
                repository.calcularValorTotalBezerroComFrete(peso, arroba, percent, pesoBase),
                repository.calcularValorPorKgComFrete(peso, arroba, percent, pesoBase)
        );
    }

    private List<DetalhePrecoBezerroUiState> listaAtual() {
        List<DetalhePrecoBezerroUiState> lista = state.getValue();
        return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
    }

    public int size() {
        List<DetalhePrecoBezerroUiState> lista = state.getValue();
        return lista != null ? lista.size() : 0;
    }
}