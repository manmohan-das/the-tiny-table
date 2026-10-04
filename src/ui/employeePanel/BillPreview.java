package employeePanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Dynamic Bill Preview for The Tiny Table.
 * Uses the Order object created from the MySQL-backed order flow.
 */
public class BillPreview extends JPanel {

    private final MenuAndOrderTable app;
    private final MenuAndOrderTable.Order order;

    public BillPreview(MenuAndOrderTable app, MenuAndOrderTable.Order order) {
        this.app = app;
        this.order = order;

        setBackground(MenuAndOrderTable.BG);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(createHeader(), BorderLayout.NORTH);
        add(createBillContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Bill Preview");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(MenuAndOrderTable.GREEN);

        JLabel status = new JLabel("Order #" + MenuAndOrderTable.formatOrder(order.number));
        status.setFont(new Font("SansSerif", Font.BOLD, 13));
        status.setForeground(MenuAndOrderTable.SUBTEXT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(status);

        JButton menu = button("←  Back to Menu", MenuAndOrderTable.SURFACE, MenuAndOrderTable.TEXT);
        menu.addActionListener(e -> app.backToMenu());

        header.add(left, BorderLayout.WEST);
        header.add(menu, BorderLayout.EAST);
        return header;
    }

    private JPanel createBillContent() {
        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setOpaque(false);

        MenuAndOrderTable.RoundedPanel billCard = new MenuAndOrderTable.RoundedPanel(MenuAndOrderTable.SURFACE, 18);
        billCard.setBorder(new EmptyBorder(22, 26, 22, 26));
        billCard.setLayout(new BorderLayout(0, 15));

        JPanel billTop = new JPanel(new BorderLayout());
        billTop.setOpaque(false);

        JLabel restaurant = new JLabel("THE TINY TABLE");
        restaurant.setFont(new Font("Serif", Font.BOLD, 28));
        restaurant.setForeground(MenuAndOrderTable.SIDE);

        JLabel date = new JLabel(order.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a")));
        date.setFont(new Font("SansSerif", Font.PLAIN, 12));
        date.setForeground(MenuAndOrderTable.SUBTEXT);

        billTop.add(restaurant, BorderLayout.WEST);
        billTop.add(date, BorderLayout.EAST);

        JTextArea billText = new JTextArea(app.createBill(order));
        billText.setEditable(false);
        billText.setFocusable(false);
        billText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        billText.setForeground(MenuAndOrderTable.TEXT);
        billText.setBackground(MenuAndOrderTable.MUTED);
        billText.setLineWrap(false);
        billText.setBorder(new EmptyBorder(18, 18, 18, 18));

        JScrollPane scroll = new JScrollPane(billText);
        scroll.setBorder(new LineBorder(MenuAndOrderTable.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        billCard.add(billTop, BorderLayout.NORTH);
        billCard.add(scroll, BorderLayout.CENTER);

        JPanel summary = createSummary();
        content.add(billCard, BorderLayout.CENTER);
        content.add(summary, BorderLayout.EAST);
        return content;
    }

    private JPanel createSummary() {
        MenuAndOrderTable.RoundedPanel panel = new MenuAndOrderTable.RoundedPanel(MenuAndOrderTable.SURFACE, 18);
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("Payment Summary");
        title.setFont(new Font("Serif", Font.BOLD, 23));
        title.setForeground(MenuAndOrderTable.TEXT);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        details.add(row("Customer", order.customer, false));
        if (order.phone != null && !order.phone.trim().isEmpty()) details.add(row("Phone", order.phone, false));
        details.add(row("Payment", order.paymentMethod, false));
        details.add(Box.createVerticalStrut(5));
        details.add(row("Subtotal", "₹ " + MenuAndOrderTable.money(order.subtotal), false));
        details.add(row("Discount", "-₹ " + MenuAndOrderTable.money(order.discount), false));
        details.add(row("GST (12%)", "₹ " + MenuAndOrderTable.money(order.gst), false));
        details.add(Box.createVerticalStrut(8));
        JSeparator sep = new JSeparator();
        sep.setForeground(MenuAndOrderTable.BORDER);
        details.add(sep);
        details.add(Box.createVerticalStrut(8));
        details.add(row("Total Amount", "₹ " + MenuAndOrderTable.money(order.total), true));

        if (order.notes != null && !order.notes.trim().isEmpty()) {
            details.add(Box.createVerticalStrut(18));
            JLabel noteTitle = new JLabel("Order Notes");
            noteTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
            noteTitle.setForeground(MenuAndOrderTable.SUBTEXT);
            details.add(noteTitle);
            details.add(Box.createVerticalStrut(5));
            JTextArea note = new JTextArea(order.notes);
            note.setEditable(false);
            note.setLineWrap(true);
            note.setWrapStyleWord(true);
            note.setFont(new Font("SansSerif", Font.PLAIN, 13));
            note.setForeground(MenuAndOrderTable.TEXT);
            note.setBackground(MenuAndOrderTable.MUTED);
            note.setBorder(new EmptyBorder(9, 10, 9, 10));
            details.add(note);
        }

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 10));
        buttons.setOpaque(false);

        JButton print = button("🖨  Print Bill", MenuAndOrderTable.GREEN, Color.WHITE);
        print.setFont(new Font("Serif", Font.BOLD, 15));
        print.addActionListener(e -> app.printBill(order));

        JButton done = button("✓  Done", MenuAndOrderTable.SURFACE, MenuAndOrderTable.TEXT);
        done.setFont(new Font("Serif", Font.BOLD, 15));
        done.addActionListener(e -> app.backToMenu());

        buttons.add(print);
        buttons.add(done);
        panel.add(title, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel row(String name, String value, boolean total) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel left = new JLabel(name);
        left.setFont(new Font("SansSerif", total ? Font.BOLD : Font.PLAIN, total ? 17 : 13));
        left.setForeground(total ? MenuAndOrderTable.GREEN : MenuAndOrderTable.SUBTEXT);

        JLabel right = new JLabel(value);
        right.setFont(new Font("SansSerif", total ? Font.BOLD : Font.PLAIN, total ? 18 : 13));
        right.setForeground(total ? MenuAndOrderTable.GREEN : MenuAndOrderTable.TEXT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton b = new MenuAndOrderTable.RoundedButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(9, 14, 9, 14));
        return b;
    }
}
