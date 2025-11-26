package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor

public class Relatorio {
    private int id;
    private String tipo;
    private String descricao;
    private LocalDateTime dataGeracao;
    private int geradoPor;
    private String dadosJson;

    // Construtor padrão
    public Relatorio() {
        this.dataGeracao = LocalDateTime.now();
    }

    // Construtor com parâmetros
    public Relatorio(String tipo, String descricao, int geradoPor) {
        this();
        setTipo(tipo);
        setDescricao(descricao);
        setGeradoPor(geradoPor);
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de relatório não pode ser vazio.");
        }
        this.tipo = tipo.trim();
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }

    public void setGeradoPor(int geradoPor) {
        if (geradoPor <= 0) {
            throw new IllegalArgumentException("ID do gerador deve ser válido.");
        }
        this.geradoPor = geradoPor;
    }
public void GerarRelatorio() {
        // Lógica para gerar o relatório
        this.dataGeracao = LocalDateTime.now();
        // Possivelmente popular dadosJson com informações relevantes
    }

    @Override
    public String toString() {
        return String.format("Relatório #%d - %s [%s]", id, tipo, dataGeracao);
    }
}