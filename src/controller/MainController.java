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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @FXML
    private void openFolderButtonOnClicked(MouseEvent mouseEvent) {
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
    private void TreeViewOnDoubleClicked(MouseEvent mouseEvent) {
        TreeItem<ProjectFile> item = projectTreeView.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && item.getValue().isFile()) {
            ProjectFile file = item.getValue();
            this.openFile(file);
        }
    }

    /**
     * 新建文件按钮点击事件
     *
     * @param actionEvent 事件
     */
    @FXML
    private void newFileButtonOnAction(ActionEvent actionEvent) {
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
            TreeItem<ProjectFile> folderTreeItem = this.getCurrentFolderTreeItem();
            controller.setCurrPath(folderTreeItem.getValue().getPath());
            controller.setFileType(".asm");
            controller.setTitleLabel("新建ASM文件");
            dialogStage.showAndWait();
            if (!controller.isSuccess()) {
                return;
            }
            this.initializeProject(this.applicationData.getRootPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void renameMenuItemOnAction(ActionEvent actionEvent) {
        //TODO implement rename
    }

    @FXML
    public void deleteMenuItemOnAction(ActionEvent actionEvent) {
        TreeItem<ProjectFile> selectedItem = this.projectTreeView.getSelectionModel().getSelectedItem();
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,selectedItem.getValue().getName());
        confirmation.setHeaderText("是否删除? ");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!this.deleteRecursively(selectedItem.getValue())) {
                this.sendMessageDialog("删除失败", "未知原因");
            } else {
                this.initializeProject(this.applicationData.getRootPath());
            }
        }
    }


    //self resources

    private Stage primaryStage;

    private ApplicationData applicationData = new ApplicationData();


    void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * 根据root path初始化
     */
    void initializeProject(String path) {
        this.applicationData.setRootPath(path);
        ProjectFile directory = new ProjectFile(applicationData.getRootPath());
        TreeItem<ProjectFile> root = this.iterateFolder(directory);
        root.setExpanded(true);
        this.projectTreeView.setRoot(root);
        this.projectNameLabel.setText(directory.getName());
    }

    /**
     * 打开一个文件
     *
     * @param file 文件
     */
    private void openFile(ProjectFile file) {
        CodeEditor codeEditor = new CodeEditor();
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
        Tab newTab = new Tab(file.getName());
        newTab.setClosable(true);
        newTab.setContent(codeEditor);
        editTabPane.getTabs().add(newTab);
        editTabPane.getSelectionModel().select(newTab);
    }

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
            List<ProjectFile> files = path.listProjectFiles();
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

    /**
     * 获取当前所在的文件夹路径
     *
     * @return String
     */
    private TreeItem<ProjectFile> getCurrentFolderTreeItem() {
        TreeItem<ProjectFile> selectedItem = this.projectTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return projectTreeView.getRoot();
        }
        if (selectedItem.getValue().isFile()) {
            return selectedItem.getParent();
        }
        return selectedItem;
    }

    private boolean deleteRecursively(ProjectFile file) {
        if (file.isFile()) {
            return file.delete();
        } else {
            List<ProjectFile> fileList = file.listProjectFiles();
            for (ProjectFile f :
                    fileList) {
                if (!deleteRecursively(f)) {
                    return false;
                }
            }
            return file.delete();
        }
    }
}
