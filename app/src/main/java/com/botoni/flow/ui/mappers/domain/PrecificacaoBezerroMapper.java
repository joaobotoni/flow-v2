package com.botoni.flow.ui.mappers.domain;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.ui.mappers.BiMapper;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;

import javax.inject.Inject;

public class PrecificacaoBezerroMapper implements BiMapper<PrecificacaoBezerroUiState, PrecificacaoBezerro> {

    @Inject
    public PrecificacaoBezerroMapper() {
    }

    @Override
    public PrecificacaoBezerro mapTo(PrecificacaoBezerroUiState precificacaoBezerroUiState) {
        return new PrecificacaoBezerro(
                precificacaoBezerroUiState.getValorPorKg(),
                precificacaoBezerroUiState.getValorPorCabeca(),
                precificacaoBezerroUiState.getValorTotal());
    }

    @Override
    public PrecificacaoBezerroUiState mapFrom(PrecificacaoBezerro precificacaoBezerro) {
        return new PrecificacaoBezerroUiState(
                precificacaoBezerro.getValorPorKg(),
                precificacaoBezerro.getValorPorCabeca(),
                precificacaoBezerro.getValorTotal());
    }
}
