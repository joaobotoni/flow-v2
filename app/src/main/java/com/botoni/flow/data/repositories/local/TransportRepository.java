package com.botoni.flow.data.repositories.local;

import android.content.Context;

import com.botoni.flow.data.source.local.AppDatabase;
import com.botoni.flow.data.source.local.entities.CapacidadeFrete;
import com.botoni.flow.domain.entities.Transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class TransportRepository {
    private final AppDatabase database;

    @Inject
    public TransportRepository(@ApplicationContext Context context) {
        this.database = AppDatabase.getDatabase(context);
    }

    public List<Transport> recommend(long category, int quantity) {
        List<CapacidadeFrete> capacities = capacities(category);
        if (capacities.isEmpty()) return Collections.emptyList();
        capacities.sort((a, b) -> Integer.compare(b.getQtdeFinal(), a.getQtdeFinal()));
        return greedy(capacities, quantity);
    }

    private List<Transport> greedy(List<CapacidadeFrete> capacities, int total) {
        List<Transport> result = new ArrayList<>();
        int remaining = total;
        for (CapacidadeFrete c : capacities) {
            if (remaining <= 0) break;
            int before = remaining;
            int count = 0;
            while (remaining >= c.getQtdeInicial()) {
                remaining -= c.getQtdeFinal();
                count++;
            }
            if (count > 0) {
                int loaded = before - Math.max(remaining, 0);
                result.add(map(c, count, loaded));
            }
        }
        return result;
    }

    private Transport map(CapacidadeFrete c, int count, int loaded) {
        long id = c.getIdTipoVeiculoFrete();
        return new Transport(id, database.tipoVeiculoFreteDao().findById(id).getDescricao(),
                count, c.getQtdeFinal(), Math.min(100, loaded * 100 / (count * c.getQtdeFinal())));
    }

    private List<CapacidadeFrete> capacities(long category) {
        List<CapacidadeFrete> capacities = database.capacidadeFreteDao().findByCategoria(category);
        return capacities != null ? capacities : Collections.emptyList();
    }
}