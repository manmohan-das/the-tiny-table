package employeePanel;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.util.*;

public class MenuAndOrderTable extends JFrame {

        // ================= COLORS =================
        static final Color BG = Color.decode("#F4F0E8");
        static final Color SURFACE = Color.decode("#FFFCF6");
        static final Color MUTED = Color.decode("#EEE8DD");
        static final Color TEXT = Color.decode("#292C29");
        static final Color SUBTEXT = Color.decode("#73756F");
        static final Color GREEN = Color.decode("#496352");
        static final Color SUCCESS = Color.decode("#5F8A6E");
        static final Color RED = Color.decode("#A85F5F");
        static final Color BORDER = Color.decode("#D9D1C4");
        static final Color SIDE = Color.decode("#0F3D26");

        // ================= USER =================
        String employee = "Rohit Kumar";

        // ================= STATE =================
        String category = "All";
        String subCategory = "";

        JTextField search;
        JPanel foodArea;
        JPanel cartList;
        JPanel sidebar;

        JLabel subtotalValue;
        JLabel gstValue;
        JLabel totalValue;

        // ================= SEARCH STATE =================
        boolean managementSearch = false;
        boolean currentBillPage = false;

        final Map<Food, Integer> cart = new LinkedHashMap<>();

        static final java.util.List<Order> orders = new ArrayList<>();

        static int dailyOrderNo = 0;
        static LocalDate orderDate = LocalDate.now();
        static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a");
        static final DateTimeFormatter DB_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // ================= FOOD DATA =================
        // Food is loaded from MySQL at startup.
        static Food[] FOOD = new Food[0];

        static Food food(
                        String name,
                        String category,
                        String sub,
                        double price,
                        String image) {

                return new Food(
                                name,
                                category,
                                sub,
                                price,
                                image);
        }

        // ================= LOAD FOOD FROM MYSQL =================
        void loadFoodFromDatabase() {
                java.util.List<Food> list = new ArrayList<>();
                String sql = "SELECT f.food_id, f.food_name, f.price, f.image, f.available, " +
                                "c.category_name FROM food_items f " +
                                "JOIN categories c ON c.category_id=f.category_id " +
                                "WHERE f.available=1 ORDER BY c.category_name, f.food_id";
                try (Connection c = DB.connect();
                                PreparedStatement p = c.prepareStatement(sql);
                                ResultSet r = p.executeQuery()) {
                        while (r.next()) {
                                list.add(new Food(r.getString("food_name"), r.getString("category_name"),
                                                "", r.getDouble("price"), r.getString("image")));
                        }
                        FOOD = list.toArray(new Food[0]);
                } catch (Exception ex) {
                        FOOD = new Food[0];
                        System.err.println("Unable to load food menu from database: " + ex.getMessage());
                }
        }

        java.util.List<String> loadCategoryNames() {
                java.util.List<String> names = new ArrayList<>();
                String sql = "SELECT category_name FROM categories ORDER BY category_name";
                try (Connection c = DB.connect();
                                PreparedStatement p = c.prepareStatement(sql);
                                ResultSet r = p.executeQuery()) {
                        while (r.next())
                                names.add(r.getString(1));
                } catch (Exception ex) {
                        System.out.println("Category load error: " + ex.getMessage());
                }
                return names;
        }

        // ================= CONSTRUCTOR =================
        public MenuAndOrderTable() {

                setTitle("The Tiny Table");

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setMinimumSize(
                                new Dimension(1050, 650));

                setLayout(
                                new BorderLayout());

                getContentPane().setBackground(BG);

                add(createHeader(), BorderLayout.NORTH);
                add(createMain(), BorderLayout.CENTER);

                setSize(1450, 820);
                setLocationRelativeTo(null);

                loadFoodFromDatabase();
                loadOrdersFromDatabase();
                showFood();
        }

        // ================= HEADER =================
        JPanel createHeader() {

                JPanel header = new JPanel(new BorderLayout());

                header.setBackground(SURFACE);

                header.setBorder(
                                new EmptyBorder(
                                                8, 22, 8, 22));

                header.setPreferredSize(
                                new Dimension(0, 76));

                JPanel logo = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                0,
                                                0));

                logo.setOpaque(false);

                JLabel title = new JLabel("The Tiny Table");

                title.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                32));

                title.setForeground(TEXT);

                logo.add(title);
                logo.add(new Leaf());

                header.add(
                                logo,
                                BorderLayout.WEST);

                return header;
        }

        // ================= MAIN =================
        JPanel createMain() {

                JPanel main = new JPanel(
                                new BorderLayout());

                main.setBackground(BG);

                sidebar = createSidebar();

                main.add(
                                sidebar,
                                BorderLayout.WEST);

                JPanel center = new JPanel(
                                new BorderLayout(12, 0));

                center.setBackground(BG);

                center.setBorder(
                                new EmptyBorder(
                                                16, 14, 16, 14));

                JPanel menu = new JPanel(
                                new BorderLayout());

                menu.setBackground(BG);

                JLabel heading = new JLabel("All Food Items");

                heading.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                34));

                heading.setForeground(GREEN);

                heading.setBorder(
                                new EmptyBorder(
                                                0, 4, 12, 0));

                if (search == null) {
                        search = createSearchField();
                }

                JPanel menuHeading = new JPanel(new BorderLayout(15, 0));
                menuHeading.setOpaque(false);
                menuHeading.setBorder(new EmptyBorder(0, 0, 12, 0));
                menuHeading.add(heading, BorderLayout.WEST);
                menuHeading.add(search, BorderLayout.EAST);

                foodArea = new FoodGrid();

                JScrollPane scroll = new JScrollPane(foodArea);

                scroll.setBorder(null);

                scroll.getVerticalScrollBar()
                                .setUnitIncrement(16);
                scroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.getViewport().addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                                foodArea.revalidate();
                        }
                });

                menu.add(
                                menuHeading,
                                BorderLayout.NORTH);

                menu.add(
                                scroll,
                                BorderLayout.CENTER);

                center.add(
                                menu,
                                BorderLayout.CENTER);

                center.add(
                                createCartPanel(),
                                BorderLayout.EAST);

                main.add(
                                center,
                                BorderLayout.CENTER);

                return main;
        }

        // ================= SIDEBAR =================
        JPanel createSidebar() {
                JPanel p = new JPanel();
                p.setPreferredSize(new Dimension(205, 0));
                p.setBackground(SIDE);
                p.setBorder(new EmptyBorder(20, 10, 12, 10));
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

                addCategory(p, "▦", "All", null);

                String[] icons = { "◈", "◈", "▱", "◇", "◆", "●", "▣" };
                java.util.List<String> cats = loadCategoryNames();
                int iconIndex = 0;
                for (String cat : cats) {
                        addCategory(p, icons[Math.min(iconIndex++, icons.length - 1)], cat, null);
                }

                p.add(Box.createVerticalStrut(12));
                JSeparator line = new JSeparator();
                line.setForeground(new Color(75, 120, 92));
                p.add(line);
                p.add(Box.createVerticalStrut(8));
                p.add(Box.createVerticalGlue());

                StyledButton order = new StyledButton("▣   Order", false);
                order.addActionListener(e -> myOrders());
                p.add(order);

                JSeparator line2 = new JSeparator();
                line2.setForeground(new Color(75, 120, 92));
                p.add(line2);
                p.add(Box.createVerticalStrut(6));

                JPanel employeeButton = new JPanel(new BorderLayout(8, 0));
                employeeButton.setOpaque(true);
                employeeButton.setBackground(SIDE);
                employeeButton.setBorder(new EmptyBorder(6, 5, 6, 5));
                employeeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
                employeeButton.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel ep = new JLabel(circularImage(employeeImagePath(), 44));
                employeeButton.add(ep, BorderLayout.WEST);
                JPanel employeeInfo = new JPanel();
                employeeInfo.setOpaque(false);
                employeeInfo.setLayout(new BoxLayout(employeeInfo, BoxLayout.Y_AXIS));
                JLabel en = new JLabel(employee);
                en.setForeground(Color.WHITE);
                en.setFont(new Font("SansSerif", Font.BOLD, 13));
                JLabel er = new JLabel("Employee");
                er.setForeground(new Color(0xC8D4CB));
                er.setFont(new Font("SansSerif", Font.PLAIN, 11));
                employeeInfo.add(en);
                employeeInfo.add(Box.createVerticalStrut(2));
                employeeInfo.add(er);
                employeeButton.add(employeeInfo, BorderLayout.CENTER);
                employeeButton.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                                employeeButton.setBackground(GREEN);
                        }

                        public void mouseExited(MouseEvent e) {
                                employeeButton.setBackground(SIDE);
                        }

                        public void mouseClicked(MouseEvent e) {
                                employeeMenu(p);
                        }
                });
                p.add(employeeButton);
                return p;
        }

        // ================= CATEGORY =================
        void addCategory(
                        JPanel p,
                        String icon,
                        String name,
                        String[] subs) {

                boolean selected = name.equals(category);

                StyledButton main = new StyledButton(
                                icon + "   " + name,
                                selected &&
                                                subCategory.isEmpty());

                main.addActionListener(e -> {

                        if (name.equals("All")) {

                                category = "All";
                                subCategory = "";

                        } else {

                                if (category.equals(name))
                                        category = "All";
                                else
                                        category = name;

                                subCategory = "";
                        }

                        rebuildSidebar();
                        clearSearch();
                        showFood();
                });

                p.add(main);

                if (selected && subs != null) {

                        for (String sub : subs) {

                                StyledButton child = new StyledButton(
                                                "      " + sub,
                                                sub.equals(subCategory));

                                child.setFont(
                                                new Font(
                                                                "SansSerif",
                                                                Font.PLAIN,
                                                                13));

                                child.setPreferredSize(
                                                new Dimension(
                                                                185,
                                                                32));

                                child.setMaximumSize(
                                                new Dimension(
                                                                Integer.MAX_VALUE,
                                                                32));

                                child.addActionListener(e -> {

                                        subCategory = sub;

                                        rebuildSidebar();
                                        clearSearch();
                                        showFood();
                                });

                                p.add(child);
                        }
                }
        }

        void rebuildSidebar() {

                Container parent = sidebar.getParent();

                parent.remove(sidebar);

                sidebar = createSidebar();

                parent.add(
                                sidebar,
                                BorderLayout.WEST);

                parent.revalidate();
                parent.repaint();
        }

        private JTextField createSearchField() {
                JTextField field = new RoundedTextField();

                field.setPreferredSize(new Dimension(315, 40));
                field.setFont(new Font("SansSerif", Font.PLAIN, 15));
                field.setBackground(SURFACE);
                field.setForeground(TEXT);
                field.setBorder(new EmptyBorder(5, 14, 5, 14));
                field.putClientProperty("JTextField.placeholderText", "Search");

                field.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                                if (managementSearch) {
                                        showTodaysOrders(currentBillPage);
                                } else {
                                        showFood();
                                }
                        }
                });

                return field;
        }

        private void clearSearch() {
                if (search != null) {
                        search.setText("");
                        search.putClientProperty("JTextField.placeholderText", "Search");
                        search.repaint();
                }
        }

        // ================= ROUNDED SEARCH FIELD =================
        static class RoundedTextField extends JTextField {

                RoundedTextField() {
                        super();
                        setOpaque(false);
                        setBorder(new EmptyBorder(5, 14, 5, 14));
                        addFocusListener(new FocusAdapter() {
                                public void focusGained(FocusEvent e) {
                                        repaint();
                                }

                                public void focusLost(FocusEvent e) {
                                        repaint();
                                }
                        });
                }

                protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        int arc = Math.min(20, Math.min(getWidth(), getHeight()) / 2);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                        g2.setColor(BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                        g2.dispose();

                        super.paintComponent(g);

                        // Show "Search" inside the bar without putting it into the text value.
                        if (getText().isEmpty() && !hasFocus()) {
                                Graphics2D p = (Graphics2D) g.create();
                                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                p.setFont(getFont());
                                p.setColor(SUBTEXT);
                                Insets in = getInsets();
                                FontMetrics fm = p.getFontMetrics();
                                p.drawString("Search", in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                                p.dispose();
                        }
                }
        }

        // ================= ROUNDED BUTTON =================
        static class RoundedButton extends JButton {

                RoundedButton(String text) {
                        super(text);
                        setFocusPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);
                }

                protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        int arc = Math.min(18, Math.min(getWidth(), getHeight()) / 2);
                        Color fill = getBackground();

                        if (getModel().isPressed())
                                fill = fill.darker();

                        g2.setColor(fill);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                        g2.dispose();

                        setContentAreaFilled(false);
                        setOpaque(false);
                        super.paintComponent(g);
                }

                protected void paintBorder(Graphics g) {
                        if (getBorder() instanceof LineBorder) {
                                LineBorder b = (LineBorder) getBorder();
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);
                                int arc = Math.min(18, Math.min(getWidth(), getHeight()) / 2);
                                g2.setColor(b.getLineColor());
                                g2.setStroke(new BasicStroke(b.getThickness()));
                                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
                                g2.dispose();
                        }
                }
        }

        // ================= CUSTOM BUTTON =================
        static class StyledButton extends RoundedButton {

                StyledButton(
                                String text,
                                boolean selected) {

                        super(text);

                        setForeground(Color.WHITE);

                        setBackground(
                                        selected
                                                        ? SUCCESS
                                                        : SIDE);

                        setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.BOLD,
                                                        14));

                        setFocusPainted(false);
                        setBorderPainted(false);
                        setContentAreaFilled(false);
                        setOpaque(false);

                        setBorder(
                                        new EmptyBorder(
                                                        8, 10, 8, 10));

                        setHorizontalAlignment(
                                        SwingConstants.LEFT);

                        setAlignmentX(
                                        Component.LEFT_ALIGNMENT);

                        setMaximumSize(
                                        new Dimension(
                                                        Integer.MAX_VALUE,
                                                        47));
                }
        }

        // ================= CART =================
        JPanel createCartPanel() {

                RoundedPanel panel = new RoundedPanel(SURFACE, 16);
                panel.setPreferredSize(new Dimension(365, 0));
                panel.setBorder(new EmptyBorder(14, 14, 14, 14));
                panel.setLayout(new BorderLayout(0, 8));

                JPanel top = new JPanel(new BorderLayout());
                top.setOpaque(false);

                JLabel title = new JLabel("🛒  Order Items");
                title.setFont(new Font("Serif", Font.BOLD, 24));
                title.setForeground(TEXT);

                JButton clear = softButton("🗑  Clear All");
                clear.setForeground(RED);
                clear.addActionListener(e -> {
                        cart.clear();
                        refreshCart();
                });

                top.add(title, BorderLayout.WEST);
                top.add(clear, BorderLayout.EAST);

                cartList = new JPanel();
                cartList.setBackground(SURFACE);
                cartList.setLayout(new BoxLayout(cartList, BoxLayout.Y_AXIS));

                JScrollPane scroll = new JScrollPane(cartList);
                scroll.setBorder(null);
                scroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.getVerticalScrollBar().setUnitIncrement(12);

                // Full-width bottom section. The price rows are kept at the actual
                // left edge instead of being centered by the SOUTH region.
                JPanel bottom = new JPanel(new BorderLayout(0, 8));
                bottom.setBackground(SURFACE);

                JPanel details = new JPanel();
                details.setBackground(SURFACE);
                details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

                JSeparator topBorder = new JSeparator();
                topBorder.setForeground(BORDER);
                topBorder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                details.add(topBorder);
                details.add(Box.createVerticalStrut(6));

                JPanel subtotalRow = priceRow("Total Price", 16, true);
                subtotalValue = getRightValue(subtotalRow);

                JPanel gstRow = priceRow("GST (12%)", 14, false);
                gstValue = getRightValue(gstRow);

                JPanel totalRow = priceRow("Total Amount", 20, true);
                totalValue = getRightValue(totalRow);

                details.add(subtotalRow);
                details.add(gstRow);
                details.add(Box.createVerticalStrut(5));

                JSeparator separator = new JSeparator();
                separator.setForeground(BORDER);
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                details.add(separator);
                details.add(Box.createVerticalStrut(5));
                details.add(totalRow);

                bottom.add(details, BorderLayout.CENTER);

                JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
                buttons.setBackground(SURFACE);
                buttons.setMaximumSize(
                                new Dimension(Integer.MAX_VALUE, 48));

                StyledButton order = new StyledButton("▣  Order", true);
                order.setBackground(GREEN);
                order.setFont(new Font("Serif", Font.BOLD, 16));

                StyledButton cancel = new StyledButton("✕  Cancel", true);
                cancel.setBackground(RED);
                cancel.setFont(new Font("Serif", Font.BOLD, 16));

                order.addActionListener(e -> placeOrder());

                cancel.addActionListener(e -> {
                        cart.clear();
                        refreshCart();
                });

                buttons.add(order);
                buttons.add(cancel);
                bottom.add(buttons, BorderLayout.SOUTH);

                panel.add(top, BorderLayout.NORTH);
                panel.add(scroll, BorderLayout.CENTER);
                panel.add(bottom, BorderLayout.SOUTH);

                return panel;
        }

        JPanel priceRow(String name, int size, boolean bold) {

                // Label stays on the LEFT, amount stays on the RIGHT.
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(SURFACE);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setPreferredSize(new Dimension(330, 40));
                row.setMinimumSize(new Dimension(330, 40));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                row.setBorder(new EmptyBorder(6, 0, 6, 0));

                JLabel left = new JLabel(name);
                left.setHorizontalAlignment(SwingConstants.LEFT);
                left.setFont(new Font(
                                "SansSerif",
                                bold ? Font.BOLD : Font.PLAIN,
                                size));

                if ("GST (12%)".equals(name))
                        left.setForeground(SUBTEXT);
                else
                        left.setForeground(GREEN);

                JLabel right = new JLabel("₹ 0");
                right.setHorizontalAlignment(SwingConstants.RIGHT);
                right.setFont(new Font(
                                "SansSerif",
                                bold ? Font.BOLD : Font.PLAIN,
                                size));

                if ("GST (12%)".equals(name))
                        right.setForeground(SUBTEXT);
                else
                        right.setForeground(GREEN);

                row.add(left, BorderLayout.WEST);
                row.add(right, BorderLayout.EAST);

                return row;
        }

        JLabel getRightValue(JPanel row) {
                Component c = row.getComponent(1);
                return (JLabel) c;
        }

        JButton softButton(String text) {
                JButton b = new RoundedButton(text);
                b.setFont(new Font("SansSerif", Font.BOLD, 12));
                b.setForeground(TEXT);
                b.setBackground(SURFACE);
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                                b.setForeground(RED.brighter());
                        }

                        public void mouseExited(MouseEvent e) {
                                b.setForeground(RED);
                        }
                });
                return b;
        }

        // ================= FOOD =================
        void restoreFoodLayout() {
                Container root = sidebar.getParent();
                Component oldCenter = ((BorderLayout) ((JPanel) root).getLayout())
                                .getLayoutComponent(BorderLayout.CENTER);
                if (oldCenter != null)
                        root.remove(oldCenter);

                JPanel center = new JPanel(new BorderLayout(12, 0));
                center.setBackground(BG);
                center.setBorder(new EmptyBorder(16, 14, 16, 14));

                JPanel menu = new JPanel(new BorderLayout());
                menu.setBackground(BG);

                JLabel heading = new JLabel("All Food Items");
                heading.setFont(new Font("Serif", Font.BOLD, 34));
                heading.setForeground(GREEN);
                heading.setBorder(new EmptyBorder(0, 4, 0, 0));

                if (search == null) {
                        search = createSearchField();
                }

                JPanel menuHeading = new JPanel(new BorderLayout(15, 0));
                menuHeading.setOpaque(false);
                menuHeading.setBorder(new EmptyBorder(0, 0, 12, 0));
                menuHeading.add(heading, BorderLayout.WEST);
                menuHeading.add(search, BorderLayout.EAST);

                foodArea = new FoodGrid();
                JScrollPane scroll = new JScrollPane(foodArea);
                scroll.setBorder(null);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                scroll.setHorizontalScrollBarPolicy(
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.getViewport().addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                                foodArea.revalidate();
                        }
                });

                menu.add(menuHeading, BorderLayout.NORTH);
                menu.add(scroll, BorderLayout.CENTER);
                center.add(menu, BorderLayout.CENTER);
                center.add(createCartPanel(), BorderLayout.EAST);

                root.add(center, BorderLayout.CENTER);
                root.revalidate();
                root.repaint();

                // Do not automatically focus the search field when Menu opens.
                center.setFocusable(true);
                SwingUtilities.invokeLater(center::requestFocusInWindow);

                // Rebuild the food cards first, then rebuild the cart UI from the
                // existing cart map. This keeps previously selected items visible
                // immediately when returning from Checkout / Bill Preview.
                showFood();
                refreshCart();
        }

        void showFood() {

                managementSearch = false;
                currentBillPage = false;
                if (sidebar != null && foodArea != null) {
                        Container root = sidebar.getParent();
                        if (root != null && !SwingUtilities.isDescendingFrom(foodArea, root)) {
                                restoreFoodLayout();
                                return;
                        }
                }

                foodArea.removeAll();

                String query = search.getText()
                                .trim()
                                .toLowerCase();

                for (Food f : FOOD) {

                        boolean catOK = category.equals("All") ||
                                        f.category.equals(category);

                        boolean subOK = subCategory.isEmpty() ||
                                        f.subCategory.equals(
                                                        subCategory);

                        boolean searchOK = query.isEmpty() ||
                                        f.name.toLowerCase()
                                                        .contains(query);

                        if (catOK &&
                                        subOK &&
                                        searchOK) {

                                foodArea.add(
                                                createFoodCard(f));
                        }
                }

                foodArea.revalidate();
                foodArea.repaint();
        }

        // ================= FOOD CARD =================
        JPanel createFoodCard(Food f) {

                RoundedPanel card = new RoundedPanel(
                                SURFACE,
                                13);

                // IMPORTANT:
                // Fixed card size.
                card.setPreferredSize(
                                new Dimension(
                                                280,
                                                160));

                card.setMinimumSize(
                                new Dimension(
                                                280,
                                                160));

                card.setMaximumSize(
                                new Dimension(
                                                280,
                                                160));

                card.setBorder(
                                new EmptyBorder(
                                                8, 8, 8, 8));

                card.setLayout(
                                new BorderLayout(9, 0));

                // FIXED SQUARE IMAGE
                JLabel image = new JLabel();

                image.setPreferredSize(
                                new Dimension(
                                                118,
                                                118));

                image.setMinimumSize(
                                new Dimension(
                                                118,
                                                118));

                image.setMaximumSize(
                                new Dimension(
                                                118,
                                                118));

                image.setHorizontalAlignment(
                                SwingConstants.CENTER);

                ImageIcon icon = loadImage(
                                f.image,
                                118,
                                118);

                if (icon != null) {

                        image.setIcon(icon);

                } else {

                        image.setText("FOOD");
                        image.setOpaque(true);
                        image.setBackground(MUTED);
                        image.setForeground(SUBTEXT);
                }

                JPanel info = new JPanel();

                info.setOpaque(false);

                info.setLayout(
                                new BoxLayout(
                                                info,
                                                BoxLayout.Y_AXIS));

                JLabel name = new JLabel(f.name);

                name.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                16));

                name.setForeground(TEXT);

                JLabel price = new JLabel(
                                "₹ " +
                                                money(f.price));

                price.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                17));

                price.setForeground(TEXT);

                StyledButton add = new StyledButton(
                                "🛒  Add",
                                true);

                add.setBackground(GREEN);

                add.addActionListener(e -> {

                        cart.put(
                                        f,
                                        cart.getOrDefault(
                                                        f,
                                                        0) + 1);

                        refreshCart();
                });

                info.add(name);

                info.add(
                                Box.createVerticalStrut(5));

                info.add(price);

                info.add(
                                Box.createVerticalGlue());

                info.add(add);

                card.add(
                                image,
                                BorderLayout.WEST);

                card.add(
                                info,
                                BorderLayout.CENTER);

                return card;
        }

        // ================= CART UPDATE =================
        void refreshCart() {

                cartList.removeAll();

                double subtotal = 0;

                for (Map.Entry<Food, Integer> entry : cart.entrySet()) {

                        Food f = entry.getKey();
                        int quantity = entry.getValue();
                        subtotal += f.price * quantity;

                        // FIXED CART ROW: height never changes with image size.
                        JPanel row = new JPanel(new BorderLayout(8, 0));
                        row.setBackground(SURFACE);
                        row.setAlignmentX(Component.LEFT_ALIGNMENT);
                        row.setPreferredSize(new Dimension(330, 78));
                        row.setMinimumSize(new Dimension(330, 78));
                        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
                        row.setBorder(new CompoundBorder(
                                        new MatteBorder(0, 0, 1, 0, BORDER),
                                        new EmptyBorder(8, 2, 8, 2)));

                        JLabel image = new JLabel();
                        image.setPreferredSize(new Dimension(56, 56));
                        image.setMinimumSize(new Dimension(56, 56));
                        image.setMaximumSize(new Dimension(56, 56));

                        ImageIcon icon = loadImage(f.image, 56, 56);
                        if (icon != null) {
                                image.setIcon(icon);
                        } else {
                                image.setText("FOOD");
                                image.setHorizontalAlignment(SwingConstants.CENTER);
                                image.setOpaque(true);
                                image.setBackground(MUTED);
                                image.setForeground(SUBTEXT);
                        }

                        JPanel info = new JPanel();
                        info.setOpaque(false);
                        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

                        JLabel name = new JLabel(f.name);
                        name.setFont(new Font("Serif", Font.BOLD, 14));
                        name.setForeground(TEXT);

                        JLabel price = new JLabel("₹ " + money(f.price * quantity));
                        price.setFont(new Font("Serif", Font.PLAIN, 14));
                        price.setForeground(SUBTEXT);

                        info.add(Box.createVerticalStrut(5));
                        info.add(name);
                        info.add(Box.createVerticalStrut(4));
                        info.add(price);

                        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 14));
                        controls.setOpaque(false);

                        JButton minus = new CartButton("−");
                        JButton qty = new CartButton(String.valueOf(quantity));
                        JButton plus = new CartButton("+");
                        JButton delete = new CartButton("✕");
                        delete.setForeground(RED);

                        minus.addActionListener(e -> changeQuantity(f, -1));
                        plus.addActionListener(e -> changeQuantity(f, 1));
                        delete.addActionListener(e -> {
                                cart.remove(f);
                                refreshCart();
                        });

                        controls.add(minus);
                        controls.add(qty);
                        controls.add(plus);
                        controls.add(delete);

                        row.add(image, BorderLayout.WEST);
                        row.add(info, BorderLayout.CENTER);
                        row.add(controls, BorderLayout.EAST);

                        cartList.add(row);
                }

                if (cart.isEmpty()) {
                        JPanel empty = new JPanel();
                        empty.setOpaque(false);
                        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));

                        JLabel icon = new JLabel("🛒", SwingConstants.CENTER);
                        icon.setForeground(GREEN);
                        icon.setFont(new Font("SansSerif", Font.PLAIN, 28));
                        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

                        JLabel title = new JLabel("No items added", SwingConstants.CENTER);
                        title.setFont(new Font("SansSerif", Font.BOLD, 15));
                        title.setForeground(TEXT);
                        title.setAlignmentX(Component.CENTER_ALIGNMENT);

                        JLabel hint = new JLabel("Add food items from menu", SwingConstants.CENTER);
                        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        hint.setForeground(SUBTEXT);
                        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

                        empty.setBorder(new EmptyBorder(28, 10, 28, 10));
                        empty.add(icon);
                        empty.add(Box.createVerticalStrut(8));
                        empty.add(title);
                        empty.add(Box.createVerticalStrut(3));
                        empty.add(hint);
                        cartList.add(empty);
                }

                double gst = subtotal * .12;
                double total = subtotal + gst;

                subtotalValue.setText("₹ " + money(subtotal));
                gstValue.setText("₹ " + money(gst));
                totalValue.setText("₹ " + money(total));

                cartList.revalidate();
                cartList.repaint();
        }

        void changeQuantity(Food f, int change) {
                int q = cart.getOrDefault(f, 0) + change;
                if (q <= 0)
                        cart.remove(f);
                else
                        cart.put(f, q);
                refreshCart();
        }

        static class CartButton extends RoundedButton {
                CartButton(String text) {
                        super(text);
                        setFont(new Font("SansSerif", Font.BOLD, 15));
                        setPreferredSize(new Dimension(30, 30));
                        setMinimumSize(new Dimension(30, 30));
                        setMaximumSize(new Dimension(30, 30));
                        setMargin(new Insets(0, 0, 0, 0));
                        if ("✕".equals(text))
                                setForeground(RED);
                        else if ("+".equals(text) || "−".equals(text))
                                setForeground(GREEN);
                        else
                                setForeground(SUBTEXT);
                        setBackground(SURFACE);
                        setFocusPainted(false);
                        setOpaque(false);
                        setBorder(new LineBorder(BORDER, 1, true));
                }
        }

        // ================= ORDER PREVIEW FLOW =================
        void placeOrder() {
                if (cart.isEmpty()) {
                        showMessage("Order", "Please add at least one food item.");
                        return;
                }
                Map<Food, Integer> snapshot = new LinkedHashMap<>(cart);
                showOrderPreview(snapshot);
        }

        void showOrderPreview(Map<Food, Integer> items) {
                managementSearch = false;
                currentBillPage = false;
                replaceCenter(new OrderPreview(this, items));
        }

        void createOrderFromPreview(
                        Map<Food, Integer> items,
                        String customerName,
                        String phoneNumber,
                        double discountRate,
                        String notes,
                        String paymentMethod) {

                if (!orderDate.equals(LocalDate.now())) {
                        orderDate = LocalDate.now();
                        dailyOrderNo = DB.nextDailyOrderNo();
                }

                double subtotal = 0;
                for (Map.Entry<Food, Integer> e : items.entrySet()) {
                        subtotal += e.getKey().price * e.getValue();
                }

                double discount = subtotal * discountRate;
                double taxable = subtotal - discount;
                double gst = taxable * .12;
                double total = taxable + gst;

                Order order = new Order(
                                0,
                                employee,
                                customerName,
                                phoneNumber,
                                items,
                                subtotal,
                                discount,
                                gst,
                                total,
                                notes,
                                LocalDateTime.now());
                order.paymentMethod = paymentMethod == null ? "CASH" : paymentMethod;

                orders.add(order);
                DB.save(order);
                cart.clear();
                refreshCart();

                showBillPreview(order);
        }

        void showBillPreview(Order order) {
                managementSearch = false;
                currentBillPage = true;
                replaceCenter(new BillPreview(this, order));
        }

        void replaceCenter(JPanel page) {
                Container root = sidebar.getParent();
                if (root == null)
                        return;

                BorderLayout layout = (BorderLayout) ((JPanel) root).getLayout();
                Component oldCenter = layout.getLayoutComponent(BorderLayout.CENTER);
                if (oldCenter != null)
                        root.remove(oldCenter);

                root.add(page, BorderLayout.CENTER);
                root.revalidate();
                root.repaint();
        }

        void backToMenu() {
                managementSearch = false;
                currentBillPage = false;
                clearSearch();
                restoreFoodLayout();
        }

        String createBill(Order o) {
                StringBuilder s = new StringBuilder();

                s.append("          THE TINY TABLE\n");
                s.append("--------------------------------------\n");
                s.append("Order No : #").append(formatOrder(o.number)).append("\n");
                s.append("Employee : ").append(o.employee).append("\n");
                s.append("Customer : ").append(o.customer).append("\n");
                s.append("Phone    : ").append(o.phone).append("\n");
                s.append("--------------------------------------\n");

                for (Map.Entry<Food, Integer> e : o.items.entrySet()) {
                        s.append(e.getKey().name)
                                        .append(" x ").append(e.getValue())
                                        .append(" = ₹").append(money(e.getKey().price * e.getValue()))
                                        .append("\n");
                }

                if (o.notes != null && !o.notes.trim().isEmpty()) {
                        s.append("--------------------------------------\n");
                        s.append("Notes    : ").append(o.notes.replace("\n", " ")).append("\n");
                }

                s.append("--------------------------------------\n");
                s.append("Subtotal : ₹").append(money(o.subtotal)).append("\n");
                s.append("Discount : -₹").append(money(o.discount)).append("\n");
                s.append("GST 12%  : ₹").append(money(o.gst)).append("\n");
                s.append("--------------------------------------\n");
                s.append("TOTAL    : ₹").append(money(o.total));

                return s.toString();
        }

        // ================= PRINT =================
        void printBill(Order order) {

                PrinterJob job = PrinterJob.getPrinterJob();

                job.setJobName(
                                "The Tiny Table - Order #" +
                                                formatOrder(order.number));

                job.setPrintable(
                                (graphics, pageFormat, page) -> {

                                        if (page > 0)
                                                return Printable.NO_SUCH_PAGE;

                                        Graphics2D g = (Graphics2D) graphics;

                                        g.translate(
                                                        pageFormat
                                                                        .getImageableX(),
                                                        pageFormat
                                                                        .getImageableY());

                                        g.setFont(
                                                        new Font(
                                                                        "Monospaced",
                                                                        Font.PLAIN,
                                                                        10));

                                        int y = 20;

                                        for (String line : createBill(order)
                                                        .split("\n")) {

                                                g.drawString(
                                                                line,
                                                                10,
                                                                y);

                                                y += 15;
                                        }

                                        return Printable.PAGE_EXISTS;
                                });

                if (!job.printDialog())
                        return;

                try {

                        job.print();

                        showMessage(
                                        "Print Bill",
                                        "Bill sent to printer.");

                } catch (PrinterException e) {

                        showMessage(
                                        "Print Error",
                                        e.getMessage());
                }
        }

        // ================= TODAY'S ORDERS PAGE =================
        void myOrders() {
                managementSearch = true;
                currentBillPage = false;
                search.setText("");
                search.putClientProperty("JTextField.placeholderText", "Search");
                showTodaysOrders(false);
        }

        void pastBills() {
                managementSearch = true;
                currentBillPage = true;
                search.setText("");
                search.putClientProperty("JTextField.placeholderText", "Search");
                showTodaysOrders(true);
        }

        void showTodaysOrders(boolean billPage) {
                JPanel oldCenter = (JPanel) ((BorderLayout) ((JPanel) sidebar.getParent()).getLayout())
                                .getLayoutComponent(BorderLayout.CENTER);
                if (oldCenter != null)
                        sidebar.getParent().remove(oldCenter);

                TodaysOrders page = new TodaysOrders(this, billPage);
                sidebar.getParent().add(page, BorderLayout.CENTER);
                sidebar.getParent().revalidate();
                sidebar.getParent().repaint();
        }

        void employeeMenu(JPanel parent) {

                JPopupMenu popup = new JPopupMenu();
                popup.setBackground(SURFACE);

                JPanel box = new JPanel();
                box.setPreferredSize(new Dimension(250, 220));
                box.setBackground(SURFACE);
                box.setBorder(new EmptyBorder(14, 15, 14, 15));
                box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

                JLabel photo = new JLabel(circularImage(employeeImagePath(), 58));
                photo.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(photo);
                box.add(Box.createVerticalStrut(7));

                JLabel name = new JLabel(employee);
                name.setFont(new Font("Serif", Font.BOLD, 17));
                name.setForeground(TEXT);
                name.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(name);

                JLabel role = new JLabel("Employee");
                role.setForeground(SUBTEXT);
                role.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(role);
                box.add(Box.createVerticalStrut(12));

                JLabel ordersToday = new JLabel("Today's Orders: " + myOrdersCount());
                ordersToday.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(ordersToday);

                JLabel billsToday = new JLabel("Today's Bills: " + myBillsCount());
                billsToday.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(billsToday);
                box.add(Box.createVerticalGlue());

                JButton logout = redButton("Logout");
                logout.setAlignmentX(Component.CENTER_ALIGNMENT);
                logout.addActionListener(e -> {
                        popup.setVisible(false);
                        dispose();
                });
                box.add(logout);

                popup.add(box);
                popup.show(parent, 10, parent.getHeight() - 230);
        }

        int myOrdersCount() {
                int count = 0;
                for (Order o : orders)
                        if (o.employee.equals(employee))
                                count++;
                return count;
        }

        int myBillsCount() {
                int count = 0;
                for (Order o : orders)
                        if (o.employee.equals(employee) && !o.status.equals("CANCELLED"))
                                count++;
                return count;
        }

        String employeeImagePath() {
                String[] paths = { "assets/employee.png", "assets/rohit_kumar.png", "assets/Rohit Kumar.png",
                                "assets/profile.png" };
                for (String path : paths)
                        if (new File(path).exists())
                                return path;
                return null;
        }

        ImageIcon circularImage(String path, int size) {
                BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = out.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                if (path != null) {
                        Image im = new ImageIcon(path).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        g.drawImage(im, 0, 0, null);
                } else {
                        g.setColor(MUTED);
                        g.fillOval(0, 0, size, size);
                        g.setColor(GREEN);
                        g.setFont(new Font("SansSerif", Font.BOLD, size / 2));
                        String initial = employee.substring(0, 1).toUpperCase();
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString(initial, (size - fm.stringWidth(initial)) / 2,
                                        (size - fm.getHeight()) / 2 + fm.getAscent());
                }
                g.dispose();
                return new ImageIcon(out);
        }

        JButton greenButton(String text) {
                JButton b = new RoundedButton(text);
                b.setFont(new Font("Serif", Font.BOLD, 14));
                b.setForeground(Color.WHITE);
                b.setBackground(GREEN);
                b.setOpaque(true);
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setBorder(new EmptyBorder(8, 14, 8, 14));
                return b;
        }

        JButton redButton(String text) {
                JButton b = greenButton(text);
                b.setBackground(RED);
                return b;
        }

        JButton plainButton(String text) {
                JButton b = new RoundedButton(text);
                b.setFont(new Font("Serif", Font.BOLD, 13));
                b.setForeground(TEXT);
                b.setBackground(SURFACE);
                b.setFocusPainted(false);
                b.setOpaque(false);
                b.setBorder(new LineBorder(BORDER, 1, true));
                return b;
        }

        // ================= IMAGE =================
        ImageIcon loadImage(
                        String name,
                        int width,
                        int height) {

                // Database image column may be NULL
                if (name == null || name.trim().isEmpty()) {
                        return null;
                }

                name = name.trim();

                String[] paths = {
                                name,
                                "resources/" + name,
                                "resources/foodImages/" + name
                };

                for (String path : paths) {

                        if (path == null || path.trim().isEmpty()) {
                                continue;
                        }

                        File file = new File(path);

                        if (!file.exists() || !file.isFile()) {
                                continue;
                        }

                        Image image = new ImageIcon(path)
                                        .getImage()
                                        .getScaledInstance(
                                                        width,
                                                        height,
                                                        Image.SCALE_SMOOTH);

                        return new ImageIcon(image);
                }

                return null;
        }

        // ================= MESSAGE =================
        void showMessage(
                        String title,
                        String text) {

                JDialog dialog = new JDialog(
                                this,
                                title,
                                true);

                dialog.setSize(
                                390,
                                210);

                dialog.setLocationRelativeTo(
                                this);

                RoundedPanel panel = new RoundedPanel(
                                SURFACE,
                                18);

                panel.setBorder(
                                new EmptyBorder(
                                                20, 25, 20, 25));

                panel.setLayout(
                                new BorderLayout(
                                                10, 10));

                JTextArea message = new JTextArea(text);
                message.setFont(new Font("SansSerif", Font.PLAIN, 15));
                message.setForeground(TEXT);
                message.setBackground(SURFACE);
                message.setEditable(false);
                message.setFocusable(false);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);
                message.setOpaque(false);
                message.setBorder(new EmptyBorder(4, 0, 4, 0));

                StyledButton ok = new StyledButton(
                                "OK",
                                true);

                ok.setBackground(GREEN);

                ok.addActionListener(
                                e -> dialog.dispose());

                panel.add(
                                message,
                                BorderLayout.CENTER);

                panel.add(
                                ok,
                                BorderLayout.SOUTH);

                dialog.setContentPane(panel);
                dialog.setVisible(true);
        }

        // ================= FIXED FOOD GRID =================
        class FoodGrid extends JPanel {

                FoodGrid() {
                        setBackground(BG);
                        setLayout(new WrapLayout(FlowLayout.LEFT, 14, 14));
                        setBorder(new EmptyBorder(0, 0, 12, 0));
                }

                public Dimension getPreferredSize() {
                        int width = 1100;
                        if (getParent() != null && getParent().getWidth() > 0) {
                                width = getParent().getWidth();
                        }

                        int available = Math.max(1, width - 28);
                        int columns = Math.max(1, available / 314);
                        int rows = Math.max(1,
                                        (getComponentCount() + columns - 1) / columns);

                        return new Dimension(
                                        width,
                                        Math.max(600, rows * 174 + 20));
                }
        }

        // ================= WRAP LAYOUT =================
        static class WrapLayout extends FlowLayout {
                WrapLayout(int align, int hgap, int vgap) {
                        super(align, hgap, vgap);
                }

                @Override
                public Dimension preferredLayoutSize(Container target) {
                        synchronized (target.getTreeLock()) {
                                int targetWidth = target.getWidth();
                                if (targetWidth <= 0)
                                        targetWidth = 850;

                                int maxWidth = Math.max(1, targetWidth - getHgap() * 2);
                                int x = 0;
                                int rowHeight = 0;
                                int rows = 1;

                                for (Component c : target.getComponents()) {
                                        if (!c.isVisible())
                                                continue;
                                        Dimension d = c.getPreferredSize();
                                        if (x > 0 && x + d.width > maxWidth) {
                                                rows++;
                                                x = 0;
                                                rowHeight = 0;
                                        }
                                        x += d.width + getHgap();
                                        rowHeight = Math.max(rowHeight, d.height);
                                }

                                Insets in = target.getInsets();
                                return new Dimension(
                                                Math.max(targetWidth, 850),
                                                Math.max(600, rows * rowHeight
                                                                + (rows + 1) * getVgap()
                                                                + in.top + in.bottom));
                        }
                }
        }

        // ================= ROUNDED PANEL =================
        static class RoundedPanel
                        extends JPanel {

                Color color;
                int radius;

                RoundedPanel(
                                Color color,
                                int radius) {

                        this.color = color;
                        this.radius = radius;

                        setOpaque(false);
                }

                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(color);

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

        // ================= MINI BUTTON =================
        static class MiniButton
                        extends RoundedButton {

                MiniButton(String text) {

                        super(text);

                        setFont(
                                        new Font(
                                                        "SansSerif",
                                                        Font.BOLD,
                                                        12));

                        setMargin(
                                        new Insets(
                                                        2, 7, 2, 7));

                        setFocusPainted(false);
                }
        }

        // ================= LEAF =================
        class Leaf extends JPanel {

                Leaf() {

                        setOpaque(false);

                        setPreferredSize(
                                        new Dimension(
                                                        48,
                                                        52));
                }

                protected void paintComponent(
                                Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();

                        g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(GREEN);

                        g2.setStroke(
                                        new BasicStroke(
                                                        2.3f));

                        g2.drawLine(
                                        21,
                                        47,
                                        28,
                                        14);

                        g2.fillOval(
                                        25,
                                        7,
                                        16,
                                        25);

                        g2.rotate(
                                        -.55,
                                        22,
                                        25);

                        g2.fillOval(
                                        13,
                                        12,
                                        14,
                                        24);

                        g2.dispose();
                }
        }

        // ================= DATA =================
        public static class Food {

                public String name;
                public String category;
                public String subCategory;
                public double price;
                public String image;

                public Food(
                                String name,
                                String category,
                                String subCategory,
                                double price,
                                String image) {

                        this.name = name;
                        this.category = category;
                        this.subCategory = subCategory;
                        this.price = price;
                        this.image = image;
                }
        }

        public static class Order {

                public int number;
                public String employee;
                public String customer;
                public String phone;
                public Map<Food, Integer> items;
                public double subtotal;
                public double discount;
                public double gst;
                public double total;
                public String notes;
                public String paymentMethod = "CASH";
                public String status = "Pending";
                public LocalDateTime createdAt;

                public Order(int number, String employee, String customer, String phone,
                                Map<Food, Integer> items, double subtotal, double discount,
                                double gst, double total, String notes, LocalDateTime createdAt) {
                        this.number = number;
                        this.employee = employee;
                        this.customer = customer;
                        this.phone = phone;
                        this.items = items;
                        this.subtotal = subtotal;
                        this.discount = discount;
                        this.gst = gst;
                        this.total = total;
                        this.notes = notes == null ? "" : notes;
                        this.createdAt = createdAt;
                }
        }

        // ================= DATABASE =================
        static class DB {
                static Connection connect() throws SQLException {
                        Connection c = util.DBConnection.getConnection();
                        if (c == null) {
                                throw new SQLException("Database connection returned null.");
                        }
                        return c;
                }

                static void init() {
                        // MySQL schema is created separately. Nothing to initialize here.
                }

                static int nextDailyOrderNo() {
                        String sql = "SELECT COALESCE(MAX(order_id),0) FROM orders WHERE DATE(order_date)=CURDATE()";
                        try (Connection c = connect();
                                        PreparedStatement p = c.prepareStatement(sql);
                                        ResultSet r = p.executeQuery()) {
                                return r.next() ? r.getInt(1) : 0;
                        } catch (SQLException e) {
                                return 0;
                        }
                }

                static int findEmployeeId(Connection c, String employeeName) throws SQLException {
                        String sql = "SELECT user_id FROM users WHERE name=? AND role='employee' LIMIT 1";
                        try (PreparedStatement p = c.prepareStatement(sql)) {
                                p.setString(1, employeeName);
                                try (ResultSet r = p.executeQuery()) {
                                        if (r.next())
                                                return r.getInt(1);
                                }
                        }
                        sql = "SELECT user_id FROM users WHERE role='employee' AND status=1 ORDER BY user_id LIMIT 1";
                        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
                                if (r.next())
                                        return r.getInt(1);
                        }
                        throw new SQLException("No active employee found in users table.");
                }

                static void save(Order o) {
                        String orderSql = "INSERT INTO orders(employee_id,customer_name,customer_phone,order_date,subtotal,discount,tax,total_amount,status,note) VALUES(?,?,?,?,?,?,?,?,?,?)";
                        String itemSql = "INSERT INTO order_items(order_id,food_id,quantity,price,subtotal) VALUES(?,?,?,?,?)";
                        try (Connection c = connect()) {
                                c.setAutoCommit(false);
                                int employeeId = findEmployeeId(c, o.employee);
                                try (PreparedStatement p = c.prepareStatement(orderSql,
                                                Statement.RETURN_GENERATED_KEYS)) {
                                        p.setInt(1, employeeId);
                                        p.setString(2, o.customer);
                                        p.setString(3, o.phone == null || o.phone.isEmpty() ? null : o.phone);
                                        p.setTimestamp(4, Timestamp.valueOf(o.createdAt));
                                        p.setDouble(5, o.subtotal);
                                        p.setDouble(6, o.discount);
                                        p.setDouble(7, o.gst);
                                        p.setDouble(8, o.total);
                                        p.setString(9, "Pending");
                                        p.setString(10, o.notes);
                                        p.executeUpdate();
                                        try (ResultSet keys = p.getGeneratedKeys()) {
                                                if (keys.next())
                                                        o.number = keys.getInt(1);
                                        }
                                }
                                try (PreparedStatement p = c.prepareStatement(itemSql)) {
                                        for (Map.Entry<Food, Integer> e : o.items.entrySet()) {
                                                int foodId = getFoodId(c, e.getKey().name);
                                                if (foodId <= 0)
                                                        continue;
                                                p.setInt(1, o.number);
                                                p.setInt(2, foodId);
                                                p.setInt(3, e.getValue());
                                                p.setDouble(4, e.getKey().price);
                                                p.setDouble(5, e.getKey().price * e.getValue());
                                                p.addBatch();
                                        }
                                        p.executeBatch();
                                }
                                String paySql = "INSERT INTO payments(order_id,amount,payment_method,payment_status,payment_date) VALUES(?,?,?,?,?)";
                                try (PreparedStatement p = c.prepareStatement(paySql)) {
                                        p.setInt(1, o.number);
                                        p.setDouble(2, o.total);
                                        p.setString(3, o.paymentMethod);
                                        p.setString(4, "Paid");
                                        p.setTimestamp(5, Timestamp.valueOf(o.createdAt));
                                        p.executeUpdate();
                                }
                                c.commit();
                                o.status = "Pending";
                        } catch (SQLException e) {
                                System.out.println("Save order error: " + e.getMessage());
                        }
                }

                static int getFoodId(Connection c, String name) throws SQLException {
                        try (PreparedStatement p = c
                                        .prepareStatement("SELECT food_id FROM food_items WHERE food_name=? LIMIT 1")) {
                                p.setString(1, name);
                                try (ResultSet r = p.executeQuery()) {
                                        return r.next() ? r.getInt(1) : 0;
                                }
                        }
                }

                static void update(Order o) {
                        String sql = "UPDATE orders SET customer_name=?,customer_phone=?,status=?,subtotal=?,discount=?,tax=?,total_amount=?,note=? WHERE order_id=?";
                        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
                                p.setString(1, o.customer);
                                p.setString(2, o.phone == null || o.phone.isEmpty() ? null : o.phone);
                                p.setString(3, toDbStatus(o.status));
                                p.setDouble(4, o.subtotal);
                                p.setDouble(5, o.discount);
                                p.setDouble(6, o.gst);
                                p.setDouble(7, o.total);
                                p.setString(8, o.notes);
                                p.setInt(9, o.number);
                                p.executeUpdate();
                        } catch (SQLException e) {
                                System.out.println("Update order error: " + e.getMessage());
                        }
                }

                static String toDbStatus(String s) {
                        if (s == null)
                                return "Pending";
                        if (s.equalsIgnoreCase("PLACED"))
                                return "Pending";
                        if (s.equalsIgnoreCase("PREPARING"))
                                return "Preparing";
                        if (s.equalsIgnoreCase("READY"))
                                return "Ready";
                        if (s.equalsIgnoreCase("COMPLETED"))
                                return "Completed";
                        if (s.equalsIgnoreCase("CANCELLED"))
                                return "Cancelled";
                        return s;
                }

                static void updateStatus(Order o) {
                        update(o);
                }

                static void delete(Order o) {
                        try (Connection c = connect()) {
                                try (PreparedStatement p = c
                                                .prepareStatement("DELETE FROM payments WHERE order_id=?")) {
                                        p.setInt(1, o.number);
                                        p.executeUpdate();
                                }
                                try (PreparedStatement p = c
                                                .prepareStatement("DELETE FROM order_items WHERE order_id=?")) {
                                        p.setInt(1, o.number);
                                        p.executeUpdate();
                                }
                                try (PreparedStatement p = c.prepareStatement("DELETE FROM orders WHERE order_id=?")) {
                                        p.setInt(1, o.number);
                                        p.executeUpdate();
                                }
                        } catch (SQLException e) {
                                System.out.println("Delete order error: " + e.getMessage());
                        }
                }

                static void loadInto(java.util.List<Order> target, String employee) {
                        String sql = "SELECT o.order_id,o.customer_name,o.customer_phone,o.order_date,o.subtotal,o.discount,o.tax,o.total_amount,o.status,o.note,u.name employee_name FROM orders o JOIN users u ON u.user_id=o.employee_id WHERE DATE(o.order_date)=CURDATE() AND u.name=? ORDER BY o.order_id";
                        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
                                p.setString(1, employee);
                                try (ResultSet r = p.executeQuery()) {
                                        while (r.next()) {
                                                int id = r.getInt("order_id");
                                                Map<Food, Integer> items = loadItems(c, id);
                                                LocalDateTime dt = r.getTimestamp("order_date").toLocalDateTime();
                                                Order o = new Order(id, r.getString("employee_name"),
                                                                r.getString("customer_name"),
                                                                r.getString("customer_phone"), items,
                                                                r.getDouble("subtotal"), r.getDouble("discount"),
                                                                r.getDouble("tax"), r.getDouble("total_amount"),
                                                                r.getString("note"), dt);
                                                o.status = r.getString("status");
                                                target.add(o);
                                        }
                                }
                        } catch (SQLException e) {
                                System.out.println("Load orders error: " + e.getMessage());
                        }
                }

                static Map<Food, Integer> loadItems(Connection c, int orderId) throws SQLException {
                        Map<Food, Integer> map = new LinkedHashMap<>();
                        String sql = "SELECT oi.quantity,f.food_name,f.price,f.image,c.category_name FROM order_items oi JOIN food_items f ON f.food_id=oi.food_id JOIN categories c ON c.category_id=f.category_id WHERE oi.order_id=? ORDER BY oi.order_item_id";
                        try (PreparedStatement p = c.prepareStatement(sql)) {
                                p.setInt(1, orderId);
                                try (ResultSet r = p.executeQuery()) {
                                        while (r.next()) {
                                                Food f = findFood(r.getString("food_name"));
                                                if (f == null) {
                                                        f = new Food(r.getString("food_name"),
                                                                        r.getString("category_name"), "",
                                                                        r.getDouble("price"), r.getString("image"));
                                                }
                                                map.put(f, r.getInt("quantity"));
                                        }
                                }
                        }
                        return map;
                }

                static Food findFood(String name) {
                        for (Food f : FOOD)
                                if (f.name.equals(name))
                                        return f;
                        return null;
                }
        }

        void loadOrdersFromDatabase() {
                orders.clear();
                try {
                        DB.init();
                        DB.loadInto(orders, employee);
                        dailyOrderNo = DB.nextDailyOrderNo();
                } catch (Exception ex) {
                        dailyOrderNo = 0;
                        System.err.println("Order load error: " + ex.getMessage());
                }
        }

        // ================= HELPERS =================
        static String money(double value) {

                if (value == Math.floor(value))
                        return String.format(
                                        "%.0f",
                                        value);

                return String.format(
                                "%.2f",
                                value);
        }

        static String formatOrder(
                        int number) {

                return String.format(
                                "%03d",
                                number);
        }


}