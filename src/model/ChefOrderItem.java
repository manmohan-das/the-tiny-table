package model;

/**
 * Represents a single food item (name + quantity) inside a ChefOrder.
 * Used by the Chef Panel to display what needs to be prepared.
 */
public class ChefOrderItem {

    public String name;
    public int quantity;

    public ChefOrderItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}