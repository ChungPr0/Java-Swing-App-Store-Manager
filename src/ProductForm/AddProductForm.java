package ProductForm;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUtils.Style.*;

public class AddProductForm extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN (UI) ---
    private JTextField txtName, txtPrice, txtCount;
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnSave, btnCancel;

    // --- 2. BIẾN TRẠNG THÁI ---
    private boolean isAdded = false; // Cờ đánh dấu thêm thành công để báo cho form cha reload lại list

    public AddProductForm(Frame parent) {
        super(parent, true); // Modal = true (Chặn tương tác với cửa sổ cha)
        this.setTitle("Thêm Sản Phẩm Mới");

        initUI();               // Dựng giao diện
        loadTypeData();         // Nạp dữ liệu Loại SP vào ComboBox
        loadSupplierData();     // Nạp dữ liệu Nhà Cung Cấp vào ComboBox
        addEvents();            // Gán sự kiện nút bấm

        this.pack();
        this.setLocationRelativeTo(parent); // Căn giữa màn hình
        this.setResizable(false);
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (INIT UI) ---
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
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPrice, "Giá Bán (VND):"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtCount, "Số Lượng Tồn:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Các ComboBox chọn
        cbType = new JComboBox<>();
        mainPanel.add(createComboBoxWithLabel(cbType,"Phân Loại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        mainPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // D. Khu vực nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);

        // Bấm Enter sẽ kích hoạt nút Lưu
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. TẢI DỮ LIỆU COMBOBOX ---

    // Tải danh sách tên Loại sản phẩm
    private void loadTypeData() {
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_id, type_name FROM ProductTypes";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Tạo ComboItem lưu trữ giá trị
                cbType.addItem(new ComboItem(rs.getString("type_name"), rs.getInt("type_id")));
            }
        } catch (Exception e) {
            showError(AddProductForm.this, "Lỗi: " + e.getMessage());
        }
    }

    // Tải danh sách tên Nhà cung cấp
    private void loadSupplierData() {
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_id, sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Tạo ComboItem lưu trữ giá trị
                cbSupplier.addItem(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
            }
        } catch (Exception e) {
            showError(AddProductForm.this, "Lỗi: " + e.getMessage());
        }
    }

    // --- 5. XỬ LÝ SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // Sự kiện nút Lưu
        btnSave.addActionListener(_ -> {
            if (txtName.getText().trim().isEmpty() ||
                    txtPrice.getText().trim().isEmpty() ||
                    txtCount.getText().trim().isEmpty()) {
                showError(AddProductForm.this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                ComboItem selectedType = (ComboItem) cbType.getSelectedItem();
                ComboItem selectedSup = (ComboItem) cbSupplier.getSelectedItem();

                if (selectedType == null || selectedSup == null) {
                    showError(this, "Vui lòng chọn Phân loại và Nhà cung cấp!");
                    return;
                }

                int typeID = selectedType.getValue();
                int supID = selectedSup.getValue();

                String sql = "INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setDouble(2, Double.parseDouble(txtPrice.getText().trim()));
                ps.setInt(3, Integer.parseInt(txtCount.getText().trim()));
                ps.setInt(4, typeID);
                ps.setInt(5, supID);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    showSuccess(AddProductForm.this, "Thêm sản phẩm thành công!");
                    isAdded = true;
                    dispose();
                }
            } catch (Exception ex) {
                showError(AddProductForm.this, "Lỗi CSDL: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(_ -> dispose());

        // Chặn không cho nhập chữ vào ô chứa số
        KeyAdapter numberFilter = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        };
        txtPrice.addKeyListener(numberFilter);
        txtCount.addKeyListener(numberFilter);
    }

    // Getter kiểm tra trạng thái
    public boolean isAddedSuccess() {
        return isAdded;
    }
}