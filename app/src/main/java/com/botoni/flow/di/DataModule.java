package com.botoni.flow.di;

import android.content.Context;

import com.botoni.flow.data.source.local.AppDatabase;
import com.botoni.flow.data.source.local.dao.CapacidadeFreteDao;
import com.botoni.flow.data.source.local.dao.CategoriaFreteDao;
import com.botoni.flow.data.source.local.dao.FreteDao;
import com.botoni.flow.data.source.local.dao.TipoVeiculoFreteDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;


@Module
@InstallIn(SingletonComponent.class)
public class DataModule {

    @Provides
    @Singleton
    public FreteDao provideFreteDao(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context).freteDao();
    }

    @Provides
    @Singleton
    public CategoriaFreteDao provideCategoriaFreteDao(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context).categoriaFreteDao();
    }

    @Provides
    @Singleton
    public CapacidadeFreteDao provideCapacidadeFreteDao(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context).capacidadeFreteDao();
    }

    @Provides
    @Singleton
    public TipoVeiculoFreteDao provideTipoVeiculoFreteDao(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context).tipoVeiculoFreteDao();
    }
}
