package production.weather;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.database.DatabaseUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WeatherApi {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApi.class);
    private Location location;
    private String urlString;
    public WeatherApi(Location location) {

        Optional<ApiData>optionalApi = DatabaseUtils.getApiData();
        if(optionalApi.isPresent()){
            this.location = location;
            String api_key = optionalApi.get().key();
            this.urlString = optionalApi.get().url() + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=" + api_key;
        }
    }

    public static Map<String, Object> jsonToMap(String string){
        Map<String, Object> map = new Gson().fromJson(
                string, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
        return map;
    }

    public Optional<Weather> getWeather(){
        Optional<Weather> weather = Optional.empty();
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(this.urlString);
            URLConnection connection = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while((line = rd.readLine()) != null){
                result.append(line);
            }
            rd.close();
            logger.info("Fetched data from weather api.");

            Map<String, Object> respMap = jsonToMap(result.toString());
            Map<String, Object> mainMap = jsonToMap(respMap.get("main").toString());
            Map<String, Object> windMap = jsonToMap(respMap.get("wind").toString());

            Double temp = Double.parseDouble(mainMap.get("temp").toString());
            Double pressure = Double.parseDouble(mainMap.get("pressure").toString()) * 100; //Convert hPa to Pa
            Double windSpreed = Double.parseDouble(windMap.get("speed").toString());
            Double windOrientation = Double.parseDouble(windMap.get("deg").toString());
            weather = Optional.of(new Weather(temp, windSpreed, pressure, getLocation(), windOrientation));

        }catch (IOException e){
            logger.error("City does not exist");
        }

        return weather;
    }

    public Location getLocation() {
        return location;
    }
}
