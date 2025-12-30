package Main.ProductManager;

import Utils.ComboItem;
import Utils.DBConnection;
import Utils.Session;
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
import java.sql.Statement;

import static Utils.Style.*;

/**
 * Panel for managing products.
 * Allows adding, editing, deleting, and searching for products.
 */
public class ProductManagerPanel extends JPanel {
    // --- 1. UI VARIABLES ---
    private JList<ComboItem> listProduct;
    private JTextField txtSearch, txtName, txtPrice, txtCount;
    private JTextArea txtDescription;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnEditType, btnAddType;
    private JButton btnAddSupplier;
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnSort;

    // --- 2. STATE VARIABLES ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "PUP", "PDW", "NEW", "OLD"};
    private int selectedProductID = -1; // -1: Create mode
    private boolean isDataLoading = false;

    /**
     * Constructor to initialize the Product Manager Panel.
     */
    public ProductManagerPanel() {
        initUI();
        loadListData();
        loadTypeData();
        loadSupplierData();
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

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm", "Nhập tên sản phẩm...");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listProduct = new JList<>();
        listProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listProduct.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listProduct), BorderLayout.CENTER);

        // B. RIGHT PANEL (FORM + FOOTER)

        // B1. Form Panel (Scrollable)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        formPanel.add(createHeaderLabel("THÔNG TIN SẢN PHẨM"));
        formPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtName, "Tên Sản Phẩm:"));
        formPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtPrice, "Giá Bán (VND):"));
        formPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        formPanel.add(createTextFieldWithLabel(txtCount, "Số Lượng Tồn:"));
        formPanel.add(Box.createVerticalStrut(15));

        cbType = new JComboBox<>();
        btnEditType = createSmallButton("Sửa", Color.GRAY);
        btnAddType = createSmallButton("Mới", Color.GRAY);
        formPanel.add(createComboBoxWithLabel(cbType, "Phân Loại:", btnEditType, btnAddType));
        formPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        btnAddSupplier = createSmallButton("Mới", Color.GRAY);
        formPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:", btnAddSupplier, null));
        formPanel.add(Box.createVerticalStrut(15));

        txtDescription = new JTextArea(4, 20);
        JPanel pDesc = createTextAreaWithLabel(txtDescription, "Mô tả / Ghi chú:");
        formPanel.add(pDesc);

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
        if (!Session.canManageProducts()) {
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
     * Loads the list of products from the database.
     */
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Nhập tên sản phẩm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT pro_id, pro_name FROM Products");
            if (isSearching) sql.append(" WHERE pro_name LIKE ?");

            switch (currentSortIndex) {
                case 1:
                    sql.append(" ORDER BY pro_name DESC");
                    break;
                case 2:
                    sql.append(" ORDER BY pro_price ASC");
                    break;
                case 3:
                    sql.append(" ORDER BY pro_price DESC");
                    break;
                case 4:
                    sql.append(" ORDER BY pro_id DESC");
                    break;
                case 5:
                    sql.append(" ORDER BY pro_id ASC");
                    break;
                default:
                    sql.append(" ORDER BY pro_name ASC");
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

    /**
     * Loads product types into the combo box.
     */
    private void loadTypeData() {
        isDataLoading = true;
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT type_id, type_name FROM ProductTypes");
            while (rs.next()) cbType.addItem(new ComboItem(rs.getString("type_name"), rs.getInt("type_id")));
        } catch (Exception ignored) {
        } finally {
            isDataLoading = false;
        }
    }

    /**
     * Loads suppliers into the combo box.
     */
    private void loadSupplierData() {
        isDataLoading = true;
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT sup_id, sup_name FROM Suppliers");
            while (rs.next()) cbSupplier.addItem(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
        } catch (Exception ignored) {
        } finally {
            isDataLoading = false;
        }
    }

    /**
     * Loads details of a specific product.
     *
     * @param id The product ID.
     */
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
                txtDescription.setText(rs.getString("pro_description"));

                setSelectedComboItem(cbType, rs.getInt("type_ID"));
                setSelectedComboItem(cbSupplier, rs.getInt("sup_ID"));

                // Button visibility logic
                if (Session.canManageProducts()) {
                    enableForm(true);
                    btnAdd.setVisible(true);
                    btnDelete.setVisible(true);
                    btnSave.setText("Lưu");
                    btnSave.setVisible(false); // Wait for edit
                } else {
                    enableForm(false);
                    btnAdd.setVisible(false);
                    btnDelete.setVisible(false);
                    btnSave.setVisible(false);
                }
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

        btnSort.addActionListener(e -> {
            currentSortIndex = (currentSortIndex + 1) % sortModes.length;
            btnSort.setText(sortModes[currentSortIndex]);
            loadListData();
        });

        // --- ADD BUTTON ---
        btnAdd.addActionListener(e -> prepareCreate());

        // --- SAVE BUTTON (Insert/Update) ---
        btnSave.addActionListener(e -> saveProduct());

        // --- DELETE BUTTON ---
        btnDelete.addActionListener(e -> deleteProduct());

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
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent, currentItem.getValue(), currentItem.toString());
            dialog.setVisible(true);
            if (dialog.isUpdated()) {
                loadTypeData();
                setSelectedComboItem(cbType, currentItem.getValue());
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
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        };
        txtPrice.addKeyListener(numberFilter);
        txtCount.addKeyListener(numberFilter);
    }

    // --- MAIN LOGIC METHODS ---

    /**
     * Prepares the form for creating a new product.
     */
    private void prepareCreate() {
        listProduct.clearSelection();
        selectedProductID = -1; // Mark as Create Mode

        isDataLoading = true;
        txtName.setText("");
        txtPrice.setText("");
        txtCount.setText("");
        txtDescription.setText("");
        if (cbType.getItemCount() > 0) cbType.setSelectedIndex(0);
        if (cbSupplier.getItemCount() > 0) cbSupplier.setSelectedIndex(0);
        isDataLoading = false;

        enableForm(true);
        txtName.requestFocus();

        btnAdd.setVisible(false);      // Hide Add button
        btnDelete.setVisible(false);   // Hide Delete button
        btnSave.setText("Lưu");
        btnSave.setVisible(true);      // Show Save button
    }

    /**
     * Saves the product (Insert or Update).
     */
    private void saveProduct() {
        if (txtName.getText().trim().isEmpty()) {
            showError(this, "Tên không được để trống!");
            return;
        }
        if (txtPrice.getText().trim().isEmpty()) {
            showError(this, "Giá không được để trống!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            ComboItem selectedType = (ComboItem) cbType.getSelectedItem();
            ComboItem selectedSup = (ComboItem) cbSupplier.getSelectedItem();
            if (selectedType == null || selectedSup == null) {
                showError(this, "Vui lòng chọn Loại và NCC!");
                return;
            }

            String name = txtName.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int count = txtCount.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtCount.getText().trim());
            String desc = txtDescription.getText().trim();

            if (selectedProductID == -1) {
                // INSERT
                String sql = "INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID, pro_description) VALUES (?,?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, count);
                ps.setInt(4, selectedType.getValue());
                ps.setInt(5, selectedSup.getValue());
                ps.setString(6, desc);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    showSuccess(this, "Thêm sản phẩm thành công!");
                    loadListData();
                    selectProductByID(rs.getInt(1)); // Select newly added product
                }
            } else {
                // UPDATE
                String sql = "UPDATE Products SET pro_name=?, pro_price=?, pro_count=?, type_ID=?, sup_ID=?, pro_description=? WHERE pro_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, count);
                ps.setInt(4, selectedType.getValue());
                ps.setInt(5, selectedSup.getValue());
                ps.setString(6, desc);
                ps.setInt(7, selectedProductID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();
                    selectProductByID(selectedProductID);
                }
            }
        } catch (Exception ex) {
            showError(this, "Lỗi: " + ex.getMessage());
        }
    }

    /**
     * Deletes the selected product.
     */
    private void deleteProduct() {
        if (selectedProductID == -1) return;
        if (showConfirm(this, "Bạn chắc chắn muốn xóa?")) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE pro_id=?");
                ps.setInt(1, selectedProductID);
                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Đã xóa!");
                    loadListData();
                    clearForm();
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("foreign key"))
                    showError(this, "Sản phẩm đã có trong hóa đơn, không thể xóa!");
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
            if (!isDataLoading && Session.canManageProducts()) {
                btnSave.setVisible(true);
                if (selectedProductID != -1) btnSave.setText("Lưu");
            }
        });
        txtName.getDocument().addDocumentListener(docListener);
        txtPrice.getDocument().addDocumentListener(docListener);
        txtCount.getDocument().addDocumentListener(docListener);
        txtDescription.getDocument().addDocumentListener(docListener);
        cbType.addActionListener(e -> checkChange());
        cbSupplier.addActionListener(e -> checkChange());
    }

    /**
     * Checks for changes and enables the Save button.
     */
    private void checkChange() {
        if (!isDataLoading && Session.canManageProducts()) btnSave.setVisible(true);
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        isDataLoading = true;
        txtName.setText("");
        txtPrice.setText("");
        txtCount.setText("");
        txtDescription.setText("");
        isDataLoading = false;

        enableForm(false);
        selectedProductID = -1;

        // Waiting state
        if (Session.canManageProducts()) btnAdd.setVisible(true);
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
    }

    /**
     * Enables or disables form fields.
     *
     * @param enable True to enable, false to disable.
     */
    private void enableForm(boolean enable) {
        boolean canManageTypes = Session.canManageTypes();
        boolean canManageSuppliers = Session.canManageSuppliers();

        txtName.setEnabled(enable);
        txtPrice.setEnabled(enable);
        txtCount.setEnabled(enable);
        txtDescription.setEnabled(enable);
        cbType.setEnabled(enable);
        cbSupplier.setEnabled(enable);

        // Edit/Add Type buttons only visible if user has permission (Manager)
        btnEditType.setVisible(enable && canManageTypes);
        btnAddType.setVisible(enable && canManageTypes);

        // Add Supplier button only visible if user has permission (StorageStaff/Manager)
        btnAddSupplier.setVisible(enable && canManageSuppliers);
    }

    /**
     * Selects a product in the list by ID.
     *
     * @param id The product ID.
     */
    private void selectProductByID(int id) {
        ListModel<ComboItem> model = listProduct.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getValue() == id) {
                listProduct.setSelectedIndex(i);
                listProduct.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    /**
     * Sets the selected item in a combo box by ID.
     *
     * @param cb The combo box.
     * @param id The ID to select.
     */
    private void setSelectedComboItem(JComboBox<ComboItem> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getValue() == id) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Selects the newest item in a combo box.
     *
     * @param cb The combo box.
     */
    private void selectNewestItem(JComboBox<ComboItem> cb) {
        if (cb.getItemCount() > 0) cb.setSelectedIndex(cb.getItemCount() - 1);
    }

    /**
     * Refreshes the data in the panel.
     */
    public void refreshData() {
        loadTypeData();
        loadSupplierData();
        loadListData();
        selectProductByID(selectedProductID);
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
