package model;

import lombok.*;

import java.time.LocalDateTime;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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