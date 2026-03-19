package com.botoni.flow.data.model;

import java.util.Objects;

public class Transporte {

    private long id;
    private String nome;
    private Integer quantidade;
    private Integer percentual;
    private Integer capacidade;

    public Transporte() {}

    public Transporte(long id, String nome, Integer quantidade, Integer capacidade, Integer percentual) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.capacidade = capacidade;
        this.percentual = percentual;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Integer getPercentual() { return percentual; }
    public void setPercentual(Integer percentual) { this.percentual = percentual; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transporte transporte = (Transporte) o;
        return id == transporte.id
                && Objects.equals(nome, transporte.nome)
                && Objects.equals(quantidade, transporte.quantidade)
                && Objects.equals(percentual, transporte.percentual)
                && Objects.equals(capacidade, transporte.capacidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, quantidade, percentual, capacidade);
    }
}