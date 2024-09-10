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
import production.WindTurbines.Turbine;
import production.files.FileUtils;
import production.generics.CredentialsUpdatesManager;
import production.generics.UpdatesManager;
import production.weather.Location;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TurbinesUpdatesController {

    private static final String TURBINES_UPDATES_FILE_NAME = "dat/serialization/turbinesUpdates.dat";
    @FXML
    private TableView<UpdatesManager<Credentials, Turbine>> updatesManagerTableView;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Turbine>, String> updateDescription;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Turbine>, Integer> turbineId;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Turbine>, String> turbineManufacturer;
    @FXML
    private TableColumn<UpdatesManager<Credentials, Turbine>, String> updateDate;

    public void initialize(){
        Optional<List<UpdatesManager<Credentials, Turbine>>> turbineUpdatesList = FileUtils.deserializeLocationAndTurbineUpdates(TURBINES_UPDATES_FILE_NAME);
        if(turbineUpdatesList.isPresent()){
            updateDescription.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getUpdateType() + " by user: " + param.getValue().getUser().getUsername());
                }
            });

            turbineId.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, Integer>, ObservableValue<Integer>>() {
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, Integer> param) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().getAddedElement().getId());
                }
            });

            turbineManufacturer.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getAddedElement().getManufacturer());
                }
            });

            updateDate.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<UpdatesManager<Credentials, Turbine>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            });

            //set data
            ObservableList observableUpdateList = FXCollections.observableArrayList(turbineUpdatesList.get());
            updatesManagerTableView.setItems(observableUpdateList);

        }
    }
}
