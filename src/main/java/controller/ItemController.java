package controller;

import dao.ItemDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Item;
import model.Vendedor;

import java.util.List;

public class ItemController {

    @FXML private Label lblBoasVindas;
    @FXML private TableView<Item> tabelaItens;
    @FXML private TableColumn<Item, String> colNome;
    @FXML private TableColumn<Item, String> colDescricao;
    @FXML private TableColumn<Item, Double> colValor;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private TextField txtValor;
    @FXML private TextField txtImagemUrl;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label lblStatus;

    private ItemDAO itemDAO = new ItemDAO();
    private Vendedor vendedorLogado;
    private Item itemSelecionado;

    public void initData(Object data) {
        this.vendedorLogado = (Vendedor) data;
        lblBoasVindas.setText("Itens de " + vendedorLogado.getNome());
        configurarTabela();
        carregarItens();
    }

    @FXML
    public void initialize() {
        // Listener para seleção na tabela
        tabelaItens.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        preencherFormulario(newSelection);
                    }
                }
        );
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorInicial"));

        // Formatar coluna de valor
        colValor.setCellFactory(column -> new TableCell<Item, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", item));
                }
            }
        });
    }

    private void carregarItens() {
        try {
            List<Item> itens = itemDAO.readByVendedor(vendedorLogado.getId());
            ObservableList<Item> obsItens = FXCollections.observableArrayList(itens);
            tabelaItens.setItems(obsItens);
            lblStatus.setText("Total de itens: " + itens.size());
            lblStatus.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblStatus.setText("Erro ao carregar itens: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void preencherFormulario(Item item) {
        this.itemSelecionado = item;
        txtNome.setText(item.getNome());
        txtDescricao.setText(item.getDescricao());
        txtValor.setText(String.valueOf(item.getValorInicial()));
        txtImagemUrl.setText(item.getImagemUrl());
        btnSalvar.setText("Atualizar Item");
    }

    private void limparFormulario() {
        this.itemSelecionado = null;
        txtNome.clear();
        txtDescricao.clear();
        txtValor.clear();
        txtImagemUrl.clear();
        btnSalvar.setText("Cadastrar Item");
        tabelaItens.getSelectionModel().clearSelection();
        lblStatus.setText("");
    }

    @FXML
    protected void handleSalvar() {
        try {
            // Validar campos
            if (txtNome.getText().trim().isEmpty() ||
                    txtDescricao.getText().trim().isEmpty() ||
                    txtValor.getText().trim().isEmpty()) {
                lblStatus.setText("Preencha todos os campos obrigatórios.");
                lblStatus.setStyle("-fx-text-fill: red;");
                return;
            }

            double valor = Double.parseDouble(txtValor.getText());

            if (itemSelecionado == null) {
                // Criar novo item
                Item novoItem = new Item(
                        txtNome.getText().trim(),
                        txtDescricao.getText().trim(),
                        valor,
                        vendedorLogado
                );
                novoItem.setImagemUrl(txtImagemUrl.getText().trim());

                itemDAO.create(novoItem);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText("Item cadastrado com sucesso!");
                alert.showAndWait();

            } else {
                // Atualizar item existente
                itemSelecionado.setNome(txtNome.getText().trim());
                itemSelecionado.setDescricao(txtDescricao.getText().trim());
                itemSelecionado.setValorInicial(valor);
                itemSelecionado.setImagemUrl(txtImagemUrl.getText().trim());

                itemDAO.update(itemSelecionado);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText("Item atualizado com sucesso!");
                alert.showAndWait();
            }

            limparFormulario();
            carregarItens();

        } catch (NumberFormatException e) {
            lblStatus.setText("Valor inicial inválido.");
            lblStatus.setStyle("-fx-text-fill: red;");
        } catch (IllegalArgumentException e) {
            lblStatus.setText("Erro: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            lblStatus.setText("Erro ao salvar: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCancelar() {
        limparFormulario();
    }

    @FXML
    protected void handleCriarLeilao() {
        Item item = tabelaItens.getSelectionModel().getSelectedItem();

        if (item == null) {
            lblStatus.setText("Selecione um item para criar leilão.");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        Object[] dados = { item, vendedorLogado };
        application.Main.changeScene(
                "/view/CriarLeilaoView.fxml",
                "Criar Leilão",
                dados
        );
    }

    @FXML
    protected void handleExcluir() {
        Item item = tabelaItens.getSelectionModel().getSelectedItem();

        if (item == null) {
            lblStatus.setText("Selecione um item para excluir.");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Deseja excluir o item?");
        confirmacao.setContentText(item.getNome());

        if (confirmacao.showAndWait().get() == ButtonType.OK) {
            try {
                itemDAO.delete(item.getId());

                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Sucesso");
                sucesso.setHeaderText("Item excluído com sucesso!");
                sucesso.showAndWait();

                limparFormulario();
                carregarItens();
            } catch (Exception e) {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro");
                erro.setHeaderText("Não foi possível excluir o item");
                erro.setContentText(e.getMessage());
                erro.showAndWait();
            }
        }
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