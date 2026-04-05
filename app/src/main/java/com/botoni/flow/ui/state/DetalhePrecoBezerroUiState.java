package com.botoni.flow.ui.state;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public class DetalhePrecoBezerroUiState {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final int id;
    private final BigDecimal peso;
    private final BigDecimal valorTotal;
    private final BigDecimal valorPorKg;

    public DetalhePrecoBezerroUiState(BigDecimal peso, BigDecimal valorTotal, BigDecimal valorPorKg) {
        this.id = COUNTER.incrementAndGet();
        this.peso = peso;
        this.valorTotal = valorTotal;
        this.valorPorKg = valorPorKg;
    }

    public DetalhePrecoBezerroUiState(int id, BigDecimal peso, BigDecimal valorTotal, BigDecimal valorPorKg) {
        this.id = id;
        this.peso = peso;
        this.valorTotal = valorTotal;
        this.valorPorKg = valorPorKg;
    }

    public int getId() { return id; }
    public BigDecimal getPeso() { return peso; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getValorPorKg() { return valorPorKg; }
}