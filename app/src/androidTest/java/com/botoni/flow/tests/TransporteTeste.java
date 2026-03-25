package com.botoni.flow.tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.data.repositories.TransporteRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class TransporteTeste {

    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
    @Inject
    TransporteRepository transporteRepository;

    @Before
    public void init() { hiltRule.inject(); }

    @Test
    public void recomendarTransporte_deveRetornarTransportesAdequados() {
        List<Transporte> result = transporteRepository.recomendarTransportes(1L, 200);
        result.forEach(t -> System.out.println("Veículo: " + t.getNomeVeiculo() + " | Quantidade: " + t.getQuantidade()));
    }
}
