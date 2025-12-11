package StaffForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ChangeStaffForm extends JDialog {
    private JPanel changeStaffTime;
    private JTextField name;
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JComboBox<String> comboBox3;
    private JTextField phone;
    private JButton changeButton;
    private JTextField address;
    private String originalName;

    private boolean isChanged = false;

    public ChangeStaffForm(java.awt.Frame parent, String staffName) {
        super(parent, true);
        this.setContentPane(changeStaffTime);
        this.setSize(550, 250);
        this.setTitle("Sửa Nhân Viên: " + staffName);
        this.setLocationRelativeTo(parent);
        this.originalName = staffName;

        initComboBoxData();
        loadData(staffName);
        addEvents();
    }

    private void loadData(String staffName) {
        String sql = "SELECT * FROM Staffs WHERE sta_name = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, staffName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                name.setText(rs.getString("sta_name"));
                phone.setText(rs.getString("sta_phone"));
                address.setText(rs.getString("sta_address"));

                Date sqlDate = rs.getDate("sta_date_of_birth");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    String nam = parts[0];
                    String thang = parts[1];
                    String ngay = parts[2];

                    comboBox3.setSelectedItem(nam);
                    comboBox2.setSelectedItem(thang);
                    comboBox1.setSelectedItem(ngay);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void addEvents() {
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Connection con = DBConnection.getConnection()) {
                    String strDate = comboBox3.getSelectedItem() + "-" +
                            comboBox2.getSelectedItem() + "-" +
                            comboBox1.getSelectedItem();

                    String sql = "UPDATE Staffs SET sta_name=?, sta_date_of_birth=?, sta_phone=?, sta_address=? WHERE sta_name=?";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name.getText());
                    ps.setString(2, strDate);
                    ps.setString(3, phone.getText());
                    ps.setString(4, address.getText());
                    ps.setString(5, originalName);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(ChangeStaffForm.this, "Cập nhật thành công!");
                        isChanged = true;
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ChangeStaffForm.this, "Không tìm thấy nhân viên để sửa!");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ChangeStaffForm.this, "Lỗi cập nhật: " + ex.getMessage());
                }
            }
        });
    }

    public boolean isChangedSuccess() {
        return isChanged;
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
