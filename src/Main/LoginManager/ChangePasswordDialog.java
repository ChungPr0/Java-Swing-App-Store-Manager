package Main.LoginManager;

import Utils.DBConnection;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static Utils.Style.*;

/**
 * Dialog for changing user password.
 */
public class ChangePasswordDialog extends JDialog {

    // --- 1. UI VARIABLES ---
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;

    /**
     * Constructor to initialize the Change Password Dialog.
     *
     * @param parent The parent frame.
     */
    public ChangePasswordDialog(JFrame parent) {
        // Dialog configuration (Modal = true to block parent window interaction)
        super(parent, "Đổi Mật Khẩu", true);
        setSize(400, 500);
        setLocationRelativeTo(parent); // Center on parent screen

        initUI(); // Initialize UI
    }

    // --- 2. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // A. Title
        JLabel lblTitle = createHeaderLabel("ĐỔI MẬT KHẨU");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // B. Password Fields
        txtOldPass = new JPasswordField();
        JCheckBox chkShowOldPass = new JCheckBox();
        JPanel pOld = createPasswordFieldWithLabel(txtOldPass, "Mật khẩu hiện tại:", chkShowOldPass);
        mainPanel.add(pOld);
        mainPanel.add(Box.createVerticalStrut(20));

        txtNewPass = new JPasswordField();
        JCheckBox chkShowNewPass = new JCheckBox();
        JPanel pNew = createPasswordFieldWithLabel(txtNewPass, "Mật khẩu mới:", chkShowNewPass);
        mainPanel.add(pNew);
        mainPanel.add(Box.createVerticalStrut(20));

        txtConfirmPass = new JPasswordField();
        JCheckBox chkShowConfirmPass = new JCheckBox();
        JPanel pConfirm = createPasswordFieldWithLabel(txtConfirmPass, "Xác nhận mật khẩu mới:", chkShowConfirmPass);
        mainPanel.add(pConfirm);
        mainPanel.add(Box.createVerticalStrut(30));

        // C. Button Area
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pBtn.setBackground(Color.WHITE);

        JButton btnSave = createButton("Lưu", new Color(46, 204, 113)); // Green
        JButton btnCancel = createButton("Hủy", new Color(231, 76, 60));     // Red

        pBtn.add(btnSave);
        pBtn.add(btnCancel);
        mainPanel.add(pBtn);

        add(mainPanel);

        // D. Assign Events
        btnCancel.addActionListener(e -> dispose()); // Close window
        btnSave.addActionListener(e -> doChangePassword()); // Perform password change

        // Press Enter to Save
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 3. PASSWORD CHANGE LOGIC ---

    /**
     * Handles the logic for changing the password.
     */
    private void doChangePassword() {
        // 1. Get passwords as char arrays instead of Strings
        char[] oldPass = txtOldPass.getPassword();
        char[] newPass = txtNewPass.getPassword();
        char[] confirmPass = txtConfirmPass.getPassword();

        try {
            // 2. Check for empty fields using array length
            if (oldPass.length == 0 || newPass.length == 0 || confirmPass.length == 0) {
                showError(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // 3. Compare content of two char arrays
            if (!java.util.Arrays.equals(newPass, confirmPass)) {
                showError(this, "Mật khẩu xác nhận không trùng khớp!");
                return;
            }

            if (java.util.Arrays.equals(newPass, oldPass)) {
                showError(this, "Mật khẩu mới không được trùng mật khẩu cũ!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Check old password
                String checkSql = "SELECT * FROM Staffs WHERE sta_ID = ? AND sta_password = ?";
                PreparedStatement psCheck = con.prepareStatement(checkSql);
                psCheck.setInt(1, Session.loggedInStaffID);

                // Create temporary String only to send to DB, then it will be garbage collected
                psCheck.setString(2, new String(oldPass));

                ResultSet rs = psCheck.executeQuery();

                if (!rs.next()) {
                    showError(this, "Mật khẩu hiện tại không đúng!");
                    return;
                }

                // Update new password
                String updateSql = "UPDATE Staffs SET sta_password = ? WHERE sta_ID = ?";
                PreparedStatement psUpdate = con.prepareStatement(updateSql);

                // Create temporary String for update
                psUpdate.setString(1, new String(newPass));
                psUpdate.setInt(2, Session.loggedInStaffID);

                if (psUpdate.executeUpdate() > 0) {
                    showSuccess(this, "Đổi mật khẩu thành công!");
                    dispose();
                }

            } catch (Exception ex) {
                showError(this, "Lỗi kết nối: " + ex.getMessage());
            }
        } finally {
            // 4. IMPORTANT: Clear memory (overwrite all with zeros)
            // This part is always executed, whether the code succeeds or fails
            java.util.Arrays.fill(oldPass, '0');
            java.util.Arrays.fill(newPass, '0');
            java.util.Arrays.fill(confirmPass, '0');
        }
    }
}
