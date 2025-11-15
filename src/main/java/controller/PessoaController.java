package controller;

import dao.PessoaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Comprador;

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
            // 1. Validação (Regra de Negócio no Model)
            // (Aqui poderíamos validar o formato do email antes de ir ao DAO)
            if (email.isEmpty() || senha.isEmpty()) {
                lblStatus.setText("Email e senha são obrigatórios.");
                return;
            }

            // 2. Chama o DAO
            Comprador comprador = pessoaDAO.validateLogin(email, senha);

            if (comprador != null) {
                // 3. Sucesso: Navega para a próxima tela
                lblStatus.setText("Login bem-sucedido!");

                // Passa o objeto 'comprador' para a próxima cena
                application.Main.changeScene(
                        "/view/LeilaoView.fxml",
                        "Leilões Ativos - " + comprador.getNome(),
                        comprador
                );

            } else {
                // 4. Falha
                lblStatus.setText("Email ou senha inválidos.");
            }

        } catch (Exception e) {
            lblStatus.setText("Erro de banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}