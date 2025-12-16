package CustomerForm;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static JDBCUtils.Style.*;

public class AddCustomerForm extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN (UI) ---
    private JTextField txtName, txtPhone, txtAddress;
    private JButton btnSave, btnCancel;

    // --- 2. BIẾN TRẠNG THÁI ---
    // Cờ đánh dấu xem việc thêm mới có thành công không để báo lại cho form cha
    private boolean isAdded = false;

    public AddCustomerForm(Frame parent) {
        // Cấu hình Dialog: Modal = true (chặn tương tác với cửa sổ cha khi form này mở)
        super(parent, true);
        this.setTitle("Thêm Khách Hàng Mới");

        initUI();       // Khởi tạo giao diện
        addEvents();    // Gán sự kiện nút bấm

        this.pack();
        this.setLocationRelativeTo(parent); // Căn giữa màn hình cha
        this.setResizable(false);
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (INIT UI) ---
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
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên khách hàng:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Khu vực nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);

        // Bấm Enter sẽ kích hoạt nút Lưu
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. XỬ LÝ SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // --- CHẶN NHẬP CHỮ CHO SỐ ĐIỆN THOẠI ---
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                // Nếu ký tự gõ vào không phải số -> Hủy bỏ (không cho nhập)
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Sự kiện nút Lưu
        btnSave.addActionListener(_ -> {
            if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty()) {
                showError(AddCustomerForm.this, "Vui lòng nhập Tên và Số điện thoại!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO Customers (cus_name, cus_phone, cus_address) VALUES (?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtPhone.getText().trim());
                ps.setString(3, txtAddress.getText().trim());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showSuccess(AddCustomerForm.this, "Thêm thành công!");
                    isAdded = true; // Đánh dấu thành công
                    dispose();      // Đóng cửa sổ
                }
            } catch (Exception ex) {
                showError(AddCustomerForm.this, "Lỗi: " + ex.getMessage());
            }
        });

        // Sự kiện nút Hủy
        btnCancel.addActionListener(_ -> dispose());
    }

    // --- 5. HÀM TIỆN ÍCH ---
    public boolean isAddedSuccess() {
        return isAdded;
    }
}