package Main.SupplierManager;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;
// Import DashBoard để gọi hàm chuyển tab

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel; // Import TableModel
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter; // Import MouseAdapter
import java.awt.event.MouseEvent;   // Import MouseEvent
import java.sql.*;
import java.text.DecimalFormat;     // Import DecimalFormat

import static JDBCUtils.Style.*;

public class SupplierManagerPanel extends JPanel {
    // --- KHAI BÁO BIẾN GIAO DIỆN ---
    private JList<ComboItem> listSupplier;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JTextArea txtDescription;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnSort;

    // [MỚI] Khai báo bảng sản phẩm
    private JTable tableProducts;
    private DefaultTableModel modelProducts;

    // --- BIẾN TRẠNG THÁI ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "NEW", "OLD"};
    private int selectedSupID = -1;
    private boolean isDataLoading = false;

    public SupplierManagerPanel() {
        initUI();
        initComboBoxData();
        loadListData();
        addEvents();
        addChangeListeners();
    }

    // --- 1. KHỞI TẠO GIAO DIỆN (UI) ---
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

        listSupplier = new JList<>();
        listSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listSupplier.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listSupplier), BorderLayout.CENTER);

        // B. PANEL PHẢI
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN NHÀ CUNG CẤP"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtName, "Tên Nhà Cung Cấp:"));
        rightPanel.add(Box.createVerticalStrut(15));

        cbDay = new JComboBox<>();
        cbMonth = new JComboBox<>();
        cbYear = new JComboBox<>();
        JPanel pDate = createDatePanel("Ngày bắt đầu hợp tác:", cbDay, cbMonth, cbYear);
        rightPanel.add(pDate);
        rightPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtDescription = new JTextArea(4, 20);
        JPanel pDescription = createTextAreaWithLabel(txtDescription, "Mô tả / Ghi chú:");
        rightPanel.add(pDescription);
        rightPanel.add(Box.createVerticalStrut(15));

        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Giá Bán", "Tồn Kho", "Đã Bán"};
        modelProducts = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableProducts = new JTable(modelProducts);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableProducts.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        tableProducts.getColumnModel().getColumn(0).setMaxWidth(50);
        tableProducts.getColumnModel().getColumn(0).setMinWidth(50);

        JPanel pTable = createTableWithLabel(tableProducts, "CÁC SẢN PHẨM CUNG CẤP");
        pTable.setPreferredSize(new Dimension(0, 180));

        rightPanel.add(pTable);
        rightPanel.add(Box.createVerticalStrut(15));

        // C. KHU VỰC NÚT BẤM
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Thêm nhà cung cấp", Color.decode("#3498db"));
        btnSave = createButton("Lưu thay đổi", new Color(46, 204, 113));
        btnDelete = createButton("Xóa nhà cung cấp", new Color(231, 76, 60));

        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        rightPanel.add(buttonPanel);

        // Thêm Scroll cho Panel phải
        JScrollPane rightScroll = new JScrollPane(rightPanel);
        rightScroll.setBorder(null);
        rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightScroll, BorderLayout.CENTER);

        enableForm(false);
    }

    // --- 2. TẢI DỮ LIỆU DANH SÁCH ---
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT sup_id, sup_name FROM Suppliers");

            if (isSearching) {
                sql.append(" WHERE sup_name LIKE ?");
            }

            switch (currentSortIndex) {
                case 1: sql.append(" ORDER BY sup_name DESC"); break;
                case 2: sql.append(" ORDER BY sup_start_date DESC"); break;
                case 3: sql.append(" ORDER BY sup_start_date ASC"); break;
                default: sql.append(" ORDER BY sup_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());

            if (isSearching) {
                ps.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("sup_id");
                String name = rs.getString("sup_name");
                model.addElement(new ComboItem(name, id));
            }
            listSupplier.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadSupplierProducts(int supID) {
        modelProducts.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");

        try (Connection con = DBConnection.getConnection()) {
            // Query lấy thông tin SP + Tổng đã bán (từ invoice_details)
            String sql = """
                SELECT p.pro_id, p.pro_name, p.pro_price, p.pro_count,\s
                       COALESCE(SUM(d.ind_count), 0) as sold_count
                FROM Products p
                LEFT JOIN Invoice_details d ON p.pro_id = d.pro_id
                WHERE p.sup_id = ?
                GROUP BY p.pro_id, p.pro_name, p.pro_price, p.pro_count
           \s""";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, supID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modelProducts.addRow(new Object[]{
                        rs.getInt("pro_id"),
                        rs.getString("pro_name"),
                        df.format(rs.getDouble("pro_price")),
                        rs.getInt("pro_count"),
                        rs.getInt("sold_count")
                });
            }
        } catch (Exception ignored) {}
    }

    // --- 3. TẢI CHI TIẾT ---
    private void loadDetail(int id) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Suppliers WHERE sup_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedSupID = rs.getInt("sup_id");

                txtName.setText(rs.getString("sup_name"));
                txtPhone.setText(rs.getString("sup_phone"));
                txtAddress.setText(rs.getString("sup_address"));
                txtDescription.setText(rs.getString("sup_description"));

                Date sqlDate = rs.getDate("sup_start_date");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    cbYear.setSelectedItem(parts[0]);
                    cbMonth.setSelectedItem(parts[1]);
                    cbDay.setSelectedItem(parts[2]);
                } else {
                    cbYear.setSelectedIndex(0);
                    cbMonth.setSelectedIndex(0);
                    cbDay.setSelectedIndex(0);
                }

                enableForm(true);
                btnDelete.setVisible(true);
                btnSave.setVisible(false);
                btnAdd.setVisible(true);

                loadSupplierProducts(selectedSupID);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // --- 4. XỬ LÝ SỰ KIỆN ---
    private void addEvents() {
        listSupplier.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listSupplier.getSelectedValue();
                if (selected != null) {
                    selectedSupID = selected.getValue();
                    loadDetail(selectedSupID);
                }
            }
        });

        tableProducts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click
                    int row = tableProducts.getSelectedRow();
                    if (row != -1) {
                        int proID = Integer.parseInt(tableProducts.getValueAt(row, 0).toString());

                        Window win = SwingUtilities.getWindowAncestor(SupplierManagerPanel.this);

                        if (win instanceof Main.DashBoard) {
                            ((Main.DashBoard) win).showProductAndLoad(proID);
                        }
                    }
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        });

        btnSort.addActionListener(e -> {
            currentSortIndex++;
            if (currentSortIndex >= sortModes.length) {
                currentSortIndex = 0;
            }
            btnSort.setText(sortModes[currentSortIndex]);
            // Tooltip hướng dẫn
            switch (currentSortIndex) {
                case 0: btnSort.setToolTipText("A -> Z"); break;
                case 1: btnSort.setToolTipText("Z -> A"); break;
                case 2: btnSort.setToolTipText("Hợp tác gần đây nhất"); break;
                case 3: btnSort.setToolTipText("Hợp tác lâu năm nhất"); break;
            }
            loadListData();
        });

        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddSupplierDialog addSupplierDialog = new AddSupplierDialog(parentFrame);
            addSupplierDialog.setVisible(true);

            if (addSupplierDialog.isAddedSuccess()) {
                txtSearch.setText("");
                loadListData();
                int newID = addSupplierDialog.getNewSupplierID();
                if (newID != -1) selectSupplierByID(newID);
            }
        });

        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() ||
                    txtPhone.getText().trim().isEmpty() ||
                    txtAddress.getText().trim().isEmpty()) {
                showError(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
                String sql = "UPDATE Suppliers SET sup_name=?, sup_phone=?, sup_address=?, sup_start_date=?, sup_description=? WHERE sup_id=?";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtPhone.getText().trim());
                ps.setString(3, txtAddress.getText().trim());
                ps.setString(4, strDate);
                ps.setString(5, txtDescription.getText().trim());
                ps.setInt(6, selectedSupID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();
                    selectSupplierByID(selectedSupID);
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if(showConfirm(this, "Xóa nhà cung cấp này?")){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Suppliers WHERE sup_id=?");
                    ps.setInt(1, selectedSupID);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("foreign key")) {
                        showError(this, "Không thể xóa vì đang cung cấp sản phẩm!");
                    } else {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    // --- 5. THEO DÕI THAY ĐỔI ---
    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
        txtDescription.getDocument().addDocumentListener(docListener);

        ActionListener actionListener = e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        };
        cbDay.addActionListener(actionListener);
        cbMonth.addActionListener(actionListener);
        cbYear.addActionListener(actionListener);
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            cbDay.addItem(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            cbMonth.addItem(String.format("%02d", i));
        }
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear; i >= 1990; i--) {
            cbYear.addItem(String.valueOf(i));
        }
    }

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");
        txtDescription.setText("");

        if(cbYear.getItemCount() > 0) cbYear.setSelectedIndex(0);
        if(cbMonth.getItemCount() > 0) cbMonth.setSelectedIndex(0);
        if(cbDay.getItemCount() > 0) cbDay.setSelectedIndex(0);

        modelProducts.setRowCount(0);

        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPhone.setEnabled(enable);
        txtAddress.setEnabled(enable);
        txtDescription.setEnabled(enable);
        cbDay.setEnabled(enable);
        cbMonth.setEnabled(enable);
        cbYear.setEnabled(enable);
    }

    private void selectSupplierByID(int id) {
        ListModel<ComboItem> model = listSupplier.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = model.getElementAt(i);
            if (item.getValue() == id) {
                listSupplier.setSelectedIndex(i);
                listSupplier.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public void refreshData() {
        loadListData();
        selectSupplierByID(selectedSupID);
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