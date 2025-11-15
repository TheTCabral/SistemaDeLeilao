package model;

import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Leilao {

    private int id;
    private Item item;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status; // (ATIVO, ENCERRADO, PENDENTE)
    private List<Lance> lances;

    public Leilao(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Leilão deve ter um item.");
        }
        this.item = item;
        this.lances = new ArrayList<>();
        this.status = "PENDENTE";
    }

    public int getId() { return id; }
    public Item getItem() { return item; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public String getStatus() { return status; }
    public List<Lance> getLances() { return lances; }

    public void setDataFim(LocalDateTime dataFim) {
        if (dataFim == null || (dataInicio != null && dataFim.isBefore(dataInicio))) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início.");
        }
        this.dataFim = dataFim;
    }

    /**
     * Regra de negócio principal: Adicionar um lance.
     * Esta é a regra de [cite: 75] ("deve ser maior que o lance atual")
     * e [cite: 14] ("lance deve ser superior ao valor atual mais alto").
     */
    public void proporLance(Lance novoLance) {
        if (!"ATIVO".equals(this.status)) {
            throw new IllegalStateException("Leilão não está ativo. Lances não são aceitos.");
        }

        double maiorLance = getMaiorLanceValor();

        if (novoLance.getValor() <= maiorLance) {
            throw new IllegalArgumentException("O valor do lance deve ser maior que o lance atual (R$ " + maiorLance + ").");
        }

        // Regra [cite: 17] (Não pode cancelar, apenas adicionar)
        this.lances.add(novoLance);
    }

    /**
     * Retorna o valor do maior lance atual.
     * Se não houver lances, retorna o valor inicial do item. [cite: 11]
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
}