package com.app.vjetroelektranejavafx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import production.Users.Credentials;
import production.WindTurbines.Turbine;
import production.database.DatabaseUtils;
import production.weather.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LocationsController {
    @FXML
    private ComboBox<String> countryComboBox;
    @FXML
    private TableView<Location> locationsTableView;
    @FXML
    private TableColumn<Location, Integer> locationIdTableColumn;
    @FXML
    private TableColumn<Location, Double> locationLatitudeTableColumn;
    @FXML
    private TableColumn<Location, Double> locationLongitudeTableColumn;
    @FXML
    private TableColumn<Location, String> locationCountryTableColumn;
    @FXML
    private TableColumn<Location, String> locationCityTableColumn;

    public void initialize(){

        Optional<List<Location>>locationList = DatabaseUtils.getLocations();
        Set<String> countrySet = new HashSet<>();
        if(locationList.isPresent()){
            countrySet = locationList.get().stream()
                    .map(Location::getCountry)
                    .collect(Collectors.toSet());
        }
        ObservableList<String> observableManufacturerList = FXCollections.observableArrayList(countrySet);
        countryComboBox.setItems(observableManufacturerList);

        locationIdTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Location, Integer>, ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Location, Integer> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getId());
            }
        });

        locationLatitudeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Location, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Location, Double> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getLatitude());
            }
        });

        locationLongitudeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Location, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Location, Double> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getLongitude());
            }
        });

        locationCountryTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Location, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Location, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getCountry());
            }
        });

        locationCityTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Location, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Location, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getCity());
            }
        });
    }

    public void locationsSearch(){
        Optional<String> optionalSelectedCountry = Optional.ofNullable(countryComboBox.getValue());
        String selectedCountry = optionalSelectedCountry.orElse("");

        List<Location> locationList = DatabaseUtils.getLocationsByCountry(selectedCountry);
        ObservableList observableItemList = FXCollections.observableArrayList(locationList);
        locationsTableView.setItems(observableItemList);
    }
}
