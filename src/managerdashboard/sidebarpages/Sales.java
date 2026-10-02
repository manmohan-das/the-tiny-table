package managerdashboard.sidebarpages;

import dao.OrderDAO;
import model.Order;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Sales extends JPanel {

    private final Color CREAM = new Color(250, 245, 235);
    private final Color LIGHT_CREAM = new Color(255, 250, 242);
    private final Color DARK_CREAM = new Color(225, 210, 185);
    private final Color BROWN = new Color(110, 85, 60);
    private final Color TEXT = new Color(45, 40, 35);
    private final Color MUTED = new Color(100, 90, 80);

    private final OrderDAO orderDAO = new OrderDAO();

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> dateFilterBox;

    private JLabel completedOrdersLabel;
    private JLabel totalSalesLabel;
    private JLabel averageOrderLabel;

    private List<Order> allOrders = new ArrayList<>();
    private Date customStartDate;
    private Date customEndDate;

    public Sales() {
        setLayout(new BorderLayout(15, 15));
        setBackground(CREAM);
        createTopPanel();
        createCenterPanel();
        loadSalesData();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(15, 10));
        topPanel.setBackground(CREAM);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 5, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(CREAM);

        JLabel title = new JLabel("Sales Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("View completed orders and restaurant sales");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        topPanel.add(titlePanel, BorderLayout.WEST);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setBackground(CREAM);

        completedOrdersLabel = new JLabel("0");
        totalSalesLabel = new JLabel("₹0.00");
        averageOrderLabel = new JLabel("₹0.00");

        cardsPanel.add(createSummaryCard("Completed Orders", completedOrdersLabel));
        cardsPanel.add(createSummaryCard("Total Sales", totalSalesLabel));
        cardsPanel.add(createSummaryCard("Average Order", averageOrderLabel));

        topPanel.add(cardsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(CREAM);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 25, 20, 25));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(LIGHT_CREAM);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_CREAM),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(TEXT);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        periodLabel.setForeground(TEXT);

        dateFilterBox = new JComboBox<>(new String[]{
                "Today", "This Month", "This Year", "Custom Range", "All Time"
        });
        dateFilterBox.setPreferredSize(new Dimension(140, 30));

        JButton refreshButton = new JButton("⟳ Refresh");
        refreshButton.setPreferredSize(new Dimension(110, 32));
        refreshButton.setBackground(new Color(145, 125, 100));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(periodLabel);
        filterPanel.add(dateFilterBox);
        filterPanel.add(refreshButton);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {
                "Order ID", "Employee ID", "Customer Name", "Phone",
                "Order Date", "Subtotal", "Discount", "Tax", "Total Amount"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(35);
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        salesTable.setForeground(TEXT);
        salesTable.setBackground(Color.WHITE);
        salesTable.setSelectionBackground(new Color(235, 220, 195));
        salesTable.setSelectionForeground(TEXT);
        salesTable.setGridColor(new Color(225, 215, 200));

        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        salesTable.getTableHeader().setBackground(BROWN);
        salesTable.getTableHeader().setForeground(Color.WHITE);
        salesTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < salesTable.getColumnCount(); i++) {
            salesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        centerPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        dateFilterBox.addActionListener(e -> {
            if ("Custom Range".equals(dateFilterBox.getSelectedItem())) {
                if (!showCustomDateDialog()) {
                    dateFilterBox.setSelectedItem("Today");
                    return;
                }
            }
            applyFilters();
        });

        refreshButton.addActionListener(e -> refreshTable());
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(LIGHT_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_CREAM),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadSalesData() {
        allOrders = orderDAO.getAllOrders();
        applyFilters();
    }

    private void applyFilters() {
        tableModel.setRowCount(0);

        String search = searchField.getText().trim().toLowerCase();
        String period = String.valueOf(dateFilterBox.getSelectedItem());

        int completedCount = 0;
        BigDecimal totalSales = BigDecimal.ZERO;

        for (Order order : allOrders) {
            if (!"Completed".equalsIgnoreCase(order.getStatus())) {
                continue;
            }

            if (!matchesDate(order.getOrderDate(), period)) {
                continue;
            }

            if (!search.isEmpty() && !matchesSearch(order, search)) {
                continue;
            }

            completedCount++;

            BigDecimal amount = order.getTotalAmount() == null
                    ? BigDecimal.ZERO
                    : order.getTotalAmount();

            totalSales = totalSales.add(amount);

            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getEmployeeId(),
                    order.getCustomerName(),
                    order.getCustomerPhone(),
                    formatDate(order.getOrderDate()),
                    formatCurrency(order.getSubtotal()),
                    formatCurrency(order.getDiscount()),
                    formatCurrency(order.getTax()),
                    formatCurrency(order.getTotalAmount())
            });
        }

        BigDecimal average = completedCount == 0
                ? BigDecimal.ZERO
                : totalSales.divide(
                        BigDecimal.valueOf(completedCount),
                        2,
                        RoundingMode.HALF_UP
                );

        completedOrdersLabel.setText(String.valueOf(completedCount));
        totalSalesLabel.setText(formatCurrency(totalSales));
        averageOrderLabel.setText(formatCurrency(average));
    }

    private boolean matchesSearch(Order order, String search) {
        return String.valueOf(order.getOrderId()).contains(search)
                || String.valueOf(order.getEmployeeId()).contains(search)
                || safe(order.getCustomerName()).contains(search)
                || safe(order.getCustomerPhone()).contains(search);
    }

    private boolean matchesDate(Timestamp timestamp, String period) {
        if (timestamp == null) {
            return false;
        }

        Date orderDate = new Date(timestamp.getTime());
        Calendar order = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        order.setTime(orderDate);

        if ("All Time".equals(period)) {
            return true;
        }

        if ("Today".equals(period)) {
            return order.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && order.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        }

        if ("This Month".equals(period)) {
            return order.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && order.get(Calendar.MONTH) == today.get(Calendar.MONTH);
        }

        if ("This Year".equals(period)) {
            return order.get(Calendar.YEAR) == today.get(Calendar.YEAR);
        }

        if ("Custom Range".equals(period)) {
            return customStartDate != null
                    && customEndDate != null
                    && !orderDate.before(startOfDay(customStartDate))
                    && !orderDate.after(endOfDay(customEndDate));
        }

        return false;
    }

    private boolean showCustomDateDialog() {
        JSpinner fromSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );

        JSpinner toSpinner = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );

        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "dd-MM-yyyy"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "dd-MM-yyyy"));

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        panel.add(new JLabel("From Date:"));
        panel.add(fromSpinner);
        panel.add(new JLabel("To Date:"));
        panel.add(toSpinner);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Select Sales Date Range",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        Date from = (Date) fromSpinner.getValue();
        Date to = (Date) toSpinner.getValue();

        if (from.after(to)) {
            JOptionPane.showMessageDialog(
                    this,
                    "From Date cannot be after To Date.",
                    "Invalid Date Range",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        customStartDate = from;
        customEndDate = to;
        return true;
    }

    private Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private void refreshTable() {
        searchField.setText("");
        customStartDate = null;
        customEndDate = null;
        dateFilterBox.setSelectedItem("Today");
        loadSalesData();
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(timestamp);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        return "₹" + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
