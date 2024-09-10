package production.WindTurbines;

import production.enums.TurbineType;
import production.weather.Weather;

import java.io.Serializable;
import java.time.Duration;

public class HorizontalAxisTurbine extends Turbine implements Serializable {

    private static final double RATED_WIND_SPEED = 12.0;
    private static final double MAX_POWER_COEFFICIENT = 0.53;

    public HorizontalAxisTurbine(String manufacturer, Integer id, Integer rotorDiameter, Double generatorEfficiency, Double cutInWindSpeed, Long maintenanceInterval) {
        super(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
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
        return 0.5 * weather.getAirDensity()  * calculateSweptArea() * Math.pow(weather.getWindSpeed(), 3) * efficiencyWithAdjustedPowerCoefficient / 1000;
    }
}
