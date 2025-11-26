package controller;

import dao.PessoaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Comprador;
import model.Vendedor;
import model.Pessoa;

/**
 * Controller para tela de cadastro
 * Implementa as regras de negócio 1-4
 */
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
    @FXML private ProgressIndicator progressIndicator;

    private PessoaDAO pessoaDAO = new PessoaDAO();

    @FXML
    public void initialize() {
        // Configurar grupo de botões
        tipoUsuario = new ToggleGroup();
        rbComprador.setToggleGroup(tipoUsuario);
        rbVendedor.setToggleGroup(tipoUsuario);
        rbComprador.setSelected(true);

        // Formatação automática do CPF durante digitação
        txtCpf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtCpf.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 11) {
                txtCpf.setText(newValue.substring(0, 11));
            }
        });

        // Validação em tempo real
        txtNome.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtNome.getText().isEmpty()) {
                validarNome();
            }
        });

        txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtEmail.getText().isEmpty()) {
                validarEmail();
            }
        });

        txtCpf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtCpf.getText().isEmpty()) {
                validarCpf();
            }
        });

        // Esconder progress indicator inicialmente
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }

    /**
     * Valida nome em tempo real
     */
    private boolean validarNome() {
        String nome = txtNome.getText().trim();

        if (nome.isEmpty()) {
            mostrarErro("Nome é obrigatório.");
            return false;
        }
        if (nome.length() < 3) {
            mostrarErro("Nome deve ter no mínimo 3 caracteres.");
            return false;
        }
        if (!nome.matches("^[A-Za-zÀ-ÿ\\s]+$")) {
            mostrarErro("Nome deve conter apenas letras.");
            return false;
        }

        limparErro();
        return true;
    }

    /**
     * Valida email em tempo real
     */
    private boolean validarEmail() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            mostrarErro("Email é obrigatório.");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarErro("Email inválido. Use o formato: usuario@dominio.com");
            return false;
        }

        limparErro();
        return true;
    }

    /**
     * Valida CPF em tempo real
     */
    private boolean validarCpf() {
        String cpf = txtCpf.getText().replaceAll("[^0-9]", "");

        if (cpf.isEmpty()) {
            mostrarErro("CPF é obrigatório.");
            return false;
        }
        if (cpf.length() != 11) {
            mostrarErro("CPF deve conter 11 dígitos.");
            return false;
        }
        if (cpf.matches("(\\d)\\1{10}")) {
            mostrarErro("CPF inválido.");
            return false;
        }

        limparErro();
        return true;
    }

    /**
     * Handler para cadastro
     * Regras 1-4: Validação completa antes de persistir
     */
    @FXML
    protected void handleCadastrar() {
        try {
            // Mostrar indicador de progresso
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }

            // Regra 2: Validar campos obrigatórios
            if (!validarCamposObrigatorios()) {
                return;
            }

            // Regra 2: Validar confirmação de senha
            if (!txtSenha.getText().equals(txtConfirmaSenha.getText())) {
                mostrarErro("As senhas não coincidem.");
                return;
            }

            // Validar complexidade da senha
            if (!validarSenha(txtSenha.getText())) {
                return;
            }

            // Limpar CPF (remover formatação)
            String cpfLimpo = txtCpf.getText().replaceAll("[^0-9]", "");

            // Regra 2: Verificar se email já existe
            if (pessoaDAO.emailExists(txtEmail.getText().trim())) {
                mostrarErro("Este email já está cadastrado no sistema.");
                return;
            }

            // Regra 2: Verificar se CPF já existe
            if (pessoaDAO.cpfExists(cpfLimpo)) {
                mostrarErro("Este CPF já está cadastrado no sistema.");
                return;
            }

            // Regra 4: Criar objeto baseado no tipo selecionado
            Pessoa pessoa;
            String tipo;

            if (rbComprador.isSelected()) {
                pessoa = new Comprador();
                tipo = "COMPRADOR";
            } else {
                pessoa = new Vendedor();
                tipo = "VENDEDOR";
            }

            // Preencher dados (validações ocorrem nos setters do Model)
            pessoa.setNome(txtNome.getText().trim());
            pessoa.setEmail(txtEmail.getText().trim());
            pessoa.setCpf(cpfLimpo);
            pessoa.setSenha(txtSenha.getText());

            // Validar objeto completo
            if (!pessoa.isValido()) {
                mostrarErro("Dados incompletos ou inválidos.");
                return;
            }

            // Regra 1: Persistir no banco de dados
            pessoaDAO.create(pessoa, tipo);

            // Sucesso
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cadastro Realizado");
            alert.setHeaderText("Bem-vindo ao Sistema de Leilões!");
            alert.setContentText(String.format(
                    "Cadastro de %s realizado com sucesso!\n\n" +
                            "Nome: %s\n" +
                            "Email: %s\n" +
                            "Tipo: %s\n\n" +
                            "Você já pode fazer login no sistema.",
                    tipo, pessoa.getNome(), pessoa.getEmail(), tipo
            ));
            alert.showAndWait();

            // Voltar para tela de login
            handleVoltar();

        } catch (IllegalArgumentException e) {
            // Erro de validação do Model (Regra 2)
            mostrarErro("Validação: " + e.getMessage());
        } catch (Exception e) {
            // Erro de banco de dados ou sistema
            mostrarErro("Erro ao cadastrar: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Esconder indicador de progresso
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        }
    }

    /**
     * Valida todos os campos obrigatórios
     * Regra 2: Dados devem ser válidos
     */
    private boolean validarCamposObrigatorios() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarErro("Nome é obrigatório.");
            txtNome.requestFocus();
            return false;
        }

        if (!validarNome()) {
            txtNome.requestFocus();
            return false;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            mostrarErro("Email é obrigatório.");
            txtEmail.requestFocus();
            return false;
        }

        if (!validarEmail()) {
            txtEmail.requestFocus();
            return false;
        }

        if (txtCpf.getText().trim().isEmpty()) {
            mostrarErro("CPF é obrigatório.");
            txtCpf.requestFocus();
            return false;
        }

        if (!validarCpf()) {
            txtCpf.requestFocus();
            return false;
        }

        if (txtSenha.getText().isEmpty()) {
            mostrarErro("Senha é obrigatória.");
            txtSenha.requestFocus();
            return false;
        }

        if (txtConfirmaSenha.getText().isEmpty()) {
            mostrarErro("Confirmação de senha é obrigatória.");
            txtConfirmaSenha.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Valida complexidade da senha
     * Regra 27: Senha deve ser segura
     */
    private boolean validarSenha(String senha) {
        if (senha.length() < 6) {
            mostrarErro("Senha deve ter no mínimo 6 caracteres.");
            txtSenha.requestFocus();
            return false;
        }

        if (!senha.matches(".*[A-Za-z].*")) {
            mostrarErro("Senha deve conter pelo menos uma letra.");
            txtSenha.requestFocus();
            return false;
        }

        if (!senha.matches(".*[0-9].*")) {
            mostrarAviso("Recomendação: Senha deveria conter pelo menos um número.");
        }

        return true;
    }

    /**
     * Formata CPF automaticamente durante digitação
     */
    @FXML
    protected void handleFormatarCpf() {
        String cpf = txtCpf.getText().replaceAll("[^0-9]", "");

        if (cpf.length() == 11) {
            String cpfFormatado = String.format("%s.%s.%s-%s",
                    cpf.substring(0, 3),
                    cpf.substring(3, 6),
                    cpf.substring(6, 9),
                    cpf.substring(9, 11));
            txtCpf.setText(cpfFormatado);
        }
    }

    /**
     * Volta para tela de login
     */
    @FXML
    protected void handleVoltar() {
        application.Main.changeScene(
                "/view/LoginView.fxml",
                "Sistema de Leilões - Login",
                null
        );
    }

    /**
     * Limpa o formulário
     */
    @FXML
    protected void handleLimpar() {
        txtNome.clear();
        txtEmail.clear();
        txtCpf.clear();
        txtSenha.clear();
        txtConfirmaSenha.clear();
        rbComprador.setSelected(true);
        limparErro();
        txtNome.requestFocus();
    }

    // ===== MÉTODOS AUXILIARES =====

    private void mostrarErro(String mensagem) {
        lblStatus.setText("❌ " + mensagem);
        lblStatus.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
    }

    private void mostrarAviso(String mensagem) {
        lblStatus.setText("⚠️ " + mensagem);
        lblStatus.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
    }

    private void mostrarSucesso(String mensagem) {
        lblStatus.setText("✅ " + mensagem);
        lblStatus.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
    }

    private void limparErro() {
        lblStatus.setText("");
    }
}