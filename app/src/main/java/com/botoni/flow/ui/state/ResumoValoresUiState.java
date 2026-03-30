package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class ResumoValoresUiState {
    private final BigDecimal valorPrincipal;
    private final BigDecimal valorSecundario;
    public ResumoValoresUiState(BigDecimal valorPrincipal, BigDecimal valorSecundario) {
        this.valorPrincipal = valorPrincipal;
        this.valorSecundario = valorSecundario;
    }
    public BigDecimal getValorPrincipal() {
        return valorPrincipal;
    }

    public BigDecimal getValorSecundario() {
        return valorSecundario;
    }
}
