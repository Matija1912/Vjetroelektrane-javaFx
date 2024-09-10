package production.WindTurbines;

import production.weather.Weather;

public interface PowerGenerator {
    Double calculatePower(Weather weather);
}
