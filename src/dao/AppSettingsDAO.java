package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;

/** Reads and writes simple string settings stored in app_settings. */
public final class AppSettingsDAO {
    private AppSettingsDAO() { }

    public static String getValue(String key) {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("setting_value") : null;
            }
        } catch (SQLException ex) {
            System.err.println("Could not load app setting '" + key + "': " + ex.getMessage());
            return null;
        }
    }

    public static boolean saveValue(String key, String value) {
        String sql = "INSERT INTO app_settings(setting_key, setting_value) VALUES(?, ?) "
                + "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Could not save app setting '" + key + "': " + ex.getMessage());
            return false;
        }
    }
}
