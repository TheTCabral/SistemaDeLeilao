package dao;

import model.Item;
import model.Lance;
import model.Leilao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeilaoDAO {

    /**
     * Lê todos os leilões ATIVOS para exibir na tela principal.
     * (Usamos um JOIN para buscar dados do Item também).
     */
    public List<Leilao> readAllActive() throws SQLException {
        List<Leilao> leiloes = new ArrayList<>();

        // SQL une Leilao [cite: 119] e Item [cite: 118]
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "WHERE l.status = 'ATIVO' AND l.data_fim > NOW()"; // [cite: 20]

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // 1. Criar o Item
                Item item = new Item();
                item.setId(rs.getInt("item_id"));
                item.setNome(rs.getString("nome"));
                item.setDescricao(rs.getString("descricao"));
                item.setValorInicial(rs.getDouble("valor_inicial"));
                // (Vendedor não carregado aqui para simplificar a listagem)

                // 2. Criar o Leilão
                Leilao leilao = new Leilao(item); // Composição [cite: 167]
                leilao.setId(rs.getInt("id"));
                leilao.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());
                leilao.setDataFim(rs.getTimestamp("data_fim").toLocalDateTime());
                leilao.setStatus(rs.getString("status"));

                // 3. (Opcional) Carregar o maior lance atual
                // Para performance, o maior lance poderia ser uma coluna em 'leilao'
                // Aqui, vamos buscar separado (pode ser lento com muitos leilões)
                LanceDAO lanceDAO = new LanceDAO();
                Lance maiorLance = lanceDAO.findMaiorLance(leilao.getId());
                if (maiorLance != null) {
                    leilao.proporLance(maiorLance); // Adiciona ao histórico interno
                }

                leiloes.add(leilao);
            }
        }
        return leiloes;
    }

    // ... Aqui entrariam os métodos create(), update() e delete() de Leilao
}