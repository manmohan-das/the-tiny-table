package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import util.DBConnection;

/**
 * DAO for all Employee Panel database operations.
 * Previously lived as OrderDAO.EmployeePanelDB (nested static class).
 */
public class EmployeePanelDAO {

    // =========================================================
    // DATABASE CONNECTION
    // =========================================================

    public static Connection connect() throws SQLException {
        Connection c = DBConnection.getConnection();

        if (c == null) {
            throw new SQLException("Database connection returned null.");
        }

        return c;
    }

    // =========================================================
    // GET TODAY'S LAST ORDER ID
    // =========================================================

    public static int nextDailyOrderNo() {

        String sql = "SELECT COALESCE(MAX(order_id), 0) " +
                "FROM orders " +
                "WHERE DATE(order_date) = CURDATE()";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            return r.next() ? r.getInt(1) : 0;

        } catch (SQLException e) {
            return 0;
        }
    }

    // =========================================================
    // FIND EMPLOYEE ID
    // =========================================================

    public static int findEmployeeId(
            Connection c,
            String employeeName) throws SQLException {

        String sql = "SELECT user_id FROM users " +
                "WHERE name=? AND role='employee' " +
                "LIMIT 1";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, employeeName);

            try (ResultSet r = p.executeQuery()) {

                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }

        // If employee name was not found,
        // use the first active employee.
        sql = "SELECT user_id FROM users " +
                "WHERE role='employee' AND status=1 " +
                "ORDER BY user_id " +
                "LIMIT 1";

        try (
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            if (r.next()) {
                return r.getInt(1);
            }
        }

        throw new SQLException(
                "No active employee found in users table.");
    }

    // =========================================================
    // SAVE COMPLETE ORDER
    // =========================================================

    public static void save(
            employeePanel.MenuAndOrderTable.Order o) {

        try (Connection c = connect()) {

            c.setAutoCommit(false);

            try {

                int employeeId = findEmployeeId(c, o.employee);

                int orderId = saveOrder(c, o, employeeId);

                saveOrderItems(c, o, orderId);

                savePayment(c, o, orderId);

                c.commit();

                o.number = orderId;
                o.status = "Pending";

            } catch (SQLException e) {

                c.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Save order error: "
                            + e.getMessage());
        }
    }

    // =========================================================
    // SAVE ORDER
    // =========================================================

    private static int saveOrder(
            Connection c,
            employeePanel.MenuAndOrderTable.Order o,
            int employeeId) throws SQLException {

        String sql = "INSERT INTO orders " +
                "(employee_id, customer_name, customer_phone, " +
                "order_date, subtotal, discount, tax, total_amount, " +
                "status, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                PreparedStatement p = c.prepareStatement(
                        sql,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

            p.setInt(1, employeeId);
            p.setString(2, o.customer);

            p.setString(
                    3,
                    o.phone == null || o.phone.isEmpty()
                            ? null
                            : o.phone);

            p.setTimestamp(
                    4,
                    java.sql.Timestamp.valueOf(o.createdAt));

            p.setDouble(5, o.subtotal);
            p.setDouble(6, o.discount);
            p.setDouble(7, o.gst);
            p.setDouble(8, o.total);

            p.setString(9, "Pending");
            p.setString(10, o.notes);

            p.executeUpdate();

            try (ResultSet keys = p.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Unable to create order.");
    }

    // =========================================================
    // SAVE ORDER ITEMS
    // =========================================================

    private static void saveOrderItems(
            Connection c,
            employeePanel.MenuAndOrderTable.Order o,
            int orderId) throws SQLException {

        String sql = "INSERT INTO order_items " +
                "(order_id, food_id, quantity, price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            for (Map.Entry<employeePanel.MenuAndOrderTable.Food, Integer> entry : o.items.entrySet()) {

                int foodId = getFoodId(
                        c,
                        entry.getKey().name);

                if (foodId <= 0) {
                    continue;
                }

                int quantity = entry.getValue();

                p.setInt(1, orderId);
                p.setInt(2, foodId);
                p.setInt(3, quantity);
                p.setDouble(
                        4,
                        entry.getKey().price);
                p.setDouble(
                        5,
                        entry.getKey().price * quantity);

                p.addBatch();
            }

            p.executeBatch();
        }
    }

    // =========================================================
    // SAVE PAYMENT
    // =========================================================

    private static void savePayment(
            Connection c,
            employeePanel.MenuAndOrderTable.Order o,
            int orderId) throws SQLException {

        String sql = "INSERT INTO payments " +
                "(order_id, amount, payment_method, " +
                "payment_status, payment_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, orderId);
            p.setDouble(2, o.total);
            p.setString(3, o.paymentMethod);
            p.setString(4, "Paid");

            p.setTimestamp(
                    5,
                    java.sql.Timestamp.valueOf(
                            o.createdAt));

            p.executeUpdate();
        }
    }

    // =========================================================
    // GET FOOD ID
    // =========================================================

    public static int getFoodId(
            Connection c,
            String name) throws SQLException {

        String sql = "SELECT food_id " +
                "FROM food_items " +
                "WHERE food_name=? " +
                "LIMIT 1";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, name);

            try (ResultSet r = p.executeQuery()) {

                return r.next()
                        ? r.getInt(1)
                        : 0;
            }
        }
    }

    // =========================================================
    // UPDATE ORDER
    // =========================================================

    public static void update(
            employeePanel.MenuAndOrderTable.Order o) {

        String sql = "UPDATE orders SET " +
                "customer_name=?, " +
                "customer_phone=?, " +
                "status=?, " +
                "subtotal=?, " +
                "discount=?, " +
                "tax=?, " +
                "total_amount=?, " +
                "note=? " +
                "WHERE order_id=?";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, o.customer);

            p.setString(
                    2,
                    o.phone == null || o.phone.isEmpty()
                            ? null
                            : o.phone);

            p.setString(
                    3,
                    toDbStatus(o.status));

            p.setDouble(4, o.subtotal);
            p.setDouble(5, o.discount);
            p.setDouble(6, o.gst);
            p.setDouble(7, o.total);
            p.setString(8, o.notes);
            p.setInt(9, o.number);

            p.executeUpdate();

        } catch (SQLException e) {

            System.out.println(
                    "Update order error: "
                            + e.getMessage());
        }
    }

    // =========================================================
    // STATUS CONVERSION
    // =========================================================

    public static String toDbStatus(String status) {

        if (status == null) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("PLACED")) {
            return "Pending";
        }

        if (status.equalsIgnoreCase("PREPARING")) {
            return "Preparing";
        }

        if (status.equalsIgnoreCase("READY")) {
            return "Ready";
        }

        if (status.equalsIgnoreCase("COMPLETED")) {
            return "Completed";
        }

        if (status.equalsIgnoreCase("CANCELLED")) {
            return "Cancelled";
        }

        return status;
    }

    public static void updateStatus(
            employeePanel.MenuAndOrderTable.Order o) {

        update(o);
    }

    // =========================================================
    // DELETE ORDER
    // =========================================================

    public static void delete(
            employeePanel.MenuAndOrderTable.Order o) {

        deleteOrderFull(o.number);
    }

    public static void deleteOrderFull(
            int orderId) {

        try (Connection c = connect()) {

            deletePayment(c, orderId);
            deleteOrderItems(c, orderId);
            deleteOrder(c, orderId);

        } catch (SQLException e) {

            System.out.println(
                    "Delete order error: "
                            + e.getMessage());
        }
    }

    private static void deletePayment(
            Connection c,
            int orderId) throws SQLException {

        String sql = "DELETE FROM payments " +
                "WHERE order_id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, orderId);
            p.executeUpdate();
        }
    }

    private static void deleteOrderItems(
            Connection c,
            int orderId) throws SQLException {

        String sql = "DELETE FROM order_items " +
                "WHERE order_id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, orderId);
            p.executeUpdate();
        }
    }

    private static void deleteOrder(
            Connection c,
            int orderId) throws SQLException {

        String sql = "DELETE FROM orders " +
                "WHERE order_id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, orderId);
            p.executeUpdate();
        }
    }

    // =========================================================
    // LOAD TODAY'S ORDERS FOR EMPLOYEE
    // =========================================================

    public static void loadInto(
            java.util.List<employeePanel.MenuAndOrderTable.Order> target,
            String employee,
            employeePanel.MenuAndOrderTable.Food[] allFood) {

        String sql = "SELECT o.order_id, o.customer_name, " +
                "o.customer_phone, o.order_date, " +
                "o.subtotal, o.discount, o.tax, " +
                "o.total_amount, o.status, o.note, " +
                "u.name AS employee_name " +
                "FROM orders o " +
                "JOIN users u ON u.user_id=o.employee_id " +
                "WHERE DATE(o.order_date)=CURDATE() " +
                "AND u.name=? " +
                "ORDER BY o.order_id";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, employee);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    int id = r.getInt("order_id");

                    Map<employeePanel.MenuAndOrderTable.Food, Integer> items = loadItems(
                            c,
                            id,
                            allFood);

                    java.time.LocalDateTime dateTime = r.getTimestamp(
                            "order_date")
                            .toLocalDateTime();

                    employeePanel.MenuAndOrderTable.Order order = new employeePanel.MenuAndOrderTable.Order(
                            id,
                            r.getString("employee_name"),
                            r.getString("customer_name"),
                            r.getString("customer_phone"),
                            items,
                            r.getDouble("subtotal"),
                            r.getDouble("discount"),
                            r.getDouble("tax"),
                            r.getDouble("total_amount"),
                            r.getString("note"),
                            dateTime);

                    order.status = r.getString("status");

                    target.add(order);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Load orders error: "
                            + e.getMessage());
        }
    }

    // =========================================================
    // LOAD ORDER ITEMS
    // =========================================================

    public static Map<employeePanel.MenuAndOrderTable.Food, Integer> loadItems(
            Connection c,
            int orderId,
            employeePanel.MenuAndOrderTable.Food[] allFood)
            throws SQLException {

        Map<employeePanel.MenuAndOrderTable.Food, Integer> items = new java.util.LinkedHashMap<>();

        String sql = "SELECT oi.quantity, " +
                "f.food_name, f.price, f.image, " +
                "c.category_name " +
                "FROM order_items oi " +
                "JOIN food_items f ON f.food_id=oi.food_id " +
                "JOIN categories c ON c.category_id=f.category_id " +
                "WHERE oi.order_id=? " +
                "ORDER BY oi.order_item_id";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, orderId);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    employeePanel.MenuAndOrderTable.Food food = findFood(
                            r.getString("food_name"),
                            allFood);

                    if (food == null) {

                        food = new employeePanel.MenuAndOrderTable.Food(
                                r.getString("food_name"),
                                r.getString("category_name"),
                                "",
                                r.getDouble("price"),
                                r.getString("image"));
                    }

                    items.put(
                            food,
                            r.getInt("quantity"));
                }
            }
        }

        return items;
    }

    // =========================================================
    // FIND FOOD IN MENU
    // =========================================================

    public static employeePanel.MenuAndOrderTable.Food findFood(
            String name,
            employeePanel.MenuAndOrderTable.Food[] allFood) {

        for (employeePanel.MenuAndOrderTable.Food food : allFood) {

            if (food.name.equals(name)) {
                return food;
            }
        }

        return null;
    }

    // =========================================================
    // LOAD AVAILABLE FOOD
    // =========================================================

    public static java.util.List<employeePanel.MenuAndOrderTable.Food> loadFoodFromDatabase() {

        java.util.List<employeePanel.MenuAndOrderTable.Food> list = new java.util.ArrayList<>();

        String sql = "SELECT f.food_id, f.food_name, f.price, " +
                "f.image, f.available, c.category_name " +
                "FROM food_items f " +
                "JOIN categories c " +
                "ON c.category_id=f.category_id " +
                "WHERE f.available=1 " +
                "ORDER BY c.category_name, f.food_id";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {

            while (r.next()) {

                list.add(
                        new employeePanel.MenuAndOrderTable.Food(
                                r.getString("food_name"),
                                r.getString("category_name"),
                                "",
                                r.getDouble("price"),
                                r.getString("image")));
            }

        } catch (SQLException e) {

            System.out.println(
                    "Unable to load food menu: "
                            + e.getMessage());
        }

        return list;
    }

    // =========================================================
    // LOAD CATEGORY NAMES
    // =========================================================

    public static java.util.List<String> loadCategoryNames() {

        java.util.List<String> names = new java.util.ArrayList<>();

        String sql = "SELECT category_name " +
                "FROM categories " +
                "ORDER BY category_name";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {

            while (r.next()) {
                names.add(r.getString(1));
            }

        } catch (SQLException e) {

            System.out.println(
                    "Category load error: "
                            + e.getMessage());
        }

        return names;
    }

    // =========================================================
    // LOAD ALL TODAY'S ORDERS
    // =========================================================

    public static void loadAllTodaysOrders(
            Map<Integer, employeePanel.MenuAndOrderTable.Order> grouped,
            employeePanel.MenuAndOrderTable.Food[] allFood) {

        String sql = "SELECT o.order_id, o.customer_name, " +
                "o.customer_phone, o.order_date, " +
                "o.subtotal, o.discount, o.tax, " +
                "o.total_amount, o.status, o.note, " +
                "u.name AS employee_name, " +
                "oi.order_item_id, oi.quantity, " +
                "f.food_name, f.price, f.image, " +
                "c.category_name " +
                "FROM orders o " +
                "JOIN users u ON u.user_id=o.employee_id " +
                "LEFT JOIN order_items oi " +
                "ON oi.order_id=o.order_id " +
                "LEFT JOIN food_items f " +
                "ON f.food_id=oi.food_id " +
                "LEFT JOIN categories c " +
                "ON c.category_id=f.category_id " +
                "WHERE DATE(o.order_date)=CURDATE() " +
                "ORDER BY o.order_id ASC, oi.order_item_id ASC";

        try (
                Connection c = connect();
                PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {

            while (r.next()) {

                int orderId = r.getInt("order_id");

                employeePanel.MenuAndOrderTable.Order order = grouped.get(orderId);

                if (order == null) {

                    java.sql.Timestamp timestamp = r.getTimestamp("order_date");

                    java.time.LocalDateTime dateTime = timestamp == null
                            ? java.time.LocalDateTime.now()
                            : timestamp.toLocalDateTime();

                    order = new employeePanel.MenuAndOrderTable.Order(
                            orderId,
                            r.getString("employee_name"),
                            r.getString("customer_name"),
                            r.getString("customer_phone"),
                            new java.util.LinkedHashMap<>(),
                            r.getDouble("subtotal"),
                            r.getDouble("discount"),
                            r.getDouble("tax"),
                            r.getDouble("total_amount"),
                            r.getString("note"),
                            dateTime);

                    order.status = r.getString("status");

                    grouped.put(orderId, order);
                }

                String foodName = r.getString("food_name");

                int quantity = r.getInt("quantity");

                if (foodName != null
                        && !foodName.trim().isEmpty()
                        && quantity > 0) {

                    employeePanel.MenuAndOrderTable.Food food = findFood(
                            foodName,
                            allFood);

                    if (food == null) {

                        food = new employeePanel.MenuAndOrderTable.Food(
                                foodName,
                                r.getString("category_name"),
                                "",
                                r.getDouble("price"),
                                r.getString("image"));
                    }

                    order.items.put(
                            food,
                            quantity);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // UPDATE ORDER + ITEMS (FULL REPLACE)
    // =========================================================

    public static boolean updateOrderAndItemsFull(
            employeePanel.MenuAndOrderTable.Order o) {

        try (Connection c = connect()) {

            c.setAutoCommit(false);

            try {

                updateOrderDetails(c, o);

                deleteOrderItems(c, o.number);

                insertUpdatedOrderItems(c, o);

                c.commit();

                return true;

            } catch (SQLException e) {

                c.rollback();

                throw e;
            }

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // UPDATE ORDER DETAILS
    // =========================================================

    private static void updateOrderDetails(
            Connection c,
            employeePanel.MenuAndOrderTable.Order o)
            throws SQLException {

        String sql = "UPDATE orders SET " +
                "customer_name=?, " +
                "customer_phone=?, " +
                "subtotal=?, " +
                "discount=?, " +
                "tax=?, " +
                "total_amount=?, " +
                "note=? " +
                "WHERE order_id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, o.customer);

            p.setString(
                    2,
                    o.phone == null || o.phone.isEmpty()
                            ? null
                            : o.phone);

            p.setDouble(3, o.subtotal);
            p.setDouble(4, o.discount);
            p.setDouble(5, o.gst);
            p.setDouble(6, o.total);
            p.setString(7, o.notes);
            p.setInt(8, o.number);

            p.executeUpdate();
        }
    }

    // =========================================================
    // INSERT UPDATED ORDER ITEMS
    // =========================================================

    private static void insertUpdatedOrderItems(
            Connection c,
            employeePanel.MenuAndOrderTable.Order o)
            throws SQLException {

        String findFoodSql = "SELECT food_id FROM food_items " +
                "WHERE food_name=? LIMIT 1";

        String insertSql = "INSERT INTO order_items " +
                "(order_id, food_id, quantity, price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (
                PreparedStatement foodStmt = c.prepareStatement(findFoodSql);
                PreparedStatement itemStmt = c.prepareStatement(insertSql)) {

            for (Map.Entry<employeePanel.MenuAndOrderTable.Food, Integer> entry : o.items.entrySet()) {

                foodStmt.setString(
                        1,
                        entry.getKey().name);

                int foodId = 0;

                try (ResultSet r = foodStmt.executeQuery()) {

                    if (r.next()) {
                        foodId = r.getInt(1);
                    }
                }

                if (foodId <= 0) {
                    continue;
                }

                int quantity = entry.getValue();

                double price = entry.getKey().price;

                itemStmt.setInt(
                        1,
                        o.number);

                itemStmt.setInt(
                        2,
                        foodId);

                itemStmt.setInt(
                        3,
                        quantity);

                itemStmt.setDouble(
                        4,
                        price);

                itemStmt.setDouble(
                        5,
                        price * quantity);

                itemStmt.addBatch();
            }

            itemStmt.executeBatch();
        }
    }
}