package com.botoni.flow.ui.mappers.domain;

import com.botoni.flow.data.models.PrecificacaoFrete;
import com.botoni.flow.ui.mappers.BiMapper;
import com.botoni.flow.ui.state.PrecificacaoFreteUiState;

public class PrecificacaoFreteMapper implements BiMapper<PrecificacaoFreteUiState, PrecificacaoFrete> {
    @Override
    public PrecificacaoFrete mapTo(PrecificacaoFreteUiState freteUiState) {
        return new PrecificacaoFrete(freteUiState.getValorTotal(), freteUiState.getValorParcial());
    }

    @Override
    public PrecificacaoFreteUiState mapFrom(PrecificacaoFrete precificacaoFrete) {
        return new PrecificacaoFreteUiState(precificacaoFrete.getValorTotal(), precificacaoFrete.getValorParcial());
    }
}
