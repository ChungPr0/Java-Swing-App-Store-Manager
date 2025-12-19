package Main.StaffManager;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static JDBCUtils.Style.*;

public class AddStaffDialog extends JDialog {

    // --- KHAI BÁO BIẾN GIAO DIỆN ---
    private JTextField txtName, txtPhone, txtAddress, txtSalary, txtUsername, txtPassword;
    private JCheckBox chkIsAdmin;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JComboBox<String> cbStartDay, cbStartMonth, cbStartYear;
    private JButton btnSave, btnCancel;

    // --- BIẾN TRẠNG THÁI ---
    private boolean isAdded = false;
    private int newStaffID = -1; // Biến lưu ID nhân viên vừa thêm

    public AddStaffDialog(Frame parent) {
        super(parent, true); // Modal = true
        this.setTitle("Thêm Nhân Viên Mới");

        initUI();
        initComboBoxData();
        addEvents();

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

        txtSalary = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtSalary, "Lương cơ bản (VNĐ):"));
        mainPanel.add(Box.createVerticalStrut(16));

        cbStartDay = new JComboBox<>();
        cbStartMonth = new JComboBox<>();
        cbStartYear = new JComboBox<>();
        JPanel startDatePanel = createDatePanel("Ngày vào làm:", cbStartDay, cbStartMonth, cbStartYear);
        mainPanel.add(startDatePanel);
        mainPanel.add(Box.createVerticalStrut(16));

        txtUsername = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtUsername, "Tài khoản đăng nhập (Có thể để trống):"));
        mainPanel.add(Box.createVerticalStrut(16));

        txtPassword = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPassword, "Mật khẩu (Có thể để trống):"));
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

        // Chặn nhập chữ vào số điện thoại và lương
        java.awt.event.KeyAdapter digitOnly = new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        };
        txtPhone.addKeyListener(digitOnly);
        txtSalary.addKeyListener(digitOnly);

        // Sự kiện nút Lưu
        btnSave.addActionListener(e -> {
            // Validate
            if (txtName.getText().trim().isEmpty() ||
                    txtPhone.getText().trim().isEmpty() ||
                    txtAddress.getText().trim().isEmpty() ||
                    txtSalary.getText().trim().isEmpty()) {
                showError(AddStaffDialog.this, "Vui lòng nhập đầy đủ: Tên, SĐT, Địa chỉ và Lương!");
                return;
            }

            if (txtUsername.getText().trim().isEmpty() && !txtPassword.getText().trim().isEmpty()) {
                showError(this, "Vui lòng nhập tài khoản nếu có mật khẩu!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String strDob = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
                String strStartDate = cbStartYear.getSelectedItem() + "-" + cbStartMonth.getSelectedItem() + "-" + cbStartDay.getSelectedItem();

                double salary = 0;
                try { salary = Double.parseDouble(txtSalary.getText().trim()); } catch (Exception ignored) {}

                String sql = "INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address, sta_salary, sta_start_date, sta_username, sta_password, sta_role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                // Thêm RETURN_GENERATED_KEYS
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, strDob);
                ps.setString(3, txtPhone.getText().trim());
                ps.setString(4, txtAddress.getText().trim());
                ps.setDouble(5, salary);
                ps.setString(6, strStartDate);

                String user = txtUsername.getText().trim();
                String pass = txtPassword.getText().trim();

                if (user.isEmpty()) ps.setNull(7, java.sql.Types.VARCHAR);
                else ps.setString(7, user);

                if (pass.isEmpty()) ps.setNull(8, java.sql.Types.VARCHAR);
                else ps.setString(8, pass);

                ps.setString(9, chkIsAdmin.isSelected() ? "Admin" : "Staff");

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Lấy ID vừa sinh ra
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.newStaffID = rs.getInt(1);
                    }

                    showSuccess(AddStaffDialog.this, "Thêm nhân viên thành công!");
                    isAdded = true;
                    dispose();
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    showError(AddStaffDialog.this, "Tài khoản '" + txtUsername.getText() + "' đã tồn tại!");
                } else {
                    showError(AddStaffDialog.this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    // --- 3. HÀM HỖ TRỢ & GETTER ---
    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            String val = String.format("%02d", i);
            cbDay.addItem(val);
            cbStartDay.addItem(val);
        }
        for (int i = 1; i <= 12; i++) {
            String val = String.format("%02d", i);
            cbMonth.addItem(val);
            cbStartMonth.addItem(val);
        }
        for (int i = 2025; i >= 1960; i--) {
            String val = String.valueOf(i);
            cbYear.addItem(val);
            cbStartYear.addItem(val);
        }
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }

    // Getter lấy ID nhân viên mới
    public int getNewStaffID() {
        return newStaffID;
    }
}