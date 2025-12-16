package Main;

import Login.LoginForm;
import JDBCUtils.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class MainRun {

    /**
     * Hàm main: Điểm khởi chạy của toàn bộ chương trình.
     * Sử dụng invokeLater để đảm bảo luồng giao diện (EDT) chạy an toàn.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainRun::createAndShowGUI);
    }

    /**
     * Thiết lập giao diện và khởi động màn hình Đăng nhập.
     */
    private static void createAndShowGUI() {
        try {
            // 1. Cài đặt giao diện hệ thống (Windows/Mac/Linux) cho phần mềm
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // 2. Tùy chỉnh màu sắc cho các ô nhập liệu khi bị vô hiệu hóa (Disabled/Inactive)
            // Mục đích: Mặc định Java Swing làm mờ chữ (màu xám) khi setEnabled(false).
            // Đoạn này ép nó hiển thị màu Đen để người dùng vẫn đọc được rõ ràng.
            Color black = Color.BLACK;
            UIManager.put("TextField.inactiveForeground", black);      // TextField không focus
            UIManager.put("TextField.disabledTextColor", black);       // TextField bị disable
            UIManager.put("FormattedTextField.inactiveForeground", black);
            UIManager.put("PasswordField.inactiveForeground", black);  // Mật khẩu
            UIManager.put("TextArea.inactiveForeground", black);       // Vùng văn bản
            UIManager.put("ComboBox.disabledForeground", black);       // ComboBox

            // Màu chữ của Button khi bị disable (giữ màu xám cho nút bấm là hợp lý)
            UIManager.put("Button.disabledText", Color.GRAY);

            // 3. Kiểm tra kết nối CSDL
            if (checkDatabaseConnection()) {
                // Kết nối thành công -> Mở form Đăng nhập
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                loginForm.setLocationRelativeTo(null);
            } else {
                // Nếu kết nối thất bại (Hàm checkDatabaseConnection đã hiện Dialog báo lỗi rồi)
                // Thì tại đây ta tắt hẳn chương trình để tránh chạy ngầm.
                System.exit(0);
            }

        } catch (Exception _) {
            System.exit(0);
        }
    }

    /**
     * Kiểm tra xem có kết nối được tới Database hay không.
     * @return true nếu kết nối thành công, false nếu thất bại.
     */
    private static boolean checkDatabaseConnection() {
        try (Connection con = DBConnection.getConnection()) {
            // Nếu con != null nghĩa là lấy được kết nối -> Trả về true
            return con != null;
        } catch (Exception e) {
            // Nếu lỗi, hiện thông báo cho người dùng biết
            JOptionPane.showMessageDialog(null,
                    "Lỗi kết nối CSDL: " + e.getMessage(),
                    "Lỗi Khởi Động",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}