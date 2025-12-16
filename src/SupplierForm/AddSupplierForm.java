package SupplierForm;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static JDBCUtils.Style.*;

public class AddSupplierForm extends JDialog {

    // --- KHAI BÁO BIẾN GIAO DIỆN ---
    private JTextField txtName, txtPhone, txtAddress;
    private JButton btnSave, btnCancel;

    // --- BIẾN TRẠNG THÁI ---
    private boolean isAdded = false; // Cờ đánh dấu xem có thêm thành công không

    public AddSupplierForm(Frame parent) {
        super(parent, true); // true = Modal (chặn không cho bấm vào cửa sổ cha)
        this.setTitle("Thêm Nhà Cung Cấp Mới");

        initUI();       // 1. Tạo giao diện
        addEvents();    // 2. Gán sự kiện

        this.pack();
        this.setLocationRelativeTo(parent); // Căn giữa so với cửa sổ cha
        this.setResizable(false);
    }

    // --- 1. KHỞI TẠO GIAO DIỆN ---
    private void initUI() {
        // Setup Panel chính
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
        JPanel pName = createTextFieldWithLabel(txtName, "Tên Nhà Cung Cấp:");
        mainPanel.add(pName);
        mainPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        JPanel pPhone = createTextFieldWithLabel(txtPhone, "Số điện thoại:");
        mainPanel.add(pPhone);
        mainPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        JPanel pAddress = createTextFieldWithLabel(txtAddress, "Địa chỉ:");
        mainPanel.add(pAddress);
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Khu vực nút bấm (Save / Cancel)
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

    // --- 2. XỬ LÝ SỰ KIỆN ---
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
            // Kiểm tra rỗng
            if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty()) {
                showError(AddSupplierForm.this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // Kết nối CSDL và thêm mới
            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO Suppliers (sup_name, sup_phone, sup_address) VALUES (?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtPhone.getText().trim());
                ps.setString(3, txtAddress.getText().trim());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showSuccess(AddSupplierForm.this, "Thêm thành công!");
                    isAdded = true; // Đánh dấu thành công để load lại danh sách bên ngoài
                    dispose();      // Đóng cửa sổ
                }
            } catch (Exception ex) {
                showError(AddSupplierForm.this, "Lỗi: " + ex.getMessage());
            }
        });

        // Sự kiện nút Hủy -> Đóng form
        btnCancel.addActionListener(_ -> dispose());
    }

    // --- Getter trả về kết quả ---
    public boolean isAddedSuccess() {
        return isAdded;
    }
}