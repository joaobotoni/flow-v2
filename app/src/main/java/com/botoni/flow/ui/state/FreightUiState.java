package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class FreightUiState {
    private BigDecimal valorFreteTotal;
    private BigDecimal valorFretePorAnimal;
    private boolean isVisible;

    public FreightUiState() {
    }

    public FreightUiState(BigDecimal valorFreteTotal, BigDecimal valorFretePorAnimal, boolean isVisible) {
        this.valorFreteTotal = valorFreteTotal;
        this.valorFretePorAnimal = valorFretePorAnimal;
        this.isVisible = isVisible;
    }

    public BigDecimal getValorFretePorAnimal() {
        return valorFretePorAnimal;
    }

    public BigDecimal getValorFreteTotal() {
        return valorFreteTotal;
    }

    public boolean isVisible() {
        return isVisible;
    }
}