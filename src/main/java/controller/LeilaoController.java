package controller;

import dao.LeilaoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import model.Comprador;
import model.Leilao;
import java.util.List;

public class LeilaoController {

    @FXML private Label lblBoasVindas;
    @FXML private TableView<Leilao> tabelaLeiloes;
    @FXML private Label lblStatus;

    private LeilaoDAO leilaoDAO = new LeilaoDAO();
    private Comprador compradorLogado;

    // Recebe o Comprador da tela de Login
    public void initData(Object data) {
        this.compradorLogado = (Comprador) data;
        lblBoasVindas.setText("Bem-vindo(a), " + compradorLogado.getNome() + "!");
        carregarLeiloes();
    }

    private void carregarLeiloes() {
        try {
            // 1. Chama o DAO
            List<Leilao> leiloesAtivos = leilaoDAO.readAllActive();
            ObservableList<Leilao> obsLeiloes = FXCollections.observableArrayList(leiloesAtivos);
            tabelaLeiloes.setItems(obsLeiloes);

        } catch (Exception e) {
            lblStatus.setText("Erro ao carregar leilões: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleDarLanceButton() {
        Leilao leilaoSelecionado = tabelaLeiloes.getSelectionModel().getSelectedItem();

        if (leilaoSelecionado == null) {
            lblStatus.setText("Por favor, selecione um leilão da lista.");
            return;
        }

        // 2. Chama a próxima tela (LanceView)
        // Precisamos passar o Leilão e o Comprador
        Object[] dadosParaLance = { leilaoSelecionado, compradorLogado };

        application.Main.changeScene(
                "/view/LanceView.fxml",
                "Dar Lance: " + leilaoSelecionado.getItem().getNome(),
                dadosParaLance
        );
    }
}