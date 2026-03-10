package com.botoni.flow.ui.viewmodel;

import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalBezerro;
import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalPorKg;
import static com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase.calcularValorTotalTodosBezerros;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.ui.state.DealUiState;
import com.botoni.flow.ui.state.RouteState;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DealViewModel extends ViewModel {
    private final MutableLiveData<DealUiState> uiState = new MutableLiveData<>(new DealUiState());

    @Inject
    public DealViewModel() {
    }

    public LiveData<DealUiState> getUiState() {
        return uiState;
    }

    public void setCategoriaSelected(String categoria) {
        DealUiState current = uiState.getValue();
        uiState.setValue(new DealUiState(
                current.isFreteVisible(),
                current.getRouteState(),
                categoria,
                current.getValorPorKg(),
                current.getValorPorCabeca(),
                current.getValorTotal(),
                current.isVisible()
        ));
    }

    public void setRouteLinked(List<String> points, double distance) {
        DealUiState current = uiState.getValue();
        uiState.setValue(new DealUiState(
                true,
                new RouteState(points, distance),
                current.getSelectedCategory(),
                current.getValorPorKg(),
                current.getValorPorCabeca(),
                current.getValorTotal(),
                current.isVisible()
        ));
    }

    public void calculate(BigDecimal peso, int quantidade) {
        BigDecimal precoArroba = new BigDecimal("310.0");
        BigDecimal percentual = new BigDecimal("30.0");

        DealUiState current = uiState.getValue();
        BigDecimal valorPorKg = calcularValorTotalPorKg(peso, precoArroba, percentual);
        BigDecimal valorPorCabeca = calcularValorTotalBezerro(peso, precoArroba, percentual);
        BigDecimal valorTotal = calcularValorTotalTodosBezerros(valorPorCabeca, quantidade);

        uiState.setValue(new DealUiState(
                current.isFreteVisible(),
                current.getRouteState(),
                current.getSelectedCategory(),
                valorPorKg,
                valorPorCabeca,
                valorTotal,
                true
        ));
    }
    public void clear() {
        uiState.setValue(new DealUiState());
    }
}