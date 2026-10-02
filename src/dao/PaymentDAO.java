package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Payment;
import util.DBConnection;

public class PaymentDAO {

    // 1. GET ALL PAYMENTS
    public List<Payment> getAllPayments() {

        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT * FROM payments ORDER BY payment_id";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Payment payment = new Payment();

                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));

                payments.add(payment);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return payments;
    }


    // 2. GET PAYMENT BY ID
    public Payment getPaymentById(int paymentId) {

        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, paymentId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Payment payment = new Payment();

                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));

                return payment;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // 3. GET PAYMENT BY ORDER ID
    public Payment getPaymentByOrderId(int orderId) {

        String sql = "SELECT * FROM payments WHERE order_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Payment payment = new Payment();

                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));

                return payment;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // 4. ADD PAYMENT
    public boolean addPayment(Payment payment) {

        String sql = "INSERT INTO payments " +
                     "(order_id, amount, payment_method, payment_status, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, payment.getOrderId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus());
            ps.setTimestamp(5, payment.getPaymentDate());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 5. UPDATE PAYMENT STATUS
    public boolean updatePaymentStatus(int paymentId, String paymentStatus) {

        String sql = "UPDATE payments SET payment_status = ? " +
                     "WHERE payment_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, paymentStatus);
            ps.setInt(2, paymentId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 6. UPDATE PAYMENT
    public boolean updatePayment(Payment payment) {

        String sql = "UPDATE payments SET " +
                     "order_id = ?, " +
                     "amount = ?, " +
                     "payment_method = ?, " +
                     "payment_status = ?, " +
                     "payment_date = ? " +
                     "WHERE payment_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, payment.getOrderId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus());
            ps.setTimestamp(5, payment.getPaymentDate());
            ps.setInt(6, payment.getPaymentId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // 7. DELETE PAYMENT
    public boolean deletePayment(int paymentId) {

        String sql = "DELETE FROM payments WHERE payment_id = ?";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, paymentId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}