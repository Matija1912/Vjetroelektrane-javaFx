package com.app.vjetroelektranejavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Admin;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.Turbine;
import production.WindTurbines.TurbineEntity;
import production.database.DatabaseUtils;
import production.files.FileUtils;
import production.weather.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeleteCredentialsController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
    @FXML
    private ComboBox<String> credentialsComboBox;
    List<Credentials>crerdentialsList = DatabaseUtils.getUsers(FileUtils.deserializeCurrentUser());

    Optional<String> selectedCredentialsOptional = Optional.empty();



    public void initialize(){
        List<String> credentialUsernames = crerdentialsList.stream()
                .map(e -> e.getId() + ":" + e.getUsername())
                .toList();

        ObservableList observableUsername = FXCollections.observableArrayList(credentialUsernames);
        credentialsComboBox.setItems(observableUsername);

        credentialsComboBox.setOnAction(event -> {
            selectedCredentialsOptional = Optional.of(credentialsComboBox.getSelectionModel().getSelectedItem());
            System.out.println(selectedCredentialsOptional.get());
        });
    }


    public void deleteUser(){
        if(selectedCredentialsOptional.isPresent()){
            String[] parts = selectedCredentialsOptional.get().split(":");
            int id = Integer.parseInt(parts[0]);
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Confirm deletion");
            confirmationAlert.setContentText("Are you sure you want to delete this user?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DatabaseUtils.deleteUserById(id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText("Deleting credentials");
                alert.setContentText("User deleted");

                alert.showAndWait();
            }

            //reload the page
            FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("deleteCredentials.fxml"));
            try {
                Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                VjetroelektraneApp.getMainStage().setTitle("delete admin");
                VjetroelektraneApp.getMainStage().setScene(scene);
                VjetroelektraneApp.getMainStage().show();
            }catch (IOException e){
                logger.error("Delete user page load failed.");
                throw new RuntimeException(e);
            }

        }
    }
}
