import JDBCUntils.DBConnection;
import StaffForm.AddStaffForm;
import StaffForm.ChangeStaffForm;

import javax.swing.*;
import java.sql.*;

public class DashBoard extends JFrame {
    private JPanel dashBoardTime;
    private JList<String> listStaff;
    private JButton staffButtonAdd;
    private JButton staffButtonDelete;
    private JButton staffButtonChange;

    public DashBoard(){
        super();
        this.setContentPane(dashBoardTime);
        this.setTitle("Dash Board");
        this.setSize(400,350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        addEvents();
        read();
    }

    void read() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection();) {
            String sql = "SELECT sta_name FROM staffs";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("sta_name");
                model.addElement(name);
            }

            listStaff.setModel(model);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + e.getMessage());
        }
    }

    void addEvents() {
        staffButtonAdd.addActionListener(e -> {
            AddStaffForm addStaffForm = new AddStaffForm(this);
            addStaffForm.setVisible(true);
            if (addStaffForm.isAddedSuccess()) {
                read();
            }
        });

        // Nút Xóa
        staffButtonDelete.addActionListener(e -> {
            String selectedName = listStaff.getSelectedValue();
            if (selectedName == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xóa!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa: " + selectedName + "?");
            if (confirm == JOptionPane.YES_OPTION) {
                delStaff(selectedName);
            }
        });

        // Nút Sửa
        staffButtonChange.addActionListener(e -> {
            String selectedName = listStaff.getSelectedValue();
            if (selectedName == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa!");
                return;
            }

            ChangeStaffForm changeStaffForm = new ChangeStaffForm(this, selectedName);
            changeStaffForm.setVisible(true);

            if (changeStaffForm.isChangedSuccess()) {
                read();
            }

        });
    }


    private void delStaff(String staffName) {
        String sql = "DELETE FROM Staffs WHERE sta_name = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, staffName);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công: " + staffName);
                read();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên này trong CSDL!");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashBoard().setVisible(true));
    }
}
