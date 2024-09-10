package production.weather;

import java.io.Serializable;

public class Weather implements Serializable {

    private Double temperature;
    private Double windSpeed;
    private Double airPressure;
    private Location location;
    private Double windOrientationInDegrees;

    private static final Integer specificGasConstantOfAir = 287;

    public Weather(Double temperature, Double windSpeed, Double airPressure, Location location, Double windOrientationInDegrees) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.airPressure = airPressure;
        this.location = location;
        this.windOrientationInDegrees = windOrientationInDegrees;
    }

    public Double getAirDensity(){
        Double rt = specificGasConstantOfAir * getAbsolutTemperature();
        return getAirPressure() / rt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getAbsolutTemperature(){
        return getTemperature() + 273.15;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public Double getAirPressure() {
        return airPressure;
    }

    public Location getLocation() {
        return location;
    }
    public Double getWindOrientationInDegrees() {
        return windOrientationInDegrees;
    }
}

