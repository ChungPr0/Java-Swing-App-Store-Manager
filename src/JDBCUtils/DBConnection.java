package JDBCUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lớp tiện ích quản lý kết nối cơ sở dữ liệu (Database Connection).
 * <br>
 * <b>Cơ chế hoạt động thông minh:</b>
 * <ol>
 * <li><b>Ưu tiên 1:</b> Tìm file <code>config.properties</code> nằm cạnh file chạy (.jar/.exe). Giúp dễ dàng sửa đổi cấu hình sau khi đóng gói phần mềm.</li>
 * <li><b>Ưu tiên 2:</b> Nếu không thấy, tìm file trong thư mục resources (bên trong file .jar). Đây là cấu hình mặc định lúc lập trình.</li>
 * <li><b>Fallback:</b> Nếu mất cả 2 file, sử dụng thông tin cứng (Hardcoded) để tránh crash chương trình.</li>
 * </ol>
 */
public class DBConnection {

    // Các biến lưu thông tin cấu hình nạp từ file
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    /*
     * Khối tĩnh (Static Block) - Chạy duy nhất 1 lần khi lớp này được gọi.
     * Nhiệm vụ: Xác định vị trí file config và nạp dữ liệu vào biến.
     */
    static {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            // --- BƯỚC 1: Ưu tiên tìm file cấu hình bên ngoài (External) ---
            File externalFile = new File("config.properties");

            if (externalFile.exists()) {
                // Nếu tìm thấy file bên ngoài -> Đọc nó
                input = new FileInputStream(externalFile);
                // System.out.println("Đang dùng cấu hình từ file ngoài: " + externalFile.getAbsolutePath());
            } else {
                // --- BƯỚC 2: Nếu không thấy, tìm trong nội bộ Resources (Internal) ---
                input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties");
            }

            // --- BƯỚC 3: Xử lý dữ liệu đọc được ---
            if (input == null) {
                // Trường hợp xấu nhất: Không tìm thấy file nào cả
                System.err.println("Lỗi nghiêm trọng: Không tìm thấy file 'config.properties' ở đâu cả!");

                // Sử dụng giá trị mặc định (Hardcoded) để chương trình không bị chết
                dbUrl = "jdbc:mysql://localhost:3306/quanlybanhang?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
                dbUser = "root";
                dbPassword = "";
            } else {
                // Nạp dữ liệu từ luồng input vào đối tượng Properties
                prop.load(input);

                // Lấy giá trị theo key
                dbUrl = prop.getProperty("db.url");
                dbUser = prop.getProperty("db.username");
                dbPassword = prop.getProperty("db.password");

                // Đóng luồng để giải phóng tài nguyên
                input.close();
            }

        } catch (IOException ex) {
            // Nếu có lỗi đọc file (ổ cứng hỏng, không có quyền đọc...) -> Báo lỗi
            JOptionPane.showMessageDialog(null, "Lỗi đọc file cấu hình: " + ex.getMessage());
        }
    }

    /**
     * Tạo và trả về kết nối tới MySQL Database.
     * * @return {@link Connection} đối tượng kết nối.
     * @throws ClassNotFoundException nếu thiếu thư viện JDBC Driver.
     * @throws SQLException nếu sai thông tin đăng nhập hoặc DB chưa chạy.
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // 1. Nạp Driver MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 2. Mở kết nối dựa trên thông tin đã load ở trên
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}