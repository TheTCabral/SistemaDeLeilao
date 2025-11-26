package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Leilao {
    private int id;
    private Item item;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status; // PENDENTE, ATIVO, ENCERRADO, CANCELADO
    private double incrementoMinimo;
    private String tipo; // ABERTO, OCULTO (Regra 29)
    private List<Lance> lances;
    private Comprador vencedor;


    public Leilao(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Leilão deve ter um item válido.");
        }
        this.item = item;
        this.lances = new ArrayList<>();
        this.status = "PENDENTE";
        this.incrementoMinimo = 10.0;
        this.tipo = "ABERTO";
    }


    public void setDataInicio(LocalDateTime dataInicio) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula.");
        }
        // Não permitir data no passado para novos leilões
        if (this.id == 0 && dataInicio.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalArgumentException("Data de início não pode ser no passado.");
        }
        this.dataInicio = dataInicio;
    }

    /**
     * Define data de fim do leilão
     * Regra 15: Cada leilão tem data e hora de fim definidas
     */
    public void setDataFim(LocalDateTime dataFim) {
        if (dataFim == null) {
            throw new IllegalArgumentException("Data de fim não pode ser nula.");
        }
        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início.");
        }
        // Garantir duração mínima de 1 hora
        if (dataInicio != null && dataFim.isBefore(dataInicio.plusHours(1))) {
            throw new IllegalArgumentException("Leilão deve ter duração mínima de 1 hora.");
        }
        this.dataFim = dataFim;
    }

    /**
     * Define status do leilão
     */
    public void setStatus(String status) {
        if (status == null || !status.matches("PENDENTE|ATIVO|ENCERRADO|CANCELADO")) {
            throw new IllegalArgumentException("Status inválido.");
        }
        this.status = status;
    }

    /**
     * Define incremento mínimo
     * Regra 10: Lance deve respeitar incremento mínimo
     */
    public void setIncrementoMinimo(double incrementoMinimo) {
        if (incrementoMinimo <= 0) {
            throw new IllegalArgumentException("Incremento mínimo deve ser maior que zero.");
        }
        if (incrementoMinimo > item.getValorInicial()) {
            throw new IllegalArgumentException("Incremento mínimo não pode ser maior que o valor inicial do item.");
        }
        this.incrementoMinimo = incrementoMinimo;
    }

    /**
     * Define tipo de leilão
     * Regra 29: Sistema pode permitir leilões abertos ou ocultos
     */
    public void setTipo(String tipo) {
        if (tipo == null || !tipo.matches("ABERTO|OCULTO")) {
            throw new IllegalArgumentException("Tipo de leilão inválido. Use ABERTO ou OCULTO.");
        }
        this.tipo = tipo;
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    /**
     * Propõe um lance no leilão
     * Regras: 9-14 (Validações de lance)
     */
    public void proporLance(Lance novoLance) {
        if (novoLance == null) {
            throw new IllegalArgumentException("Lance não pode ser nulo.");
        }

        // Regra 16: Leilão deve estar ativo
        if (!"ATIVO".equals(this.status)) {
            throw new IllegalStateException("Leilão não está ativo. Lances não são aceitos.");
        }

        // Regra 16: Verificar se não passou do horário
        if (LocalDateTime.now().isAfter(this.dataFim)) {
            throw new IllegalStateException("Leilão já encerrou. Nenhum lance pode ser aceito.");
        }

        // Regra 10: Lance deve ser superior ao valor atual mais alto
        double maiorLanceAtual = getMaiorLanceValor();
        if (novoLance.getValor() <= maiorLanceAtual) {
            throw new IllegalArgumentException(
                    String.format("O lance deve ser superior ao valor atual de R$ %.2f", maiorLanceAtual)
            );
        }

        // Regra 10: Lance deve respeitar o incremento mínimo
        double valorMinimoAceito = maiorLanceAtual + incrementoMinimo;
        if (novoLance.getValor() < valorMinimoAceito) {
            throw new IllegalArgumentException(
                    String.format("Lance deve ser no mínimo R$ %.2f (incremento de R$ %.2f)",
                            valorMinimoAceito, incrementoMinimo)
            );
        }

        // Regra 12: Permitir que usuário aumente seu próprio lance
        Lance lanceAnteriorComprador = getLanceAnteriorComprador(novoLance.getComprador());
        if (lanceAnteriorComprador != null) {
            if (novoLance.getValor() <= lanceAnteriorComprador.getValor()) {
                throw new IllegalArgumentException(
                        "Você já possui um lance de R$ " +
                                String.format("%.2f", lanceAnteriorComprador.getValor()) +
                                ". O novo lance deve ser maior."
                );
            }
        }

        // Adicionar lance à lista
        this.lances.add(novoLance);
    }

    /**
     * Retorna o valor do maior lance atual ou valor inicial
     * Regra 8: Valor inicial de lance obrigatório
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
     * Retorna o maior lance ou null se não houver lances
     * Regra 14: Em caso de empate, considera o primeiro registrado
     */
    public Lance getMaiorLance() {
        if (lances.isEmpty()) {
            return null;
        }
        // Ordenar por valor DESC e data ASC (primeiro lance em caso de empate)
        return lances.stream()
                .max(Comparator.comparing(Lance::getValor)
                        .thenComparing(Lance::getDataHora, Comparator.reverseOrder()))
                .orElse(null);
    }

    /**
     * Retorna o lance anterior de um comprador específico
     * Regra 12: Usuário pode aumentar seu próprio lance
     */
    public Lance getLanceAnteriorComprador(Comprador comprador) {
        if (comprador == null) {
            return null;
        }
        return lances.stream()
                .filter(l -> l.getComprador().getId() == comprador.getId())
                .max(Comparator.comparing(Lance::getDataHora))
                .orElse(null);
    }

    /**
     * Verifica se o leilão está ativo
     * Regra 15: Leilão com início e fim definidos
     * Regra 16: Nenhum lance aceito após encerramento
     */
    public boolean isAtivo() {
        return "ATIVO".equals(status) &&
                LocalDateTime.now().isBefore(dataFim) &&
                LocalDateTime.now().isAfter(dataInicio);
    }

    /**
     * Verifica se o leilão já começou
     */
    public boolean isIniciado() {
        return LocalDateTime.now().isAfter(dataInicio);
    }

    /**
     * Verifica se o leilão já encerrou
     * Regra 30: Encerramento automático quando tempo acabar
     */
    public boolean isVencido() {
        return LocalDateTime.now().isAfter(dataFim);
    }

    /**
     * Encerra o leilão e define o vencedor
     * Regras 17-18: Sistema identifica vencedor e notifica
     */
    public void encerrar() {
        if (!"ATIVO".equals(status)) {
            throw new IllegalStateException("Apenas leilões ativos podem ser encerrados.");
        }

        // Regra 17: Identificar vencedor (maior lance válido)
        Lance maiorLance = getMaiorLance();
        if (maiorLance != null) {
            this.vencedor = maiorLance.getComprador();
        }

        this.status = "ENCERRADO";

        // TODO: Regra 18 - Implementar notificação
        // notificarVencedor();
        // notificarVendedor();
    }

    /**
     * Cancela o leilão
     */
    public void cancelar() {
        if ("ENCERRADO".equals(status)) {
            throw new IllegalStateException("Leilão já encerrado não pode ser cancelado.");
        }
        this.status = "CANCELADO";
    }

    /**
     * Ativa o leilão se o horário de início chegou
     */
    public void ativar() {
        if (!"PENDENTE".equals(status)) {
            throw new IllegalStateException("Apenas leilões pendentes podem ser ativados.");
        }
        if (LocalDateTime.now().isBefore(dataInicio)) {
            throw new IllegalStateException("Leilão só pode ser ativado após data de início.");
        }
        this.status = "ATIVO";
    }

    /**
     * Retorna contagem de lances
     * Regra 11: Sistema registra todos os lances
     */
    public int getTotalLances() {
        return lances.size();
    }

    /**
     * Retorna lista de lances ordenada por valor
     * Regra 22: Sistema mantém histórico completo
     */
    public List<Lance> getLancesOrdenados() {
        List<Lance> lancesOrdenados = new ArrayList<>(lances);
        lancesOrdenados.sort(Comparator.comparing(Lance::getValor).reversed()
                .thenComparing(Lance::getDataHora));
        return lancesOrdenados;
    }

    /**
     * Verifica se o leilão pode ser editado
     * Regra 7: Item não pode ser editado após início do leilão
     */
    public boolean podeSerEditado() {
        return "PENDENTE".equals(status) && !isIniciado();
    }

    /**
     * Verifica se um comprador já participou do leilão
     */
    public boolean compradorParticipou(Comprador comprador) {
        if (comprador == null) {
            return false;
        }
        return lances.stream()
                .anyMatch(l -> l.getComprador().getId() == comprador.getId());
    }

    /**
     * Retorna tempo restante em minutos
     */
    public long getTempoRestanteMinutos() {
        if (dataFim == null) {
            return 0;
        }
        LocalDateTime agora = LocalDateTime.now();
        if (agora.isAfter(dataFim)) {
            return 0;
        }
        return java.time.Duration.between(agora, dataFim).toMinutes();
    }

    /**
     * Verifica se o leilão está próximo do fim (menos de 1 hora)
     */
    public boolean isProximoDoFim() {
        return getTempoRestanteMinutos() > 0 && getTempoRestanteMinutos() <= 60;
    }

    @Override
    public String toString() {
        return String.format("Leilão #%d - %s [%s] - Lance atual: R$ %.2f - Lances: %d",
                id, item.getNome(), status, getMaiorLanceValor(), getTotalLances());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Leilao leilao = (Leilao) o;
        return id == leilao.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}