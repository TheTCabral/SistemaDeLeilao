package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private int id;
    private String nome;
    private String descricao;
    private double valorInicial;
    private String imagemUrl;
    private Vendedor vendedor;


    // Construtor com parâmetros principais
    public Item(String nome, String descricao, double valorInicial, Vendedor vendedor) {
        setNome(nome);
        setDescricao(descricao);
        setValorInicial(valorInicial);
        setVendedor(vendedor);
    }

    // Setters com validações (Regras de Negócio)
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do item não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição do item não pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }

    public void setValorInicial(double valorInicial) {
        if (valorInicial <= 0) {
            throw new IllegalArgumentException("O valor inicial deve ser maior que zero.");
        }
        this.valorInicial = valorInicial;
    }

    public void setVendedor(Vendedor vendedor) {
        if (vendedor == null) {
            throw new IllegalArgumentException("Item deve ter um vendedor.");
        }
        this.vendedor = vendedor;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    @Override
    public String toString() {
        return String.format("%s - R$ %.2f", nome, valorInicial);
    }
}