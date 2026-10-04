package employeePanel;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodaysOrders extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final MenuAndOrderTable app;
    private final boolean billPage;

    public TodaysOrders(MenuAndOrderTable app, boolean billPage) {
        this.app = app;
        this.billPage = billPage;

        // This page reads today's orders directly from the current MySQL database.
        buildPage();
    }

    private void buildPage() {
        removeAll();
        setLayout(new BorderLayout(12, 14));
        setBackground(MenuAndOrderTable.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel(
                billPage ? "Today's Bills" : "Today's Orders");
        heading.setFont(new Font("Serif", Font.BOLD, 32));
        heading.setForeground(MenuAndOrderTable.GREEN);

        JLabel date = new JLabel(LocalDate.now().format(DATE_FORMAT));
        date.setFont(new Font("Serif", Font.BOLD, 15));
        date.setForeground(MenuAndOrderTable.SUBTEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        if (app.search != null)
            right.add(app.search);

        JButton refresh = actionButton("\u21BB  Refresh", MenuAndOrderTable.GREEN);
        refresh.addActionListener(e -> refreshPage());
        right.add(refresh);
        right.add(date);

        top.add(heading, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel table = new JPanel();
        table.setBackground(MenuAndOrderTable.SURFACE);
        table.setLayout(new BoxLayout(table, BoxLayout.Y_AXIS));
        table.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));

        String[] heads = billPage
                ? new String[] { "Bill / Order", "Customer", "Phone", "Bill Amount", "Date", "Time", "Status",
                        "Action" }
                : new String[] { "Order No.", "Customer", "Phone", "Bill Amount", "Status", "Date", "Time", "Action" };

        JPanel header = new JPanel(new GridLayout(1, heads.length));
        header.setBackground(MenuAndOrderTable.MUTED);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        header.setPreferredSize(new Dimension(100, 48));

        for (String h : heads) {
            JLabel label = new JLabel(h, SwingConstants.CENTER);
            label.setFont(new Font("Serif", Font.BOLD, 14));
            label.setForeground(MenuAndOrderTable.TEXT);
            header.add(label);
        }
        table.add(header);

        String query = app.search == null
                ? ""
                : app.search.getText().trim().toLowerCase();

        boolean found = false;

        // Load ALL orders for today directly from MySQL.
        // Do not filter by the currently logged-in employee.
        for (MenuAndOrderTable.Order o : loadTodaysOrdersFromDatabase()) {
            if (o == null)
                continue;

            String status = dbStatus(o.status);

            // Cancelled orders are not shown in Today's Bills.
            if (billPage && "Cancelled".equals(status))
                continue;

            if (!matchesSearch(o, query, billPage))
                continue;

            found = true;
            table.add(managementRow(o));
        }

        if (!found) {
            JLabel empty = new JLabel(
                    billPage
                            ? "No bills generated today."
                            : "No orders placed today.",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Serif", Font.PLAIN, 16));
            empty.setForeground(MenuAndOrderTable.SUBTEXT);
            empty.setBorder(new EmptyBorder(45, 10, 45, 10));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            table.add(empty);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private java.util.List<MenuAndOrderTable.Order> loadTodaysOrdersFromDatabase() {
        java.util.List<MenuAndOrderTable.Order> result = new ArrayList<>();
        Map<Integer, MenuAndOrderTable.Order> grouped = new LinkedHashMap<>();

        dao.EmployeePanelDAO.loadAllTodaysOrders(grouped, MenuAndOrderTable.FOOD);

        result.addAll(grouped.values());
        return result;
    }

    private boolean matchesSearch(MenuAndOrderTable.Order o,
            String query,
            boolean billPage) {
        if (query.isEmpty())
            return true;

        String orderNo = MenuAndOrderTable.formatOrder(o.number).toLowerCase();
        String billNo = ("b-" + MenuAndOrderTable.formatOrder(o.number)).toLowerCase();
        String customer = safe(o.customer).toLowerCase();
        String phone = safe(o.phone).toLowerCase();
        String amount = MenuAndOrderTable.money(o.total).toLowerCase();
        String status = dbStatus(o.status).toLowerCase();

        return customer.contains(query)
                || orderNo.contains(query)
                || billNo.contains(query)
                || phone.contains(query)
                || amount.contains(query)
                || status.contains(query);
    }

    private JPanel managementRow(MenuAndOrderTable.Order o) {
        String date = o.createdAt.format(DATE_FORMAT);
        String time = o.createdAt.format(TIME_FORMAT);
        String status = dbStatus(o.status);

        String[] values = billPage
                ? new String[] {
                        "B-" + MenuAndOrderTable.formatOrder(o.number),
                        safe(o.customer), safe(o.phone),
                        "\u20B9 " + MenuAndOrderTable.money(o.total),
                        date, time, status, "" }
                : new String[] {
                        MenuAndOrderTable.formatOrder(o.number),
                        safe(o.customer), safe(o.phone),
                        "\u20B9 " + MenuAndOrderTable.money(o.total),
                        status, date, time, "" };

        JPanel row = new JPanel(new GridLayout(1, values.length));
        row.setBackground(MenuAndOrderTable.SURFACE);
        row.setPreferredSize(new Dimension(100, 62));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.setBorder(new MatteBorder(0, 0, 1, 0, MenuAndOrderTable.BORDER));

        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 13));
                actions.setOpaque(false);

                JButton view = actionButton("View", MenuAndOrderTable.GREEN);
                view.addActionListener(e -> orderDetail(o, billPage));
                actions.add(view);

                if (!billPage) {
                    JButton edit = actionButton("Edit", MenuAndOrderTable.TEXT);
                    edit.addActionListener(e -> editOrder(o));
                    actions.add(edit);

                    if (!"Completed".equals(status) && !"Cancelled".equals(status)) {
                        JButton cancel = actionButton("Cancel", MenuAndOrderTable.RED);
                        cancel.addActionListener(e -> cancelOrder(o));
                        actions.add(cancel);
                    }
                }

                row.add(actions);
            } else {
                String value = values[i];

                if (isStatus(value)) {
                    JPanel holder = new JPanel(new GridBagLayout());
                    holder.setOpaque(false);

                    JLabel badge = new JLabel(value, SwingConstants.CENTER);
                    badge.setFont(new Font("SansSerif", Font.BOLD, 11));
                    badge.setForeground(Color.WHITE);
                    badge.setOpaque(true);
                    badge.setBackground(statusColor(value));
                    badge.setBorder(new EmptyBorder(5, 10, 5, 10));
                    holder.add(badge);
                    row.add(holder);
                } else {
                    JLabel label = new JLabel(value, SwingConstants.CENTER);
                    label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    label.setForeground(MenuAndOrderTable.TEXT);
                    row.add(label);
                }
            }
        }

        return row;
    }

    private JButton actionButton(String text, Color color) {
        JButton b = new MenuAndOrderTable.RoundedButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(7, 10, 7, 10));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(color);
            }
        });
        return b;
    }

    void orderDetail(MenuAndOrderTable.Order o, boolean billPage) {
        JDialog dialog = new JDialog(
                app,
                billPage ? "Bill Details" : "Order Details",
                true);
        dialog.setSize(620, 660);
        dialog.setLocationRelativeTo(app);

        MenuAndOrderTable.RoundedPanel root = new MenuAndOrderTable.RoundedPanel(
                Color.decode("#F7F1E5"), 20);
        root.setBorder(new EmptyBorder(20, 22, 18, 22));
        root.setLayout(new BorderLayout(0, 12));

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);

        JLabel h = new JLabel(
                (billPage ? "BILL  " : "ORDER  ")
                        + MenuAndOrderTable.formatOrder(o.number));
        h.setFont(new Font("Serif", Font.BOLD, 27));
        h.setForeground(MenuAndOrderTable.SIDE);
        title.add(h, BorderLayout.WEST);

        JLabel status = new JLabel(dbStatus(o.status));
        status.setFont(new Font("SansSerif", Font.BOLD, 13));
        status.setForeground(statusColor(dbStatus(o.status)));
        title.add(status, BorderLayout.EAST);
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 5));
        info.setBackground(MenuAndOrderTable.SURFACE);
        info.setBorder(new CompoundBorder(
                new LineBorder(MenuAndOrderTable.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        info.add(infoLabel("Customer", safe(o.customer)));
        info.add(infoLabel("Phone", safe(o.phone)));
        info.add(infoLabel("Employee", safe(o.employee)));
        info.add(infoLabel("Date", o.createdAt.format(DATE_FORMAT)));
        info.add(infoLabel("Time", o.createdAt.format(TIME_FORMAT)));
        info.add(infoLabel("Order No.", MenuAndOrderTable.formatOrder(o.number)));
        body.add(info);
        body.add(Box.createVerticalStrut(10));

        JLabel ih = new JLabel("Order Items");
        ih.setFont(new Font("Serif", Font.BOLD, 19));
        ih.setForeground(MenuAndOrderTable.TEXT);
        body.add(ih);

        for (Map.Entry<MenuAndOrderTable.Food, Integer> e : o.items.entrySet()) {
            double lineTotal = e.getKey().price * e.getValue();
            JLabel item = new JLabel(
                    e.getKey().name + "  \u00D7 " + e.getValue()
                            + "     \u20B9" + MenuAndOrderTable.money(lineTotal));
            item.setFont(new Font("Serif", Font.PLAIN, 14));
            item.setForeground(MenuAndOrderTable.TEXT);
            item.setBorder(new EmptyBorder(5, 8, 5, 8));
            body.add(item);
        }

        if (o.items.isEmpty()) {
            JLabel empty = new JLabel("No item details found.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            empty.setForeground(MenuAndOrderTable.SUBTEXT);
            empty.setBorder(new EmptyBorder(5, 8, 5, 8));
            body.add(empty);
        }

        body.add(Box.createVerticalStrut(8));
        body.add(detailLine("Subtotal", "\u20B9 " + MenuAndOrderTable.money(o.subtotal), 15, false));
        if (o.discount > 0) {
            body.add(detailLine("Discount", "- \u20B9 " + MenuAndOrderTable.money(o.discount), 15, false));
        }
        body.add(detailLine("GST / Tax", "\u20B9 " + MenuAndOrderTable.money(o.gst), 15, false));
        body.add(new JSeparator());
        body.add(detailLine("Total Amount", "\u20B9 " + MenuAndOrderTable.money(o.total), 20, true));

        if (o.notes != null && !o.notes.trim().isEmpty()) {
            body.add(Box.createVerticalStrut(8));
            body.add(detailLine("Note", o.notes, 13, false));
        }

        JScrollPane sc = new JScrollPane(body);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(14);
        root.add(sc, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);

        if (!billPage) {
            JComboBox<String> statusBox = new JComboBox<>(
                    new String[] { "Pending", "Preparing", "Ready", "Completed" });
            statusBox.setSelectedItem(dbStatus(o.status));

            JButton update = greenButton("Update Status");
            update.addActionListener(e -> {
                o.status = (String) statusBox.getSelectedItem();
                updateStatusDirectly(o);
                refreshPage();
                dialog.dispose();
            });
            actions.add(statusBox);
            actions.add(update);

            JButton edit = greenButton("Edit");
            edit.addActionListener(e -> {
                dialog.dispose();
                editOrder(o);
            });
            actions.add(edit);

            if (!"Completed".equals(dbStatus(o.status))) {
                JButton cancel = redButton("\u2715  Cancel Order");
                cancel.addActionListener(e -> {
                    dialog.dispose();
                    cancelOrder(o);
                });
                actions.add(cancel);
            }
        } else {
            JButton print = greenButton("\uD83D\uDDA8  Print Bill");
            print.addActionListener(e -> app.printBill(o));
            actions.add(print);
        }

        JButton close = plainButton("Close");
        close.addActionListener(e -> dialog.dispose());
        actions.add(close);

        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void updateStatusDirectly(MenuAndOrderTable.Order o) {
        dao.EmployeePanelDAO.updateStatus(o);
    }

    void refreshSearchResults() {
        Container parent = getParent();
        if (parent == null) {
            buildPage();
            revalidate();
            repaint();
            return;
        }

        String query = app.search == null
                ? ""
                : app.search.getText().trim().toLowerCase();

        Component center = parent;
        removeAll();
        setLayout(new BorderLayout(12, 14));
        setBackground(MenuAndOrderTable.BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel(
                billPage ? "Today's Bills" : "Today's Orders");
        heading.setFont(new Font("Serif", Font.BOLD, 32));
        heading.setForeground(MenuAndOrderTable.GREEN);

        JLabel date = new JLabel(LocalDate.now().format(DATE_FORMAT));
        date.setFont(new Font("Serif", Font.BOLD, 15));
        date.setForeground(MenuAndOrderTable.SUBTEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        if (app.search != null)
            right.add(app.search);

        JButton refresh = actionButton("\u21BB  Refresh", MenuAndOrderTable.GREEN);
        refresh.addActionListener(e -> refreshPage());
        right.add(refresh);
        right.add(date);

        top.add(heading, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel table = new JPanel();
        table.setBackground(MenuAndOrderTable.SURFACE);
        table.setLayout(new BoxLayout(table, BoxLayout.Y_AXIS));
        table.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));

        String[] heads = billPage
                ? new String[] { "Bill / Order", "Customer", "Phone", "Bill Amount", "Date", "Time", "Status",
                        "Action" }
                : new String[] { "Order No.", "Customer", "Phone", "Bill Amount", "Status", "Date", "Time", "Action" };

        JPanel header = new JPanel(new GridLayout(1, heads.length));
        header.setBackground(MenuAndOrderTable.MUTED);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        header.setPreferredSize(new Dimension(100, 48));

        for (String h : heads) {
            JLabel label = new JLabel(h, SwingConstants.CENTER);
            label.setFont(new Font("Serif", Font.BOLD, 14));
            label.setForeground(MenuAndOrderTable.TEXT);
            header.add(label);
        }
        table.add(header);

        boolean found = false;

        for (MenuAndOrderTable.Order o : loadTodaysOrdersFromDatabase()) {
            if (o == null)
                continue;

            String status = dbStatus(o.status);

            if (billPage && "Cancelled".equals(status))
                continue;

            if (!matchesSearch(o, query, billPage))
                continue;

            found = true;
            table.add(managementRow(o));
        }

        if (!found) {
            JLabel empty = new JLabel(
                    billPage
                            ? "No bills generated today."
                            : "No orders placed today.",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Serif", Font.PLAIN, 16));
            empty.setForeground(MenuAndOrderTable.SUBTEXT);
            empty.setBorder(new EmptyBorder(45, 10, 45, 10));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            table.add(empty);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            if (app.search != null) {
                app.search.requestFocusInWindow();
                app.search.setCaretPosition(app.search.getText().length());
            }
        });
    }

    private void refreshPage() {
        buildPage();
        revalidate();
        repaint();
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    JPanel detailLine(String left, String right, int size, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 2, 5, 2));

        JLabel name = new JLabel(left);
        name.setHorizontalAlignment(SwingConstants.LEFT);
        name.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        name.setForeground(bold ? MenuAndOrderTable.GREEN : MenuAndOrderTable.TEXT);

        JLabel value = new JLabel(right);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        value.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        value.setForeground(bold ? MenuAndOrderTable.GREEN : MenuAndOrderTable.TEXT);

        row.add(name, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    Color statusColor(String status) {
        status = dbStatus(status);
        if ("Preparing".equals(status))
            return new Color(0xB18449);
        if ("Ready".equals(status))
            return MenuAndOrderTable.SUCCESS;
        if ("Completed".equals(status))
            return MenuAndOrderTable.SUCCESS;
        if ("Cancelled".equals(status))
            return MenuAndOrderTable.RED;
        return MenuAndOrderTable.GREEN;
    }

    JLabel infoLabel(String a, String b) {
        JLabel l = new JLabel(a + ": " + safe(b));
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(MenuAndOrderTable.TEXT);
        l.setBorder(new EmptyBorder(2, 2, 2, 2));
        return l;
    }

    void cancelOrder(MenuAndOrderTable.Order o) {
        String status = dbStatus(o.status);

        if ("Completed".equals(status)) {
            app.showMessage("Cancel Order",
                    "Completed orders cannot be cancelled.");
            return;
        }

        if ("Cancelled".equals(status))
            return;

        double charge = "Preparing".equals(status) ? o.total * .25 : 0;

        String msg = charge > 0
                ? "Order " + MenuAndOrderTable.formatOrder(o.number)
                        + " is PREPARING.\n25% cancellation charge: \u20B9"
                        + MenuAndOrderTable.money(charge)
                        + "\nContinue cancellation?"
                : "Order " + MenuAndOrderTable.formatOrder(o.number)
                        + " has not been completed.\nCancellation charge: \u20B90"
                        + "\nContinue?";

        int result = JOptionPane.showConfirmDialog(
                app, msg, "Cancel Order", JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION)
            return;

        // Keep the order in MySQL with the real enum value 'Cancelled'.
        o.status = "Cancelled";
        updateStatusDirectly(o);

        refreshPage();
    }

    void editOrder(MenuAndOrderTable.Order o) {
        EditDialog d = new EditDialog(o);
        d.setVisible(true);

        if (d.saved) {
            // Reload this page directly from MySQL so all employees' orders remain visible.
            refreshPage();
        }
    }

    class EditDialog extends JDialog {
        MenuAndOrderTable.Order order;
        JTextField customer = new JTextField();
        JTextField phone = new JTextField();
        JPanel itemsPanel = new JPanel();
        boolean saved = false;

        EditDialog(MenuAndOrderTable.Order o) {
            super(app, "Edit Order " + MenuAndOrderTable.formatOrder(o.number), true);
            order = o;
            setSize(620, 610);
            setLocationRelativeTo(app);

            MenuAndOrderTable.RoundedPanel root = new MenuAndOrderTable.RoundedPanel(Color.decode("#F7F1E5"), 20);
            root.setBorder(new EmptyBorder(18, 20, 18, 20));
            root.setLayout(new BorderLayout(0, 10));

            JLabel title = new JLabel(
                    "Edit Order  \u2022  " + MenuAndOrderTable.formatOrder(o.number));
            title.setFont(new Font("Serif", Font.BOLD, 25));
            title.setForeground(MenuAndOrderTable.SIDE);
            root.add(title, BorderLayout.NORTH);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JPanel customerBox = new JPanel(new GridLayout(2, 2, 10, 6));
            customerBox.setBackground(MenuAndOrderTable.SURFACE);
            customerBox.setBorder(new CompoundBorder(
                    new LineBorder(MenuAndOrderTable.BORDER, 1, true),
                    new EmptyBorder(10, 12, 10, 12)));
            customerBox.setPreferredSize(new Dimension(500, 88));
            customerBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

            JLabel customerLabel = new JLabel("Customer Name");
            customerLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            customerLabel.setForeground(MenuAndOrderTable.SUBTEXT);
            customerBox.add(customerLabel);

            customer.setText(safe(o.customer));
            customer.setEditable(false);
            customer.setFocusable(false);
            customer.setFont(new Font("SansSerif", Font.BOLD, 13));
            customer.setBackground(MenuAndOrderTable.MUTED);
            customer.setForeground(MenuAndOrderTable.TEXT);
            customer.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
            customerBox.add(customer);

            JLabel phoneLabel = new JLabel("Phone Number");
            phoneLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            phoneLabel.setForeground(MenuAndOrderTable.SUBTEXT);
            customerBox.add(phoneLabel);

            phone.setText(safe(o.phone));
            phone.setEditable(false);
            phone.setFocusable(false);
            phone.setFont(new Font("SansSerif", Font.BOLD, 13));
            phone.setBackground(MenuAndOrderTable.MUTED);
            phone.setForeground(MenuAndOrderTable.TEXT);
            phone.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
            customerBox.add(phone);

            center.add(customerBox);
            center.add(Box.createVerticalStrut(12));

            JLabel itemTitle = new JLabel("Items");
            itemTitle.setFont(new Font("Serif", Font.BOLD, 18));
            itemTitle.setForeground(MenuAndOrderTable.TEXT);
            itemTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            center.add(itemTitle);
            center.add(Box.createVerticalStrut(5));

            itemsPanel.setBackground(MenuAndOrderTable.SURFACE);
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            refreshEditItems();

            JScrollPane itemScroll = new JScrollPane(itemsPanel);
            itemScroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
            itemScroll.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            itemScroll.getVerticalScrollBar().setUnitIncrement(12);
            center.add(itemScroll);

            root.add(center, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton addItems = greenButton("+ Add Items");
            JButton close = plainButton("Close");
            JButton save = greenButton("Save Changes");

            addItems.addActionListener(e -> showAddItemsDialog());
            close.addActionListener(e -> dispose());
            save.addActionListener(e -> saveEdit());

            actions.add(addItems);
            actions.add(close);
            actions.add(save);
            root.add(actions, BorderLayout.SOUTH);

            setContentPane(root);
        }

        void refreshEditItems() {
            itemsPanel.removeAll();

            if (order.items.isEmpty()) {
                JLabel empty = new JLabel("No items added");
                empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
                empty.setForeground(MenuAndOrderTable.SUBTEXT);
                empty.setBorder(new EmptyBorder(18, 10, 18, 10));
                itemsPanel.add(empty);
            }

            for (Map.Entry<MenuAndOrderTable.Food, Integer> e : order.items.entrySet()) {
                itemsPanel.add(editItemRow(e.getKey(), e.getValue()));
            }

            itemsPanel.revalidate();
            itemsPanel.repaint();
        }

        JPanel editItemRow(MenuAndOrderTable.Food f, int q) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(MenuAndOrderTable.SURFACE);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setPreferredSize(new Dimension(500, 78));
            row.setMinimumSize(new Dimension(500, 78));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
            row.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, MenuAndOrderTable.BORDER),
                    new EmptyBorder(8, 2, 8, 2)));

            JLabel image = new JLabel();
            image.setPreferredSize(new Dimension(56, 56));
            image.setMinimumSize(new Dimension(56, 56));
            image.setMaximumSize(new Dimension(56, 56));

            ImageIcon icon = app.loadImage(f.image, 56, 56);
            if (icon != null) {
                image.setIcon(icon);
            } else {
                image.setText("FOOD");
                image.setHorizontalAlignment(SwingConstants.CENTER);
                image.setOpaque(true);
                image.setBackground(MenuAndOrderTable.MUTED);
                image.setForeground(MenuAndOrderTable.SUBTEXT);
            }

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(f.name);
            name.setFont(new Font("Serif", Font.BOLD, 14));
            name.setForeground(MenuAndOrderTable.TEXT);

            JLabel price = new JLabel(
                    "\u20B9 " + MenuAndOrderTable.money(f.price * q));
            price.setFont(new Font("Serif", Font.PLAIN, 13));
            price.setForeground(MenuAndOrderTable.SUBTEXT);

            info.add(Box.createVerticalStrut(4));
            info.add(name);
            info.add(Box.createVerticalStrut(3));
            info.add(price);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 14));
            controls.setOpaque(false);

            JButton minus = new MenuAndOrderTable.CartButton("\u2212");
            JButton qty = new MenuAndOrderTable.CartButton(String.valueOf(q));
            JButton plus = new MenuAndOrderTable.CartButton("+");
            JButton remove = actionButton("Remove", MenuAndOrderTable.RED);

            minus.addActionListener(ev -> {
                if (q <= 1)
                    order.items.remove(f);
                else
                    order.items.put(f, q - 1);
                recalculateOrder(order);
                refreshEditItems();
            });

            plus.addActionListener(ev -> {
                order.items.put(f, q + 1);
                recalculateOrder(order);
                refreshEditItems();
            });

            remove.addActionListener(ev -> {
                order.items.remove(f);
                recalculateOrder(order);
                refreshEditItems();
            });

            controls.add(minus);
            controls.add(qty);
            controls.add(plus);
            controls.add(remove);

            row.add(image, BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            row.add(controls, BorderLayout.EAST);
            return row;
        }

        void showAddItemsDialog() {
            JDialog dialog = new JDialog(this, "Add Items", true);
            dialog.setSize(570, 620);
            dialog.setLocationRelativeTo(this);

            MenuAndOrderTable.RoundedPanel root = new MenuAndOrderTable.RoundedPanel(Color.decode("#F7F1E5"), 20);
            root.setBorder(new EmptyBorder(16, 18, 16, 18));
            root.setLayout(new BorderLayout(0, 10));

            JLabel title = new JLabel("Add Items");
            title.setFont(new Font("Serif", Font.BOLD, 24));
            title.setForeground(MenuAndOrderTable.SIDE);
            root.add(title, BorderLayout.NORTH);

            JPanel list = new JPanel();
            list.setBackground(MenuAndOrderTable.SURFACE);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

            for (MenuAndOrderTable.Food f : MenuAndOrderTable.FOOD) {
                list.add(addItemRow(f));
            }

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
            scroll.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(12);
            root.add(scroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            bottom.setOpaque(false);
            JButton close = plainButton("Close");
            close.addActionListener(e -> dialog.dispose());
            bottom.add(close);
            root.add(bottom, BorderLayout.SOUTH);

            dialog.setContentPane(root);
            dialog.setVisible(true);
        }

        JPanel addItemRow(MenuAndOrderTable.Food f) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(MenuAndOrderTable.SURFACE);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setPreferredSize(new Dimension(500, 78));
            row.setMinimumSize(new Dimension(500, 78));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
            row.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, MenuAndOrderTable.BORDER),
                    new EmptyBorder(8, 2, 8, 2)));

            JLabel image = new JLabel();
            image.setPreferredSize(new Dimension(56, 56));
            image.setMinimumSize(new Dimension(56, 56));
            image.setMaximumSize(new Dimension(56, 56));

            ImageIcon icon = app.loadImage(f.image, 56, 56);
            if (icon != null)
                image.setIcon(icon);
            else {
                image.setText("FOOD");
                image.setHorizontalAlignment(SwingConstants.CENTER);
                image.setOpaque(true);
                image.setBackground(MenuAndOrderTable.MUTED);
                image.setForeground(MenuAndOrderTable.SUBTEXT);
            }

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel name = new JLabel(f.name);
            name.setFont(new Font("Serif", Font.BOLD, 14));
            name.setForeground(MenuAndOrderTable.TEXT);

            JLabel price = new JLabel(
                    "\u20B9 " + MenuAndOrderTable.money(f.price));
            price.setFont(new Font("Serif", Font.PLAIN, 13));
            price.setForeground(MenuAndOrderTable.SUBTEXT);

            info.add(Box.createVerticalStrut(4));
            info.add(name);
            info.add(Box.createVerticalStrut(3));
            info.add(price);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 14));
            controls.setOpaque(false);

            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (order.items.getOrDefault(f, 0) == 0) {
                        order.items.put(f, 1);
                        recalculateOrder(order);
                        refreshEditItems();
                    }
                }
            });

            row.add(image, BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            row.add(controls, BorderLayout.EAST);

            rebuildAddItemControls(controls, f);
            return row;
        }

        void rebuildAddItemControls(JPanel controls,
                MenuAndOrderTable.Food f) {
            controls.removeAll();
            int q = order.items.getOrDefault(f, 0);

            if (q > 0) {
                JButton minus = new MenuAndOrderTable.CartButton("\u2212");
                JButton qty = new MenuAndOrderTable.CartButton(String.valueOf(q));
                JButton plus = new MenuAndOrderTable.CartButton("+");

                minus.addActionListener(e -> {
                    int current = order.items.getOrDefault(f, 0);
                    if (current <= 1)
                        order.items.remove(f);
                    else
                        order.items.put(f, current - 1);
                    recalculateOrder(order);
                    refreshEditItems();
                    rebuildAddItemControls(controls, f);
                });

                plus.addActionListener(e -> {
                    int current = order.items.getOrDefault(f, 0);
                    order.items.put(f, current + 1);
                    recalculateOrder(order);
                    refreshEditItems();
                    rebuildAddItemControls(controls, f);
                });

                controls.add(minus);
                controls.add(qty);
                controls.add(plus);
            } else {
                JButton select = greenButton("Select");
                select.setFont(new Font("SansSerif", Font.BOLD, 12));
                select.addActionListener(e -> {
                    order.items.put(f, 1);
                    recalculateOrder(order);
                    refreshEditItems();
                    rebuildAddItemControls(controls, f);
                });
                controls.add(select);
            }

            controls.revalidate();
            controls.repaint();
        }

        void saveEdit() {
            if (order.items.isEmpty()) {
                app.showMessage("Edit Order",
                        "Order must contain at least one item.");
                return;
            }

            recalculateOrder(order);

            // Header + order_items are both persisted in MySQL.
            if (!updateOrderAndItems(order)) {
                app.showMessage("Edit Order",
                        "Could not save order changes to the database.");
                return;
            }

            saved = true;
            dispose();
        }
    }

    private boolean updateOrderAndItems(MenuAndOrderTable.Order o) {
        return dao.EmployeePanelDAO.updateOrderAndItemsFull(o);
    }

    void recalculateOrder(MenuAndOrderTable.Order o) {
        double oldSubtotal = o.subtotal;
        double oldDiscount = o.discount;
        double rate = oldSubtotal > 0 ? oldDiscount / oldSubtotal : 0;

        double sub = 0;
        for (Map.Entry<MenuAndOrderTable.Food, Integer> e : o.items.entrySet()) {
            sub += e.getKey().price * e.getValue();
        }

        o.subtotal = sub;
        o.discount = sub * rate;
        double taxable = sub - o.discount;
        o.gst = taxable * .12;
        o.total = taxable + o.gst;
    }

    JButton greenButton(String text) {
        JButton b = new MenuAndOrderTable.RoundedButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(MenuAndOrderTable.GREEN);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    JButton redButton(String text) {
        JButton b = greenButton(text);
        b.setBackground(MenuAndOrderTable.RED);
        return b;
    }

    JButton plainButton(String text) {
        JButton b = new MenuAndOrderTable.RoundedButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 13));
        b.setForeground(MenuAndOrderTable.TEXT);
        b.setBackground(MenuAndOrderTable.SURFACE);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
        return b;
    }

    private static String safe(String s) {
        return s == null || s.trim().isEmpty() ? "\u2014" : s;
    }

    private static String dbStatus(String status) {
        if (status == null)
            return "Pending";
        if (status.equalsIgnoreCase("PLACED"))
            return "Pending";
        if (status.equalsIgnoreCase("PREPARING"))
            return "Preparing";
        if (status.equalsIgnoreCase("READY"))
            return "Ready";
        if (status.equalsIgnoreCase("COMPLETED"))
            return "Completed";
        if (status.equalsIgnoreCase("CANCELLED"))
            return "Cancelled";
        return status;
    }

    private static boolean isStatus(String s) {
        return "Pending".equals(s)
                || "Preparing".equals(s)
                || "Ready".equals(s)
                || "Completed".equals(s)
                || "Cancelled".equals(s);
    }
}
