# 🛒 Phần Mềm Quản Lý Bán Hàng (Store Manager App)

Ứng dụng desktop quản lý cửa hàng bán lẻ được xây dựng bằng **Java Swing** và cơ sở dữ liệu **SQLite**. Ứng dụng cung cấp giải pháp toàn diện từ quản lý kho, nhân viên, khách hàng, nhà cung cấp đến lập hóa đơn và báo cáo thống kê chi tiết.

---

## 🚀 Tính Năng Nổi Bật

### 1. 📊 Trang Chủ (Dashboard & Thống Kê)
*   **Tổng quan:** Hiển thị nhanh Doanh thu, Số sản phẩm đã bán, Khách hàng hoạt động, và Số đơn hàng.
*   **Bộ lọc thời gian linh hoạt:** Xem báo cáo theo **Hôm nay, 7 ngày qua, Tháng này, Quý này, Năm nay**.
*   **Biểu đồ trực quan:**
    *   Biểu đồ cột (Bar Chart): Phân tích doanh thu theo thời gian thực.
    *   Biểu đồ tròn (Pie Chart): Tỉ lệ phần trăm doanh thu theo danh mục sản phẩm.
*   **Bảng xếp hạng:** Top sản phẩm bán chạy, Top khách hàng chi tiêu nhiều nhất, Top hóa đơn giá trị cao.

### 2. 📦 Quản Lý Sản Phẩm (Product Manager)
*   Thêm, sửa, xóa thông tin sản phẩm.
*   Quản lý số lượng tồn kho, giá bán.
*   Phân loại sản phẩm (Categories) và Nhà cung cấp (Suppliers).
*   Tìm kiếm và sắp xếp sản phẩm thông minh.

### 3. 🧾 Quản Lý Hóa Đơn (Invoice / POS)
*   **Tạo hóa đơn mới:** Chọn khách hàng, nhân viên bán hàng, thêm sản phẩm vào giỏ.
*   **Tự động tính toán:** Tổng tiền, cập nhật trừ kho tự động khi thanh toán.
*   **In hóa đơn:** Xem trước và in hóa đơn (Giao diện mô phỏng HTML).
*   Xem lịch sử và chi tiết hóa đơn đã bán.

### 4. 👥 Quản Lý Đối Tác & Nhân Sự
*   **Nhân viên (Staff):** Quản lý hồ sơ, phân quyền (Admin/Staff), cấp tài khoản đăng nhập.
*   **Khách hàng (Customer):** Lưu trữ thông tin, lịch sử mua hàng.
*   **Nhà cung cấp (Supplier):** Quản lý nguồn nhập hàng, xem danh sách sản phẩm cung cấp.

### 5. 🔐 Hệ Thống & Bảo Mật
*   Đăng nhập / Đăng xuất an toàn.
*   Phân quyền chức năng dựa trên vai trò (Admin có toàn quyền, Staff bị giới hạn một số chức năng quản lý).
*   Đổi mật khẩu cá nhân.

---

## 🛠 Công Nghệ Sử Dụng

*   **Ngôn ngữ:** Java (JDK 8 trở lên).
*   **Giao diện (GUI):** Java Swing (Sử dụng các Custom Components, CardLayout, BorderLayout...).
*   **Cơ sở dữ liệu:** SQLite (Lưu trữ cục bộ, không cần cài đặt server phức tạp).
*   **Thư viện:** `sqlite-jdbc` (Kết nối Database).
*   **Mô hình:** MVC (Model-View-Controller) pattern (tương đối).

---

## ⚙️ Cài Đặt & Chạy Ứng Dụng

### Yêu cầu
*   Java Development Kit (JDK) phiên bản 8 trở lên.
*   IDE: IntelliJ IDEA, Eclipse, hoặc NetBeans.

### Các bước thực hiện
1.  **Clone dự án:**
    ```bash
    git clone https://github.com/username/Java-Swing-App-Store-Manager.git
    ```
2.  **Mở dự án:** Mở thư mục dự án trong IDE của bạn.
3.  **Cấu hình thư viện:**
    *   Đảm bảo file `sqlite-jdbc-....jar` đã được thêm vào **Libraries/Classpath** của dự án.
4.  **Cơ sở dữ liệu:**
    *   File `storedatabase.db` sẽ tự động được tạo hoặc sử dụng file có sẵn trong thư mục gốc.
    *   Nếu cần reset dữ liệu, bạn có thể chạy script trong `ResourcesPack/csdl.sql` bằng một công cụ quản lý SQLite (như *DB Browser for SQLite*).
5.  **Chạy ứng dụng:**
    *   Tìm file `src/Main/Main.java` (hoặc `src/Main/LoginManager/LoginForm.java`) và chạy (Run).

---

## 📂 Cấu Trúc Thư Mục

```
Java-Swing-App-Store-Manager/
├── assets/                 # Chứa hình ảnh, icon
├── src/
│   ├── Main/
│   │   ├── CustomerManager/ # Quản lý khách hàng
│   │   ├── HomeManager/     # Màn hình chính & Thống kê
│   │   ├── InvoiceManager/  # Quản lý hóa đơn
│   │   ├── LoginManager/    # Đăng nhập & Đổi mật khẩu
│   │   ├── ProductManager/  # Quản lý sản phẩm
│   │   ├── StaffManager/    # Quản lý nhân viên
│   │   ├── SupplierManager/ # Quản lý nhà cung cấp
│   │   └── DashBoard.java   # Khung giao diện chính
│   └── Utils/
│       ├── DBConnection.java # Kết nối SQLite
│       └── ...
├── storedatabase.db        # File cơ sở dữ liệu SQLite
└── README.md
```

---

## 📸 Screenshots

*(Bạn có thể thêm ảnh chụp màn hình ứng dụng tại đây)*

---

## 👨‍💻 Tác Giả

Dự án được phát triển bởi **[Chung]**.
Mọi đóng góp và ý kiến phản hồi đều được hoan nghênh!
