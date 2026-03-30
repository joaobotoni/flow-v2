package com.botoni.flow.ui.mappers.presentation;

import com.botoni.flow.ui.mappers.Mapper;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;

import javax.inject.Inject;

public class BezerroResumoMapper implements Mapper<PrecificacaoBezerroUiState, ResumoValoresUiState> {

    @Inject
    public BezerroResumoMapper() {
    }

    @Override
    public ResumoValoresUiState mapper(PrecificacaoBezerroUiState state) {
        return new ResumoValoresUiState(state.getValorPorCabeca(), state.getValorPorKg());
    }
}
