package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public abstract class DialogController extends Controller {
    protected boolean isConfirmed;

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void confirmButtonOnAction(ActionEvent actionEvent) {
        this.isConfirmed = true;
        super.primaryStage.close();
    }

    public void cancelButtonOnAction(ActionEvent actionEvent) {
        this.isConfirmed = false;
        super.primaryStage.close();
    }

    /**
     * 弹出提示框
     *
     * @param title 标题
     * @param msg   信息
     */
    protected void sendMessageDialog(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); //设置标题，不设置默认标题为本地语言的information
        alert.setHeaderText(msg); //设置头标题，默认标题为本地语言的information
        alert.showAndWait(); //显示弹窗，同时后续代码等挂起
    }
}
