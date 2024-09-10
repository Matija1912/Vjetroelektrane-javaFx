package com.app.vjetroelektranejavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VjetroelektraneApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(VjetroelektraneApp.class);
    public static Stage mainStage;
    @Override
    public void start(Stage stage) throws IOException {

        mainStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(VjetroelektraneApp.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        logger.info("App is running.");
    }
    public static Stage getMainStage(){
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}