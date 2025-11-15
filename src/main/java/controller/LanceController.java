package controller;

import dao.LanceDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Comprador;
import model.Lance;
import model.Leilao;

public class LanceController {

    @FXML private Label lblItemNome;
    @FXML private Label lblItemDescricao;
    @FXML private Label lblMaiorLance;
    @FXML private Label lblVendedor; // (Vendedor não foi carregado no DAO, ficaria pendente)
    @FXML private TextField txtValorLance;
    @FXML private Label lblStatus;

    private LanceDAO lanceDAO = new LanceDAO();
    private Leilao leilaoAtual;
    private Comprador compradorLogado;

    // Recebe os dados da LeilaoController
    public void initData(Object data) {
        Object[] dados = (Object[]) data;
        this.leilaoAtual = (Leilao) dados[0];
        this.compradorLogado = (Comprador) dados[1];

        // Preenche a tela com os dados
        lblItemNome.setText(leilaoAtual.getItem().getNome());
        lblItemDescricao.setText(leilaoAtual.getItem().getDescricao());

        double maiorLanceValor = leilaoAtual.getMaiorLanceValor();
        lblMaiorLance.setText("Lance atual: R$ " + String.format("%.2f", maiorLanceValor));

        // Sugere um valor inicial no campo (Valor atual + incremento, ex: 10)
        txtValorLance.setText(String.format("%.2f", maiorLanceValor + 10.0));
    }

    @FXML
    protected void handleConfirmarLance() {
        try {
            // 1. Ler valor da interface
            double valorProposto = Double.parseDouble(txtValorLance.getText());

            // 2. Criar objeto Model
            Lance novoLance = new Lance(compradorLogado, valorProposto);

            // 3. Aplicar Regra de Negócio (no Model) [cite: 162]
            // O método proporLance() do Leilao vai disparar uma exceção
            // se o valor for menor ou igual ao atual [cite: 14, 75]
            leilaoAtual.proporLance(novoLance); // Isso valida o valor

            // 4. Se válido, persistir no Banco (DAO) [cite: 163]
            lanceDAO.create(novoLance, leilaoAtual.getId());

            // 5. Sucesso [cite: 156]
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Lance realizado com sucesso!");
            alert.setContentText("Seu lance de R$ " + valorProposto + " foi registrado.");
            alert.showAndWait();

            handleVoltar(); // Volta para a lista

        } catch (NumberFormatException e) {
            // Erro de digitação
            lblStatus.setText("Erro: Insira um valor numérico válido (ex: 150.50)");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Erro da Regra de Negócio (ex: "Valor muito baixo") [cite: 162]
            lblStatus.setText("Erro: " + e.getMessage());
        } catch (Exception e) {
            // Erro de Banco de Dados [cite: 163]
            lblStatus.setText("Erro de sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleVoltar() {
        // Volta para a tela de leilões, passando o usuário logado de volta
        application.Main.changeScene(
                "/view/LeilaoView.fxml",
                "Leilões Ativos - " + compradorLogado.getNome(),
                compradorLogado
        );
    }
}