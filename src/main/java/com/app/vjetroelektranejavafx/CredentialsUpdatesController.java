package com.app.vjetroelektranejavafx;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CredentialsUpdatesController {
    @FXML
    private TableView<CredentialsUpdatesManager<Credentials>> credentialsUpdatesManagerTableView;
    @FXML
    private TableColumn<CredentialsUpdatesManager<Credentials>, String> updateDescription;
    @FXML
    private TableColumn<CredentialsUpdatesManager<Credentials>, String> user;
    @FXML
    private TableColumn<CredentialsUpdatesManager<Credentials>, String> oldNewValue;
    @FXML
    private TableColumn<CredentialsUpdatesManager<Credentials>, String> updateDateTime;

    public void initialize(){
        Optional<List<CredentialsUpdatesManager<Credentials>>> credentialsUpdatesList = FileUtils.deserializeUsersAndAdminsUpdates();
        if(credentialsUpdatesList.isPresent()){
            updateDescription.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getUpdateType());
                }
            });

            user.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getUser().getUsername() + " (" + param.getValue().getUser().getUserTypeString() + ")");
                }
            });

            oldNewValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String> param) {
                    if((!param.getValue().getOldValue().isEmpty()) && (!param.getValue().getNewValue().isEmpty())){
                        return new ReadOnlyStringWrapper(param.getValue().getOldValue() + " / " + param.getValue().getNewValue());
                    }else{
                        return new ReadOnlyStringWrapper("");
                    }
                }
            });

            updateDateTime.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<CredentialsUpdatesManager<Credentials>, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            });


            //set data
            ObservableList observableUpdateList = FXCollections.observableArrayList(credentialsUpdatesList.get());
            credentialsUpdatesManagerTableView.setItems(observableUpdateList);

        }
    }

}
