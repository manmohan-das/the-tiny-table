package employeePanel;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
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
    private final MenuAndOrderTable.Order editedOrder;
    private final MenuAndOrderTable.Order previousOrder;
    private final double previouslyPaid;
    private final Consumer<String> editPaymentHandler;

    private final JTextField customerName = new JTextField("Walk-in Guest");
    private final JTextField phone = new JTextField();
    private final JTextArea notes = new JTextArea(3, 20);

    private final JLabel subtotalValue = valueLabel();
    private final JLabel discountValue = valueLabel();
    private final JLabel discountAmountDisplay = valueLabel();
    private final JLabel taxableValue = valueLabel();
    private final JLabel gstValue = valueLabel();
    private final JLabel totalValue = valueLabel();
    private final JLabel previousBillValue = valueLabel();
    private final JLabel previousTaxValue = valueLabel();
    private final JLabel addedSubtotalValue = valueLabel();
    private final JLabel addedDiscountValue = valueLabel();
    private final JLabel addedTaxValue = valueLabel();
    private final JLabel removedSubtotalValue = valueLabel();
    private final JLabel removedDiscountValue = valueLabel();
    private final JLabel returnedTaxValue = valueLabel();
    private final JLabel newBillValue = valueLabel();
    private final JLabel previouslyPaidValue = valueLabel();
    private final JLabel returnValue = valueLabel();
    private final JLabel changeValue = valueLabel();

    private final JTextField customDiscount = new JTextField();
    private final JTextField fixedDiscountAmount = new JTextField();
    private final JTextField cashReceived = new JTextField();
    private JComboBox<String> paymentTender;

    private JButton selectedDiscountButton;
    private JButton confirmButton;
    private JLabel tenderLabel;
    private JPanel customDiscountRow;
    private JPanel discountButtonsPanel;
    private JPanel cashReceivedRow;
    private JPanel returnSummaryRow;
    private JPanel cashChangeRow;
    private JRadioButton percentageModeButton;
    private JRadioButton fixedAmountModeButton;
    private final Map<String, JToggleButton> tenderButtons = new LinkedHashMap<>();
    private double selectedDiscountRate = 0;
    private boolean fixedAmountMode = false;

    private final Color ACTIVE = MenuAndOrderTable.GREEN;

    public OrderPreview(MenuAndOrderTable app,
            Map<MenuAndOrderTable.Food, Integer> items) {
        this(app, items, null, null, 0.0, null);
    }

    /** Checkout preview for an existing order being edited. */
    public OrderPreview(MenuAndOrderTable app,
            MenuAndOrderTable.Order order,
            MenuAndOrderTable.Order previousOrder,
            double previouslyPaid,
            Consumer<String> editPaymentHandler) {
        this(app, order.items, order, previousOrder, previouslyPaid, editPaymentHandler);
    }

    private OrderPreview(MenuAndOrderTable app,
            Map<MenuAndOrderTable.Food, Integer> items,
            MenuAndOrderTable.Order editedOrder,
            MenuAndOrderTable.Order previousOrder,
            double previouslyPaid,
            Consumer<String> editPaymentHandler) {
        this.app = app;
        this.items = new LinkedHashMap<>(items);
        this.editedOrder = editedOrder;
        this.previousOrder = previousOrder;
        this.previouslyPaid = Math.max(0.0, previouslyPaid);
        this.editPaymentHandler = editPaymentHandler;

        if (editedOrder != null) {
            customerName.setText(editedOrder.customer == null ? "" : editedOrder.customer);
            phone.setText(editedOrder.phone == null ? "" : editedOrder.phone);
            notes.setText(editedOrder.notes == null ? "" : editedOrder.notes);
            selectedDiscountRate = previousOrder != null && previousOrder.subtotal > 0
                    ? previousOrder.discount / previousOrder.subtotal
                    : 0.0;
        }

        setBackground(MenuAndOrderTable.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(22, 26, 16, 26));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        if (editedOrder != null && paymentTender != null) {
            String existingMethod = editedOrder.paymentMethod == null
                    ? "CASH"
                    : editedOrder.paymentMethod.toUpperCase(Locale.ROOT);
            if ("CASH".equals(existingMethod)
                    || "UPI".equals(existingMethod)
                    || "CARD".equals(existingMethod)) {
                paymentTender.setSelectedItem(existingMethod);
            }
            refreshTenderButtons();
        }

        boolean customDiscountSelected = editedOrder != null || selectedDiscountButton != null
                && "Custom".equals(selectedDiscountButton.getText());
        customDiscount.setVisible(customDiscountSelected);
        if (customDiscountRow != null) {
            customDiscountRow.setVisible(editedOrder != null || customDiscountSelected);
        }
        refreshDiscountButtons();
        customDiscount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomPercentChange();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomPercentChange();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                handleCustomPercentChange();
            }
        });
        customDiscount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }
        });

        cashReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }
        });

        fixedDiscountAmount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSummary();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSummary();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSummary();
            }
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

        JLabel title = new JLabel(editedOrder == null
                ? "Checkout & Order Settlement"
                : "Review Updated Order & Payment");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(MenuAndOrderTable.TEXT);

        JLabel subtitle = new JLabel(editedOrder == null
                ? "Confirm guest information, select payment method, and finalize order."
                : "Review the updated items, tax, previous payment, and remaining balance.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(MenuAndOrderTable.SUBTEXT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        JButton back = outlineButton(editedOrder == null
                ? "← Back to Menu / Edit Cart"
                : "← Back to Today's Orders");
        back.addActionListener(e -> {
            if (editedOrder == null) {
                app.backToMenu();
            } else {
                app.replaceCenter(new TodaysOrders(app, false));
            }
        });

        header.add(titleBox, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel root = new JPanel(new GridLayout(1, 2, 20, 0));
        root.setOpaque(false);

        JPanel left;
        if (editedOrder == null) {
            left = new JPanel(new BorderLayout(0, 10));
            left.setOpaque(false);
            left.add(createGuestCard(), BorderLayout.NORTH);
            left.add(createCheckoutItemsCard(), BorderLayout.CENTER);
        } else {
            left = new JPanel(new BorderLayout(0, 10));
            left.setOpaque(false);
            JPanel notesCard = createOrderNotesCard();
            notesCard.setPreferredSize(new Dimension(0, 125));
            left.add(createExistingGuestCard(), BorderLayout.NORTH);
            left.add(createEditedItemsCard(), BorderLayout.CENTER);
            left.add(notesCard, BorderLayout.SOUTH);
        }

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        right.add(createDiscountCard(), BorderLayout.NORTH);
        right.add(createPaymentCard(), BorderLayout.CENTER);

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
        JLabel sub = smallText(editedOrder == null
                ? "Enter guest details or leave as default walk-in customer."
                : "Existing guest details for order #" + MenuAndOrderTable.formatOrder(editedOrder.number));
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
        notes.setRows(2);

        JScrollPane noteScroll = new JScrollPane(notes);
        noteScroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1));
        noteScroll.setPreferredSize(new Dimension(0, 72));
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        fields.add(noteScroll);

        card.add(top, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel createExistingGuestCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setLayout(new BorderLayout(0, 8));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(sectionTitle("Guest Information"));
        heading.add(smallText("Order #" + MenuAndOrderTable.formatOrder(editedOrder.number)));
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 7));
        fields.setOpaque(false);
        fields.add(fieldLabel("Guest / Customer"));
        styleField(customerName);
        customerName.setEditable(false);
        fields.add(customerName);
        fields.add(fieldLabel("Mobile Number"));
        styleField(phone);
        phone.setEditable(false);
        fields.add(phone);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel createOrderNotesCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setLayout(new BorderLayout(0, 8));
        card.add(sectionTitle("Order Notes (Optional)"), BorderLayout.NORTH);

        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        notes.setForeground(MenuAndOrderTable.TEXT);
        notes.setBackground(MenuAndOrderTable.SURFACE);
        notes.setBorder(new EmptyBorder(8, 10, 8, 10));
        notes.setRows(3);
        notes.setEditable(true);

        JScrollPane scroll = new JScrollPane(notes);
        scroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1));
        scroll.setPreferredSize(new Dimension(0, 72));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCheckoutItemsCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(16, 22, 16, 22));
        card.setLayout(new BorderLayout(0, 8));
        card.add(sectionTitle("Order Items"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(0, 0, 0, 14));

        if (items.isEmpty()) {
            addItemEmptyLabel(rows, "No items added.");
        } else {
            for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : items.entrySet()) {
                addItemSummaryRow(rows, entry.getKey(), entry.getValue());
            }
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEditedItemsCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setLayout(new BorderLayout(0, 10));
        card.add(sectionTitle("Order Item Changes"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(0, 0, 0, 14));

        addItemSectionTitle(rows, "New Order Items Added");

        boolean hasNewItems = false;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : items.entrySet()) {
            int oldQuantity = previousOrder == null
                    ? 0
                    : previousOrder.items.getOrDefault(entry.getKey(), 0);
            int addedQuantity = Math.max(0, entry.getValue() - oldQuantity);
            if (addedQuantity > 0) {
                hasNewItems = true;
                addItemSummaryRow(rows, entry.getKey(), addedQuantity);
            }
        }
        if (!hasNewItems)
            addItemEmptyLabel(rows, "No new items added.");

        rows.add(Box.createVerticalStrut(5));
        rows.add(new JSeparator());
        rows.add(Box.createVerticalStrut(5));
        addItemSectionTitle(rows, "Removed from Order");

        boolean hasRemovedItems = false;
        if (previousOrder != null) {
            for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : previousOrder.items.entrySet()) {
                int currentQuantity = items.getOrDefault(entry.getKey(), 0);
                int removedQuantity = Math.max(0, entry.getValue() - currentQuantity);
                if (removedQuantity > 0) {
                    hasRemovedItems = true;
                    addItemSummaryRow(rows, entry.getKey(), removedQuantity);
                }
            }
        }
        if (!hasRemovedItems)
            addItemEmptyLabel(rows, "No items removed.");

        rows.add(Box.createVerticalStrut(5));
        rows.add(new JSeparator());
        rows.add(Box.createVerticalStrut(5));
        addItemSectionTitle(rows, "Old Order");

        if (previousOrder == null || previousOrder.items.isEmpty()) {
            addItemEmptyLabel(rows, "No previous items.");
        } else {
            for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : previousOrder.items.entrySet()) {
                addItemSummaryRow(rows, entry.getKey(), entry.getValue());
            }
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void addItemSectionTitle(JPanel parent, String title) {
        JLabel label = sectionTitle(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setBorder(new EmptyBorder(3, 0, 3, 0));
        parent.add(label);
    }

    private void addItemEmptyLabel(JPanel parent, String text) {
        JLabel empty = smallText(text);
        empty.setBorder(new EmptyBorder(4, 0, 5, 0));
        parent.add(empty);
    }

    private void addItemSummaryRow(
            JPanel parent,
            MenuAndOrderTable.Food food,
            int quantity) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel name = new JLabel(food.name + "  × " + quantity);
        name.setFont(new Font("SansSerif", Font.PLAIN, 13));
        name.setForeground(MenuAndOrderTable.TEXT);
        JLabel lineTotal = new JLabel("₹ " + MenuAndOrderTable.money(food.price * quantity));
        lineTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lineTotal.setForeground(MenuAndOrderTable.TEXT);
        row.add(name, BorderLayout.WEST);
        row.add(lineTotal, BorderLayout.EAST);
        parent.add(row);
    }

    private JPanel createDiscountCard() {
        if (editedOrder != null) {
            JPanel card = createEditDiscountCard();
            card.setVisible(calculateAddedSubtotal() > 0.005);
            return card;
        }

        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(8, 30, 8, 24));
        card.setLayout(new BorderLayout(0, 6));

        JLabel title = sectionTitle("Discount & Promotions");
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 8));
        body.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        discountButtonsPanel = buttons;

        addDiscountButton(buttons, "0%", 0);
        addDiscountButton(buttons, "5%", .05);
        addDiscountButton(buttons, "10%", .10);
        addDiscountButton(buttons, "15%", .15);
        addDiscountButton(buttons, "Custom", -1);

        customDiscountRow = new JPanel(new BorderLayout(8, 0));
        customDiscountRow.setOpaque(false);
        JLabel customLabel = new JLabel("Custom %");
        customLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        customLabel.setForeground(MenuAndOrderTable.SUBTEXT);
        styleField(customDiscount);
        customDiscount.setPreferredSize(new Dimension(90, 34));
        customDiscountRow.add(customLabel, BorderLayout.WEST);
        customDiscountRow.add(customDiscount, BorderLayout.CENTER);
        boolean customSelected = selectedDiscountButton != null
                && "Custom".equals(selectedDiscountButton.getText());
        customDiscountRow.setVisible(customSelected);
        JPanel discountAmount = new JPanel(new BorderLayout(12, 0));
        discountAmount.setOpaque(false);
        discountAmount.setBorder(new EmptyBorder(5, 0, 0, 0));

        JLabel amountLabel = new JLabel("Discount Amount (₹):");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amountLabel.setForeground(MenuAndOrderTable.TEXT);

        discountAmountDisplay.setHorizontalAlignment(SwingConstants.LEFT);
        discountAmountDisplay.setFont(new Font("SansSerif", Font.PLAIN, 13));
        discountAmountDisplay.setForeground(MenuAndOrderTable.TEXT);
        discountAmount.add(amountLabel, BorderLayout.WEST);
        discountAmount.add(discountAmountDisplay, BorderLayout.CENTER);

        body.add(buttons, BorderLayout.NORTH);
        body.add(discountAmount, BorderLayout.CENTER);
        body.add(customDiscountRow, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEditDiscountCard() {
        JPanel card = cardPanel();
        card.setBorder(new EmptyBorder(8, 16, 8, 16));
        card.setLayout(new BorderLayout(0, 4));

        JLabel title = sectionTitle("Discount on New Items");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        modeRow.setOpaque(false);
        percentageModeButton = new JRadioButton("Percentage");
        fixedAmountModeButton = new JRadioButton("Fixed amount (₹)");
        for (JRadioButton modeButton : new JRadioButton[] { percentageModeButton, fixedAmountModeButton }) {
            modeButton.setOpaque(false);
            modeButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
            modeButton.setForeground(MenuAndOrderTable.TEXT);
        }
        ButtonGroup discountMode = new ButtonGroup();
        discountMode.add(percentageModeButton);
        discountMode.add(fixedAmountModeButton);
        percentageModeButton.setSelected(true);
        modeRow.add(percentageModeButton);
        modeRow.add(fixedAmountModeButton);

        JPanel percentButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        percentButtons.setOpaque(false);
        discountButtonsPanel = percentButtons;
        addDiscountButton(percentButtons, "0%", 0);
        addDiscountButton(percentButtons, "5%", .05);
        addDiscountButton(percentButtons, "10%", .10);
        addDiscountButton(percentButtons, "15%", .15);
        selectExistingDiscountButton(percentButtons);

        customDiscountRow = new JPanel(new BorderLayout(4, 0));
        customDiscountRow.setOpaque(false);
        JLabel customLabel = new JLabel("Custom %");
        customLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        customLabel.setForeground(MenuAndOrderTable.SUBTEXT);
        styleField(customDiscount);
        customDiscount.setPreferredSize(new Dimension(72, 30));
        customDiscountRow.add(customLabel, BorderLayout.WEST);
        customDiscountRow.add(customDiscount, BorderLayout.CENTER);
        customDiscountRow.setPreferredSize(new Dimension(134, 30));
        customDiscountRow.setMinimumSize(new Dimension(134, 30));
        customDiscountRow.setMaximumSize(new Dimension(134, 30));

        JPanel percentControls = new JPanel(new BorderLayout(6, 0));
        percentControls.setOpaque(false);
        percentControls.add(percentButtons, BorderLayout.CENTER);
        percentControls.add(customDiscountRow, BorderLayout.EAST);

        JPanel amountControls = new JPanel(new BorderLayout(5, 0));
        amountControls.setOpaque(false);
        JLabel amountLabel = new JLabel("Amount ₹");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        amountLabel.setForeground(MenuAndOrderTable.SUBTEXT);
        styleField(fixedDiscountAmount);
        fixedDiscountAmount.setPreferredSize(new Dimension(76, 30));
        amountControls.add(amountLabel, BorderLayout.WEST);
        amountControls.add(fixedDiscountAmount, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.add(percentControls, BorderLayout.CENTER);
        inputRow.add(amountControls, BorderLayout.EAST);

        body.add(modeRow, BorderLayout.NORTH);
        body.add(inputRow, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        percentageModeButton.addActionListener(e -> {
            fixedAmountMode = false;
            fixedDiscountAmount.setText("");
            if (selectedDiscountButton == null && customDiscount.getText().trim().isEmpty()
                    && selectedDiscountRate > 0) {
                customDiscount.setText(String.format(Locale.ROOT,
                        "%.2f", selectedDiscountRate * 100.0));
            }
            refreshEditDiscountFields();
            updateSummary();
        });
        fixedAmountModeButton.addActionListener(e -> {
            if (!customDiscount.getText().trim().isEmpty()) {
                selectedDiscountRate = readCustomDiscount();
            }
            fixedAmountMode = true;
            customDiscount.setText("");
            refreshEditDiscountFields();
            updateSummary();
        });
        refreshEditDiscountFields();
        return card;
    }

    private JButton addDiscountButton(JPanel parent, String text, double rate) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(MenuAndOrderTable.TEXT);
        b.setBackground(MenuAndOrderTable.SURFACE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        int discountButtonVerticalPadding = editedOrder == null ? 8 : 4;
        int discountButtonHorizontalPadding = editedOrder == null ? 17 : 12;
        b.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(discountButtonVerticalPadding,
                        discountButtonHorizontalPadding,
                        discountButtonVerticalPadding,
                        discountButtonHorizontalPadding)));
        b.addActionListener(e -> {
            selectedDiscountRate = rate;
            selectedDiscountButton = b;
            if (editedOrder != null) {
                fixedAmountMode = false;
                if (percentageModeButton != null)
                    percentageModeButton.setSelected(true);
                fixedDiscountAmount.setText("");
                customDiscount.setText("");
                refreshEditDiscountFields();
            } else {
                customDiscount.setVisible(rate < 0);
                if (customDiscountRow != null)
                    customDiscountRow.setVisible(rate < 0);
                if (rate < 0) {
                    selectedDiscountRate = readCustomDiscount();
                    customDiscount.requestFocus();
                }
            }
            refreshDiscountButtons();
            updateSummary();
        });

        if (rate == 0)
            selectedDiscountButton = b;
        parent.add(b);
        return b;
    }

    private void selectExistingDiscountButton(JPanel buttons) {
        JButton customButton = null;
        for (Component component : buttons.getComponents()) {
            if (!(component instanceof JButton))
                continue;
            JButton button = (JButton) component;
            if ("Custom".equals(button.getText())) {
                customButton = button;
                continue;
            }

            try {
                double buttonRate = Double.parseDouble(
                        button.getText().replace("%", "")) / 100.0;
                if (Math.abs(buttonRate - selectedDiscountRate) < 0.0001) {
                    selectedDiscountButton = button;
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        selectedDiscountButton = customButton;
        customDiscount.setText(String.format(Locale.ROOT, "%.2f", selectedDiscountRate * 100.0));
    }

    private void refreshDiscountButtons() {
        Container parent = discountButtonsPanel;
        if (parent == null)
            return;
        for (Component c : parent.getComponents()) {
            if (!(c instanceof JButton))
                continue;
            JButton b = (JButton) c;
            boolean selected = b == selectedDiscountButton
                    && (editedOrder == null || !fixedAmountMode);
            b.setEnabled(editedOrder == null || !fixedAmountMode);
            b.setBackground(selected ? ACTIVE : MenuAndOrderTable.SURFACE);
            b.setForeground(selected ? Color.WHITE : MenuAndOrderTable.TEXT);
            int verticalPadding = editedOrder == null ? 8 : 4;
            int horizontalPadding = editedOrder == null ? 17 : 12;
            b.setBorder(new CompoundBorder(
                    new LineBorder(selected ? ACTIVE : MenuAndOrderTable.BORDER, 1),
                    new EmptyBorder(verticalPadding, horizontalPadding,
                            verticalPadding, horizontalPadding)));
        }
        parent.revalidate();
        parent.repaint();
    }

    private void refreshEditDiscountFields() {
        if (editedOrder == null)
            return;
        boolean percentageMode = !fixedAmountMode;
        if (percentageModeButton != null)
            percentageModeButton.setSelected(percentageMode);
        if (fixedAmountModeButton != null)
            fixedAmountModeButton.setSelected(fixedAmountMode);
        customDiscount.setEnabled(percentageMode);
        fixedDiscountAmount.setEnabled(fixedAmountMode);
        refreshDiscountButtons();
    }

    private void handleCustomPercentChange() {
        if (editedOrder != null && !fixedAmountMode) {
            if (!customDiscount.getText().trim().isEmpty()) {
                selectedDiscountButton = null;
                selectedDiscountRate = readCustomDiscount();
            } else if (selectedDiscountButton == null) {
                selectedDiscountRate = 0.0;
                for (Component component : discountButtonsPanel.getComponents()) {
                    if (component instanceof JButton
                            && "0%".equals(((JButton) component).getText())) {
                        selectedDiscountButton = (JButton) component;
                        break;
                    }
                }
            }
            refreshDiscountButtons();
        }
        updateSummary();
    }

    private JPanel createPaymentCard() {
        JPanel card = cardPanel();
        card.setBorder(editedOrder == null
                ? new EmptyBorder(26, 22, 20, 22)
                : new EmptyBorder(14, 22, 12, 22));
        card.setLayout(new BorderLayout(0, editedOrder == null ? 15 : 8));

        JLabel title = sectionTitle("Payment Terms");
        card.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        int paymentRow = 0;

        JPanel tender = createTenderPanel();
        addPaymentRow(center, tender, paymentRow++);
        addPaymentRow(center, Box.createVerticalStrut(editedOrder == null ? 14 : 10), paymentRow++);

        JSeparator separator = new JSeparator();
        separator.setForeground(MenuAndOrderTable.BORDER);
        addPaymentRow(center, separator, paymentRow++);
        addPaymentRow(center, Box.createVerticalStrut(editedOrder == null ? 10 : 6), paymentRow++);

        if (editedOrder != null) {
            addPaymentRow(center, compactSummaryRow("Previous bill total", previousBillValue, false), paymentRow++);
            addPaymentRow(center, compactSummaryRow("Previously paid", previouslyPaidValue, false), paymentRow++);
            addPaymentRow(center, compactSummaryRow("Old tax (already paid)", previousTaxValue, false), paymentRow++);

            if (calculateAddedSubtotal() > 0.005) {
                addPaymentRow(center, compactSummaryRow("Added items subtotal", addedSubtotalValue, false),
                        paymentRow++);
                addPaymentRow(center, compactSummaryRow("Discount on additions", addedDiscountValue, false),
                        paymentRow++);
                addPaymentRow(center, compactSummaryRow("Tax on new items only", addedTaxValue, false), paymentRow++);
            }
            if (calculateRemovedSubtotal() > 0.005) {
                addPaymentRow(center, compactSummaryRow("Removed items subtotal", removedSubtotalValue, false),
                        paymentRow++);
                addPaymentRow(center, compactSummaryRow("Discount returned", removedDiscountValue, false),
                        paymentRow++);
                addPaymentRow(center, compactSummaryRow("Tax returned", returnedTaxValue, false), paymentRow++);
            }
            addPaymentRow(center, compactSummaryRow("Updated bill total", newBillValue, false), paymentRow++);
        } else {
            addPaymentRow(center, summaryRow("Subtotal", subtotalValue, false), paymentRow++);
            addPaymentRow(center, summaryRow("Discount", discountValueForSummary(), false), paymentRow++);
            addPaymentRow(center, summaryRow("Taxable Value", taxableValue, false), paymentRow++);
            addPaymentRow(center, summaryRow("GST (12%)", gstValue, false), paymentRow++);
        }
        addPaymentRow(center, Box.createVerticalStrut(editedOrder == null ? 4 : 2), paymentRow++);

        if (editedOrder == null) {
            addPaymentRow(center, summaryRow("Net Payable Amount", totalValue, true), paymentRow++);
        } else {
            addPaymentRow(center, compactSummaryRow("Balance due / Pay now", totalValue, true), paymentRow++);
            returnSummaryRow = compactSummaryRow("Return to customer", returnValue, true);
            returnSummaryRow.setVisible(previouslyPaid > editedOrder.total + 0.005);
            addPaymentRow(center, returnSummaryRow, paymentRow++);
        }

        // Keep the payment controls and bill details at the top of the center
        // area; let any remaining vertical space collect below the details.
        GridBagConstraints bottomSpace = new GridBagConstraints();
        bottomSpace.gridx = 0;
        bottomSpace.gridy = paymentRow;
        bottomSpace.weightx = 1.0;
        bottomSpace.weighty = 1.0;
        bottomSpace.fill = GridBagConstraints.BOTH;
        center.add(Box.createGlue(), bottomSpace);

        card.add(center, BorderLayout.CENTER);

        confirmButton = new JButton(editedOrder == null
                ? "Confirm Payment & Print Paid Bill"
                : "Confirm Additional Payment & View Updated Bill");
        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(ACTIVE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(new EmptyBorder(13, 14, 13, 14));
        confirmButton.addActionListener(e -> confirmOrder());

        card.add(confirmButton, BorderLayout.SOUTH);
        return card;
    }

    private void addPaymentRow(JPanel panel, Component component, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(component, constraints);
    }

    private JPanel createTenderPanel() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(MenuAndOrderTable.MUTED);
        box.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(12, 12, 12, 12)));
        // Keep this payment section at a fixed height so switching between
        // payment methods does not move the summary panel below it.
        box.setPreferredSize(new Dimension(0, 170));
        box.setMinimumSize(new Dimension(0, 170));
        box.setMaximumSize(new Dimension(Short.MAX_VALUE, 170));

        paymentTender = new JComboBox<>(new String[] { "CASH", "UPI", "CARD" });
        paymentTender.setSelectedItem("CASH");

        tenderLabel = new JLabel("Choose payment method");
        tenderLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        tenderLabel.setForeground(MenuAndOrderTable.TEXT);
        box.add(tenderLabel);
        box.add(Box.createVerticalStrut(8));

        JPanel choices = new JPanel(new GridLayout(1, 3, 8, 0));
        choices.setOpaque(false);
        choices.setPreferredSize(new Dimension(0, 40));
        choices.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        choices.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup tenderGroup = new ButtonGroup();
        for (String method : new String[] { "CASH", "UPI", "CARD" }) {
            JToggleButton choice = new JToggleButton(method) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g2 = (Graphics2D) graphics.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected() ? ACTIVE : MenuAndOrderTable.SURFACE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(isSelected() ? ACTIVE : MenuAndOrderTable.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(graphics);
                }
            };
            choice.setFont(new Font("SansSerif", Font.BOLD, 12));
            choice.setFocusPainted(false);
            choice.setContentAreaFilled(false);
            choice.setOpaque(false);
            choice.setBorderPainted(false);
            choice.setMargin(new Insets(8, 10, 8, 10));
            choice.setPreferredSize(new Dimension(100, 40));
            choice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            choice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            choice.addActionListener(e -> paymentTender.setSelectedItem(method));
            tenderGroup.add(choice);
            tenderButtons.put(method, choice);
            choices.add(choice);
        }
        box.add(choices);
        refreshTenderButtons();

        box.add(Box.createVerticalStrut(10));

        cashReceivedRow = new JPanel(new BorderLayout(10, 0));
        cashReceivedRow.setOpaque(false);
        cashReceivedRow.setPreferredSize(new Dimension(0, 34));
        cashReceivedRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        cashReceivedRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel cashLabel = new JLabel("Cash Received:");
        cashLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cashLabel.setForeground(MenuAndOrderTable.TEXT);

        styleField(cashReceived);
        cashReceived.setText("");
        cashReceived.setPreferredSize(new Dimension(130, 32));
        cashReceived.setMinimumSize(new Dimension(110, 32));
        cashReceived.setMaximumSize(new Dimension(150, 32));

        cashReceivedRow.add(cashLabel, BorderLayout.WEST);
        cashReceivedRow.add(cashReceived, BorderLayout.EAST);
        box.add(cashReceivedRow);

        box.add(Box.createVerticalStrut(8));

        cashChangeRow = new JPanel(new BorderLayout(10, 0));
        cashChangeRow.setOpaque(false);
        cashChangeRow.setPreferredSize(new Dimension(0, 20));
        cashChangeRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        cashChangeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel changeLabel = new JLabel("Change to Return:");
        changeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        changeLabel.setForeground(MenuAndOrderTable.TEXT);

        changeValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        changeValue.setForeground(ACTIVE);
        cashChangeRow.add(changeLabel, BorderLayout.WEST);
        cashChangeRow.add(changeValue, BorderLayout.CENTER);
        box.add(cashChangeRow);

        paymentTender.addActionListener(e -> {
            updateCashVisibility(cashReceivedRow);
            refreshTenderButtons();
        });

        updateCashVisibility(cashReceivedRow);
        return box;
    }

    private void refreshTenderButtons() {
        if (paymentTender == null || tenderButtons.isEmpty())
            return;
        String selected = String.valueOf(paymentTender.getSelectedItem());
        for (Map.Entry<String, JToggleButton> entry : tenderButtons.entrySet()) {
            boolean active = entry.getKey().equalsIgnoreCase(selected);
            JToggleButton button = entry.getValue();
            button.setSelected(active);
            button.setForeground(active ? Color.WHITE : MenuAndOrderTable.TEXT);
            button.setText(active ? "✓ " + entry.getKey() : entry.getKey());
            button.repaint();
        }
    }

    private void updateCashVisibility(JPanel cashRow) {
        boolean isCash = paymentTender != null
                && "CASH".equals(String.valueOf(paymentTender.getSelectedItem()));
        boolean hasAmountToCollect = editedOrder == null
                || editedOrder.total - previouslyPaid > 0.005;
        boolean showCashFields = isCash && hasAmountToCollect;

        cashRow.setVisible(showCashFields);
        if (cashChangeRow != null)
            cashChangeRow.setVisible(showCashFields);
        cashReceived.setEnabled(showCashFields);

        if (!showCashFields) {
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

    private JPanel compactSummaryRow(String name, JLabel value, boolean emphasize) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(1, 6, 1, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, emphasize ? 32 : 27));

        JLabel label = new JLabel(name);
        label.setFont(new Font("SansSerif", emphasize ? Font.BOLD : Font.PLAIN, emphasize ? 15 : 13));
        label.setForeground(emphasize ? MenuAndOrderTable.TEXT : MenuAndOrderTable.SUBTEXT);
        value.setFont(new Font("SansSerif", emphasize ? Font.BOLD : Font.PLAIN, emphasize ? 17 : 13));
        value.setForeground(emphasize ? ACTIVE : MenuAndOrderTable.TEXT);
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
        double amountToPay;

        if (editedOrder != null) {
            double addedSubtotal = calculateAddedSubtotal();
            double removedSubtotal = calculateRemovedSubtotal();
            double oldDiscountRate = previousOrder.subtotal > 0
                    ? previousOrder.discount / previousOrder.subtotal
                    : 0.0;
            double addedDiscount = fixedAmountMode
                    ? Math.min(addedSubtotal, readFixedDiscountAmount())
                    : addedSubtotal * getDiscountRate();
            double removedDiscount = removedSubtotal * oldDiscountRate;
            double addedTax = (addedSubtotal - addedDiscount) * .12;
            double returnedTax = (removedSubtotal - removedDiscount) * .12;

            // Keep the original bill's paid amounts intact. Apply this screen's
            // selected discount only to additions; removed items return their
            // proportional old discount and tax.
            editedOrder.subtotal = roundMoney(
                    previousOrder.subtotal + addedSubtotal - removedSubtotal);
            editedOrder.discount = roundMoney(
                    previousOrder.discount + addedDiscount - removedDiscount);
            editedOrder.gst = roundMoney(previousOrder.gst + addedTax - returnedTax);
            editedOrder.total = roundMoney(previousOrder.total
                    + addedSubtotal - addedDiscount + addedTax
                    - removedSubtotal + removedDiscount - returnedTax);

            previousBillValue.setText("₹" + MenuAndOrderTable.money(previousOrder.total));
            previouslyPaidValue.setText("₹" + MenuAndOrderTable.money(previouslyPaid));
            previousTaxValue.setText("₹" + MenuAndOrderTable.money(previousOrder.gst));
            addedSubtotalValue.setText("₹" + MenuAndOrderTable.money(addedSubtotal));
            addedDiscountValue.setText("- ₹" + MenuAndOrderTable.money(addedDiscount));
            discountAmountDisplay.setText(MenuAndOrderTable.money(addedDiscount));
            addedTaxValue.setText("₹" + MenuAndOrderTable.money(addedTax));
            removedSubtotalValue.setText("₹" + MenuAndOrderTable.money(removedSubtotal));
            removedDiscountValue.setText("+ ₹" + MenuAndOrderTable.money(removedDiscount));
            returnedTaxValue.setText("₹" + MenuAndOrderTable.money(returnedTax));
            newBillValue.setText("₹" + MenuAndOrderTable.money(editedOrder.total));
            amountToPay = Math.max(0.0, editedOrder.total - previouslyPaid);
            returnValue.setText("₹" + MenuAndOrderTable.money(
                    Math.max(0.0, previouslyPaid - editedOrder.total)));
            if (returnSummaryRow != null) {
                returnSummaryRow.setVisible(previouslyPaid > editedOrder.total + 0.005);
            }
            if (tenderLabel != null) {
                tenderLabel.setText(previouslyPaid > editedOrder.total + 0.005
                        ? "Choose how to return the refund"
                        : "Choose payment method");
            }
        } else {
            subtotalValue.setText("₹" + MenuAndOrderTable.money(subtotal));
            discountValue.setText("- ₹" + MenuAndOrderTable.money(discount));
            discountAmountDisplay.setText(MenuAndOrderTable.money(discount));
            taxableValue.setText("₹" + MenuAndOrderTable.money(taxable));
            gstValue.setText("₹" + MenuAndOrderTable.money(gst));
            amountToPay = total;
        }

        totalValue.setText("₹" + MenuAndOrderTable.money(amountToPay));
        if (editedOrder != null && confirmButton != null) {
            double refund = Math.max(0.0, previouslyPaid - editedOrder.total);
            confirmButton.setText(amountToPay > 0.005
                    ? "Confirm Additional Payment & View Updated Bill"
                    : refund > 0.005
                            ? "Confirm Refund & View Updated Bill"
                            : "Confirm Changes & View Updated Bill");
        }
        if (cashReceivedRow != null) {
            updateCashVisibility(cashReceivedRow);
        } else {
            updateChange();
        }
        revalidate();
        repaint();
    }

    private double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> e : items.entrySet()) {
            subtotal += e.getKey().price * e.getValue();
        }
        return subtotal;
    }

    private double calculateAddedSubtotal() {
        if (editedOrder == null || previousOrder == null)
            return 0.0;

        double added = 0.0;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : items.entrySet()) {
            int oldQuantity = previousOrder.items.getOrDefault(entry.getKey(), 0);
            int addedQuantity = Math.max(0, entry.getValue() - oldQuantity);
            added += entry.getKey().price * addedQuantity;
        }
        return added;
    }

    private double calculateRemovedSubtotal() {
        if (editedOrder == null || previousOrder == null)
            return 0.0;

        double removed = 0.0;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> entry : previousOrder.items.entrySet()) {
            int currentQuantity = items.getOrDefault(entry.getKey(), 0);
            int removedQuantity = Math.max(0, entry.getValue() - currentQuantity);
            removed += entry.getKey().price * removedQuantity;
        }
        return removed;
    }

    private double getDiscountRate() {
        if (editedOrder != null) {
            if (fixedAmountMode)
                return 0.0;
            return !customDiscount.getText().trim().isEmpty()
                    ? readCustomDiscount()
                    : selectedDiscountRate;
        }
        if (selectedDiscountButton == null)
            return 0;
        String text = selectedDiscountButton.getText();
        if ("Custom".equals(text))
            return readCustomDiscount();
        try {
            return Double.parseDouble(text.replace("%", "")) / 100.0;
        } catch (NumberFormatException e) {
            return selectedDiscountRate;
        }
    }

    private double readCustomDiscount() {
        try {
            double value = Double.parseDouble(customDiscount.getText().trim());
            if (value >= 0 && value <= 100)
                return value / 100.0;
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    private double readFixedDiscountAmount() {
        try {
            double amount = Double.parseDouble(fixedDiscountAmount.getText().trim());
            if (amount >= 0)
                return amount;
        } catch (NumberFormatException ignored) {
        }
        return 0.0;
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

        double total;
        if (editedOrder != null) {
            total = Math.max(0.0, editedOrder.total - previouslyPaid);
        } else {
            total = calculateSubtotal() -
                    (calculateSubtotal() * getDiscountRate());
            total += total * .12;
        }

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

        boolean customPercentSelected = editedOrder == null
                ? "Custom".equals(selectedDiscountButton == null ? "" : selectedDiscountButton.getText())
                : !fixedAmountMode && selectedDiscountButton == null;
        if (customPercentSelected) {
            try {
                double custom = Double.parseDouble(customDiscount.getText().trim());
                if (custom < 0 || custom > 100)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(app,
                        "Enter a custom discount between 0 and 100%.",
                        "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                customDiscount.requestFocus();
                return;
            }
        }

        if (editedOrder != null && fixedAmountMode
                && !fixedDiscountAmount.getText().trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(fixedDiscountAmount.getText().trim());
                if (amount < 0 || amount > calculateAddedSubtotal() + 0.005) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(app,
                        "Discount amount cannot be greater than the added items subtotal.",
                        "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                fixedDiscountAmount.requestFocus();
                return;
            }
        }

        boolean isCash = paymentTender != null
                && "CASH".equals(String.valueOf(paymentTender.getSelectedItem()));

        if (isCash) {
            double total;
            if (editedOrder != null) {
                total = Math.max(0.0, editedOrder.total - previouslyPaid);
            } else {
                total = calculateSubtotal() * (1 - getDiscountRate());
                total += total * .12;
            }

            if (total > 0.005) {
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
        }

        if (editedOrder != null) {
            if (editPaymentHandler == null) {
                JOptionPane.showMessageDialog(app,
                        "Unable to continue this edited order.",
                        "Order", JOptionPane.ERROR_MESSAGE);
                return;
            }
            editedOrder.notes = notes.getText().trim();
            editPaymentHandler.accept(String.valueOf(paymentTender.getSelectedItem()));
        } else {
            app.createOrderFromPreview(
                    items,
                    customer,
                    phoneNumber,
                    getDiscountRate(),
                    notes.getText().trim(),
                    String.valueOf(paymentTender.getSelectedItem()));
        }
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
                new EmptyBorder(8, 11, 8, 11)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    }

    private void stylePaymentButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1),
                new EmptyBorder(10, 8, 10, 8)));
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
                new EmptyBorder(9, 12, 9, 12)));
        return b;
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel("₹0.00");
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }
}
