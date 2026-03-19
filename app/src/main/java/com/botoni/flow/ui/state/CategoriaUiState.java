package com.botoni.flow.ui.state;


import com.botoni.flow.data.source.local.entities.CategoriaFrete;

import java.util.List;

public class CategoriaUiState {

    public final long id;
    public final String descricao;
    public final boolean selecionada;


    public CategoriaUiState() {
        this.id = 0;
        this.descricao = null;
        this.selecionada = false;
    }

    public CategoriaUiState(long id, String descricao, boolean selecionada) {
        this.id = id;
        this.descricao = descricao;
        this.selecionada = selecionada;
    }
}