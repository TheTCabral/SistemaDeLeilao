package dao;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeilaoDAO {

    /**
     * CREATE - Cria um novo leilão
     */
    public void create(Leilao leilao) throws SQLException {
        String sql = "INSERT INTO leilao (item_id, data_inicio, data_fim, status, incremento_minimo, tipo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, leilao.getItem().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(leilao.getDataInicio()));
            stmt.setTimestamp(3, Timestamp.valueOf(leilao.getDataFim()));
            stmt.setString(4, leilao.getStatus());
            stmt.setDouble(5, leilao.getIncrementoMinimo());
            stmt.setString(6, leilao.getTipo());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    leilao.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * READ - Busca um leilão por ID
     */
    public Leilao read(int id) throws SQLException {
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial, i.imagem_url, " +
                "v.id AS vendedor_id, v.nome AS vendedor_nome, v.email AS vendedor_email, v.cpf AS vendedor_cpf, " +
                "vc.nome AS vencedor_nome " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "JOIN pessoa v ON i.vendedor_id = v.id " +
                "LEFT JOIN pessoa vc ON l.vencedor_id = vc.id " +
                "WHERE l.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeilao(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ ALL - Busca todos os leilões
     */
    public List<Leilao> readAll() throws SQLException {
        List<Leilao> leiloes = new ArrayList<>();
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial, i.imagem_url, " +
                "v.id AS vendedor_id, v.nome AS vendedor_nome, v.email AS vendedor_email, v.cpf AS vendedor_cpf, " +
                "vc.nome AS vencedor_nome " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "JOIN pessoa v ON i.vendedor_id = v.id " +
                "LEFT JOIN pessoa vc ON l.vencedor_id = vc.id " +
                "ORDER BY l.data_inicio DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                leiloes.add(mapResultSetToLeilao(rs));
            }
        }
        return leiloes;
    }

    /**
     * READ ALL ACTIVE - Busca leilões ativos
     */
    public List<Leilao> readAllActive() throws SQLException {
        List<Leilao> leiloes = new ArrayList<>();
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial, i.imagem_url, " +
                "v.id AS vendedor_id, v.nome AS vendedor_nome, v.email AS vendedor_email, v.cpf AS vendedor_cpf, " +
                "vc.nome AS vencedor_nome " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "JOIN pessoa v ON i.vendedor_id = v.id " +
                "LEFT JOIN pessoa vc ON l.vencedor_id = vc.id " +
                "WHERE l.status = 'ATIVO' AND l.data_fim > NOW() " +
                "ORDER BY l.data_fim ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Leilao leilao = mapResultSetToLeilao(rs);

                // Carregar o maior lance
                LanceDAO lanceDAO = new LanceDAO();
                Lance maiorLance = lanceDAO.findMaiorLance(leilao.getId());
                if (maiorLance != null) {
                    leilao.getLances().add(maiorLance);
                }

                leiloes.add(leilao);
            }
        }
        return leiloes;
    }

    /**
     * READ BY STATUS - Busca leilões por status
     */
    public List<Leilao> readByStatus(String status) throws SQLException {
        List<Leilao> leiloes = new ArrayList<>();
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial, i.imagem_url, " +
                "v.id AS vendedor_id, v.nome AS vendedor_nome, v.email AS vendedor_email, v.cpf AS vendedor_cpf, " +
                "vc.nome AS vencedor_nome " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "JOIN pessoa v ON i.vendedor_id = v.id " +
                "LEFT JOIN pessoa vc ON l.vencedor_id = vc.id " +
                "WHERE l.status = ? " +
                "ORDER BY l.data_inicio DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leiloes.add(mapResultSetToLeilao(rs));
                }
            }
        }
        return leiloes;
    }

    /**
     * READ BY VENDEDOR - Busca leilões de um vendedor
     */
    public List<Leilao> readByVendedor(int vendedorId) throws SQLException {
        List<Leilao> leiloes = new ArrayList<>();
        String sql = "SELECT l.*, i.nome, i.descricao, i.valor_inicial, i.imagem_url, " +
                "v.id AS vendedor_id, v.nome AS vendedor_nome, v.email AS vendedor_email, v.cpf AS vendedor_cpf, " +
                "vc.nome AS vencedor_nome " +
                "FROM leilao l " +
                "JOIN item i ON l.item_id = i.id " +
                "JOIN pessoa v ON i.vendedor_id = v.id " +
                "LEFT JOIN pessoa vc ON l.vencedor_id = vc.id " +
                "WHERE i.vendedor_id = ? " +
                "ORDER BY l.data_inicio DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendedorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leiloes.add(mapResultSetToLeilao(rs));
                }
            }
        }
        return leiloes;
    }

    /**
     * UPDATE - Atualiza dados do leilão
     */
    public void update(Leilao leilao) throws SQLException {
        String sql = "UPDATE leilao SET data_inicio = ?, data_fim = ?, status = ?, " +
                "incremento_minimo = ?, tipo = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(leilao.getDataInicio()));
            stmt.setTimestamp(2, Timestamp.valueOf(leilao.getDataFim()));
            stmt.setString(3, leilao.getStatus());
            stmt.setDouble(4, leilao.getIncrementoMinimo());
            stmt.setString(5, leilao.getTipo());
            stmt.setInt(6, leilao.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Leilão não encontrado.");
            }
        }
    }

    /**
     * UPDATE STATUS - Atualiza apenas o status
     */
    public void updateStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE leilao SET status = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Leilão não encontrado.");
            }
        }
    }

    /**
     * ENCERRAR LEILAO - Encerra um leilão e define o vencedor
     */
    public void encerrarLeilao(int id) throws SQLException {
        String sql = "UPDATE leilao SET status = 'ENCERRADO' WHERE id = ? AND status = 'ATIVO'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Leilão não encontrado ou não está ativo.");
            }

            // O trigger do BD define automaticamente o vencedor
        }
    }

    /**
     * DELETE - Remove um leilão (apenas se não tiver lances)
     */
    public void delete(int id) throws SQLException {
        // Verificar se há lances
        String checkSql = "SELECT COUNT(*) FROM lance WHERE leilao_id = ?";
        String deleteSql = "DELETE FROM leilao WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Verificar lances
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Leilão não pode ser excluído: possui lances registrados.");
                    }
                }
            }

            // Deletar leilão
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Leilão não encontrado.");
                }
            }
        }
    }

    /**
     * CANCELAR LEILAO - Cancela um leilão
     */
    public void cancelarLeilao(int id) throws SQLException {
        String sql = "UPDATE leilao SET status = 'CANCELADO' WHERE id = ? AND status IN ('PENDENTE', 'ATIVO')";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Leilão não encontrado ou já encerrado.");
            }
        }
    }

    /**
     * Mapeia ResultSet para objeto Leilao
     */
    private Leilao mapResultSetToLeilao(ResultSet rs) throws SQLException {
        // Criar Vendedor
        Vendedor vendedor = new Vendedor();
        vendedor.setId(rs.getInt("vendedor_id"));
        vendedor.setNome(rs.getString("vendedor_nome"));
        vendedor.setEmail(rs.getString("vendedor_email"));
        vendedor.setCpf(rs.getString("vendedor_cpf"));

        // Criar Item
        Item item = new Item();
        item.setId(rs.getInt("item_id"));
        item.setNome(rs.getString("nome"));
        item.setDescricao(rs.getString("descricao"));
        item.setValorInicial(rs.getDouble("valor_inicial"));
        item.setImagemUrl(rs.getString("imagem_url"));
        item.setVendedor(vendedor);

        // Criar Leilão
        Leilao leilao = new Leilao(item);
        leilao.setId(rs.getInt("id"));
        leilao.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());
        leilao.setDataFim(rs.getTimestamp("data_fim").toLocalDateTime());
        leilao.setStatus(rs.getString("status"));
        leilao.setIncrementoMinimo(rs.getDouble("incremento_minimo"));
        leilao.setTipo(rs.getString("tipo"));

        // Vencedor (se houver)
        int vencedorId = rs.getInt("vencedor_id");
        if (vencedorId > 0) {
            Comprador vencedor = new Comprador();
            vencedor.setId(vencedorId);
            vencedor.setNome(rs.getString("vencedor_nome"));
            leilao.setVencedor(vencedor);
        }

        return leilao;
    }
}