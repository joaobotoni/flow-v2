package com.botoni.flow.ui.mappers.domain;

import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.ui.mappers.BiMapper;
import com.botoni.flow.ui.state.CategoriaUiState;

import javax.inject.Inject;

public class CategoriaMapper implements BiMapper<CategoriaUiState, CategoriaFrete> {

    @Inject
    public CategoriaMapper() {
    }

    @Override
    public CategoriaFrete mapTo(CategoriaUiState state) {
        return new CategoriaFrete(state.getId(), state.getDescricao());
    }

    @Override
    public CategoriaUiState mapFrom(CategoriaFrete entity) {
        return new CategoriaUiState(entity.getId(), entity.getDescricao(), false);
    }
}
