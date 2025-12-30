package Main.ProductManager;

import Utils.DBConnection;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;

import static Utils.Style.*;

/**
 * Dialog for both adding and editing Product Types.
 * 1. Add new Product Type (if typeID = -1)
 * 2. Edit/Delete Product Type (if typeID > 0)
 */
public class TypeEditorDialog extends JDialog {
    // --- 1. VARIABLES ---
    private JTextField txtName;
    private JButton btnAction; // This button will be "Add New" or "Save Changes" depending on the context
    private JButton btnDelete, btnCancel;

    // Data variables
    private final int typeID;  // ID of the product type (-1 for new)
    private String currentName = "";
    private boolean isUpdated = false; // Flag to notify the parent form if data has changed

    // --- 2. CONSTRUCTORS ---

    // Constructor 1: For ADDING NEW (No ID needed)
    public TypeEditorDialog(Frame parent) {
        super(parent, true); // Modal = true
        this.typeID = -1;    // Mark as Add mode
        setupDialog(parent, "THÊM PHÂN LOẠI SẢN PHẨM");
    }

    // Constructor 2: For EDITING (ID and old name needed)
    public TypeEditorDialog(Frame parent, int typeID, String currentName) {
        super(parent, true);
        this.typeID = typeID;
        this.currentName = currentName;
        setupDialog(parent, "CHỈNH SỬA PHÂN LOẠI SẢN PHẨM");
    }

    // Common setup method for both constructors
    private void setupDialog(Frame parent, String title) {
        setTitle(title);
        initUI(title); // Build UI
        addEvents();   // Assign events
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // --- 3. UI INITIALIZATION ---
    private void initUI(String titleText) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Title
        mainPanel.add(createHeaderLabel(titleText));
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Name input
        txtName = new JTextField(currentName); // Show old name if editing
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên phân loại Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(20));

        // C. Button area (Changes depending on Add or Edit mode)
        JPanel btnPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        if (typeID == -1) {
            // Add mode: Green button
            btnAction = createButton("Thêm Mới", new Color(46, 204, 113));
        } else {
            // Edit mode: Green button (Save)
            btnAction = createButton("Lưu Thay Đổi", new Color(46, 204, 113));
        }

        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        btnCancel = createButton("Hủy", Color.GRAY);

        // Only enable actions if user has permission
        if (Session.canManageTypes()) {
            btnAction.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            btnAction.setEnabled(false);
            btnDelete.setEnabled(false);
            txtName.setEnabled(false);
        }

        btnPanel.add(btnAction);

        // Only show Delete button in Edit mode (typeID != -1)
        if (typeID != -1) {
            btnPanel.add(btnDelete);
        }

        btnPanel.add(btnCancel);

        mainPanel.add(btnPanel);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(btnAction);
    }

    // --- 4. EVENT HANDLING ---
    private void addEvents() {
        // Action button event (Add or Save)
        btnAction.addActionListener(e -> {
            if (!Session.canManageTypes()) {
                showError(this, "Bạn không có quyền thực hiện chức năng này!");
                return;
            }

            String newName = txtName.getText().trim();
            if (newName.isEmpty()) {
                showError(this, "Tên không được để trống!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if (typeID == -1) {
                    // ADD NEW logic
                    String sql = "INSERT INTO ProductTypes (type_name) VALUES (?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, newName);
                    if (ps.executeUpdate() > 0) {
                        showSuccess(this, "Thêm mới thành công!");
                        isUpdated = true;
                        dispose();
                    }
                } else {
                    // UPDATE logic
                    String sql = "UPDATE ProductTypes SET type_name = ? WHERE type_ID = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, newName);
                    ps.setInt(2, typeID);
                    if (ps.executeUpdate() > 0) {
                        showSuccess(this, "Cập nhật thành công!");
                        isUpdated = true;
                        dispose();
                    }
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    showError(this, "Loại sản phẩm '" + newName + "' đã tồn tại!");
                } else {
                    showError(this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        // Delete button event (Only when editing)
        if (btnDelete != null) {
            btnDelete.addActionListener(e -> {
                if (!Session.canManageTypes()) {
                    showError(this, "Bạn không có quyền thực hiện chức năng này!");
                    return;
                }

                if (showConfirm(this, "Xóa loại: " + currentName + "?\n(Không thể xóa nếu đang có sản phẩm thuộc loại này)")) {
                    try (Connection con = DBConnection.getConnection()) {
                        String sql = "DELETE FROM ProductTypes WHERE type_ID = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setInt(1, typeID);
                        if (ps.executeUpdate() > 0) {
                            showSuccess(this, "Đã xóa thành công!");
                            isUpdated = true;
                            dispose();
                        }
                    } catch (SQLIntegrityConstraintViolationException ex) {
                        // Catch Foreign Key constraint violation
                        showError(this, "Không thể xóa vì đang có sản phẩm thuộc loại này!");
                    } catch (Exception ex) {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            });
        }

        // Cancel button event
        btnCancel.addActionListener(e -> dispose());
    }

    // Getter for parent form to know if it needs to reload the ComboBox
    public boolean isUpdated() {
        return isUpdated;
    }
}
