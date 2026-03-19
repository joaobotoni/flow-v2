package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class FreteUiState {

    public final BigDecimal valorTotal;
    public final BigDecimal valorPorAnimal;

    public FreteUiState() {
        this.valorTotal = BigDecimal.ZERO;
        this.valorPorAnimal = BigDecimal.ZERO;
    }
    public FreteUiState(BigDecimal valorTotal, BigDecimal valorPorAnimal) {
        this.valorTotal = valorTotal;
        this.valorPorAnimal = valorPorAnimal;
    }
}