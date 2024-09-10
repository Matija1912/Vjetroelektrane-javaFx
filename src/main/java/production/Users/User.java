package production.Users;

import production.WindTurbines.Turbine;
import production.enums.UserType;
import production.weather.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class User extends Credentials implements Serializable {
    private Location location;



    public User(Integer id, String username, String password, Location location) {
        super(id, Credentials.USER, username, password);
        this.location = location;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
