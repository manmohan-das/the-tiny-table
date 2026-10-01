package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.User;
import util.DBConnection;
import util.PasswordUtil;

/**
 * users table ke saare DB operations.
 *
 * Improvements over old version:
 *  - SELECT * hataya, fixed column list
 *  - password SQL me compare nahi hota (Java me hash verify hota hai)
 *  - ResultSet bhi try-with-resources me
 *  - duplicate mapping code -> mapRow()
 *  - catch (SQLException) instead of catch (Exception)
 *  - password/list me UI tak nahi jata
 *  - magic numbers hata diye (STATUS_ACTIVE / STATUS_INACTIVE)
 *  - usernameExists / getUserByUsername / updatePassword / updateStatus add kiye
 */
public class UserDAO {

    /* ---------------- constants (magic numbers gone) ---------------- */
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE   = 1;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER  = "USER";

    /** SELECT * ki jagah yahi column list har query me use hoti hai. */
    private static final String USER_COLUMNS =
            "user_id, name, username, password, role, salary, status";

    /* ================================================================== */
    /* 1. LOGIN                                                            */
    /* ================================================================== */
    public User login(String username, String password) {

        // Password SQL me compare NAHI karte.
        // MySQL ki default collation case-insensitive hai -> 'Pass123' aur
        // 'pass123' dono login ho jate the. Ab Java me verify hota hai.
        String sql = "SELECT " + USER_COLUMNS +
                     " FROM users WHERE username = ? AND status = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, STATUS_ACTIVE);

            // ResultSet bhi try-with-resources me (pehle leak ho raha tha)
            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;                        // aisa koi active user nahi
                }

                User user = mapRow(rs);

                if (!PasswordUtil.verify(password, user.getPassword())) {
                    return null;                        // galat password
                }

                // Migration: purana password plain text me pada tha ->
                // ab hash karke DB me save kar do.
                if (!PasswordUtil.isHashed(user.getPassword())) {
                    updatePassword(user.getUserId(), password);
                }

                user.setPassword(null);                 // hash session/UI tak na jaye
                return user;
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.login() error, username = " + username);
            e.printStackTrace();
        }

        return null;
    }

    /* ================================================================== */
    /* 2. GET USER BY ID                                                   */
    /* ================================================================== */
    public User getUserById(int userId) {

        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPassword(null);
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getUserById() error, userId = " + userId);
            e.printStackTrace();
        }

        return null;
    }

    /* ================================================================== */
    /* 3. GET USER BY USERNAME  (naya - duplicate check ke liye kaam aata) */
    /* ================================================================== */
    public User getUserByUsername(String username) {

        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRow(rs);
                    user.setPassword(null);
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getUserByUsername() error, username = " + username);
            e.printStackTrace();
        }

        return null;
    }

    /* ================================================================== */
    /* 4. USERNAME EXISTS?  (naya - addUser/update se pehle check)         */
    /* ================================================================== */
    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.usernameExists() error, username = " + username);
            e.printStackTrace();
        }

        return false;
    }

    /* ================================================================== */
    /* 5. GET ALL USERS                                                    */
    /* ================================================================== */
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT " + USER_COLUMNS + " FROM users ORDER BY user_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = mapRow(rs);
                user.setPassword(null);     // list/table me password kabhi na jaye
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getAllUsers() error");
            e.printStackTrace();
        }

        return users;
    }

    /* ================================================================== */
    /* 6. GET USERS PAGE-WISE  (naya - users bahut ho jayein to)           */
    /*    page 1 = pehla page, pageSize = ek page me kitne users            */
    /* ================================================================== */
    public List<User> getUsers(int page, int pageSize) {

        List<User> users = new ArrayList<>();
        int offset = Math.max(page - 1, 0) * pageSize;

        String sql = "SELECT " + USER_COLUMNS +
                     " FROM users ORDER BY user_id LIMIT ? OFFSET ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = mapRow(rs);
                    user.setPassword(null);
                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("UserDAO.getUsers() error, page = " + page);
            e.printStackTrace();
        }

        return users;
    }

    /* ================================================================== */
    /* 7. ADD USER                                                         */
    /* ================================================================== */
    public boolean addUser(User user) {

        // pehle duplicate username check -> user ko saaf message de sakte ho
        if (usernameExists(user.getUsername())) {
            System.err.println("UserDAO.addUser(): username already exists -> "
                    + user.getUsername());
            return false;
        }

        String sql = "INSERT INTO users (name, username, password, role, salary, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, PasswordUtil.hash(user.getPassword()));  // yahin hash
            ps.setString(4, user.getRole());
            ps.setBigDecimal(5, user.getSalary());
            ps.setInt(6, user.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.addUser() error, username = " + user.getUsername());
            e.printStackTrace();
        }

        return false;
    }

    /* ================================================================== */
    /* 8. UPDATE USER  (name / role / status)                              */
    /*    username aur password yahan se nahi badalte - uske liye          */
    /*    updatePassword() use karo.                                       */
    /* ================================================================== */
    public boolean updateUser(User user) {

        String sql = "UPDATE users SET name = ?,username= ?, role = ?, salary = ?, status = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getRole());
            ps.setBigDecimal(4, user.getSalary());
            ps.setInt(5, user.getStatus());
            ps.setInt(6, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updateUser() error, userId = " + user.getUserId());
            e.printStackTrace();
        }

        return false;
    }

    /* ================================================================== */
    /* 9. UPDATE PASSWORD  (naya) - plain password do, ye hash karega       */
    /* ================================================================== */
    public boolean updatePassword(int userId, String plainPassword) {

        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, PasswordUtil.hash(plainPassword));
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updatePassword() error, userId = " + userId);
            e.printStackTrace();
        }

        return false;
    }

    /* ================================================================== */
    /* 10. UPDATE STATUS / DEACTIVATE  (naya - soft delete)                 */
    /* ================================================================== */
    public boolean updateStatus(int userId, int status) {

        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updateStatus() error, userId = " + userId);
            e.printStackTrace();
        }

        return false;
    }

    /** Delete ki jagah best option: user ko inactive kar do. */
    public boolean deactivateUser(int userId) {
        return updateStatus(userId, STATUS_INACTIVE);
    }

    /* ================================================================== */
    /* 11. DELETE USER  (hard delete)                                      */
    /*     Sirf tab chalega jab user ka koi record books/issue table me     */
    /*     na ho. Warna FK constraint error aayega (aur ye chup-chaap       */
    /*     false return kar dega). Library project me deactivateUser()     */
    /*     use karna better hai.                                            */
    /* ================================================================== */
    public boolean deleteUser(int userId) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.deleteUser() error, userId = " + userId);
            e.printStackTrace();
        }

        return false;
    }

    /* ================================================================== */
    /* helper - mapping code ek jagah (pehle 4 baar repeat ho raha tha)     */
    /* ================================================================== */
    private User mapRow(ResultSet rs) throws SQLException {

        User user = new User();

        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setSalary(rs.getBigDecimal("salary"));
        user.setStatus(rs.getInt("status"));

        return user;
    }
}
