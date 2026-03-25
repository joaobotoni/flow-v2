package com.botoni.flow.data.models;

import java.math.BigDecimal;

public class PrecificacaoFrete {
    private final BigDecimal valorTotal;
    private final BigDecimal valorParcial;

    public PrecificacaoFrete(BigDecimal valorTotal, BigDecimal valorParcial) {
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
