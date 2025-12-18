package ProductForm;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;

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
    private JButton btnEditType, btnAddType; // Nút phụ để quản lý Loại SP nhanh
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnSort;

    // --- 2. KHAI BÁO BIẾN TRẠNG THÁI (STATE VARIABLES) ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "PUP", "PDW", "NEW", "OLD"};
    private int selectedProductID = -1;    // ID sản phẩm đang chọn
    private boolean isDataLoading = false; // Cờ chặn sự kiện khi đang đổ dữ liệu

    public ProductManagerPanel() {
        initUI();               // Khởi tạo giao diện
        loadListData();         // Tải danh sách sản phẩm
        loadTypeData();         // Tải danh sách Loại SP
        loadSupplierData();     // Tải danh sách NCC
        addEvents();            // Gán sự kiện click/search
        addChangeListeners();   // Gán sự kiện thay đổi dữ liệu (để hiện nút Lưu)
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // A. PANEL TRÁI (Tìm kiếm & Danh sách)
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

        // B. PANEL PHẢI (Form thông tin chi tiết)
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

        // ComboBox Loại sản phẩm (Kèm nút Thêm/Sửa nhanh)
        cbType = new JComboBox<>();
        btnEditType = createSmallButton("Sửa", Color.LIGHT_GRAY);
        btnAddType = createSmallButton("Thêm", Color.LIGHT_GRAY);
        rightPanel.add(createComboBoxWithLabel(cbType,"Phân Loại:", btnEditType, btnAddType));
        rightPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        rightPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:", null, null));
        rightPanel.add(Box.createVerticalStrut(15));

        // C. KHU VỰC BUTTON (Thêm, Lưu, Xóa)
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

        // Phân quyền: Nếu không phải Admin thì ẩn nút Thêm
        if (!JDBCUtils.Session.isAdmin()) {
            btnAdd.setVisible(false);
        }

        enableForm(false);
    }

    // --- 4. CÁC HÀM TẢI DỮ LIỆU TỪ DB ---

    // 4.1 Tải danh sách sản phẩm (Left Panel)
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT pro_id, pro_name FROM Products");

            if (isSearching) {
                sql.append(" WHERE pro_name LIKE ?");
            }

            // Xử lý sắp xếp
            switch (currentSortIndex) {
                case 1: sql.append(" ORDER BY pro_name DESC"); break;
                case 2: sql.append(" ORDER BY pro_price ASC"); break; // Giá tăng
                case 3: sql.append(" ORDER BY pro_price DESC"); break; // Giá giảm
                case 4: sql.append(" ORDER BY pro_id DESC"); break;   // Mới nhất
                case 5: sql.append(" ORDER BY pro_id ASC"); break;    // Cũ nhất
                default: sql.append(" ORDER BY pro_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());

            if (isSearching) {
                ps.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("pro_id");
                String name = rs.getString("pro_name");
                model.addElement(new ComboItem(name, id));
            }
            listProduct.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    // 4.2 Tải danh sách Loại sản phẩm (ComboBox)
    private void loadTypeData() {
        isDataLoading = true; // Chặn sự kiện
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_id, type_name FROM ProductTypes";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("type_id");
                String name = rs.getString("type_name");
                cbType.addItem(new ComboItem(name, id));
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // 4.3 Tải danh sách Nhà cung cấp (ComboBox)
    private void loadSupplierData() {
        isDataLoading = true; // Chặn sự kiện
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_id, sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("sup_id");
                String name = rs.getString("sup_name");
                cbSupplier.addItem(new ComboItem(name, id));
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // 4.4 Tải chi tiết 1 sản phẩm (Right Panel)
    public void loadDetail(int id) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Products WHERE pro_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Gán lại id để tránh lỗi
                selectedProductID = rs.getInt("pro_id");

                txtName.setText(rs.getString("pro_name"));
                txtPrice.setText(String.format("%.0f", rs.getDouble("pro_price")));
                txtCount.setText(String.valueOf(rs.getInt("pro_count")));

                int typeID = rs.getInt("type_ID");
                setSelectedComboItem(cbType, typeID);

                int supID = rs.getInt("sup_ID");
                setSelectedComboItem(cbSupplier, supID);

                btnSave.setVisible(false);

                // Phân quyền hiển thị nút
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
            if (!JDBCUtils.Session.isAdmin()) {
                btnSave.setVisible(false);
            }
            isDataLoading = false;
        }
    }

    // --- 5. XỬ LÝ CÁC SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // Sự kiện chọn sản phẩm
        listProduct.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listProduct.getSelectedValue();
                if (selected != null) {
                    selectedProductID = selected.getValue();
                    loadDetail(selectedProductID);
                }
            }
        });

        // Sự kiện tìm kiếm
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        // Sự kiện sắp xếp
        btnSort.addActionListener(_ -> {
            currentSortIndex++;
            if (currentSortIndex >= sortModes.length) {
                currentSortIndex = 0;
            }
            btnSort.setText(sortModes[currentSortIndex]);
            // Cập nhật tooltip để người dùng hiểu đang xếp kiểu gì
            switch (currentSortIndex) {
                case 0: btnSort.setToolTipText("Đang xếp: Tên A -> Z"); break;
                case 1: btnSort.setToolTipText("Đang xếp: Tên Z -> A"); break;
                case 2: btnSort.setToolTipText("Đang xếp: Giá thấp đến cao"); break;
                case 3: btnSort.setToolTipText("Đang xếp: Giá cao đến thấp"); break;
                case 4: btnSort.setToolTipText("Đang xếp: Mới nhập trước"); break;
                case 5: btnSort.setToolTipText("Đang xếp: Nhập lâu rồi"); break;
            }
            loadListData();
        });

        // Nút thêm sản phẩm mới
        btnAdd.addActionListener(_ -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddProductForm addProductForm = new AddProductForm(parentFrame);
            addProductForm.setVisible(true);

            if (addProductForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // Nút Lưu thay đổi
        btnSave.addActionListener(_ -> {
            // Kiểm tra rỗng trước khi xử lý
            if (txtName.getText().trim().isEmpty()) {
                showError(this, "Tên sản phẩm không được để trống!");
                return;
            }
            if (txtPrice.getText().trim().isEmpty()) {
                showError(this, "Vui lòng nhập giá bán!");
                return;
            }
            if (txtCount.getText().trim().isEmpty()) {
                showError(this, "Vui lòng nhập số lượng tồn!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                ComboItem selectedType = (ComboItem) cbType.getSelectedItem();
                ComboItem selectedSup = (ComboItem) cbSupplier.getSelectedItem();

                if (selectedType == null || selectedSup == null) {
                    showError(this, "Vui lòng chọn Loại và Nhà cung cấp!");
                    return;
                }

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
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        // Nút Xóa sản phẩm
        btnDelete.addActionListener(_ -> {
            if(showConfirm(this, "Xóa sản phẩm này?")){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE pro_id=?");
                    ps.setInt(1, selectedProductID);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("foreign key")) {
                        showError(this, "Không thể xóa sản phẩm này vì đã có lịch sử giao dịch!\n(Sản phẩm đã nằm trong một hóa đơn nào đó)");
                    } else {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });

        // Nút thêm nhanh Loại SP
        btnAddType.addActionListener(_ -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent);
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                // Tự động chọn cái mới nhất
                if (cbType.getItemCount() > 0) cbType.setSelectedIndex(cbType.getItemCount() - 1);
            }
        });

        // Nút sửa nhanh Loại SP
        btnEditType.addActionListener(_ -> {
            ComboItem currentItem = (ComboItem) cbType.getSelectedItem();
            if (currentItem == null) return;

            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent, currentItem.getValue(), currentItem.toString());
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
            }
        });

        // --- CHẶN NHẬP CHỮ CHO GIÁ VÀ SỐ LƯỢNG ---
        KeyAdapter numberFilter = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                // Nếu không phải số -> chặn luôn
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        };
        txtPrice.addKeyListener(numberFilter);
        txtCount.addKeyListener(numberFilter);
    }

    // --- 6. SỰ KIỆN THEO DÕI THAY ĐỔI FORM ---
    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(_ -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });
        txtName.getDocument().addDocumentListener(docListener);
        txtPrice.getDocument().addDocumentListener(docListener);
        txtCount.getDocument().addDocumentListener(docListener);
        cbType.addActionListener(_ -> checkChange());
        cbSupplier.addActionListener(_ -> checkChange());
    }

    private void checkChange() {
        if (!isDataLoading) btnSave.setVisible(true);
    }

    // --- CÁC HÀM TIỆN ÍCH ---

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
    }

    private void setSelectedComboItem(JComboBox<ComboItem> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getValue() == id) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }

    public void refreshData() {
        loadTypeData();
        loadSupplierData();
        loadListData();
    }

    // Helper Interface & Class cho DocumentListener
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