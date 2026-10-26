package managerdashboard;

import dao.OrderDAO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import managerdashboard.sidebarpages.Employees;
import managerdashboard.sidebarpages.Orders;
import managerdashboard.sidebarpages.Payments;
import managerdashboard.sidebarpages.Sales;
import managerdashboard.sidebarpages.Settings;
import managerdashboard.sidebarpages.CategoryManagement;
import dao.StoreProfileDAO;
import java.beans.PropertyChangeListener;

public class ManagerDashboard extends JFrame {

        // =====================================================
        // COLORS - CREAM CONTRAST THEME
        // =====================================================

        private final Color SIDEBAR = new Color(232, 220, 196);
        private final Color SIDEBAR_HOVER = new Color(220, 203, 170);
        private final Color SIDEBAR_ACTIVE = new Color(196, 171, 132);
        private final Color SIDEBAR_ACTIVE_TEXT = new Color(70, 50, 30);
        private final Color SIDEBAR_ACCENT = new Color(128, 92, 52);

        private final Color CREAM = new Color(247, 240, 223);
        private final Color LIGHT_CREAM = new Color(255, 249, 236);

        private final Color TABLE_CREAM = new Color(255, 251, 242);
        private final Color TABLE_SELECTED = new Color(232, 217, 185);

        private final Color TEXT = new Color(62, 52, 40);
        private final Color MUTED = new Color(118, 107, 91);
        private final Color BORDER = new Color(216, 200, 168);

        // =====================================================
        // VARIABLES
        // =====================================================

        private JPanel contentPanel;
        private JButton activeMenuButton;

        private Employees employeesPage;
        private managerdashboard.sidebarpages.Menu menuPage;
        private Payments paymentsPage;
        private Settings settingsPage;
        private OrderDAO orderDAO = new OrderDAO();

        private final String managerName;
        private String storeName;
        private JLabel storeNameLogo;

        private final PropertyChangeListener storeProfileListener = event -> {
                if (!(event.getNewValue() instanceof StoreProfileDAO.Profile)) {
                        return;
                }

                StoreProfileDAO.Profile profile = (StoreProfileDAO.Profile) event.getNewValue();

                SwingUtilities.invokeLater(() -> {
                        storeName = profile.getStoreName();
                        setTitle(storeName + " - Manager Dashboard");

                        if (storeNameLogo != null) {
                                storeNameLogo.setText("🍃 " + storeName.toUpperCase());
                        }
                });
        };

        // =====================================================
        // CONSTRUCTOR
        // =====================================================

        public ManagerDashboard(String managerName) {
                this.managerName = (managerName == null || managerName.trim().isEmpty())
                                ? "Manager"
                                : managerName.trim();
                this.storeName = StoreProfileDAO.loadProfile().getStoreName();

                setTitle(storeName + " - Manager Dashboard");

                setSize(1600, 850);

                setMinimumSize(
                                new Dimension(1100, 700));

                setDefaultCloseOperation(
                                JFrame.EXIT_ON_CLOSE);

                setLocationRelativeTo(null);

                // =================================================
                // CREATE PAGES
                // =================================================

                employeesPage = new Employees();
                menuPage = new managerdashboard.sidebarpages.Menu();
                paymentsPage = new Payments();
                settingsPage = new Settings();

                // =================================================
                // MAIN PANEL
                // =================================================

                JPanel mainPanel = new JPanel(
                                new BorderLayout());

                mainPanel.setBackground(CREAM);

                // =================================================
                // SIDEBAR
                // =================================================

                JPanel sidebar = createSidebar();

                // =================================================
                // CONTENT PANEL
                // =================================================

                contentPanel = new JPanel(
                                new BorderLayout());

                contentPanel.setBackground(CREAM);

                // Dashboard opens first
                showDashboard();

                // =================================================
                // ADD COMPONENTS
                // =================================================

                mainPanel.add(
                                sidebar,
                                BorderLayout.WEST);

                mainPanel.add(
                                contentPanel,
                                BorderLayout.CENTER);

                add(mainPanel);

                StoreProfileDAO.addProfileChangeListener(storeProfileListener);

                addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                                StoreProfileDAO.removeProfileChangeListener(storeProfileListener);
                        }
                });
        }

        // =====================================================
        // CREATE SIDEBAR
        // =====================================================

        private JPanel createSidebar() {

                JPanel sidebar = new JPanel(
                                new BorderLayout());

                sidebar.setPreferredSize(
                                new Dimension(245, 0));

                sidebar.setBackground(SIDEBAR);

                // =================================================
                // TOP PANEL
                // =================================================

                JPanel topPanel = new JPanel();

                topPanel.setLayout(
                                new BoxLayout(
                                                topPanel,
                                                BoxLayout.Y_AXIS));

                topPanel.setBackground(SIDEBAR);

                // =================================================
                // LOGO
                // =================================================

                JPanel logoPanel = new JPanel(
                                new BorderLayout());

                logoPanel.setBackground(SIDEBAR);

                logoPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                25,
                                                20,
                                                25,
                                                15));

                storeNameLogo = new JLabel("🍃 " + storeName.toUpperCase());

                storeNameLogo.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                20));

                storeNameLogo.setForeground(
                                new Color(92, 72, 45));

                logoPanel.add(
                                storeNameLogo,
                                BorderLayout.CENTER);

                topPanel.add(logoPanel);

                // =================================================
                // MENU TITLE
                // =================================================

                JLabel menuTitle = new JLabel(
                                "MAIN MENU");

                menuTitle.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                12));

                menuTitle.setForeground(
                                new Color(118, 101, 75));

                menuTitle.setBorder(
                                BorderFactory.createEmptyBorder(
                                                5,
                                                20,
                                                10,
                                                10));

                menuTitle.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                topPanel.add(menuTitle);

                // =================================================
                // SIDEBAR BUTTONS
                // =================================================

                JButton dashboardBtn = createMenuButton(
                                "🏠  Dashboard");

                JButton menuBtn = createMenuButton(
                                "🍽  Menu");

                JButton employeesBtn = createMenuButton(
                                "👥  Employees");

                JButton ordersBtn = createMenuButton(
                                "🧾  Orders");

                JButton salesBtn = createMenuButton(
                                "📈  Sales");

                JButton paymentsBtn = createMenuButton(
                                "💳  Payments");

                JButton settingsBtn = createMenuButton(
                                "⚙  Settings");

                JButton categoryBtn = createMenuButton(
                                "📂 Categories");

                topPanel.add(dashboardBtn);
                topPanel.add(menuBtn);
                topPanel.add(categoryBtn);
                topPanel.add(employeesBtn);
                topPanel.add(ordersBtn);
                topPanel.add(salesBtn);
                topPanel.add(paymentsBtn);
                topPanel.add(settingsBtn);
                setActiveMenuButton(dashboardBtn);

                // =================================================
                // BOTTOM PANEL
                // =================================================

                JPanel bottomPanel = new JPanel();

                bottomPanel.setLayout(
                                new BoxLayout(
                                                bottomPanel,
                                                BoxLayout.Y_AXIS));

                bottomPanel.setBackground(SIDEBAR);

                JButton logoutBtn = createMenuButton(
                                "🚪  Logout");

                bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setBackground(SIDEBAR);

                logoutBtn = createMenuButton("🚪  Logout");

                JLabel managerNameLabel = new JLabel(managerName, SwingConstants.LEFT);
                managerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                managerNameLabel.setForeground(TEXT);

                JLabel managerRoleLabel = new JLabel("Manager", SwingConstants.LEFT);
                managerRoleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                managerRoleLabel.setForeground(MUTED);

                JPanel managerInfoPanel = new JPanel(new GridLayout(2, 1));
                managerInfoPanel.setBackground(SIDEBAR);
                managerInfoPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 0));
                managerInfoPanel.add(managerNameLabel);
                managerInfoPanel.add(managerRoleLabel);
                bottomPanel.add(managerInfoPanel, BorderLayout.NORTH);

                JPanel logoutHolder = new JPanel(new BorderLayout());
                logoutHolder.setOpaque(false);
                logoutHolder.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
                logoutHolder.add(logoutBtn, BorderLayout.CENTER);
                bottomPanel.add(logoutHolder, BorderLayout.SOUTH);

                sidebar.add(
                                topPanel,
                                BorderLayout.NORTH);

                sidebar.add(
                                bottomPanel,
                                BorderLayout.SOUTH);

                // =================================================
                // BUTTON ACTIONS
                // =================================================

                dashboardBtn.addActionListener(e -> {
                        setActiveMenuButton(dashboardBtn);
                        showDashboard();
                });

                // IMPORTANT:
                // Menu button opens Menu.java
                menuBtn.addActionListener(e -> {
                        setActiveMenuButton(menuBtn);
                        showMenuPage();
                });

                categoryBtn.addActionListener(e -> {
                        setActiveMenuButton(categoryBtn);

                        contentPanel.removeAll();

                        CategoryManagement categoryPage = new CategoryManagement();

                        contentPanel.add(
                                        categoryPage,
                                        BorderLayout.CENTER);

                        contentPanel.revalidate();
                        contentPanel.repaint();
                });

                employeesBtn.addActionListener(e -> {
                        setActiveMenuButton(employeesBtn);
                        showEmployeesPage();
                });

                ordersBtn.addActionListener(e -> {
                        setActiveMenuButton(ordersBtn);
                        showOrdersPage();
                });

                salesBtn.addActionListener(e -> {
                        setActiveMenuButton(salesBtn);
                        showSalesPage();
                });

                paymentsBtn.addActionListener(e -> {
                        setActiveMenuButton(paymentsBtn);
                        showPaymentsPage();
                });

                settingsBtn.addActionListener(e -> {
                        setActiveMenuButton(settingsBtn);
                        showSettingsPage();
                });

                // =================================================
                // LOGOUT
                // =================================================

                logoutBtn.addActionListener(e -> {

                        int result = JOptionPane.showConfirmDialog(
                                        this,
                                        "Are you sure you want to logout?",
                                        "Logout",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);

                        if (result == JOptionPane.YES_OPTION) {

                                dispose();

                                SwingUtilities.invokeLater(() -> {

                                        try {

                                                Class<?> loginClass = Class.forName("Login");

                                                java.lang.reflect.Constructor<?> constructor = loginClass
                                                                .getDeclaredConstructor();

                                                constructor.setAccessible(true);

                                                JFrame login = (JFrame) constructor.newInstance();

                                                login.setVisible(true);

                                        } catch (Exception ex) {

                                                ex.printStackTrace();

                                                JOptionPane.showMessageDialog(
                                                                null,
                                                                "Unable to open Login page.\n\n"
                                                                                + ex.getClass().getSimpleName()
                                                                                + ": "
                                                                                + ex.getMessage(),
                                                                "Logout Error",
                                                                JOptionPane.ERROR_MESSAGE);
                                        }
                                });
                        }
                });

                return sidebar;
        }

        // =====================================================
        // SIDEBAR BUTTON
        // =====================================================

        private JButton createMenuButton(
                        String text) {

                JButton button = new JButton(text);

                button.setPreferredSize(
                                new Dimension(
                                                225,
                                                44));

                button.setMaximumSize(
                                new Dimension(
                                                Integer.MAX_VALUE,
                                                44));

                button.setMinimumSize(
                                new Dimension(
                                                190,
                                                44));

                button.setAlignmentX(
                                Component.CENTER_ALIGNMENT);

                button.setHorizontalAlignment(
                                SwingConstants.LEFT);

                button.setForeground(TEXT);

                button.setBackground(SIDEBAR);

                button.setFont(
                                new Font(
                                                "Arial",
                                                Font.PLAIN,
                                                15));

                button.setBorder(
                                BorderFactory.createEmptyBorder(
                                                7,
                                                20,
                                                7,
                                                10));

                button.setFocusPainted(false);
                button.setContentAreaFilled(true);
                button.setOpaque(true);
                button.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                // =================================================
                // HOVER + ACTIVE PAGE EFFECT
                // =================================================

                button.addMouseListener(
                                new java.awt.event.MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        java.awt.event.MouseEvent e) {

                                                if (button != activeMenuButton) {
                                                        button.setBackground(
                                                                        SIDEBAR_HOVER);
                                                }
                                        }

                                        @Override
                                        public void mouseExited(
                                                        java.awt.event.MouseEvent e) {

                                                if (button != activeMenuButton) {
                                                        button.setBackground(
                                                                        SIDEBAR);
                                                }
                                        }
                                });

                return button;
        }

        // =====================================================
        // ACTIVE SIDEBAR PAGE
        // =====================================================

        private void setActiveMenuButton(JButton button) {

                if (activeMenuButton != null) {
                        activeMenuButton.setBackground(SIDEBAR);
                        activeMenuButton.setForeground(TEXT);
                        activeMenuButton.setBorder(
                                        BorderFactory.createEmptyBorder(
                                                        7,
                                                        20,
                                                        7,
                                                        10));
                }

                activeMenuButton = button;

                activeMenuButton.setBackground(SIDEBAR_ACTIVE);
                activeMenuButton.setForeground(SIDEBAR_ACTIVE_TEXT);
                activeMenuButton.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createMatteBorder(
                                                                0,
                                                                4,
                                                                0,
                                                                0,
                                                                SIDEBAR_ACCENT),
                                                BorderFactory.createEmptyBorder(
                                                                7,
                                                                16,
                                                                7,
                                                                10)));
        }

        // =====================================================
        // DASHBOARD
        // =====================================================

        private void showDashboard() {

                contentPanel.removeAll();

                JPanel main = new JPanel(
                                new BorderLayout(
                                                0,
                                                15));

                main.setBackground(CREAM);

                main.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20,
                                                30,
                                                15,
                                                30));

                // =================================================
                // HEADER
                // =================================================

                JPanel headerPanel = new JPanel(
                                new BorderLayout());

                headerPanel.setBackground(CREAM);

                JPanel headerLeft = new JPanel();

                headerLeft.setLayout(
                                new BoxLayout(
                                                headerLeft,
                                                BoxLayout.Y_AXIS));

                headerLeft.setBackground(CREAM);

                JLabel welcome = new JLabel(
                                "Good Evening, Manager 👋");

                welcome.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                30));

                welcome.setForeground(TEXT);

                JLabel subtitle = new JLabel(
                                "Here's what's happening in your restaurant today.");

                subtitle.setFont(
                                new Font(
                                                "Arial",
                                                Font.PLAIN,
                                                13));

                subtitle.setForeground(MUTED);

                headerLeft.add(welcome);

                headerLeft.add(
                                Box.createVerticalStrut(4));

                headerLeft.add(subtitle);

                headerPanel.add(
                                headerLeft,
                                BorderLayout.WEST);

                // =================================================
                // DATE
                // =================================================

                JPanel datePanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                0,
                                                10));

                datePanel.setBackground(CREAM);

                JLabel date = new JLabel(
                                "📅  Today");

                date.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                13));

                date.setForeground(TEXT);

                datePanel.add(date);

                headerPanel.add(
                                datePanel,
                                BorderLayout.EAST);

                main.add(
                                headerPanel,
                                BorderLayout.NORTH);

                // =================================================
                // CENTER PANEL
                // =================================================

                JPanel centerPanel = new JPanel(
                                new BorderLayout(
                                                0,
                                                15));

                centerPanel.setBackground(CREAM);

                // =================================================
                // DASHBOARD CARDS
                // =================================================

                JPanel cardsPanel = new JPanel(
                                new GridLayout(
                                                1,
                                                4,
                                                15,
                                                15));

                cardsPanel.setOpaque(false);

                int totalOrders = orderDAO.getTodayOrderCount();
                BigDecimal todaySales = orderDAO.getTodaySales();
                BigDecimal monthlyRevenue = orderDAO.getMonthlyRevenue();

                JPanel card1 = createDashboardCard(
                                "TOTAL ORDERS",
                                String.valueOf(totalOrders),
                                "Today",
                                "🧾");

                int activeEmployees = orderDAO.getActiveEmployeeCount();

                JPanel card2 = createDashboardCard(
                                "EMPLOYEES",
                                String.valueOf(activeEmployees),
                                "Active staff",
                                "👥");

                JPanel card3 = createDashboardCard(
                                "TODAY'S SALES",
                                "₹" + todaySales.toPlainString(),
                                "From orders table",
                                "📈");

                JPanel card4 = createDashboardCard(
                                "REVENUE",
                                "₹" + monthlyRevenue.toPlainString(),
                                "This month",
                                "💰");

                // =================================================
                // EMPLOYEE CARD CLICK
                // =================================================

                card2.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                card2.addMouseListener(
                                new java.awt.event.MouseAdapter() {

                                        @Override
                                        public void mouseClicked(
                                                        java.awt.event.MouseEvent e) {

                                                showEmployeesPage();
                                        }

                                        @Override
                                        public void mouseEntered(
                                                        java.awt.event.MouseEvent e) {

                                                card2.setBackground(
                                                                TABLE_SELECTED);
                                        }

                                        @Override
                                        public void mouseExited(
                                                        java.awt.event.MouseEvent e) {

                                                card2.setBackground(
                                                                LIGHT_CREAM);
                                        }
                                });

                cardsPanel.add(card1);
                cardsPanel.add(card2);
                cardsPanel.add(card3);
                cardsPanel.add(card4);

                centerPanel.add(
                                cardsPanel,
                                BorderLayout.NORTH);

                // =================================================
                // BOTTOM PANEL
                // =================================================

                JPanel bottomPanel = new JPanel(
                                new GridLayout(
                                                1,
                                                2,
                                                15,
                                                15));

                bottomPanel.setOpaque(false);

                // TOP PERFORMING MENU ITEMS
                JPanel topItemsPanel = createSectionPanel();

                JLabel topItemsTitle = new JLabel("Top Performing Menu Items");
                topItemsTitle.setFont(new Font("Serif", Font.BOLD, 19));
                topItemsTitle.setForeground(TEXT);
                topItemsPanel.add(topItemsTitle, BorderLayout.NORTH);

                List<Object[]> topItems = orderDAO.getTopSellingItems(10);

                JPanel itemRows = new JPanel();
                itemRows.setOpaque(false);
                itemRows.setLayout(new BoxLayout(itemRows, BoxLayout.Y_AXIS));

                int maxSold = 1;
                for (Object[] item : topItems) {
                        maxSold = Math.max(maxSold, ((Number) item[1]).intValue());
                }

                if (topItems.isEmpty()) {
                        JLabel emptyLabel = new JLabel("No item sales data yet.");
                        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                        emptyLabel.setForeground(MUTED);
                        itemRows.add(emptyLabel);
                } else {
                        for (int index = 0; index < topItems.size(); index++) {
                                Object[] item = topItems.get(index);
                                String itemName = String.valueOf(item[0]);
                                int sold = ((Number) item[1]).intValue();

                                JPanel itemHeader = new JPanel(new BorderLayout());
                                itemHeader.setOpaque(false);

                                JLabel nameLabel = new JLabel(itemName);
                                nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
                                nameLabel.setForeground(TEXT);

                                JLabel soldLabel = new JLabel(sold + " sold", SwingConstants.RIGHT);
                                soldLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                                soldLabel.setForeground(MUTED);

                                itemHeader.add(nameLabel, BorderLayout.WEST);
                                itemHeader.add(soldLabel, BorderLayout.EAST);

                                JProgressBar bar = new JProgressBar(0, maxSold);
                                bar.setValue(sold);
                                bar.setPreferredSize(new Dimension(0, 14));
                                bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
                                bar.setForeground(SIDEBAR_ACCENT);
                                bar.setBackground(new Color(235, 226, 207));
                                bar.setOpaque(true);
                                bar.setBorder(BorderFactory.createLineBorder(BORDER));
                                bar.setBorderPainted(true);
                                bar.setStringPainted(false);

                                JPanel itemRow = new JPanel(new BorderLayout(0, 6));
                                itemRow.setOpaque(false);
                                itemRow.add(itemHeader, BorderLayout.NORTH);
                                itemRow.add(bar, BorderLayout.CENTER);
                                itemRow.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
                                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

                                itemRows.add(itemRow);
                                if (index < topItems.size() - 1) {
                                        itemRows.add(Box.createVerticalGlue());
                                }
                        }
                }

                JScrollPane itemsScrollPane = new JScrollPane(itemRows);
                itemsScrollPane.setBorder(BorderFactory.createEmptyBorder());
                itemsScrollPane.setHorizontalScrollBarPolicy(
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                itemsScrollPane.getViewport().setBackground(TABLE_CREAM);

                topItemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

                // =================================================
                // WEEKLY SALES
                // =================================================

                JPanel salesPanel = createSectionPanel();

                JLabel salesTitle = new JLabel(
                                "Weekly Sales");

                salesTitle.setFont(
                                new Font(
                                                "Serif",
                                                Font.BOLD,
                                                19));

                salesTitle.setForeground(TEXT);

                salesPanel.add(
                                salesTitle,
                                BorderLayout.NORTH);

                // =================================================
                // GRAPH
                // =================================================

                JPanel graphPanel = new JPanel() {

                        @Override
                        protected void paintComponent(
                                        Graphics g) {

                                super.paintComponent(g);

                                Graphics2D g2 = (Graphics2D) g;

                                g2.setRenderingHint(
                                                RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON);

                                int width = getWidth();
                                int height = getHeight();

                                if (width <= 0 ||
                                                height <= 0) {
                                        return;
                                }

                                int left = 88;
                                int right = width - 20;
                                int top = 20;
                                int bottom = height - 30;

                                // =================================================
                                // GRID
                                // =================================================

                                g2.setColor(BORDER);

                                for (int i = 0; i <= 4; i++) {

                                        int y = top +
                                                        i *
                                                                        (bottom - top)
                                                                        / 4;

                                        g2.drawLine(
                                                        left,
                                                        y,
                                                        right,
                                                        y);
                                }

                                // =================================================
                                // SALES DATA
                                // =================================================

                                BigDecimal[] weeklySales = orderDAO.getWeeklySales();

                                int[] sales = new int[7];
                                int max = 1;

                                for (int i = 0; i < 7; i++) {
                                        sales[i] = weeklySales[i].intValue();
                                        if (sales[i] > max) {
                                                max = sales[i];
                                        }
                                }

                                String[] days = {
                                                "Mon",
                                                "Tue",
                                                "Wed",
                                                "Thu",
                                                "Fri",
                                                "Sat",
                                                "Sun"
                                };

                                max = Math.max(max, 1);
                                double rawStep = Math.max(1.0, max / 4.0);
                                double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
                                double normalizedStep = rawStep / magnitude;

                                double multiplier;
                                if (normalizedStep <= 1) {
                                        multiplier = 1;
                                } else if (normalizedStep <= 2) {
                                        multiplier = 2;
                                } else if (normalizedStep <= 5) {
                                        multiplier = 5;
                                } else {
                                        multiplier = 10;
                                }

                                int step = (int) Math.ceil(multiplier * magnitude);
                                int axisMax = step * 4;
                                g2.setColor(TEXT);
                                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                                FontMetrics metrics = g2.getFontMetrics();

                                for (int i = 0; i <= 4; i++) {
                                        int y = top + i * (bottom - top) / 4;
                                        int amount = axisMax - i * step;
                                        String label = String.format("₹%,d", amount);
                                        int baseline = y + (metrics.getAscent() - metrics.getDescent()) / 2;

                                        g2.drawString(label, left - metrics.stringWidth(label) - 8, baseline);
                                }

                                // =================================================
                                // SALES LINE
                                // =================================================

                                g2.setColor(
                                                new Color(92, 72, 45));

                                g2.setStroke(
                                                new BasicStroke(3));

                                for (int i = 0; i < sales.length - 1; i++) {

                                        int x1 = left +
                                                        i *
                                                                        (right - left)
                                                                        / 6;

                                        int y1 = bottom -
                                                        sales[i] *
                                                                        (bottom - top)
                                                                        / axisMax;

                                        int x2 = left +
                                                        (i + 1) *
                                                                        (right - left)
                                                                        / 6;

                                        int y2 = bottom -
                                                        sales[i + 1] *
                                                                        (bottom - top)
                                                                        / axisMax;

                                        g2.drawLine(
                                                        x1,
                                                        y1,
                                                        x2,
                                                        y2);

                                        g2.fillOval(
                                                        x1 - 4,
                                                        y1 - 4,
                                                        8,
                                                        8);
                                }

                                // =================================================
                                // LAST POINT
                                // =================================================

                                int lastX = right;

                                int lastY = bottom -
                                                sales[6] *
                                                                (bottom - top)
                                                                / axisMax;

                                g2.fillOval(
                                                lastX - 4,
                                                lastY - 4,
                                                8,
                                                8);

                                // =================================================
                                // DAYS
                                // =================================================

                                g2.setColor(TEXT);

                                g2.setFont(
                                                new Font(
                                                                "Arial",
                                                                Font.PLAIN,
                                                                10));

                                for (int i = 0; i < days.length; i++) {

                                        int x = left +
                                                        i *
                                                                        (right - left)
                                                                        / 6;

                                        g2.drawString(
                                                        days[i],
                                                        x - 9,
                                                        bottom + 18);
                                }
                        }
                };

                graphPanel.setBackground(
                                LIGHT_CREAM);

                salesPanel.add(
                                graphPanel,
                                BorderLayout.CENTER);

                // =================================================
                // ADD BOTTOM PANELS
                // =================================================

                bottomPanel.add(topItemsPanel);

                bottomPanel.add(
                                salesPanel);

                centerPanel.add(
                                bottomPanel,
                                BorderLayout.CENTER);

                main.add(
                                centerPanel,
                                BorderLayout.CENTER);

                // =================================================
                // FOOTER
                // =================================================

                JLabel footer = new JLabel(
                                "Good Food • Good Mood 🍃",
                                SwingConstants.CENTER);

                footer.setFont(
                                new Font(
                                                "Serif",
                                                Font.ITALIC,
                                                11));

                footer.setForeground(MUTED);

                main.add(
                                footer,
                                BorderLayout.SOUTH);

                // =================================================
                // SHOW DASHBOARD
                // =================================================

                contentPanel.add(
                                main,
                                BorderLayout.CENTER);

                contentPanel.revalidate();

                contentPanel.repaint();
        }

        // =====================================================
        // DASHBOARD CARD
        // =====================================================

        private JPanel createDashboardCard(
                        String title,
                        String value,
                        String smallText,
                        String icon) {

                JPanel card = new JPanel(
                                new BorderLayout());

                card.setBackground(
                                LIGHT_CREAM);

                card.setBorder(
                                BorderFactory.createCompoundBorder(

                                                BorderFactory.createLineBorder(
                                                                BORDER),

                                                BorderFactory.createEmptyBorder(
                                                                12,
                                                                15,
                                                                12,
                                                                15)));

                // =================================================
                // TOP
                // =================================================

                JPanel top = new JPanel(
                                new BorderLayout());

                top.setOpaque(false);

                JLabel titleLabel = new JLabel(title);

                titleLabel.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                11));

                titleLabel.setForeground(TEXT);

                JLabel iconLabel = new JLabel(icon);

                iconLabel.setFont(
                                new Font(
                                                "Segoe UI Emoji",
                                                Font.PLAIN,
                                                21));

                top.add(
                                titleLabel,
                                BorderLayout.WEST);

                top.add(
                                iconLabel,
                                BorderLayout.EAST);

                card.add(
                                top,
                                BorderLayout.NORTH);

                // =================================================
                // VALUE
                // =================================================

                JLabel valueLabel = new JLabel(value);

                valueLabel.setFont(
                                new Font(
                                                "Arial",
                                                Font.BOLD,
                                                27));

                valueLabel.setForeground(TEXT);

                card.add(
                                valueLabel,
                                BorderLayout.CENTER);

                // =================================================
                // SMALL TEXT
                // =================================================

                JLabel smallLabel = new JLabel(smallText);

                smallLabel.setFont(
                                new Font(
                                                "Arial",
                                                Font.PLAIN,
                                                10));

                smallLabel.setForeground(MUTED);

                card.add(
                                smallLabel,
                                BorderLayout.SOUTH);

                return card;
        }

        // =====================================================
        // SECTION PANEL
        // =====================================================

        private JPanel createSectionPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                0,
                                                8));

                panel.setBackground(
                                LIGHT_CREAM);

                panel.setBorder(
                                BorderFactory.createCompoundBorder(

                                                BorderFactory.createLineBorder(
                                                                BORDER),

                                                BorderFactory.createEmptyBorder(
                                                                12,
                                                                14,
                                                                12,
                                                                14)));

                return panel;
        }

        // =====================================================
        // STATUS RENDERER
        // =====================================================

        private class StatusRenderer
                        extends DefaultTableCellRenderer {

                @Override
                public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column);
                        label.setHorizontalAlignment(
                                        SwingConstants.CENTER);
                        if (isSelected) {
                                label.setBackground(
                                                TABLE_SELECTED);
                                label.setForeground(TEXT);
                        } else {
                                label.setBackground(
                                                TABLE_CREAM);

                                label.setForeground(TEXT);
                        }

                        return label;
                }
        }

        // =====================================================
        // EMPLOYEES PAGE
        // =====================================================

        public void showEmployeesPage() {

                contentPanel.removeAll();

                contentPanel.add(
                                employeesPage,
                                BorderLayout.CENTER);

                contentPanel.revalidate();

                contentPanel.repaint();
        }

        // =====================================================
        // ORDERS PAGE
        // =====================================================

        public void showOrdersPage() {

                contentPanel.removeAll();

                Orders ordersPage = new Orders();

                contentPanel.add(
                                ordersPage,
                                BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
        }

        // =====================================================
        // SALES PAGE
        // =====================================================

        public void showSalesPage() {

                contentPanel.removeAll();

                Sales salesPage = new Sales();

                contentPanel.add(
                                salesPage,
                                BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
        }

        // =====================================================
        // MENU PAGE
        // =====================================================

        public void showMenuPage() {

                contentPanel.removeAll();

                contentPanel.add(
                                menuPage,
                                BorderLayout.CENTER);

                contentPanel.revalidate();

                contentPanel.repaint();
        }

        // =====================================================
        // PAYMENTS PAGE
        // =====================================================

        public void showPaymentsPage() {

                contentPanel.removeAll();

                contentPanel.add(
                                paymentsPage,
                                BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
        }

        // =====================================================
        // SETTING PAGE
        // =====================================================

        public void showSettingsPage() {
                contentPanel.removeAll();
                contentPanel.add(settingsPage, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
        }

}