package controller;

import dao.RelatorioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import model.Pessoa;

import java.util.Map;

public class RelatorioController {

    @FXML private Label lblTotalLeiloes;
    @FXML private Label lblTotalLances;
    @FXML private Label lblValorMedio;
    @FXML private TableView tabelaCompradoresAtivos;
    @FXML private PieChart chartLeiloeStatus;

    private RelatorioDAO relatorioDAO = new RelatorioDAO();
    private Pessoa usuarioLogado;

    public void initData(Object data) {
        this.usuarioLogado = (Pessoa) data;
        carregarEstatisticas();
    }

    @FXML
    public void initialize() {
        carregarEstatisticas();
    }

    private void carregarEstatisticas() {
        try {
            // Estatísticas gerais
            Map<String, Object> stats = relatorioDAO.getEstatisticasGerais();

            lblTotalLeiloes.setText(String.valueOf(stats.getOrDefault("total_leiloes", 0)));
            lblTotalLances.setText(String.valueOf(stats.getOrDefault("total_lances", 0)));

            double valorMedio = (double) stats.getOrDefault("valor_medio_lance", 0.0);
            lblValorMedio.setText(String.format("R$ %.2f", valorMedio));

            // Gráfico de leilões por status
            Map<String, Integer> statusMap = relatorioDAO.getLeiloesStatus();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            statusMap.forEach((status, count) -> {
                pieChartData.add(new PieChart.Data(status + " (" + count + ")", count));
            });

            chartLeiloeStatus.setData(pieChartData);
            chartLeiloeStatus.setTitle("Distribuição de Leilões por Status");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleAtualizar() {
        carregarEstatisticas();
    }

    @FXML
    protected void handleExportarPDF() {
        // TODO: Implementar exportação para PDF
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar PDF");
        alert.setHeaderText("Funcionalidade em desenvolvimento");
        alert.setContentText("A exportação para PDF será implementada em breve.");
        alert.showAndWait();
    }

    @FXML
    protected void handleVoltar() {
        if (usuarioLogado instanceof model.Vendedor) {
            application.Main.changeScene(
                    "/view/MenuVendedorView.fxml",
                    "Menu Vendedor",
                    usuarioLogado
            );
        } else if (usuarioLogado instanceof model.Administrador) {
            application.Main.changeScene(
                    "/view/AdminView.fxml",
                    "Menu Administrador",
                    usuarioLogado
            );
        } else {
            application.Main.changeScene(
                    "/view/LeilaoView.fxml",
                    "Leilões Ativos",
                    usuarioLogado
            );
        }
    }
}