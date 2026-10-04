package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import model.ChefOrder;
import model.ChefOrderItem;
import model.Order;
import util.DBConnection;

public class OrderDAO {

    // 1. GET ALL ORDERS
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id DESC";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // 2. GET RECENT ORDERS
    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_id DESC LIMIT ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // 3. GET ORDER BY ID
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. ADD ORDER
    public boolean addOrder(Order order) {
        String sql = "INSERT INTO orders " +
                "(employee_id, customer_name, customer_phone, order_date, " +
                "subtotal, discount, tax, total_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getEmployeeId());
            ps.setString(2, order.getCustomerName());
            ps.setString(3, order.getCustomerPhone());
            ps.setTimestamp(4, order.getOrderDate());
            ps.setBigDecimal(5, order.getSubtotal());
            ps.setBigDecimal(6, order.getDiscount());
            ps.setBigDecimal(7, order.getTax());
            ps.setBigDecimal(8, order.getTotalAmount());
            ps.setString(9, order.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 5. UPDATE ORDER
    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET " +
                "employee_id = ?, customer_name = ?, customer_phone = ?, " +
                "order_date = ?, subtotal = ?, discount = ?, tax = ?, " +
                "total_amount = ?, status = ? WHERE order_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getEmployeeId());
            ps.setString(2, order.getCustomerName());
            ps.setString(3, order.getCustomerPhone());
            ps.setTimestamp(4, order.getOrderDate());
            ps.setBigDecimal(5, order.getSubtotal());
            ps.setBigDecimal(6, order.getDiscount());
            ps.setBigDecimal(7, order.getTax());
            ps.setBigDecimal(8, order.getTotalAmount());
            ps.setString(9, order.getStatus());
            ps.setInt(10, order.getOrderId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 6. UPDATE ORDER STATUS
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 7. TODAY'S ORDER COUNT
    public int getTodayOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURDATE()";
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 8. ACTIVE EMPLOYEE / KITCHEN STAFF COUNT
    public int getActiveEmployeeCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 1 " +
                "AND LOWER(role) IN ('employee', 'kitchen staff', 'kitchen_staff')";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 9. TODAY'S SALES
    public BigDecimal getTodaySales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(order_date) = CURDATE()";
        return getAmount(sql);
    }

    // 10. CURRENT MONTH REVENUE
    public BigDecimal getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders " +
                "WHERE YEAR(order_date) = YEAR(CURDATE()) " +
                "AND MONTH(order_date) = MONTH(CURDATE())";
        return getAmount(sql);
    }

    // 11. ITEM COUNT FOR ONE ORDER
    public int getItemCountByOrderId(int orderId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE order_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 12. LAST 7 DAYS SALES - MONDAY TO SUNDAY
    public BigDecimal[] getWeeklySales() {
        BigDecimal[] sales = new BigDecimal[7];
        for (int i = 0; i < 7; i++) {
            sales[i] = BigDecimal.ZERO;
        }

        String sql = "SELECT DAYOFWEEK(order_date) AS day_no, " +
                "COALESCE(SUM(total_amount), 0) AS total " +
                "FROM orders " +
                "WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "AND order_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY) " +
                "GROUP BY DAYOFWEEK(order_date)";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int mysqlDay = rs.getInt("day_no");
                int index = (mysqlDay + 5) % 7;
                sales[index] = rs.getBigDecimal("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sales;
    }

    // COMMON ORDER MAPPER
    private Order mapOrder(ResultSet rs) throws Exception {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setEmployeeId(rs.getInt("employee_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        return order;
    }

    private BigDecimal getAmount(String sql) {
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal amount = rs.getBigDecimal(1);
                return amount == null ? BigDecimal.ZERO : amount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    // ==========================================
    // CHEF ORDERS
    // ==========================================

    public List<ChefOrder> getTodayChefOrders() {
        List<ChefOrder> orders = new ArrayList<>();
        String sql = "SELECT " +
                "o.order_id, o.customer_name, o.customer_phone, o.order_date, o.status, o.note, " +
                "oi.food_id, oi.quantity, " +
                "f.food_name " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON oi.order_id = o.order_id " +
                "LEFT JOIN food_items f ON f.food_id = oi.food_id " +
                "WHERE DATE(o.order_date) = CURDATE() " +
                "AND o.status NOT IN ('COMPLETED', 'CANCELLED') " +
                "ORDER BY o.order_id ASC, oi.order_item_id ASC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            LinkedHashMap<Integer, ChefOrder> orderMap = new LinkedHashMap<>();

            while (result.next()) {
                int orderNo = result.getInt("order_id");
                ChefOrder order = orderMap.get(orderNo);

                if (order == null) {
                    order = new ChefOrder();
                    order.orderNo = orderNo;
                    String cName = result.getString("customer_name");
                    order.customer_name = (cName == null || cName.trim().isEmpty()) ? "Walk-in Guest" : cName.trim();
                    String cPhone = result.getString("customer_phone");
                    order.customer_phone = (cPhone == null || cPhone.trim().isEmpty()) ? "" : cPhone.trim();

                    java.sql.Timestamp timestamp = result.getTimestamp("order_date");
                    order.order_date = (timestamp != null)
                            ? timestamp.toLocalDateTime()
                            : LocalDateTime.now();

                    String status = result.getString("status");
                    order.status = (status == null || status.trim().isEmpty()) ? "PLACED" : status.trim();

                    String note = result.getString("note");
                    order.note = (note == null || note.trim().isEmpty()) ? "" : note.trim();

                    order.items = new java.util.LinkedList<>();
                    orderMap.put(orderNo, order);
                }

                String foodName = result.getString("food_name");
                int quantity = result.getInt("quantity");

                if (foodName != null && !foodName.trim().isEmpty() && quantity > 0) {
                    order.items.add(new ChefOrderItem(foodName, quantity));
                }
            }

            orders.addAll(orderMap.values());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orders;
    }
}