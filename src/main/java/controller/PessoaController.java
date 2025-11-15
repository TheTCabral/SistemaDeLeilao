package controller;

import dao.PessoaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Administrador;
import model.Comprador;
import model.Pessoa;
import model.Vendedor;

public class PessoaController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblStatus;

    private PessoaDAO pessoaDAO = new PessoaDAO();

    @FXML
    protected void handleLoginButton() {
        String email = txtEmail.getText();
        String senha = txtSenha.getText();

        try {
            // 1. Validação básica
            if (email.isEmpty() || senha.isEmpty()) {
                lblStatus.setText("Email e senha são obrigatórios.");
                return;
            }

            // 2. Validar login no DAO
            Pessoa pessoa = pessoaDAO.validateLogin(email, senha);

            if (pessoa != null) {
                lblStatus.setText("Login bem-sucedido!");

                // 3. Redirecionar para a tela apropriada baseado no tipo
                if (pessoa instanceof Comprador) {
                    application.Main.changeScene(
                            "/view/LeilaoView.fxml",
                            "Leilões Ativos - " + pessoa.getNome(),
                            pessoa
                    );
                } else if (pessoa instanceof Vendedor) {
                    application.Main.changeScene(
                            "/view/MenuVendedorView.fxml",
                            "Menu Vendedor - " + pessoa.getNome(),
                            pessoa
                    );
                } else if (pessoa instanceof Administrador) {
                    application.Main.changeScene(
                            "/view/AdminView.fxml",
                            "Administração - " + pessoa.getNome(),
                            pessoa
                    );
                }

            } else {
                lblStatus.setText("Email ou senha inválidos.");
            }

        } catch (Exception e) {
            lblStatus.setText("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCadastrarButton() {
        application.Main.changeScene(
                "/view/CadastroView.fxml",
                "Cadastro de Usuário",
                null
        );
    }
}