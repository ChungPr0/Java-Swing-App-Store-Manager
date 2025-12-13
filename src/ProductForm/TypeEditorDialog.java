package ProductForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;

import static JDBCUntils.Style.*;

public class TypeEditorDialog extends JDialog {
    private JTextField txtName;
    private JButton btnAction, btnDelete, btnCancel;

    private int typeID = -1;
    private String currentName = "";
    private boolean isUpdated = false;

    // --- CONSTRUCTOR 1: Dùng cho THÊM MỚI (Không cần ID) ---
    public TypeEditorDialog(Frame parent) {
        super(parent, true);
        this.typeID = -1;
        setupDialog(parent, "THÊM PHÂN LOẠI SẢN PHẨM");
    }

    // --- CONSTRUCTOR 2: Dùng cho SỬA/XÓA (Có ID và Tên cũ) ---
    public TypeEditorDialog(Frame parent, int typeID, String currentName) {
        super(parent, true);
        this.typeID = typeID;
        this.currentName = currentName;
        setupDialog(parent, "CHỈNH SỬA PHÂN LOẠI SẢN PHẨM");
    }

    private void setupDialog(Frame parent, String title) {
        setTitle(title);
        initUI(title);
        addEvents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI(String titleText) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeaderLabel(titleText));
        mainPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField(currentName);
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên phân loại Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(20));


        JPanel btnPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        if (typeID == -1) {
            btnAction = createButton("Thêm Mới", Color.GREEN);
        } else {
            btnAction = createButton("Lưu Thay Đổi", Color.GREEN);
        }

        btnDelete = createButton("Xóa", Color.RED);
        btnCancel = createButton("Hủy", Color.GRAY);

        btnPanel.add(btnAction);

        if (typeID != -1) {
            btnPanel.add(btnDelete);
        }

        btnPanel.add(btnCancel);

        mainPanel.add(btnPanel);
        setContentPane(mainPanel);
    }

    private void addEvents() {
        btnAction.addActionListener(e -> {
            String newName = txtName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được để trống!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if (typeID == -1) {
                    String sql = "INSERT INTO ProductTypes (type_name) VALUES (?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, newName);
                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                        isUpdated = true;
                        dispose();
                    }
                } else {
                    String sql = "UPDATE ProductTypes SET type_name = ? WHERE type_ID = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, newName);
                    ps.setInt(2, typeID);
                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                        isUpdated = true;
                        dispose();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        if (btnDelete != null) {
            btnDelete.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Xóa loại: " + currentName + "?\n(Không thể xóa nếu đang có sản phẩm thuộc loại này)",
                        "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection con = DBConnection.getConnection()) {
                        String sql = "DELETE FROM ProductTypes WHERE type_ID = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setInt(1, typeID);
                        if (ps.executeUpdate() > 0) {
                            JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                            isUpdated = true;
                            dispose();
                        }
                    } catch (SQLIntegrityConstraintViolationException ex) {
                        JOptionPane.showMessageDialog(this, "Không thể xóa vì đang có sản phẩm thuộc loại này!", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                    }
                }
            });
        }

        btnCancel.addActionListener(e -> dispose());
    }


    public boolean isUpdated() {
        return isUpdated;
    }
}