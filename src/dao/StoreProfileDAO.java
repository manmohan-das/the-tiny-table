package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public final class StoreProfileDAO {
    private static final int PROFILE_ID = 1;
    private static final String DEFAULT_STORE_NAME = "The Tiny Table";
    private static final PropertyChangeSupport PROFILE_CHANGES = new PropertyChangeSupport(StoreProfileDAO.class);

    private StoreProfileDAO() {
    }

    public static void addProfileChangeListener(PropertyChangeListener listener) {
        PROFILE_CHANGES.addPropertyChangeListener(listener);
    }

    public static void removeProfileChangeListener(PropertyChangeListener listener) {
        PROFILE_CHANGES.removePropertyChangeListener(listener);
    }

    public static final class Profile {
        private final String storeName;
        private final String address;
        private final String phone;
        private final String gstin;

        public Profile(String storeName, String address, String phone, String gstin) {
            this.storeName = storeName;
            this.address = address;
            this.phone = phone;
            this.gstin = gstin;
        }

        public String getStoreName() {
            return storeName;
        }

        public String getAddress() {
            return address;
        }

        public String getPhone() {
            return phone;
        }

        public String getGstin() {
            return gstin;
        }
    }

    public static Profile loadProfile() {
        String sql = "SELECT store_name, address, phone, gstin "
                + "FROM store_profile WHERE profile_id=?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, PROFILE_ID);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Profile(
                            clean(result.getString("store_name")),
                            clean(result.getString("address")),
                            clean(result.getString("phone")),
                            clean(result.getString("gstin")));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Could not load store profile: " + ex.getMessage());
        }

        return new Profile(DEFAULT_STORE_NAME, "", "", "");
    }

    public static boolean saveProfile(
            String storeName, String address, String phone, String gstin) {

        String name = clean(storeName);
        String savedAddress = clean(address);
        String savedPhone = clean(phone);
        String savedGstin = clean(gstin);

        if (name.isEmpty()
                || name.length() > 150
                || savedAddress.length() > 255
                || savedPhone.length() > 30
                || savedGstin.length() > 30) {
            return false;
        }

        String updateSql = "UPDATE store_profile "
                + "SET store_name=?, address=?, phone=?, gstin=? "
                + "WHERE profile_id=?";

        String insertSql = "INSERT INTO store_profile "
                + "(profile_id, store_name, address, phone, gstin) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            int updated;

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setString(1, name);
                statement.setString(2, savedAddress);
                statement.setString(3, savedPhone);
                statement.setString(4, savedGstin);
                statement.setInt(5, PROFILE_ID);
                updated = statement.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, PROFILE_ID);
                    statement.setString(2, name);
                    statement.setString(3, savedAddress);
                    statement.setString(4, savedPhone);
                    statement.setString(5, savedGstin);
                    statement.executeUpdate();
                }
            }

            PROFILE_CHANGES.firePropertyChange(
                    "profile",
                    null,
                    new Profile(name, savedAddress, savedPhone, savedGstin));

            return true;
        } catch (SQLException ex) {
            System.err.println("Could not save store profile: " + ex.getMessage());
            return false;
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}