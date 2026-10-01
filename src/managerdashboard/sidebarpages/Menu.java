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

        // =========================================================
        // SUMMARY
        // =========================================================

        private JLabel totalItemsLabel;
        private JLabel availableLabel;
        private JLabel outOfStockLabel;

        // =========================================================
        // CATEGORY MAP
        // =========================================================

        private final Map<String, Integer> categoryMap = new HashMap<>();

        // =========================================================
        // IMAGE FOLDER
        // =========================================================

        private final File imageFolder = new File("images");

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public Menu() {

                setLayout(new BorderLayout(15, 15));
                setBackground(CREAM);

                // Create images folder if it does not exist
                if (!imageFolder.exists()) {
                        imageFolder.mkdirs();
                }

                // =====================================================
                // TOP PANEL
                // =====================================================

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
                topPanel.setBackground(CREAM);

                topPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                20, 25, 5, 25));

                JLabel title = new JLabel("Menu Management");

                title.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                30));

                title.setForeground(TEXT);

                JLabel subtitle = new JLabel(
                                "Manage restaurant food items, prices, images and availability");

                subtitle.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                15));

                subtitle.setForeground(
                                new Color(100, 90, 80));

                topPanel.add(title);
                topPanel.add(Box.createVerticalStrut(5));
                topPanel.add(subtitle);

                // =====================================================
                // SUMMARY CARDS
                // =====================================================

                JPanel cardsPanel = new JPanel(
                                new GridLayout(1, 3, 15, 0));

                cardsPanel.setBackground(CREAM);

                cardsPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                15, 0, 5, 0));

                totalItemsLabel = new JLabel("0");
                availableLabel = new JLabel("0");
                outOfStockLabel = new JLabel("0");

                cardsPanel.add(
                                createSummaryCard(
                                                "Total Items",
                                                totalItemsLabel));

                cardsPanel.add(
                                createSummaryCard(
                                                "Available",
                                                availableLabel));

                cardsPanel.add(
                                createSummaryCard(
                                                "Out of Stock",
                                                outOfStockLabel));

                topPanel.add(cardsPanel);

                // =====================================================
                // FILTER PANEL
                // =====================================================

                JPanel filterPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                10,
                                                5));

                filterPanel.setBackground(CREAM);

                searchField = new JTextField(18);

                searchField.setToolTipText(
                                "Search by ID or food name");

                categoryBox = new JComboBox<>();
                availabilityBox = new JComboBox<>();

                availabilityBox.addItem("All");
                availabilityBox.addItem("Available");
                availabilityBox.addItem("Out of Stock");

                filterPanel.add(
                                new JLabel("Search:"));

                filterPanel.add(searchField);

                filterPanel.add(
                                new JLabel("Category:"));

                filterPanel.add(categoryBox);

                filterPanel.add(
                                new JLabel("Availability:"));

                filterPanel.add(availabilityBox);

                JButton refreshButton = createButton(
                                "Refresh",
                                BROWN);

                filterPanel.add(refreshButton);

                topPanel.add(filterPanel);

                add(
                                topPanel,
                                BorderLayout.NORTH);

                // =====================================================
                // TABLE
                // =====================================================

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

                menuTable.setRowHeight(60);

                menuTable.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                menuTable.setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION);

                menuTable.setGridColor(DARK_CREAM);

                // =====================================================
                // HEADER
                // =====================================================

                JTableHeader header = menuTable.getTableHeader();

                header.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                14));

                header.setBackground(BROWN);
                header.setForeground(Color.WHITE);
                header.setPreferredSize(
                                new Dimension(
                                                header.getWidth(),
                                                40));

                // =====================================================
                // CENTER ALIGNMENT
                // =====================================================

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

                // =====================================================
                // IMAGE COLUMN
                // =====================================================

                menuTable.getColumnModel()
                                .getColumn(6)
                                .setCellRenderer(
                                                new ImageCellRenderer());

                // =====================================================
                // COLUMN WIDTH
                // =====================================================

                menuTable.getColumnModel()
                                .getColumn(0)
                                .setPreferredWidth(60);

                menuTable.getColumnModel()
                                .getColumn(1)
                                .setPreferredWidth(170);

                menuTable.getColumnModel()
                                .getColumn(2)
                                .setPreferredWidth(130);

                menuTable.getColumnModel()
                                .getColumn(3)
                                .setPreferredWidth(90);

                menuTable.getColumnModel()
                                .getColumn(4)
                                .setPreferredWidth(120);

                menuTable.getColumnModel()
                                .getColumn(5)
                                .setPreferredWidth(90);

                menuTable.getColumnModel()
                                .getColumn(6)
                                .setPreferredWidth(100);

                // =====================================================
                // SORTER
                // =====================================================

                sorter = new TableRowSorter<>(
                                tableModel);

                menuTable.setRowSorter(sorter);

                // =====================================================
                // SCROLL PANE
                // =====================================================

                JScrollPane scrollPane = new JScrollPane(menuTable);

                scrollPane.setBorder(
                                BorderFactory.createLineBorder(
                                                DARK_CREAM));

                JPanel centerPanel = new JPanel(
                                new BorderLayout(10, 10));

                centerPanel.setBackground(CREAM);

                centerPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                0, 25, 10, 25));

                centerPanel.add(
                                scrollPane,
                                BorderLayout.CENTER);

                // =====================================================
                // BUTTON PANEL
                // =====================================================

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

                centerPanel.add(
                                buttonPanel,
                                BorderLayout.SOUTH);

                add(
                                centerPanel,
                                BorderLayout.CENTER);

                // =====================================================
                // LOAD DATA
                // =====================================================

                loadCategories();
                loadFoodItems();

                // =====================================================
                // BUTTON ACTIONS
                // =====================================================

                addButton.addActionListener(
                                e -> showAddDialog());

                updateButton.addActionListener(
                                e -> updateItem());

                deleteButton.addActionListener(
                                e -> deleteItem());

                refreshButton.addActionListener(
                                e -> refreshTable());

                // =====================================================
                // SEARCH
                // =====================================================

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

                // =====================================================
                // DOUBLE CLICK
                // =====================================================

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

        // =========================================================
        // SUMMARY CARD
        // =========================================================

        private JPanel createSummaryCard(
                        String title,
                        JLabel valueLabel) {

                JPanel panel = new JPanel(
                                new BorderLayout());

                panel.setBackground(
                                LIGHT_CREAM);

                panel.setBorder(
                                BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(
                                                                DARK_CREAM),
                                                BorderFactory.createEmptyBorder(
                                                                15, 15, 15, 15)));

                JLabel titleLabel = new JLabel(title);

                titleLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                titleLabel.setForeground(
                                new Color(100, 90, 80));

                valueLabel.setFont(
                                new Font(
                                                "Segoe UI",
                                                Font.BOLD,
                                                26));

                valueLabel.setForeground(
                                BROWN);

                panel.add(
                                titleLabel,
                                BorderLayout.NORTH);

                panel.add(
                                valueLabel,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // BUTTON
        // =========================================================

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

                return button;
        }

        // =========================================================
        // LOAD CATEGORIES
        // =========================================================

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

        // =========================================================
        // LOAD FOOD ITEMS
        // =========================================================

        private void loadFoodItems() {

                tableModel.setRowCount(0);

                List<FoodItem> foodItems = foodItemDAO.getAllFoodItems();

                for (FoodItem food : foodItems) {

                        addFoodRow(food);
                }

                updateSummary();
        }

        // =========================================================
        // ADD FOOD ROW
        // =========================================================

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

        // =========================================================
        // GET CATEGORY NAME
        // =========================================================

        private String getCategoryName(
                        int categoryId) {

                for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {

                        if (entry.getValue() == categoryId) {

                                return entry.getKey();
                        }
                }

                return "Unknown";
        }

        // =========================================================
        // ADD ITEM
        // =========================================================

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

                // =====================================================
                // VALIDATE NAME
                // =====================================================

                String name = nameField.getText()
                                .trim();

                if (name.isEmpty()) {

                        showWarning(
                                        "Please enter item name.");

                        return;
                }

                // =====================================================
                // VALIDATE CATEGORY
                // =====================================================

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

                // =====================================================
                // VALIDATE PRICE
                // =====================================================

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

                // =====================================================
                // VALIDATE QUANTITY
                // =====================================================

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

                // =====================================================
                // AVAILABILITY
                // =====================================================

                int available = availabilityField
                                .getSelectedItem()
                                .toString()
                                .equals("Available")
                                                ? 1
                                                : 0;

                // =====================================================
                // IMAGE
                // =====================================================

                String imagePath = null;

                if (selectedImage[0] != null) {

                        imagePath = copyImage(
                                        selectedImage[0]);

                        if (imagePath == null) {
                                return;
                        }
                }

                // =====================================================
                // CREATE FOOD OBJECT
                // =====================================================

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

                // =====================================================
                // SAVE
                // =====================================================

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

        // =========================================================
        // UPDATE ITEM
        // =========================================================

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

                // =====================================================
                // VALIDATE NAME
                // =====================================================

                String name = nameField.getText()
                                .trim();

                if (name.isEmpty()) {

                        showWarning(
                                        "Please enter item name.");

                        return;
                }

                // =====================================================
                // CATEGORY
                // =====================================================

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

                // =====================================================
                // PRICE
                // =====================================================

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

                // =====================================================
                // QUANTITY
                // =====================================================

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

                // =====================================================
                // AVAILABILITY
                // =====================================================

                int available = availabilityField
                                .getSelectedItem()
                                .toString()
                                .equals("Available")
                                                ? 1
                                                : 0;

                // =====================================================
                // IMAGE
                // =====================================================

                String imagePath = existingFood.getImage();

                if (selectedImage[0] != null) {

                        imagePath = copyImage(
                                        selectedImage[0]);

                        if (imagePath == null) {
                                return;
                        }
                }

                // =====================================================
                // UPDATE OBJECT
                // =====================================================

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

                // =====================================================
                // SAVE
                // =====================================================

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

        // =========================================================
        // DELETE ITEM
        // =========================================================

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

        // =========================================================
        // CHOOSE IMAGE
        // =========================================================

        private File chooseImage() {

                JFileChooser fileChooser = new JFileChooser();

                fileChooser.setDialogTitle(
                                "Select Food Image");

                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                                "Image Files (*.jpg, *.jpeg, *.png)",
                                "jpg",
                                "jpeg",
                                "png");

                fileChooser.setFileFilter(
                                filter);

                int result = fileChooser.showOpenDialog(
                                this);

                if (result == JFileChooser.APPROVE_OPTION) {

                        return fileChooser
                                        .getSelectedFile();
                }

                return null;
        }

        // =========================================================
        // COPY IMAGE
        // =========================================================

        private String copyImage(
                        File sourceFile) {

                try {

                        String originalName = sourceFile.getName();

                        String extension = "";

                        int dot = originalName.lastIndexOf('.');

                        if (dot >= 0) {

                                extension = originalName.substring(
                                                dot);
                        }

                        String newFileName = System.currentTimeMillis()
                                        + extension;

                        File destination = new File(
                                        imageFolder,
                                        newFileName);

                        Files.copy(
                                        sourceFile.toPath(),
                                        destination.toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);

                        return "images/" + newFileName;

                } catch (IOException ex) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "Image copy failed:\n"
                                                        + ex.getMessage(),
                                        "Image Error",
                                        JOptionPane.ERROR_MESSAGE);

                        return null;
                }
        }

        // =========================================================
        // IMAGE TABLE RENDERER
        // =========================================================

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

                        label.setHorizontalAlignment(
                                        SwingConstants.CENTER);

                        label.setVerticalAlignment(
                                        SwingConstants.CENTER);

                        if (value != null
                                        && !value.toString()
                                                        .isEmpty()) {

                                File file = new File(
                                                value.toString());

                                if (file.exists()) {

                                        ImageIcon icon = new ImageIcon(
                                                        file.getAbsolutePath());

                                        Image image = icon.getImage()
                                                        .getScaledInstance(
                                                                        50,
                                                                        50,
                                                                        Image.SCALE_SMOOTH);

                                        label.setIcon(
                                                        new ImageIcon(image));

                                } else {

                                        label.setText(
                                                        "No Image");
                                }

                        } else {

                                label.setText(
                                                "No Image");
                        }

                        if (isSelected) {

                                label.setOpaque(true);

                                label.setBackground(
                                                table.getSelectionBackground());
                        }

                        return label;
                }
        }

        // =========================================================
        // SEARCH
        // =========================================================

        private void searchItems() {

                if (searchField == null
                                || categoryBox == null
                                || availabilityBox == null) {
                        return;
                }

                String searchText = searchField
                                .getText()
                                .trim()
                                .toLowerCase();

                String category = categoryBox
                                .getSelectedItem()
                                .toString();

                String availability = availabilityBox
                                .getSelectedItem()
                                .toString();

                sorter.setRowFilter(
                                new RowFilter<DefaultTableModel, Integer>() {

                                        @Override
                                        public boolean include(
                                                        Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                                                int row = entry.getIdentifier();

                                                String id = tableModel
                                                                .getValueAt(
                                                                                row,
                                                                                0)
                                                                .toString()
                                                                .toLowerCase();

                                                String name = tableModel
                                                                .getValueAt(
                                                                                row,
                                                                                1)
                                                                .toString()
                                                                .toLowerCase();

                                                String itemCategory = tableModel
                                                                .getValueAt(
                                                                                row,
                                                                                2)
                                                                .toString();

                                                String itemAvailability = tableModel
                                                                .getValueAt(
                                                                                row,
                                                                                4)
                                                                .toString();

                                                boolean searchMatch = searchText.isEmpty()
                                                                || id.contains(
                                                                                searchText)
                                                                || name.contains(
                                                                                searchText);

                                                boolean categoryMatch = category.equals("All")
                                                                || category.equals(
                                                                                itemCategory);

                                                boolean availabilityMatch = availability.equals("All")
                                                                || availability.equals(
                                                                                itemAvailability);

                                                return searchMatch
                                                                && categoryMatch
                                                                && availabilityMatch;
                                        }
                                });
        }

        // =========================================================
        // REFRESH
        // =========================================================

        private void refreshTable() {

                searchField.setText("");

                categoryBox.setSelectedItem(
                                "All");

                availabilityBox.setSelectedItem(
                                "All");

                loadCategories();
                loadFoodItems();

                sorter.setRowFilter(null);
        }

        // =========================================================
        // SUMMARY
        // =========================================================

        private void updateSummary() {

                if (tableModel == null) {
                        return;
                }

                int total = tableModel.getRowCount();

                int available = 0;
                int outOfStock = 0;

                for (int i = 0; i < total; i++) {

                        String status = tableModel
                                        .getValueAt(
                                                        i,
                                                        4)
                                        .toString();

                        if (status.equals(
                                        "Available")) {

                                available++;

                        } else {

                                outOfStock++;
                        }
                }

                if (totalItemsLabel != null) {

                        totalItemsLabel.setText(
                                        String.valueOf(total));
                }

                if (availableLabel != null) {

                        availableLabel.setText(
                                        String.valueOf(available));
                }

                if (outOfStockLabel != null) {

                        outOfStockLabel.setText(
                                        String.valueOf(outOfStock));
                }
        }

        // =========================================================
        // WARNING
        // =========================================================

        private void showWarning(
                        String message) {

                JOptionPane.showMessageDialog(
                                this,
                                message,
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
        }
}