
package managerdashboard.sidebarpages;

import dao.FoodItemDAO;
import dao.OrderDAO;
import dao.OrderItemDAO;
import model.FoodItem;
import model.Order;
import model.OrderItem;
import util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Orders extends JPanel {

    private static final Color VIEW_BUTTON_COLOR = new Color(255, 250, 242);
    private static final Color VIEW_BUTTON_HOVER_COLOR = new Color(232, 215, 190);
    private static final Color VIEW_BUTTON_TEXT_COLOR = new Color(105, 105, 105);

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final FoodItemDAO foodItemDAO = new FoodItemDAO();

    // Cache employee names to avoid repeatedly querying the database.
    private final Map<Integer, String> employeeNameCache = new HashMap<>();

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private int hoveredViewButtonRow = -1;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> dateFilter;

    private Date customStartDate;
    private Date customEndDate;

    private JLabel totalOrdersLabel;
    private JLabel completedLabel;
    private JLabel preparingLabel;
    private JLabel cancelledLabel;

    private List<Order> allOrders = new ArrayList<>();

    public Orders() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(250, 246, 239));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createComponents();
        loadOrderData();
    }

    // =========================================================
    // CREATE COMPONENTS
    // =========================================================

    private void createComponents() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Order Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(76, 58, 42));

        JLabel subtitle = new JLabel(
                "Manage and monitor restaurant orders");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 100, 85));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        // Summary cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);

        totalOrdersLabel = new JLabel("0");
        completedLabel = new JLabel("0");
        preparingLabel = new JLabel("0");
        cancelledLabel = new JLabel("0");

        cards.add(createCard("TOTAL ORDERS", totalOrdersLabel));
        cards.add(createCard("COMPLETED", completedLabel));
        cards.add(createCard("PREPARING", preparingLabel));
        cards.add(createCard("CANCELLED", cancelledLabel));

        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);

        topSection.add(header, BorderLayout.NORTH);
        topSection.add(cards, BorderLayout.CENTER);

        // Filter panel
        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 8));

        filterPanel.setBackground(new Color(255, 250, 242));
        filterPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(224, 214, 202)),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(91, 58, 38));

        searchField = new JTextField(18);
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setToolTipText(
                "Search order, customer, employee or phone");

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }
                });

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(91, 58, 38));

        statusFilter = new JComboBox<>(new String[] {
                "All Status",
                "Pending",
                "Preparing",
                "Ready",
                "Completed",
                "Cancelled"
        });

        statusFilter.setPreferredSize(new Dimension(125, 30));
        statusFilter.addActionListener(e -> applyFilters());

        JLabel dateLabel = new JLabel("Period:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(91, 58, 38));

        dateFilter = new JComboBox<>(new String[] {
                "Today",
                "This Month",
                "This Year",
                "Custom Range",
                "All Time"
        });

        dateFilter.setPreferredSize(new Dimension(135, 30));
        dateFilter.setSelectedItem("Today");

        dateFilter.addActionListener(e -> {

            if ("Custom Range".equals(dateFilter.getSelectedItem())) {

                if (!showCustomDateRangeDialog()) {
                    dateFilter.setSelectedItem("Today");
                    return;
                }
            }

            applyFilters();
        });

        JButton refreshButton = new JButton("⟳ Refresh");
        refreshButton.setPreferredSize(new Dimension(105, 30));
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setBackground(new Color(104, 70, 48));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        refreshButton.addActionListener(e -> {
            employeeNameCache.clear();
            loadOrderData();
        });

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(dateLabel);
        filterPanel.add(dateFilter);
        filterPanel.add(refreshButton);

        JPanel tableContainer = createTablePanel();

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        center.add(filterPanel, BorderLayout.NORTH);
        center.add(tableContainer, BorderLayout.CENTER);

        main.add(topSection, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    // =========================================================
    // CREATE ORDER TABLE
    // =========================================================

    private JPanel createTablePanel() {

        String[] columns = {
                "Order ID",
                "Customer Name",
                "Customer Phone",
                "Employee Name",
                "Order Date",
                "Total Amount",
                "Status",
                "Action"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the View button column is editable.
                return column == 7;
            }
        };

        orderTable = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);
        orderTable.setRowSorter(sorter);

        orderTable.setRowHeight(30);
        orderTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        orderTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        orderTable.getTableHeader().setBackground(
                new Color(104, 70, 48));

        orderTable.getTableHeader().setForeground(Color.WHITE);
        orderTable.setSelectionBackground(
                new Color(238, 225, 211));

        orderTable.setGridColor(
                new Color(230, 220, 210));

        // Column widths
        int[] widths = {
                70, 150, 120, 150, 145, 110, 95, 80
        };

        for (int i = 0; i < widths.length; i++) {
            orderTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // View button renderer
        orderTable.getColumnModel()
                .getColumn(7)
                .setCellRenderer(new ViewButtonRenderer());

        // View button editor
        orderTable.getColumnModel()
                .getColumn(7)
                .setCellEditor(new ViewButtonEditor(orderTable));

        orderTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = orderTable.rowAtPoint(e.getPoint());
                int column = orderTable.columnAtPoint(e.getPoint());
                int hoveredRow = column == 7 ? row : -1;

                if (hoveredViewButtonRow != hoveredRow) {
                    hoveredViewButtonRow = hoveredRow;
                    orderTable.repaint();
                }
            }
        });

        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredViewButtonRow = -1;
                orderTable.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 214, 202)));

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        bottom.setOpaque(false);

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(
                e -> updateSelectedOrderStatus());

        bottom.add(updateButton);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // SUMMARY CARD
    // =========================================================

    private JPanel createCard(String title, JLabel valueLabel) {

        JPanel card = new JPanel(new BorderLayout(0, 5));

        card.setBackground(new Color(255, 250, 242));
        card.setPreferredSize(new Dimension(0, 78));

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(224, 214, 202)),
                        BorderFactory.createEmptyBorder(
                                10, 16, 10, 16)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(
                new Color(125, 105, 90));

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(
                new Color(91, 58, 38));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // LOAD ORDERS
    // =========================================================

    private void loadOrderData() {

        allOrders = orderDAO.getAllOrders();

        if (allOrders == null) {
            allOrders = new ArrayList<>();
        }

        tableModel.setRowCount(0);

        for (Order order : allOrders) {
            addOrderToTable(order);
        }

        updateSummary();
        applyFilters();
    }

    private void addOrderToTable(Order order) {

        tableModel.addRow(new Object[] {
                order.getOrderId(),
                order.getCustomerName(),
                order.getCustomerPhone(),

                // Display employee name instead of employee ID.
                getEmployeeName(order.getEmployeeId()),

                formatDate(order.getOrderDate()),
                money(order.getTotalAmount()),
                order.getStatus(),
                "View"
        });
    }

    // =========================================================
    // GET EMPLOYEE NAME FROM USERS TABLE
    // =========================================================

    private String getEmployeeName(int employeeId) {

        if (employeeNameCache.containsKey(employeeId)) {
            return employeeNameCache.get(employeeId);
        }

        String employeeName = "Unknown Employee";

        String sql = "SELECT name FROM users WHERE user_id = ?";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String name = rs.getString("name");

                    if (name != null && !name.trim().isEmpty()) {
                        employeeName = name;
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            employeeName = "Employee #" + employeeId;
        }

        employeeNameCache.put(employeeId, employeeName);

        return employeeName;
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    private void updateSelectedOrderStatus() {

        int viewRow = orderTable.getSelectedRow();

        if (viewRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an order first.",
                    "Select Order",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        int modelRow = orderTable.convertRowIndexToModel(viewRow);

        int orderId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString());

        String currentStatus = String.valueOf(
                tableModel.getValueAt(modelRow, 6));

        String[] statuses = {
                "Pending",
                "Preparing",
                "Ready",
                "Completed",
                "Cancelled"
        };

        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status:",
                "Update Order Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                statuses,
                currentStatus);

        if (newStatus == null || newStatus.equals(currentStatus)) {
            return;
        }

        boolean success = orderDAO.updateOrderStatus(
                orderId,
                newStatus);

        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Order status updated successfully.");

            loadOrderData();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update order status.",
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // SHOW ORDER DETAILS
    // =========================================================

    private void showOrderDetails(int orderId) {

        Order order = orderDAO.getOrderById(orderId);

        if (order == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Order details could not be loaded.",
                    "Order Not Found",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        List<OrderItem> items = orderItemDAO.getItemsByOrderId(orderId);

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Order Details - #" + orderId,
                true);

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE);

        dialog.setSize(750, 600);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(
                new BorderLayout(12, 12));

        content.setBackground(
                new Color(250, 246, 239));

        content.setBorder(
                BorderFactory.createEmptyBorder(
                        16, 16, 16, 16));

        // Order information
        JPanel infoPanel = new JPanel(
                new GridLayout(0, 2, 10, 8));

        infoPanel.setOpaque(false);

        infoPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(224, 214, 202)),
                        BorderFactory.createEmptyBorder(
                                12, 12, 12, 12)));

        infoPanel.add(createDetailLabel(
                "Customer Name",
                order.getCustomerName()));

        infoPanel.add(createDetailLabel(
                "Phone",
                order.getCustomerPhone()));

        // Employee name replaces employee ID.
        infoPanel.add(createDetailLabel(
                "Employee Name",
                getEmployeeName(order.getEmployeeId())));

        infoPanel.add(createDetailLabel(
                "Status",
                order.getStatus()));

        infoPanel.add(createDetailLabel(
                "Order Date",
                formatDate(order.getOrderDate())));

        infoPanel.add(createDetailLabel(
                "Subtotal",
                money(order.getSubtotal())));

        infoPanel.add(createDetailLabel(
                "Discount",
                money(order.getDiscount())));

        infoPanel.add(createDetailLabel(
                "Tax",
                money(order.getTax())));

        infoPanel.add(createDetailLabel(
                "Total Amount",
                money(order.getTotalAmount())));

        content.add(infoPanel, BorderLayout.NORTH);

        // Ordered food items
        String[] columnNames = {
                "Item",
                "Qty",
                "Rate",
                "Subtotal"
        };

        DefaultTableModel detailModel = new DefaultTableModel(columnNames, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {
                return false;
            }
        };

        if (items != null && !items.isEmpty()) {

            for (OrderItem item : items) {

                FoodItem food = foodItemDAO.getFoodItemById(
                        item.getFoodId());

                String foodName = food != null
                        ? food.getFoodName()
                        : "Unknown Item";

                detailModel.addRow(new Object[] {
                        foodName,
                        item.getQuantity(),
                        money(item.getPrice()),
                        money(item.getSubtotal())
                });
            }

        } else {

            detailModel.addRow(new Object[] {
                    "No items found",
                    "-",
                    "-",
                    "-"
            });
        }

        JTable detailTable = new JTable(detailModel);

        detailTable.setRowHeight(28);
        detailTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        detailTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        detailTable.getTableHeader().setBackground(
                new Color(104, 70, 48));

        detailTable.getTableHeader().setForeground(
                Color.WHITE);

        detailTable.setSelectionBackground(
                new Color(238, 225, 211));

        JScrollPane tableScroll = new JScrollPane(detailTable);

        tableScroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 214, 202)));

        content.add(tableScroll, BorderLayout.CENTER);

        // Close button
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        footer.setOpaque(false);

        JButton closeButton = new JButton("Close");

        closeButton.setBackground(
                new Color(104, 70, 48));

        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);

        closeButton.addActionListener(
                e -> dialog.dispose());

        footer.add(closeButton);

        content.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // =========================================================
    // ORDER DETAIL LABEL
    // =========================================================

    private JPanel createDetailLabel(
            String title,
            String value) {

        JPanel panel = new JPanel(
                new BorderLayout(5, 0));

        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title + ":");

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        titleLabel.setForeground(
                new Color(91, 58, 38));

        JLabel valueLabel = new JLabel(
                value == null || value.trim().isEmpty()
                        ? "-"
                        : value);

        valueLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setForeground(
                new Color(76, 58, 42));

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // SEARCH AND FILTERS
    // =========================================================

    private void applyFilters() {

        if (sorter == null) {
            return;
        }

        String search = searchField.getText()
                .trim()
                .toLowerCase();

        String selectedStatus = String.valueOf(statusFilter.getSelectedItem());

        String selectedDate = String.valueOf(dateFilter.getSelectedItem());

        boolean searchOrStatusUsed = !search.isEmpty()
                || !"All Status".equals(selectedStatus);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Search all displayed columns.
        if (!search.isEmpty()) {

            filters.add(new RowFilter<Object, Object>() {

                @Override
                public boolean include(
                        Entry<?, ?> entry) {

                    for (int i = 0; i < entry.getValueCount(); i++) {

                        if (String.valueOf(entry.getValue(i))
                                .toLowerCase()
                                .contains(search)) {

                            return true;
                        }
                    }

                    return false;
                }
            });
        }

        // Status filter
        if (!"All Status".equals(selectedStatus)) {

            filters.add(new RowFilter<Object, Object>() {

                @Override
                public boolean include(
                        Entry<?, ?> entry) {

                    return selectedStatus.equalsIgnoreCase(
                            String.valueOf(entry.getValue(6)));
                }
            });
        }

        /*
         * Default: Today shows today's orders.
         *
         * If searching or filtering by status, older orders
         * can also be searched.
         *
         * Custom Range applies the selected date range.
         */

        boolean useToday = "Today".equals(selectedDate)
                && !searchOrStatusUsed;

        boolean useNormalDateFilter = ("This Month".equals(selectedDate)
                || "This Year".equals(selectedDate))
                && !searchOrStatusUsed;

        if (useToday || useNormalDateFilter) {

            String filter = useToday
                    ? "Today"
                    : selectedDate;

            filters.add(new RowFilter<Object, Object>() {

                @Override
                public boolean include(
                        Entry<?, ?> entry) {

                    int modelRow = (Integer) entry.getIdentifier();

                    if (modelRow < 0
                            || modelRow >= allOrders.size()) {
                        return false;
                    }

                    Order order = allOrders.get(modelRow);

                    return matchesDateFilter(
                            order.getOrderDate(),
                            filter);
                }
            });

        } else if ("Custom Range".equals(selectedDate)
                && customStartDate != null
                && customEndDate != null) {

            filters.add(new RowFilter<Object, Object>() {

                @Override
                public boolean include(
                        Entry<?, ?> entry) {

                    int modelRow = (Integer) entry.getIdentifier();

                    if (modelRow < 0
                            || modelRow >= allOrders.size()) {
                        return false;
                    }

                    Order order = allOrders.get(modelRow);

                    return matchesCustomDateRange(
                            order.getOrderDate());
                }
            });
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.andFilter(filters));
        }

        updateSummary();
    }

    // =========================================================
    // CUSTOM DATE RANGE
    // =========================================================

    private boolean showCustomDateRangeDialog() {

        JSpinner fromSpinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        Calendar.DAY_OF_MONTH));

        JSpinner toSpinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        Calendar.DAY_OF_MONTH));

        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(
                fromSpinner,
                "dd-MM-yyyy");

        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(
                toSpinner,
                "dd-MM-yyyy");

        fromSpinner.setEditor(fromEditor);
        toSpinner.setEditor(toEditor);

        JPanel panel = new JPanel(
                new GridLayout(2, 2, 10, 10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 5, 10));

        panel.add(new JLabel("From Date:"));
        panel.add(fromSpinner);

        panel.add(new JLabel("To Date:"));
        panel.add(toSpinner);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Select Order Date Range",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        Date start = (Date) fromSpinner.getValue();
        Date end = (Date) toSpinner.getValue();

        Calendar startCal = Calendar.getInstance();

        startCal.setTime(start);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();

        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        if (startCal.after(endCal)) {

            JOptionPane.showMessageDialog(
                    this,
                    "From Date cannot be after To Date.",
                    "Invalid Date Range",
                    JOptionPane.WARNING_MESSAGE);

            return false;
        }

        customStartDate = startCal.getTime();
        customEndDate = endCal.getTime();

        return true;
    }

    // =========================================================
    // CHECK CUSTOM DATE RANGE
    // =========================================================

    private boolean matchesCustomDateRange(
            Timestamp timestamp) {

        if (timestamp == null
                || customStartDate == null
                || customEndDate == null) {

            return false;
        }

        Date orderDate = new Date(timestamp.getTime());

        return !orderDate.before(customStartDate)
                && !orderDate.after(customEndDate);
    }

    // =========================================================
    // CHECK DATE FILTER
    // =========================================================

    private boolean matchesDateFilter(
            Timestamp timestamp,
            String filter) {

        if (timestamp == null) {
            return false;
        }

        Date orderDate = new Date(timestamp.getTime());
        Date now = new Date();

        Calendar orderCal = Calendar.getInstance();
        Calendar nowCal = Calendar.getInstance();

        orderCal.setTime(orderDate);
        nowCal.setTime(now);

        if ("Today".equals(filter)) {

            return orderCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    && orderCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR);
        }

        if ("This Month".equals(filter)) {

            return orderCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    && orderCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH);
        }

        if ("This Year".equals(filter)) {

            return orderCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR);
        }

        // All Time
        return true;
    }

    // =========================================================
    // UPDATE SUMMARY CARDS
    // =========================================================

    private void updateSummary() {

        int total = 0;
        int completed = 0;
        int preparing = 0;
        int cancelled = 0;

        String selectedStatus = statusFilter == null
                ? "All Status"
                : String.valueOf(
                        statusFilter.getSelectedItem());

        String selectedDate = dateFilter == null
                ? "Today"
                : String.valueOf(
                        dateFilter.getSelectedItem());

        String search = searchField == null
                ? ""
                : searchField.getText()
                        .trim()
                        .toLowerCase();

        boolean searchOrStatusUsed = !search.isEmpty()
                || !"All Status".equals(selectedStatus);

        for (Order order : allOrders) {

            // Custom range always applies when selected.
            if ("Custom Range".equals(selectedDate)) {

                if (!matchesCustomDateRange(
                        order.getOrderDate())) {
                    continue;
                }

            } else if (!searchOrStatusUsed) {

                if (!matchesDateFilter(
                        order.getOrderDate(),
                        selectedDate)) {
                    continue;
                }
            }

            if (!"All Status".equals(selectedStatus)
                    && !selectedStatus.equalsIgnoreCase(
                            order.getStatus())) {
                continue;
            }

            if (!search.isEmpty()
                    && !matchesSearch(order, search)) {
                continue;
            }

            total++;

            if ("Completed".equalsIgnoreCase(
                    order.getStatus())) {

                completed++;

            } else if ("Preparing".equalsIgnoreCase(
                    order.getStatus())) {

                preparing++;

            } else if ("Cancelled".equalsIgnoreCase(
                    order.getStatus())) {

                cancelled++;
            }
        }

        totalOrdersLabel.setText(String.valueOf(total));
        completedLabel.setText(String.valueOf(completed));
        preparingLabel.setText(String.valueOf(preparing));
        cancelledLabel.setText(String.valueOf(cancelled));
    }

    // =========================================================
    // SEARCH ORDER DATA
    // =========================================================

    private boolean matchesSearch(
            Order order,
            String search) {

        return String.valueOf(order.getOrderId())
                .contains(search)

                || String.valueOf(order.getEmployeeId())
                        .contains(search)

                || safe(getEmployeeName(order.getEmployeeId()))
                        .contains(search)

                || safe(order.getCustomerName())
                        .contains(search)

                || safe(order.getCustomerPhone())
                        .contains(search)

                || safe(order.getStatus())
                        .contains(search);
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value.toLowerCase();
    }

    // =========================================================
    // FORMAT MONEY
    // =========================================================

    private String money(BigDecimal amount) {

        if (amount == null) {
            return "₹0.00";
        }

        return "₹" + amount.toPlainString();
    }

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private String formatDate(Timestamp timestamp) {

        if (timestamp == null) {
            return "";
        }

        return new SimpleDateFormat(
                "dd-MM-yyyy HH:mm").format(timestamp);
    }

    // =========================================================
    // VIEW BUTTON RENDERER
    // =========================================================

    private class ViewButtonRenderer extends JButton
            implements TableCellRenderer {

        public ViewButtonRenderer() {

            setText("View");
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);

            setCursor(
                    new Cursor(Cursor.HAND_CURSOR));

            setBackground(VIEW_BUTTON_COLOR);
            setForeground(VIEW_BUTTON_TEXT_COLOR);
            setMargin(new Insets(4, 10, 4, 10));

            setFont(
                    new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            setText("View");
            setBackground(row == hoveredViewButtonRow
                    ? VIEW_BUTTON_HOVER_COLOR
                    : VIEW_BUTTON_COLOR);

            return this;
        }
    }

    // =========================================================
    // VIEW BUTTON EDITOR
    // =========================================================

    private class ViewButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JButton button;
        private final JTable table;

        private int orderId;

        public ViewButtonEditor(JTable table) {

            this.table = table;

            button = new JButton("View");

            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);

            button.setCursor(
                    new Cursor(Cursor.HAND_CURSOR));

            button.setBackground(VIEW_BUTTON_COLOR);

            button.setForeground(VIEW_BUTTON_TEXT_COLOR);
            button.setMargin(new Insets(4, 10, 4, 10));

            button.setFont(
                    new Font("Segoe UI", Font.BOLD, 12));

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    button.setBackground(VIEW_BUTTON_HOVER_COLOR);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    button.setBackground(VIEW_BUTTON_COLOR);
                }
            });

            button.addActionListener(e -> {

                fireEditingStopped();

                showOrderDetails(orderId);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            // Convert the visible row to the model row.
            // This also works when sorting or filtering.
            int modelRow = table.convertRowIndexToModel(row);

            orderId = Integer.parseInt(
                    table.getModel()
                            .getValueAt(modelRow, 0)
                            .toString());

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "View";
        }
    }
}
