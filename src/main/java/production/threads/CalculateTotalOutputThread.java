package production.threads;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import production.Users.Credentials;
import production.Users.User;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.StaticHorizontalAxisTurbine;
import production.WindTurbines.Turbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.database.DatabaseUtils;
import production.weather.Weather;
import production.weather.WeatherApi;

import java.util.List;
import java.util.Optional;

public class CalculateTotalOutputThread implements Runnable{
    private List<Turbine>turbineList;
    private Credentials currentUser;
    private Label windSpeedLabel;
    private Label energyOutputLabel;
    private TableView<Turbine> turbineTableView;
    private TableColumn<Turbine, Integer> turbineIdTableColumn;
    private TableColumn<Turbine, Double> turbineOutputTableColumn;

    public static Boolean stop = false;

    public CalculateTotalOutputThread(List<Turbine> turbineList, Credentials currentUser, Label windSpeedLabel, Label energyOutputLabel, TableColumn<Turbine, Double> turbineOutputTableColumn, TableView<Turbine> turbineTableView, TableColumn<Turbine, Integer> turbineIdTableColumn) {
        this.turbineList = turbineList;
        this.currentUser = currentUser;
        this.windSpeedLabel = windSpeedLabel;
        this.energyOutputLabel = energyOutputLabel;
        this.turbineOutputTableColumn = turbineOutputTableColumn;
        this.turbineTableView = turbineTableView;
        this.turbineIdTableColumn = turbineIdTableColumn;
    }

    @Override
    public void run() {

        while (true){
            User user = (User) currentUser;
            WeatherApi weatherApi = new WeatherApi(user.getLocation());
            Optional<Weather> currentWeather = weatherApi.getWeather();
            if(currentWeather.isPresent()){
                if(stop){
                    return;
                }
                Platform.runLater(() -> {
                    ObservableList observableTurbineList = FXCollections.observableArrayList(turbineList);
                    turbineTableView.setItems(observableTurbineList);

                    turbineIdTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Integer>, ObservableValue<Integer>>() {
                        public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Turbine, Integer> param) {
                            return new ReadOnlyObjectWrapper<>(param.getValue().getId());
                        }
                    });
                        turbineOutputTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Turbine, Double>, ObservableValue<Double>>() {
                            public ObservableValue<Double> call(TableColumn.CellDataFeatures<Turbine, Double> param) {
                                if((param.getValue() instanceof HorizontalAxisTurbine) && currentWeather.isPresent()){
                                    HorizontalAxisTurbine hat = (HorizontalAxisTurbine) param.getValue();
                                    return new ReadOnlyObjectWrapper<>(hat.calculatePower(currentWeather.get()));
                                }else if((param.getValue() instanceof StaticHorizontalAxisTurbine) && currentWeather.isPresent()){
                                    StaticHorizontalAxisTurbine sat = (StaticHorizontalAxisTurbine) param.getValue();
                                    return new ReadOnlyObjectWrapper<>(sat.calculatePower(currentWeather.get()));
                                }else{
                                    VerticalAxisTurbine vat = (VerticalAxisTurbine) param.getValue();
                                    return new ReadOnlyObjectWrapper<>(vat.calculatePower(currentWeather.get()));
                                }
                            }
                        });

                    //Initialize current wind speed
                    windSpeedLabel.setText(currentWeather.get().getWindSpeed().toString());
                    //Initialize total output
                    String formattedEnergyOutput = String.format("%.2f", getTotalOutput(turbineList, currentWeather.get()));
                    energyOutputLabel.setText(formattedEnergyOutput + " kW");
                });
            }
            try {
                Thread.sleep(1000);
                System.out.println("Current output refresh");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
