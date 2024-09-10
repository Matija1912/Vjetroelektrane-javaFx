package com.app.vjetroelektranejavafx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import production.Users.Credentials;
import production.files.FileUtils;
import production.generics.CredentialsUpdatesManager;
import production.generics.UpdatesManager;
import production.weather.Location;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class LocationsUpdatesController {

    private static final String LOCATIONS_UPDATES_FILE_NAME = "dat/serialization/locationUpdates.dat";

    @FXML
    private TableView<UpdatesManager<Credentials, Location>> locationsUpdatesManagerTableView;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, String> updateDescription;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, String> country;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, String> city;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, Double> latitude;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, Double> longitude;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Location>, String> updateDate;

    public void initialize(){
        Optional<List<UpdatesManager<Credentials, Location>>> locationUpdatesList = FileUtils.deserializeLocationAndTurbineUpdates(LOCATIONS_UPDATES_FILE_NAME);
        if(locationUpdatesList.isPresent()){
            updateDescription.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getUpdateType() + " by: " + param.getValue().getUser().getUserTypeString());
                }
            });

            country.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getAddedElement().getCountry());
                }
            });

            city.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getAddedElement().getCity());
                }
            });

            latitude.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, Double>, ObservableValue<Double>>() {
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, Double> param) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().getAddedElement().getLatitude());
                }
            });

            longitude.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, Double>, ObservableValue<Double>>() {
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, Double> param) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().getAddedElement().getLongitude());
                }
            });

            updateDate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Location>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            });

            //set data
            ObservableList observableUpdateList = FXCollections.observableArrayList(locationUpdatesList.get());
            locationsUpdatesManagerTableView.setItems(observableUpdateList);

        }
    }
}
