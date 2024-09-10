package production.WindTurbines;

import production.weather.Weather;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Turbine implements Serializable, PowerGenerator {
    private String manufacturer;
    private Integer rotorDiameter;
    private Double generatorEfficiency;
    private Double cutInWindSpeed;
    private Integer id;

    private LocalDateTime installationTime;
    private Duration maintenanceInterval;

    public Turbine(String manufacturer, Integer id, Integer rotorDiameter, Double generatorEfficiency, Double cutInWindSpeed, Long maintenanceInterval) {
        this.installationTime = LocalDateTime.now();
        this.maintenanceInterval = Duration.ofDays(maintenanceInterval);
        this.manufacturer = manufacturer;
        this.id = id;
        this.rotorDiameter = rotorDiameter;
        this.generatorEfficiency = generatorEfficiency;
        this.cutInWindSpeed = cutInWindSpeed;
    }

    public LocalDateTime getInstallationTime() {
        return installationTime;
    }

    public Long getMaintenanceInterval() {
        return maintenanceInterval.toDays();
    }

    public LocalDateTime calculateNextMaintenanceDueDate(){
        return installationTime.plus(maintenanceInterval);
    }

//    public boolean isMaintenanceDue() {
//        LocalDateTime nextMaintenanceDueDate = calculateNextMaintenanceDueDate();
//        return LocalDateTime.now().isAfter(nextMaintenanceDueDate);
//    }

    public Double getCutInWindSpeed() {
        return cutInWindSpeed;
    }

    public void setCutInWindSpeed(Double cutInWindSpeed) {
        this.cutInWindSpeed = cutInWindSpeed;
    }

    public abstract Double calculatePowerCoefficient(Double windSpeed);
    public abstract Double calculateSweptArea();

    public Double getGeneratorEfficiency() {
        return generatorEfficiency;
    }

    public void setGeneratorEfficiency(Double generatorEfficiency) {
        this.generatorEfficiency = generatorEfficiency;
    }

    public Integer getRotorDiameter() {
        return rotorDiameter;
    }

    public void setRotorDiameter(Integer rotorDiameter) {
        this.rotorDiameter = rotorDiameter;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
