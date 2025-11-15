package controller;

import dao.LeilaoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Leilao;
import model.Vendedor;

import java.util.List;

public class MeusLeiloesController {

    @FXML private Label lblBoasVindas;
    @FXML private TableView<Leilao> tabelaLeiloes;
    @FXML private TableColumn<Leilao, String> colItem;
    @FXML private TableColumn<Leilao, String> colStatus;
    @FXML private TableColumn<Leilao, String> colDataInicio;
    @FXML private TableColumn<Leilao, String> colDataFim;
    @FXML private TableColumn<Leilao, Double> colValorAtual;
    @FXML private Label lblStatus;

    private LeilaoDAO leilaoDAO = new LeilaoDAO();
    private Vendedor vendedorLogado;

    public void initData(Object data) {
        this.vendedorLogado = (Vendedor) data;
        lblBoasVindas.setText("Meus Leilões - " + vendedorLogado.getNome());
        configurarTabela();
        carregarLeiloes();
    }

    private void configurarTabela() {
        colItem.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getItem().getNome()));
        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        colDataInicio.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDataInicio().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colDataFim.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDataFim().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colValorAtual.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(
                        cellData.getValue().getMaiorLanceValor()).asObject());
    }

    private void carregarLeiloes() {
        try {
            List<Leilao> leiloes = leilaoDAO.readByVendedor(vendedorLogado.getId());
            ObservableList<Leilao> obsLeiloes = FXCollections.observableArrayList(leiloes);
            tabelaLeiloes.setItems(obsLeiloes);
        } catch (Exception e) {
            lblStatus.setText("Erro ao carregar leilões: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleEncerrar() {
        Leilao leilao = tabelaLeiloes.getSelectionModel().getSelectedItem();

        if (leilao == null) {
            lblStatus.setText("Selecione um leilão.");
            return;
        }

        if (!"ATIVO".equals(leilao.getStatus())) {
            lblStatus.setText("Apenas leilões ativos podem ser encerrados.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Encerrar Leilão");
        confirmacao.setHeaderText("Deseja encerrar este leilão?");
        confirmacao.setContentText(leilao.getItem().getNome());

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                leilaoDAO.encerrarLeilao(leilao.getId());

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Sucesso");
                sucesso.setHeaderText("Leilão encerrado!");
                sucesso.showAndWait();

                carregarLeiloes();
            } catch (Exception e) {
                lblStatus.setText("Erro: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleCancelar() {
        Leilao leilao = tabelaLeiloes.getSelectionModel().getSelectedItem();

        if (leilao == null) {
            lblStatus.setText("Selecione um leilão.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Cancelar Leilão");
        confirmacao.setHeaderText("Deseja cancelar este leilão?");

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                leilaoDAO.cancelarLeilao(leilao.getId());
                carregarLeiloes();
            } catch (Exception e) {
                lblStatus.setText("Erro: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleAtualizar() {
        carregarLeiloes();
    }

    @FXML
    protected void handleVoltar() {
        application.Main.changeScene(
                "/view/MenuVendedorView.fxml",
                "Menu Vendedor",
                vendedorLogado
        );
    }
}