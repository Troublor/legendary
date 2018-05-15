package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

public class CreateProjectController extends DialogController {
    //controls
    @FXML
    private TextField projectNameTextField;

    @FXML
    private TextField projectPathTextField;

    @FXML
    public void selectPathButtonOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("打开项目");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            if (directory.getName().equals(projectNameTextField.getText())) {
                projectPathTextField.setText(directory.getPath());
            } else {
                projectPathTextField.setText(directory.getPath() + File.separator + projectNameTextField.getText());
            }
        }
    }

    @FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
        String projectPath = projectPathTextField.getText();
        if (!projectPath.endsWith(projectNameTextField.getText())) {
            projectPath += File.separator + projectNameTextField.getText();
        }
        File directory = new File(projectPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                new Alert(Alert.AlertType.ERROR, "创建失败").showAndWait();
                return;
            }
        }
        result = projectPath;
        super.confirmButtonOnAction(actionEvent);
    }

    @FXML
    public void cancelButtonOnAction(ActionEvent actionEvent){
        super.cancelButtonOnAction(actionEvent);
    }

    //self
    private String result = null;

    public String getResult() {
        return result;
    }
}
