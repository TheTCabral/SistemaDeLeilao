package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor

public class Pagamento {
    private int id;
    private Lance lance;
    private Comprador comprador;
    private double valor;
    private String metodo; // PIX, CARTAO, BOLETO
    private String status; // PENDENTE, PROCESSANDO, APROVADO, RECUSADO
    private LocalDateTime dataPagamento;
    private LocalDateTime dataConfirmacao;
    private String codigoTransacao;

    // Construtor padrão
    public Pagamento() {
        this.status = "PENDENTE";
        this.dataPagamento = LocalDateTime.now();
    }

    // Construtor com parâmetros
    public Pagamento(Lance lance, String metodo) {
        this();
        setLance(lance);
        setComprador(lance.getComprador());
        setValor(lance.getValor());
        setMetodo(metodo);
    }

    // Setters com validações
    public void setLance(Lance lance) {
        if (lance == null) {
            throw new IllegalArgumentException("Pagamento deve estar associado a um lance.");
        }
        this.lance = lance;
    }

    public void setComprador(Comprador comprador) {
        if (comprador == null) {
            throw new IllegalArgumentException("Pagamento deve ter um comprador.");
        }
        this.comprador = comprador;
    }

    public void setValor(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser positivo.");
        }
        this.valor = valor;
    }

    public void setMetodo(String metodo) {
        if (metodo == null || !metodo.matches("PIX|CARTAO|BOLETO")) {
            throw new IllegalArgumentException("Método de pagamento inválido.");
        }
        this.metodo = metodo;
    }

    public void setStatus(String status) {
        if (status == null || !status.matches("PENDENTE|PROCESSANDO|APROVADO|RECUSADO")) {
            throw new IllegalArgumentException("Status de pagamento inválido.");
        }
        this.status = status;
    }

    /**
     * Processa o pagamento
     */
    public void processar() {
        if (!"PENDENTE".equals(status)) {
            throw new IllegalStateException("Apenas pagamentos pendentes podem ser processados.");
        }
        this.status = "PROCESSANDO";
    }

    /**
     * Aprova o pagamento
     */
    public void aprovar(String codigoTransacao) {
        if (!"PROCESSANDO".equals(status) && !"PENDENTE".equals(status)) {
            throw new IllegalStateException("Pagamento não pode ser aprovado neste status.");
        }
        this.status = "APROVADO";
        this.codigoTransacao = codigoTransacao;
        this.dataConfirmacao = LocalDateTime.now();
    }

    /**
     * Recusa o pagamento
     */
    public void recusar() {
        if ("APROVADO".equals(status)) {
            throw new IllegalStateException("Pagamento já aprovado não pode ser recusado.");
        }
        this.status = "RECUSADO";
    }

    @Override
    public String toString() {
        return String.format("Pagamento #%d - R$ %.2f [%s via %s]", id, valor, status, metodo);
    }
}
