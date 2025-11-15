package controller;

import dao.LeilaoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Item;
import model.Leilao;
import model.Vendedor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CriarLeilaoController {

    @FXML private Label lblItemNome;
    @FXML private Label lblItemDescricao;
    @FXML private Label lblValorInicial;
    @FXML private DatePicker dpDataInicio;
    @FXML private TextField txtHoraInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private TextField txtHoraFim;
    @FXML private TextField txtIncrementoMinimo;
    @FXML private RadioButton rbAberto;
    @FXML private RadioButton rbOculto;
    @FXML private ToggleGroup tipoLeilao;
    @FXML private Label lblStatus;

    private LeilaoDAO leilaoDAO = new LeilaoDAO();
    private Item item;
    private Vendedor vendedor;

    @FXML
    public void initialize() {
        tipoLeilao = new ToggleGroup();
        rbAberto.setToggleGroup(tipoLeilao);
        rbOculto.setToggleGroup(tipoLeilao);
        rbAberto.setSelected(true);

        // Definir valores padrão
        dpDataInicio.setValue(LocalDate.now());
        txtHoraInicio.setText("10:00");
        dpDataFim.setValue(LocalDate.now().plusDays(7));
        txtHoraFim.setText("18:00");
        txtIncrementoMinimo.setText("10.00");
    }

    public void initData(Object data) {
        Object[] dados = (Object[]) data;
        this.item = (Item) dados[0];
        this.vendedor = (Vendedor) dados[1];

        lblItemNome.setText("Item: " + item.getNome());
        lblItemDescricao.setText(item.getDescricao());
        lblValorInicial.setText(String.format("Valor Inicial: R$ %.2f", item.getValorInicial()));
    }

    @FXML
    protected void handleCriarLeilao() {
        try {
            // Validar campos
            if (dpDataInicio.getValue() == null || dpDataFim.getValue() == null ||
                    txtHoraInicio.getText().trim().isEmpty() || txtHoraFim.getText().trim().isEmpty() ||
                    txtIncrementoMinimo.getText().trim().isEmpty()) {
                lblStatus.setText("Preencha todos os campos.");
                return;
            }

            // Parsear datas e horas
            LocalTime horaInicio = LocalTime.parse(txtHoraInicio.getText().trim());
            LocalTime horaFim = LocalTime.parse(txtHoraFim.getText().trim());

            LocalDateTime dataInicio = LocalDateTime.of(dpDataInicio.getValue(), horaInicio);
            LocalDateTime dataFim = LocalDateTime.of(dpDataFim.getValue(), horaFim);

            double incremento = Double.parseDouble(txtIncrementoMinimo.getText());

            // Criar leilão
            Leilao novoLeilao = new Leilao(item);
            novoLeilao.setDataInicio(dataInicio);
            novoLeilao.setDataFim(dataFim);
            novoLeilao.setIncrementoMinimo(incremento);
            novoLeilao.setTipo(rbAberto.isSelected() ? "ABERTO" : "OCULTO");

            // Definir status baseado na data
            if (dataInicio.isBefore(LocalDateTime.now().plusHours(1))) {
                novoLeilao.setStatus("ATIVO");
            } else {
                novoLeilao.setStatus("PENDENTE");
            }

            // Persistir no banco
            leilaoDAO.create(novoLeilao);

            // Sucesso
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Leilão criado com sucesso!");
            alert.setContentText("O leilão foi cadastrado e está " + novoLeilao.getStatus().toLowerCase() + ".");
            alert.showAndWait();

            handleVoltar();

        } catch (Exception e) {
            lblStatus.setText("Erro ao criar leilão: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleVoltar() {
        application.Main.changeScene(
                "/view/ItemView.fxml",
                "Gerenciar Itens",
                vendedor
        );
    }
}
