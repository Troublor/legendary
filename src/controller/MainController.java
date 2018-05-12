package controller;

import com.sun.istack.internal.NotNull;
import custom.control.CodeEditor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.ApplicationData;

import java.io.*;

public class MainController {
    private Stage primaryStage;

    private ApplicationData applicationData = new ApplicationData();
    //Controls
    @FXML
    private TreeView<String> projectTreeView;
    @FXML
    private Label projectNameLabel;
    @FXML
    private CodeEditor codeEditor;
    @FXML
    private TabPane editTabPane;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void openFolderButtonOnClicked(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("打开项目");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            TreeItem<String> root = this.iterateFolder(directory);
            root.setExpanded(true);
            this.projectTreeView.setRoot(root);
            this.projectNameLabel.setText(directory.getName());
            //设置项目信息
            applicationData.setRootPath(directory.getAbsolutePath());
        }

    }

    /**
     * 遍历显示文件夹
     *
     * @param path 文件夹
     * @return TreeView的节点
     */
    private TreeItem<String> iterateFolder(@NotNull File path) {
        TreeItem<String> rootItem = new TreeItem<>(path.getName());
        if (path.isDirectory()) {
            rootItem.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("../img/folder.png"))));
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    TreeItem<String> item;
                    if (file.isDirectory()) {
                        item = this.iterateFolder(file);
                    } else {
                        item = new TreeItem<>(file.getName());
                        item.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("../img/file.png"))));
                    }
                    rootItem.getChildren().add(item);
                }
            }
        } else {
            rootItem.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("../img/file.png"))));
        }
        return rootItem;
    }


    public void stop() {
        codeEditor.stop();
    }

    /**
     * 双击一个文件时打开
     *
     * @param mouseEvent 鼠标点击事件
     */
    public void TreeViewOnDoubleClicked(MouseEvent mouseEvent) {
        TreeItem<String> item = projectTreeView.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && item.isLeaf()) {
            String fileName = item.getValue();
            CodeEditor codeEditor = new CodeEditor();

            File file = new File(applicationData.getRootPath() + File.separator + fileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while ((temp = reader.readLine()) != null) {
                    stringBuilder.append("<p>");
                    stringBuilder.append(temp);
                    stringBuilder.append("</p>");
                }
                codeEditor.setHtmlText(stringBuilder.toString());
            } catch (Exception e) {
                this.sendMessageDialog("发生错误", e.getMessage());
            }
            Tab newTab = new Tab(fileName);
            newTab.setClosable(true);
            newTab.setContent(codeEditor);
            editTabPane.getTabs().add(newTab);
            editTabPane.getSelectionModel().select(newTab);
        }
    }

    /**
     * 弹出提示框
     *
     * @param title 标题
     * @param msg   信息
     */
    private void sendMessageDialog(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); //设置标题，不设置默认标题为本地语言的information
        alert.setHeaderText(msg); //设置头标题，默认标题为本地语言的information
        alert.showAndWait(); //显示弹窗，同时后续代码等挂起
    }
}
