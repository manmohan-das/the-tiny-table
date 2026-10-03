package managerdashboard.sidebarpages;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import dao.PaymentDAO;
import model.Payment;

import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class Payments extends JPanel {

    // COLORS

    private final Color CREAM = new Color(250, 245, 235);
    private final Color LIGHT_CREAM = new Color(255, 250, 242);
    private final Color DARK_CREAM = new Color(225, 210, 185);
    private final Color BROWN = new Color(110, 85, 60);
    private final Color TEXT = new Color(45, 40, 35);

    // TABLE

    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // FILTERS

    private JTextField searchField;
    private JComboBox<String> statusBox;
    private JComboBox<String> methodBox;

    // SUMMARY

    private JLabel totalPaymentsLabel;
    private JLabel totalRevenueLabel;
    private JLabel paidPaymentsLabel;

    // CONSTRUCTOR

    public Payments() {
        setLayout(new BorderLayout(15, 15));
        setBackground(CREAM);
        createTopPanel();
        createCenterPanel();
        loadPaymentData();
        loadStatuses();
        loadMethods();
        updateSummary();
    }

    // TOP PANEL

    private void createTopPanel() {
        JPanel topPanel =new JPanel(new BorderLayout(20, 0));
        topPanel.setBackground(CREAM);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 5, 25 ));

        // =====================================================
        // TITLE
        // =====================================================

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.Y_AXIS)  );
       titlePanel.setBackground(CREAM);

        JLabel titleLabel = new JLabel("Payment Management");
        titleLabel.setFont(new Font( "Segoe UI",Font.BOLD,    30 ));
        titleLabel.setForeground(new Color(76, 58, 42));
        JLabel subtitleLabel = new JLabel(  "View restaurant payments and transactions" );
        subtitleLabel.setFont(new Font("Segoe UI",Font.PLAIN,    15));
        subtitleLabel.setForeground(new Color(100, 90, 80));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        topPanel.add(titlePanel,BorderLayout.WEST
        );

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(new GridLayout( 1,  3,  15, 0) );
        summaryPanel.setBackground(CREAM);
        totalPaymentsLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("₹0.00");
        paidPaymentsLabel = new JLabel("0");
        summaryPanel.add(createCard(   "Total Payments",totalPaymentsLabel));
        summaryPanel.add(createCard("Total Revenue",totalRevenueLabel ));
        summaryPanel.add(createCard( "Paid Payments",paidPaymentsLabel ) );
        topPanel.add(summaryPanel,  BorderLayout.CENTER);
        add( topPanel, BorderLayout.NORTH);
    }

    // SUMMARY CARD

    private JPanel createCard(
            String title,
            JLabel valueLabel) {

        JPanel card =new JPanel(new BorderLayout()  );
        card.setBackground(LIGHT_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(DARK_CREAM),BorderFactory.createEmptyBorder(  12,   20, 12,    20 ) ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,  14) );
        titleLabel.setForeground(
        new Color(100, 85, 70));
        valueLabel.setFont(new Font("Segoe UI",Font.BOLD,25));
        valueLabel.setForeground(TEXT);
        card.add(titleLabel,BorderLayout.NORTH);
        card.add(valueLabel,BorderLayout.CENTER);
        return card;
 }

    // CENTER PANEL

    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10) );
        centerPanel.setBackground(CREAM);
        centerPanel.setBorder(BorderFactory.createEmptyBorder( 0,   25, 20,    25 ) );

        // FILTER PANEL

        JPanel filterPanel = new JPanel( new BorderLayout() );
        filterPanel.setBackground(LIGHT_CREAM);
        filterPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                DARK_CREAM
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                10,
                                8,
                                10
                        )
                )
        );

        // =====================================================
        // LEFT FILTER PANEL
        // =====================================================

        JPanel leftFilterPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                2
                        )
                );

        leftFilterPanel.setBackground(LIGHT_CREAM);

        // Search

        JLabel searchLabel =
                new JLabel("Search:");

        searchLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        searchLabel.setForeground(TEXT);

        searchField =new JTextField(18);
        searchField.setFont(new Font(    "Segoe UI",Font.PLAIN,  14 ));
        // Status
        JLabel statusLabel =  new JLabel("Payment Status:");
        statusLabel.setFont(new Font(   "Segoe UI",Font.BOLD, 14) );
        statusLabel.setForeground(TEXT);
        statusBox = new JComboBox<>();
        statusBox.setPreferredSize(new Dimension( 130,     30 ));
           statusBox.setFont( new Font( "Segoe UI",Font.PLAIN,14 ) );

        // Method
        JLabel methodLabel =new JLabel("Method:");
        methodLabel.setFont(new Font( "Segoe UI",Font.BOLD, 14   ) );
        methodLabel.setForeground(TEXT);
        methodBox = new JComboBox<>();
        methodBox.setPreferredSize(new Dimension( 120,  30) );
        methodBox.setFont(new Font( "Segoe UI", Font.PLAIN, 14 ));
        leftFilterPanel.add(searchLabel);
        leftFilterPanel.add(searchField);
        leftFilterPanel.add(statusLabel);
        leftFilterPanel.add(statusBox);
        leftFilterPanel.add(methodLabel);
        leftFilterPanel.add(methodBox);
        filterPanel.add(leftFilterPanel,BorderLayout.WEST);

        // REFRESH BUTTON

        JPanel rightFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,2 ));
        rightFilterPanel.setBackground(LIGHT_CREAM);
        JButton refreshButton = createButton( "⟳ Refresh",new Color( 145, 125, 100)  );
        refreshButton.setPreferredSize( new Dimension(110, 32 ) );
        rightFilterPanel.add(refreshButton);
        filterPanel.add(rightFilterPanel,BorderLayout.EAST);
        centerPanel.add(filterPanel, BorderLayout.NORTH
        );

        // TABLE

        String[] columns = {
                "Payment ID",
                "Order ID",
                "Amount",
                "Payment Method",
                "Payment Status",
                "Payment Date"
        };

        tableModel =  new DefaultTableModel(columns,0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                             int column) {
               return false; 
        } };

        paymentTable = new JTable(tableModel);
        sorter =new TableRowSorter<>( tableModel);
        paymentTable.setRowSorter(sorter);
        paymentTable.setRowHeight(40);
        paymentTable.setFont(new Font( "Segoe UI", Font.PLAIN, 14 ));
        paymentTable.setForeground(TEXT);
        paymentTable.setBackground(Color.WHITE);
        paymentTable.setSelectionBackground(new Color(   235,  220,195  ));
        paymentTable.setSelectionForeground(TEXT);
        paymentTable.setGridColor(new Color( 225, 215, 200));
        paymentTable.setShowGrid(true);

        // TABLE HEADER

        JTableHeader header =  paymentTable.getTableHeader();
        header.setFont(new Font("Segoe UI",Font.BOLD,   14 ) );
        header.setBackground(BROWN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(    0,  40 ) );

        // CENTER ALIGNMENT

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
             centerRenderer.setHorizontalAlignment( SwingConstants.CENTER);

        for (
                int i = 0;
                i < paymentTable.getColumnCount();
                i++
        ) {
            paymentTable.getColumnModel() .getColumn(i) .setCellRenderer( centerRenderer  );
        }

        // COLUMN WIDTH

        int[] widths = {
                90,
                90,
                110,
                150,
                130,
                150
        };
        for (
                int i = 0;
                i < widths.length;
                i++
        ) {
           paymentTable.getColumnModel().getColumn(i) .setPreferredWidth( widths[i] );
        }

        JScrollPane scrollPane = new JScrollPane(paymentTable );

        scrollPane.setBorder(BorderFactory.createLineBorder( DARK_CREAM ) );

        centerPanel.add(scrollPane, BorderLayout.CENTER );

        // ADD CENTER PANEL
        add( centerPanel,BorderLayout.CENTER
        );

        // EVENTS

        refreshButton.addActionListener(
                e -> refreshTable()
        );
        // SEARCH EVENT

        searchField.getDocument().addDocumentListener( new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                searchPayments();
                            }
                            @Override
                            public void removeUpdate( DocumentEvent e) {
                                searchPayments();
                            }
                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                searchPayments();
                            }
                        }
                );

        // FILTER EVENTS

        statusBox.addActionListener(
                e -> searchPayments()
        );

        methodBox.addActionListener(
                e -> searchPayments()
        );
    }

    // BUTTON DESIGN

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font( "Segoe UI",Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder( 9, 16, 9,   16  ) );
        return button;
    }

    // LOAD PAYMENT DATA

    private void loadPaymentData() {
        tableModel.setRowCount(0);
        List<Payment> payments = paymentDAO.getAllPayments();
        SimpleDateFormat format = new SimpleDateFormat(  "dd-MM-yyyy HH:mm");
        for (Payment payment : payments) {
            String dateText = payment.getPaymentDate() == null? "NULL": format.format(payment.getPaymentDate());
            String amountText = payment.getAmount() == null? "₹0.00": "₹" + payment.getAmount().setScale(2).toPlainString();

            tableModel.addRow(
                    new Object[]{
                            payment.getPaymentId(),
                            payment.getOrderId(),
                            amountText,
                            payment.getPaymentMethod(),
                            payment.getPaymentStatus(),
                            dateText
                    }
            );
        }
    }

    // LOAD STATUS FILTER

    private void loadStatuses() {
        statusBox.removeAllItems();
        statusBox.addItem("All Status");
        statusBox.addItem("Paid");
        statusBox.addItem("Pending");
    }

    // LOAD METHOD FILTER
    private void loadMethods() {
        methodBox.removeAllItems();
        methodBox.addItem("All Methods");
        methodBox.addItem("Cash");
        methodBox.addItem("UPI");
    }
    // SUMMARY
    private void updateSummary() {
        List<Payment> payments = paymentDAO.getAllPayments();
        int total = payments.size();
        int paid = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        for (Payment payment : payments) {
            if (payment.getAmount() != null) {
                revenue = revenue.add( payment.getAmount());
            }
            if (
                "Paid".equalsIgnoreCase(payment.getPaymentStatus())
            ) {
                paid++;
            }
        }

        totalPaymentsLabel.setText(String.valueOf(total) );
        totalRevenueLabel.setText("₹" +revenue.setScale(2) .toPlainString() );
        paidPaymentsLabel.setText( String.valueOf(paid));
    }

    // SEARCH + FILTER

    private void searchPayments() {
        if (sorter == null) {
            return;
        }
        String searchText = searchField.getText().trim().toLowerCase();

        String selectedStatus = statusBox.getSelectedItem() == null? "All Status": statusBox.getSelectedItem().toString();
        String selectedMethod =methodBox.getSelectedItem() == null? "All Methods": methodBox.getSelectedItem() .toString();
        RowFilter<DefaultTableModel, Object> filter =
                new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel,? extends Object > entry) {

                        String paymentId = entry.getStringValue(0) .toLowerCase();
                        String orderId = entry.getStringValue(1).toLowerCase();
                        String amount = entry.getStringValue(2).toLowerCase();
                        String method = entry.getStringValue(3).toLowerCase();
                        String status = entry.getStringValue(4).toLowerCase();
                        String date = entry.getStringValue(5).toLowerCase();

                        boolean matchesSearch = searchText.isEmpty()
                                || paymentId.contains(searchText)
                                || orderId.contains(searchText)
                                || amount.contains(searchText)
                                || method.contains(searchText)
                                || status.contains(searchText)
                                || date.contains(searchText);

                        boolean matchesStatus = selectedStatus.equals("All Status")|| status.equals(selectedStatus.toLowerCase());

                        boolean matchesMethod = selectedMethod.equals("All Methods")|| method.equals(selectedMethod.toLowerCase() );
                        return matchesSearch&& matchesStatus && matchesMethod;
                    }
                };

        sorter.setRowFilter(filter);
    }
    // REFRESH
    private void refreshTable() {
        loadPaymentData();
        updateSummary();
        searchField.setText("");
        if (statusBox.getItemCount() > 0) {
            statusBox.setSelectedIndex(0);
        }
        if (methodBox.getItemCount() > 0) {
            methodBox.setSelectedIndex(0);
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }
}