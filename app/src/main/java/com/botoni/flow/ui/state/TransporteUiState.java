package com.botoni.flow.ui.state;

public class TransporteUiState {

    public final long id;
    public final String nomeVeiculo;
    public final int quantidade;
    public final int capacidade;
    public final int ocupacao;
    public TransporteUiState(long id, String nomeVeiculo, int quantidade, int capacidade, int ocupacao) {
        this.id = id;
        this.nomeVeiculo = nomeVeiculo;
        this.quantidade = quantidade;
        this.capacidade = capacidade;
        this.ocupacao = ocupacao;
    }

    public long getId() {
        return id;
    }

    public String getNomeVeiculo() {
        return nomeVeiculo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getCapacidade() {
        return capacidade;
    }
    public int getOcupacao() {
        return ocupacao;
    }
}