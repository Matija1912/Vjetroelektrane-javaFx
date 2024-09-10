package com.app.vjetroelektranejavafx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.Turbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.database.DatabaseUtils;

import java.util.*;
import java.util.stream.Collectors;

public class TurbinesController {
    @FXML
    private ComboBox<String> manufacturerComboBox;
    @FXML
    private TableView<Turbine> turbinesTableView;
    @FXML
    private TableColumn<Turbine, Integer> turbineIdTableColumn;
    @FXML
    private TableColumn<Turbine, String> manufacturerTableColumn;
    @FXML
    private TableColumn<Turbine, String> turbineTypeTableColumn;
    @FXML
    private TableColumn<Turbine, Integer> rotorDiameterTableColumn;
    @FXML
    private TableColumn<Turbine, Double> generatorEfficiencyTableColumn;
    @FXML
    private TableColumn<Turbine, Double> cutInWindSpeedTableColumn;

    public void initialize(){
        Optional<List<Turbine>>turbineList = DatabaseUtils.getTurbines();
        Set<String> manufacturerSet = new HashSet<>();
        if(turbineList.isPresent()){
            manufacturerSet = turbineList.get().stream()
                    .map(Turbine::getManufacturer)
                    .collect(Collectors.toSet());
        }
        ObservableList<String> observableManufacturerList = FXCollections.observableArrayList(manufacturerSet);
        manufacturerComboBox.setItems(observableManufacturerList);

        turbineIdTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Integer>, ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Turbine, Integer> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getId());
            }
        });

        manufacturerTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Turbine, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getManufacturer());
            }
        });

        turbineTypeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Turbine, String> param) {
                if(param.getValue() instanceof HorizontalAxisTurbine){
                    return new ReadOnlyStringWrapper("HAT");
                }else if(param.getValue() instanceof VerticalAxisTurbine){
                    return new ReadOnlyStringWrapper("VAT");
                }else{
                    return new ReadOnlyStringWrapper("SAT");
                }
            }
        });

        rotorDiameterTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Integer>, ObservableValue<Integer>>() {
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Turbine, Integer> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getRotorDiameter());
            }
        });

        generatorEfficiencyTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Turbine, Double> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getGeneratorEfficiency());
            }
        });

        cutInWindSpeedTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Turbine, Double> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getCutInWindSpeed());
            }
        });
    }
    public void turbinesSearch(){
        Optional<String> optionalSelectedManufacturer = Optional.ofNullable(manufacturerComboBox.getValue());
        String selectedManufacturer = optionalSelectedManufacturer.orElse("");

        List<Turbine> turbineList = DatabaseUtils.getTurbinesByManufacturer(selectedManufacturer);
        ObservableList observableTurbineList = FXCollections.observableArrayList(turbineList);
        turbinesTableView.setItems(observableTurbineList);


    }
}
