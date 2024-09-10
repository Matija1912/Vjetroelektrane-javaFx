package com.app.vjetroelektranejavafx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Credentials;
import production.database.DatabaseUtils;
import production.exceptions.NewCredentialsEmptyStringException;
import production.threads.DeserializeCurrentUserThread;
import production.threads.SerializeCurrentUserThread;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ResetCredentialsController {
    @FXML
    private TextField currentPasswordTextField;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private TextField currentUsernameTextField;
    @FXML
    private TextField newUsernameTextField;

    private static final Logger logger = LoggerFactory.getLogger(ResetCredentialsController.class);

    public void initialize(){
    }

    public void setNewPassword(){
        Optional<Credentials> currentCredentials = Optional.empty();
        DeserializeCurrentUserThread deserializeCurrentUserThread = new DeserializeCurrentUserThread();
        Executor executorDeserialization = Executors.newSingleThreadExecutor();
        executorDeserialization.execute(deserializeCurrentUserThread);
        currentCredentials = deserializeCurrentUserThread.deserializeCurrentUser();

        String currentPassword = currentPasswordTextField.getText();
        Optional<String> hashedCurrentPassword = DatabaseUtils.hashPassword(currentPassword);
        String newPassword = newPasswordTextField.getText();
        if(currentCredentials.isPresent() && !currentPassword.isEmpty() && hashedCurrentPassword.isPresent()){
            if(currentCredentials.get().getPassword().equals(hashedCurrentPassword.get())){
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation");
                confirmationAlert.setHeaderText("Confirm credentials update");
                confirmationAlert.setContentText("Are you sure you want to update your password?");

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        Optional<String> hashedNewPassword = DatabaseUtils.hashPassword(newPassword);
                        DatabaseUtils.updatePasswordById(currentCredentials.get().getId(), hashedNewPassword.get(), hashedCurrentPassword.get());
                        logger.info("Password changed");
                        //FileUtils.serializeCurrentUser(DatabaseUtils.getUserOrAdminById(currentCredentials.get().getId()).get());
                        SerializeCurrentUserThread serializeCurrentUserThread = new SerializeCurrentUserThread(DatabaseUtils.getUserOrAdminById(currentCredentials.get().getId()).get());
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(serializeCurrentUserThread);

                        showAlertInfo("Change password.", "Password successfully updated.");
                    }catch (NewCredentialsEmptyStringException ex){
                        logger.error(ex.getMessage());
                        showAlertInfo("Info", "Password not updated");
                    }
                }
            }else{
                logger.error("Password reset failed.");
                showAlertError("Error.", "Incorrect password.");
            }
        }
    }


    public void setNewUsername(){
        //Optional<Credentials> currentCredentials = FileUtils.deserializeCurrentUser();

        Optional<Credentials> currentCredentials = Optional.empty();
        DeserializeCurrentUserThread deserializeCurrentUserThread = new DeserializeCurrentUserThread();
        Executor executorDeserialization = Executors.newSingleThreadExecutor();
        executorDeserialization.execute(deserializeCurrentUserThread);
        currentCredentials = deserializeCurrentUserThread.deserializeCurrentUser();

        String currentUsername = currentUsernameTextField.getText();
        String newUsername = newUsernameTextField.getText();
        if(currentCredentials.isPresent() && !currentUsername.isEmpty()){
            if(currentCredentials.get().getUsername().equals(currentUsernameTextField.getText())){
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation");
                confirmationAlert.setHeaderText("Confirm credentials update");
                confirmationAlert.setContentText("Are you sure you want to update your username?");

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        DatabaseUtils.updateUsernameById(currentCredentials.get().getId(), newUsername, currentUsername);
                        //FileUtils.serializeCurrentUser(DatabaseUtils.getUserOrAdminById(currentCredentials.get().getId()).get());
                        SerializeCurrentUserThread serializeCurrentUserThread = new SerializeCurrentUserThread(DatabaseUtils.getUserOrAdminById(currentCredentials.get().getId()).get());
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(serializeCurrentUserThread);

                        showAlertInfo("change username", "Username successfully updated.");
                        logger.info("Username reset.");

                    }catch (NewCredentialsEmptyStringException ex){
                        logger.error(ex.getMessage());
                        showAlertInfo("Info", "Username not updated");
                    }
                }
            }else{
                logger.error("Username reset failed.");
                showAlertError("Error", "Incorrect username.");
            }
        }
    }

    private static void showAlertInfo(String update, String text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(update);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private static void showAlertError(String update, String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Info");
        alert.setHeaderText(update);
        alert.setContentText(text);
        alert.showAndWait();
    }

}
