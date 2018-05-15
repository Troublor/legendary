package controller;

import com.sun.istack.internal.NotNull;
import custom.control.CodeEditor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.ApplicationData;
import model.ProjectFile;

import java.io.*;

public class MainController {
    //Controls
    @FXML
    private TreeView<ProjectFile> projectTreeView;
    @FXML
    private Label projectNameLabel;
    @FXML
    private CodeEditor codeEditor;
    @FXML
    private TabPane editTabPane;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * 根据root path初始化
     */
    public void initializeProject(String path) {
        this.applicationData.setRootPath(path);
        ProjectFile directory = new ProjectFile(applicationData.getRootPath());
        TreeItem<ProjectFile> root = this.iterateFolder(directory);
        root.setExpanded(true);
        this.projectTreeView.setRoot(root);
        this.projectNameLabel.setText(directory.getName());
    }

    @FXML
    public void openFolderButtonOnClicked(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("打开项目");
        File directory = directoryChooser.showDialog(primaryStage);
        if (directory != null) {
            this.initializeProject(directory.getPath());
        }
    }

    /**
     * 双击一个文件时打开
     *
     * @param mouseEvent 鼠标点击事件
     */
    @FXML
    public void TreeViewOnDoubleClicked(MouseEvent mouseEvent) {
        /*TreeItem<String> item = projectTreeView.getSelectionModel().getSelectedItem();
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
        }*/
    }

    /**
     * 新建文件按钮点击事件
     *
     * @param actionEvent 事件
     */
    @FXML
    public void newFileButtonOnAction(ActionEvent actionEvent) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SelectProjectController.class.getResource("../layout/NewItemDialog.fxml"));
            Pane page = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("新建");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            NewItemController controller = loader.getController();
            controller.setPrimaryStage(dialogStage);
//            controller.setCurrPath();
            controller.setFileType(".asm");
            controller.setTitleLabel("新建ASM文件");
            dialogStage.showAndWait();
            String fileName = controller.getFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //self resources

    private Stage primaryStage;

    private ApplicationData applicationData = new ApplicationData();

    /**
     * 遍历显示文件夹
     *
     * @param path 文件夹
     * @return TreeView的节点
     */
    private TreeItem<ProjectFile> iterateFolder(@NotNull ProjectFile path) {
        TreeItem<ProjectFile> rootItem = new TreeItem<>(path);
        if (path.isDirectory()) {
            rootItem.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("../img/folder.png"))));
            ProjectFile[] files = path.listFiles();
            if (files != null) {
                for (ProjectFile file : files) {
                    TreeItem<ProjectFile> item;
                    if (file.isDirectory()) {
                        item = this.iterateFolder(file);
                    } else {
                        item = new TreeItem<>(file);
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

    private void getCurrentFolder() {
//        TreeItem<String> item = this.projectTreeView.getSelectionModel().getSelectedItem();

    }
}
