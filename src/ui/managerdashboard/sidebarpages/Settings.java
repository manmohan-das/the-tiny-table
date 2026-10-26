package managerdashboard.sidebarpages;

import dao.TaxSettingsDAO;
import dao.StoreProfileDAO;
import dao.AppSettingsDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    private final JTextField upiIdField = new JTextField(24);
    private final JLabel qrFileLabel = new JLabel("No QR image selected");
    private final JLabel qrPreviewLabel = new JLabel("No QR image", SwingConstants.CENTER);
    private final JLabel upiStatusLabel = new JLabel(" ");
    private File selectedQrFile;

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
        cardArea.add(Box.createVerticalStrut(16));

        JPanel upiCard = createUpiSettingsCard();
        upiCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        upiCard.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, upiCard.getPreferredSize().height));
        cardArea.add(upiCard);

        JScrollPane settingsScroll = new JScrollPane(cardArea);
        settingsScroll.setBorder(null);
        settingsScroll.setOpaque(false);
        settingsScroll.getViewport().setOpaque(false);
        settingsScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(settingsScroll, BorderLayout.CENTER);
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

    private JPanel createUpiSettingsCard() {
        String savedUpiId = AppSettingsDAO.getValue("upi_id");
        upiIdField.setText(savedUpiId == null ? "" : savedUpiId);
        styleProfileField(upiIdField);

        String savedQrSetting = AppSettingsDAO.getValue("upi_qr");
        File currentQr = findQrFile(savedQrSetting);
        if (currentQr != null) {
            qrFileLabel.setText(currentQr.getName());
            updateQrPreview(currentQr);
        }
        qrFileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qrFileLabel.setForeground(MUTED);

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(LIGHT_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(20, 26, 20, 26)));

        JLabel title = new JLabel("UPI Payment Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(profileLabel("UPI ID"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(7, 0, 7, 0);
        form.add(upiIdField, gbc);

        JButton chooseQr = new JButton("Choose QR Image");
        chooseQr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chooseQr.setForeground(TEXT);
        chooseQr.setBackground(CREAM);
        chooseQr.setFocusPainted(false);
        chooseQr.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1), new EmptyBorder(9, 13, 9, 13)));
        chooseQr.addActionListener(e -> chooseQrImage());

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(7, 0, 7, 10);
        form.add(profileLabel("UPI QR Image"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel chooserRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chooserRow.setOpaque(false);
        chooserRow.add(chooseQr);
        chooserRow.add(qrFileLabel);
        form.add(chooserRow, gbc);

        JLabel note = new JLabel("The QR image is saved in the project resources folder.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(MUTED);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(note, gbc);
        body.add(form, BorderLayout.CENTER);

        JPanel preview = new JPanel(new BorderLayout(0, 6));
        preview.setOpaque(false);
        preview.setPreferredSize(new Dimension(142, 160));
        JLabel previewTitle = new JLabel("QR Preview", SwingConstants.CENTER);
        previewTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewTitle.setForeground(MUTED);
        qrPreviewLabel.setPreferredSize(new Dimension(132, 132));
        qrPreviewLabel.setBorder(new LineBorder(BORDER, 1));
        qrPreviewLabel.setForeground(MUTED);
        preview.add(previewTitle, BorderLayout.NORTH);
        preview.add(qrPreviewLabel, BorderLayout.CENTER);
        body.add(preview, BorderLayout.EAST);
        card.add(body, BorderLayout.CENTER);

        JButton save = new JButton("Save UPI Details");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setForeground(Color.WHITE);
        save.setBackground(GREEN);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(10, 18, 10, 18));
        save.addActionListener(e -> saveUpiDetails());

        upiStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);
        footer.add(save);
        footer.add(upiStatusLabel);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void chooseQrImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose UPI QR Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "QR images (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                showUpiStatus("Selected file is not a supported image.", true);
                return;
            }
            selectedQrFile = file;
            qrFileLabel.setText(file.getName());
            updateQrPreview(image);
            showUpiStatus(" ", false);
        } catch (IOException ex) {
            showUpiStatus("Could not open the selected image.", true);
        }
    }

    private void updateQrPreview(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                updateQrPreview(image);
            }
        } catch (IOException ignored) {
            qrPreviewLabel.setIcon(null);
            qrPreviewLabel.setText("Cannot preview");
        }
    }

    private void updateQrPreview(BufferedImage image) {
        Image scaled = image.getScaledInstance(126, 126, Image.SCALE_SMOOTH);
        qrPreviewLabel.setText("");
        qrPreviewLabel.setIcon(new ImageIcon(scaled));
    }

    private File findQrFile(String configuredValue) {
        String configured = configuredValue == null ? "" : configuredValue.trim();
        String[] candidates = "qr_image".equalsIgnoreCase(configured) || configured.isEmpty()
                ? new String[] { "resources/qr_image.jpeg", "resources/qr_image.png", "resources/qr_image" }
                : new String[] { configured, "resources/" + configured };
        for (String candidate : candidates) {
            File file = new File(candidate);
            if (!file.isFile()) {
                file = new File("resources", candidate);
            }
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    private void saveUpiDetails() {
        String upiId = upiIdField.getText().trim();
        if (upiId.isEmpty() || !upiId.contains("@")) {
            showUpiStatus("Enter a valid UPI ID.", true);
            upiIdField.requestFocusInWindow();
            return;
        }

        File destination = new File("resources", "qr_image.jpeg");
        try {
            Files.createDirectories(destination.toPath().getParent());
            boolean sameQrFile = selectedQrFile != null
                    && Files.exists(destination.toPath())
                    && Files.isSameFile(selectedQrFile.toPath(), destination.toPath());
            if (selectedQrFile != null && !sameQrFile) {
                BufferedImage image = ImageIO.read(selectedQrFile);
                if (image == null || !ImageIO.write(image, "jpeg", destination)) {
                    showUpiStatus("Could not save the selected QR image.", true);
                    return;
                }
            } else if (selectedQrFile == null && findQrFile("qr_image") == null) {
                showUpiStatus("Choose a QR image before saving.", true);
                return;
            }
        } catch (IOException ex) {
            showUpiStatus("Could not save the QR image in project resources.", true);
            return;
        }

        if (!AppSettingsDAO.saveValue("upi_id", upiId)
                || !AppSettingsDAO.saveValue("upi_qr", "qr_image")) {
            showUpiStatus("Could not save UPI settings. Check app_settings.", true);
            return;
        }

        selectedQrFile = null;
        qrFileLabel.setText(destination.getName());
        updateQrPreview(destination);
        showUpiStatus("UPI details saved.", false);
    }

    private void showUpiStatus(String message, boolean error) {
        upiStatusLabel.setText(message);
        upiStatusLabel.setForeground(error ? new Color(165, 67, 67) : GREEN);
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
            showStatus("Could not save. Check that the app_settings table exists.", true);
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
