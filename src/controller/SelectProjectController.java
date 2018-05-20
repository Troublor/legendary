package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SelectProjectController extends Controller {
    //controls
    @FXML
    public void CreateProjectButtonOnAction(ActionEvent actionEvent) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SelectProjectController.class.getResource("../layout/CreateProjectDialog.fxml"));
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
                this.openMainPage(controller.getResult());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openProjectButtonOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("打开项目");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            this.openMainPage(directory.getPath());
        }
    }

    //self
    private void openMainPage(String projectPath){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/MainPage.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.initializeProject(projectPath);
            primaryStage.setTitle("ASMax");
            primaryStage.setX(50);
            primaryStage.setY(50);
            primaryStage.setWidth(1080);
            primaryStage.setHeight(768);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
