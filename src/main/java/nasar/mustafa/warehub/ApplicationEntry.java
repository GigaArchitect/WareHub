package nasar.mustafa.warehub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import nasar.mustafa.warehub.controllers.LoggedController;
import nasar.mustafa.warehub.ConnectionManager;
import org.apache.ibatis.jdbc.ScriptRunner;



public class ApplicationEntry extends Application {

    public static String convertArabicNumerals(String input) {
        return input.replace("٠", "0")
                .replace("١", "1")
                .replace("٢", "2")
                .replace("٣", "3")
                .replace("٤", "4")
                .replace("٥", "5")
                .replace("٦", "6")
                .replace("٧", "7")
                .replace("٨", "8")
                .replace("٩", "9");
    }

    public static void runSqlFile(Connection connection, String path) {
        try {
            ScriptRunner runner = new ScriptRunner(connection);
            runner.setLogWriter(null);  // Disable console output
            runner.setErrorLogWriter(null);  // Disable error console output

            InputStream is = ApplicationEntry.class.getResourceAsStream(path);
            Reader reader = new InputStreamReader(is);
            runner.runScript(reader);
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException("Error executing SQL file: " + path, e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationEntry.class.getResource("Logged.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("WareHub");
        stage.setMaximized(true);
        stage.setScene(scene);
        LoggedController loggedController = fxmlLoader.getController();
        loggedController.setStage(stage);
        loggedController.setConnection(ConnectionManager.getInstance());

        Image icon = new Image(getClass().getResourceAsStream("WareHub.png"));
        stage.getIcons().add(icon);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        runSqlFile(ConnectionManager.getInstance(), "SQL/SETUP.sql"); // This Bitchy Line caused auto commit to be set to false
        launch();
    }
}