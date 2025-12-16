package Login;

import JDBCUtils.DBConnection;
import JDBCUtils.Session;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUtils.Style.*;

public class LoginForm extends JFrame {
    // --- 1. KHAI BÁO BIẾN GIAO DIỆN ---
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        // Cấu hình cơ bản cho cửa sổ Đăng nhập
        setTitle("Đăng Nhập Hệ Thống");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        initUI(); // Khởi tạo giao diện
    }

    // --- 2. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI() {
        // Setup Panel chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // A. Tiêu đề
        JLabel lblTitle = createHeaderLabel("ĐĂNG NHẬP");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // B. Ô nhập Tài khoản
        txtUsername = new JTextField();
        JPanel pUser = createTextFieldWithLabel(txtUsername, "Tài khoản:");
        mainPanel.add(pUser);
        mainPanel.add(Box.createVerticalStrut(20));

        // C. Ô nhập Mật khẩu
        txtPassword = new JPasswordField();
        JPanel pPass = createPasswordFieldWithLabel(txtPassword, "Mật khẩu:");
        mainPanel.add(pPass);
        mainPanel.add(Box.createVerticalStrut(30));

        // D. Khu vực nút bấm (Đăng nhập / Thoát)
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBtn.setBackground(Color.WHITE);

        JButton btnLogin = createButton("Đăng Nhập", new Color(46, 204, 113));
        JButton btnExit = createButton("Thoát", new Color(231, 76, 60));

        pBtn.add(btnLogin);
        pBtn.add(btnExit);
        mainPanel.add(pBtn);

        // Thêm mainPanel vào Frame
        add(mainPanel);

        // E. Gán sự kiện (Events)
        btnExit.addActionListener(_ -> System.exit(0)); // Nút thoát
        btnLogin.addActionListener(_ -> checkLogin());  // Nút đăng nhập

        // Bấm Enter để Đăng nhập luôn
        getRootPane().setDefaultButton(btnLogin);
    }

    // --- 3. XỬ LÝ LOGIC ĐĂNG NHẬP ---
    private void checkLogin() {
        String user = txtUsername.getText().trim();
        char[] pass = txtPassword.getPassword(); // 1. Lấy mảng ký tự

        // 2. Kiểm tra độ dài mảng
        if (user.isEmpty() || pass.length == 0) {
            showError(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_username = ? AND sta_password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user);

            // 3. Chỉ chuyển sang String ngay lúc gửi đi (tạo String tạm thời)
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
            // 4. QUAN TRỌNG: Xóa trắng mảng ký tự trong bộ nhớ sau khi dùng xong
            java.util.Arrays.fill(pass, '0');
        }
    }
}