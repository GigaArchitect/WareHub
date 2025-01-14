package nasar.mustafa.warehub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import org.apache.commons.dbutils.QueryRunner;


public class ApplicationEntry extends Application {
    public static Connection connection;
    public static Connection connect(){
        String con_string = "jdbc:sqlite:DATA.db";
        try {
            return DriverManager.getConnection(con_string);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runSqlFile(Connection connection, String path) {
        QueryRunner runner = new QueryRunner();
        URL resourceUrl = ApplicationEntry.class.getResource(path);
        Path resourcePath;
        try {
            assert resourceUrl != null : " Resource Doesn't Exist !";
            resourcePath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }

        try {
            String sql = new String(Files.readAllBytes(resourcePath));
            String[] sqlStatements = sql.split(";");
            for (String statement : sqlStatements) {
                if (!statement.trim().isEmpty()) {
                    runner.update(connection, statement.trim());
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Error executing SQL: " + e.getMessage(), e);
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
        Image icon = new Image(getClass().getResourceAsStream("WareHub.png"));
        stage.getIcons().add(icon);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        connection = connect();
        runSqlFile(connection, "SQL/SETUP.sql");
        launch();
    }
}