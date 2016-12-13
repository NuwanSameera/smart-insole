package uom.synergen.fyp.smart_insole.gait_analysis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

public class DBConnection {

    private static Connection connection;
    private static DBConnection dbConnection;

    private DBConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(SmartInsoleConstants.DB_URL, SmartInsoleConstants.DB_USER,
                SmartInsoleConstants.DB_PASSWORD);
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if(dbConnection == null) {
            dbConnection = new DBConnection();
        }
        return dbConnection.connection;
    }

}
