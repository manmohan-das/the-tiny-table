package chefPanel;
import javax.swing.*;
import javax.swing.border.*;

import util.DBConnection;

import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ============================================================
 * THE TINY TABLE
 * CHEF PANEL
 * ============================================================
 *
 * Chef Panel Rules:
 *
 * 1. Shows today's orders only.
 * 2. Orders are displayed in FIFO / Order ID order.
 * 3. First 3 pending orders are shown with full details.
 * 4. Remaining orders are shown in the left waiting list.
 * 5. Any of the 3 active orders can be completed.
 * 6. After any active order is completed, the next
 * pending order by Order ID moves into the active 3.
 * 7. Active and waiting orders remain sorted by Order ID.
 *
 * 7. Payment information is NOT shown.
 * 8. Chef only sees:
 * - Order ID
 * - Time
 * - customer_name name
 * - Mobile number
 * - Food items
 * - Quantity
 * - note
 *
 * 9. Uses the existing MySQL DBConnection class.
 *
 * DATABASE:
 *
 * orders
 * order_id
 * employee_id
 * customer_name
 * customer_phone
 * order_date
 * subtotal
 * discount
 * tax
 * total_amount
 * status
 * note
 *
 * order_items
 * order_item_id
 * order_id
 * food_id
 * quantity
 * price
 * subtotal
 *
 * food_items
 * food_id
 * category_id
 * food_name
 * price
 * image
 * available
 * available_qty
 *
 * ============================================================
 */

public class ChefPage extends JFrame {

        // ========================================================
        // THE TINY TABLE THEME
        // ========================================================

        private static final Color BG = Color.decode("#F4F0E8");

        private static final Color SURFACE = Color.decode("#FFFCF6");

        private static final Color MUTED = Color.decode("#EEE8DD");

        private static final Color TEXT = Color.decode("#292C29");

        private static final Color SUBTEXT = Color.decode("#73756F");

        private static final Color GREEN = Color.decode("#496352");

        private static final Color SUCCESS = Color.decode("#5F8A6E");

        private static final Color RED = Color.decode("#A85F5F");

        private static final Color BORDER = Color.decode("#D9D1C4");

        private static final Color SIDE = Color.decode("#0F3D26");

        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

        // ========================================================
        // UI COMPONENTS
        // ========================================================

        private JPanel activeOrdersPanel;

        private JPanel waitingOrdersPanel;

        private JLabel activeCountLabel;

        private JLabel waitingCountLabel;

        private JLabel dateLabel;

        // ========================================================
        // DATA
        // ========================================================

        private final java.util.List<ChefOrder> orders = new ArrayList<>();

        // ========================================================
        // CONSTRUCTOR
        // ========================================================

        public ChefPage() {

                setTitle(
                                "The Tiny Table - Chef Panel");

                setDefaultCloseOperation(
                                JFrame.DISPOSE_ON_CLOSE);

                setMinimumSize(
                                new Dimension(
                                                1100,
                                                650));

                setLayout(
                                new BorderLayout());

                getContentPane()
                                .setBackground(BG);

                add(
                                createHeader(),
                                BorderLayout.NORTH);

                add(
                                createMainContent(),
                                BorderLayout.CENTER);

                setSize(
                                1450,
                                820);

                setLocationRelativeTo(null);

                loadOrders();
        }

        // ========================================================
        // HEADER
        // ========================================================

        private JPanel createHeader() {

                JPanel header = new JPanel(
                                new BorderLayout());

                header.setBackground(
                                SURFACE);

                header.setBorder(
                                new EmptyBorder(
                                                10,
                                                22,
                                                10,
                                                22));

                header.setPreferredSize(
                                new Dimension(
                                                0,
                                                78));

                // ----------------------------------------------------
                // LOGO
                // ----------------------------------------------------

                JPanel logoPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                0,
                                                0));

                logoPanel.setOpaque(false);

                JLabel title = new JLabel(
                                "The Tiny Table");

                title.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                32));

                title.setForeground(
                                TEXT);

                JLabel leaf = new JLabel("❧");

                leaf.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                30));

                leaf.setForeground(
                                GREEN);

                leaf.setBorder(
                                new EmptyBorder(
                                                0,
                                                8,
                                                0,
                                                0));

                logoPanel.add(title);

                logoPanel.add(leaf);

                // ----------------------------------------------------
                // RIGHT SIDE
                // ----------------------------------------------------

                JPanel right = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                10,
                                                8));

                right.setOpaque(false);

                JLabel chefLabel = new JLabel(
                                "CHEF PANEL");

                chefLabel.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                12));

                chefLabel.setForeground(
                                GREEN);

                dateLabel = new JLabel();

                dateLabel.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                13));

                dateLabel.setForeground(
                                SUBTEXT);

                JButton refresh = createOutlineButton(
                                "↻ Refresh");

                refresh.addActionListener(
                                e -> loadOrders());

                right.add(chefLabel);

                right.add(dateLabel);

                right.add(refresh);

                header.add(
                                logoPanel,
                                BorderLayout.WEST);

                header.add(
                                right,
                                BorderLayout.EAST);

                return header;
        }

        // ========================================================
        // MAIN CONTENT
        // ========================================================

        private JPanel createMainContent() {

                JPanel main = new JPanel(
                                new BorderLayout(
                                                16,
                                                0));

                main.setBackground(BG);

                main.setBorder(
                                new EmptyBorder(
                                                18,
                                                18,
                                                18,
                                                18));

                // LEFT WAITING LIST

                main.add(
                                createWaitingPanel(),
                                BorderLayout.WEST);

                // RIGHT / CENTER ACTIVE ORDERS

                main.add(
                                createActiveOrdersSection(),
                                BorderLayout.CENTER);

                return main;
        }

        // ========================================================
        // WAITING ORDERS PANEL
        // ========================================================

        private JPanel createWaitingPanel() {

                RoundedPanel panel = new RoundedPanel(
                                SURFACE,
                                16);

                panel.setPreferredSize(
                                new Dimension(
                                                235,
                                                0));

                panel.setBorder(
                                new EmptyBorder(
                                                18,
                                                14,
                                                14,
                                                14));

                panel.setLayout(
                                new BorderLayout(
                                                0,
                                                12));

                // ----------------------------------------------------
                // HEADING
                // ----------------------------------------------------

                JPanel heading = new JPanel(
                                new BorderLayout());

                heading.setOpaque(false);

                JLabel title = new JLabel(
                                "Waiting Orders");

                title.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                21));

                title.setForeground(TEXT);

                waitingCountLabel = new JLabel("0");

                waitingCountLabel.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                12));

                waitingCountLabel.setForeground(
                                GREEN);

                waitingCountLabel.setHorizontalAlignment(
                                SwingConstants.RIGHT);

                heading.add(
                                title,
                                BorderLayout.WEST);

                heading.add(
                                waitingCountLabel,
                                BorderLayout.EAST);

                JLabel subtitle = new JLabel(
                                "Orders after active 3");

                subtitle.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                11));

                subtitle.setForeground(
                                SUBTEXT);

                JPanel top = new JPanel(
                                new BorderLayout(
                                                0,
                                                3));

                top.setOpaque(false);

                top.add(
                                heading,
                                BorderLayout.NORTH);

                top.add(
                                subtitle,
                                BorderLayout.SOUTH);

                // ----------------------------------------------------
                // WAITING LIST
                // ----------------------------------------------------

                waitingOrdersPanel = new JPanel();

                waitingOrdersPanel.setOpaque(false);

                waitingOrdersPanel.setLayout(
                                new BoxLayout(
                                                waitingOrdersPanel,
                                                BoxLayout.Y_AXIS));

                JScrollPane scroll = new JScrollPane(
                                waitingOrdersPanel);

                scroll.setBorder(null);

                scroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                scroll.getVerticalScrollBar()
                                .setUnitIncrement(14);

                scroll.getViewport()
                                .setBackground(SURFACE);

                panel.add(
                                top,
                                BorderLayout.NORTH);

                panel.add(
                                scroll,
                                BorderLayout.CENTER);

                return panel;
        }

        // ========================================================
        // ACTIVE ORDERS SECTION
        // ========================================================

        private JPanel createActiveOrdersSection() {

                JPanel root = new JPanel(
                                new BorderLayout(
                                                0,
                                                12));

                root.setOpaque(false);

                // ----------------------------------------------------
                // TITLE
                // ----------------------------------------------------

                JPanel titlePanel = new JPanel(
                                new BorderLayout());

                titlePanel.setOpaque(false);

                JPanel titleBox = new JPanel();

                titleBox.setOpaque(false);

                titleBox.setLayout(
                                new BoxLayout(
                                                titleBox,
                                                BoxLayout.Y_AXIS));

                JLabel title = new JLabel(
                                "Kitchen Orders");

                title.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                28));

                title.setForeground(TEXT);

                JLabel subtitle = new JLabel(
                                "Prepare orders in order. Complete the first order to release the next one.");

                subtitle.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                13));

                subtitle.setForeground(
                                SUBTEXT);

                titleBox.add(title);

                titleBox.add(
                                Box.createVerticalStrut(4));

                titleBox.add(subtitle);

                activeCountLabel = new JLabel(
                                "0 active");

                activeCountLabel.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                13));

                activeCountLabel.setForeground(
                                GREEN);

                activeCountLabel.setHorizontalAlignment(
                                SwingConstants.RIGHT);

                titlePanel.add(
                                titleBox,
                                BorderLayout.WEST);

                titlePanel.add(
                                activeCountLabel,
                                BorderLayout.EAST);

                // ----------------------------------------------------
                // ACTIVE ORDER AREA
                // ----------------------------------------------------

                activeOrdersPanel = new JPanel();

                activeOrdersPanel.setOpaque(false);

                activeOrdersPanel.setLayout(
                                new GridLayout(
                                                1,
                                                3,
                                                12,
                                                0));

                JScrollPane scroll = new JScrollPane(
                                activeOrdersPanel);

                scroll.setBorder(null);

                scroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                scroll.setVerticalScrollBarPolicy(
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

                scroll.setOpaque(false);

                scroll.getViewport()
                                .setOpaque(false);

                root.add(
                                titlePanel,
                                BorderLayout.NORTH);

                root.add(
                                scroll,
                                BorderLayout.CENTER);

                return root;
        }

        // ========================================================
        // LOAD TODAY'S ORDERS FROM MYSQL
        // ========================================================

        private void loadOrders() {

                orders.clear();

                dateLabel.setText(
                                LocalDate.now().format(
                                                DateTimeFormatter.ofPattern(
                                                                "dd MMM yyyy")));

                /*
                 * Actual MySQL structure:
                 *
                 * orders:
                 * order_id
                 * customer_name
                 * customer_phone
                 * order_date
                 * status
                 * note
                 *
                 * order_items:
                 * order_item_id
                 * order_id
                 * food_id
                 * quantity
                 * price
                 * subtotal
                 *
                 * food_items:
                 * food_id
                 * food_name
                 *
                 * The orders table does NOT contain "items".
                 *
                 * Food details are therefore loaded through:
                 *
                 * orders
                 * ↓
                 * order_items
                 * ↓
                 * food_items
                 */

                String sql = "SELECT " +

                                "o.order_id, " +
                                "o.customer_name, " +
                                "o.customer_phone, " +
                                "o.order_date, " +
                                "o.status, " +
                                "o.note, " +

                                "oi.food_id, " +
                                "oi.quantity, " +

                                "f.food_name " +

                                "FROM orders o " +

                                "LEFT JOIN order_items oi " +
                                "ON oi.order_id = o.order_id " +

                                "LEFT JOIN food_items f " +
                                "ON f.food_id = oi.food_id " +

                                "WHERE DATE(o.order_date) = CURDATE() " +

                                "AND o.status NOT IN " +
                                "('COMPLETED', 'CANCELLED') " +

                                "ORDER BY " +
                                "o.order_id ASC, " +
                                "oi.order_item_id ASC";

                try (
                                Connection connection = DBConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(sql);

                                ResultSet result = statement.executeQuery()) {

                        /*
                         * One order can have multiple
                         * order_items rows.
                         *
                         * Example:
                         *
                         * Order #5
                         * Classic Burger x2
                         * Cheese Burger x1
                         *
                         * SQL therefore returns two rows.
                         *
                         * LinkedHashMap groups them into
                         * one ChefOrder object.
                         */

                        LinkedHashMap<Integer, ChefOrder> orderMap = new LinkedHashMap<>();

                        while (result.next()) {

                                int orderNo = result.getInt(
                                                "order_id");

                                ChefOrder order = orderMap.get(orderNo);

                                // ------------------------------------------------
                                // CREATE ORDER OBJECT
                                // ------------------------------------------------

                                if (order == null) {

                                        order = new ChefOrder();

                                        order.orderNo = orderNo;

                                        order.customer_name= safeText(
                                                        result.getString(
                                                                        "customer_name"),
                                                        "Walk-in Guest");

                                        order.customer_phone = safeText(
                                                        result.getString(
                                                                        "customer_phone"),
                                                        "");

                                        Timestamp timestamp = result.getTimestamp(
                                                        "order_date");

                                        if (timestamp != null) {

                                                order.order_date = timestamp.toLocalDateTime();

                                        } else {

                                                order.order_date = LocalDateTime.now();
                                        }

                                        order.status = safeText(
                                                        result.getString(
                                                                        "status"),
                                                        "PLACED");

                                        order.note = safeText(
                                                        result.getString(
                                                                        "note"),
                                                        "");

                                        order.items = new LinkedList<>();

                                        orderMap.put(
                                                        orderNo,
                                                        order);
                                }

                                // ------------------------------------------------
                                // FOOD ITEM
                                // ------------------------------------------------

                                String foodName = result.getString(
                                                "food_name");

                                int quantity = result.getInt(
                                                "quantity");

                                /*
                                 * LEFT JOIN can return NULL food
                                 * information if an order has no
                                 * corresponding order_items row.
                                 */

                                if (foodName != null
                                                &&
                                                !foodName.trim().isEmpty()
                                                &&
                                                quantity > 0) {

                                        order.items.add(
                                                        new FoodItem(
                                                                        foodName,
                                                                        quantity));
                                }
                        }

                        // ------------------------------------------------
                        // ADD GROUPED ORDERS TO MAIN LIST
                        // ------------------------------------------------

                        orders.addAll(
                                        orderMap.values());

                } catch (SQLException ex) {

                        showDatabaseError(ex);
                }

                // ------------------------------------------------
                // REFRESH UI
                // ------------------------------------------------

                refreshActiveOrders();

                refreshWaitingOrders();
        }

        // ========================================================
        // REFRESH ACTIVE ORDERS
        // ========================================================

        private void refreshActiveOrders() {

                activeOrdersPanel.removeAll();

                int active = Math.min(
                                3,
                                orders.size());

                activeCountLabel.setText(
                                active +
                                                " active / " +
                                                orders.size() +
                                                " pending");

                if (active == 0) {

                        activeOrdersPanel.setLayout(
                                        new GridLayout(
                                                        1,
                                                        1));

                        activeOrdersPanel.add(
                                        createEmptyKitchenPanel());

                } else {

                        activeOrdersPanel.setLayout(
                                        new GridLayout(
                                                        1,
                                                        3,
                                                        12,
                                                        0));

                        for (int i = 0; i < 3; i++) {

                                if (i < active) {

                                        activeOrdersPanel.add(
                                                        createOrderCard(
                                                                        orders.get(i),
                                                                        i == 0));

                                } else {

                                        JPanel empty = new JPanel();

                                        empty.setOpaque(false);

                                        activeOrdersPanel.add(
                                                        empty);
                                }
                        }
                }

                activeOrdersPanel.revalidate();

                activeOrdersPanel.repaint();
        }

        // ========================================================
        // EMPTY KITCHEN
        // ========================================================

        private JPanel createEmptyKitchenPanel() {

                RoundedPanel panel = new RoundedPanel(
                                SURFACE,
                                16);

                panel.setLayout(
                                new GridBagLayout());

                JPanel box = new JPanel();

                box.setOpaque(false);

                box.setLayout(
                                new BoxLayout(
                                                box,
                                                BoxLayout.Y_AXIS));

                JLabel check = new JLabel("✓");

                check.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                46));

                check.setForeground(
                                SUCCESS);

                check.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                JLabel title = new JLabel(
                                "Kitchen is clear");

                title.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                21));

                title.setForeground(
                                TEXT);

                title.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                JLabel subtitle = new JLabel(
                                "No pending orders for today");

                subtitle.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                12));

                subtitle.setForeground(
                                SUBTEXT);

                subtitle.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                box.add(check);

                box.add(
                                Box.createVerticalStrut(10));

                box.add(title);

                box.add(
                                Box.createVerticalStrut(5));

                box.add(subtitle);

                panel.add(box);

                return panel;
        }

        // ========================================================
        // CREATE ORDER CARD
        // ========================================================

        private JPanel createOrderCard(
                        ChefOrder order,
                        boolean canComplete) {

                RoundedPanel card = new RoundedPanel(
                                SURFACE,
                                16);

                card.setBorder(
                                new EmptyBorder(
                                                16,
                                                16,
                                                14,
                                                16));

                card.setLayout(
                                new BorderLayout(
                                                0,
                                                12));

                // ----------------------------------------------------
                // HEADER
                // ----------------------------------------------------

                JPanel header = new JPanel(
                                new BorderLayout());

                header.setOpaque(false);

                JPanel idBox = new JPanel();

                idBox.setOpaque(false);

                idBox.setLayout(
                                new BoxLayout(
                                                idBox,
                                                BoxLayout.Y_AXIS));

                JLabel orderNumber = new JLabel(
                                "ORDER #" +
                                                order.orderNo);

                orderNumber.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                22));

                orderNumber.setForeground(
                                GREEN);

                JLabel time = new JLabel(
                                order.getTime());

                time.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                11));

                time.setForeground(
                                SUBTEXT);

                idBox.add(orderNumber);

                idBox.add(
                                Box.createVerticalStrut(3));

                idBox.add(time);

                JLabel state = new JLabel(
                                canComplete
                                                ? "NEXT"
                                                : "QUEUED");

                state.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                10));

                state.setForeground(
                                canComplete
                                                ? SUCCESS
                                                : SUBTEXT);

                header.add(
                                idBox,
                                BorderLayout.WEST);

                header.add(
                                state,
                                BorderLayout.EAST);

                card.add(
                                header,
                                BorderLayout.NORTH);

                // ----------------------------------------------------
                // BODY
                // ----------------------------------------------------

                JPanel body = new JPanel();

                body.setOpaque(false);

                body.setLayout(
                                new BoxLayout(
                                                body,
                                                BoxLayout.Y_AXIS));

                // customer_name

                JLabel customer_nameTitle = createSmallTitle(
                                "customer_name");

                body.add(
                                customer_nameTitle);

                body.add(
                                Box.createVerticalStrut(3));

                JLabel customer_name = new JLabel(
                                shortText(
                                                order.customer_name,
                                                28));

                customer_name.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                17));

                customer_name.setForeground(
                                TEXT);

                body.add(customer_name);

                // customer_phone

                if (!order.customer_phone.isEmpty()) {

                        JLabel customer_phone = new JLabel(
                                        "Mobile: " +
                                                        order.customer_phone);

                        customer_phone.setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.PLAIN,
                                                        11));

                        customer_phone.setForeground(
                                        SUBTEXT);

                        body.add(
                                        Box.createVerticalStrut(3));

                        body.add(customer_phone);
                }

                body.add(
                                Box.createVerticalStrut(14));

                // FOOD ITEMS

                body.add(
                                createSmallTitle(
                                                "FOOD ITEMS"));

                body.add(
                                Box.createVerticalStrut(6));

                JPanel foodList = new JPanel();

                foodList.setOpaque(false);

                foodList.setLayout(
                                new BoxLayout(
                                                foodList,
                                                BoxLayout.Y_AXIS));

                if (order.items.isEmpty()) {

                        JLabel empty = new JLabel(
                                        "No food details found");

                        empty.setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.PLAIN,
                                                        12));

                        empty.setForeground(
                                        RED);

                        foodList.add(empty);

                } else {

                        for (FoodItem item : order.items) {

                                JPanel foodRow = new JPanel(
                                                new BorderLayout(
                                                                8,
                                                                0));

                                foodRow.setOpaque(false);

                                foodRow.setBorder(
                                                new EmptyBorder(
                                                                5,
                                                                0,
                                                                5,
                                                                0));

                                foodRow.setMaximumSize(
                                                new Dimension(
                                                                Integer.MAX_VALUE,
                                                                34));

                                JLabel foodName = new JLabel(
                                                shortText(
                                                                item.name,
                                                                24));

                                foodName.setFont(
                                                new Font(
                                                                "SansSerif",
                                                                Font.BOLD,
                                                                14));

                                foodName.setForeground(
                                                TEXT);

                                JLabel quantity = new JLabel(
                                                "× " +
                                                                item.quantity);

                                quantity.setFont(
                                                new Font(
                                                                "SansSerif",
                                                                Font.BOLD,
                                                                14));

                                quantity.setForeground(
                                                GREEN);

                                foodRow.add(
                                                foodName,
                                                BorderLayout.WEST);

                                foodRow.add(
                                                quantity,
                                                BorderLayout.EAST);

                                foodList.add(foodRow);
                        }
                }

                JScrollPane foodScroll = new JScrollPane(
                                foodList);

                foodScroll.setBorder(
                                new LineBorder(
                                                BORDER,
                                                1,
                                                true));

                foodScroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                foodScroll.setPreferredSize(
                                new Dimension(
                                                0,
                                                180));

                foodScroll.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                180));

                foodScroll.getVerticalScrollBar()
                                .setUnitIncrement(12);

                foodScroll.getViewport()
                                .setBackground(MUTED);

                body.add(foodScroll);

                // ----------------------------------------------------
                // note
                // ----------------------------------------------------

                if (order.note != null
                                &&
                                !order.note.trim().isEmpty()) {

                        body.add(
                                        Box.createVerticalStrut(12));

                        body.add(
                                        createSmallTitle(
                                                        "ORDER note"));

                        body.add(
                                        Box.createVerticalStrut(4));

                        JTextArea note = new JTextArea(
                                        order.note);

                        note.setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.PLAIN,
                                                        12));

                        note.setForeground(
                                        TEXT);

                        note.setBackground(
                                        MUTED);

                        note.setLineWrap(true);

                        note.setWrapStyleWord(true);

                        note.setEditable(false);

                        note.setFocusable(false);

                        note.setRows(2);

                        note.setBorder(
                                        new EmptyBorder(
                                                        8,
                                                        8,
                                                        8,
                                                        8));

                        note.setMaximumSize(
                                        new Dimension(
                                                        Integer.MAX_VALUE,
                                                        65));

                        body.add(note);
                }

                card.add(
                                body,
                                BorderLayout.CENTER);

                // ----------------------------------------------------
                // COMPLETE BUTTON
                // ----------------------------------------------------

                JButton complete = new RoundedButton(
                                "✓  Mark Order Complete");

                complete.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                12));

                complete.setForeground(
                                Color.WHITE);

                complete.setBackground(
                                SUCCESS);

                complete.setFocusPainted(false);

                complete.setEnabled(
                                true);

                complete.setBorder(
                                new EmptyBorder(
                                                11,
                                                10,
                                                11,
                                                10));

                complete.addActionListener(
                                e -> completeOrder(
                                                order));

                card.add(
                                complete,
                                BorderLayout.SOUTH);

                return card;
        }

        // ========================================================
        // WAITING ORDERS
        // ========================================================

        private void refreshWaitingOrders() {

                waitingOrdersPanel.removeAll();

                int waiting = Math.max(
                                0,
                                orders.size() - 3);

                waitingCountLabel.setText(
                                waiting + " waiting");

                if (waiting == 0) {

                        JLabel empty = new JLabel(
                                        "No more orders");

                        empty.setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.PLAIN,
                                                        12));

                        empty.setForeground(
                                        SUBTEXT);

                        empty.setAlignmentX(
                                        Component.LEFT_ALIGNMENT);

                        empty.setBorder(
                                        new EmptyBorder(
                                                        14,
                                                        4,
                                                        14,
                                                        4));

                        waitingOrdersPanel.add(empty);

                } else {

                        int position = 1;

                        for (int i = 3; i < orders.size(); i++) {

                                waitingOrdersPanel.add(
                                                createWaitingRow(
                                                                orders.get(i),
                                                                position));

                                position++;

                                if (i < orders.size() - 1) {

                                        waitingOrdersPanel.add(
                                                        Box.createVerticalStrut(7));
                                }
                        }
                }

                waitingOrdersPanel.revalidate();

                waitingOrdersPanel.repaint();
        }

        // ========================================================
        // WAITING ORDER ROW
        // ========================================================

        private JPanel createWaitingRow(
                        ChefOrder order,
                        int position) {

                JPanel row = new JPanel(
                                new BorderLayout(
                                                8,
                                                0));

                row.setOpaque(true);

                row.setBackground(
                                MUTED);

                row.setBorder(
                                new CompoundBorder(
                                                new LineBorder(
                                                                BORDER,
                                                                1,
                                                                true),
                                                new EmptyBorder(
                                                                9,
                                                                9,
                                                                9,
                                                                9)));

                row.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                62));

                JLabel number = new JLabel(
                                "#" +
                                                order.orderNo);

                number.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                16));

                number.setForeground(
                                GREEN);

                JPanel information = new JPanel();

                information.setOpaque(false);

                information.setLayout(
                                new BoxLayout(
                                                information,
                                                BoxLayout.Y_AXIS));

                JLabel customer_name = new JLabel(
                                shortText(
                                                order.customer_name,
                                                18));

                customer_name.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                12));

                customer_name.setForeground(
                                TEXT);

                JLabel time = new JLabel(
                                order.getTime());

                time.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.PLAIN,
                                                10));

                time.setForeground(
                                SUBTEXT);

                information.add(customer_name);

                information.add(
                                Box.createVerticalStrut(3));

                information.add(time);

                JLabel positionLabel = new JLabel(
                                "#" +
                                                position);

                positionLabel.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                11));

                positionLabel.setForeground(
                                SUBTEXT);

                row.add(
                                number,
                                BorderLayout.WEST);

                row.add(
                                information,
                                BorderLayout.CENTER);

                row.add(
                                positionLabel,
                                BorderLayout.EAST);

                return row;
        }

        // ========================================================
        // COMPLETE ORDER
        // ========================================================

        private void completeOrder(
                        ChefOrder order) {

                /*
                 * Any of the three visible active orders can be
                 * completed independently.
                 *
                 * After completion, the order is removed from the
                 * active list by reloading today's pending orders.
                 * The remaining orders stay sorted by Order ID.
                 */

                int result = JOptionPane.showConfirmDialog(
                                this,

                                "Mark Order #" +
                                                order.orderNo +
                                                " as completed?",

                                "Complete Order",

                                JOptionPane.YES_NO_OPTION,

                                JOptionPane.QUESTION_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {

                        return;
                }

                String sql = "UPDATE orders " +
                                "SET status = 'COMPLETED' " +
                                "WHERE order_id = ? " +
                                "AND DATE(order_date) = CURDATE()";

                try (
                                Connection connection = DBConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(sql)) {

                        statement.setInt(
                                        1,
                                        order.orderNo);

                        int updated = statement.executeUpdate();

                        if (updated > 0) {

                                /*
                                 * Reload from database.
                                 *
                                 * The completed order disappears.
                                 * The next pending order automatically
                                 * moves into the active 3, while the
                                 * remaining orders stay in Order ID order.
                                 */

                                loadOrders();

                        } else {

                                JOptionPane.showMessageDialog(
                                                this,
                                                "Order was not updated.",
                                                "Update Error",
                                                JOptionPane.WARNING_MESSAGE);
                        }

                } catch (SQLException ex) {

                        showDatabaseError(ex);
                }
        }

        // ========================================================
        // HELPERS
        // ========================================================

        private String safeText(
                        String value,
                        String defaultValue) {

                if (value == null
                                ||
                                value.trim().isEmpty()) {

                        return defaultValue;
                }

                return value.trim();
        }

        private String shortText(
                        String value,
                        int max) {

                if (value == null) {

                        return "";
                }

                if (value.length() <= max) {

                        return value;
                }

                return value.substring(
                                0,
                                Math.max(
                                                1,
                                                max - 3))
                                + "...";
        }

        private JLabel createSmallTitle(
                        String text) {

                JLabel label = new JLabel(
                                text);

                label.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                10));

                label.setForeground(
                                SUBTEXT);

                return label;
        }

        private void showDatabaseError(
                        SQLException ex) {

                JOptionPane.showMessageDialog(
                                this,

                                "Unable to load today's orders.\n\n" +
                                                ex.getMessage(),

                                "Database Error",

                                JOptionPane.ERROR_MESSAGE);
        }

        // ========================================================
        // OUTLINE BUTTON
        // ========================================================

        private JButton createOutlineButton(
                        String text) {

                JButton button = new JButton(
                                text);

                button.setFont(
                                new Font(
                                                "SansSerif",
                                                Font.BOLD,
                                                12));

                button.setForeground(
                                TEXT);

                button.setBackground(
                                SURFACE);

                button.setFocusPainted(false);

                button.setOpaque(true);

                button.setBorder(
                                new CompoundBorder(
                                                new LineBorder(
                                                                BORDER,
                                                                1,
                                                                true),

                                                new EmptyBorder(
                                                                7,
                                                                12,
                                                                7,
                                                                12)));

                return button;
        }

        // ========================================================
        // ROUNDED PANEL
        // ========================================================

        private static class RoundedPanel
                        extends JPanel {

                private final Color color;

                private final int radius;

                RoundedPanel(
                                Color color,
                                int radius) {

                        this.color = color;

                        this.radius = radius;

                        setOpaque(false);
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(
                                        color);

                        g2.fillRoundRect(
                                        0,
                                        0,
                                        getWidth(),
                                        getHeight(),
                                        radius,
                                        radius);

                        g2.dispose();

                        super.paintComponent(g);
                }
        }

        // ========================================================
        // ROUNDED BUTTON
        // ========================================================

        private static class RoundedButton
                        extends JButton {

                RoundedButton(
                                String text) {

                        super(text);

                        setContentAreaFilled(false);

                        setBorderPainted(false);

                        setOpaque(false);
                }

                @Override
                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        Color color = getBackground();

                        if (getModel().isPressed()) {

                                color = color.darker();

                        } else if (getModel().isRollover()) {

                                color = color.brighter();
                        }

                        g2.setColor(color);

                        g2.fillRoundRect(
                                        0,
                                        0,
                                        getWidth(),
                                        getHeight(),
                                        12,
                                        12);

                        g2.dispose();

                        super.paintComponent(g);
                }
        }

        // ========================================================
        // FOOD ITEM MODEL
        // ========================================================

        private static class FoodItem {

                String name;

                int quantity;

                FoodItem(
                                String name,
                                int quantity) {

                        this.name = name;

                        this.quantity = quantity;
                }
        }

        // ========================================================
        // CHEF ORDER MODEL
        // ========================================================

        private static class ChefOrder {

                int orderNo;

                String customer_name = "";

                String customer_phone = "";

                String status = "";

                String note = "";

                LocalDateTime order_date = LocalDateTime.now();

                LinkedList<FoodItem> items = new LinkedList<>();

                String getTime() {

                        return order_date.format(
                                        TIME_FORMAT);
                }
        }

        // ========================================================
        // MAIN
        // ========================================================

        public static void main(
                        String[] args) {

                SwingUtilities.invokeLater(
                                () -> {

                                        ChefPage page = new ChefPage();

                                        page.setExtendedState(
                                                        JFrame.MAXIMIZED_BOTH);

                                        page.setVisible(true);
                                });
        }
}