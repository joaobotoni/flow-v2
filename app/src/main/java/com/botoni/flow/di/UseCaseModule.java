package com.botoni.flow.di;

import com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {
    @Provides
     public AvaliacaoPrecoAnimalUseCase provideUseCase(){
        return new AvaliacaoPrecoAnimalUseCase();
    }
}
