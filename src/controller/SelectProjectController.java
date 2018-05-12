package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sun.applet.Main;

import java.io.IOException;

public class SelectProjectController {
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void CreateProjectButtonOnAction(ActionEvent actionEvent) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SelectProjectController.class.getResource("../CreateProjectDialog.fxml"));
            Pane page = loader.load();
            // Create the dialog Stage.

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            CreateProjectController controller = loader.getController();
            controller.setPrimaryStage(dialogStage);
            // Set the person into the controller.
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            if (controller.getResult() != null) {
                System.out.println("打开工程");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
