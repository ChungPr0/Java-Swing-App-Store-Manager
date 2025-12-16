package JDBCUtils;

/**
 * Lớp quản lý Phiên làm việc (Session) của người dùng hiện tại.
 * <br>
 * <b>Nhiệm vụ:</b> Lưu trữ thông tin toàn cục (Global) của nhân viên sau khi đăng nhập thành công.
 * Các biến này có thể được truy cập từ bất kỳ đâu trong phần mềm để:
 * <ul>
 * <li>Hiển thị tên người dùng ("Xin chào, Nguyễn Văn A").</li>
 * <li>Ghi lại lịch sử (Ai là người tạo hóa đơn này?).</li>
 * <li>Phân quyền (Ẩn/Hiện nút bấm dựa trên vai trò).</li>
 * </ul>
 */
public class Session {

    // --- 1. THÔNG TIN LƯU TRỮ (STATE) ---

    /**
     * ID của nhân viên trong cơ sở dữ liệu.
     * <br>Giá trị mặc định: -1 (Chưa có ai đăng nhập).
     */
    public static int loggedInStaffID = -1;

    /**
     * Tên hiển thị của nhân viên.
     */
    public static String loggedInStaffName = "";

    /**
     * Vai trò của nhân viên (Ví dụ: "Admin", "Staff").
     * Dùng để quyết định quyền hạn truy cập các chức năng.
     */
    public static String userRole = "";

    /**
     * Cờ đánh dấu trạng thái đăng nhập.
     * <br>True: Đã đăng nhập | False: Chưa đăng nhập.
     */
    public static boolean isLoggedIn = false;


    // --- 2. CÁC PHƯƠNG THỨC XỬ LÝ (BEHAVIOR) ---

    /**
     * Xóa sạch thông tin phiên làm việc hiện tại.
     * <br>
     * <b>Sử dụng khi:</b> Người dùng bấm nút "Đăng Xuất".
     * Các giá trị sẽ được reset về mặc định để tránh người sau dùng nhầm tài khoản cũ.
     */
    public static void clear() {
        loggedInStaffID = -1;
        loggedInStaffName = "";
        userRole = "";
        isLoggedIn = false;
    }

    /**
     * Kiểm tra quyền hạn Quản trị viên.
     *
     * @return <b>true</b> nếu vai trò là "Admin" (không phân biệt hoa thường).
     * <br><b>false</b> nếu là các vai trò khác (Staff, User...).
     */
    public static boolean isAdmin() {
        // So sánh chuỗi không phân biệt hoa thường (Admin == admin == ADMIN)
        return userRole.equalsIgnoreCase("Admin");
    }
}