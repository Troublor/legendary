package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ProjectFile;

import java.io.File;
import java.io.IOException;

public class NewItemController extends Controller {
    //controls
    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    public void createButtonOnAction(ActionEvent actionEvent) {
        if (this.mode == Mode.FILE) {
            String fileName = this.nameTextField.getText();
            if (!fileName.toLowerCase().endsWith(this.fileType)) {
                fileName = fileName + this.fileType;
            }
            ProjectFile file = new ProjectFile(this.currPath + ProjectFile.separator + fileName);
            if (file.exists()) {
                this.sendMessageDialog("创建失败", "存在同名文件");
                this.success = false;
            } else {
                try {
                    if (file.createNewFile()) {
                        this.success = true;
                        this.primaryStage.close();
                    }
                } catch (IOException e) {
                    this.sendMessageDialog("创建失败", "未知错误");
                    this.success = false;
                }
            }
        } else if (this.mode == Mode.FOLDER) {
            String folderName = this.nameTextField.getText();
            ProjectFile folder = new ProjectFile(this.currPath + ProjectFile.separator + folderName);
            if (folder.exists()) {
                this.sendMessageDialog("创建失败", "存在同名文件夹");
                this.success = false;
            } else {
                if (!folder.mkdirs()) {
                    this.sendMessageDialog("创建失败", "未知错误");
                    this.success = false;
                } else {
                    this.success = true;
                    this.primaryStage.close();
                }
            }
        }

    }

    @FXML
    public void cancelButtonOnAction(ActionEvent actionEvent) {
        primaryStage.close();
    }

    //self
    private String currPath;

    private String fileType = "";

    private Mode mode;

    private boolean success = false;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFileName() {
        return this.nameTextField.getText().toLowerCase().endsWith(this.fileType)
                ? this.nameTextField.getText() : this.nameTextField.getText() + this.fileType;
    }

    public String getFilePathWithName() {
        return this.currPath + File.separator + this.getFileName();
    }

    public void setFileType(String fileType) {
        this.fileType = fileType.toLowerCase();
    }

    public void setCurrPath(String currPath) {
        this.currPath = currPath;
    }

    public void setTitleLabel(String title) {
        titleLabel.setText(title);
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

    public enum Mode {
        FILE, FOLDER
    }

}
