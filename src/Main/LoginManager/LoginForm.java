package Main.LoginManager;

import Utils.DBConnection;
import Utils.Session;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static Utils.Style.*;

/**
 * Login Form class.
 * Handles user authentication.
 */
public class LoginForm extends JFrame {
    // --- 1. UI VARIABLES ---
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    /**
     * Constructor to initialize the Login Form.
     */
    public LoginForm() {
        // Basic configuration for Login window
        setTitle("Đăng Nhập Hệ Thống");
        setSize(400, 373);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        initUI(); // Initialize UI
    }

    // --- 2. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        // Setup Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // A. Title
        JLabel lblTitle = createHeaderLabel("ĐĂNG NHẬP");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // B. Username Field
        txtUsername = new JTextField();
        JPanel pUser = createTextFieldWithLabel(txtUsername, "Tài khoản:");
        mainPanel.add(pUser);
        mainPanel.add(Box.createVerticalStrut(20));

        // C. Password Field
        txtPassword = new JPasswordField();
        JCheckBox chkShowPass = new JCheckBox();
        JPanel pPass = createPasswordFieldWithLabel(txtPassword, "Mật khẩu:", chkShowPass);
        mainPanel.add(pPass);
        mainPanel.add(Box.createVerticalStrut(15));

        // D. Button Area (Login / Exit)
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBtn.setBackground(Color.WHITE);

        JButton btnLogin = createButton("Đăng Nhập", new Color(46, 204, 113));
        JButton btnExit = createButton("Thoát", new Color(231, 76, 60));

        pBtn.add(btnLogin);
        pBtn.add(btnExit);
        mainPanel.add(pBtn);

        // Add mainPanel to Frame
        add(mainPanel);

        // E. Assign Events
        btnExit.addActionListener(e -> System.exit(0)); // Exit button
        btnLogin.addActionListener(e -> checkLogin());  // Login button

        // Press Enter to Login
        getRootPane().setDefaultButton(btnLogin);
    }

    // --- 3. LOGIN LOGIC ---

    /**
     * Validates user credentials and logs in.
     */
    private void checkLogin() {
        String user = txtUsername.getText().trim();
        char[] pass = txtPassword.getPassword(); // 1. Get character array

        // 2. Check length
        if (user.isEmpty() || pass.length == 0) {
            showError(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_username = ? AND sta_password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user);

            // 3. Convert to String only when sending (create temporary String)
            ps.setString(2, new String(pass));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Session.isLoggedIn = true;
                Session.loggedInStaffID = rs.getInt("sta_ID");
                Session.loggedInStaffName = rs.getString("sta_name");
                Session.userRole = rs.getString("sta_role");

                new DashBoard().setVisible(true);
                this.dispose();

            } else {
                showError(this, "Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception ex) {
            showError(this, "Lỗi kết nối: " + ex.getMessage());
        } finally {
            // 4. IMPORTANT: Clear character array in memory after use
            java.util.Arrays.fill(pass, '0');
        }
    }
}
