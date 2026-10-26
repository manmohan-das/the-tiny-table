package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.FoodItem;
import util.DBConnection;

public class FoodItemDAO {

    // 1. GET ALL FOOD ITEMS
    public List<FoodItem> getAllFoodItems() {

        List<FoodItem> foodItems = new ArrayList<>();

        String sql = "SELECT * FROM food_items ORDER BY food_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                FoodItem food = new FoodItem();

                food.setFoodId(rs.getInt("food_id"));
                food.setCategoryId(rs.getInt("category_id"));
                food.setFoodName(rs.getString("food_name"));
                food.setFoodType(getFoodType(rs));
                food.setPrice(rs.getBigDecimal("price"));
                food.setImage(rs.getString("image"));
                food.setAvailable(rs.getInt("available"));
                food.setAvailableQty(rs.getInt("available_qty"));

                foodItems.add(food);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return foodItems;
    }


    // 2. GET FOOD ITEM BY ID
    public FoodItem getFoodItemById(int foodId) {

        String sql = "SELECT * FROM food_items WHERE food_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, foodId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                FoodItem food = new FoodItem();

                food.setFoodId(rs.getInt("food_id"));
                food.setCategoryId(rs.getInt("category_id"));
                food.setFoodName(rs.getString("food_name"));
                food.setFoodType(getFoodType(rs));
                food.setPrice(rs.getBigDecimal("price"));
                food.setImage(rs.getString("image"));
                food.setAvailable(rs.getInt("available"));
                food.setAvailableQty(rs.getInt("available_qty"));

                return food;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getFoodType(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int column = 1; column <= metadata.getColumnCount(); column++) {
            if ("food_type".equalsIgnoreCase(metadata.getColumnLabel(column))) {
                String foodType = rs.getString(column);
                return foodType == null ? "Veg" : foodType;
            }
        }
        return "Veg";
    }


    // 3. ADD FOOD ITEM
    public boolean addFoodItem(FoodItem food) {

        try (Connection con = DBConnection.getConnection()) {
            ensureFoodTypeColumn(con);

            String sql = "INSERT INTO food_items " +
                         "(category_id, food_name, food_type, price, image, available, available_qty) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, food.getCategoryId());
                ps.setString(2, food.getFoodName());
                ps.setString(3, food.getFoodType());
                ps.setBigDecimal(4, food.getPrice());
                ps.setString(5, food.getImage());
                ps.setInt(6, food.getAvailable());
                ps.setInt(7, food.getAvailableQty());

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 4. UPDATE FOOD ITEM
    public boolean updateFoodItem(FoodItem food) {

        try (Connection con = DBConnection.getConnection()) {
            ensureFoodTypeColumn(con);
            String sql = "UPDATE food_items SET " +
                         "category_id = ?, food_name = ?, food_type = ?, price = ?, " +
                         "image = ?, available = ?, available_qty = ? " +
                         "WHERE food_id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, food.getCategoryId());
                ps.setString(2, food.getFoodName());
                ps.setString(3, food.getFoodType());
                ps.setBigDecimal(4, food.getPrice());
                ps.setString(5, food.getImage());
                ps.setInt(6, food.getAvailable());
                ps.setInt(7, food.getAvailableQty());
                ps.setInt(8, food.getFoodId());

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void ensureFoodTypeColumn(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM food_items WHERE 1 = 0");
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                if ("food_type".equalsIgnoreCase(metadata.getColumnLabel(column))) {
                    return;
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "ALTER TABLE food_items ADD COLUMN food_type " +
                        "ENUM('Veg', 'Non-Veg') NOT NULL DEFAULT 'Veg' AFTER food_name")) {
            ps.executeUpdate();
        }
    }


    // 5. DELETE FOOD ITEM
    public boolean deleteFoodItem(int foodId) {

        String sql = "DELETE FROM food_items WHERE food_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, foodId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}