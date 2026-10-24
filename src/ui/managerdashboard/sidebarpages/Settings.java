package managerdashboard.sidebarpages;

import dao.TaxSettingsDAO;
import dao.StoreProfileDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Manager page for changing the GST rate applied to new orders. */
public class Settings extends JPanel {
    private static final Color CREAM = new Color(250, 245, 235);
    private static final Color LIGHT_CREAM = new Color(255, 250, 242);
    private static final Color BORDER = new Color(225, 210, 185);
    private static final Color TEXT = new Color(45, 40, 35);
    private static final Color MUTED = new Color(100, 90, 80);
    private static final Color GREEN = new Color(73, 112, 83);

    private final JTextField rateField = new JTextField(10);
    private final JLabel statusLabel = new JLabel(" ");

    private final JTextField storeNameField = new JTextField(24);
    private final JTextField addressField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField gstinField = new JTextField(24);
    private final JLabel profileStatusLabel = new JLabel(" ");

    public Settings() {
        setLayout(new BorderLayout(0, 18));
        setBackground(CREAM);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(76, 58, 42));

        JLabel subtitle = new JLabel("Manage store details and tax settings.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        add(heading, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(LIGHT_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(24, 26, 24, 26)));

        JLabel cardTitle = new JLabel("Tax & Billing");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cardTitle.setForeground(TEXT);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel rateLabel = new JLabel("Enter new GST rate for orders");
        rateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rateLabel.setForeground(TEXT);

        rateField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        rateField.setText(TaxSettingsDAO.formatPercent(TaxSettingsDAO.getRatePercent()));
        rateField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
        rateField.setPreferredSize(new Dimension(120, 38));

        JLabel percent = new JLabel("%");
        percent.setFont(new Font("Segoe UI", Font.BOLD, 15));
        percent.setForeground(TEXT);

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(rateLabel, gbc);
        gbc.gridx = 1;
        form.add(rateField, gbc);
        gbc.gridx = 2;
        form.add(percent, gbc);

        JLabel note = new JLabel("This rate applies to new orders. Saved bills keep their recorded tax amounts.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        note.setForeground(MUTED);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        form.add(note, gbc);
        card.add(form, BorderLayout.CENTER);

        JButton save = new JButton("Save GST Rate");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setForeground(Color.WHITE);
        save.setBackground(GREEN);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(10, 18, 10, 18));
        save.addActionListener(e -> saveRate());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);
        footer.add(save);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footer.add(statusLabel);
        card.add(footer, BorderLayout.SOUTH);

        JPanel cardArea = new JPanel();
        cardArea.setOpaque(false);
        cardArea.setLayout(new BoxLayout(cardArea, BoxLayout.Y_AXIS));

        JPanel profileCard = createStoreProfileCard();
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, profileCard.getPreferredSize().height));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        cardArea.add(profileCard);
        cardArea.add(Box.createVerticalStrut(16));
        cardArea.add(card);

        add(cardArea, BorderLayout.CENTER);
    }

    private JPanel createStoreProfileCard() {
        StoreProfileDAO.Profile profile = StoreProfileDAO.loadProfile();

        storeNameField.setText(profile.getStoreName());
        addressField.setText(profile.getAddress());
        phoneField.setText(profile.getPhone());
        gstinField.setText(profile.getGstin());

        styleProfileField(storeNameField);
        styleProfileField(addressField);
        styleProfileField(phoneField);
        styleProfileField(gstinField);

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(LIGHT_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(20, 26, 20, 26)));

        JLabel title = new JLabel("Store Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 10));
        form.setOpaque(false);

        form.add(profileLabel("Store Name"));
        form.add(storeNameField);
        form.add(profileLabel("Address"));
        form.add(addressField);
        form.add(profileLabel("Phone"));
        form.add(phoneField);
        form.add(profileLabel("GSTIN"));
        form.add(gstinField);

        card.add(form, BorderLayout.CENTER);

        JButton save = new JButton("Save Store Details");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setForeground(Color.WHITE);
        save.setBackground(GREEN);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(10, 18, 10, 18));
        save.addActionListener(e -> saveStoreProfile());

        profileStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);
        footer.add(save);
        footer.add(profileStatusLabel);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JLabel profileLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);
        return label;
    }

    private void styleProfileField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(240, 38));
    }

    private void saveStoreProfile() {
        boolean saved = StoreProfileDAO.saveProfile(
                storeNameField.getText(),
                addressField.getText(),
                phoneField.getText(),
                gstinField.getText());

        profileStatusLabel.setText(saved
                ? "Store details saved."
                : "Could not save. Check database and field lengths.");
        profileStatusLabel.setForeground(saved
                ? GREEN
                : new Color(165, 67, 67));
    }

    private void saveRate() {
        final double percent;
        try {
            percent = Double.parseDouble(rateField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus("Enter a valid percentage.", true);
            rateField.requestFocusInWindow();
            return;
        }

        if (Double.isNaN(percent) || Double.isInfinite(percent) || percent < 0.0 || percent > 100.0) {
            showStatus("Rate must be between 0 and 100.", true);
            rateField.requestFocusInWindow();
            return;
        }

        if (!TaxSettingsDAO.saveRatePercent(percent)) {
            showStatus("Could not save. Run the gst_settings SQL setup first.", true);
            return;
        }

        rateField.setText(TaxSettingsDAO.formatPercent(percent));
        showStatus("GST rate saved. New orders will use "
                + TaxSettingsDAO.formatPercent(percent) + "%.", false);
    }

    private void showStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error ? new Color(165, 67, 67) : GREEN);
    }
}
