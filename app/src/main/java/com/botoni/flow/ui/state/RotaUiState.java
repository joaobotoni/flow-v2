package com.botoni.flow.ui.state;

import java.util.Collections;
import java.util.List;

public class RotaUiState {
    public final List<String> pontos;
    public final double distancia;

    public RotaUiState() {
        this.pontos = Collections.emptyList();
        this.distancia = 0.0;
    }

    public RotaUiState(List<String> pontos, double distancia) {
        this.pontos = pontos;
        this.distancia = distancia;
    }
}