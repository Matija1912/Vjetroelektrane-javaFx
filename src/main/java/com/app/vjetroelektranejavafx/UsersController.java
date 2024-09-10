package com.app.vjetroelektranejavafx;

import javafx.application.Platform;
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
import production.Users.User;
import production.database.DatabaseUtils;
import production.enums.UserType;
import production.files.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsersController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private ComboBox<String> userTypeComboBox;
    @FXML
    private TableView<Credentials> usersTableView;

    @FXML
    private TableColumn<Credentials, String> usernameTableColumn;
    @FXML
    private TableColumn<Credentials, String> passwordTableColumn;
    @FXML
    private TableColumn<Credentials, String> userTypeTableColumn;

    public void initialize(){

        ObservableList<String> observableCategoryNames = FXCollections.observableArrayList(List.of("User", "Admin"));
        userTypeComboBox.setItems(observableCategoryNames);

        usernameTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Credentials, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Credentials, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getUsername());
            }
        });

        passwordTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Credentials, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Credentials, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getPassword());
            }
        });

        userTypeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Credentials, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Credentials, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getUserTypeString());
            }
        });

    }

    public void usersSearch(){
        Optional<String> optionalSelectedUserType = Optional.ofNullable(userTypeComboBox.getValue());
        String selectedUsertype = optionalSelectedUserType.orElse("");
        String inputUsername = usernameTextField.getText();

        List<Credentials> credentialsList = DatabaseUtils.getUsersAndAdminsByFilter(inputUsername, selectedUsertype);
        ObservableList observableItemList = FXCollections.observableArrayList(credentialsList);
        usersTableView.setItems(observableItemList);


    }

}
