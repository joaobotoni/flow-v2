package com.botoni.flow.data.repositories.local;

import android.content.Context;

import com.botoni.flow.data.source.local.AppDatabase;
import com.botoni.flow.data.source.local.entities.Frete;
import com.botoni.flow.domain.entities.Transport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class FreteRepository {
    private final AppDatabase database;

    @Inject
    public FreteRepository(@ApplicationContext Context context) {
        this.database = AppDatabase.getDatabase(context);
    }
    public List<Frete> getAll() {
        return database.freteDao().getAll();
    }

    public Frete findById(long id) {
        return database.freteDao().findById(id);
    }

    public Frete findByValueInRange(long id, double range) {
        return database.freteDao().findByVehicleAndDistance(id, range);
    }

    public long insert(Frete frete) {
        return database.freteDao().insert(frete);
    }

    public void insertAll(List<Frete> fretes) {
        database.freteDao().insertAll(fretes);
    }

    public int update(Frete frete) {
        return database.freteDao().update(frete);
    }

    public int delete(Frete frete) {
        return database.freteDao().delete(frete);
    }

    public void deleteAll() {
        database.freteDao().deleteAll();
    }

    public BigDecimal calcularTotal(List<Transport> transports, double distance) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transport transport : transports) {
            Frete frete = database.freteDao()
                    .findByVehicleAndDistance(transport.getId(), distance);
            if (frete != null) {
                BigDecimal valor = BigDecimal.valueOf(frete.getValor());
                BigDecimal quantity = BigDecimal.valueOf(transport.getQuantity());
                total = total.add(valor.multiply(quantity));
            }
        }
        return total;
    }

    public BigDecimal calcularPorCabeca(List<Transport> transports, double distance, int animalCount) {
        return calcularTotal(transports, distance)
                .divide(BigDecimal.valueOf(animalCount), 2, RoundingMode.HALF_UP);
    }
}