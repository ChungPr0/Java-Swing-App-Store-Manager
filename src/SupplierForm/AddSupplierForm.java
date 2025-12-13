package SupplierForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static JDBCUntils.Functions.*;

public class AddSupplierForm extends JDialog {
    private JTextField txtName, txtPhone, txtAddress;
    private JButton btnSave, btnCancel;

    private boolean isAdded = false;

    public AddSupplierForm(Frame parent) {
        super(parent, true); // Modal = true (chặn form cha)
        this.setTitle("Thêm Nhà Cung Cấp Mới");
        initUI();
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
                    JOptionPane.showMessageDialog(AddSupplierForm.this, "Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                try (Connection con = DBConnection.getConnection()) {
                    String sql = "INSERT INTO Suppliers (sup_name, sup_phone, sup_address) VALUES (?, ?, ?)";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, txtName.getText().trim());
                    ps.setString(2, txtPhone.getText().trim());
                    ps.setString(3, txtAddress.getText().trim());

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(AddSupplierForm.this, "Thêm thành công!");
                        isAdded = true;
                        dispose();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddSupplierForm.this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }

}