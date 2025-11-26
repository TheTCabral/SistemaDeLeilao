package dao;

import model.Administrador;

public class AdministradorDAO {

    // private Connection connection; // Conexão com o BD (ex: JDBC, JPA)

    public AdministradorDAO() {
        // Inicializa a conexão com o banco de dados
        // this.connection = ...;
    }

    /**
     * Salva um novo administrador no banco de dados.
     * A senha já deve chegar "hasheada". [cite: 35]
     */
    public void salvar(Administrador admin) {
        System.out.println("Salvando admin no BD: " + admin.getNome());
        // Lógica de INSERT INTO administrador (nome, email, cpfCnpj, senhaHash) ...
        // Deve garantir que email e cpfCnpj sejam únicos
    }

    /**
     * Busca um administrador pelo seu ID.
     */
    public Administrador buscarPorId(int id) {
        System.out.println("Buscando admin por ID: " + id);
        // Lógica de SELECT * FROM administrador WHERE id = ?
        return null; // Retorno de exemplo
    }

    /**
     * Busca um administrador pelo email (usado no login).
     */
    public Administrador buscarPorEmail(String email) {
        System.out.println("Buscando admin por email: " + email);
        // Lógica de SELECT * FROM administrador WHERE email = ?
        return null; // Retorno de exemplo
    }

    /**
     * Atualiza os dados de um administrador.
     */
    public void atualizar(Administrador admin) {
        System.out.println("Atualizando admin no BD: " + admin.getNome());
        // Lógica de UPDATE administrador SET ... WHERE id = ?
    }
}