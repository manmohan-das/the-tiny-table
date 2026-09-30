package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import util.DBConnection;

public class OrderDAO {

    // 1. GET ALL ORDERS
    public List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM orders ORDER BY order_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

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

                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }


    // 2. GET ORDER BY ID
    public Order getOrderById(int orderId) {

        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // 3. ADD ORDER
    public boolean addOrder(Order order) {

        String sql = "INSERT INTO orders " +
                     "(employee_id, customer_name, customer_phone, order_date, " +
                     "subtotal, discount, tax, total_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

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


    // 4. UPDATE ORDER
    public boolean updateOrder(Order order) {

        String sql = "UPDATE orders SET " +
                     "employee_id = ?, customer_name = ?, customer_phone = ?, " +
                     "order_date = ?, subtotal = ?, discount = ?, tax = ?, " +
                     "total_amount = ?, status = ? " +
                     "WHERE order_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

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


    // 5. UPDATE ORDER STATUS
    public boolean updateOrderStatus(int orderId, String status) {

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}