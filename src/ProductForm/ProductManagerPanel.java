package ProductForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUntils.Style.*;

public class ProductManagerPanel extends JPanel {
    private JList<String> listProduct;
    private JTextField txtSearch, txtName, txtPrice, txtCount;
    private JButton btnAdd, btnSave, btnDelete, btnEditType, btnAddType;
    private JComboBox<String> cbType, cbSupplier;

    private String originalName;
    private boolean isDataLoading = false;

    public ProductManagerPanel() {
        initUI();
        loadListData();
        loadTypeData();
        loadSupplierData();
        addEvents();
        addChangeListeners();
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // --- 1. PANEL TRÁI (Tìm kiếm + List) ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        JPanel searchPanel = createTextFieldWithPlaceholder(txtSearch,"Tìm kiếm");
        btnAdd = createButton("Thêm", Color.GRAY);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnAdd, BorderLayout.EAST);

        listProduct = new JList<>();
        listProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listProduct.setFixedCellHeight(30);

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(listProduct), BorderLayout.CENTER);

        // 2. Panel Phải: Form thông tin
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN SẢN PHẨM"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        JPanel pName = createTextFieldWithLabel(txtName, "Tên Sản Phẩm:");
        rightPanel.add(pName);
        rightPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        JPanel pPhone = createTextFieldWithLabel(txtPrice, "Giá Bán (VND):");
        rightPanel.add(pPhone);
        rightPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        JPanel pAddress = createTextFieldWithLabel(txtCount, "Số Lượng Tồn:");
        rightPanel.add(pAddress);
        rightPanel.add(Box.createVerticalStrut(15));

        cbType = new JComboBox<>();

        btnEditType = createButton("Sửa", Color.GRAY);
        btnAddType = createButton("Thêm", Color.GRAY);

        JPanel pType = createComboBoxWithLabel(cbType,"Phân Loại:", btnEditType, btnAddType);
        rightPanel.add(pType);
        rightPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        JPanel pSupplier = createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:");
        rightPanel.add(pSupplier);
        rightPanel.add(Box.createVerticalStrut(15));

        // Panel Nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = createButton("Lưu thay đổi", Color.GREEN);
        btnDelete = createButton("Xóa sản phẩm", Color.RED);

        // Ẩn mặc định
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        rightPanel.add(buttonPanel);

        // Ghép vào panel chính
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);

        enableForm(false);
    }

    // --- PHẦN LOGIC DỮ LIỆU ---
    private void loadListData() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT pro_name FROM Products";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("pro_name"));
            }
            listProduct.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadTypeData() {
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_name FROM ProductTypes";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbType.addItem(rs.getString("type_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadSupplierData() {
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbSupplier.addItem(rs.getString("sup_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadDetail(String name) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT p.*, s.sup_name, t.type_name " +
                    "FROM Products p " +
                    "LEFT JOIN Suppliers s ON p.sup_ID = s.sup_ID " +
                    "LEFT JOIN ProductTypes t ON p.type_ID = t.type_ID " +
                    "WHERE p.pro_name = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("pro_name"));
                txtPrice.setText(String.format("%.0f", rs.getDouble("pro_price")));
                txtCount.setText(String.valueOf(rs.getInt("pro_count")));
                cbType.setSelectedItem(rs.getString("type_name"));
                cbSupplier.setSelectedItem(rs.getString("sup_name"));

                enableForm(true);
                btnDelete.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
        finally {
            btnSave.setVisible(false);
            isDataLoading = false;
        }
    }

    private void addEvents() {
        listProduct.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listProduct.getSelectedValue();
                if (selected != null) {
                    originalName = selected;
                    loadDetail(selected);
                }
            }
        });

        // 2. Tìm kiếm
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }

            private void doSearch() {
                String key = txtSearch.getText().trim();
                if (key.isEmpty() || key.equals("Tìm kiếm...")) {
                    loadListData();
                } else {
                    search(key);
                }
            }
        });

        // 3. Nút Thêm
        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddProductForm addProductForm = new AddProductForm(parentFrame);
            addProductForm.setVisible(true);

            if (addProductForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // 4. Nút Lưu
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                String selectedTypeName = (String) cbType.getSelectedItem();
                String selectedSupName = (String) cbSupplier.getSelectedItem();
                int typeID = getTypeID(con, selectedTypeName);
                int supID = getSupplierID(con, selectedSupName);

                String sql = "UPDATE Products SET pro_name=?, pro_price=?, pro_count=?, type_ID=?, sup_ID=? WHERE pro_name=?";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, txtName.getText());
                ps.setDouble(2, Double.parseDouble(txtPrice.getText()));
                ps.setInt(3, Integer.parseInt(txtCount.getText()));
                ps.setInt(4, typeID);
                ps.setInt(5, supID);
                ps.setString(6, originalName);

                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadListData();
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        // 5. Nút Xóa
        btnDelete.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Xóa " + originalName + "?") == JOptionPane.YES_OPTION){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE pro_name=?");
                    ps.setString(1, originalName);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnAddType.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

            TypeEditorDialog dialog = new TypeEditorDialog(parent);
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                if (cbType.getItemCount() > 0) cbType.setSelectedIndex(cbType.getItemCount() - 1);
            }
        });

        btnEditType.addActionListener(e -> {
            String currentName = (String) cbType.getSelectedItem();
            if (currentName == null) return;

            try (Connection con = DBConnection.getConnection()) {
                int typeID = getTypeID(con, currentName);
                if (typeID == 0) return;

                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

                TypeEditorDialog dialog = new TypeEditorDialog(parent, typeID, currentName);
                dialog.setVisible(true);

                if (dialog.isUpdated()) {
                    loadTypeData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });
    }

    private int getTypeID(Connection con, String typeName) throws Exception {
        PreparedStatement ps = con.prepareStatement("SELECT type_ID FROM ProductTypes WHERE type_name = ?");
        ps.setString(1, typeName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("type_ID");
        return 0;
    }

    private int getSupplierID(Connection con, String supName) throws Exception {
        PreparedStatement ps = con.prepareStatement("SELECT sup_ID FROM Suppliers WHERE sup_name = ?");
        ps.setString(1, supName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("sup_ID");
        return 0;
    }

    private void search(String key) {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT pro_name FROM Products WHERE pro_name LIKE ?");
            ps.setString(1, "%" + key + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("pro_name"));
            }
            listProduct.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());;
        }
    }

    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });
        txtName.getDocument().addDocumentListener(docListener);
        txtPrice.getDocument().addDocumentListener(docListener);
        txtCount.getDocument().addDocumentListener(docListener);
        cbType.addActionListener(e -> checkChange());
        cbSupplier.addActionListener(e -> checkChange());
    }

    private void checkChange() {
        if (!isDataLoading) btnSave.setVisible(true);
    }

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPrice.setText(""); txtCount.setText("");
        btnSave.setVisible(false); btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPrice.setEnabled(enable);
        txtCount.setEnabled(enable);
        cbType.setEnabled(enable);
        btnEditType.setEnabled(enable);
        btnAddType.setEnabled(enable);
        cbSupplier.setEnabled(enable);
    }

    @FunctionalInterface
    interface DocumentUpdateListener { void update(DocumentEvent e); }

    static class SimpleDocumentListener implements DocumentListener {
        private final DocumentUpdateListener listener;
        public SimpleDocumentListener(DocumentUpdateListener listener) { this.listener = listener; }
        public void insertUpdate(DocumentEvent e) { listener.update(e); }
        public void removeUpdate(DocumentEvent e) { listener.update(e); }
        public void changedUpdate(DocumentEvent e) { listener.update(e); }
    }
}