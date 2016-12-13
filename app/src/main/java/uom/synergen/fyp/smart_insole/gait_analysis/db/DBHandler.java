package uom.synergen.fyp.smart_insole.gait_analysis.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHandler {

    public static boolean setData(String sql) throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getConnection();
        Statement statement = connection.createStatement();
        int res = statement.executeUpdate(sql);

        if(res != -1) {
            return true;
        }else {
            return false;
        }
    }

    public static ResultSet getData(String sql) throws SQLException, ClassNotFoundException {
        Connection connection = DBConnection.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        return resultSet;
    }

}
