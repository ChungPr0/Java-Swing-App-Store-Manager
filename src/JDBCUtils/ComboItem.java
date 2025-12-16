package JDBCUtils;

/**
 * Lớp đối tượng hỗ trợ cho JComboBox và JList.
 * <br>
 * Nhiệm vụ: Lưu trữ một cặp dữ liệu gồm (Tên hiển thị - ID ẩn).
 * Giúp hiển thị tên trên giao diện nhưng khi lập trình viên lấy giá trị sẽ nhận được ID (để lưu vào CSDL).
 */
public class ComboItem {

    // --- 1. KHAI BÁO BIẾN ---
    private final String key;   // Chuỗi văn bản hiển thị (Ví dụ: "Nhân viên A", "Laptop Dell")
    private final int value;    // Giá trị ID ẩn tương ứng trong Database (Ví dụ: 1, 50, 102)

    // --- 2. HÀM KHỞI TẠO (CONSTRUCTOR) ---
    public ComboItem(String key, int value) {
        this.key = key;
        this.value = value;
    }

    // --- 3. CÁC PHƯƠNG THỨC (METHODS) ---

    /**
     * Lấy giá trị ID ẩn của đối tượng.
     * Dùng khi người dùng chọn một mục trên ComboBox và bạn cần lấy ID để lưu xuống DB.
     * @return int ID
     */
    public int getValue() {
        return value;
    }

    /**
     * Ghi đè phương thức toString().
     * <br>
     * <b>Quan trọng:</b> Các component như JComboBox, JList trong Java Swing sẽ gọi hàm này
     * để quyết định xem nó sẽ hiển thị chữ gì lên màn hình.
     * @return String tên hiển thị
     */
    @Override
    public String toString() {
        return key;
    }
}