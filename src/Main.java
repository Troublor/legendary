import controller.MainController;
import controller.SelectProjectController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/SelectProjectPage.fxml"));
        Parent root = loader.load();
        SelectProjectController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setTitle("ASMax");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        controller.stop();
    }
}
