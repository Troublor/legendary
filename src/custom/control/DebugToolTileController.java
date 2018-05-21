package custom.control;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DebugToolTileController extends VBox {
    public DebugToolTileController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DebugToolTileControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
