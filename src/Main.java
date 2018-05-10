package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.controller.MainController;

public class Main extends Application {
    MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setTitle("ASMax");
        primaryStage.setScene(new Scene(root, 1080, 640));
        primaryStage.show();
    }

    @Override
    public void stop() {
        controller.stop();
    }
}
