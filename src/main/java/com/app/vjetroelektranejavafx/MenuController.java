package com.app.vjetroelektranejavafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import production.threads.CalculateTotalOutputThread;
import production.threads.FileUtilsThread;
import production.threads.GetCurrentUserThread;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;

public class MenuController {

    private static final String CURRENT_USER_FILE_NAME = "dat/serialization/currentUser.dat";
    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    public void initialize(){
        FileUtilsThread.isCurrentUserFileInUse = false;
        GetCurrentUserThread currentUserThread = new GetCurrentUserThread();
        Thread starter = new Thread(currentUserThread);
        starter.start();
    }

    public void showAddTurbineToUser(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("addUserTurbines.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("Add turbine");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Add turbine page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void showDeleteTurbineFromUser(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("deleteUserTurbines.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("delete turbine");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Delete turbine page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void showEnergyOutput(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("userEnergyOutput.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("Energy output");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("User energy output page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void showUserTurbines(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("userTurbines.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("List turbines");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("User turbines page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void resetPasswordUserView(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("resetPasswordUser.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("reset password");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Reset password page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void resetUsernameUserView(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("resetUsernameUser.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("reset password");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Reset username page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void resetPassword(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("resetPassword.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("reset password");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Reset password page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void resetUsername(){
        CalculateTotalOutputThread.stop = true;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("resetUsername.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("reset username");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Reset username page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void logout(){
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Confirm logout");
        confirmationAlert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("loginScreen.fxml"));
            CalculateTotalOutputThread.stop = true;
            try {

                logger.info("Logged out.");
                Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                VjetroelektraneApp.getMainStage().setTitle("login");
                VjetroelektraneApp.getMainStage().setScene(scene);
                //erase current user
                try {
                    RandomAccessFile file = new RandomAccessFile(CURRENT_USER_FILE_NAME, "rw");
                    file.setLength(0);
                    file.close();
                } catch (IOException e) {
                    logger.error("Error erasing file contents: " + e.getMessage());
                }

                VjetroelektraneApp.getMainStage().show();

            }catch (IOException e){
                logger.error("Logout failed.");
                throw new RuntimeException(e);
            }
        }
    }

    public void listUsers(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("users.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("users");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Users list page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void listTurbines(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("turbines.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("turbines");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Turbines list page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void listLocations(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("locations.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("locations");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Locations list page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void addNewUser(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("addNewUser.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("New user");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Add new user page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void addNewAdmin(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("addNewAdmin.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("New admin");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Add new admin page load failed.");
            throw new RuntimeException(e);
        }
    }


    public void deleteCredentials(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("deleteCredentials.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("delete admin");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Delete user page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void addNewTurbine(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("addNewTurbine.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("New Turbine");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Add new turbine page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void deleteTurbine(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("deleteTurbine.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("delete turbine");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Delete turbine page load failed.");
            throw new RuntimeException(e);
        }
    }

    public void credentialsUpdatesLog(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("credentialsUpdates.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("updates log");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Credentials updates log page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void turbinesUpdatesLog(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("turbinesUpdates.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("updates log");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Turbines updates log page load failed.");
            throw new RuntimeException(e);
        }
    }
    public void locationsUpdatesLog(){
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("locationsUpdates.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            VjetroelektraneApp.getMainStage().setTitle("updates log");
            VjetroelektraneApp.getMainStage().setScene(scene);
            VjetroelektraneApp.getMainStage().show();
        }catch (IOException e){
            logger.error("Locations updates log page load failed.");
            throw new RuntimeException(e);
        }
    }
}
