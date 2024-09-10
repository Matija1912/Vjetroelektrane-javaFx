package com.app.vjetroelektranejavafx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.Duration;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.StaticHorizontalAxisTurbine;
import production.WindTurbines.Turbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.database.DatabaseUtils;
import production.files.FileUtils;
import production.threads.CalculateTotalOutputThread;
import production.weather.Weather;
import production.weather.WeatherApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnergyOutputController {
    @FXML
    private Label energyOutputLabel;
    @FXML
    private Label windSpeedLabel;
    @FXML
    private TableView<Turbine> turbineTableView;
    @FXML
    private TableColumn<Turbine, Integer> turbineIdTableColumn;
    @FXML
    private TableColumn<Turbine, Double> turbineOutputTableColumn;

    public void initialize(){
        List<Turbine> turbineList = new ArrayList<>();
        Optional<Credentials> currentUser = FileUtils.deserializeCurrentUser();
        if(currentUser.isPresent()){
            //get all turbines
            turbineList = DatabaseUtils.getTurbinesByManufacturerAndUser("", currentUser.get().getId());

            //thread
            CalculateTotalOutputThread.stop = false;
            CalculateTotalOutputThread calculateTotalOutputThread = new CalculateTotalOutputThread(turbineList, currentUser.get(), windSpeedLabel, energyOutputLabel, turbineOutputTableColumn, turbineTableView, turbineIdTableColumn);
            Thread starter = new Thread(calculateTotalOutputThread);
            starter.start();

        }

    }


    private void printCurrentTotalOutput(List<Turbine>turbineList, Credentials currentUser, Label windSpeedLabel, Label energyOutputLabel){
        turbineList = DatabaseUtils.getTurbinesByManufacturerAndUser("", currentUser.getId());

        User user = (User) currentUser;
        WeatherApi weatherApi = new WeatherApi(user.getLocation());
        Optional<Weather> currentWeather = weatherApi.getWeather();
        if(currentWeather.isPresent()){
            //Initialize current wind speed
            windSpeedLabel.setText(currentWeather.get().getWindSpeed().toString());
            //Initialize total output
            String formattedEnergyOutput = String.format("%.2f", getTotalOutput(turbineList, currentWeather.get()));
            energyOutputLabel.setText(formattedEnergyOutput + " kW");
        }
    }
    private Double getTotalOutput(List<Turbine>turbineList, Weather weather){
        Double totalOutput = 0.0;
        for(Turbine turbine : turbineList){
            if(turbine instanceof HorizontalAxisTurbine){
                HorizontalAxisTurbine hat = (HorizontalAxisTurbine) turbine;
                totalOutput += hat.calculatePower(weather);
            }else if(turbine instanceof StaticHorizontalAxisTurbine){
                StaticHorizontalAxisTurbine sat = (StaticHorizontalAxisTurbine) turbine;
                totalOutput += sat.calculatePower(weather);
            }else if(turbine instanceof VerticalAxisTurbine){
                VerticalAxisTurbine vat = (VerticalAxisTurbine) turbine;
                totalOutput += vat.calculatePower(weather);
            }
        }
        return totalOutput;
    }
}
