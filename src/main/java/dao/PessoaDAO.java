package dao;

import model.Comprador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PessoaDAO {

    /**
     * Valida o login.
     * Retorna um objeto (Comprador) se o login for válido, ou null se inválido.
     * Usamos Comprador pois apenas eles podem dar lances[cite: 13].
     */
    public Comprador validateLogin(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM pessoa WHERE email = ? AND senha = ? AND tipo = 'COMPRADOR'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha); // Em produção, a senha no BD deve estar com HASH [cite: 35]

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Comprador comprador = new Comprador();
                    comprador.setId(rs.getInt("id"));
                    comprador.setNome(rs.getString("nome"));
                    comprador.setEmail(rs.getString("email"));
                    comprador.setCpf(rs.getString("cpf"));
                    return comprador;
                }
            }
        }
        return null; // Login inválido
    }

    // ... Aqui entrariam os métodos create(), update() e delete() de Pessoa
}