module com.app.vjetroelektranejavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires java.sql;
    requires org.slf4j;


    opens com.app.vjetroelektranejavafx to javafx.fxml;
    exports com.app.vjetroelektranejavafx;
}