package com.app.vjetroelektranejavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.Turbine;
import production.database.DatabaseUtils;
import production.enums.UserType;
import production.files.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AddTurbinesToUserController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @FXML
    private ComboBox<String> turbinesComboBox;
    List<Turbine> turbinesList = DatabaseUtils.getTurbines().get();

    Optional<String> selectedTurbineOptional = Optional.empty();



    public void initialize(){
        List<String> turbineStrings = turbinesList.stream()
                .map(e -> e.getId() + ":" + e.getManufacturer())
                .toList();

        ObservableList observableUsername = FXCollections.observableArrayList(turbineStrings);
        turbinesComboBox.setItems(observableUsername);

        turbinesComboBox.setOnAction(event -> {
            selectedTurbineOptional = Optional.of(turbinesComboBox.getSelectionModel().getSelectedItem());
            System.out.println(selectedTurbineOptional.get());
        });

    }


    public void addTurbine(){
        if(selectedTurbineOptional.isPresent()){
            String[] parts = selectedTurbineOptional.get().split(":");
            int id = Integer.parseInt(parts[0]);
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Confirm");
            confirmationAlert.setContentText("Are you sure you want to add this turbine?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                //add turbine
                if(FileUtils.deserializeCurrentUser().isPresent()){
                    Credentials currentCredentials = FileUtils.deserializeCurrentUser().get();
                    if(UserType.USER.getValue().equals(currentCredentials.getUserType())){
                        User currentUser = (User) currentCredentials;
                        DatabaseUtils.addNewTurbineToUser(id, currentUser);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Info");
                        alert.setHeaderText("Adding turbine");
                        alert.setContentText("Turbine added");

                        alert.showAndWait();
                    }
                }
            }

            //Clear the page by reloading it
            FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("addUserTurbines.fxml"));
            try {
                Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                VjetroelektraneApp.getMainStage().setTitle("Add turbine");
                VjetroelektraneApp.getMainStage().setScene(scene);
                VjetroelektraneApp.getMainStage().show();
            }catch (IOException e){
                logger.error("Add turbine page load failed.");
                throw new RuntimeException(e);
            }


        }
    }
}
