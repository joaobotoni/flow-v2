package com.botoni.flow.tests;

import android.location.Address;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.botoni.flow.data.models.Rota;
import com.botoni.flow.data.repositories.LocalizacaoRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class LocalizacaoTeste {
    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
    @Inject
    LocalizacaoRepository localizacaoRepository;

    @Before
    public void init() { hiltRule.inject(); }

    @Test
    public void calcularRota_deveRetornarRotaCorreta() throws Exception {
        Address origem = localizacaoRepository.enderecoPorNome("Sinop").orElseThrow();
        Address destino = localizacaoRepository.enderecoPorNome("Cuiabá").orElseThrow();
        Rota result = localizacaoRepository.calcularRota(origem, destino);
        System.out.printf("\nROTA: %s -> %s (%.2f km)%n", result.getCidadeOrigem(), result.getCidadeDestino(), result.getDistancia());
    }
}
