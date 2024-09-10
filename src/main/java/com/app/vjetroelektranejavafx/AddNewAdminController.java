package com.app.vjetroelektranejavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import production.Users.Admin;
import production.Users.User;
import production.WindTurbines.Turbine;
import production.WindTurbines.TurbineEntity;
import production.database.DatabaseUtils;
import production.files.FileUtils;
import production.weather.Location;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class AddNewAdminController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField roleTextField;

    public void saveNewAdmin(){
        Integer userId = DatabaseUtils.getNextId("CREDENTIALS");
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String role = roleTextField.getText();
        if(!username.isEmpty() && !password.isEmpty() && !role.isEmpty()){
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Confirm New Admin");
            confirmationAlert.setContentText("Are you sure you want to add this new admin?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Optional<String> hashedPassword = DatabaseUtils.hashPassword(password);
                boolean checkIfUsernameExists = DatabaseUtils.checkIfUsernameExists(username);
                if(!checkIfUsernameExists){
                    if(hashedPassword.isPresent()){
                        Admin newAdmin = new Admin(userId, username, hashedPassword.get(), role);
                        DatabaseUtils.addNewUserOrAdmin(newAdmin, Optional.empty());

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Info");
                        alert.setHeaderText("new admin");
                        alert.setContentText("Added new admin.");

                        alert.showAndWait();
                    }
                }else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Info");
                    alert.setHeaderText("new user");
                    alert.setContentText("Username already exists!");

                    alert.showAndWait();
                }

            }
        }
    }
}
