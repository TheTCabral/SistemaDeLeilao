package model;

import lombok.Setter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {

    private int id;
    private String nome;
    private String descricao;
    private double valorInicial;
    private Vendedor vendedor;

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getValorInicial() { return valorInicial; }
    public Vendedor getVendedor() { return vendedor; }

    public void setValorInicial(double valorInicial) {

        if (valorInicial <= 0) {
            throw new IllegalArgumentException("O valor inicial deve ser maior que zero.");
        }
        this.valorInicial = valorInicial;
    }
}