package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RenameController extends Controller{
    //controls
    @FXML
    private TextField nameTextField;

    @FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
    }

    @FXML
    public void cancelButtonOnAction(ActionEvent actionEvent) {
    }

    //self
    private Stage primaryStage;
}
