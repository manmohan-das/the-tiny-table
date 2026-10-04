

import  util.DBConnection;
import  employeePanel.MenuAndOrderTable;
import  chefPanel.ChefPage;
import  managerdashboard.ManagerDashboard;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.GroupLayout.Group;
import javax.swing.border.EmptyBorder;
import java.sql.*;
public class Login extends JFrame {

    final int IW = 1536, IH = 1024;

    Login() {
        setTitle("The Tiny Table");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        add(new MainPage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new Login().setVisible(true));
    }

    class MainPage extends JPanel {
        Image bg;
        JLabel roleLogo;
        LoginBox login;

        MainPage() {
            bg = new ImageIcon("resources/logo_banner/3d.png").getImage();
            setLayout(null);

            roleLogo = new JLabel();
            roleLogo.setHorizontalAlignment(SwingConstants.CENTER);
            add(roleLogo);

            login = new LoginBox();
            add(login);

            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    arrange();
                }
            });
        }

        void arrange() {
            double s = Math.min(
                (double)getWidth() / IW,
                (double)getHeight() / IH
            );

            int area = (int)(220 * s);
            int photo = (int)(190 * s);

            roleLogo.setBounds(
                (int)(1500 * s),
                (int)(130 * s),
                area,
                area
            );

            setRoleLogo("Employee", photo);

            int x = (int)(1360 * s);
            int y = (int)(330 * s);
            int w = (int)(520 * s);
            int h = (int)(500 * s);

            login.setBounds(x, y, w, h);
            login.resizeUI(s);
        }

        void setRoleLogo(String role, int size) {
            if (size <= 0) {
                return;
            }

            String file;

            if (role.equals("Chef"))
                file = "assets/chef.png";
            else if (role.equals("Manager"))
                file = "assets/manager.png";
            else
                file = "assets/employee.png";

            Image image = new ImageIcon(file).getImage();

            BufferedImage result =
                new BufferedImage(
                    size, size,
                    BufferedImage.TYPE_INT_ARGB
                );

            Graphics2D g = result.createGraphics();

            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setClip(
                new java.awt.geom.Ellipse2D.Double(
                    0, 0, size, size
                )
            );

            g.drawImage(
                image, 0, 0, size, size, null
            );

            g.dispose();

            roleLogo.setIcon(new ImageIcon(result));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            // Keep the background image's real aspect ratio.
            int bgW = bg.getWidth(null);
            int bgH = bg.getHeight(null);

            if (bgW <= 0 || bgH <= 0)
                return;

            double s = Math.max(
                (double)w / bgW,
                (double)h / bgH
            );

            int nw = (int)(bgW * s);
            int nh = (int)(bgH * s);

            int x = (w - nw) / 2;
            int y = (h - nh) / 2;

            Graphics2D g2 = (Graphics2D)g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            );

            g2.drawImage(
                bg, x, y, nw, nh, null
            );

            g2.dispose();
        }
    }

    class LoginBox extends JPanel {

        JLabel title, userText, passText, forgot;

        RoundedField username;
        RoundedPassword password;

        RoundedButton loginButton;
        JButton eyeButton;

        JRadioButton option1, option2;
        ButtonGroup group;

        String role = "Employee";
        String authenticatedName = "";
        String authenticatedUsername = "";

        Image emp, chefImg, managerImg;

        LoginBox() {

            setLayout(null);
            setOpaque(false);

            emp =
                new ImageIcon("assets/employee.png").getImage();

            chefImg =
                new ImageIcon("assets/chef.png").getImage();

            managerImg =
                new ImageIcon("assets/manager.png").getImage();

            title = label("Employee Login", 28);

            userText =
                label("Enter your username", 15);

            passText =
                label("Enter password", 15);

            username =
                new RoundedField("Username");

            password =
                new RoundedPassword("Password");

            eyeButton =
                new JButton("◉");

            loginButton =
                new RoundedButton("Login");

            forgot =
                new JLabel(
                    "<html><u>Forgot Password?</u></html>"
                );

            option1 = new JRadioButton();
            option2 = new JRadioButton();

            add(title);
            add(userText);
            add(username);
            add(passText);
            add(password);
            add(eyeButton);
            add(loginButton);
            add(forgot);
            add(option1);
            add(option2);

            group = new ButtonGroup();
            

            group.add(option1);
            group.add(option2);

            option1.addActionListener(e ->
                changeRole(
                    (String)option1.getClientProperty("role")
                )
            );

            option2.addActionListener(e ->
                changeRole(
                    (String)option2.getClientProperty("role")
                )
            );

            eyeButton.addActionListener(e ->
                showPassword()
            );

            loginButton.addActionListener(e ->
                checkLogin()
            );

            forgot.setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );

            forgot.addMouseListener(new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {

                    JOptionPane.showMessageDialog(
                        Login.this,
                        "Please contact your manager to reset your password.",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });

            style();
            setOptions();
        }

        JLabel label(String text, int size) {

            JLabel l = new JLabel(text);

            l.setFont(
                new Font(
                    "Serif",
                    Font.BOLD,
                    size
                )
            );

            l.setForeground(Color.BLACK);

            return l;
        }

        void setOptions() {

            if (role.equals("Employee")) {

                option1.setText("Chef Login");
                option1.putClientProperty(
                    "role", "Chef"
                );

                option2.setText("Manager Login");
                option2.putClientProperty(
                    "role", "Manager"
                );

            } else if (role.equals("Chef")) {

                option1.setText("Employee Login");
                option1.putClientProperty(
                    "role", "Employee"
                );

                option2.setText("Manager Login");
                option2.putClientProperty(
                    "role", "Manager"
                );

            } else {

                option1.setText("Employee Login");
                option1.putClientProperty(
                    "role", "Employee"
                );

                option2.setText("Chef Login");
                option2.putClientProperty(
                    "role", "Chef"
                );
            }

            option1.setSelected(false);
            option2.setSelected(false);

            option1.setOpaque(false);
            option2.setOpaque(false);

            option1.setForeground(
                new Color(25,55,40)
            );

            option2.setForeground(
                new Color(25,55,40)
            );

            option1.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    14
                )
            );

            option2.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    14
                )
            );
        }

        void changeRole(String r) {
            group.clearSelection();

            role = r;

            title.setText(r + " Login");

            setOptions();
            option1.setSelected(false);
            option2.setSelected(false);

            MainPage p =
                (MainPage)getParent();

            double s = Math.min(
                (double)p.getWidth() / IW,
                (double)p.getHeight() / IH
            );

            int photo = (int)(190 * s);

            p.setRoleLogo(r, photo);

            username.resetText();
            password.resetText();
        }

        void resizeUI(double s) {

            int W = getWidth();

            title.setBounds(
                (int)(35*s),
                (int)(20*s),
                W-(int)(70*s),
                (int)(42*s)
            );

            userText.setBounds(
                (int)(35*s),
                (int)(82*s),
                W-(int)(70*s),
                (int)(25*s)
            );

            username.setBounds(
                (int)(35*s),
                (int)(110*s),
                W-(int)(70*s),
                (int)(48*s)
            );

            passText.setBounds(
                (int)(35*s),
                (int)(172*s),
                W-(int)(70*s),
                (int)(25*s)
            );

            password.setBounds(
                (int)(35*s),
                (int)(200*s),
                W-(int)(70*s),
                (int)(48*s)
            );

            eyeButton.setBounds(
                W-(int)(72*s),
                (int)(206*s),
                (int)(38*s),
                (int)(36*s)
            );

            loginButton.setBounds(
                (int)(35*s),
                (int)(265*s),
                W-(int)(70*s),
                (int)(48*s)
            );

            forgot.setBounds(
                (int)(35*s),
                (int)(322*s),
                (int)(210*s),
                (int)(25*s)
            );

            option1.setBounds(
                (int)(35*s),
                (int)(370*s),
                W/2-(int)(35*s),
                (int)(35*s)
            );

            option2.setBounds(
                W/2,
                (int)(370*s),
                W/2-(int)(25*s),
                (int)(35*s)
            );
        }

        void style() {

            forgot.setForeground(
                new Color(0,55,150)
            );

            forgot.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    13
                )
            );

            eyeButton.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    17
                )
            );

            eyeButton.setForeground(
                new Color(50,60,55)
            );

            eyeButton.setBorderPainted(false);
            eyeButton.setContentAreaFilled(false);
            eyeButton.setFocusPainted(true);

            eyeButton.setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );

            loginButton.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    17
                )
            );

            loginButton.setForeground(Color.WHITE);

            loginButton.setBackground(
                new Color(36,111,69)
            );

            loginButton.setFocusPainted(true);

            loginButton.setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );

            option1.setOpaque(false);
            option2.setOpaque(false);

            option1.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    14
                )
            );

            option2.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    14
                )
            );

            option1.setForeground(
                new Color(25,55,40)
            );

            option2.setForeground(
                new Color(25,55,40)
            );
        }

        void showPassword() {

            if (password.isPlaceholder())
                return;

            if (password.getEchoChar() == 0) {

                password.setEchoChar('•');
                eyeButton.setText("◉");

            } else {

                password.setEchoChar((char)0);
                eyeButton.setText("○");
            }

            password.requestFocusInWindow();
        }

        private Connection getDatabaseConnection() throws SQLException {
            return DBConnection.getConnection();
        }

        void checkLogin() {

            String u = username.getRealText();
            String p = password.getRealText();

            if (u.isEmpty()) {

                error("Please enter username.");
                return;
            }

            if (p.isEmpty()) {

                error("Please enter password.");
                return;
            }

            /*
             * Authenticate against the actual MySQL users table.
             *
             * Login-page role -> database role:
             * Employee -> employee
             * Chef     -> kitchen_staff
             * Manager  -> manager
             */
            String dbRole;

            if (role.equals("Employee"))
                dbRole = "employee";
            else if (role.equals("Chef"))
                dbRole = "kitchen_staff";
            else
                dbRole = "manager";

            String sql =
                "SELECT name, username, role " +
                "FROM users " +
                "WHERE username = ? " +
                "AND password = ? " +
                "AND role = ? " +
                "AND status = 1";

            try (
                Connection connection =
                    getDatabaseConnection();

                PreparedStatement statement =
                    connection.prepareStatement(sql)
            ) {

                statement.setString(1, u);
                statement.setString(2, p);
                statement.setString(3, dbRole);

                try (ResultSet result =
                    statement.executeQuery()) {

                    if (!result.next()) {

                        error(
                            "Incorrect username or password."
                        );

                        return;
                    }

                    authenticatedName =
                        result.getString("name");

                    authenticatedUsername =
                        result.getString("username");
                }

            } catch (SQLException ex) {

                ex.printStackTrace();

                error(
                    "Unable to connect to the database.\n\n"
                    + ex.getMessage()
                );

                return;
            }

            if (role.equals("Manager"))
                managerOTP();
            else
                success();
        }

        void error(String text) {

            JOptionPane.showMessageDialog(
                Login.this,
                text,
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        // UPDATED SUCCESS POPUP ONLY
        void success() {

            JDialog d =
                new JDialog(
                    Login.this,
                    "Login Successful",
                    true
                );

            JPanel p = new JPanel();

            p.setBackground(
                new Color(242,248,243)
            );

            p.setBorder(
                new EmptyBorder(
                    30,45,30,45
                )
            );

            p.setLayout(
                new BorderLayout(
                    10,15
                )
            );

            JLabel check =
                new JLabel(
                    "✓",
                    SwingConstants.CENTER
                );

            check.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    55
                )
            );

            check.setForeground(
                new Color(36,111,69)
            );

            JLabel text =
                new JLabel(
                    "<html><center>"
                    + "<b style='font-size:20px'>"
                    + "Login Successful"
                    + "</b><br>"
                    + "<font size='4'>"
                    + "Welcome, " + authenticatedName + "!"
                    + "</font>"
                    + "</center></html>",
                    SwingConstants.CENTER
                );

            JButton ok =
                new JButton("Continue");

            ok.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    16
                )
            );

            ok.setForeground(Color.WHITE);

            ok.setBackground(
                new Color(36,111,69)
            );

            ok.setFocusPainted(false);

            ok.addActionListener(e -> {
                d.dispose();
                openRolePage();
            });

            p.add(
                check,
                BorderLayout.NORTH
            );

            p.add(
                text,
                BorderLayout.CENTER
            );

            p.add(
                ok,
                BorderLayout.SOUTH
            );

            d.add(p);

            d.setSize(400,400);

            d.setLocationRelativeTo(
                Login.this
            );

            d.setVisible(true);
        }

        // OPEN THE CORRECT PANEL AFTER SUCCESSFUL LOGIN
        void openRolePage() {
            try {
                if (role.equals("Employee")) {
                    MenuAndOrderTable page = new MenuAndOrderTable();
                    page.setVisible(true);
                } else if (role.equals("Chef")) {
                    ChefPage page = new ChefPage();
                    page.setVisible(true);
                } else {
                    ManagerDashboard page = new ManagerDashboard();
                    page.setVisible(true);
                }

                Login.this.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                    Login.this,
                    "Unable to open " + role + " panel.\n\n"
                    + ex.getClass().getSimpleName() + ": "
                    + ex.getMessage(),
                    "Page Opening Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // UPDATED MANAGER OTP FLOW
        void managerOTP() {

            RoundedField phone =
                new RoundedField(
                    "Enter registered phone number"
                );

            RoundedField otp =
                new RoundedField(
                    "Enter 6-digit OTP"
                );

            JLabel phoneLabel =
                new JLabel(
                    "REGISTERED MOBILE NUMBER"
                );

            JLabel otpLabel =
                new JLabel(
                    "ENTER OTP"
                );

            phoneLabel.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    18
                )
            );

            otpLabel.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    18
                )
            );

            phoneLabel.setForeground(
                new Color(38,91,61)
            );

            otpLabel.setForeground(
                new Color(38,91,61)
            );

            RoundedButton send =
                new RoundedButton("Send OTP");

            RoundedButton verify =
                new RoundedButton("Verify OTP");

            RoundedButton resend =
                new RoundedButton("Resend OTP");

            otp.setVisible(false);

            verify.setVisible(false);

            resend.setVisible(false);
            resend.setEnabled(false);

            send.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    18
                )
            );

            verify.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    18
                )
            );

            send.setBackground(
                new Color(36,111,69)
            );

            verify.setBackground(
                new Color(36,111,69)
            );

            resend.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    16
                )
            );

            resend.setForeground(
                new Color(36,111,69)
            );

            resend.setBackground(
                new Color(225,238,228)
            );

            JPanel panel =
                new JPanel();

            panel.setLayout(
                new GridLayout(
                    0,1,12,12
                )
            );

            panel.setBorder(
                new EmptyBorder(
                    30,40,30,40
                )
            );

            panel.setBackground(
                new Color(242,248,243)
            );

            panel.add(phoneLabel);
            panel.add(phone);
            panel.add(otpLabel);
            panel.add(otp);
            panel.add(send);
            panel.add(verify);
            panel.add(resend);

            JDialog dialog =
                new JDialog(
                    Login.this,
                    "Manager • Secure OTP Verification",
                    true
                );

            dialog.add(panel);

            dialog.setSize(
                400,
                500
            );

            dialog.setLocationRelativeTo(
                Login.this
            );

            send.addActionListener(e -> {

                String number =
                    phone.getRealText();

                if (!number.matches("\\d+")) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Phone number must contain digits only.",
                        "Invalid Number",
                        JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                if (!number.equals("9508082761")) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "This phone number is not registered.",
                        "Invalid Number",
                        JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                /*
                 * FUTURE OTP PROCESS:
                 *
                 * 1. Generate random 6 digit OTP.
                 * 2. Send OTP through SMS API.
                 * 3. Store OTP temporarily.
                 * 4. Set expiry time.
                 * 5. Verify OTP.
                 *
                 * CURRENT DEMO OTP = 123456
                 */

                showOtpSentDialog(dialog);

                otp.setVisible(true);
                verify.setVisible(true);
                resend.setVisible(true);

                send.setVisible(false);

                dialog.setSize(
                    620,
                    520
                );

                dialog.setLocationRelativeTo(
                    Login.this
                );

                startResendTimer(resend);
            });

            verify.addActionListener(e -> {

                if (otp.getRealText()
                    .equals("123456")) {

                    dialog.dispose();
                    success();

                } else {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "The OTP you entered is incorrect.",
                        "Verification Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            resend.addActionListener(e -> {

                /*
                 * FUTURE:
                 * Generate new OTP.
                 * Send through SMS API.
                 * Store new OTP.
                 * Start expiry timer.
                 */

                showOtpSentDialog(dialog);

                startResendTimer(resend);
            });

            dialog.setVisible(true);
        }

        // NEW OTP SUCCESS POPUP
        void showOtpSentDialog(JDialog parent) {

            JDialog d =
                new JDialog(
                    parent,
                    "OTP Sent",
                    true
                );

            JPanel p =
                new JPanel();

            p.setBorder(
                new EmptyBorder(
                    20,20,10,20
                )
            );

            p.setBackground(
                new Color(242,248,243)
            );

            p.setLayout(
                new BorderLayout(
                    10,15
                )
            );

            JLabel icon =
                new JLabel(
                    "✓",
                    SwingConstants.CENTER
                );

            icon.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    45
                )
            );

            icon.setForeground(
                new Color(36,111,69)
            );

            JLabel text =
                new JLabel(
                    "<html><center>"
                    + "<b style='font-size:18px'>"
                    + "OTP Sent Successfully"
                    + "</b><br>"
                    + "<font size='4'>"
                    + "A verification OTP has been sent "
                    + "to your registered number."
                    + "</font>"
                    + "</center></html>",
                    SwingConstants.CENTER
                );

            JButton ok =
                new JButton("OK");

            ok.setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    15
                )
            );

            ok.setForeground(Color.WHITE);

            ok.setBackground(
                new Color(36,111,69)
            );

            ok.setFocusPainted(false);

            ok.addActionListener(
                e -> d.dispose()
            );

            p.add(
                icon,
                BorderLayout.NORTH
            );

            p.add(
                text,
                BorderLayout.CENTER
            );

            p.add(
                ok,
                BorderLayout.SOUTH
            );

            d.add(p);

            d.setSize(
                420,
                250
            );

            d.setLocationRelativeTo(parent);

            d.setVisible(true);
        }

        // NEW 60 SECOND RESEND TIMER
        void startResendTimer(
            RoundedButton resend
        ) {

            resend.setEnabled(false);

            final int[] seconds = {60};

            Timer timer =
                new Timer(1000, null);

            timer.addActionListener(e -> {

                seconds[0]--;

                resend.setText(
                    "Resend OTP ("
                    + seconds[0]
                    + "s)"
                );

                if (seconds[0] <= 0) {

                    timer.stop();

                    resend.setEnabled(true);

                    resend.setText(
                        "Resend OTP"
                    );
                }
            });

            resend.setText(
                "Resend OTP (60s)"
            );

            timer.start();
        }

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D x =
                (Graphics2D)g.create();

            x.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            x.setColor(
                new Color(
                    246,242,225,238
                )
            );

            x.fillRoundRect(
                0,0,
                getWidth(),
                getHeight(),
                48,48
            );

            x.setColor(
                new Color(38,91,61)
            );

            x.setStroke(
                new BasicStroke(3f)
            );

            x.drawRoundRect(
                2,2,
                getWidth()-4,
                getHeight()-4,
                48,48
            );

            x.dispose();
        }
    }

    class RoundedField extends JTextField {

        String hint;

        RoundedField(String hint) {

            this.hint = hint;

            setText(hint);

            setForeground(
                new Color(125,125,125)
            );

            setFont(
                new Font(
                    "SansSerif",
                    Font.PLAIN,
                    15
                )
            );

            setOpaque(false);

            setBorder(
                new EmptyBorder(
                    5,15,5,15
                )
            );

            addFocusListener(
                new FocusAdapter() {

                    public void focusGained(
                        FocusEvent e
                    ) {

                        if (getText()
                            .equals(hint)) {

                            setText("");
                            setForeground(Color.BLACK);
                        }
                    }

                    public void focusLost(
                        FocusEvent e
                    ) {

                        if (getText()
                            .trim()
                            .isEmpty()) {

                            resetText();
                        }
                    }
                }
            );
        }

        String getRealText() {

            return getText()
                .equals(hint)
                ? ""
                : getText().trim();
        }

        void resetText() {

            setText(hint);

            setForeground(
                new Color(125,125,125)
            );
        }

        protected void paintComponent(Graphics g) {

            Graphics2D x =
                (Graphics2D)g.create();

            x.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            x.setColor(
                new Color(
                    255,255,255,220
                )
            );

            x.fillRoundRect(
                0,0,
                getWidth(),
                getHeight(),
                18,18
            );

            super.paintComponent(g);

            x.setColor(
                new Color(160,160,150)
            );

            x.drawRoundRect(
                0,0,
                getWidth()-1,
                getHeight()-1,
                18,18
            );

            x.dispose();
        }
    }

    class RoundedPassword
        extends JPasswordField {

        String hint;

        RoundedPassword(String hint) {

            this.hint = hint;

            setText(hint);

            setForeground(
                new Color(125,125,125)
            );

            setEchoChar((char)0);

            setFont(
                new Font(
                    "SansSerif",
                    Font.PLAIN,
                    15
                )
            );

            setOpaque(false);

            setBorder(
                new EmptyBorder(
                    5,15,5,45
                )
            );

            addFocusListener(
                new FocusAdapter() {

                    public void focusGained(
                        FocusEvent e
                    ) {

                        if (new String(
                            getPassword()
                        ).equals(hint)) {

                            setText("");
                            setForeground(Color.BLACK);
                            setEchoChar('•');
                        }
                    }

                    public void focusLost(
                        FocusEvent e
                    ) {

                        if (new String(
                            getPassword()
                        ).trim().isEmpty()) {

                            resetText();
                        }
                    }
                }
            );
        }

        String getRealText() {

            String text =
                new String(getPassword());

            return text.equals(hint)
                ? ""
                : text;
        }

        boolean isPlaceholder() {

            return new String(
                getPassword()
            ).equals(hint);
        }

        void resetText() {

            setText(hint);

            setForeground(
                new Color(125,125,125)
            );

            setEchoChar((char)0);
        }

        protected void paintComponent(Graphics g) {

            Graphics2D x =
                (Graphics2D)g.create();

            x.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            x.setColor(
                new Color(
                    255,255,255,220
                )
            );

            x.fillRoundRect(
                0,0,
                getWidth(),
                getHeight(),
                18,18
            );

            super.paintComponent(g);

            x.setColor(
                new Color(160,160,150)
            );

            x.drawRoundRect(
                0,0,
                getWidth()-1,
                getHeight()-1,
                18,18
            );

            x.dispose();
        }
    }

    class RoundedButton extends JButton {

        RoundedButton(String text) {

            super(text);

            setFont(
                new Font(
                    "SansSerif",
                    Font.BOLD,
                    15
                )
            );

            setForeground(Color.WHITE);

            setBackground(
                new Color(36,111,69)
            );

            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(true);

            setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );
        }

        protected void paintComponent(Graphics g) {

            Graphics2D x =
                (Graphics2D)g.create();

            x.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (getModel().isPressed())

                x.setColor(
                    new Color(25,80,50)
                );

            else if (getModel().isRollover())

                x.setColor(
                    new Color(48,130,82)
                );

            else

                x.setColor(
                    new Color(36,111,69)
                );

            x.fillRoundRect(
                0,0,
                getWidth(),
                getHeight(),
                18,18
            );

            x.dispose();

            super.paintComponent(g);
        }
    }
}