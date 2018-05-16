package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RenameController extends DialogController{
    //controls
    @FXML
    private TextField nameTextField;

    @FXML
    public void confirmButtonOnAction(ActionEvent actionEvent) {
        String name = this.nameTextField.getText();
        if (name.isEmpty()) {
            super.sendMessageDialog("错误", "名称不能为空");
            return;
        }
        super.confirmButtonOnAction(actionEvent);
    }

    //self
    public void setName(String formerName) {
        this.nameTextField.setText(formerName);
    }

    public String getName() {
        return this.nameTextField.getText();
    }
}
