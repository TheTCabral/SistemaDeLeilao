package model;

import lombok.Setter;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lance {

    private int id;
    private Comprador comprador;
    private double valor;
    private LocalDateTime dataHora;

    public Lance(Comprador comprador, double valor) {
        setComprador(comprador);
        setValor(valor);
        this.dataHora = LocalDateTime.now();
    }

    public int getId() { return id; }
    public Comprador getComprador() { return comprador; }
    public double getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }

    public void setComprador(Comprador comprador) {
        if (comprador == null) {
            throw new IllegalArgumentException("Lance deve ter um comprador.");
        }
        this.comprador = comprador;
    }

    public void setValor(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do lance deve ser positivo.");
        }
        this.valor = valor;
    }
}