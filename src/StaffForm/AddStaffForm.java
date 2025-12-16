package StaffForm;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static JDBCUtils.Style.*;

public class AddStaffForm extends JDialog {

    // --- KHAI BÁO BIẾN GIAO DIỆN ---
    private JTextField txtName, txtPhone, txtAddress, txtUsername, txtPassword;
    private JCheckBox chkIsAdmin;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnSave, btnCancel;

    // --- BIẾN TRẠNG THÁI ---
    private boolean isAdded = false; // Cờ đánh dấu thêm thành công

    public AddStaffForm(Frame parent) {
        super(parent, true); // Modal = true (chặn cửa sổ cha)
        this.setTitle("Thêm Nhân Viên Mới");

        initUI();               // 1. Dựng giao diện
        initComboBoxData();     // 2. Nạp dữ liệu ngày tháng
        addEvents();            // 3. Gán sự kiện

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
    }

    // --- 1. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Tiêu đề
        JLabel lblTitle = createHeaderLabel("NHẬP THÔNG TIN");
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Các ô nhập liệu
        txtName = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên Nhân Viên:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // Khu vực chọn ngày sinh
        cbDay = new JComboBox<>();
        cbMonth = new JComboBox<>();
        cbYear = new JComboBox<>();
        JPanel datePanel = createDatePanel("Ngày sinh:", cbDay, cbMonth, cbYear);
        mainPanel.add(datePanel);
        mainPanel.add(Box.createVerticalStrut(16));

        txtPhone = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        mainPanel.add(Box.createVerticalStrut(16));

        txtAddress = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        mainPanel.add(Box.createVerticalStrut(16));

        txtUsername = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtUsername, "Tài khoản đăng nhập:"));
        mainPanel.add(Box.createVerticalStrut(16));

        txtPassword = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPassword, "Mật khẩu:"));
        mainPanel.add(Box.createVerticalStrut(16));

        // Checkbox Vai trò
        chkIsAdmin = new JCheckBox();
        JPanel pRoleWrapper = createCheckBoxWithLabel(chkIsAdmin, "Vai trò:", "QUẢN TRỊ VIÊN");
        mainPanel.add(pRoleWrapper);
        mainPanel.add(Box.createVerticalStrut(20));

        // C. Khu vực nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel);
        this.setContentPane(mainPanel);

        // Kích hoạt nút Lưu khi bấm Enter
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 2. XỬ LÝ SỰ KIỆN (EVENTS) ---
    private void addEvents() {

        // --- ĐOẠN NÀY ĐỂ CHẶN NHẬP CHỮ VÀO SỐ ĐIỆN THOẠI ---
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Sự kiện nút Lưu
        btnSave.addActionListener(_ -> {
            // Kiểm tra dữ liệu rỗng (Validate cơ bản)
            if (txtName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()) {
                showError(AddStaffForm.this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Tạo chuỗi ngày sinh YYYY-MM-DD
                String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();

                String sql = "INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address, sta_username, sta_password, sta_role) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, strDate);
                ps.setString(3, txtPhone.getText().trim());
                ps.setString(4, txtAddress.getText().trim());
                ps.setString(5, txtUsername.getText().trim());
                ps.setString(6, txtPassword.getText().trim());
                ps.setString(7, chkIsAdmin.isSelected() ? "Admin" : "Staff");

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showSuccess(AddStaffForm.this, "Thêm nhân viên thành công!");
                    isAdded = true; // Đánh dấu thành công
                    dispose();      // Đóng cửa sổ
                }
            } catch (Exception ex) {
                // Bắt lỗi trùng lặp (Username đã tồn tại)
                if (ex.getMessage().contains("Duplicate")) {
                    showError(AddStaffForm.this, "Tài khoản '" + txtUsername.getText() + "' đã tồn tại!");
                } else {
                    showError(AddStaffForm.this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        // Sự kiện nút Hủy
        btnCancel.addActionListener(_ -> dispose());
    }

    // --- 3. HÀM HỖ TRỢ & GETTER ---
    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) cbMonth.addItem(String.format("%02d", i));
        for (int i = 2025; i >= 1960; i--) cbYear.addItem(String.valueOf(i));
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }
}