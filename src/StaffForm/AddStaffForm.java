package StaffForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static JDBCUntils.Functions.*;

public class AddStaffForm extends JDialog {
    private JTextField txtName, txtPhone, txtAddress;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnSave, btnCancel;

    private boolean isAdded = false;

    public AddStaffForm(Frame parent) {
        super(parent, true); // Modal = true (chặn form cha)
        this.setTitle("Thêm Nhân Viên Mới");
        initUI();
        initComboBoxData();
        addEvents();

        this.pack(); // Tự động co giãn kích thước vừa với nội dung
        this.setLocationRelativeTo(parent); // Hiện giữa form cha
        this.setResizable(false); // Không cho kéo giãn lung tung
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("NHẬP THÔNG TIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.decode("#2c3e50"));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20)); // Khoảng cách

        txtName = createPrettyField(mainPanel, "Họ và Tên:");

        // --- PHẦN NGÀY SINH---
        JPanel dobContainer = new JPanel(new BorderLayout(5, 5));
        dobContainer.setBackground(Color.WHITE);
        dobContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        JLabel lblDob = createTitleLabel("Ngày sinh:");
        JPanel pickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pickerPanel.setBackground(Color.WHITE);
        cbDay = createStyleComboBox();
        cbMonth = createStyleComboBox();
        cbYear = createStyleComboBox();
        pickerPanel.add(cbDay);
        pickerPanel.add(createSeparator());
        pickerPanel.add(cbMonth);
        pickerPanel.add(createSeparator());
        pickerPanel.add(cbYear);

        // 6. Ghép lại
        dobContainer.add(lblDob, BorderLayout.NORTH);
        dobContainer.add(pickerPanel, BorderLayout.CENTER);

        // 7. Thêm vào Panel chính
        mainPanel.add(dobContainer);
        mainPanel.add(Box.createVerticalStrut(15));

        txtPhone = createPrettyField(mainPanel, "Số điện thoại:");
        txtAddress = createPrettyField(mainPanel, "Địa chỉ:");

        // 3. Các nút bấm (Lưu / Hủy)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);
    }

    // --- CÁC HÀM LOGIC ---
    private void addEvents() {
        // Nút Lưu
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(AddStaffForm.this, "Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                try (Connection con = DBConnection.getConnection()) {
                    String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
                    String sql = "INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address) VALUES (?, ?, ?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, txtName.getText().trim());
                    ps.setString(2, strDate);
                    ps.setString(3, txtPhone.getText().trim());
                    ps.setString(4, txtAddress.getText().trim());

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(AddStaffForm.this, "Thêm thành công!");
                        isAdded = true;
                        dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddStaffForm.this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) cbMonth.addItem(String.format("%02d", i));
        for (int i = 2025; i >= 1960; i--) cbYear.addItem(String.valueOf(i));
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }

}