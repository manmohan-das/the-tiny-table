package managerdashboard.sidebarpages;

import dao.CategoryDAO;
import dao.FoodItemDAO;
import model.Category;
import model.FoodItem;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu extends JPanel {

        // =========================================================
        // COLORS
        // =========================================================

        private final Color CREAM = new Color(250, 245, 235);
        private final Color LIGHT_CREAM = new Color(255, 250, 242);
        private final Color DARK_CREAM = new Color(225, 210, 185);
        private final Color BROWN = new Color(110, 85, 60);
        private final Color TEXT = new Color(45, 40, 35);

        // =========================================================
        // DAO
        // =========================================================

        private final FoodItemDAO foodItemDAO = new FoodItemDAO();
        private final CategoryDAO categoryDAO = new CategoryDAO();

        // =========================================================
        // TABLE
        // =========================================================

        private JTable menuTable;
        private DefaultTableModel tableModel;
        private TableRowSorter<DefaultTableModel> sorter;

        // =========================================================
        // FILTERS
        // =========================================================

        private JTextField searchField;
        private JComboBox<String> categoryBox;
        private JComboBox<String> availabilityBox;

        // SUMMARY

        private JLabel totalItemsLabel;
        private JLabel availableLabel;
        private JLabel outOfStockLabel;

        
        // CATEGORY MAP

        private final Map<String, Integer> categoryMap = new HashMap<>();


        // IMAGE FOLDER

        private final File imageFolder = new File("images");

        // CONSTRUCTOR

        public Menu() {

                setLayout(new BorderLayout());
                setBackground(CREAM);

                if (!imageFolder.exists()) {
                        imageFolder.mkdirs();
                }

                // HEADER

                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(CREAM);
                headerPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                22, 25, 12, 25));

                JPanel titlePanel = new JPanel();
                titlePanel.setLayout(new BoxLayout(
                                titlePanel,
                                BoxLayout.Y_AXIS));
                titlePanel.setBackground(CREAM);

                JLabel title = new JLabel("Menu Management");
                title.setFont(new Font(
                                "Segoe UI",
                                Font.BOLD,
                                30));
                title.setForeground( new Color(76, 58, 42));

                JLabel subtitle = new JLabel(
                                "Manage food items, prices, stock and availability");
                subtitle.setFont(new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                14));
                subtitle.setForeground(new Color(100, 90, 80));

                titlePanel.add(title);
                titlePanel.add(Box.createVerticalStrut(5));
                titlePanel.add(subtitle);

                headerPanel.add(
                                titlePanel,
                                BorderLayout.WEST);

                // TABLE MODEL

                tableModel = new DefaultTableModel(
                                new Object[] {
                                                "ID",
                                                "Item Name",
                                                "Category",
                                                "Price",
                                                "Availability",
                                                "Quantity",
                                                "Image"
                                },
                                0) {

                        @Override
                        public boolean isCellEditable(
                                        int row,
                                        int column) {
                                return false;
                        }
                };

                menuTable = new JTable(tableModel);
                menuTable.setRowHeight(58);
                menuTable.setFont(new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                14));
                menuTable.setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION);
                menuTable.setShowVerticalLines(true);
                menuTable.setShowHorizontalLines(true);
                menuTable.setGridColor(DARK_CREAM);
                menuTable.setIntercellSpacing(
                                new Dimension(1, 1));
                menuTable.setSelectionBackground(
                                new Color(232, 220, 201));
                menuTable.setSelectionForeground(TEXT);

                JTableHeader tableHeader = menuTable.getTableHeader();

                tableHeader.setFont(new Font(
                                "Segoe UI",
                                Font.BOLD,
                                14));
                tableHeader.setBackground(BROWN);
                tableHeader.setForeground(Color.WHITE);
                tableHeader.setPreferredSize(
                                new Dimension(0, 42));

                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(
                                SwingConstants.CENTER);

                menuTable.getColumnModel()
                                .getColumn(0)
                                .setCellRenderer(centerRenderer);

                menuTable.getColumnModel()
                                .getColumn(2)
                                .setCellRenderer(centerRenderer);

                menuTable.getColumnModel()
                                .getColumn(3)
                                .setCellRenderer(centerRenderer);

                menuTable.getColumnModel()
                                .getColumn(4)
                                .setCellRenderer(centerRenderer);

                menuTable.getColumnModel()
                                .getColumn(5)
                                .setCellRenderer(centerRenderer);

                menuTable.getColumnModel()
                                .getColumn(6)
                                .setCellRenderer(
                                                new ImageCellRenderer());

                menuTable.getColumnModel()
                                .getColumn(0)
                                .setPreferredWidth(60);

                menuTable.getColumnModel()
                                .getColumn(1)
                                .setPreferredWidth(180);

                menuTable.getColumnModel()
                                .getColumn(2)
                                .setPreferredWidth(130);

                menuTable.getColumnModel()
                                .getColumn(3)
                                .setPreferredWidth(100);

                menuTable.getColumnModel()
                                .getColumn(4)
                                .setPreferredWidth(130);

                menuTable.getColumnModel()
                                .getColumn(5)
                                .setPreferredWidth(90);

                menuTable.getColumnModel()
                                .getColumn(6)
                                .setPreferredWidth(110);

                sorter = new TableRowSorter<>(
                                tableModel);

                menuTable.setRowSorter(sorter);

                // =====================================================
                // SUMMARY CARDS
                // =====================================================

                JPanel cardsPanel = new JPanel(
                                new GridLayout(
                                                1,
                                                3,
                                                15,
                                                0));
                cardsPanel.setBackground(CREAM);

                totalItemsLabel = new JLabel("0");
                availableLabel = new JLabel("0");
                outOfStockLabel = new JLabel("0");

                cardsPanel.add(createModernSummaryCard(
                                "TOTAL ITEMS",
                                totalItemsLabel,
                                BROWN));

                cardsPanel.add(createModernSummaryCard(
                                "AVAILABLE",
                                availableLabel,
                                new Color(70, 120, 75)));

                cardsPanel.add(createModernSummaryCard(
                                "OUT OF STOCK",
                                outOfStockLabel,
                                new Color(170, 70, 65)));

                // FILTER BAR

                JPanel filterPanel = new JPanel(new BorderLayout());
                filterPanel.setBackground(Color.WHITE);
                filterPanel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(DARK_CREAM),
                                                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

                JPanel leftFilterPanel = new JPanel(
                                new FlowLayout(FlowLayout.LEFT, 10, 8));
                leftFilterPanel.setBackground(Color.WHITE);

                JLabel searchLabel = new JLabel("Search");
                searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                searchLabel.setForeground(TEXT);

                searchField = new JTextField(17);
                searchField.setPreferredSize(new Dimension(180, 32));

                JLabel categoryLabel = new JLabel("Category");
                categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                categoryLabel.setForeground(TEXT);

                categoryBox = new JComboBox<>();
                categoryBox.setPreferredSize(new Dimension(125, 32));

                JLabel availabilityLabel = new JLabel("Availability");
                availabilityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                availabilityLabel.setForeground(TEXT);

                availabilityBox = new JComboBox<>(
                                new String[] {
                                                "All",
                                                "Available",
                                                "Out of Stock"
                                });
                availabilityBox.setPreferredSize(new Dimension(135, 32));

                JButton refreshButton = createButton("Refresh", BROWN);
                refreshButton.setPreferredSize(new Dimension(125,32));
                refreshButton.setFont(new Font("Segoe UI", Font.BOLD,13));

                leftFilterPanel.add(searchLabel);
                leftFilterPanel.add(searchField);
                leftFilterPanel.add(categoryLabel);
                leftFilterPanel.add(categoryBox);
                leftFilterPanel.add(availabilityLabel);
                leftFilterPanel.add(availabilityBox);

                filterPanel.add(leftFilterPanel, BorderLayout.CENTER);
                filterPanel.add(refreshButton, BorderLayout.EAST);
               
                // TOP CONTENT

                JPanel topContent = new JPanel();
                topContent.setLayout(new BoxLayout(
                                topContent,
                                BoxLayout.Y_AXIS));
                topContent.setBackground(CREAM);
                topContent.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 25, 8, 25));

                topContent.add(headerPanel);
                topContent.add(cardsPanel);
                topContent.add(Box.createVerticalStrut(10));
                topContent.add(filterPanel);

                add(
                                topContent,
                                BorderLayout.NORTH);

                // TABLE AREA

                JScrollPane scrollPane = new JScrollPane(menuTable);

                scrollPane.setBorder(
                                BorderFactory.createLineBorder(
                                                DARK_CREAM));
                scrollPane.getViewport().setBackground(
                                Color.WHITE);

                JPanel tablePanel = new JPanel(
                                new BorderLayout());
                tablePanel.setBackground(CREAM);
                tablePanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 25, 8, 25));

                tablePanel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                // ACTION BUTTONS

                JPanel buttonPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.RIGHT,
                                                10,
                                                5));
                buttonPanel.setBackground(CREAM);

                JButton addButton = createButton(
                                "+ Add Item",
                                new Color(105, 130, 90));

                JButton updateButton = createButton(
                                "Update",
                                new Color(180, 140, 75));

                JButton deleteButton = createButton(
                                "Delete",
                                new Color(170, 85, 75));

                buttonPanel.add(addButton);
                buttonPanel.add(updateButton);
                buttonPanel.add(deleteButton);

                tablePanel.add(
                                buttonPanel,
                                BorderLayout.SOUTH);

                add(
                                tablePanel,
                                BorderLayout.CENTER);

                // LOAD DATA

                loadCategories();
                loadFoodItems();

                // BUTTON ACTIONS

                addButton.addActionListener(
                                e -> showAddDialog());

                updateButton.addActionListener(
                                e -> updateItem());

                deleteButton.addActionListener(
                                e -> deleteItem());

                refreshButton.addActionListener(
                                e -> refreshTable());

                // SEARCH

                searchField.getDocument()
                                .addDocumentListener(
                                                new javax.swing.event.DocumentListener() {

                                                        @Override
                                                        public void insertUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                searchItems();
                                                        }

                                                        @Override
                                                        public void removeUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                searchItems();
                                                        }

                                                        @Override
                                                        public void changedUpdate(
                                                                        javax.swing.event.DocumentEvent e) {
                                                                searchItems();
                                                        }
                                                });

                categoryBox.addActionListener(
                                e -> searchItems());

                availabilityBox.addActionListener(
                                e -> searchItems());

                // DOUBLE CLICK

                menuTable.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseClicked(
                                                        MouseEvent e) {

                                                if (e.getClickCount() == 2) {
                                                        updateItem();
                                                }
                                        }
                                });
        }

        // MODERN SUMMARY CARD

        private JPanel createModernSummaryCard(
                        String title,
                        JLabel valueLabel,
                        Color accent) {

                JPanel panel = new JPanel(
                                new BorderLayout(12, 0));

                panel.setBackground(LIGHT_CREAM);

                panel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                DARK_CREAM),
                                                BorderFactory.createEmptyBorder(
                                                                13, 16, 13, 16)));

                JPanel accentBar = new JPanel();
                accentBar.setBackground(accent);
                accentBar.setPreferredSize(
                                new Dimension(5, 0));

                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(
                                content,
                                BoxLayout.Y_AXIS));
                content.setBackground(LIGHT_CREAM);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font(
                                "Segoe UI",
                                Font.BOLD,
                                12));
                titleLabel.setForeground(
                                new Color(105, 95, 85));

                valueLabel.setFont(new Font(
                                "Segoe UI",
                                Font.BOLD,
                                27));
                valueLabel.setForeground(accent);

                content.add(titleLabel);
                content.add(Box.createVerticalStrut(5));
                content.add(valueLabel);

                panel.add(
                                accentBar,
                                BorderLayout.WEST);

                panel.add(
                                content,
                                BorderLayout.CENTER);

                return panel;
        }
        // BUTTON
        private JButton createButton(
                        String text,
                        Color color) {

                JButton button = new JButton(text);

                button.setBackground(color);
                button.setForeground(Color.WHITE);

                button.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                13));

                button.setFocusPainted(false);
                button.setBorderPainted(false);

                button.setCursor(
                                new Cursor(
                                                Cursor.HAND_CURSOR));

                button.setBorder(
                                BorderFactory.createEmptyBorder(
                                                9, 16, 9, 16));

                button.addMouseListener(
                                new MouseAdapter() {

                                        @Override
                                        public void mouseEntered(
                                                        MouseEvent e) {
                                                button.setBackground(
                                                                color.brighter());
                                        }

                                        @Override
                                        public void mouseExited(
                                                        MouseEvent e) {
                                                button.setBackground(color);
                                        }
                                });

                return button;
        }

        // LOAD CATEGORIES

        private void loadCategories() {

                categoryBox.removeAllItems();

                categoryMap.clear();

                categoryBox.addItem("All");

                List<Category> categories = categoryDAO.getAllCategories();

                for (Category category : categories) {

                        String categoryName = category.getCategoryName();

                        categoryMap.put(
                                        categoryName,
                                        category.getCategoryId());

                        categoryBox.addItem(
                                        categoryName);
                }
        }

        // LOAD FOOD ITEMS

        private void loadFoodItems() {

                tableModel.setRowCount(0);

                List<FoodItem> foodItems = foodItemDAO.getAllFoodItems();

                for (FoodItem food : foodItems) {

                        addFoodRow(food);
                }

                updateSummary();
        }

        // ADD FOOD ROW

        private void addFoodRow(
                        FoodItem food) {

                String categoryName = getCategoryName(
                                food.getCategoryId());

                String availability = food.getAvailable() == 1
                                ? "Available"
                                : "Out of Stock";

                String image = food.getImage();

                tableModel.addRow(
                                new Object[] {
                                                food.getFoodId(),
                                                food.getFoodName(),
                                                categoryName,
                                                "₹" + food.getPrice(),
                                                availability,
                                                food.getAvailableQty(),
                                                image
                                });
        }

        // GET CATEGORY NAME

        private String getCategoryName(
                        int categoryId) {

                for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {

                        if (entry.getValue() == categoryId) {

                                return entry.getKey();
                        }
                }

                return "Unknown";
        }

        // ADD ITEM

        private void showAddDialog() {

                JTextField nameField = new JTextField();

                JComboBox<String> categoryField = new JComboBox<>();

                for (String category : categoryMap.keySet()) {

                        categoryField.addItem(
                                        category);
                }

                JTextField priceField = new JTextField();

                JComboBox<String> availabilityField = new JComboBox<>(
                                new String[] {
                                                "Available",
                                                "Out of Stock"
                                });

                JTextField quantityField = new JTextField("0");

                JLabel imageLabel = new JLabel(
                                "No image selected");

                imageLabel.setPreferredSize(
                                new Dimension(
                                                180,
                                                30));

                JButton chooseImageButton = new JButton(
                                "Choose Image");

                final File[] selectedImage = new File[1];

                chooseImageButton.addActionListener(
                                e -> {

                                        File file = chooseImage();

                                        if (file != null) {

                                                selectedImage[0] = file;

                                                imageLabel.setText(
                                                                file.getName());
                                        }
                                });

                JPanel imagePanel = new JPanel(
                                new BorderLayout(10, 0));

                imagePanel.setBackground(
                                UIManager.getColor(
                                                "Panel.background"));

                imagePanel.add(
                                chooseImageButton,
                                BorderLayout.WEST);

                imagePanel.add(
                                imageLabel,
                                BorderLayout.CENTER);

                JPanel panel = new JPanel(
                                new GridLayout(
                                                6,
                                                2,
                                                10,
                                                10));

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                10,
                                                10,
                                                10,
                                                10));

                panel.add(
                                new JLabel("Item Name:"));

                panel.add(nameField);

                panel.add(
                                new JLabel("Category:"));

                panel.add(categoryField);

                panel.add(
                                new JLabel("Price:"));

                panel.add(priceField);

                panel.add(
                                new JLabel("Availability:"));

                panel.add(availabilityField);

                panel.add(
                                new JLabel("Quantity:"));

                panel.add(quantityField);

                panel.add(
                                new JLabel("Image:"));

                panel.add(imagePanel);

                int result = JOptionPane.showConfirmDialog(
                                this,
                                panel,
                                "Add New Menu Item",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);

                if (result != JOptionPane.OK_OPTION) {
                        return;
                }

                // VALIDATE NAME

                String name = nameField.getText()
                                .trim();

                if (name.isEmpty()) {

                        showWarning(
                                        "Please enter item name.");

                        return;
                }

                // VALIDATE CATEGORY

                if (categoryField.getSelectedItem() == null) {

                        showWarning(
                                        "Please select category.");

                        return;
                }

                String categoryName = categoryField
                                .getSelectedItem()
                                .toString();

                Integer categoryId = categoryMap.get(
                                categoryName);

                if (categoryId == null) {

                        showWarning(
                                        "Invalid category.");

                        return;
                }

                // VALIDATE PRICE

                BigDecimal price;

                try {

                        price = new BigDecimal(
                                        priceField
                                                        .getText()
                                                        .trim());

                        if (price.compareTo(
                                        BigDecimal.ZERO) <= 0) {

                                showWarning(
                                                "Price must be greater than 0.");

                                return;
                        }

                } catch (Exception ex) {

                        showWarning(
                                        "Please enter a valid price.");

                        return;
                }

                // VALIDATE QUANTITY

                int quantity;

                try {

                        quantity = Integer.parseInt(
                                        quantityField
                                                        .getText()
                                                        .trim());

                        if (quantity < 0) {

                                showWarning(
                                                "Quantity cannot be negative.");

                                return;
                        }

                } catch (Exception ex) {

                        showWarning(
                                        "Please enter a valid quantity.");

                        return;
                }

                // AVAILABILITY

                int available = availabilityField
                                .getSelectedItem()
                                .toString()
                                .equals("Available")
                                                ? 1
                                                : 0;

                // IMAGE

                String imagePath = null;

                if (selectedImage[0] != null) {

                        imagePath = copyImage(
                                        selectedImage[0]);

                        if (imagePath == null) {
                                return;
                        }
                }

                // CREATE FOOD OBJECT

                FoodItem food = new FoodItem();

                food.setCategoryId(
                                categoryId);

                food.setFoodName(
                                name);

                food.setPrice(
                                price);

                food.setImage(
                                imagePath);

                food.setAvailable(
                                available);

                food.setAvailableQty(
                                quantity);

                // SAVE

                boolean success = foodItemDAO.addFoodItem(
                                food);

                if (success) {

                        loadFoodItems();

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Menu item added successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);

                } else {

                        showWarning(
                                        "Failed to add menu item.");
                }
        }

        // UPDATE ITEM

        private void updateItem() {

                int viewRow = menuTable.getSelectedRow();

                if (viewRow == -1) {

                        showWarning(
                                        "Please select an item to update.");

                        return;
                }

                int selectedRow = menuTable.convertRowIndexToModel(
                                viewRow);

                int foodId = Integer.parseInt(
                                tableModel
                                                .getValueAt(
                                                                selectedRow,
                                                                0)
                                                .toString());

                FoodItem existingFood = foodItemDAO.getFoodItemById(
                                foodId);

                if (existingFood == null) {

                        showWarning(
                                        "Food item not found.");

                        return;
                }

                JTextField nameField = new JTextField(
                                existingFood.getFoodName());

                JComboBox<String> categoryField = new JComboBox<>();

                for (String category : categoryMap.keySet()) {

                        categoryField.addItem(
                                        category);
                }

                String currentCategory = getCategoryName(
                                existingFood.getCategoryId());

                categoryField.setSelectedItem(
                                currentCategory);

                JTextField priceField = new JTextField(
                                existingFood
                                                .getPrice()
                                                .toString());

                JComboBox<String> availabilityField = new JComboBox<>(
                                new String[] {
                                                "Available",
                                                "Out of Stock"
                                });

                availabilityField.setSelectedItem(
                                existingFood.getAvailable() == 1
                                                ? "Available"
                                                : "Out of Stock");

                JTextField quantityField = new JTextField(
                                String.valueOf(
                                                existingFood
                                                                .getAvailableQty()));

                JLabel imageLabel = new JLabel();

                String currentImage = existingFood.getImage();

                if (currentImage != null
                                && !currentImage.isEmpty()) {

                        imageLabel.setText(
                                        currentImage);

                } else {

                        imageLabel.setText(
                                        "No image selected");
                }

                JButton chooseImageButton = new JButton(
                                "Choose New Image");

                final File[] selectedImage = new File[1];

                chooseImageButton.addActionListener(
                                e -> {

                                        File file = chooseImage();

                                        if (file != null) {

                                                selectedImage[0] = file;

                                                imageLabel.setText(
                                                                file.getName());
                                        }
                                });

                JPanel imagePanel = new JPanel(
                                new BorderLayout(10, 0));

                imagePanel.add(
                                chooseImageButton,
                                BorderLayout.WEST);

                imagePanel.add(
                                imageLabel,
                                BorderLayout.CENTER);

                JPanel panel = new JPanel(
                                new GridLayout(
                                                6,
                                                2,
                                                10,
                                                10));

                panel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                10,
                                                10,
                                                10,
                                                10));

                panel.add(
                                new JLabel("Item Name:"));

                panel.add(nameField);

                panel.add(
                                new JLabel("Category:"));

                panel.add(categoryField);

                panel.add(
                                new JLabel("Price:"));

                panel.add(priceField);

                panel.add(
                                new JLabel("Availability:"));

                panel.add(availabilityField);

                panel.add(
                                new JLabel("Quantity:"));

                panel.add(quantityField);

                panel.add(
                                new JLabel("Image:"));

                panel.add(imagePanel);

                int result = JOptionPane.showConfirmDialog(
                                this,
                                panel,
                                "Update Menu Item",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);

                if (result != JOptionPane.OK_OPTION) {
                        return;
                }

                // VALIDATE NAME

                String name = nameField.getText()
                                .trim();

                if (name.isEmpty()) {

                        showWarning(
                                        "Please enter item name.");

                        return;
                }

                // CATEGORY

                String categoryName = categoryField
                                .getSelectedItem()
                                .toString();

                Integer categoryId = categoryMap.get(
                                categoryName);

                if (categoryId == null) {

                        showWarning(
                                        "Invalid category.");

                        return;
                }

                // PRICE

                BigDecimal price;

                try {

                        price = new BigDecimal(
                                        priceField
                                                        .getText()
                                                        .trim());

                        if (price.compareTo(
                                        BigDecimal.ZERO) <= 0) {

                                showWarning(
                                                "Price must be greater than 0.");

                                return;
                        }

                } catch (Exception ex) {

                        showWarning(
                                        "Please enter a valid price.");

                        return;
                }

                // QUANTITY

                int quantity;

                try {

                        quantity = Integer.parseInt(
                                        quantityField
                                                        .getText()
                                                        .trim());

                        if (quantity < 0) {

                                showWarning(
                                                "Quantity cannot be negative.");

                                return;
                        }

                } catch (Exception ex) {

                        showWarning(
                                        "Please enter a valid quantity.");

                        return;
                }

                // AVAILABILITY

                int available = availabilityField
                                .getSelectedItem()
                                .toString()
                                .equals("Available")
                                                ? 1
                                                : 0;

                // IMAGE

                String imagePath = existingFood.getImage();

                if (selectedImage[0] != null) {

                        imagePath = copyImage(
                                        selectedImage[0]);

                        if (imagePath == null) {
                                return;
                        }
                }

                // UPDATE OBJECT

                existingFood.setCategoryId(
                                categoryId);

                existingFood.setFoodName(
                                name);

                existingFood.setPrice(
                                price);

                existingFood.setImage(
                                imagePath);

                existingFood.setAvailable(
                                available);

                existingFood.setAvailableQty(
                                quantity);

                // SAVE

                boolean success = foodItemDAO.updateFoodItem(
                                existingFood);

                if (success) {

                        loadFoodItems();

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Menu item updated successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);

                } else {

                        showWarning(
                                        "Failed to update menu item.");
                }
        }

        // DELETE ITEM

        private void deleteItem() {

                int viewRow = menuTable.getSelectedRow();

                if (viewRow == -1) {

                        showWarning(
                                        "Please select an item to delete.");

                        return;
                }

                int selectedRow = menuTable.convertRowIndexToModel(
                                viewRow);

                int foodId = Integer.parseInt(
                                tableModel
                                                .getValueAt(
                                                                selectedRow,
                                                                0)
                                                .toString());

                String foodName = tableModel
                                .getValueAt(
                                                selectedRow,
                                                1)
                                .toString();

                int confirm = JOptionPane.showConfirmDialog(
                                this,
                                "Delete \"" +
                                                foodName +
                                                "\"?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                        return;
                }

                boolean success = foodItemDAO.deleteFoodItem(
                                foodId);

                if (success) {

                        loadFoodItems();

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Menu item deleted successfully!",
                                        "Deleted",
                                        JOptionPane.INFORMATION_MESSAGE);

                } else {

                        showWarning(
                                        "Failed to delete item.\n"
                                                        + "It may be used in an order.");
                }
        }

        // CHOOSE IMAGE

        private File chooseImage() {

                JFileChooser fileChooser = new JFileChooser();

                fileChooser.setDialogTitle(
                                "Select Food Image");

                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                                "Image Files (*.jpg, *.jpeg, *.png)",
                                "jpg",
                                "jpeg", "png");

                fileChooser.setFileFilter(  filter);

                int result = fileChooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                        return fileChooser .getSelectedFile();
                }
                return null;
        }

        // COPY IMAGE
        private String copyImage( File sourceFile) {

                try {
                        String originalName = sourceFile.getName();
                        String extension = "";
                        int dot = originalName.lastIndexOf('.');
                        if (dot >= 0) {
                                extension = originalName.substring( dot);
                        }
                        String newFileName = System.currentTimeMillis()+ extension;
                        File destination = new File(imageFolder,newFileName);
                        Files.copy(sourceFile.toPath(),destination.toPath(),StandardCopyOption.REPLACE_EXISTING);
                        return "images/" + newFileName;
                } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this,"Image copy failed:\n"+ ex.getMessage(),"Image Error", JOptionPane.ERROR_MESSAGE);

                        return null;
                 }
        }
        // IMAGE TABLE RENDERER
        private class ImageCellRenderer
                        extends DefaultTableCellRenderer {

                @Override
                public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean hasFocus,
                                int row,
                                int column) {

                        JLabel label = new JLabel();
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setVerticalAlignment(SwingConstants.CENTER);
                        if (value != null && !value.toString().isEmpty()) {

                                File file = new File(value.toString());

                                if (file.exists()) {
                                        ImageIcon icon = new ImageIcon( file.getAbsolutePath());
                                        Image image = icon.getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH);
                                        label.setIcon( new ImageIcon(image));

                                } else {
                                   label.setText( "No Image");
                                }
                        } else {
                                label.setText("No Image");
                        }
                        if (isSelected) {
                                label.setOpaque(true);
                                label.setBackground(table.getSelectionBackground());
                        }
                        return label;
                }
        }
        // SEARCH
        private void searchItems() {
                if (searchField == null|| categoryBox == null|| availabilityBox == null) {
                 return;
                 }

                String searchText = searchField.getText().trim().toLowerCase();
                String category = categoryBox .getSelectedItem() .toString();
                String availability = availabilityBox.getSelectedItem().toString();
                sorter.setRowFilter(
                                new RowFilter<DefaultTableModel, Integer>() {
                                        @Override
                                        public boolean include( Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                                                int row = entry.getIdentifier();
                                                String id = tableModel.getValueAt(row, 0) .toString() .toLowerCase();
                                                String name = tableModel.getValueAt(row,1).toString() .toLowerCase();
                                                String itemCategory = tableModel.getValueAt( row, 2) .toString();
                                                String itemAvailability = tableModel.getValueAt(row, 4).toString();
                                                boolean searchMatch = searchText.isEmpty()|| id.contains(searchText)|| name.contains( searchText);

                                                boolean categoryMatch = category.equals("All")|| category.equals(  itemCategory);
                                                boolean availabilityMatch = availability.equals("All")|| availability.equals(itemAvailability);
                                                return searchMatch&& categoryMatch&& availabilityMatch;}});
        }

        // REFRESH

        private void refreshTable() {
                searchField.setText("");
                categoryBox.setSelectedItem("All");
                availabilityBox.setSelectedItem("All");

                loadCategories();
                loadFoodItems();
                sorter.setRowFilter(null);
        }
        // SUMMARY

        private void updateSummary() {
                if (tableModel == null) {
                        return;
                }
                int total = tableModel.getRowCount();
                int available = 0;
                int outOfStock = 0;
                for (int i = 0; i < total; i++) {
                        String status = tableModel.getValueAt(i, 4) .toString();
                        if (status.equals(
                                        "Available")) {
                                available++;

                        } else {
                               outOfStock++;
                        }
                }

                if (totalItemsLabel != null) {
                        totalItemsLabel.setText(String.valueOf(total));
                }
                if (availableLabel != null) {
                      availableLabel.setText(String.valueOf(available));
                }
                if (outOfStockLabel != null) {
                        outOfStockLabel.setText(String.valueOf(outOfStock));
                }
        }

        // WARNING

        private void showWarning(
                        String message) {

                JOptionPane.showMessageDialog(
                                this,
                                message,
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
        }
}