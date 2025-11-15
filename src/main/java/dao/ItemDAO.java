package dao;

import model.Item;
import model.Vendedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    /**
     * CREATE - Cadastra um novo item no banco de dados
     * Regra: Apenas vendedores podem cadastrar itens
     */
    public void create(Item item) throws SQLException {
        String sql = "INSERT INTO item (nome, descricao, valor_inicial, imagem_url, vendedor_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getDescricao());
            stmt.setDouble(3, item.getValorInicial());
            stmt.setString(4, item.getImagemUrl());
            stmt.setInt(5, item.getVendedor().getId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * READ - Busca um item por ID
     */
    public Item read(int id) throws SQLException {
        String sql = "SELECT i.*, p.nome AS vendedor_nome, p.email AS vendedor_email, p.cpf AS vendedor_cpf " +
                "FROM item i " +
                "JOIN pessoa p ON i.vendedor_id = p.id " +
                "WHERE i.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ ALL - Busca todos os itens
     */
    public List<Item> readAll() throws SQLException {
        List<Item> itens = new ArrayList<>();
        String sql = "SELECT i.*, p.nome AS vendedor_nome, p.email AS vendedor_email, p.cpf AS vendedor_cpf " +
                "FROM item i " +
                "JOIN pessoa p ON i.vendedor_id = p.id " +
                "ORDER BY i.data_cadastro DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                itens.add(mapResultSetToItem(rs));
            }
        }
        return itens;
    }

    /**
     * READ BY VENDEDOR - Busca itens de um vendedor específico
     */
    public List<Item> readByVendedor(int vendedorId) throws SQLException {
        List<Item> itens = new ArrayList<>();
        String sql = "SELECT i.*, p.nome AS vendedor_nome, p.email AS vendedor_email, p.cpf AS vendedor_cpf " +
                "FROM item i " +
                "JOIN pessoa p ON i.vendedor_id = p.id " +
                "WHERE i.vendedor_id = ? " +
                "ORDER BY i.data_cadastro DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendedorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapResultSetToItem(rs));
                }
            }
        }
        return itens;
    }

    /**
     * READ DISPONIVEIS - Busca itens sem leilão ativo
     */
    public List<Item> readDisponiveis() throws SQLException {
        List<Item> itens = new ArrayList<>();
        String sql = "SELECT i.*, p.nome AS vendedor_nome, p.email AS vendedor_email, p.cpf AS vendedor_cpf " +
                "FROM item i " +
                "JOIN pessoa p ON i.vendedor_id = p.id " +
                "LEFT JOIN leilao l ON i.id = l.item_id " +
                "WHERE l.id IS NULL OR l.status IN ('ENCERRADO', 'CANCELADO') " +
                "ORDER BY i.data_cadastro DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                itens.add(mapResultSetToItem(rs));
            }
        }
        return itens;
    }

    /**
     * UPDATE - Atualiza um item
     * Regra: Item não pode ser editado após início do leilão (trigger do BD garante isso)
     */
    public void update(Item item) throws SQLException {
        String sql = "UPDATE item SET nome = ?, descricao = ?, valor_inicial = ?, imagem_url = ? " +
                "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getDescricao());
            stmt.setDouble(3, item.getValorInicial());
            stmt.setString(4, item.getImagemUrl());
            stmt.setInt(5, item.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Item não encontrado ou não pode ser atualizado.");
            }
        }
    }

    /**
     * DELETE - Remove um item
     * Apenas se não tiver leilão associado
     */
    public void delete(int id) throws SQLException {
        // Verificar se há leilão associado
        String checkSql = "SELECT COUNT(*) FROM leilao WHERE item_id = ?";
        String deleteSql = "DELETE FROM item WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Verificar leilões
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Item não pode ser excluído: possui leilão associado.");
                    }
                }
            }

            // Deletar item
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Item não encontrado.");
                }
            }
        }
    }

    /**
     * Mapeia ResultSet para objeto Item
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Vendedor vendedor = new Vendedor();
        vendedor.setId(rs.getInt("vendedor_id"));
        vendedor.setNome(rs.getString("vendedor_nome"));
        vendedor.setEmail(rs.getString("vendedor_email"));
        vendedor.setCpf(rs.getString("vendedor_cpf"));

        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setNome(rs.getString("nome"));
        item.setDescricao(rs.getString("descricao"));
        item.setValorInicial(rs.getDouble("valor_inicial"));
        item.setImagemUrl(rs.getString("imagem_url"));
        item.setVendedor(vendedor);

        return item;
    }
}