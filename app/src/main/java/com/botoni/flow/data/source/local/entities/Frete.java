package com.botoni.flow.data.source.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "xgp_frete")
public class Frete {
    @ColumnInfo(name = "id_frete")
    @PrimaryKey(autoGenerate = true)
    private Long id;
    @ColumnInfo(name = "id_tipo_veiculo_frete")
    private Long idTipoVeiculoFrete;
    @ColumnInfo(name = "tipo_cobranca")
    private int tipoCobranca;
    @ColumnInfo(name = "km_inicial")
    private double kmInicial;
    @ColumnInfo(name = "km_final")
    private double kmFinal;
    @ColumnInfo(name = "valor")
    private double valor;


    public Frete(Long idTipoVeiculoFrete, int tipoCobranca, double kmInicial, double kmFinal, double valor) {
        this.idTipoVeiculoFrete = idTipoVeiculoFrete;
        this.tipoCobranca = tipoCobranca;
        this.kmInicial = kmInicial;
        this.kmFinal = kmFinal;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdTipoVeiculoFrete() {
        return idTipoVeiculoFrete;
    }

    public void setIdTipoVeiculoFrete(Long idTipoVeiculoFrete) {
        this.idTipoVeiculoFrete = idTipoVeiculoFrete;
    }

    public double getKmInicial() {
        return kmInicial;
    }

    public void setKmInicial(double kmInicial) {
        this.kmInicial = kmInicial;
    }

    public double getKmFinal() {
        return kmFinal;
    }

    public void setKmFinal(double kmFinal) {
        this.kmFinal = kmFinal;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getTipoCobranca() {
        return tipoCobranca;
    }

    public void setTipoCobranca(int tipoCobranca) {
        this.tipoCobranca = tipoCobranca;
    }
}