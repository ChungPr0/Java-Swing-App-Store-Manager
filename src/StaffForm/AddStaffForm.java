package StaffForm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AddStaffForm extends JDialog {
    private JTextField name;
    private JButton addButton;
    private JPanel addStaffTime;
    private JTextField phone;
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JComboBox<String> comboBox3;
    private JTextField address;

    private boolean isAdded = false;

    public AddStaffForm(java.awt.Frame parent) {
        super(parent, true);
        this.setContentPane(addStaffTime);
        this.setSize(550, 250);
        this.setTitle("Thêm Nhân Viên Mới");
        this.setLocationRelativeTo(parent);

        initComboBoxData();
        addEvents();
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/quanlybanhang?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
    }

    public void addEvents() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(name.getText().trim().isEmpty() || phone.getText().trim().isEmpty()){
                    JOptionPane.showMessageDialog(AddStaffForm.this, "Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                Connection con = null;
                PreparedStatement ps = null;

                try {
                    con = getConnection();

                    String ngay = comboBox1.getSelectedItem().toString();
                    String thang = comboBox2.getSelectedItem().toString();
                    String nam = comboBox3.getSelectedItem().toString();
                    String strDate = nam + "-" + thang + "-" + ngay;
                    String sql = "INSERT INTO Staffs (sta_name, sta_date_of_birth, sta_phone, sta_address) VALUES (?, ?, ?, ?)";

                    ps = con.prepareStatement(sql);

                    ps.setString(1, name.getText());
                    ps.setString(2, strDate);
                    ps.setString(3, phone.getText());
                    ps.setString(4, address.getText());

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(AddStaffForm.this, "Thêm nhân viên thành công!");
                        isAdded = true;
                        dispose();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AddStaffForm.this, "Lỗi thêm nhân viên: " + ex.getMessage());
                } finally {
                    try { if(ps != null) ps.close(); if(con != null) con.close(); } catch (Exception ex) {}
                }
            }
        });
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            comboBox1.addItem(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            comboBox2.addItem(String.format("%02d", i));
        }
        for (int i = 2025; i >= 1960; i--) {
            comboBox3.addItem(String.valueOf(i));
        }
    }
}