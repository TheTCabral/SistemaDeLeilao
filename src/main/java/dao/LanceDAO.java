package dao;

import model.Lance;
import model.Comprador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class LanceDAO {

    /**
     * Cria um novo lance no banco de dados[cite: 163].
     * Tabela 'lance' [cite: 120]
     */
    public void create(Lance lance, int leilaoId) throws SQLException {
        String sql = "INSERT INTO lance (comprador_id, leilao_id, valor, data_lance) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, lance.getComprador().getId());
            stmt.setInt(2, leilaoId);
            stmt.setDouble(3, lance.getValor());
            stmt.setTimestamp(4, Timestamp.valueOf(lance.getDataHora()));

            stmt.executeUpdate();

            // Opcional: recuperar o ID gerado
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lance.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Busca o maior lance válido para um leilão específico.
     * (Usado pelo LeilaoDAO e pela Tela de Lance).
     */
    public Lance findMaiorLance(int leilaoId) throws SQLException {
        String sql = "SELECT l.*, p.nome " +
                "FROM lance l " +
                "JOIN pessoa p ON l.comprador_id = p.id " +
                "WHERE l.leilao_id = ? " +
                "ORDER BY l.valor DESC " +
                "LIMIT 1"; // Queremos apenas o maior [cite: 22]

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, leilaoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Comprador comprador = new Comprador();
                    comprador.setId(rs.getInt("comprador_id"));
                    comprador.setNome(rs.getString("nome"));

                    Lance lance = new Lance(comprador, rs.getDouble("valor"));
                    lance.setId(rs.getInt("id"));
                    lance.setDataHora(rs.getTimestamp("data_lance").toLocalDateTime());

                    return lance;
                }
            }
        }
        return null; // Nenhum lance encontrado
    }

    // ... Métodos read(), update(), delete() de Lance
}