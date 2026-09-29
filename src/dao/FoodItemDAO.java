package dao;

import model.FoodItem;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public List<FoodItem> getAllFoodItems() {

        List<FoodItem> foodItems = new ArrayList<>();

        String sql = "SELECT * FROM food_items ORDER BY food_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {

            while (rs.next()) {

                FoodItem food = new FoodItem();

                food.setFoodId(rs.getInt("food_id"));
                food.setCategoryId(rs.getInt("category_id"));
                food.setFoodName(rs.getString("food_name"));
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
}