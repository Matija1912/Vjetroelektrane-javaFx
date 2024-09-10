package com.app.vjetroelektranejavafx;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.User;
import production.database.DatabaseUtils;
import production.exceptions.UserNotFoundException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import production.Users.Credentials;
import production.Users.LoginRecord;
import production.enums.UserType;
import production.files.FileUtils;
import production.threads.GetCurrentUserThread;
import production.threads.SerializeCurrentUserThread;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginPageController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    private static final Logger logger = LoggerFactory.getLogger(LoginPageController.class);
    public void showUser(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("userView.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("Hello user");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void showAdmin(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("adminView.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("Hello admin");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void getUser(){
        Optional<String> hashedPassword = DatabaseUtils.hashPassword(passwordTextField.getText());
        if(hashedPassword.isPresent()){
            LoginRecord lr = new LoginRecord(usernameTextField.getText(), hashedPassword.get());
            try {
                Optional<Credentials>credentialsoptional = DatabaseUtils.checkForCredentials(lr);
                if(credentialsoptional.isPresent()){

                    SerializeCurrentUserThread serializeCurrentUserThread = new SerializeCurrentUserThread(credentialsoptional.get());
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(serializeCurrentUserThread);

                    if(UserType.USER.getValue().equals(credentialsoptional.get().getUserType())){
                        //FileUtils.serializeCurrentUser((Credentials) credentialsoptional.get());
                        showUser();
                    }
                    if(UserType.ADMIN.getValue().equals(credentialsoptional.get().getUserType())){
                        //FileUtils.serializeCurrentUser((Credentials) credentialsoptional.get());
                        showAdmin();
                    }
                }
            }catch (UserNotFoundException ex){
                logger.error("No such user.");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText("Error");
                alert.setContentText(ex.getMessage());

                alert.showAndWait();
            }

        }
    }


}