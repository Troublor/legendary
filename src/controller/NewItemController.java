package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class NewItemController {
    //controls
    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    public void createButtonOnAction(ActionEvent actionEvent) {
        String fileName = nameTextField.getText();
        if (!fileName.toLowerCase().endsWith(this.fileType)) {
            fileName = fileType + this.fileType;
        }
        File file = new File(currPath + File.separator + fileName);
        if (file.exists()) {
            this.sendMessageDialog("创建失败", "存在同名文件");
        } else {
            if (file.mkdirs()) {
                try {
                    if (file.createNewFile()) {
                        primaryStage.close();
                    }
                } catch (IOException e) {
                    this.sendMessageDialog("创建失败", "未知错误");
                }
            }
            this.sendMessageDialog("创建失败", "未知错误");
        }
    }

    @FXML
    public void cancelButtonOnAction(ActionEvent actionEvent) {
        primaryStage.close();
    }

    //self
    private Stage primaryStage;

    private String currPath;

    private String fileType;

    public String getFileName() {
        return this.nameTextField.getText().toLowerCase().endsWith(this.fileType)
                ? this.nameTextField.getText() : this.nameTextField.getText() + this.fileType;
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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
