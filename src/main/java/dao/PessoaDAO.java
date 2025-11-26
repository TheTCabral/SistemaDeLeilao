package dao;

import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar operações de Pessoa (Comprador, Vendedor, Administrador)
 * Implementa as regras de negócio 1-4, 25-28
 */
public class PessoaDAO {

    /**
     * CREATE - Cadastra uma nova pessoa no sistema
     * Regras: 1-4 (Cadastro obrigatório, dados válidos e únicos, papéis diferentes)
     */
    public void create(Pessoa pessoa, String tipo) throws SQLException {
        // Validar tipo
        if (!tipo.matches("COMPRADOR|VENDEDOR|ADMINISTRADOR")) {
            throw new IllegalArgumentException("Tipo de usuário inválido.");
        }

        // Verificar duplicidade de email e CPF
        if (emailExists(pessoa.getEmail())) {
            throw new SQLException("Este email já está cadastrado.");
        }
        if (cpfExists(pessoa.getCpf())) {
            throw new SQLException("Este CPF já está cadastrado.");
        }

        String sql = "INSERT INTO pessoa (nome, email, cpf, senha, tipo, data_cadastro) VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getEmail());
            stmt.setString(3, pessoa.getCpf());
            // Regra 27: Hash da senha com BCrypt
            stmt.setString(4, BCrypt.hashpw(pessoa.getSenha(), BCrypt.gensalt(12)));
            stmt.setString(5, tipo);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pessoa.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * READ - Busca pessoa por ID
     */
    public Pessoa read(int id) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPessoa(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ BY EMAIL - Busca pessoa por email
     */
    public Pessoa readByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPessoa(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ ALL - Busca todas as pessoas
     */
    public List<Pessoa> readAll() throws SQLException {
        List<Pessoa> pessoas = new ArrayList<>();
        String sql = "SELECT * FROM pessoa ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pessoas.add(mapResultSetToPessoa(rs));
            }
        }
        return pessoas;
    }

    /**
     * READ BY TIPO - Busca pessoas por tipo
     */
    public List<Pessoa> readByTipo(String tipo) throws SQLException {
        List<Pessoa> pessoas = new ArrayList<>();
        String sql = "SELECT * FROM pessoa WHERE tipo = ? ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pessoas.add(mapResultSetToPessoa(rs));
                }
            }
        }
        return pessoas;
    }

    /**
     * UPDATE - Atualiza dados da pessoa
     */
    public void update(Pessoa pessoa) throws SQLException {
        String sql = "UPDATE pessoa SET nome = ?, email = ?, cpf = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getEmail());
            stmt.setString(3, pessoa.getCpf());
            stmt.setInt(4, pessoa.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pessoa não encontrada.");
            }
        }
    }

    /**
     * UPDATE PASSWORD - Atualiza senha do usuário
     * Regra 27: Senha deve ser armazenada com hash
     */
    public void updatePassword(int id, String senhaAtual, String novaSenha) throws SQLException {
        // Buscar senha atual
        String selectSql = "SELECT senha FROM pessoa WHERE id = ?";
        String updateSql = "UPDATE pessoa SET senha = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Verificar senha atual
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String senhaHash = rs.getString("senha");
                        // Verificar se a senha atual está correta
                        if (!BCrypt.checkpw(senhaAtual, senhaHash)) {
                            throw new SQLException("Senha atual incorreta.");
                        }
                    } else {
                        throw new SQLException("Usuário não encontrado.");
                    }
                }
            }

            // Atualizar para nova senha
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, BCrypt.hashpw(novaSenha, BCrypt.gensalt(12)));
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
            }
        }
    }

    /**
     * DELETE - Remove uma pessoa (apenas se não tiver participações)
     */
    public void delete(int id) throws SQLException {
        // Verificar participações
        String checkCompradorSql = "SELECT COUNT(*) FROM lance WHERE comprador_id = ?";
        String checkVendedorSql = "SELECT COUNT(*) FROM item WHERE vendedor_id = ?";
        String deleteSql = "DELETE FROM pessoa WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            // Verificar lances
            try (PreparedStatement checkStmt = conn.prepareStatement(checkCompradorSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Não é possível excluir: usuário possui lances registrados.");
                    }
                }
            }

            // Verificar itens
            try (PreparedStatement checkStmt = conn.prepareStatement(checkVendedorSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Não é possível excluir: usuário possui itens cadastrados.");
                    }
                }
            }

            // Deletar pessoa
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Pessoa não encontrada.");
                }
            }
        }
    }

    /**
     * VALIDATE LOGIN - Valida credenciais de login
     * Regras: 3 (Somente autenticados podem participar), 27 (Senha com hash)
     */
    public Pessoa validateLogin(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaHash = rs.getString("senha");

                    // Verificar senha com BCrypt
                    if (BCrypt.checkpw(senha, senhaHash)) {
                        return mapResultSetToPessoa(rs);
                    }
                }
            }
        }
        return null; // Login inválido
    }

    /**
     * EMAIL EXISTS - Verifica se email já está cadastrado
     * Regra 2: Dados únicos
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pessoa WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * CPF EXISTS - Verifica se CPF já está cadastrado
     * Regra 2: Dados únicos
     */
    public boolean cpfExists(String cpf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pessoa WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Mapeia ResultSet para objeto Pessoa (polimórfico)
     * Regra 4: Papéis diferentes no sistema
     */
    private Pessoa mapResultSetToPessoa(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        Pessoa pessoa;

        switch (tipo) {
            case "COMPRADOR":
                pessoa = new Comprador();
                break;
            case "VENDEDOR":
                pessoa = new Vendedor();
                break;
            case "ADMINISTRADOR":
                pessoa = new Administrador();
                break;
            default:
                throw new SQLException("Tipo de pessoa inválido: " + tipo);
        }

        pessoa.setId(rs.getInt("id"));
        pessoa.setNome(rs.getString("nome"));
        pessoa.setEmail(rs.getString("email"));
        pessoa.setCpf(rs.getString("cpf"));
        // Não retornamos a senha por segurança

        return pessoa;
    }
}