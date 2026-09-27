package model;

import java.math.BigDecimal;

public class FoodItem {

    private int foodId;
    private int categoryId;
    private String foodName;
    private BigDecimal price;
    private String image;
    private int available;
    private int availableQty;

    // Parameterized Constructor
    public FoodItem(int foodId, int categoryId, String foodName,
                    BigDecimal price, String image,
                    int available, int availableQty) {

        this.foodId = foodId;
        this.categoryId = categoryId;
        this.foodName = foodName;
        this.price = price;
        this.image = image;
        this.available = available;
        this.availableQty = availableQty;
    }

    // Default Constructor
    public FoodItem() {
    }

    // Food ID
    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    // Category ID
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    // Food Name
    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    // Price
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Image
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Available
    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    // Available Quantity
    public int getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(int availableQty) {
        this.availableQty = availableQty;
    }
}