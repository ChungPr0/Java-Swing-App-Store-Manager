package Main.ProductManager;

import Utils.ComboItem;
import Utils.DBConnection;
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

import static Utils.Style.*;

/**
 * Dialog for adding a new product.
 */
public class AddProductDialog extends JDialog {

    // --- 1. UI VARIABLES ---
    private JTextField txtName, txtPrice, txtCount;
    private JTextArea txtDescription; // Added description field
    private JComboBox<ComboItem> cbType, cbSupplier;
    private JButton btnAddType, btnAddSupplier;
    private JButton btnSave, btnCancel;

    // --- 2. STATE VARIABLES ---
    private boolean isAdded = false;
    private int newProductID = -1; // Variable to store the ID of the newly added product

    /**
     * Constructor to initialize the Add Product Dialog.
     *
     * @param parent The parent frame.
     */
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

    // --- 3. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
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

        // Category
        cbType = new JComboBox<>();
        btnAddType = createSmallButton("Mới", Color.GRAY);
        mainPanel.add(createComboBoxWithLabel(cbType, "Phân Loại:", btnAddType, null));
        mainPanel.add(Box.createVerticalStrut(15));

        // Supplier
        cbSupplier = new JComboBox<>();
        btnAddSupplier = createSmallButton("Mới", Color.GRAY);
        mainPanel.add(createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:", btnAddSupplier, null));
        mainPanel.add(Box.createVerticalStrut(15));

        // Product Description
        txtDescription = new JTextArea(4, 20);
        JPanel pDesc = createTextAreaWithLabel(txtDescription, "Mô tả / Ghi chú:");
        mainPanel.add(pDesc);
        mainPanel.add(Box.createVerticalStrut(16));

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

    // --- 4. DATA LOADING ---

    /**
     * Loads product types into the combo box.
     */
    private void loadTypeData() {
        cbType.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_id, type_name FROM ProductTypes ORDER BY type_name ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbType.addItem(new ComboItem(rs.getString("type_name"), rs.getInt("type_id")));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads suppliers into the combo box.
     */
    private void loadSupplierData() {
        cbSupplier.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_id, sup_name FROM Suppliers ORDER BY sup_name ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbSupplier.addItem(new ComboItem(rs.getString("sup_name"), rs.getInt("sup_id")));
            }
        } catch (Exception ignored) {
        }
    }

    // --- 5. EVENT HANDLING ---

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {

        // Add Product Type button
        btnAddType.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            TypeEditorDialog dialog = new TypeEditorDialog(parent);
            dialog.setVisible(true);

            if (dialog.isUpdated()) {
                loadTypeData();
                selectNewestItem(cbType);
            }
        });

        // Add Supplier button
        btnAddSupplier.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            AddSupplierDialog dialog = new AddSupplierDialog(parent);
            dialog.setVisible(true);

            if (dialog.isAddedSuccess()) {
                loadSupplierData();
                selectNewestItem(cbSupplier);
            }
        });

        // Save button
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

                String sql = "INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID, pro_description) VALUES (?, ?, ?, ?, ?, ?)";

                // Add RETURN_GENERATED_KEYS parameter to get the ID
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, txtName.getText().trim());
                ps.setDouble(2, Double.parseDouble(txtPrice.getText().trim()));
                ps.setInt(3, Integer.parseInt(txtCount.getText().trim()));
                ps.setInt(4, selectedType.getValue());
                ps.setInt(5, selectedSup.getValue());
                ps.setString(6, txtDescription.getText().trim()); // Add description

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Get the newly generated ID from the Database
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

    // --- HELPER METHODS ---

    /**
     * Selects the newest item in a combo box.
     *
     * @param cb The combo box.
     */
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

    /**
     * Checks if the product was added successfully.
     *
     * @return true if added, false otherwise.
     */
    public boolean isAddedSuccess() {
        return isAdded;
    }

    /**
     * Gets the ID of the newly added product.
     *
     * @return The new product ID.
     */
    public int getNewProductID() {
        return newProductID;
    }
}
