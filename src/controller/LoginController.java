package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

import client.ChatClient;

public class LoginController {
	@FXML
	private TextField userNameInput;
	@FXML
	private Label errorLabel;
	
	@FXML
    private Button loginButton;
	
	@FXML
	void handleLogin(ActionEvent e) {
		String userName = userNameInput.getText().trim();
		if(userName.isEmpty()) {
			errorLabel.setText("Username tidak boleh kosong");
			return;
		}
		
		try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/main-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            
            // connect ke server
            ChatClient client = new ChatClient("localhost", 1234, mainController);
            client.login(userName);
            
            mainController.setClient(client, userName);
            
            // load main view
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
            
		} catch(IOException err) {
			errorLabel.setText("Gagal login");
			err.printStackTrace();
		}
	}
}
