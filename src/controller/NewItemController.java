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

public class NewItemController extends DialogController {
    //controls
    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
        if (this.mode == Mode.FILE) {
            String fileName = this.nameTextField.getText();
            if (!fileName.toLowerCase().endsWith(this.fileType)) {
                fileName = fileName + this.fileType;
            }
            ProjectFile file = new ProjectFile(this.currPath + ProjectFile.separator + fileName);
            if (file.exists()) {
                this.sendMessageDialog("创建失败", "存在同名文件");
            } else {
                try {
                    if (file.createNewFile()) {
                        super.confirmButtonOnAction(actionEvent);
                    }
                } catch (IOException e) {
                    this.sendMessageDialog("创建失败", "未知错误");
                }
            }
        } else if (this.mode == Mode.FOLDER) {
            String folderName = this.nameTextField.getText();
            ProjectFile folder = new ProjectFile(this.currPath + ProjectFile.separator + folderName);
            if (folder.exists()) {
                this.sendMessageDialog("创建失败", "存在同名文件夹");
            } else {
                if (!folder.mkdirs()) {
                    this.sendMessageDialog("创建失败", "未知错误");
                } else {
                    super.confirmButtonOnAction(actionEvent);
                }
            }
        }

    }

    @FXML
    public void cancelButtonOnAction(ActionEvent actionEvent) {
        super.cancelButtonOnAction(actionEvent);
    }

    //self
    private String currPath;

    private String fileType = "";

    private Mode mode;

    public void setMode(Mode mode) {
        this.mode = mode;
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

    public enum Mode {
        FILE, FOLDER
    }

}
