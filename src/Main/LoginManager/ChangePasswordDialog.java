package Main.LoginManager;

import JDBCUtils.DBConnection;
import JDBCUtils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUtils.Style.*;

public class ChangePasswordDialog extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN ---
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;

    public ChangePasswordDialog(JFrame parent) {
        // Cấu hình Dialog (Modal = true để chặn tương tác cửa sổ cha)
        super(parent, "Đổi Mật Khẩu", true);
        setSize(400, 400);
        setLocationRelativeTo(parent); // Căn giữa màn hình cha

        initUI(); // Khởi tạo giao diện
    }

    // --- 2. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // A. Tiêu đề
        JLabel lblTitle = createHeaderLabel("ĐỔI MẬT KHẨU");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // B. Các ô nhập mật khẩu
        txtOldPass = new JPasswordField();
        JPanel pOld = createPasswordFieldWithLabel(txtOldPass, "Mật khẩu hiện tại:");
        mainPanel.add(pOld);
        mainPanel.add(Box.createVerticalStrut(20));

        txtNewPass = new JPasswordField();
        JPanel pNew = createPasswordFieldWithLabel(txtNewPass, "Mật khẩu mới:");
        mainPanel.add(pNew);
        mainPanel.add(Box.createVerticalStrut(20));

        txtConfirmPass = new JPasswordField();
        JPanel pConfirm = createPasswordFieldWithLabel(txtConfirmPass, "Xác nhận mật khẩu mới:");
        mainPanel.add(pConfirm);
        mainPanel.add(Box.createVerticalStrut(30));

        // C. Khu vực nút bấm
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBtn.setBackground(Color.WHITE);

        JButton btnSave = createButton("Lưu", new Color(46, 204, 113)); // Màu xanh lá
        JButton btnCancel = createButton("Hủy", new Color(231, 76, 60));     // Màu đỏ

        pBtn.add(btnSave);
        pBtn.add(btnCancel);
        mainPanel.add(pBtn);

        add(mainPanel);

        // D. Gán sự kiện
        btnCancel.addActionListener(e -> dispose()); // Đóng cửa sổ
        btnSave.addActionListener(e -> doChangePassword()); // Thực hiện đổi pass

        // Bấm Enter để Lưu luôn
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 3. XỬ LÝ LOGIC ĐỔI MẬT KHẨU ---
    private void doChangePassword() {
        // 1. Lấy mật khẩu dưới dạng mảng ký tự (char[]) thay vì String
        char[] oldPass = txtOldPass.getPassword();
        char[] newPass = txtNewPass.getPassword();
        char[] confirmPass = txtConfirmPass.getPassword();

        try {
            // 2. Kiểm tra dữ liệu rỗng bằng độ dài mảng
            if (oldPass.length == 0 || newPass.length == 0 || confirmPass.length == 0) {
                showError(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // 3. So sánh nội dung 2 mảng ký tự
            if (!java.util.Arrays.equals(newPass, confirmPass)) {
                showError(this, "Mật khẩu xác nhận không trùng khớp!");
                return;
            }

            if (java.util.Arrays.equals(newPass, oldPass)) {
                showError(this, "Mật khẩu mới không được trùng mật khẩu cũ!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Kiểm tra mật khẩu cũ
                String checkSql = "SELECT * FROM Staffs WHERE sta_ID = ? AND sta_password = ?";
                PreparedStatement psCheck = con.prepareStatement(checkSql);
                psCheck.setInt(1, Session.loggedInStaffID);

                // Tạo String tạm thời chỉ để gửi xuống DB, sau đó nó sẽ được GC dọn dẹp
                psCheck.setString(2, new String(oldPass));

                ResultSet rs = psCheck.executeQuery();

                if (!rs.next()) {
                    showError(this, "Mật khẩu hiện tại không đúng!");
                    return;
                }

                // Cập nhật mật khẩu mới
                String updateSql = "UPDATE Staffs SET sta_password = ? WHERE sta_ID = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateSql);

                // Tạo String tạm thời để update
                psUpdate.setString(1, new String(newPass));
                psUpdate.setInt(2, Session.loggedInStaffID);

                if (psUpdate.executeUpdate() > 0) {
                    showSuccess(this, "Đổi mật khẩu thành công!");
                    dispose();
                }

            } catch (Exception ex) {
                showError(this, "Lỗi kết nối: " + ex.getMessage());
            }
        } finally {
            // 4. QUAN TRỌNG: Xóa trắng bộ nhớ (Ghi đè tất cả thành số 0)
            // Dù code chạy thành công hay thất bại, phần này luôn được thực thi
            java.util.Arrays.fill(oldPass, '0');
            java.util.Arrays.fill(newPass, '0');
            java.util.Arrays.fill(confirmPass, '0');
        }
    }
}