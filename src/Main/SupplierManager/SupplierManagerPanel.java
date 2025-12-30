package Main.SupplierManager;

import Utils.ComboItem;
import Utils.DBConnection;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.DecimalFormat;

import static Utils.Style.*;

/**
 * Panel for managing suppliers.
 * Allows adding, editing, deleting, and searching for suppliers.
 */
public class SupplierManagerPanel extends JPanel {
    // --- 1. UI VARIABLES ---
    private JList<ComboItem> listSupplier;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JTextArea txtDescription;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnSort;

    // Product Table
    private JTable tableProducts;
    private DefaultTableModel modelProducts;

    // --- 2. STATE VARIABLES ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "NEW", "OLD"};
    private int selectedSupID = -1; // -1: Create mode
    private boolean isDataLoading = false;

    /**
     * Constructor to initialize the Supplier Manager Panel.
     */
    public SupplierManagerPanel() {
        initUI();
        initComboBoxData();
        loadListData();
        addEvents();
        addChangeListeners();
    }

    // --- 3. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // A. LEFT PANEL (LIST)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        btnSort = new JButton("A-Z");
        btnSort.setFocusable(false);

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm", "Nhập tên nhà cung cấp...");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listSupplier = new JList<>();
        listSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listSupplier.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listSupplier), BorderLayout.CENTER);

        // B. RIGHT PANEL (FORM + TABLE + FOOTER)

        // B1. Form Panel (Scrollable)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        formPanel.add(createHeaderLabel("THÔNG TIN NHÀ CUNG CẤP"));
        formPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtName, "Tên Nhà Cung Cấp:"));
        formPanel.add(Box.createVerticalStrut(15));

        cbDay = new JComboBox<>();
        cbMonth = new JComboBox<>();
        cbYear = new JComboBox<>();
        JPanel pDate = createDatePanel("Ngày bắt đầu hợp tác:", cbDay, cbMonth, cbYear);
        formPanel.add(pDate);
        formPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        formPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        formPanel.add(Box.createVerticalStrut(15));

        txtDescription = new JTextArea(4, 20);
        JPanel pDescription = createTextAreaWithLabel(txtDescription, "Mô tả / Ghi chú:");
        formPanel.add(pDescription);
        formPanel.add(Box.createVerticalStrut(15));

        // Supplied Products Table
        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Giá Bán", "Tồn Kho", "Đã Bán"};
        modelProducts = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tableProducts = new JTable(modelProducts);

        // Center align Product ID column
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableProducts.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableProducts.getColumnModel().getColumn(0).setMaxWidth(50);

        JPanel pTable = createTableWithLabel(tableProducts, "CÁC SẢN PHẨM CUNG CẤP");
        pTable.setPreferredSize(new Dimension(0, 180)); // Fixed height for table
        formPanel.add(pTable);

        formPanel.add(Box.createVerticalGlue()); // Push content up

        JScrollPane scrollForm = new JScrollPane(formPanel);
        scrollForm.setBorder(null);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        // B2. Footer Panel (Fixed)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 0, 5, 0)
        ));

        btnAdd = createButton("Tạo mới", Color.decode("#3498db"));
        btnSave = createButton("Lưu", new Color(46, 204, 113));
        btnDelete = createButton("Xóa", new Color(231, 76, 60));

        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        // Only Admin or StorageStaff can see Add button
        if (!Session.canManageSuppliers()) {
            btnAdd.setVisible(false);
        }

        footerPanel.add(btnAdd);
        footerPanel.add(btnSave);
        footerPanel.add(btnDelete);

        // B3. Combine into Right Panel
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(scrollForm, BorderLayout.CENTER);
        rightContainer.add(footerPanel, BorderLayout.SOUTH);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightContainer, BorderLayout.CENTER);

        enableForm(false);
    }

    // --- 4. DATA LOADING ---

    /**
     * Loads the list of suppliers from the database.
     */
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Nhập tên nhà cung cấp...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT sup_id, sup_name FROM Suppliers");
            if (isSearching) sql.append(" WHERE sup_name LIKE ?");

            switch (currentSortIndex) {
                case 1:
                    sql.append(" ORDER BY sup_name DESC");
                    break;
                case 2:
                    sql.append(" ORDER BY sup_start_date DESC");
                    break;
                case 3:
                    sql.append(" ORDER BY sup_start_date ASC");
                    break;
                default:
                    sql.append(" ORDER BY sup_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());
            if (isSearching) ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
            }
            listSupplier.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Loads products supplied by a specific supplier.
     *
     * @param supID The supplier ID.
     */
    private void loadSupplierProducts(int supID) {
        modelProducts.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");

        try (Connection con = DBConnection.getConnection()) {
            // Get products + total sold quantity (from invoice details)
            String sql = """
                        SELECT p.pro_id, p.pro_name, p.pro_price, p.pro_count,
                               COALESCE(SUM(d.ind_count), 0) as sold_count
                        FROM Products p
                        LEFT JOIN Invoice_details d ON p.pro_id = d.pro_id
                        WHERE p.sup_id = ?
                        GROUP BY p.pro_id, p.pro_name, p.pro_price, p.pro_count
                    """;

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
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads details of a specific supplier.
     *
     * @param id The supplier ID.
     */
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

                setComboBoxDate(rs.getString("sup_start_date"), cbDay, cbMonth, cbYear);

                if (Session.canManageSuppliers()) {
                    enableForm(true);
                    btnAdd.setVisible(true);
                    btnDelete.setVisible(true);
                    btnSave.setText("Lưu");
                    btnSave.setVisible(false); // Wait for edit
                } else {
                    enableForm(false);
                    btnAdd.setVisible(false);
                    btnDelete.setVisible(false);
                }

                loadSupplierProducts(selectedSupID);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // --- 5. EVENTS ---

    /**
     * Adds event listeners to components.
     */
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

        // Double click on product table -> Open product details
        tableProducts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
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
            public void insertUpdate(DocumentEvent e) {
                loadListData();
            }

            public void removeUpdate(DocumentEvent e) {
                loadListData();
            }

            public void changedUpdate(DocumentEvent e) {
                loadListData();
            }
        });

        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        });

        btnSort.addActionListener(e -> {
            currentSortIndex = (currentSortIndex + 1) % sortModes.length;
            btnSort.setText(sortModes[currentSortIndex]);
            loadListData();
        });

        // --- ADD BUTTON ---
        btnAdd.addActionListener(e -> prepareCreate());

        // --- SAVE BUTTON (Insert/Update) ---
        btnSave.addActionListener(e -> saveSupplier());

        // --- DELETE BUTTON ---
        btnDelete.addActionListener(e -> deleteSupplier());
    }

    // --- MAIN LOGIC METHODS ---

    /**
     * Prepares the form for creating a new supplier.
     */
    private void prepareCreate() {
        listSupplier.clearSelection();
        selectedSupID = -1; // Create mode

        isDataLoading = true;
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtDescription.setText("");
        cbDay.setSelectedIndex(0);
        cbMonth.setSelectedIndex(0);
        cbYear.setSelectedItem(String.valueOf(java.time.Year.now().getValue()));
        modelProducts.setRowCount(0); // New supplier has no products
        isDataLoading = false;

        enableForm(true);
        txtName.requestFocus();

        btnAdd.setVisible(false);
        btnDelete.setVisible(false);
        btnSave.setText("Lưu");
        btnSave.setVisible(true);
    }

    /**
     * Saves the supplier (Insert or Update).
     */
    private void saveSupplier() {
        if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty()) {
            showError(this, "Tên và SĐT là bắt buộc!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String date = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String addr = txtAddress.getText().trim();
            String desc = txtDescription.getText().trim();

            if (selectedSupID == -1) {
                // INSERT
                String sql = "INSERT INTO Suppliers (sup_name, sup_phone, sup_address, sup_start_date, sup_description) VALUES (?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, addr);
                ps.setString(4, date);
                ps.setString(5, desc);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    showSuccess(this, "Thêm thành công!");
                    loadListData();
                    selectSupplierByID(rs.getInt(1));
                }
            } else {
                // UPDATE
                String sql = "UPDATE Suppliers SET sup_name=?, sup_phone=?, sup_address=?, sup_start_date=?, sup_description=? WHERE sup_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, addr);
                ps.setString(4, date);
                ps.setString(5, desc);
                ps.setInt(6, selectedSupID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();
                    selectSupplierByID(selectedSupID);
                }
            }
        } catch (Exception ex) {
            showError(this, "Lỗi: " + ex.getMessage());
        }
    }

    /**
     * Deletes the selected supplier.
     */
    private void deleteSupplier() {
        if (selectedSupID == -1) return;
        if (showConfirm(this, "Xóa nhà cung cấp này?")) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM Suppliers WHERE sup_id=?");
                ps.setInt(1, selectedSupID);
                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Đã xóa!");
                    loadListData();
                    clearForm();
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("foreign key"))
                    showError(this, "Không thể xóa: NCC đang cung cấp sản phẩm!");
                else showError(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    // --- UTILITY METHODS ---

    /**
     * Adds change listeners to form fields to enable the Save button.
     */
    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
            if (!isDataLoading && Session.canManageSuppliers()) {
                btnSave.setVisible(true);
                if (selectedSupID != -1) btnSave.setText("Lưu");
            }
        });
        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
        txtDescription.getDocument().addDocumentListener(docListener);

        ActionListener dateListener = e -> {
            if (!isDataLoading && Session.canManageSuppliers()) btnSave.setVisible(true);
        };
        cbDay.addActionListener(dateListener);
        cbMonth.addActionListener(dateListener);
        cbYear.addActionListener(dateListener);
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        isDataLoading = true;
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtDescription.setText("");
        modelProducts.setRowCount(0);
        isDataLoading = false;

        enableForm(false);
        selectedSupID = -1;

        if (Session.canManageSuppliers()) btnAdd.setVisible(true);
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
    }

    /**
     * Enables or disables form fields.
     *
     * @param enable True to enable, false to disable.
     */
    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPhone.setEnabled(enable);
        txtAddress.setEnabled(enable);
        txtDescription.setEnabled(enable);
        cbDay.setEnabled(enable);
        cbMonth.setEnabled(enable);
        cbYear.setEnabled(enable);
    }

    /**
     * Initializes data for date combo boxes.
     */
    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) cbMonth.addItem(String.format("%02d", i));
        for (int i = java.time.Year.now().getValue(); i >= 1990; i--) cbYear.addItem(String.valueOf(i));
    }

    /**
     * Sets the selected date in combo boxes.
     *
     * @param dateStr The date string (YYYY-MM-DD).
     * @param d       Day ComboBox.
     * @param m       Month ComboBox.
     * @param y       Year ComboBox.
     */
    private void setComboBoxDate(String dateStr, JComboBox<String> d, JComboBox<String> m, JComboBox<String> y) {
        if (dateStr != null && !dateStr.isEmpty()) {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                y.setSelectedItem(parts[0]);
                m.setSelectedItem(parts[1]);
                d.setSelectedItem(parts[2]);
            }
        }
    }

    /**
     * Selects a supplier in the list by ID.
     *
     * @param id The supplier ID.
     */
    private void selectSupplierByID(int id) {
        ListModel<ComboItem> model = listSupplier.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getValue() == id) {
                listSupplier.setSelectedIndex(i);
                listSupplier.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    /**
     * Refreshes the data in the panel.
     */
    public void refreshData() {
        loadListData();
        selectSupplierByID(selectedSupID);
    }

    @FunctionalInterface
    interface DocumentUpdateListener {
        void update(DocumentEvent e);
    }

    static class SimpleDocumentListener implements DocumentListener {
        private final DocumentUpdateListener listener;

        public SimpleDocumentListener(DocumentUpdateListener listener) {
            this.listener = listener;
        }

        public void insertUpdate(DocumentEvent e) {
            listener.update(e);
        }

        public void removeUpdate(DocumentEvent e) {
            listener.update(e);
        }

        public void changedUpdate(DocumentEvent e) {
            listener.update(e);
        }
    }
}
