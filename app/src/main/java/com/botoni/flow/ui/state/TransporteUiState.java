package com.botoni.flow.ui.state;

import com.botoni.flow.data.model.Transporte;

import java.util.List;


public class TransporteUiState {

    public final long id;
    public final String nomeVeiculo;
    public final int quantidade;
    public final int capacidade;
    public final int percentual;

    public TransporteUiState() {
        this.id = 0;
        this.nomeVeiculo = null;
        this.quantidade = 0;
        this.capacidade = 0;
        this.percentual = 0;
    }

    public TransporteUiState(long id, String nomeVeiculo, int quantidade, int capacidade, int percentual) {
        this.id = id;
        this.nomeVeiculo = nomeVeiculo;
        this.quantidade = quantidade;
        this.capacidade = capacidade;
        this.percentual = percentual;
    }
}