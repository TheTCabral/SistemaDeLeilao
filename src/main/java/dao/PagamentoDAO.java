package dao;

import model.Comprador;
import model.Lance;
import model.Pagamento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    /**
     * CREATE - Registra um novo pagamento
     */
    public void create(Pagamento pagamento) throws SQLException {
        String sql = "INSERT INTO pagamento (lance_id, comprador_id, valor, metodo, status, codigo_transacao) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pagamento.getLance().getId());
            stmt.setInt(2, pagamento.getComprador().getId());
            stmt.setDouble(3, pagamento.getValor());
            stmt.setString(4, pagamento.getMetodo());
            stmt.setString(5, pagamento.getStatus());
            stmt.setString(6, pagamento.getCodigoTransacao());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * READ - Busca um pagamento por ID
     */
    public Pagamento read(int id) throws SQLException {
        String sql = "SELECT p.*, l.valor AS lance_valor, l.data_lance, " +
                "c.nome AS comprador_nome, c.email AS comprador_email, c.cpf AS comprador_cpf " +
                "FROM pagamento p " +
                "JOIN lance l ON p.lance_id = l.id " +
                "JOIN pessoa c ON p.comprador_id = c.id " +
                "WHERE p.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPagamento(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ BY LANCE - Busca pagamento de um lance específico
     */
    public Pagamento readByLance(int lanceId) throws SQLException {
        String sql = "SELECT p.*, l.valor AS lance_valor, l.data_lance, " +
                "c.nome AS comprador_nome, c.email AS comprador_email, c.cpf AS comprador_cpf " +
                "FROM pagamento p " +
                "JOIN lance l ON p.lance_id = l.id " +
                "JOIN pessoa c ON p.comprador_id = c.id " +
                "WHERE p.lance_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lanceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPagamento(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ BY COMPRADOR - Busca pagamentos de um comprador
     */
    public List<Pagamento> readByComprador(int compradorId) throws SQLException {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT p.*, l.valor AS lance_valor, l.data_lance, " +
                "c.nome AS comprador_nome, c.email AS comprador_email, c.cpf AS comprador_cpf " +
                "FROM pagamento p " +
                "JOIN lance l ON p.lance_id = l.id " +
                "JOIN pessoa c ON p.comprador_id = c.id " +
                "WHERE p.comprador_id = ? " +
                "ORDER BY p.data_pagamento DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compradorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagamentos.add(mapResultSetToPagamento(rs));
                }
            }
        }
        return pagamentos;
    }

    /**
     * READ BY STATUS - Busca pagamentos por status
     */
    public List<Pagamento> readByStatus(String status) throws SQLException {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT p.*, l.valor AS lance_valor, l.data_lance, " +
                "c.nome AS comprador_nome, c.email AS comprador_email, c.cpf AS comprador_cpf " +
                "FROM pagamento p " +
                "JOIN lance l ON p.lance_id = l.id " +
                "JOIN pessoa c ON p.comprador_id = c.id " +
                "WHERE p.status = ? " +
                "ORDER BY p.data_pagamento DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagamentos.add(mapResultSetToPagamento(rs));
                }
            }
        }
        return pagamentos;
    }

    /**
     * READ ALL - Busca todos os pagamentos
     */
    public List<Pagamento> readAll() throws SQLException {
        List<Pagamento> pagamentos = new ArrayList<>();
        String sql = "SELECT p.*, l.valor AS lance_valor, l.data_lance, " +
                "c.nome AS comprador_nome, c.email AS comprador_email, c.cpf AS comprador_cpf " +
                "FROM pagamento p " +
                "JOIN lance l ON p.lance_id = l.id " +
                "JOIN pessoa c ON p.comprador_id = c.id " +
                "ORDER BY p.data_pagamento DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pagamentos.add(mapResultSetToPagamento(rs));
            }
        }
        return pagamentos;
    }

    /**
     * UPDATE - Atualiza dados do pagamento
     */
    public void update(Pagamento pagamento) throws SQLException {
        String sql = "UPDATE pagamento SET metodo = ?, status = ?, codigo_transacao = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pagamento.getMetodo());
            stmt.setString(2, pagamento.getStatus());
            stmt.setString(3, pagamento.getCodigoTransacao());
            stmt.setInt(4, pagamento.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pagamento não encontrado.");
            }
        }
    }

    /**
     * UPDATE STATUS - Atualiza status do pagamento
     */
    public void updateStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE pagamento SET status = ?, data_confirmacao = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus);

            if ("APROVADO".equals(novoStatus)) {
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }

            stmt.setInt(3, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pagamento não encontrado.");
            }
        }
    }

    /**
     * CONFIRMAR PAGAMENTO - Confirma um pagamento
     */
    public void confirmarPagamento(int id, String codigoTransacao) throws SQLException {
        String sql = "UPDATE pagamento SET status = 'APROVADO', data_confirmacao = ?, codigo_transacao = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, codigoTransacao);
            stmt.setInt(3, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pagamento não encontrado.");
            }
        }
    }

    /**
     * DELETE - Remove um pagamento (apenas se pendente)
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pagamento WHERE id = ? AND status = 'PENDENTE'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pagamento não encontrado ou não pode ser excluído.");
            }
        }
    }

    /**
     * Mapeia ResultSet para objeto Pagamento
     */
    private Pagamento mapResultSetToPagamento(ResultSet rs) throws SQLException {
        Comprador comprador = new Comprador();
        comprador.setId(rs.getInt("comprador_id"));
        comprador.setNome(rs.getString("comprador_nome"));
        comprador.setEmail(rs.getString("comprador_email"));
        comprador.setCpf(rs.getString("comprador_cpf"));

        Lance lance = new Lance(comprador, rs.getDouble("lance_valor"));
        lance.setId(rs.getInt("lance_id"));
        lance.setDataHora(rs.getTimestamp("data_lance").toLocalDateTime());

        Pagamento pagamento = new Pagamento();
        pagamento.setId(rs.getInt("id"));
        pagamento.setLance(lance);
        pagamento.setComprador(comprador);
        pagamento.setValor(rs.getDouble("valor"));
        pagamento.setMetodo(rs.getString("metodo"));
        pagamento.setStatus(rs.getString("status"));
        pagamento.setCodigoTransacao(rs.getString("codigo_transacao"));

        Timestamp dataPagamento = rs.getTimestamp("data_pagamento");
        if (dataPagamento != null) {
            pagamento.setDataPagamento(dataPagamento.toLocalDateTime());
        }

        Timestamp dataConfirmacao = rs.getTimestamp("data_confirmacao");
        if (dataConfirmacao != null) {
            pagamento.setDataConfirmacao(dataConfirmacao.toLocalDateTime());
        }

        return pagamento;
    }
}