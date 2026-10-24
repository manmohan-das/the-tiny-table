package managerdashboard.sidebarpages;

import dao.CategoryDAO;
import dao.FoodItemDAO;
import model.Category;
import model.FoodItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CategoryManagement extends JPanel {

    // COLORS — matched to Menu.java
    private final Color CREAM = new Color(250, 245, 235);
    private final Color LIGHT_CREAM = new Color(255, 250, 242);
    private final Color DARK_CREAM = new Color(225, 210, 185);
    private final Color BROWN = new Color(110, 85, 60);
    private final Color TEXT = new Color(45, 40, 35);

    // DAO
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final FoodItemDAO foodItemDAO = new FoodItemDAO();

    // TABLE
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // SEARCH
    private JTextField searchField;

    // SUMMARY CARDS
    private JLabel totalCategoriesLabel;
    private JLabel filteredCategoriesLabel;
    private JLabel totalFoodItemsLabel;

    public CategoryManagement() {
        setLayout(new BorderLayout());
        setBackground(CREAM);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CREAM);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 12, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(CREAM);

        JLabel title = new JLabel("Category Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(76, 58, 42));

        JLabel subtitle = new JLabel("Create, update, search and manage food categories");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 90, 80));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // TABLE MODEL
        tableModel = new DefaultTableModel(new Object[]{"Category ID", "Category Name", "Food Items"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(48);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.setShowVerticalLines(true);
        categoryTable.setShowHorizontalLines(true);
        categoryTable.setGridColor(DARK_CREAM);
        categoryTable.setIntercellSpacing(new Dimension(1, 1));
        categoryTable.setSelectionBackground(new Color(232, 220, 201));
        categoryTable.setSelectionForeground(TEXT);
        JTableHeader tableHeader = categoryTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(BROWN);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 42));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        categoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        sorter = new TableRowSorter<>(tableModel);
        categoryTable.setRowSorter(sorter);

        // SUMMARY CARDS
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setBackground(CREAM);

        totalCategoriesLabel = new JLabel("0");
        filteredCategoriesLabel = new JLabel("0");
        totalFoodItemsLabel = new JLabel("0");

        cardsPanel.add(createSummaryCard("TOTAL CATEGORIES", totalCategoriesLabel, BROWN));
        cardsPanel.add(createSummaryCard("SEARCH RESULTS", filteredCategoriesLabel, new Color(70, 120, 75)));
        cardsPanel.add(createSummaryCard("TOTAL FOOD ITEMS", totalFoodItemsLabel, new Color(130, 95, 60)));

        // SEARCH BAR
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_CREAM),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        JPanel leftFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftFilterPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search category");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(TEXT);

        searchField = new JTextField(22);
        searchField.setPreferredSize(new Dimension(260, 32));

        JButton refreshButton = createButton("Refresh", BROWN);
        refreshButton.setPreferredSize(new Dimension(125, 32));

        leftFilterPanel.add(searchLabel);
        leftFilterPanel.add(searchField);
        filterPanel.add(leftFilterPanel, BorderLayout.CENTER);
        filterPanel.add(refreshButton, BorderLayout.EAST);

        // TOP CONTENT
        JPanel topContent = new JPanel();
        topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));
        topContent.setBackground(CREAM);
        topContent.setBorder(BorderFactory.createEmptyBorder(0, 25, 8, 25));
        topContent.add(headerPanel);
        topContent.add(cardsPanel);
        topContent.add(Box.createVerticalStrut(10));
        topContent.add(filterPanel);
        add(topContent, BorderLayout.NORTH);

        // TABLE AREA
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DARK_CREAM));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CREAM);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 8, 25));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // ACTION BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(CREAM);

        JButton addButton = createButton("+ Add Category", new Color(105, 130, 90));
        JButton updateButton = createButton("Update", new Color(180, 140, 75));
        JButton deleteButton = createButton("Delete", new Color(170, 85, 75));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        add(tablePanel, BorderLayout.CENTER);

        // EVENTS
        addButton.addActionListener(e -> addCategory());
        updateButton.addActionListener(e -> updateCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        refreshButton.addActionListener(e -> refreshTable());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchCategories();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchCategories();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchCategories();
            }
        });

        categoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateCategory();
                }
            }
        });

        loadCategories();
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(LIGHT_CREAM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_CREAM),
                BorderFactory.createEmptyBorder(13, 16, 13, 16)));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(5, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(LIGHT_CREAM);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(105, 95, 85));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(accent);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(valueLabel);
        panel.add(accentBar, BorderLayout.WEST);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    // LOAD / REFRESH CATEGORIES AND THEIR FOOD-ITEM COUNTS
    private void loadCategories() {
        tableModel.setRowCount(0);

        List<Category> categories = categoryDAO.getAllCategories();
        List<FoodItem> foodItems = foodItemDAO.getAllFoodItems();

        Map<Integer, Integer> itemCountByCategory = new HashMap<>();
        for (FoodItem item : foodItems) {
            itemCountByCategory.merge(item.getCategoryId(), 1, Integer::sum);
        }

        for (Category category : categories) {
            tableModel.addRow(new Object[]{
                    category.getCategoryId(),
                    category.getCategoryName(),
                    itemCountByCategory.getOrDefault(category.getCategoryId(), 0)
            });
        }

        totalFoodItemsLabel.setText(String.valueOf(foodItems.size()));
        updateSummary();
    }

    // ADD CATEGORY — uses CategoryDAO.addCategory()
    private void addCategory() {
        String enteredName = showCategoryNameDialog("Add New Category", "");
        if (enteredName == null) {
            return;
        }

        String name = enteredName.trim();
        if (name.isEmpty()) {
            showWarning("Please enter a category name.");
            return;
        }

        if (categoryNameExists(name, -1)) {
            showWarning("This category already exists.");
            return;
        }

        Category category = new Category();
        category.setCategoryName(name);

        if (categoryDAO.addCategory(category)) {
            loadCategories();
            JOptionPane.showMessageDialog(this, "Category added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showWarning("Failed to add category. Check the database connection and category table.");
        }
    }

    // UPDATE CATEGORY — uses CategoryDAO.getCategoryById() and updateCategory()
    private void updateCategory() {
        int viewRow = categoryTable.getSelectedRow();
        if (viewRow == -1) {
            showWarning("Please select a category to update.");
            return;
        }

        int modelRow = categoryTable.convertRowIndexToModel(viewRow);
        int categoryId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        Category existingCategory = categoryDAO.getCategoryById(categoryId);

        if (existingCategory == null) {
            showWarning("Category not found. Refresh the table and try again.");
            return;
        }

        String enteredName = showCategoryNameDialog(
                "Update Category", existingCategory.getCategoryName());
        if (enteredName == null) {
            return;
        }

        String name = enteredName.trim();
        if (name.isEmpty()) {
            showWarning("Category name cannot be empty.");
            return;
        }

        if (categoryNameExists(name, categoryId)) {
            showWarning("Another category already uses this name.");
            return;
        }

        existingCategory.setCategoryName(name);
        if (categoryDAO.updateCategory(existingCategory)) {
            loadCategories();
            selectCategoryById(categoryId);
            JOptionPane.showMessageDialog(this, "Category updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showWarning("Failed to update category.");
        }
    }

    // DELETE CATEGORY — uses CategoryDAO.deleteCategory()
    private void deleteCategory() {
        int viewRow = categoryTable.getSelectedRow();
        if (viewRow == -1) {
            showWarning("Please select a category to delete.");
            return;
        }

        int modelRow = categoryTable.convertRowIndexToModel(viewRow);
        int categoryId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        String categoryName = tableModel.getValueAt(modelRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete category \"" + categoryName + "\"?\n\n"
                        + "Deletion may fail if food items are linked to this category.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (categoryDAO.deleteCategory(categoryId)) {
            loadCategories();
            JOptionPane.showMessageDialog(this, "Category deleted successfully.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showWarning("Could not delete category. It may be linked to food items, or a database error occurred.");
        }
    }

    // SEARCH
    private void searchCategories() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 1));
        }
        updateSummary();
    }

    // REFRESH
    private void refreshTable() {
        searchField.setText("");
        loadCategories();
        sorter.setRowFilter(null);
    }

    private void updateSummary() {
        totalCategoriesLabel.setText(String.valueOf(tableModel.getRowCount()));
        filteredCategoriesLabel.setText(String.valueOf(categoryTable.getRowCount()));
    }


    // POLISHED MODAL DIALOG FOR ADDING / RENAMING A CATEGORY
    private String showCategoryNameDialog(String dialogTitle, String initialValue) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        Color headerColor = new Color(92, 68, 47);
        Color fieldBorder = new Color(205, 188, 163);
        String[] result = {null};

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT_CREAM);
        root.setBorder(BorderFactory.createLineBorder(DARK_CREAM, 1));

        JPanel titleBar = new JPanel(new BorderLayout(10, 0));
        titleBar.setBackground(headerColor);
        titleBar.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 12));

        JLabel titleLabel = new JLabel(dialogTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(headerColor);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());

        titleBar.add(titleLabel, BorderLayout.CENTER);
        titleBar.add(closeButton, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(LIGHT_CREAM);
        body.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JLabel fieldLabel = new JLabel("Category name");
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(TEXT);

        JTextField nameField = new JTextField(initialValue == null ? "" : initialValue, 24);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setForeground(TEXT);
        nameField.setBackground(Color.WHITE);
        nameField.setCaretColor(BROWN);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fieldBorder),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        nameField.setPreferredSize(new Dimension(360, 39));

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 8));
        fieldPanel.setOpaque(false);
        fieldPanel.add(fieldLabel, BorderLayout.NORTH);
        fieldPanel.add(nameField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setForeground(BROWN);
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fieldBorder),
                BorderFactory.createEmptyBorder(8, 17, 8, 17)));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = new JButton("Save Category");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(BROWN);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(9, 17, 9, 17));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            result[0] = nameField.getText();
            dialog.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        body.add(fieldPanel, BorderLayout.CENTER);
        body.add(buttonPanel, BorderLayout.SOUTH);

        root.add(titleBar, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private boolean categoryNameExists(String name, int excludedCategoryId) {
        for (Category category : categoryDAO.getAllCategories()) {
            if (category.getCategoryId() != excludedCategoryId
                    && category.getCategoryName() != null
                    && category.getCategoryName().trim().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void selectCategoryById(int categoryId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int currentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            if (currentId == categoryId) {
                int viewRow = categoryTable.convertRowIndexToView(row);
                if (viewRow >= 0) {
                    categoryTable.setRowSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}