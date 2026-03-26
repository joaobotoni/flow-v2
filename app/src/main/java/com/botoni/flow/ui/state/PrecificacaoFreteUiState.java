package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class PrecificacaoFreteUiState {
    private final BigDecimal valorTotal;
    private final BigDecimal valorParcial;

    public PrecificacaoFreteUiState(BigDecimal valorTotal, BigDecimal valorParcial) {
        this.valorTotal = valorTotal;
        this.valorParcial = valorParcial;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorParcial() {
        return valorParcial;
    }
}
