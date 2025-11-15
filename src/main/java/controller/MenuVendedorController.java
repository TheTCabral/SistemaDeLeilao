package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Vendedor;

public class MenuVendedorController {

    @FXML private Label lblBoasVindas;
    @FXML private Label lblStatus;

    private Vendedor vendedorLogado;

    public void initData(Object data) {
        this.vendedorLogado = (Vendedor) data;
        lblBoasVindas.setText("Bem-vindo, " + vendedorLogado.getNome() + "!");
    }

    @FXML
    protected void handleGerenciarItens() {
        application.Main.changeScene(
                "/view/ItemView.fxml",
                "Gerenciar Itens",
                vendedorLogado
        );
    }

    @FXML
    protected void handleMeusLeiloes() {
        application.Main.changeScene(
                "/view/MeusLeiloesView.fxml",
                "Meus Leilões",
                vendedorLogado
        );
    }

    @FXML
    protected void handleRelatorios() {
        application.Main.changeScene(
                "/view/RelatorioView.fxml",
                "Relatórios",
                vendedorLogado
        );
    }

    @FXML
    protected void handleSair() {
        application.Main.changeScene(
                "/view/LoginView.fxml",
                "Sistema de Leilões - Login",
                null
        );
    }
}