package com.botoni.flow.tests;

import static org.junit.Assert.assertFalse;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.botoni.flow.data.repositories.CategoriaFreteRepository;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;

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
public class CategoriaFreteTest {
    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
    @Inject
    CategoriaFreteRepository categoriaFreteRepository;

    @Before
    public void init() {
        hiltRule.inject();
    }

    @Test
    public void listarCategorias_deveRetornarLista() {
        List<CategoriaFrete> result = categoriaFreteRepository.getAll();
        System.out.println("\nCATEGORIAS");
        result.forEach(c -> System.out.printf("%-5d %s%n", c.getId(), c.getDescricao()));
    }
}
