package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.TransportRepository;
import com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.CalfResultUiState;

import java.math.BigDecimal;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalfResultViewModel extends ViewModel {
    private final TaskHelper taskExecutor;
    private final AvaliacaoPrecoAnimalUseCase useCase;
    private final MutableLiveData<CalfResultUiState> uiState = new MutableLiveData<>(new CalfResultUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();
    private static final BigDecimal ARROBA = new BigDecimal("310");
    private static final BigDecimal PERCENT = new BigDecimal("30");

    @Inject
    public CalfResultViewModel(TaskHelper taskExecutor, AvaliacaoPrecoAnimalUseCase useCase) {
        this.taskExecutor = taskExecutor;
        this.useCase = useCase;
    }

    public void calculate(BigDecimal peso, int quantity) {
        taskExecutor.execute(
                () -> setState(peso, quantity),
                uiState::setValue,
                errorEvent::setValue
        );
    }

    private CalfResultUiState setState(BigDecimal peso, int quantity) {
        BigDecimal valorPorKg = useCase.calcularValorTotalPorKg(peso, ARROBA, PERCENT);
        BigDecimal valorPorCabeca = useCase.calcularValorTotalBezerro(peso, ARROBA, PERCENT);
        BigDecimal valorTotal = useCase.calcularValorTotalTodosBezerros(valorPorCabeca, quantity);
        return new CalfResultUiState(valorPorKg, valorPorCabeca, valorTotal, true);
    }

    public LiveData<CalfResultUiState> getUiState() {
        return uiState;
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    public void reset() {
        uiState.setValue(new CalfResultUiState());
    }
}