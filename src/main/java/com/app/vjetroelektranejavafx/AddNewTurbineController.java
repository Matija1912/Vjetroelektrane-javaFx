package com.app.vjetroelektranejavafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.StaticHorizontalAxisTurbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.database.DatabaseUtils;
import production.enums.TurbineType;
import production.files.FileUtils;

import java.util.Optional;

public class AddNewTurbineController {
    @FXML
    private TextField manufacturerTextField;
    @FXML
    private TextField rotorDiameterTextField;
    @FXML
    private TextField generatorEfficiencyTextField;
    @FXML
    private TextField cutInWindSpeedTextField;
    @FXML
    private TextField maintenanceIntervalTextField;
    @FXML
    private RadioButton horizontalAxisRadioButton;
    @FXML
    private RadioButton verticalAxisRadioButton;
    @FXML
    private RadioButton staticHorizontalAxisRadioButton;
    @FXML
    private Label orientationLabel;
    @FXML
    private TextField orientationInDegreesTextField;
    @FXML
    private Label bladeHeightLabel;
    @FXML
    private TextField bladeHeightTextField;
    Optional<Integer>turbineType = Optional.empty();

    public void initialize() {

        //visibility
        orientationLabel.setVisible(false);
        orientationInDegreesTextField.setVisible(false);
        bladeHeightLabel.setVisible(false);
        bladeHeightTextField.setVisible(false);

        //radio button toggle group
        ToggleGroup toggleGroup = new ToggleGroup();
        horizontalAxisRadioButton.setToggleGroup(toggleGroup);
        staticHorizontalAxisRadioButton.setToggleGroup(toggleGroup);
        verticalAxisRadioButton.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            orientationLabel.setVisible(false);
            orientationInDegreesTextField.setVisible(false);
            bladeHeightLabel.setVisible(false);
            bladeHeightTextField.setVisible(false);

            if (newValue == horizontalAxisRadioButton) {
                turbineType = Optional.of(TurbineType.HORIZONTAL_AXIS.getValue());
            } else if (newValue == staticHorizontalAxisRadioButton) {
                turbineType = Optional.of(TurbineType.STATIC_HORIZONTAL_AXIS.getValue());
                orientationLabel.setVisible(true);
                orientationInDegreesTextField.setVisible(true);
            } else if (newValue == verticalAxisRadioButton) {
                turbineType = Optional.of(TurbineType.VERTICAL_AXIS.getValue());
                bladeHeightLabel.setVisible(true);
                bladeHeightTextField.setVisible(true);
            }
        });

    }

    private void showAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("new turbine");
        alert.setContentText("Added new turbine.");

        // Show the alert and wait for the user to close it
        alert.showAndWait();
    }

    public void saveNewTurbine()throws NumberFormatException{
        try {
            Integer turbineId = DatabaseUtils.getNextId("TURBINES");
            String manufacturer = manufacturerTextField.getText();
            String rotorDiameterString = rotorDiameterTextField.getText();
            String generatorEfficiencyString = generatorEfficiencyTextField.getText();
            String cutInWindSpeedString = cutInWindSpeedTextField.getText();
            String maintenanceIntervalString = maintenanceIntervalTextField.getText();

            if(turbineType.isPresent() && !manufacturer.isEmpty() && !rotorDiameterString.isEmpty() && !generatorEfficiencyString.isEmpty() && !cutInWindSpeedString.isEmpty() && !maintenanceIntervalString.isEmpty()){
                Integer rotorDiameter = Integer.parseInt(rotorDiameterString);
                Double generatorEfficiency = Double.parseDouble(generatorEfficiencyString);
                Double cutInWindSpeed = Double.parseDouble(cutInWindSpeedString);
                Long maintenanceInterval = Long.parseLong(maintenanceIntervalString);
                if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType.get())){
                    String orientationInDegreesString = orientationInDegreesTextField.getText();
                    Optional<Credentials> credentials = FileUtils.deserializeCurrentUser();
                    if(credentials.isPresent() && !orientationInDegreesString.isEmpty()){
                        Alert confiramtionAlert = confirmation();
                        Optional<ButtonType> result = confiramtionAlert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            Integer orientationIdDegrees = Integer.parseInt(orientationInDegreesString);
                            StaticHorizontalAxisTurbine shat = new StaticHorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationIdDegrees);
                            DatabaseUtils.addNewTurbine(shat);
                            showAlert();
                        }
                    }
                }else if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType.get())){
                    Optional<Credentials> credentials = FileUtils.deserializeCurrentUser();
                    if(credentials.isPresent()){
                        Alert confiramtionAlert = confirmation();
                        Optional<ButtonType> result = confiramtionAlert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                            DatabaseUtils.addNewTurbine(hat);
                            showAlert();
                        }
                    }
                }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType.get())){
                    String bladeHeightString = bladeHeightTextField.getText();
                    Optional<Credentials> credentials = FileUtils.deserializeCurrentUser();
                    if(credentials.isPresent() && !bladeHeightString.isEmpty()){
                        Alert confiramtionAlert = confirmation();
                        Optional<ButtonType> result = confiramtionAlert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            Integer bladeHeight = Integer.parseInt(bladeHeightString);
                            VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                            DatabaseUtils.addNewTurbine(vat);
                            showAlert();
                        }
                    }
                }
            }
        }catch (NumberFormatException ex){
            System.out.println("Pogresan unos");
        }

    }

    public Alert confirmation(){
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Confirm New Turbine");
        confirmationAlert.setContentText("Are you sure you want to add this new Turbine?");
        return confirmationAlert;
    }

}
