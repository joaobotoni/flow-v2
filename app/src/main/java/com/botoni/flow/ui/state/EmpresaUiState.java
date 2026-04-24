package com.botoni.flow.ui.state;

public class EmpresaUiState {
    private final Integer id;
    private final String nome;
    private final String iniciais;
    private final boolean selecionada;

    public EmpresaUiState(Integer id, String nome, String iniciais, boolean selecionada) {
        this.id = id;
        this.nome = nome;
        this.iniciais = iniciais;
        this.selecionada = selecionada;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getIniciais() {
        return iniciais;
    }

    public boolean isSelecionada() {
        return selecionada;
    }
}