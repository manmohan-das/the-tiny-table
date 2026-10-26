package dao;

import java.math.BigDecimal;
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

        if (AppSettingsDAO.saveValue(SETTING_KEY,
                BigDecimal.valueOf(percent).stripTrailingZeros().toPlainString())) {
            cachedRatePercent = percent;
            lastReadAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private static double readRatePercent() {
        String storedValue = AppSettingsDAO.getValue(SETTING_KEY);
        if (storedValue != null) {
            try {
                double value = new BigDecimal(storedValue.trim()).doubleValue();
                if (!Double.isNaN(value) && !Double.isInfinite(value)
                        && value >= 0.0 && value <= 100.0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                System.err.println("Invalid GST rate in app_settings; using 5% default.");
            }
        }
        return DEFAULT_RATE_PERCENT;
    }
}
