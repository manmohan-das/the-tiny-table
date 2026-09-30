package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.OrderItem;
import util.DBConnection;

public class OrderItemDAO {

    // 1. GET ALL ORDER ITEMS
    public List<OrderItem> getAllOrderItems() {

        List<OrderItem> orderItems = new ArrayList<>();

        String sql = "SELECT * FROM order_items ORDER BY order_item_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                OrderItem item = new OrderItem();

                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setFoodId(rs.getInt("food_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));

                orderItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orderItems;
    }


    // 2. GET ORDER ITEM BY ID
    public OrderItem getOrderItemById(int orderItemId) {

        String sql = "SELECT * FROM order_items WHERE order_item_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, orderItemId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                OrderItem item = new OrderItem();

                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setFoodId(rs.getInt("food_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));

                return item;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // 3. GET ITEMS BY ORDER ID
    public List<OrderItem> getItemsByOrderId(int orderId) {

        List<OrderItem> orderItems = new ArrayList<>();

        String sql = "SELECT * FROM order_items " +
                     "WHERE order_id = ? ORDER BY order_item_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                OrderItem item = new OrderItem();

                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setFoodId(rs.getInt("food_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));

                orderItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orderItems;
    }


    // 4. ADD ORDER ITEM
    public boolean addOrderItem(OrderItem item) {

        String sql = "INSERT INTO order_items " +
                     "(order_id, food_id, quantity, price, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getFoodId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            ps.setBigDecimal(5, item.getSubtotal());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 5. UPDATE ORDER ITEM
    public boolean updateOrderItem(OrderItem item) {

        String sql = "UPDATE order_items SET " +
                     "order_id = ?, food_id = ?, quantity = ?, " +
                     "price = ?, subtotal = ? " +
                     "WHERE order_item_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getFoodId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            ps.setBigDecimal(5, item.getSubtotal());
            ps.setInt(6, item.getOrderItemId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 6. DELETE ORDER ITEM
    public boolean deleteOrderItem(int orderItemId) {

        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, orderItemId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}