package managerdashboard.sidebarpages;

import dao.UserDAO;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.User;

public class Employees extends JPanel {

    private final Color CREAM = new Color(247, 240, 223);
    private final Color WHITE = new Color(255, 252, 246);
    private final Color DARK_BROWN = new Color(76, 58, 42);
    private final Color COFFEE = new Color(124, 91, 54);
    private final Color BUTTON_BROWN = new Color(139, 96, 55);
    private final Color BUTTON_HOVER = new Color(164, 119, 70);
    private final Color DELETE_COLOR = new Color(177, 91, 73);
    private final Color UPDATE_COLOR = new Color(176, 132, 69);
    private final Color TEXT = new Color(55, 46, 38);
    private final Color MUTED = new Color(112, 101, 88);
    private final Color BORDER = new Color(205, 187, 153);
    private final Color TABLE_HEADER = new Color(232, 217, 185);
    private final Color TABLE_ALT = new Color(250, 245, 235);
    private final Color TABLE_SELECTED = new Color(235, 220, 190);

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalCount;
    private JLabel presentCount;
    private JLabel absentCount;
    private String statusFilter;
    private final ArrayList<User> users = new ArrayList<>();
    private final UserDAO userDAO = new UserDAO();

    public Employees() {
        setLayout(new BorderLayout());
        setBackground(CREAM);
        loadUsersFromDatabase();
        createHeader();
        createTable();
        createBottomButtons();
    }

    private void loadUsersFromDatabase() {
        users.clear();
        List<User> userList = userDAO.getAllUsers();
        users.addAll(userList);
    }

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CREAM);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        JLabel title = new JLabel("Employees");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(DARK_BROWN);

        JLabel subtitle = new JLabel("Manage your restaurant team and staff access");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(285, 44));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(WHITE);
        searchField.setForeground(TEXT);
        searchField.setToolTipText("Search employee...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 18));
        searchIcon.setForeground(DARK_BROWN);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        header.add(searchPanel, BorderLayout.EAST);

        JPanel pageTop = new JPanel(new BorderLayout());
        pageTop.setBackground(CREAM);
        pageTop.add(header, BorderLayout.NORTH);

        JPanel summary = createSummaryCards();
        summary.setBorder(BorderFactory.createEmptyBorder(0, 30, 16, 30));
        pageTop.add(summary, BorderLayout.CENTER);
        add(pageTop, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterUsers(); }
            public void removeUpdate(DocumentEvent e) { filterUsers(); }
            public void changedUpdate(DocumentEvent e) { filterUsers(); }
        });
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.setOpaque(false);
        cards.add(createSummaryCard("TEAM SIZE", "All users", 0));
        cards.add(createSummaryCard("PRESENT", "Present today", 1));
        cards.add(createSummaryCard("ABSENT", "Absent today", 2));
        return cards;
    }

    private JPanel createSummaryCard(String caption, String note, int cardIndex) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel heading = new JLabel(caption);
        heading.setFont(new Font("SansSerif", Font.BOLD, 10));
        heading.setForeground(MUTED);

        JLabel count = new JLabel("0");
        count.setFont(new Font("SansSerif", Font.BOLD, 25));
        count.setForeground(DARK_BROWN);

        JLabel detail = new JLabel(note);
        detail.setFont(new Font("SansSerif", Font.PLAIN, 10));
        detail.setForeground(MUTED);

        card.add(heading);
        card.add(Box.createVerticalStrut(2));
        card.add(count);
        card.add(detail);

        if (cardIndex == 0) totalCount = count;
        else if (cardIndex == 1) presentCount = count;
        else absentCount = count;

        final String selectedStatus = cardIndex == 1 ? "Present" : cardIndex == 2 ? "Absent" : null;
        MouseAdapter interaction = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 241, 226));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(WHITE);
            }
            public void mouseClicked(MouseEvent e) {
                statusFilter = selectedStatus;
                filterUsers();
            }
        };
        card.addMouseListener(interaction);
        heading.addMouseListener(interaction);
        count.addMouseListener(interaction);
        detail.addMouseListener(interaction);
        return card;
    }

    private int countByStatus(String status) {
        int count = 0;
        for (User user : users) {
            String current = user.getStatus() == 1 ? "Present" : "Absent";
            if (status.equalsIgnoreCase(current)) count++;
        }
        return count;
    }

    private void createTable() {
        String[] columns = {"User ID", "Name", "Username", "Password", "Role", "Salary", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(44);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setBackground(WHITE);
        userTable.setForeground(TEXT);
        userTable.setSelectionBackground(TABLE_SELECTED);
        userTable.setSelectionForeground(TEXT);
        userTable.setShowGrid(true);
        userTable.setGridColor(BORDER);
        userTable.setIntercellSpacing(new Dimension(1, 1));
        userTable.setFillsViewportHeight(true);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoCreateRowSorter(true);

        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int column) {

                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (selected) setBackground(TABLE_SELECTED);
                else if (row % 2 == 0) setBackground(WHITE);
                else setBackground(TABLE_ALT);

                setForeground(TEXT);
                setFont(table.getFont());

                if (column == 5 && value != null) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText("₹ " + value.toString());
                } else if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (column == 6 && value != null) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(table.getFont().deriveFont(Font.BOLD));
                    if ("Present".equalsIgnoreCase(value.toString())) {
                        setForeground(new Color(55, 120, 78));
                    } else {
                        setForeground(DELETE_COLOR);
                    }
                }
                return this;
            }
        });

        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT);
        header.setPreferredSize(new Dimension(100, 46));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));

        userTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 30, 12, 30),
                BorderFactory.createLineBorder(BORDER)));
        add(scrollPane, BorderLayout.CENTER);
        refreshTable();
    }

    private void refreshTable() {
        if (totalCount != null) totalCount.setText(String.valueOf(users.size()));
        if (presentCount != null) presentCount.setText(String.valueOf(countByStatus("Present")));
        if (absentCount != null) absentCount.setText(String.valueOf(countByStatus("Absent")));
        filterUsers();
    }

    private void filterUsers() {
        String search = searchField.getText().toLowerCase().trim();
        tableModel.setRowCount(0);

        for (User user : users) {
            String status = user.getStatus() == 1 ? "Present" : "Absent";
            String data = user.getUserId() + " " + user.getName() + " "
                    + user.getUsername() + " " + user.getRole() + " "
                    + user.getSalary() + " " + status;

            if (data.toLowerCase().contains(search)
                    && (statusFilter == null || statusFilter.equalsIgnoreCase(status))) {

                tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getName(),
                    user.getUsername(),
                    "********",
                    user.getRole(),
                    user.getSalary() == null ? BigDecimal.ZERO : user.getSalary(),
                    status
                });
            }
        }
    }

    private void createBottomButtons() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setBackground(new Color(238, 228, 208));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(5, 30, 12, 30)));

        JButton addButton = createButton("➕ Add User", BUTTON_BROWN, new Color(187, 145, 98));
        JButton updateButton = createButton("✎ Update", UPDATE_COLOR, new Color(195, 151, 83));
        JButton deleteButton = createButton("🗑 Delete", DELETE_COLOR, new Color(198, 112, 94));

        bottom.add(addButton);
        bottom.add(updateButton);
        bottom.add(deleteButton);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
    }

    private JButton createButton(String text, Color normalColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(145, 44));
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(normalColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(normalColor); }
        });
        return button;
    }

    private void addUser() {
        UserForm form = new UserForm(null);
        form.setVisible(true);

        if (form.isSaved()) {
            User user = form.getUser();
            boolean success = userDAO.addUser(user);

            if (success) {
                loadUsersFromDatabase();
                refreshTable();
                JOptionPane.showMessageDialog(this, "User added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user. Username may already exist.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "No User Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        int userId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        User selectedUser = findUserById(userId);

        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserForm form = new UserForm(selectedUser);
        form.setVisible(true);

        if (form.isSaved()) {
            User updatedUser = form.getUser();
            updatedUser.setUserId(userId);

            boolean success = userDAO.updateUser(updatedUser);

            String newPassword = form.getNewPassword();
            if (success && newPassword != null && !newPassword.isEmpty()) {
                success = userDAO.updatePassword(userId, newPassword);
            }

            if (success) {
                loadUsersFromDatabase();
                refreshTable();
                JOptionPane.showMessageDialog(this, "User updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private User findUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) return user;
        }
        return null;
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "No User Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        int userId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userDAO.deleteUser(userId);
            if (success) {
                loadUsersFromDatabase();
                refreshTable();
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UserForm extends JDialog {
        private JTextField nameField;
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JComboBox<String> roleBox;
        private JTextField salaryField;
        private JComboBox<String> statusBox;
        private boolean saved = false;
        private User user;
        private final boolean editMode;

        UserForm(User existingUser) {
            editMode = existingUser != null;
            setTitle(editMode ? "Update User" : "Add User");
            setSize(520, 530);
            setLocationRelativeTo(Employees.this);
            setModal(true);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(CREAM);
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            int row = 0;

            nameField = addField(formPanel, gbc, "Name:", row++);
            usernameField = addField(formPanel, gbc, "Username:", row++);

            passwordField = new JPasswordField();
            styleTextField(passwordField);
            addComponent(formPanel, gbc, "Password:", passwordField, row++);

            roleBox = new JComboBox<>(new String[]{"manager", "waiter", "cashier", "chef", "employee", "kitchen_staff"});
            styleComboBox(roleBox);
            addComponent(formPanel, gbc, "Role:", roleBox, row++);

            salaryField = addField(formPanel, gbc, "Salary:", row++);

            statusBox = new JComboBox<>(new String[]{"Present", "Absent"});
            styleComboBox(statusBox);
            addComponent(formPanel, gbc, "Status:", statusBox, row++);

            JLabel passwordNote = new JLabel(editMode
                    ? "Leave password blank to keep the current password."
                    : "Password will be stored securely as a hash.");
            passwordNote.setFont(new Font("Arial", Font.PLAIN, 11));
            passwordNote.setForeground(MUTED);
            gbc.gridx = 1;
            gbc.gridy = row++;
            formPanel.add(passwordNote, gbc);

            if (editMode) {
                // usernameField.setEditable(false);
                // usernameField.setBackground(new Color(242, 237, 226));
                nameField.setText(existingUser.getName());
                usernameField.setText(existingUser.getUsername());
                roleBox.setSelectedItem(existingUser.getRole());
                salaryField.setText(existingUser.getSalary() == null ? "0" : existingUser.getSalary().toPlainString());
                statusBox.setSelectedItem(existingUser.getStatus() == 1 ? "Present" : "Absent");
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(CREAM);
            JButton saveButton = createButton(editMode ? "Update" : "Save", BUTTON_BROWN, BUTTON_HOVER);
            JButton cancelButton = createButton("Cancel", DELETE_COLOR, new Color(198, 112, 94));
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            formPanel.add(buttonPanel, gbc);
            add(formPanel);

            saveButton.addActionListener(e -> saveUser());
            cancelButton.addActionListener(e -> dispose());
        }

        private JTextField addField(JPanel panel, GridBagConstraints gbc, String label, int row) {
            JTextField field = new JTextField();
            styleTextField(field);
            addComponent(panel, gbc, label, field, row);
            return field;
        }

        private void styleTextField(JTextField field) {
            field.setBackground(WHITE);
            field.setForeground(TEXT);
            field.setFont(new Font("Arial", Font.PLAIN, 13));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        }

        private void styleComboBox(JComboBox<String> comboBox) {
            comboBox.setBackground(WHITE);
            comboBox.setForeground(TEXT);
            comboBox.setPreferredSize(new Dimension(250, 32));
        }

        private void addComponent(JPanel panel, GridBagConstraints gbc, String label,
                Component component, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            JLabel labelComponent = new JLabel(label);
            labelComponent.setFont(new Font("Arial", Font.BOLD, 13));
            labelComponent.setForeground(TEXT);
            panel.add(labelComponent, gbc);

            gbc.gridx = 1;
            component.setPreferredSize(new Dimension(250, 32));
            panel.add(component, gbc);
        }

        private void saveUser() {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String salary = salaryField.getText().trim();
            String role = roleBox.getSelectedItem().toString();
            String status = statusBox.getSelectedItem().toString();

            if (name.isEmpty() || username.isEmpty() || salary.isEmpty()
                    || (!editMode && password.isEmpty())) {
                JOptionPane.showMessageDialog(this,
                        editMode ? "Please fill Name and Salary."
                                : "Please fill all fields.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal salaryValue = new BigDecimal(salary);
                if (salaryValue.compareTo(BigDecimal.ZERO) < 0) {
                    throw new NumberFormatException();
                }

                user = new User();
                user.setName(name);
                user.setUsername(username);
                user.setPassword(password);
                user.setSalary(salaryValue);
                user.setRole(role);
                user.setStatus(status.equals("Present") ? 1 : 0);
                saved = true;
                dispose();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Salary must be a valid positive number.",
                        "Invalid Salary", JOptionPane.WARNING_MESSAGE);
            }
        }

        public User getUser() { return user; }
        public boolean isSaved() { return saved; }
        public String getNewPassword() { return new String(passwordField.getPassword()); }
    }
}
