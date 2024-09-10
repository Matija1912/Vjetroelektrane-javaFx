package production.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.Users.Admin;
import production.Users.Credentials;
import production.Users.LoginRecord;
import production.Users.User;
import production.WindTurbines.HorizontalAxisTurbine;
import production.WindTurbines.StaticHorizontalAxisTurbine;
import production.WindTurbines.Turbine;
import production.WindTurbines.VerticalAxisTurbine;
import production.enums.TurbineType;
import production.enums.UserType;
import production.exceptions.DatabaseConnectionException;
import production.exceptions.NewCredentialsEmptyStringException;
import production.exceptions.UserNotFoundException;
import production.files.FileUtils;
import production.generics.CredentialsUpdatesManager;
import production.generics.UpdatesManager;
import production.weather.ApiData;
import production.weather.Location;

import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class DatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    private static final String LOCATIONS_UPDATES_FILE_NAME = "dat/serialization/locationUpdates.dat";
    private static final String USERS_UPDATES_FILE_NAME = "dat/serialization/usersUpdates.dat";
    private static final String TURBINES_UPDATES_FILE_NAME = "dat/serialization/turbinesUpdates.dat";
    private static final String DATABASE_FILE = "conf/database.properties";


    public static Boolean isDatabaseOperationInProgress = false;
    public static Connection connectToDatabase() throws IOException, DatabaseConnectionException {

        Properties properties = new Properties();
        properties.load(new FileReader(DATABASE_FILE));
        String databaseUrl = properties.getProperty("databaseUrl");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        if (databaseUrl == null || username == null || password == null) {
            throw new DatabaseConnectionException("Missing database credentials");
        }

        try {
            return DriverManager.getConnection(databaseUrl, username, password);
        }catch (SQLException e){
            throw new DatabaseConnectionException();
        }

    }

    public static Optional<ApiData> getApiData(){
        Optional<ApiData>apiOptional = Optional.empty();
        try (Connection connection = connectToDatabase()) {
            String sqlQuery = "SELECT * FROM WEATHER_API LIMIT 1";
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();
            if(rs.next()){
                String key = rs.getString("API_KEY");
                String url = rs.getString("URL");
                apiOptional = Optional.of(new ApiData(key, url));
            }
        }catch (SQLException | DatabaseConnectionException ex){
            logger.error("Query error");
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return apiOptional;
    }

    public static Optional<Turbine> fineMostEfficientTurbine(){
        Optional<Turbine>turbineOptional = Optional.empty();
        try (Connection connection = connectToDatabase()){
            String sqlQuery = "SELECT * FROM TURBINES ORDER BY GENERATOREFFICIENCY DESC LIMIT 1";
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            if(rs.next()){
                Integer id = rs.getInt("ID");
                Integer turbineType = rs.getInt("TURBINETYPE");
                String manufacturer = rs.getString("MANUFACTURER");
                Integer rotorDiameter = rs.getInt("ROTOR_DIAMETER");
                Double generatorEfficiency = rs.getDouble("GENERATOREFFICIENCY");
                Double cutInWindSpeed = rs.getDouble("CUTINWINDSPEED");
                Long maintenanceInterval = rs.getLong("MAINTENANCEINTERVAL");
                if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                    turbineOptional = Optional.of(hat);
                }else if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    Integer orientationInDegrees = rs.getInt("ORIENTATIONINDEGREES");
                    StaticHorizontalAxisTurbine sat = new StaticHorizontalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationInDegrees);
                    turbineOptional = Optional.of(sat);
                }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType)){
                    Integer bladeHeight = rs.getInt("BLADE_HEIGHT");
                    VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                    turbineOptional = Optional.of(vat);
                }
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error("Query error");
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return turbineOptional;
    }

    public static void removeTurbineFromUser(Integer turbineId, User user){
        Optional<Turbine> turbineOptional = getTurbineById(turbineId);
        if(turbineOptional.isPresent()) {
            Turbine turbine = turbineOptional.get();
            try (Connection connection = connectToDatabase()) {
                String sql = "DELETE FROM USER_TURBINES WHERE USER_ID=? AND TURBINE_ID=? LIMIT 1;";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, user.getId());
                pstmt.setInt(2, turbineId);
                pstmt.execute();

                //Serialize updates
                UpdatesManager<Credentials, Turbine> newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), "Removed", turbine);
                FileUtils.serializeUpdates(newUpdate, TURBINES_UPDATES_FILE_NAME);

            } catch (SQLException | DatabaseConnectionException ex) {
                logger.error("Query error");
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                logger.error("Database credentials error");
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void addNewTurbineToUser(Integer turbineId, User user){
        Optional<Turbine> turbineOptional = getTurbineById(turbineId);
        if(turbineOptional.isPresent()){
            try (Connection connection = connectToDatabase()) {
                String sql = "INSERT INTO USER_TURBINES (USER_ID, TURBINE_ID) VALUES (?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, user.getId());
                pstmt.setInt(2, turbineId);
                pstmt.execute();

                //Serialize updates
                Turbine turbine = turbineOptional.get();
                UpdatesManager<Credentials, Turbine> newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), "Added", turbine);
                FileUtils.serializeUpdates(newUpdate, TURBINES_UPDATES_FILE_NAME);

            }catch (SQLException | DatabaseConnectionException ex){
                logger.error(ex.getMessage());
                System.out.println(ex.getMessage());
            }catch (IOException ex){
                logger.error("Database credentials error");
                System.out.println(ex.getMessage());
            }
        }
    }

    public static List<Turbine> getTurbinesByManufacturerAndUser(String manufacturerFilter, Integer currentUserId){
        List<Turbine>turbinelist = new ArrayList<>();
        try (Connection connection = connectToDatabase()) {
            String sql = "SELECT TURBINE_ID FROM USER_TURBINES WHERE USER_ID=?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.execute();

            ResultSet resultSet = pstmt.getResultSet();

            while(resultSet.next()){
                Integer turbineId = resultSet.getInt("TURBINE_ID");
                Optional<Turbine>turbine = getTurbineById(turbineId);
                if(turbine.isPresent()){
                    if(turbine.get().getManufacturer().contains(manufacturerFilter)){
                        turbinelist.add(turbine.get());
                    }
                }
            }
        }catch (SQLException | IOException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (DatabaseConnectionException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return turbinelist;
    }

    public static void updatePasswordById(Integer id, String newPassword, String currentPassword) throws NewCredentialsEmptyStringException{
        if(!newPassword.isEmpty()){
            try (Connection connection = connectToDatabase()){
                String sql = "UPDATE CREDENTIALS SET PASSWORD = ? WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, newPassword);
                pstmt.setInt(2, id);

                //Serialize updates
                Credentials credentials = getUserOrAdminById(id).get();
                CredentialsUpdatesManager<Credentials> newUpdate = new CredentialsUpdatesManager<>(credentials, "Changed " + credentials.getUserTypeString() + " password", FileUtils.deserializeCurrentUser().get(), currentPassword, newPassword);
                FileUtils.serializeCredentialsUpdates(newUpdate, USERS_UPDATES_FILE_NAME);

                pstmt.executeUpdate();
            }catch (SQLException | DatabaseConnectionException ex){
                logger.error(ex.getMessage());
                System.out.println(ex.getMessage());
            }catch (IOException ex){
                logger.error("Database credentials error");
                System.out.println(ex.getMessage());
            }
        }else{
            throw new NewCredentialsEmptyStringException("New password cant be an empty string");
        }
    }

    public static void updateUsernameById(Integer id, String newUsername, String currentUsername) throws NewCredentialsEmptyStringException {
        if(!newUsername.isEmpty()){
            try (Connection connection = connectToDatabase()){
                String sql = "UPDATE CREDENTIALS SET USERNAME = ? WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, newUsername);
                pstmt.setInt(2, id);

                //Serialize updates
                Credentials credentials = getUserOrAdminById(id).get();
                CredentialsUpdatesManager<Credentials> newUpdate = new CredentialsUpdatesManager<>(credentials, "Changed " + credentials.getUserTypeString() + " username", FileUtils.deserializeCurrentUser().get(), currentUsername, newUsername);
                FileUtils.serializeCredentialsUpdates(newUpdate, USERS_UPDATES_FILE_NAME);

                pstmt.executeUpdate();
            }catch (SQLException | DatabaseConnectionException ex){
                logger.error(ex.getMessage());
                System.out.println(ex.getMessage());
            }catch (IOException ex){
                logger.error("Database credentials error");
                System.out.println(ex.getMessage());
            }
        }else{
            throw new NewCredentialsEmptyStringException("New username cant be an empty string");
        }
    }

    public static void deleteUserById(Integer userId) {
        try (Connection connection = connectToDatabase()) {
            String deleteSql = "DELETE FROM CREDENTIALS WHERE ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(deleteSql);
            pstmt.setInt(1, userId);

            //Serialize updates
            Credentials user = getUserOrAdminById(userId).get();
            if(UserType.USER.getValue().equals(user.getUserType())){
                User u = (User) user;
                deleteLocationById(u.getLocation().getId(), FileUtils.deserializeCurrentUser().get());
            }
            CredentialsUpdatesManager<Credentials> newUpdate = new CredentialsUpdatesManager<>(user, "Deleted " + user.getUserTypeString(), FileUtils.deserializeCurrentUser().get());
            FileUtils.serializeCredentialsUpdates(newUpdate, USERS_UPDATES_FILE_NAME);

            pstmt.executeUpdate();

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
    }

    public static void deleteLocationById(Integer locationId, Credentials user) {
        try (Connection connection = connectToDatabase()) {
            String deleteSql = "DELETE FROM LOCATION WHERE ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(deleteSql);
            pstmt.setInt(1, locationId);

            //Serialize updates
            Location location = getLocationById(locationId).get();
            UpdatesManager<Credentials, Location> newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), "Deleted", location);
            FileUtils.serializeUpdates(newUpdate, LOCATIONS_UPDATES_FILE_NAME);

            pstmt.executeUpdate();

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
    }


    public static void addNewTurbine(Turbine turbine){
        try (Connection connection = connectToDatabase()){
            String insertItemSql = "INSERT INTO TURBINES(ID, MANUFACTURER, GENERATOREFFICIENCY, CUTINWINDSPEED, MAINTENANCEINTERVAL, ORIENTATIONINDEGREES, TURBINETYPE, ROTOR_DIAMETER, BLADE_HEIGHT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertItemSql);
            pstmt.setInt(1, turbine.getId());
            pstmt.setString(2, turbine.getManufacturer());
            pstmt.setDouble(3, turbine.getGeneratorEfficiency());
            pstmt.setDouble(4, turbine.getCutInWindSpeed());
            pstmt.setLong(5, turbine.getMaintenanceInterval());
            pstmt.setInt(8, turbine.getRotorDiameter());

            if(turbine instanceof HorizontalAxisTurbine){
                HorizontalAxisTurbine hat = (HorizontalAxisTurbine) turbine;
                pstmt.setNull(6, Types.INTEGER);
                pstmt.setInt(7, TurbineType.HORIZONTAL_AXIS.getValue());
                pstmt.setNull(9, Types.INTEGER);

                //Serialize updates
                UpdatesManager<Credentials, Turbine>newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), hat);
                FileUtils.serializeUpdates(newUpdate, TURBINES_UPDATES_FILE_NAME);
            }else if(turbine instanceof StaticHorizontalAxisTurbine){
                StaticHorizontalAxisTurbine sat = (StaticHorizontalAxisTurbine) turbine;
                pstmt.setInt(6, sat.getOrientationInDegrees());
                pstmt.setInt(7, TurbineType.STATIC_HORIZONTAL_AXIS.getValue());
                pstmt.setNull(9, Types.INTEGER);

                //Serialize updates
                UpdatesManager<Credentials, Turbine>newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), sat);
                FileUtils.serializeUpdates(newUpdate, TURBINES_UPDATES_FILE_NAME);
            }else if(turbine instanceof VerticalAxisTurbine) {
                VerticalAxisTurbine vat = (VerticalAxisTurbine) turbine;
                pstmt.setNull(6, Types.INTEGER);
                pstmt.setInt(7, TurbineType.VERTICAL_AXIS.getValue());
                pstmt.setInt(9, vat.getBladeHeight());

                //Serialize updates
                UpdatesManager<Credentials, Turbine>newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), vat);
                FileUtils.serializeUpdates(newUpdate, TURBINES_UPDATES_FILE_NAME);
            }

            pstmt.executeUpdate();

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
    }

    public static void addNewLocation(Location location){
        try (Connection connection = connectToDatabase()){
            String insertItemSql = "INSERT INTO LOCATION(ID, LATITUDE, LONGITUDE, COUNTRY, CITY) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertItemSql);
            pstmt.setInt(1, location.getId());
            pstmt.setDouble(2, location.getLatitude());
            pstmt.setDouble(3, location.getLongitude());
            pstmt.setString(4, location.getCountry());
            pstmt.setString(5, location.getCity());

            //Serialize updates
            UpdatesManager<Credentials, Location>newUpdate = new UpdatesManager<>(FileUtils.deserializeCurrentUser().get(), location);
            FileUtils.serializeUpdates(newUpdate, LOCATIONS_UPDATES_FILE_NAME);


            pstmt.executeUpdate();

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
    }

    public static void addToUserTurbines(Integer userId, List<Turbine>turbineList, Connection connection){
        String insertSql = "INSERT INTO USER_TURBINES(USER_ID, TURBINE_ID) VALUES(?, ?)";
        try{
            PreparedStatement pstmt = connection.prepareStatement(insertSql);
            connection.setAutoCommit(false);
            for (Turbine turbine : turbineList){
                pstmt.setInt(1, userId);
                pstmt.setInt(2, turbine.getId());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            pstmt.close();

        }catch (SQLException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());

        }
    }

    public static void addNewUserOrAdmin(Credentials credentials, Optional<List<Turbine>>optionalTurbineList){
        try {
            Connection connection = connectToDatabase();
            connection.setAutoCommit(false);

            String insertItemSql = "INSERT INTO CREDENTIALS(ID, TYPE, USERNAME, PASSWORD, LOCATION_ID, ROLE) VALUES(?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertItemSql);
            pstmt.setInt(1, credentials.getId());
            pstmt.setInt(2, credentials.getUserType());
            pstmt.setString(3, credentials.getUsername());
            pstmt.setString(4, credentials.getPassword());
            if(UserType.USER.getValue().equals(credentials.getUserType())){
                User user = (User) credentials;
                pstmt.setInt(5, user.getLocation().getId());
                pstmt.setString(6, "");
                pstmt.executeUpdate();

                if (optionalTurbineList.isPresent()) {
                    addToUserTurbines(user.getId(), optionalTurbineList.get(), connection);
                }

            }else if(UserType.ADMIN.getValue().equals(credentials.getUserType())){
                Admin admin = (Admin) credentials;
                pstmt.setNull(5, Types.INTEGER);
                pstmt.setString(6, admin.getRole());
                pstmt.executeUpdate();
            }

            connection.commit();

            //Serialize updates
            CredentialsUpdatesManager<Credentials> newUpdate = new CredentialsUpdatesManager<>(credentials, "Added " + credentials.getUserTypeString(), FileUtils.deserializeCurrentUser().get());
            FileUtils.serializeCredentialsUpdates(newUpdate, USERS_UPDATES_FILE_NAME);

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
    }

    public static List<Credentials> getUsers(Optional<Credentials>currentUserFilter){
        List<Credentials> users = new ArrayList<>();

        try(Connection connection = connectToDatabase()) {
            String sqlQuery = "SELECT * FROM CREDENTIALS ";
            if(currentUserFilter.isPresent()){
                sqlQuery += "WHERE ID != " + currentUserFilter.get().getId();
            }
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            while(rs.next()) {
                Integer id = rs.getInt("ID");
                Integer userType = rs.getInt("TYPE");
                String username = rs.getString("USERNAME");
                String password = rs.getString("PASSWORD");
                if(UserType.ADMIN.getValue().equals(userType)){
                    String role = rs.getString("ROLE");
                    Admin admin = new Admin(id, username, password, role);
                    users.add(admin);
                }
                else if(UserType.USER.getValue().equals(userType)){
                    //get location
                    Integer locationId = rs.getInt("LOCATION_ID");
                    Optional<Location> locationOptional = getLocationById(locationId);
                    if(locationOptional.isPresent()){
                        User user = new User(id, username, password, locationOptional.get());
                        users.add(user);
                    }
                }
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }

        return users;
    }

    //get users by filter
    public static List<Credentials> getUsersAndAdminsByFilter(String usernameFilter, String userTypeFilter){
        List<Credentials> credentialsList = new ArrayList<>();
        Map<Integer, Object> queryParams = new HashMap<>();
        Integer paramOrdinalNumber = 1;

        try (Connection connection = connectToDatabase()) {
            String baseSql = "SELECT * FROM CREDENTIALS WHERE 1=1 ";
            if(!usernameFilter.isEmpty()){
                baseSql += "AND USERNAME = ?";
                queryParams.put(paramOrdinalNumber, usernameFilter);
                paramOrdinalNumber++;
            }
            if(!userTypeFilter.isEmpty()){
                if(userTypeFilter.equals("Admin")){
                    baseSql += "AND TYPE = ?";
                    queryParams.put(paramOrdinalNumber, "2");
                    paramOrdinalNumber++;
                }else if(userTypeFilter.equals("User")){
                    baseSql += "AND TYPE = ?";
                    queryParams.put(paramOrdinalNumber, "1");
                    paramOrdinalNumber++;
                }
            }
            PreparedStatement pstmt = connection.prepareStatement(baseSql);

            for(Integer paramNumber : queryParams.keySet()){
                pstmt.setString(paramNumber, (String) queryParams.get(paramNumber));
            }

            pstmt.execute();
            ResultSet resultSet = pstmt.getResultSet();

            while(resultSet.next()){
                Integer id = resultSet.getInt("ID");
                Integer type = resultSet.getInt("TYPE");
                String username = resultSet.getString("USERNAME");
                String password = resultSet.getString("PASSWORD");
                if(UserType.ADMIN.getValue().equals(type)){
                    String role = resultSet.getString("ROLE");
                    credentialsList.add(new Admin(id, username, password, role));
                }else if(UserType.USER.getValue().equals(type)){
                    Integer locationId = resultSet.getInt("LOCATION_ID");
                    Optional<Location>locationOptional = getLocationById(locationId);
                    if(locationOptional.isPresent()){
                        credentialsList.add(new User(id, username, password, locationOptional.get()));
                    }
                }
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }

        return credentialsList;
    }

    //check for username
    public static boolean checkIfUsernameExists(String username) {

        try (Connection connection = connectToDatabase()) {
            String sql = "SELECT 1 FROM CREDENTIALS WHERE USERNAME = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return true;
            }
        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return false;
    }

    //Get users by id
    public static Optional<Credentials> getUserOrAdminById(Integer idFilter) {
        Optional<Credentials> credentials = Optional.empty();

        try (Connection connection = connectToDatabase()) {
            String baseSql = "SELECT * FROM CREDENTIALS WHERE ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(baseSql);
            pstmt.setInt(1, idFilter);

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                Integer id = resultSet.getInt("ID");
                Integer type = resultSet.getInt("TYPE");
                String username = resultSet.getString("USERNAME");
                String password = resultSet.getString("PASSWORD");

                if (UserType.ADMIN.getValue().equals(type)) {
                    String role = resultSet.getString("ROLE");
                    credentials = Optional.of(new Admin(id, username, password, role));
                } else if (UserType.USER.getValue().equals(type)) {
                    Integer locationId = resultSet.getInt("LOCATION_ID");
                    Optional<Location> locationOptional = getLocationById(locationId);
                    if (locationOptional.isPresent()) {
                        credentials = Optional.of(new User(id, username, password, locationOptional.get()));
                    }
                }
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }

        return credentials;
    }

    //get locations by country
    public static List<Location> getLocationsByCountry(String countryFilter){
        List<Location> locations = new ArrayList<>();
        try (Connection connection = connectToDatabase()) {
            StringBuilder baseSqlBuilder = new StringBuilder("SELECT * FROM LOCATION WHERE 1=1");
            if (!countryFilter.isEmpty()) {
                baseSqlBuilder.append(" AND COUNTRY=?");
            }
            String baseSql = baseSqlBuilder.toString();
            PreparedStatement pstmt = connection.prepareStatement(baseSql);
            if (!countryFilter.isEmpty()) {
                pstmt.setString(1, countryFilter);
            }
            pstmt.execute();

            ResultSet rs = pstmt.getResultSet();

            while(rs.next()) {
                Integer locationId = rs.getInt("ID");
                Double latitude = rs.getDouble("LATITUDE");
                Double longitude = rs.getDouble("LONGITUDE");
                String country = rs.getString("COUNTRY");
                String city = rs.getString("CITY");

                locations.add(new Location(locationId, latitude, longitude, country, city));
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return locations;
    }

    //get location by id
    public static Optional<Location> getLocationById(Integer id){
        Optional<Location> location = Optional.empty();
        try (Connection connection = connectToDatabase()) {
            String baseSql = "SELECT * FROM LOCATION WHERE ID = ?";
            PreparedStatement pstmt = connection.prepareStatement(baseSql);
            pstmt.setInt(1, id);
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();

            if(rs.next()) {
                Integer locationId = rs.getInt("ID");
                Double latitude = rs.getDouble("LATITUDE");
                Double longitude = rs.getDouble("LONGITUDE");
                String country = rs.getString("COUNTRY");
                String city = rs.getString("CITY");

                //Location builder pattern
                Location newLocation = new Location.Builder(locationId)
                        .atLatitude(latitude)
                        .atLongitude(longitude)
                        .atCountry(country)
                        .atCity(city)
                        .build();

                location = Optional.of(newLocation);
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return location;
    }

    //get all locations
    public static Optional<List<Location>> getLocations(){
        List<Location> locations = new ArrayList<>();
        try(Connection connection = connectToDatabase()) {
            String sqlQuery = "SELECT * FROM LOCATION ";
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            while(rs.next()) {
                Integer id = rs.getInt("ID");
                Double latitude = rs.getDouble("LATITUDE");
                Double longitude = rs.getDouble("LONGITUDE");
                String country = rs.getString("COUNTRY");
                String city = rs.getString("CITY");
                locations.add(new Location(id, latitude, longitude, country, city));
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }

        return Optional.of(locations);
    }


    //get all turbines
    public static Optional<List<Turbine>> getTurbines(){
        List<Turbine> turbines = new ArrayList<>();
        try(Connection connection = connectToDatabase()) {
            String sqlQuery = "SELECT * FROM TURBINES ";
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            while(rs.next()) {
                Integer id = rs.getInt("ID");
                Integer turbineType = rs.getInt("TURBINETYPE");
                String manufacturer = rs.getString("MANUFACTURER");
                Integer rotorDiameter = rs.getInt("ROTOR_DIAMETER");
                Double generatorEfficiency = rs.getDouble("GENERATOREFFICIENCY");
                Double cutInWindSpeed = rs.getDouble("CUTINWINDSPEED");
                Long maintenanceInterval = rs.getLong("MAINTENANCEINTERVAL");
                if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                    turbines.add(hat);
                }else if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    Integer orientationInDegrees = rs.getInt("ORIENTATIONINDEGREES");
                    StaticHorizontalAxisTurbine sat = new StaticHorizontalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationInDegrees);
                    turbines.add(sat);
                }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType)){
                    Integer bladeHeight = rs.getInt("BLADE_HEIGHT");
                    VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, id, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                    turbines.add(vat);
                }
            }

        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }

        return Optional.of(turbines);
    }

    //turbines by manufacturer
    public static List<Turbine> getTurbinesByManufacturer(String manufacturerFilter){
        List<Turbine>turbinelist = new ArrayList<>();
        try (Connection connection = connectToDatabase()) {
            StringBuilder baseSqlBuilder = new StringBuilder("SELECT * FROM TURBINES WHERE 1=1");
            if (!manufacturerFilter.isEmpty()) {
                baseSqlBuilder.append(" AND MANUFACTURER=?");
            }
            String baseSql = baseSqlBuilder.toString();
            PreparedStatement pstmt = connection.prepareStatement(baseSql);
            if (!manufacturerFilter.isEmpty()) {
                pstmt.setString(1, manufacturerFilter);
            }
            pstmt.execute();

            ResultSet resultSet = pstmt.getResultSet();

            while(resultSet.next()){
                Integer turbineId = resultSet.getInt("ID");
                String manufacturer = resultSet.getString("MANUFACTURER");
                Integer rotorDiameter = resultSet.getInt("ROTOR_DIAMETER");
                Double generatorEfficiency = resultSet.getDouble("GENERATOREFFICIENCY");
                Double cutInWindSpeed = resultSet.getDouble("CUTINWINDSPEED");
                Long maintenanceInterval = resultSet.getLong("MAINTENANCEINTERVAL");
                Integer turbineType = resultSet.getInt("TURBINETYPE");
                if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                    turbinelist.add(hat);
                }else if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    Integer orientationInDegrees = resultSet.getInt("ORIENTATIONINDEGREES");
                    StaticHorizontalAxisTurbine sht = new StaticHorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationInDegrees);
                    turbinelist.add(sht);
                }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType)){
                    Integer bladeHeight = resultSet.getInt("BLADE_HEIGHT");
                    VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                    turbinelist.add(vat);
                }
            }
        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return turbinelist;
    }

    public static Optional<Turbine> getTurbineById(Integer id){
        Optional<Turbine> turbineOptional = Optional.empty();
        try (Connection connection = connectToDatabase()) {
            String baseSql = "SELECT * FROM TURBINES WHERE ID=? ";
            PreparedStatement pstmt = connection.prepareStatement(baseSql);
            pstmt.setInt(1, id);
            pstmt.execute();

            ResultSet resultSet = pstmt.getResultSet();

            if(resultSet.next()){
                Integer turbineId = resultSet.getInt("ID");
                String manufacturer = resultSet.getString("MANUFACTURER");
                Integer rotorDiameter = resultSet.getInt("ROTOR_DIAMETER");
                Double generatorEfficiency = resultSet.getDouble("GENERATOREFFICIENCY");
                Double cutInWindSpeed = resultSet.getDouble("CUTINWINDSPEED");
                Long maintenanceInterval = resultSet.getLong("MAINTENANCEINTERVAL");
                Integer turbineType = resultSet.getInt("TURBINETYPE");
                if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                    turbineOptional = Optional.of(hat);
                }else if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType)){
                    Integer orientationInDegrees = resultSet.getInt("ORIENTATIONINDEGREES");
                    StaticHorizontalAxisTurbine sht = new StaticHorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationInDegrees);
                    turbineOptional = Optional.of(sht);
                }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType)){
                    Integer bladeHeight = resultSet.getInt("BLADE_HEIGHT");
                    VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                    turbineOptional = Optional.of(vat);
                }
            }
        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return turbineOptional;
    }

    public static List<Turbine> getTurbinesByIds(String[] turbineIdArray){
        List<Turbine>turbinelist = new ArrayList<>();
        try (Connection connection = connectToDatabase()) {
            for(String stringId : turbineIdArray){
                int id = Integer.parseInt(stringId);
                String baseSql = "SELECT * FROM TURBINES WHERE 1=1 ";
                baseSql += "AND ID=?";
                PreparedStatement pstmt = connection.prepareStatement(baseSql);
                pstmt.setLong(1, id);
                pstmt.execute();

                ResultSet resultSet = pstmt.getResultSet();

                if(resultSet.next()){
                    Integer turbineId = resultSet.getInt("ID");
                    String manufacturer = resultSet.getString("MANUFACTURER");
                    Integer rotorDiameter = resultSet.getInt("ROTOR_DIAMETER");
                    Double generatorEfficiency = resultSet.getDouble("GENERATOREFFICIENCY");
                    Double cutInWindSpeed = resultSet.getDouble("CUTINWINDSPEED");
                    Long maintenanceInterval = resultSet.getLong("MAINTENANCEINTERVAL");
                    Integer turbineType = resultSet.getInt("TURBINETYPE");
                    if(TurbineType.HORIZONTAL_AXIS.getValue().equals(turbineType)){
                        HorizontalAxisTurbine hat = new HorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval);
                        turbinelist.add(hat);
                    }else if(TurbineType.STATIC_HORIZONTAL_AXIS.getValue().equals(turbineType)){
                        Integer orientationInDegrees = resultSet.getInt("ORIENTATIONINDEGREES");
                        StaticHorizontalAxisTurbine sht = new StaticHorizontalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, orientationInDegrees);
                        turbinelist.add(sht);
                    }else if(TurbineType.VERTICAL_AXIS.getValue().equals(turbineType)){
                        Integer bladeHeight = resultSet.getInt("BLADE_HEIGHT");
                        VerticalAxisTurbine vat = new VerticalAxisTurbine(manufacturer, turbineId, rotorDiameter, generatorEfficiency, cutInWindSpeed, maintenanceInterval, bladeHeight);
                        turbinelist.add(vat);
                    }
                }
            }
        }catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        }catch (IOException ex){
            logger.error("Database credentials error");
            System.out.println(ex.getMessage());
        }
        return turbinelist;
    }

    public static Integer getNextId(String table){
        Integer nextId = 1;
        try (Connection connection = connectToDatabase()) {
            String sqlQuery = "SELECT MAX(ID) AS MaxID FROM " + table;
            Statement stmt = connection.createStatement();
            stmt.execute(sqlQuery);
            ResultSet rs = stmt.getResultSet();

            if(rs.next()) {
                nextId = rs.getInt("MaxID");
                nextId++;
            }

        }catch (SQLException | IOException | DatabaseConnectionException ex){
            System.out.println(ex.getMessage());
        }
        return nextId;
    }

    public static Optional<Credentials> checkForCredentials(LoginRecord loginRecord) throws UserNotFoundException {

        Optional<Credentials>user = Optional.empty();

        String query = "SELECT * FROM CREDENTIALS WHERE USERNAME = ? AND PASSWORD = ?";

        try(Connection connection = connectToDatabase()) {

            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, loginRecord.username());
            pstmt.setString(2, loginRecord.password());

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    Integer userType = rs.getInt("TYPE");
                    if(UserType.USER.getValue().equals(userType)){
                        //get Location for User
                        Optional<Location> locationOptional = getLocationById(rs.getInt("LOCATION_ID"));
                        if (locationOptional.isPresent()){
                            User newUser = new User(
                                    rs.getInt("ID"),
                                    rs.getString("USERNAME"),
                                    rs.getString("PASSWORD"),
                                    locationOptional.get()
                            );
                            user = Optional.of(newUser);
                        }
                    }else if(UserType.ADMIN.getValue().equals(userType)){
                        Admin newAdmin = new Admin(
                                rs.getInt("ID"),
                                rs.getString("USERNAME"),
                                rs.getString("PASSWORD"),
                                rs.getString("ROLE")
                        );
                        user = Optional.of(newAdmin);
                    }
                }else{
                    throw new UserNotFoundException("No such user. Try again.");
                }
            }

        } catch (SQLException | DatabaseConnectionException ex){
            logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (IOException ex){
            logger.error("File reading error");
            System.out.println(ex.getMessage());
        }

        return user;
    }

    public static Optional<String> hashPassword(String password) throws NewCredentialsEmptyStringException{
        if(!password.isEmpty()){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(password.getBytes());
                byte[] bytes = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return Optional.of(sb.toString());
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error hashing the password.");
                System.out.println(e.getMessage());
                return Optional.empty();
            }
        }else {
            throw new NewCredentialsEmptyStringException("Password cant be an empty string.");
        }

    }

}


