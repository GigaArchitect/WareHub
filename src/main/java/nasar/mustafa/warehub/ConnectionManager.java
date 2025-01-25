package nasar.mustafa.warehub;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class ConnectionManager {
    private static Connection instance;
    private ConnectionManager() {
        String con_string = "jdbc:sqlite:DATA.db";
        try {
            instance = DriverManager.getConnection(con_string);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            new ConnectionManager();
        }
        return instance;
    }
}
