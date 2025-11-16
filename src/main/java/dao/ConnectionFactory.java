package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory para gerenciar conexões com o banco de dados
 * Implementa o padrão Singleton para pool de conexões
 */
public class ConnectionFactory {

    // Configurações do banco de dados
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/leilao_db";
    private static final String USER = "root";
    private static final String PASSWORD = "senha123";

    // Configurações adicionais para MySQL 8.0+
    private static final Properties PROPERTIES = new Properties();

    static {
        // Configurar propriedades da conexão
        PROPERTIES.setProperty("user", USER);
        PROPERTIES.setProperty("password", PASSWORD);
        PROPERTIES.setProperty("useSSL", "false");
        PROPERTIES.setProperty("serverTimezone", "America/Sao_Paulo");
        PROPERTIES.setProperty("allowPublicKeyRetrieval", "true");
        PROPERTIES.setProperty("characterEncoding", "UTF-8");

        // Carregar o driver JDBC
        try {
            Class.forName(DRIVER);
            System.out.println("✅ Driver MySQL carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Erro ao carregar driver MySQL:");
            e.printStackTrace();
            throw new RuntimeException("Driver MySQL não encontrado", e);
        }
    }

    /**
     * Obtém uma conexão com o banco de dados
     * @return Connection objeto de conexão
     * @throws SQLException se houver erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, PROPERTIES);

            // Log de conexão bem-sucedida (apenas para desenvolvimento)
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conectado ao banco de dados: " + URL);
            }

            return conn;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco de dados:");
            System.err.println("   URL: " + URL);
            System.err.println("   User: " + USER);
            System.err.println("   Mensagem: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Fecha uma conexão com o banco de dados
     * @param conn Connection a ser fechada
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Conexão fechada com sucesso");
            } catch (SQLException e) {
                System.err.println("❌ Erro ao fechar conexão:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Testa a conexão com o banco de dados
     * @return true se a conexão foi bem-sucedida
     */
    public static boolean testConnection() {
        System.out.println("\n🔍 Testando conexão com o banco de dados...");
        System.out.println("   URL: " + URL);
        System.out.println("   User: " + USER);

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Teste de conexão bem-sucedido!");
                System.out.println("   Database: " + conn.getCatalog());
                System.out.println("   Driver: " + conn.getMetaData().getDriverName());
                System.out.println("   Versão: " + conn.getMetaData().getDriverVersion());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Teste de conexão falhou!");
            System.err.println("   Erro: " + e.getMessage());
            System.err.println("\n💡 Verifique:");
            System.err.println("   1. MySQL está rodando?");
            System.err.println("   2. Banco 'leilao_db' foi criado?");
            System.err.println("   3. Usuário e senha estão corretos?");
            System.err.println("   4. Firewall permite conexão na porta 3306?");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Método main para teste standalone
     */
    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("  TESTE DE CONEXÃO - BANCO DE DADOS");
        System.out.println("==================================\n");

        boolean sucesso = testConnection();

        System.out.println("\n==================================");
        if (sucesso) {
            System.out.println("✅ Sistema pronto para uso!");
        } else {
            System.out.println("❌ Configure o banco antes de usar");
        }
        System.out.println("==================================\n");
    }
}