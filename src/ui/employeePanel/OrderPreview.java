package employeePanel;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Checkout / Order Settlement page for The Tiny Table.
 *
 * Flow:
 * Menu -> Checkout & Order Settlement -> Bill Preview
 */
public class OrderPreview extends JPanel {

    private final MenuAndOrderTable app;
    private final Map<MenuAndOrderTable.Food, Integer> items;

    private final JTextField customerName = new JTextField("Walk-in Guest");
    private final JTextField phone = new JTextField();
    private final JTextArea notes = new JTextArea(3, 20);

    private final JLabel subtotalValue = valueLabel();
    private final JLabel discountValue = valueLabel();
    private final JLabel discountAmountDisplay = valueLabel();
    private final JLabel taxableValue = valueLabel();
    private final JLabel gstValue = valueLabel();
    private final JLabel totalValue = valueLabel();
    private final JLabel changeValue = valueLabel();

    private final JTextField customDiscount = new JTextField();
    private final JTextField cashReceived = new JTextField();
    private JComboBox<String> paymentTender;

    private JButton selectedDiscountButton;
    private double selectedDiscountRate = 0;

    private final Color ACTIVE = MenuAndOrderTable.GREEN;

    public OrderPreview(MenuAndOrderTable app,
                        Map<MenuAndOrderTable.Food, Integer> items) {
        this.app = app;
        this.items = new LinkedHashMap<>(items);

        setBackground(MenuAndOrderTable.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(22, 26, 16, 26));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        customDiscount.setVisible(false);
        refreshDiscountButtons();
        customDiscount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
        });
        customDiscount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
        });

        cashReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
        });

        updateSummary();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Checkout & Order Settlement");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(MenuAndOrderTable.TEXT);

        JLabel subtitle = new JLabel(
                "Confirm guest information, select payment method, and finalize order."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MenuAndOrderTable.SUBTEXT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        JButton back = outlineButton("← Back to Menu / Edit Cart");
        back.addActionListener(e -> app.backToMenu());

        header.add(titleBox, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel root = new JPanel(new GridLayout(1, 2, 20, 0));
        root.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 15));
        left.setOpaque(false);
        left.add(createGuestCard(), BorderLayout.CENTER);
        left.add(createDiscountCard(), BorderLayout.SOUTH);

        JPanel right = createPaymentCard();

        root.add(left);
        root.add(right);
        return root;
    }

    private JPanel createGuestCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(26, 30, 26, 30));
        card.setLayout(new BorderLayout(0, 16));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = sectionTitle("Guest Information");
        JLabel sub = smallText("Enter guest details or leave as default walk-in customer.");
        top.add(title);
        top.add(Box.createVerticalStrut(5));
        top.add(sub);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        fields.add(Box.createVerticalStrut(8));
        fields.add(fieldLabel("Guest / Customer Name *"));
        fields.add(Box.createVerticalStrut(6));
        styleField(customerName);
        fields.add(customerName);

        fields.add(Box.createVerticalStrut(14));
        fields.add(fieldLabel("Mobile Number (Optional)"));
        fields.add(Box.createVerticalStrut(6));
        styleField(phone);
        fields.add(phone);

        fields.add(Box.createVerticalStrut(14));
        fields.add(fieldLabel("Order Notes (Optional)"));
        fields.add(Box.createVerticalStrut(6));

        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        notes.setForeground(MenuAndOrderTable.TEXT);
        notes.setBackground(MenuAndOrderTable.SURFACE);
        notes.setBorder(new EmptyBorder(8, 10, 8, 10));
        notes.setRows(3);

        JScrollPane noteScroll = new JScrollPane(notes);
        noteScroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1));
        noteScroll.setPreferredSize(new Dimension(0, 72));
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        fields.add(noteScroll);

        card.add(top, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDiscountCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(22, 30, 22, 24));
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = sectionTitle("Discount & Promotions");
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 8));
        body.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        addDiscountButton(buttons, "0%", 0);
        addDiscountButton(buttons, "5%", .05);
        addDiscountButton(buttons, "10%", .10);
        addDiscountButton(buttons, "15%", .15);
        addDiscountButton(buttons, "Custom", -1);

        JPanel discountAmount = new JPanel(new BorderLayout(12, 0));
        discountAmount.setOpaque(false);
        discountAmount.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel amountLabel = new JLabel("Discount Amount (₹):");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amountLabel.setForeground(MenuAndOrderTable.TEXT);

        JLabel amount = discountAmountDisplay;
        amount.setHorizontalAlignment(SwingConstants.LEFT);
        amount.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amount.setForeground(MenuAndOrderTable.TEXT);

        discountAmount.add(amountLabel, BorderLayout.WEST);
        discountAmount.add(amount, BorderLayout.CENTER);

        body.add(buttons, BorderLayout.NORTH);
        body.add(discountAmount, BorderLayout.CENTER);

        JPanel customBox = new JPanel(new BorderLayout(8, 0));
        customBox.setOpaque(false);
        JLabel customLabel = new JLabel("Custom %");
        customLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        customLabel.setForeground(MenuAndOrderTable.SUBTEXT);
        styleField(customDiscount);
        customDiscount.setPreferredSize(new Dimension(90, 34));
        customBox.add(customLabel, BorderLayout.WEST);
        customBox.add(customDiscount, BorderLayout.CENTER);
        body.add(customBox, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void addDiscountButton(JPanel parent, String text, double rate) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(MenuAndOrderTable.TEXT);
        b.setBackground(MenuAndOrderTable.SURFACE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(8, 17, 8, 17)
        ));
        b.addActionListener(e -> {
            selectedDiscountRate = rate;
            selectedDiscountButton = b;
            customDiscount.setVisible(rate < 0);
            if (rate < 0) {
                selectedDiscountRate = readCustomDiscount();
                customDiscount.requestFocus();
            }
            refreshDiscountButtons();
            updateSummary();
        });

        if (rate == 0) selectedDiscountButton = b;
        parent.add(b);
    }

    private void refreshDiscountButtons() {
        Container parent = selectedDiscountButton == null ? null : selectedDiscountButton.getParent();
        if (parent == null) return;
        for (Component c : parent.getComponents()) {
            if (!(c instanceof JButton)) continue;
            JButton b = (JButton) c;
            boolean selected = b == selectedDiscountButton;
            b.setBackground(selected ? ACTIVE : MenuAndOrderTable.SURFACE);
            b.setForeground(selected ? Color.WHITE : MenuAndOrderTable.TEXT);
            b.setBorder(new CompoundBorder(
                    new LineBorder(selected ? ACTIVE : MenuAndOrderTable.BORDER, 1),
                    new EmptyBorder(8, 17, 8, 17)
            ));
        }
        parent.revalidate();
        parent.repaint();
    }

    private JPanel createPaymentCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(26, 22, 20, 22));
        card.setLayout(new BorderLayout(0, 15));

        JLabel title = sectionTitle("Payment Terms");
        card.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel tender = createTenderPanel();
        center.add(tender);
        center.add(Box.createVerticalStrut(14));

        JSeparator separator = new JSeparator();
        separator.setForeground(MenuAndOrderTable.BORDER);
        center.add(separator);
        center.add(Box.createVerticalStrut(10));

        center.add(summaryRow("Subtotal", subtotalValue, false));
        center.add(summaryRow("Discount", discountValueForSummary(), false));
        center.add(summaryRow("Taxable Value", taxableValue, false));
        center.add(summaryRow("GST (12%)", gstValue, false));
        center.add(Box.createVerticalStrut(4));

        JPanel total = summaryRow("Net Payable Amount", totalValue, true);
        center.add(total);

        card.add(center, BorderLayout.CENTER);

        JButton confirm = new JButton("Confirm Payment & Print Paid Bill");
        confirm.setFont(new Font("SansSerif", Font.BOLD, 15));
        confirm.setForeground(Color.WHITE);
        confirm.setBackground(ACTIVE);
        confirm.setFocusPainted(false);
        confirm.setBorder(new EmptyBorder(13, 14, 13, 14));
        confirm.addActionListener(e -> confirmOrder());

        card.add(confirm, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTenderPanel() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(MenuAndOrderTable.MUTED);
        box.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel tenderRow = new JPanel(new BorderLayout(10, 0));
        tenderRow.setOpaque(false);

        JLabel label = new JLabel("Payment Tender:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(MenuAndOrderTable.TEXT);

        paymentTender = new JComboBox<>(new String[]{"CASH", "UPI", "CARD"});
        paymentTender.setFont(new Font("SansSerif", Font.PLAIN, 13));
        paymentTender.setBackground(MenuAndOrderTable.SURFACE);
        paymentTender.setForeground(MenuAndOrderTable.TEXT);
        paymentTender.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1));

        tenderRow.add(label, BorderLayout.WEST);
        tenderRow.add(paymentTender, BorderLayout.CENTER);
        box.add(tenderRow);

        box.add(Box.createVerticalStrut(10));

        JPanel cashRow = new JPanel(new BorderLayout(10, 0));
        cashRow.setOpaque(false);
        JLabel cashLabel = new JLabel("Cash Received:");
        cashLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cashLabel.setForeground(MenuAndOrderTable.TEXT);

        styleField(cashReceived);
        cashReceived.setText("");
        cashReceived.setPreferredSize(new Dimension(130, 36));

        cashRow.add(cashLabel, BorderLayout.WEST);
        cashRow.add(cashReceived, BorderLayout.EAST);
        box.add(cashRow);

        box.add(Box.createVerticalStrut(8));

        JPanel changeRow = new JPanel(new BorderLayout(10, 0));
        changeRow.setOpaque(false);
        JLabel changeLabel = new JLabel("Change to Return:");
        changeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        changeLabel.setForeground(MenuAndOrderTable.TEXT);

        changeValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        changeValue.setForeground(ACTIVE);
        changeRow.add(changeLabel, BorderLayout.WEST);
        changeRow.add(changeValue, BorderLayout.CENTER);
        box.add(changeRow);

        paymentTender.addActionListener(e -> updateCashVisibility(cashRow));

        updateCashVisibility(cashRow);
        return box;
    }

    private void updateCashVisibility(JPanel cashRow) {
        boolean isCash = paymentTender != null
                && "CASH".equals(String.valueOf(paymentTender.getSelectedItem()));

        cashRow.setVisible(isCash);
        cashReceived.setEnabled(isCash);

        if (!isCash) {
            cashReceived.setText("");
        }

        cashRow.getParent().revalidate();
        cashRow.getParent().repaint();
        updateChange();
    }

    private JPanel summaryRow(String name, JLabel value, boolean total) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 6, 4, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, total ? 46 : 34));

        JLabel label = new JLabel(name);
        label.setFont(new Font("SansSerif", total ? Font.BOLD : Font.PLAIN, total ? 16 : 13));
        label.setForeground(total ? MenuAndOrderTable.TEXT : MenuAndOrderTable.SUBTEXT);

        value.setFont(new Font("SansSerif", total ? Font.BOLD : Font.PLAIN, total ? 21 : 13));
        value.setForeground(total ? ACTIVE : MenuAndOrderTable.TEXT);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JLabel discountValueForSummary() {
        return discountValue;
    }

    private void updateSummary() {
        double subtotal = calculateSubtotal();
        double discount = subtotal * getDiscountRate();
        double taxable = subtotal - discount;
        double gst = taxable * .12;
        double total = taxable + gst;

        subtotalValue.setText("₹" + MenuAndOrderTable.money(subtotal));
        discountValue.setText("- ₹" + MenuAndOrderTable.money(discount));
        discountAmountDisplay.setText(MenuAndOrderTable.money(discount));
        taxableValue.setText("₹" + MenuAndOrderTable.money(taxable));
        gstValue.setText("₹" + MenuAndOrderTable.money(gst));
        totalValue.setText("₹" + MenuAndOrderTable.money(total));
        updateChange();
        revalidate();
        repaint();
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> e : items.entrySet()) {
            subtotal += e.getKey().price * e.getValue();
        }
        return subtotal;
    }

    private double getDiscountRate() {
        if (selectedDiscountButton == null) return 0;
        String text = selectedDiscountButton.getText();
        if ("Custom".equals(text)) return readCustomDiscount();
        try {
            return Double.parseDouble(text.replace("%", "")) / 100.0;
        } catch (NumberFormatException e) {
            return selectedDiscountRate;
        }
    }

    private double readCustomDiscount() {
        try {
            double value = Double.parseDouble(customDiscount.getText().trim());
            if (value >= 0 && value <= 100) return value / 100.0;
        } catch (NumberFormatException ignored) { }
        return 0;
    }

    private void updateChange() {
        boolean isCash = paymentTender != null
                && "CASH".equals(String.valueOf(paymentTender.getSelectedItem()));

        // Change is calculated only for cash payments.
        // For UPI/Card there is no cash to return, so it remains ₹0.00.
        if (!isCash) {
            changeValue.setText("₹ 0.00");
            changeValue.setForeground(ACTIVE);
            return;
        }

        double total = calculateSubtotal() -
                (calculateSubtotal() * getDiscountRate());
        total += total * .12;

        try {
            String receivedText = cashReceived.getText().trim();

            if (receivedText.isEmpty()) {
                changeValue.setText("₹ 0.00");
                changeValue.setForeground(ACTIVE);
                return;
            }

            double received = Double.parseDouble(receivedText);
            double change = received - total;

            if (change < 0) {
                changeValue.setText("₹ 0.00");
                changeValue.setForeground(MenuAndOrderTable.RED);
            } else {
                changeValue.setText("₹ " + MenuAndOrderTable.money(change));
                changeValue.setForeground(ACTIVE);
            }
        } catch (NumberFormatException e) {
            changeValue.setText("₹ 0.00");
            changeValue.setForeground(ACTIVE);
        }
    }

    private void confirmOrder() {
        String customer = customerName.getText().trim();
        String phoneNumber = phone.getText().trim();

        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(app, "Enter customer name.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            customerName.requestFocus();
            return;
        }

        // Mobile is optional, but when supplied it must contain exactly 10 digits.
        if (!phoneNumber.isEmpty() && !phoneNumber.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(app,
                    "Enter a valid 10 digit mobile number or leave it blank.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            phone.requestFocus();
            return;
        }

        if ("Custom".equals(selectedDiscountButton == null ? "" : selectedDiscountButton.getText())) {
            try {
                double custom = Double.parseDouble(customDiscount.getText().trim());
                if (custom < 0 || custom > 100) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(app,
                        "Enter a custom discount between 0 and 100%.",
                        "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                customDiscount.requestFocus();
                return;
            }
        }

        boolean isCash = paymentTender != null
                && "CASH".equals(String.valueOf(paymentTender.getSelectedItem()));

        if (isCash) {
            double total = calculateSubtotal() * (1 - getDiscountRate());
            total += total * .12;

            double received;
            try {
                received = Double.parseDouble(cashReceived.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(app,
                        "Enter the cash received amount.",
                        "Payment", JOptionPane.WARNING_MESSAGE);
                cashReceived.requestFocus();
                return;
            }

            if (received < total) {
                JOptionPane.showMessageDialog(app,
                        "Cash received is less than the net payable amount.",
                        "Payment", JOptionPane.WARNING_MESSAGE);
                cashReceived.requestFocus();
                return;
            }
        }

        app.createOrderFromPreview(
                items,
                customer,
                phoneNumber,
                getDiscountRate(),
                notes.getText().trim(),
                String.valueOf(paymentTender.getSelectedItem())
        );
    }

    private JPanel cardPanel() {
        JPanel p = new MenuAndOrderTable.RoundedPanel(MenuAndOrderTable.SURFACE, 14);
        p.setOpaque(true);
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 19));
        l.setForeground(MenuAndOrderTable.TEXT);
        return l;
    }

    private JLabel smallText(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(MenuAndOrderTable.SUBTEXT);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(MenuAndOrderTable.TEXT);
        return l;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(MenuAndOrderTable.TEXT);
        field.setBackground(MenuAndOrderTable.SURFACE);
        field.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(8, 11, 8, 11)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    }

    private void stylePaymentButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(10, 8, 10, 8)
        ));
    }

    private JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setForeground(MenuAndOrderTable.TEXT);
        b.setBackground(MenuAndOrderTable.SURFACE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(9, 12, 9, 12)
        ));
        return b;
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel("₹0.00");
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }
}
