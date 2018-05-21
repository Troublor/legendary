package custom.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ToolTileController extends VBox {
    @FXML
    protected Label titleLabel;

    @FXML
    protected TextArea textArea;

    public ToolTileController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ToolTileControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
