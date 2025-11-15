package controller;

import dao.PessoaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Comprador;
import model.Vendedor;
import model.Pessoa;

public class CadastroController {

    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCpf;
    @FXML private PasswordField txtSenha;
    @FXML private PasswordField txtConfirmaSenha;
    @FXML private RadioButton rbComprador;
    @FXML private RadioButton rbVendedor;
    @FXML private ToggleGroup tipoUsuario;
    @FXML private Label lblStatus;

    private PessoaDAO pessoaDAO = new PessoaDAO();

    @FXML
    public void initialize() {
        tipoUsuario = new ToggleGroup();
        rbComprador.setToggleGroup(tipoUsuario);
        rbVendedor.setToggleGroup(tipoUsuario);
        rbComprador.setSelected(true);
    }

    @FXML
    protected void handleCadastrar() {
        try {
            // 1. Validar campos obrigatórios
            if (txtNome.getText().trim().isEmpty() ||
                    txtEmail.getText().trim().isEmpty() ||
                    txtCpf.getText().trim().isEmpty() ||
                    txtSenha.getText().isEmpty()) {
                lblStatus.setText("Todos os campos são obrigatórios.");
                return;
            }

            // 2. Validar confirmação de senha
            if (!txtSenha.getText().equals(txtConfirmaSenha.getText())) {
                lblStatus.setText("As senhas não coincidem.");
                return;
            }

            // 3. Verificar se email já existe
            if (pessoaDAO.emailExists(txtEmail.getText())) {
                lblStatus.setText("Este email já está cadastrado.");
                return;
            }

            // 4. Verificar se CPF já existe
            if (pessoaDAO.cpfExists(txtCpf.getText().replaceAll("[^0-9]", ""))) {
                lblStatus.setText("Este CPF já está cadastrado.");
                return;
            }

            // 5. Criar objeto baseado no tipo selecionado
            String tipo;
            Pessoa pessoa;

            if (rbComprador.isSelected()) {
                pessoa = new Comprador();
                tipo = "COMPRADOR";
            } else {
                pessoa = new Vendedor();
                tipo = "VENDEDOR";
            }

            // 6. Preencher dados (validações ocorrem nos setters)
            pessoa.setNome(txtNome.getText().trim());
            pessoa.setEmail(txtEmail.getText().trim());
            pessoa.setCpf(txtCpf.getText().replaceAll("[^0-9]", ""));
            pessoa.setSenha(txtSenha.getText());

            // 7. Persistir no banco de dados
            pessoaDAO.create(pessoa, tipo);

            // 8. Sucesso
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Cadastro realizado com sucesso!");
            alert.setContentText("Você já pode fazer login no sistema.");
            alert.showAndWait();

            handleVoltar();

        } catch (IllegalArgumentException e) {
            // Erro de validação do Model
            lblStatus.setText("Erro: " + e.getMessage());
        } catch (Exception e) {
            // Erro de banco de dados
            lblStatus.setText("Erro ao cadastrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleVoltar() {
        application.Main.changeScene(
                "/view/LoginView.fxml",
                "Sistema de Leilões - Login",
                null
        );
    }

    @FXML
    protected void handleFormatarCpf() {
        String cpf = txtCpf.getText().replaceAll("[^0-9]", "");
        if (cpf.length() == 11) {
            txtCpf.setText(cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9, 11));
        }
    }
}