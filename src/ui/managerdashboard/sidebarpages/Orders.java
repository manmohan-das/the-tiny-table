package managerdashboard.sidebarpages;

import dao.OrderDAO;
import model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Orders extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

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

    private void createComponents() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Order Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(76, 58, 42));

        JLabel subtitle = new JLabel("Manage and monitor restaurant orders");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 100, 85));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(new Color(255, 250, 242));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 214, 202)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(91, 58, 38));

        searchField = new JTextField(18);
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setToolTipText("Search order, customer or phone");

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
                });

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(91, 58, 38));

        statusFilter = new JComboBox<>(new String[]{
                "All Status", "Pending", "Preparing", "Ready", "Completed", "Cancelled"
        });
        statusFilter.setPreferredSize(new Dimension(125, 30));
        statusFilter.addActionListener(e -> applyFilters());

        JLabel dateLabel = new JLabel("Period:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(91, 58, 38));

        dateFilter = new JComboBox<>(new String[]{
                "Today", "This Month", "This Year", "Custom Range", "All Time"
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
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadOrderData());

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

    private JPanel createTablePanel() {
        String[] columns = {
                "Order ID", "Customer Name", "Customer Phone", "Employee ID",
                "Order Date", "Subtotal", "Discount", "Tax", "Total Amount", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        orderTable.setRowSorter(sorter);

        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        orderTable.getTableHeader().setBackground(new Color(104, 70, 48));
        orderTable.getTableHeader().setForeground(Color.WHITE);
        orderTable.setSelectionBackground(new Color(238, 225, 211));
        orderTable.setGridColor(new Color(230, 220, 210));

        int[] widths = {70, 150, 120, 85, 145, 90, 80, 70, 100, 95};
        for (int i = 0; i < widths.length; i++) {
            orderTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    updateSelectedOrderStatus();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 214, 202)));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton updateButton = new JButton("Update Status");
        updateButton.addActionListener(e -> updateSelectedOrderStatus());
        bottom.add(updateButton);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(new Color(255, 250, 242));
        card.setPreferredSize(new Dimension(0, 78));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 214, 202)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(125, 105, 90));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(new Color(91, 58, 38));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadOrderData() {
        allOrders = orderDAO.getAllOrders();

        tableModel.setRowCount(0);

        for (Order order : allOrders) {
            addOrderToTable(order);
        }

        updateSummary();
        applyFilters();
    }

    private void addOrderToTable(Order order) {
        tableModel.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getEmployeeId(),
                formatDate(order.getOrderDate()),
                money(order.getSubtotal()),
                money(order.getDiscount()),
                money(order.getTax()),
                money(order.getTotalAmount()),
                order.getStatus()
        });
    }

    private void updateSelectedOrderStatus() {
        int viewRow = orderTable.getSelectedRow();

        if (viewRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an order first.",
                    "Select Order",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = orderTable.convertRowIndexToModel(viewRow);
        int orderId = Integer.parseInt(
                tableModel.getValueAt(modelRow, 0).toString()
        );

        String currentStatus = String.valueOf(
                tableModel.getValueAt(modelRow, 9)
        );

        String[] statuses = {
                "Pending", "Preparing", "Ready", "Completed", "Cancelled"
        };

        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status:",
                "Update Order Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                statuses,
                currentStatus
        );

        if (newStatus == null || newStatus.equals(currentStatus)) {
            return;
        }

        boolean success = orderDAO.updateOrderStatus(orderId, newStatus);

        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Order status updated successfully."
            );
            loadOrderData();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update order status.",
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFilters() {
        if (sorter == null) {
            return;
        }

        String search = searchField.getText().trim().toLowerCase();
        String selectedStatus = String.valueOf(statusFilter.getSelectedItem());
        String selectedDate = String.valueOf(dateFilter.getSelectedItem());

        boolean searchOrStatusUsed =
                !search.isEmpty()
                || !"All Status".equals(selectedStatus);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Search
        if (!search.isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
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

        // Status
        if (!"All Status".equals(selectedStatus)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return selectedStatus.equalsIgnoreCase(
                            String.valueOf(entry.getValue(9))
                    );
                }
            });
        }

        /*
         * Default:
         * Today -> today's orders.
         *
         * Search/status:
         * previous orders are also searchable.
         *
         * Custom Range:
         * only orders between selected From and To dates.
         */
        boolean useToday = "Today".equals(selectedDate) && !searchOrStatusUsed;
        boolean useNormalDateFilter =
                ("This Month".equals(selectedDate)
                || "This Year".equals(selectedDate))
                && !searchOrStatusUsed;

        if (useToday || useNormalDateFilter) {
            String filter = useToday ? "Today" : selectedDate;

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    int modelRow = (Integer) entry.getIdentifier();

                    if (modelRow < 0 || modelRow >= allOrders.size()) {
                        return false;
                    }

                    Order order = allOrders.get(modelRow);
                    return matchesDateFilter(order.getOrderDate(), filter);
                }
            });
        } else if ("Custom Range".equals(selectedDate)
                && customStartDate != null
                && customEndDate != null) {

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    int modelRow = (Integer) entry.getIdentifier();

                    if (modelRow < 0 || modelRow >= allOrders.size()) {
                        return false;
                    }

                    Order order = allOrders.get(modelRow);
                    return matchesCustomDateRange(order.getOrderDate());
                }
            });
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        updateSummary();
    }

    private boolean showCustomDateRangeDialog() {
        JSpinner fromSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );

        JSpinner toSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );

        JSpinner.DateEditor fromEditor =
                new JSpinner.DateEditor(fromSpinner, "dd-MM-yyyy");
        JSpinner.DateEditor toEditor =
                new JSpinner.DateEditor(toSpinner, "dd-MM-yyyy");

        fromSpinner.setEditor(fromEditor);
        toSpinner.setEditor(toEditor);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        panel.add(new JLabel("From Date:"));
        panel.add(fromSpinner);
        panel.add(new JLabel("To Date:"));
        panel.add(toSpinner);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Select Order Date Range",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

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
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        customStartDate = startCal.getTime();
        customEndDate = endCal.getTime();

        return true;
    }

    private boolean matchesCustomDateRange(Timestamp timestamp) {
        if (timestamp == null || customStartDate == null || customEndDate == null) {
            return false;
        }

        Date orderDate = new Date(timestamp.getTime());

        return !orderDate.before(customStartDate)
                && !orderDate.after(customEndDate);
    }

    private boolean matchesDateFilter(Timestamp timestamp, String filter) {
        if (timestamp == null) {
            return false;
        }

        Date orderDate = new Date(timestamp.getTime());
        Date now = new Date();

        java.util.Calendar orderCal = java.util.Calendar.getInstance();
        java.util.Calendar nowCal = java.util.Calendar.getInstance();

        orderCal.setTime(orderDate);
        nowCal.setTime(now);

        if ("Today".equals(filter)) {
            return orderCal.get(java.util.Calendar.YEAR)
                    == nowCal.get(java.util.Calendar.YEAR)
                    && orderCal.get(java.util.Calendar.DAY_OF_YEAR)
                    == nowCal.get(java.util.Calendar.DAY_OF_YEAR);
        }

        if ("This Month".equals(filter)) {
            return orderCal.get(java.util.Calendar.YEAR)
                    == nowCal.get(java.util.Calendar.YEAR)
                    && orderCal.get(java.util.Calendar.MONTH)
                    == nowCal.get(java.util.Calendar.MONTH);
        }

        if ("This Year".equals(filter)) {
            return orderCal.get(java.util.Calendar.YEAR)
                    == nowCal.get(java.util.Calendar.YEAR);
        }

        return true;
    }

    private void updateSummary() {
        int total = 0;
        int completed = 0;
        int preparing = 0;
        int cancelled = 0;

        String selectedStatus = statusFilter == null
                ? "All Status"
                : String.valueOf(statusFilter.getSelectedItem());

        String selectedDate = dateFilter == null
                ? "Today"
                : String.valueOf(dateFilter.getSelectedItem());

        String search = searchField == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        boolean searchOrStatusUsed =
                !search.isEmpty()
                || !"All Status".equals(selectedStatus);

        for (Order order : allOrders) {

            // Custom range always applies when selected.
            if ("Custom Range".equals(selectedDate)) {
                if (!matchesCustomDateRange(order.getOrderDate())) {
                    continue;
                }
            } else if (!searchOrStatusUsed) {
                // Default = Today; other date periods also work.
                if (!matchesDateFilter(order.getOrderDate(), selectedDate)) {
                    continue;
                }
            }

            if (!"All Status".equals(selectedStatus)
                    && !selectedStatus.equalsIgnoreCase(order.getStatus())) {
                continue;
            }

            if (!search.isEmpty() && !matchesSearch(order, search)) {
                continue;
            }

            total++;

            if ("Completed".equalsIgnoreCase(order.getStatus())) {
                completed++;
            } else if ("Preparing".equalsIgnoreCase(order.getStatus())) {
                preparing++;
            } else if ("Cancelled".equalsIgnoreCase(order.getStatus())) {
                cancelled++;
            }
        }

        totalOrdersLabel.setText(String.valueOf(total));
        completedLabel.setText(String.valueOf(completed));
        preparingLabel.setText(String.valueOf(preparing));
        cancelledLabel.setText(String.valueOf(cancelled));
    }

    
    private boolean matchesSearch(Order order, String search) {
        return String.valueOf(order.getOrderId()).contains(search)
                || String.valueOf(order.getEmployeeId()).contains(search)
                || safe(order.getCustomerName()).contains(search)
                || safe(order.getCustomerPhone()).contains(search)
                || safe(order.getStatus()).contains(search);
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String money(BigDecimal amount) {
        if (amount == null) {
            return "₹0.00";
        }

        return "₹" + amount.toPlainString();
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(timestamp);
    }
}
