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
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DetalhePrecificacaoViewModel extends ViewModel {
    private final TaskHelper taskHelper;
    private final PrecificacaoBezerroRepository repository;
    private final MutableLiveData<List<DetalhePrecoBezerroUiState>> lista = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<BigDecimal> valorTotal = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<DetalhePrecoBezerroUiState> itemParaEditar = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    @Inject
    public DetalhePrecificacaoViewModel(PrecificacaoBezerroRepository repository, TaskHelper taskHelper) {
        this.repository = repository;
        this.taskHelper = taskHelper;
    }

    public LiveData<List<DetalhePrecoBezerroUiState>> getLista() {
        return lista;
    }

    public LiveData<BigDecimal> getValorTotal() {
        return valorTotal;
    }

    public LiveData<DetalhePrecoBezerroUiState> getItemParaEditar() {
        return itemParaEditar;
    }

    public void adicionarItem(BigDecimal peso, BigDecimal arroba, BigDecimal percent) {
        taskHelper.execute(
                () -> calcularNovoItem(peso, arroba, percent),
                this::anexarItemNaLista,
                error::postValue
        );
    }

    public void removerItem(int id) {
        List<DetalhePrecoBezerroUiState> listaAtual = obterListaAtual();
        List<DetalhePrecoBezerroUiState> listaFiltrada = filtrarItemPorId(listaAtual, id);
        lista.postValue(listaFiltrada);
        valorTotal.postValue(calcularSomaTotal(listaFiltrada));
    }

    public void editarItem(DetalhePrecoBezerroUiState detalhe) {
        itemParaEditar.postValue(detalhe);
        removerItem(detalhe.getId());
    }

    private DetalhePrecoBezerroUiState calcularNovoItem(BigDecimal peso, BigDecimal arroba, BigDecimal percent) {
        BigDecimal total = repository.calcularValorTotalBezerro(peso, arroba, percent);
        BigDecimal porKg = repository.calcularValorPorKg(peso, arroba, percent);
        return new DetalhePrecoBezerroUiState(peso, total, porKg);
    }

    private void anexarItemNaLista(DetalhePrecoBezerroUiState novoItem) {
        List<DetalhePrecoBezerroUiState> listaAtualizada = new ArrayList<>(obterListaAtual());
        listaAtualizada.add(novoItem);
        lista.postValue(listaAtualizada);
        valorTotal.postValue(calcularSomaTotal(listaAtualizada));
    }

    private List<DetalhePrecoBezerroUiState> filtrarItemPorId(List<DetalhePrecoBezerroUiState> origem, int id) {
        return origem.stream()
                .filter(item -> item.getId() != id)
                .collect(Collectors.toList());
    }

    private BigDecimal calcularSomaTotal(List<DetalhePrecoBezerroUiState> itens) {
        return itens.stream()
                .map(DetalhePrecoBezerroUiState::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DetalhePrecoBezerroUiState> obterListaAtual() {
        List<DetalhePrecoBezerroUiState> atual = lista.getValue();
        return atual != null ? atual : new ArrayList<>();
    }
}
