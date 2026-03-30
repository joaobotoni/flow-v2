package com.botoni.flow.ui.mappers;

public interface Mapper<I, O> {
    O mapper(I i);
}
