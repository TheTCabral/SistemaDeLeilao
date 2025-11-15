package model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Leilao {
    private int id;
    private Item item;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status; // PENDENTE, ATIVO, ENCERRADO, CANCELADO
    private double incrementoMinimo;
    private String tipo; // ABERTO, OCULTO
    private List<Lance> lances;
    private Comprador vencedor;

    // Construtor
    public Leilao(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Leilão deve ter um item.");
        }
        this.item = item;
        this.lances = new ArrayList<>();
        this.status = "PENDENTE";
        this.incrementoMinimo = 10.0;
        this.tipo = "ABERTO";
    }

    // Setters com validações
    public void setDataInicio(LocalDateTime dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula.");
        }
        this.dataInicio = dataInicio;
    }

    public void setDataFim(LocalDateTime dataFim) {
        if (dataFim == null) {
            throw new IllegalArgumentException("Data de fim não pode ser nula.");
        }
        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início.");
        }
        this.dataFim = dataFim;
    }

    public void setStatus(String status) {
        if (status == null || !status.matches("PENDENTE|ATIVO|ENCERRADO|CANCELADO")) {
            throw new IllegalArgumentException("Status inválido.");
        }
        this.status = status;
    }

    public void setIncrementoMinimo(double incrementoMinimo) {
        if (incrementoMinimo <= 0) {
            throw new IllegalArgumentException("Incremento mínimo deve ser maior que zero.");
        }
        this.incrementoMinimo = incrementoMinimo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || !tipo.matches("ABERTO|OCULTO")) {
            throw new IllegalArgumentException("Tipo de leilão inválido.");
        }
        this.tipo = tipo;
    }

    /**
     * Propõe um lance no leilão
     * Aplica as regras de negócio principais
     */
    public void proporLance(Lance novoLance) {
        // Regra: Leilão deve estar ativo
        if (!"ATIVO".equals(this.status)) {
            throw new IllegalStateException("Leilão não está ativo. Lances não são aceitos.");
        }

        // Regra: Lance deve ser maior que o lance atual
        double maiorLance = getMaiorLanceValor();
        if (novoLance.getValor() <= maiorLance) {
            throw new IllegalArgumentException(
                    String.format("O valor do lance deve ser maior que R$ %.2f", maiorLance)
            );
        }

        // Regra: Lance deve respeitar o incremento mínimo
        if (novoLance.getValor() < (maiorLance + incrementoMinimo)) {
            throw new IllegalArgumentException(
                    String.format("Lance deve ser no mínimo R$ %.2f (incremento de R$ %.2f)",
                            maiorLance + incrementoMinimo, incrementoMinimo)
            );
        }

        // Adiciona o lance à lista
        this.lances.add(novoLance);
    }

    /**
     * Retorna o valor do maior lance atual
     */
    public double getMaiorLanceValor() {
        if (lances.isEmpty()) {
            return this.item.getValorInicial();
        }
        return lances.stream()
                .mapToDouble(Lance::getValor)
                .max()
                .orElse(this.item.getValorInicial());
    }

    /**
     * Retorna o maior lance
     */
    public Lance getMaiorLance() {
        if (lances.isEmpty()) {
            return null;
        }
        return lances.stream()
                .max((l1, l2) -> Double.compare(l1.getValor(), l2.getValor()))
                .orElse(null);
    }

    /**
     * Verifica se o leilão está ativo
     */
    public boolean isAtivo() {
        return "ATIVO".equals(status) &&
                LocalDateTime.now().isBefore(dataFim) &&
                LocalDateTime.now().isAfter(dataInicio);
    }

    /**
     * Encerra o leilão e define o vencedor
     */
    public void encerrar() {
        if (!"ATIVO".equals(status)) {
            throw new IllegalStateException("Apenas leilões ativos podem ser encerrados.");
        }

        Lance maiorLance = getMaiorLance();
        if (maiorLance != null) {
            this.vencedor = maiorLance.getComprador();
        }

        this.status = "ENCERRADO";
    }

    @Override
    public String toString() {
        return String.format("Leilão #%d - %s [%s]", id, item.getNome(), status);
    }
}