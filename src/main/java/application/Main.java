package application; // ou pacote principal

import controller.LanceController;
import controller.LeilaoController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Sistema de Leilões - Login");
        stage.setScene(scene);
        stage.show();
    }

    // Método utilitário para trocar de cena
    public static void changeScene(String fxml, String title, Object userData) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent root = loader.load();

            // Passa dados (ex: o usuário logado) para o próximo controller
            if (userData != null) {
                Object controller = loader.getController();
                if (controller instanceof LeilaoController) {
                    ((LeilaoController) controller).initData(userData);
                } else if (controller instanceof LanceController) {
                    ((LanceController) controller).initData(userData);
                }
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
            // Mostrar alerta de erro
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}