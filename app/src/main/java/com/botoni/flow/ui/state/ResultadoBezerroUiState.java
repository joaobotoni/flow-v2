package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class ResultadoBezerroUiState {

    public final BigDecimal valorPorKg;
    public final BigDecimal valorPorCabeca;
    public final BigDecimal valorTotal;

    public ResultadoBezerroUiState() {
        this.valorPorKg = null;
        this.valorPorCabeca = null;
        this.valorTotal = null;
    }

    public ResultadoBezerroUiState(BigDecimal valorPorKg, BigDecimal valorPorCabeca, BigDecimal valorTotal) {
        this.valorPorKg = valorPorKg;
        this.valorPorCabeca = valorPorCabeca;
        this.valorTotal = valorTotal;
    }
}