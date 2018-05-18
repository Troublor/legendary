package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TerminalToolTileController extends VBox {
    @FXML
    private Label titleLabel;

    public TerminalToolTileController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../layout/ToolTileControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        titleLabel.setText("终端");
    }
}
