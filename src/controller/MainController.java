package src.controller;

import com.sun.istack.internal.NotNull;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {
    private Stage primaryStage;
    //Controls
    @FXML
    private TreeView<String> projectTreeView;
    @FXML
    private Label projectNameLabel;
    @FXML
    private CodeEditor codeEditor;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void openFolderButtonOnClicked(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("打开项目");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            this.projectTreeView.setRoot(this.iterateFolder(directory));
            this.projectNameLabel.setText(directory.getName());
        }
    }

    private TreeItem<String> iterateFolder(@NotNull File path) {
        TreeItem<String> rootItem = new TreeItem<>(path.getName());
        if (path.isDirectory()) {
            rootItem.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("../../img/folder.png"))));
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    TreeItem<String> item;
                    if (file.isDirectory()) {
                        item = this.iterateFolder(file);
                    } else {
                        item = new TreeItem<>(file.getName());
                        item.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("../../img/file.png"))));
                    }
                    rootItem.getChildren().add(item);
                }
            }
        } else {
            rootItem.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("../../img/file.png"))));
        }
        return rootItem;
    }


    public void stop() {
        codeEditor.stop();
    }
}
