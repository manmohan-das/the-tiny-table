package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;

/** Database access for the GST setting used by new orders. */
public final class TaxSettingsDAO {
    private static final String SETTING_KEY = "gst_rate_percent";
    private static final double DEFAULT_RATE_PERCENT = 5.0;
    private static final long CACHE_MILLIS = 15_000L;

    private static volatile double cachedRatePercent = DEFAULT_RATE_PERCENT;
    private static volatile long lastReadAt = 0L;

    private TaxSettingsDAO() { }

    /** Returns the configured GST rate as a percentage, e.g. 5.0. */
    public static double getRatePercent() {
        long now = System.currentTimeMillis();
        if (now - lastReadAt > CACHE_MILLIS) {
            synchronized (TaxSettingsDAO.class) {
                now = System.currentTimeMillis();
                if (now - lastReadAt > CACHE_MILLIS) {
                    cachedRatePercent = readRatePercent();
                    lastReadAt = now;
                }
            }
        }
        return cachedRatePercent;
    }

    /** Returns the configured GST rate as a multiplier, e.g. 0.05. */
    public static double getRate() {
        return getRatePercent() / 100.0;
    }

    public static String currentGstLabel() {
        return "GST (" + formatPercent(getRatePercent()) + "%)";
    }

    /** Labels a saved bill using its stored tax amount, not today's setting. */
    public static String gstLabelForAmounts(double subtotal, double discount, double tax) {
        double taxable = subtotal - discount;
        if (taxable <= 0.005) return "GST";
        return "GST (" + formatPercent(tax / taxable * 100.0) + "%)";
    }

    public static String formatPercent(double percent) {
        return BigDecimal.valueOf(percent).stripTrailingZeros().toPlainString();
    }

    /** Saves the percentage and refreshes this process's cached value. */
    public static synchronized boolean saveRatePercent(double percent) {
        if (Double.isNaN(percent) || Double.isInfinite(percent) || percent < 0.0 || percent > 100.0)
            return false;

        String updateSql = "UPDATE gst_settings SET setting_value=? WHERE setting_key=?";
        String insertSql = "INSERT INTO gst_settings(setting_key, setting_value) VALUES(?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            int updated;
            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setBigDecimal(1, BigDecimal.valueOf(percent));
                statement.setString(2, SETTING_KEY);
                updated = statement.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setString(1, SETTING_KEY);
                    statement.setBigDecimal(2, BigDecimal.valueOf(percent));
                    statement.executeUpdate();
                }
            }

            cachedRatePercent = percent;
            lastReadAt = System.currentTimeMillis();
            return true;
        } catch (SQLException ex) {
            System.err.println("Could not save GST setting: " + ex.getMessage());
            return false;
        }
    }

    private static double readRatePercent() {
        String sql = "SELECT setting_value FROM gst_settings WHERE setting_key=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SETTING_KEY);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    double value = result.getBigDecimal("setting_value").doubleValue();
                    if (!Double.isNaN(value) && !Double.isInfinite(value)
                            && value >= 0.0 && value <= 100.0) {
                        return value;
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Could not load GST setting; using 5% default: " + ex.getMessage());
        }
        return DEFAULT_RATE_PERCENT;
    }
}
