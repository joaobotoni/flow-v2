package com.botoni.flow.ui.mappers;

public interface BiMapper<I, O> {
    O mapTo(I i);
    I mapFrom(O o);
}
