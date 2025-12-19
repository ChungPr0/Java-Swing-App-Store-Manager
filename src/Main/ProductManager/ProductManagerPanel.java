package Main.ProductManager;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;
import Main.SupplierManager.AddSupplierDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUtils.Style.*;

public class ProductManagerPanel extends JPanel {
    // --- 1. KHAI BÁO BIẾN GIAO DIỆN (UI COMPONENTS) ---
    private JList<ComboItem> listProduct;
    private JTextField txtSearch, txtName, txtPrice, txtCount;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnEditType, btnAddType;
    private JButton btnAddSupplier;
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnSort;

    // --- 2. KHAI BÁO BIẾN TRẠNG THÁI (STATE VARIABLES) ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "PUP", "PDW", "NEW", "OLD"};
    private int selectedProductID = -1;
    private boolean isDataLoading = false;

    public ProductManagerPanel() {
        initUI();
        loadListData();
        loadTypeData();
        loadSupplierData();
        addEvents();
        addChangeListeners();
    }

    // --- 3. KHỞI TẠO GIAO DIỆN ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // A. PANEL TRÁI
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        btnSort = new JButton("A-Z");
        btnSort.setToolTipText("Đang xếp: Tên A-Z");
        btnSort.setFocusable(false);

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listProduct = new JList<>();
        listProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listProduct.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listProduct), BorderLayout.CENTER);

        // B. PANEL PHẢI
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN SẢN PHẨM"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtName, "Tên Sản Phẩm:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPrice, "Giá Bán (VND):"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtCount, "Số Lượng Tồn:"));
        rightPanel.add(Box.createVerticalStrut(15));

        cbType = new JComboBox<>();
        btnEditType = createSmallButton("Sửa", Color.GRAY);
        btnAddType = createSmallButton("Mới", Color.GRAY);
        rightPanel.add(createComboBoxWithLabel(cbType,"Phân Loại:", btnEditType, btnAddType));
        rightPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        btnAddSupplier = createSmallButton("Mới", Color.GRAY);
        rightPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:", btnAddSupplier, null));
        rightPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Tạo sản phẩm", Color.decode("#3498db"));
        btnSave = createButton("Lưu thay đổi", new Color(46, 204, 113));
        btnDelete = createButton("Xóa sản phẩm", new Color(231, 76, 60));

        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        rightPanel.add(buttonPanel);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);

        if (!JDBCUtils.Session.isAdmin()) {
            btnAdd.setVisible(false);
        }

        enableForm(false);
    }

    // --- 4. TẢI DỮ LIỆU ---
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT pro_id, pro_name FROM Products");

            if (isSearching) sql.append(" WHERE pro_name LIKE ?");

            switch (currentSortIndex) {
                case 1: sql.append(" ORDER BY pro_name DESC"); break;
                case 2: sql.append(" ORDER BY pro_price ASC"); break;
                case 3: sql.append(" ORDER BY pro_price DESC"); break;
                case 4: sql.append(" ORDER BY pro_id DESC"); break;
                case 5: sql.append(" ORDER BY pro_id ASC"); break;
                default: sql.append(" ORDER BY pro_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());
            if (isSearching) ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(new ComboItem(rs.getString("pro_name"), rs.getInt("pro_id")));
            }
            listProduct.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadTypeData() {
        isDataLoading = true;
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_id, type_name FROM ProductTypes";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbType.addItem(new ComboItem(rs.getString("type_name"), rs.getInt("type_id")));
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    private void loadSupplierData() {
        isDataLoading = true;
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_id, sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbSupplier.addItem(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    public void loadDetail(int id) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Products WHERE pro_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedProductID = rs.getInt("pro_id");
                txtName.setText(rs.getString("pro_name"));
                txtPrice.setText(String.format("%.0f", rs.getDouble("pro_price")));
                txtCount.setText(String.valueOf(rs.getInt("pro_count")));

                int typeID = rs.getInt("type_ID");
                setSelectedComboItem(cbType, typeID);

                int supID = rs.getInt("sup_ID");
                setSelectedComboItem(cbSupplier, supID);

                btnSave.setVisible(false);

                if (JDBCUtils.Session.isAdmin()) {
                    enableForm(true);
                    btnDelete.setVisible(true);
                    btnAdd.setVisible(true);
                } else {
                    enableForm(false);
                    btnDelete.setVisible(false);
                    btnAdd.setVisible(false);
                }
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
        finally {
            if (!JDBCUtils.Session.isAdmin()) btnSave.setVisible(false);
            isDataLoading = false;
        }
    }

    // --- 5. SỰ KIỆN ---
    private void addEvents() {
        listProduct.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listProduct.getSelectedValue();
                if (selected != null) {
                    selectedProductID = selected.getValue();
                    loadDetail(selectedProductID);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        btnSort.addActionListener(e -> {
            currentSortIndex++;
            if (currentSortIndex >= sortModes.length) currentSortIndex = 0;
            btnSort.setText(sortModes[currentSortIndex]);
            loadListData();
        });

        // Nút thêm sản phẩm: Thêm xong tự chọn sản phẩm mới
        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddProductDialog addProductDialog = new AddProductDialog(parentFrame);
            addProductDialog.setVisible(true);

            if (addProductDialog.isAddedSuccess()) {
                // 1. Xóa ô tìm kiếm để đảm bảo hiển thị đủ danh sách
                txtSearch.setText("");

                // 2. Tải lại danh sách
                loadListData();

                // 3. Lấy ID sản phẩm vừa tạo và chọn nó
                int newID = addProductDialog.getNewProductID();
                if (newID != -1) {
                    selectProductByID(newID);
                }
            }
        });

        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty()) { showError(this, "Tên không được để trống!"); return; }
            try (Connection con = DBConnection.getConnection()) {
                ComboItem selectedType = (ComboItem) cbType.getSelectedItem();
                ComboItem selectedSup = (ComboItem) cbSupplier.getSelectedItem();
                if (selectedType == null || selectedSup == null) { showError(this, "Chọn đủ thông tin!"); return; }

                String sql = "UPDATE Products SET pro_name=?, pro_price=?, pro_count=?, type_ID=?, sup_ID=? WHERE pro_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText().trim());
                ps.setDouble(2, Double.parseDouble(txtPrice.getText().trim()));
                ps.setInt(3, Integer.parseInt(txtCount.getText().trim()));
                ps.setInt(4, selectedType.getValue());
                ps.setInt(5, selectedSup.getValue());
                ps.setInt(6, selectedProductID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();

                    // Vẫn giữ lựa chọn ở dòng hiện tại
                    selectProductByID(selectedProductID);

                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if(showConfirm(this, "Xóa sản phẩm này?")){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE pro_id=?");
                    ps.setInt(1, selectedProductID);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("foreign key")) showError(this, "Sản phẩm đã có giao dịch, không thể xóa!");
                    else showError(this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnAddType.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent);
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                selectNewestItem(cbType);
            }
        });

        btnEditType.addActionListener(e -> {
            ComboItem currentItem = (ComboItem) cbType.getSelectedItem();
            if (currentItem == null) return;
            int savedID = currentItem.getValue();

            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent, currentItem.getValue(), currentItem.toString());
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                setSelectedComboItem(cbType, savedID);
            }
        });

        btnAddSupplier.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddSupplierDialog dialog = new AddSupplierDialog(parent);
            dialog.setVisible(true);

            if (dialog.isAddedSuccess()) {
                loadSupplierData();
                selectNewestItem(cbSupplier);
            }
        });

        KeyAdapter numberFilter = new KeyAdapter() {
            public void keyTyped(KeyEvent e) { if (!Character.isDigit(e.getKeyChar())) e.consume(); }
        };
        txtPrice.addKeyListener(numberFilter);
        txtCount.addKeyListener(numberFilter);
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
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        boolean isAdmin = JDBCUtils.Session.isAdmin();
        txtName.setEnabled(enable);
        txtPrice.setEnabled(enable);
        txtCount.setEnabled(enable);
        cbType.setEnabled(enable);
        cbSupplier.setEnabled(enable);
        btnEditType.setVisible(enable && isAdmin);
        btnAddType.setVisible(enable && isAdmin);
        btnAddSupplier.setVisible(enable && isAdmin);
    }

    // Hàm chọn sản phẩm trong JList theo ID
    private void selectProductByID(int id) {
        ListModel<ComboItem> model = listProduct.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = model.getElementAt(i);
            if (item.getValue() == id) {
                listProduct.setSelectedIndex(i);
                listProduct.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    private void setSelectedComboItem(JComboBox<ComboItem> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getValue() == id) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }

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

    public void refreshData() {
        loadTypeData();
        loadSupplierData();
        loadListData();
        selectProductByID(selectedProductID);
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