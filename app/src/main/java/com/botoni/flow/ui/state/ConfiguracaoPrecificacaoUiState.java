package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class ConfiguracaoPrecificacaoUiState {

    private final BigDecimal arrobaBoiGordo;
    private final BigDecimal agioBoiGordo;
    private final BigDecimal pesoRefBoiGordo;
    private final BigDecimal arrobaVacaGorda;
    private final BigDecimal agioVacaGorda;
    private final BigDecimal pesoRefVacaGorda;

    public ConfiguracaoPrecificacaoUiState(BigDecimal arrobaBoiGordo, BigDecimal agioBoiGordo, BigDecimal pesoRefBoiGordo, BigDecimal arrobaVacaGorda, BigDecimal agioVacaGorda, BigDecimal pesoRefVacaGorda) {
        this.arrobaBoiGordo = arrobaBoiGordo;
        this.agioBoiGordo = agioBoiGordo;
        this.pesoRefBoiGordo = pesoRefBoiGordo;
        this.arrobaVacaGorda = arrobaVacaGorda;
        this.agioVacaGorda = agioVacaGorda;
        this.pesoRefVacaGorda = pesoRefVacaGorda;
    }

    public BigDecimal getArrobaBoiGordo() {
        return arrobaBoiGordo;
    }

    public BigDecimal getAgioBoiGordo() {
        return agioBoiGordo;
    }

    public BigDecimal getPesoRefBoiGordo() {
        return pesoRefBoiGordo;
    }

    public BigDecimal getArrobaVacaGorda() {
        return arrobaVacaGorda;
    }

    public BigDecimal getAgioVacaGorda() {
        return agioVacaGorda;
    }

    public BigDecimal getPesoRefVacaGorda() {
        return pesoRefVacaGorda;
    }
}
