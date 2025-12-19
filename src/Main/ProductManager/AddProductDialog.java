package Main.ProductManager;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;
import Main.SupplierManager.AddSupplierDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static JDBCUtils.Style.*;

public class AddProductDialog extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN (UI) ---
    private JTextField txtName, txtPrice, txtCount;
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnAddType, btnAddSupplier;
    private JButton btnSave, btnCancel;

    // --- 2. BIẾN TRẠNG THÁI ---
    private boolean isAdded = false;
    private int newProductID = -1; // Biến lưu ID của sản phẩm vừa thêm

    public AddProductDialog(Frame parent) {
        super(parent, true);
        this.setTitle("Thêm Sản Phẩm Mới");

        initUI();
        loadTypeData();
        loadSupplierData();
        addEvents();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
    }

    // --- 3. KHỞI TẠO GIAO DIỆN ---
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = createHeaderLabel("NHẬP THÔNG TIN");
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPrice, "Giá Bán (VND):"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtCount, "Số Lượng Tồn:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // Phân loại
        cbType = new JComboBox<>();
        btnAddType = createSmallButton("Mới", Color.GRAY);
        mainPanel.add(createComboBoxWithLabel(cbType, "Phân Loại:", btnAddType, null));
        mainPanel.add(Box.createVerticalStrut(15));

        // Nhà cung cấp
        cbSupplier = new JComboBox<>();
        btnAddSupplier = createSmallButton("Mới", Color.GRAY);
        mainPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:", btnAddSupplier, null));
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. TẢI DỮ LIỆU ---
    private void loadTypeData() {
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_id, type_name FROM ProductTypes ORDER BY type_name ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbType.addItem(new ComboItem(rs.getString("type_name"), rs.getInt("type_id")));
            }
        } catch (Exception ignored) {}
    }

    private void loadSupplierData() {
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_id, sup_name FROM Suppliers ORDER BY sup_name ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbSupplier.addItem(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
            }
        } catch (Exception ignored) {}
    }

    // --- 5. XỬ LÝ SỰ KIỆN ---
    private void addEvents() {

        // Nút Thêm Loại SP
        btnAddType.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent);
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                selectNewestItem(cbType);
            }
        });

        // Nút Thêm NCC
        btnAddSupplier.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            AddSupplierDialog dialog = new AddSupplierDialog(parent);
            dialog.setVisible(true);

            if (dialog.isAddedSuccess()) {
                loadSupplierData();
                selectNewestItem(cbSupplier);
            }
        });

        // Nút Lưu
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() ||
                    txtPrice.getText().trim().isEmpty() ||
                    txtCount.getText().trim().isEmpty()) {
                showError(AddProductDialog.this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                ComboItem selectedType = (ComboItem) cbType.getSelectedItem();
                ComboItem selectedSup = (ComboItem) cbSupplier.getSelectedItem();

                if (selectedType == null || selectedSup == null) {
                    showError(this, "Vui lòng chọn Phân loại và Nhà cung cấp!");
                    return;
                }

                String sql = "INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES (?, ?, ?, ?, ?)";

                // Thêm tham số RETURN_GENERATED_KEYS để lấy ID
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, txtName.getText().trim());
                ps.setDouble(2, Double.parseDouble(txtPrice.getText().trim()));
                ps.setInt(3, Integer.parseInt(txtCount.getText().trim()));
                ps.setInt(4, selectedType.getValue());
                ps.setInt(5, selectedSup.getValue());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Lấy ID vừa sinh ra từ Database
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.newProductID = rs.getInt(1);
                    }

                    showSuccess(AddProductDialog.this, "Thêm sản phẩm thành công!");
                    isAdded = true;
                    dispose();
                }
            } catch (Exception ex) {
                showError(AddProductDialog.this, "Lỗi CSDL: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dispose());

        KeyAdapter numberFilter = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        };
        txtPrice.addKeyListener(numberFilter);
        txtCount.addKeyListener(numberFilter);
    }

    // --- HÀM TIỆN ÍCH ---
    private void selectNewestItem(JComboBox<ComboItem> cb) {
        int maxId = Integer.MIN_VALUE;
        int indexToSelect = -1;
        for (int i = 0; i < cb.getItemCount(); i++) {
            ComboItem item = cb.getItemAt(i);
            if (item != null && item.getValue() > maxId) {
                maxId = item.getValue();
                indexToSelect = i;
            }
        }
        if (indexToSelect != -1) {
            cb.setSelectedIndex(indexToSelect);
            cb.repaint();
        }
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }

    // Getter để lấy ID sản phẩm mới
    public int getNewProductID() {
        return newProductID;
    }
}