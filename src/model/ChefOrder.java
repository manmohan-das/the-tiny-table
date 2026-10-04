package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

/**
 * Represents a single order displayed on the Chef Panel.
 * Contains only the fields the chef needs: order number, customer info,
 * status, note, time, and the list of items to prepare.
 */
public class ChefOrder {

    public int orderNo;
    public String customer_name = "";
    public String customer_phone = "";
    public String status = "";
    public String note = "";
    public LocalDateTime order_date = LocalDateTime.now();
    public LinkedList<ChefOrderItem> items = new LinkedList<>();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    public String getTime() {
        return order_date.format(TIME_FORMAT);
    }
}