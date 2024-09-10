package com.app.vjetroelektranejavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.Turbine;
import production.WindTurbines.TurbineEntity;
import production.database.DatabaseUtils;
import production.files.FileUtils;
import production.weather.Location;

import java.util.*;
import java.util.stream.Collectors;

public class AddNewUserController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField locationLatitude;
    @FXML
    private TextField locationLongitude;
    @FXML
    private TextField locationCountry;
    @FXML
    private TextField locationCity;
    @FXML
    private ListView<String> turbineListView;

    List<Turbine> turbineList = DatabaseUtils.getTurbines().get();

    List<Turbine> selectedTurbineList = new ArrayList<>();

    public void initialize(){

        List<String> turbineNameList = turbineList.stream()
                .map(e -> new TurbineEntity(e.getId(), e.getManufacturer()).getFullName())
                .toList();

        ObservableList observableTurbineList = FXCollections.observableArrayList(turbineNameList);
        turbineListView.setItems(observableTurbineList);

        turbineListView.setOnMouseClicked(e -> {
            String turbineIdAndManufacturer = turbineListView.getSelectionModel().getSelectedItem();
            int index = turbineIdAndManufacturer.indexOf(":");
            Integer turbineId = Integer.valueOf(turbineIdAndManufacturer.substring(0, index));

            Turbine selectedTurbine = turbineList.stream().filter(i -> i.getId().equals(turbineId))
                    .findFirst().get();
            selectedTurbineList.add(selectedTurbine);
        });

    }

    public void saveNewUser()throws NumberFormatException {
        try {
            Integer userId = DatabaseUtils.getNextId("CREDENTIALS");
            Integer locationId = DatabaseUtils.getNextId("LOCATION");
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            String stringLat = locationLatitude.getText();
            String stringLon = locationLongitude.getText();
            String country = locationCountry.getText();
            String city = locationCity.getText();
            if(!username.isEmpty() && !password.isEmpty() && !stringLat.isEmpty() && !stringLon.isEmpty() && !country.isEmpty() && !city.isEmpty()){
                Double latitude = Double.parseDouble(stringLat);
                Double longitude = Double.parseDouble(stringLon);
                Location newLocation = new Location(locationId, latitude, longitude, country, city);
                Optional<String> hashedPassword = DatabaseUtils.hashPassword(password);
                if(hashedPassword.isPresent()){
                    User newUser = new User(userId, username, hashedPassword.get(), newLocation);
                    Optional<List<Turbine>>optionalTurbineList = Optional.of(selectedTurbineList);
                    if(selectedTurbineList.isEmpty()){
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("warning");
                        alert.setHeaderText("No turbines selected");
                        alert.setContentText("Please select turbines.");

                        // Show the alert and wait for the user to close it
                        alert.showAndWait();
                    }else{
                        Alert confiramtionAlert = confirmation();
                        Optional<ButtonType> result = confiramtionAlert.showAndWait();
                        boolean checkIfUsernameExists = DatabaseUtils.checkIfUsernameExists(newUser.getUsername());
                        if(!checkIfUsernameExists){
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                DatabaseUtils.addNewUserOrAdmin(newUser, optionalTurbineList);
                                DatabaseUtils.addNewLocation(newLocation);
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Info");
                                alert.setHeaderText("new user");
                                alert.setContentText("Added new user.");

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
        }catch (NumberFormatException ex){
            System.out.println("pogresan unos!");
        }
    }

    public Alert confirmation(){
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Confirm New user");
        confirmationAlert.setContentText("Are you sure you want to add this new user?");
        return confirmationAlert;
    }
}
