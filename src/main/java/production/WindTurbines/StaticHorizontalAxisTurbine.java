package production.WindTurbines;

import production.enums.TurbineType;
import production.weather.Weather;

import java.io.Serializable;

public final class StaticHorizontalAxisTurbine extends Turbine implements NonWindDirectionSensitive, Serializable {

    private static final double RATED_WIND_SPEED = 12.0;
    private static final double MAX_POWER_COEFFICIENT = 0.53;
    private Integer orientationInDegrees;
    public StaticHorizontalAxisTurbine(String manufacturer, Integer id, Integer rotorDiameter, Double generatorEfficiency, Double cutInWindSpeed, Long maintenanceInterval, Integer orientationInDegrees) {
        super(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
        this.orientationInDegrees = orientationInDegrees % 360;
    }

    public Integer getOrientationInDegrees() {
        return orientationInDegrees;
    }

    public void setOrientationInDegrees(Integer orientationInDegrees) {
        this.orientationInDegrees = orientationInDegrees;
    }

    @Override
    public Double windOrientationLossCoefficient(Double windOrientationInDegrees){
        Double angleBetweenWindAndTurbine = getOrientationInDegrees() - windOrientationInDegrees;
        Double efficiency = (1 + Math.cos(angleBetweenWindAndTurbine)) / 2; // +1 and /2 => normalization (range 0, 1)
        return efficiency;
    }

    @Override
    public Double calculatePowerCoefficient(Double windSpeed) {
        Double cutInWindSpeed = getCutInWindSpeed();

        if (windSpeed < cutInWindSpeed) {
            return 0.0;
        } else {
            double normalizedWindSpeed = (windSpeed - cutInWindSpeed) / (RATED_WIND_SPEED - cutInWindSpeed);
            return MAX_POWER_COEFFICIENT * normalizedWindSpeed;
        }
    }

    @Override
    public Double calculateSweptArea(){
        return Math.PI * Math.pow(getRotorDiameter() / 2, 2);
    }
    @Override
    public Double calculatePower(Weather weather){
        Double efficiencyWithAdjustedPowerCoefficient = calculatePowerCoefficient(weather.getWindSpeed()) * getGeneratorEfficiency();
        return 0.5 * weather.getAirDensity()  * calculateSweptArea() * Math.pow(weather.getWindSpeed(), 3) * efficiencyWithAdjustedPowerCoefficient * windOrientationLossCoefficient(weather.getWindOrientationInDegrees()) / 1000;
    }
}
