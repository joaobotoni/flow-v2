package com.botoni.flow.ui.mappers.presentation;

import com.botoni.flow.ui.mappers.Mapper;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;
import com.botoni.flow.ui.state.ResumoValoresUiState;

import javax.inject.Inject;

public class FreteResumoMapper implements Mapper<PrecificacaoFreteUiState, ResumoValoresUiState> {
    @Inject
    public FreteResumoMapper() {
    }

    @Override
    public ResumoValoresUiState mapper(PrecificacaoFreteUiState state) {
        return new ResumoValoresUiState(state.getValorParcial(), state.getValorParcial());
    }
}
