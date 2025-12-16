package ProductForm;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;

import static JDBCUtils.Style.*;

/**
 * Dialog dùng chung cho 2 chức năng:
 * 1. Thêm mới Phân loại (nếu typeID = -1)
 * 2. Chỉnh sửa / Xóa Phân loại (nếu typeID > 0)
 */
public class TypeEditorDialog extends JDialog {
    // --- 1. KHAI BÁO BIẾN ---
    private JTextField txtName;
    private JButton btnAction; // Nút này sẽ là "Thêm Mới" hoặc "Lưu Thay Đổi" tùy ngữ cảnh
    private JButton btnDelete, btnCancel;

    // Biến dữ liệu
    private final int typeID;  // ID của loại SP (-1 là thêm mới)
    private String currentName = "";
    private boolean isUpdated = false; // Cờ báo cho form cha biết có dữ liệu thay đổi

    // --- 2. CONSTRUCTORS (HÀM KHỞI TẠO) ---

    // Constructor 1: Dùng cho THÊM MỚI (Không cần truyền ID)
    public TypeEditorDialog(Frame parent) {
        super(parent, true); // Modal = true
        this.typeID = -1;    // Đánh dấu là chế độ Thêm
        setupDialog(parent, "THÊM PHÂN LOẠI SẢN PHẨM");
    }

    // Constructor 2: Dùng cho CHỈNH SỬA (Cần truyền ID và Tên cũ)
    public TypeEditorDialog(Frame parent, int typeID, String currentName) {
        super(parent, true);
        this.typeID = typeID;
        this.currentName = currentName;
        setupDialog(parent, "CHỈNH SỬA PHÂN LOẠI SẢN PHẨM");
    }

    // Hàm cấu hình chung cho cả 2 constructor
    private void setupDialog(Frame parent, String title) {
        setTitle(title);
        initUI(title); // Dựng giao diện
        addEvents();   // Gán sự kiện
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (UI) ---
    private void initUI(String titleText) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Tiêu đề
        mainPanel.add(createHeaderLabel(titleText));
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Ô nhập tên
        txtName = new JTextField(currentName); // Nếu sửa thì hiện tên cũ
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên phân loại Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(20));

        // C. Khu vực nút bấm (Thay đổi tùy theo chế độ Thêm hay Sửa)
        JPanel btnPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        if (typeID == -1) {
            // Chế độ Thêm: Nút màu Xanh lá
            btnAction = createButton("Thêm Mới", new Color(46, 204, 113));
        } else {
            // Chế độ Sửa: Nút màu Xanh lá (Lưu)
            btnAction = createButton("Lưu Thay Đổi", new Color(46, 204, 113));
        }

        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        btnCancel = createButton("Hủy", Color.GRAY);

        btnPanel.add(btnAction);

        // Chỉ hiện nút Xóa khi đang ở chế độ Sửa (typeID != -1)
        if (typeID != -1) {
            btnPanel.add(btnDelete);
        }

        btnPanel.add(btnCancel);

        mainPanel.add(btnPanel);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(btnAction);
    }

    // --- 4. XỬ LÝ SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // Sự kiện nút Action (Thêm hoặc Lưu)
        btnAction.addActionListener(_ -> {
            String newName = txtName.getText().trim();
            if (newName.isEmpty()) {
                showError(this, "Tên không được để trống!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if (typeID == -1) {
                    // Logic THÊM MỚI
                    String sql = "INSERT INTO ProductTypes (type_name) VALUES (?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, newName);
                    if (ps.executeUpdate() > 0) {
                        showSuccess(this, "Thêm mới thành công!");
                        isUpdated = true;
                        dispose();
                    }
                } else {
                    // Logic CẬP NHẬT
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

        // Sự kiện nút Xóa (Chỉ có khi sửa)
        if (btnDelete != null) {
            btnDelete.addActionListener(_ -> {
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
                        // Bắt lỗi ràng buộc khóa ngoại (Foreign Key)
                        showError(this, "Không thể xóa vì đang có sản phẩm thuộc loại này!");
                    } catch (Exception ex) {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            });
        }

        // Sự kiện nút Hủy
        btnCancel.addActionListener(_ -> dispose());
    }

    // Getter để form cha biết có cần reload lại ComboBox hay không
    public boolean isUpdated() {
        return isUpdated;
    }
}