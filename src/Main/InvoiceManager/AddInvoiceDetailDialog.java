package Main.InvoiceManager;

import Utils.ComboItem;
import Utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static Utils.Style.*;

/**
 * Dialog for adding a product to an invoice.
 */
public class AddInvoiceDetailDialog extends JDialog {

    // --- 1. UI VARIABLES ---
    private JComboBox<ComboItem> cbProduct;
    private JTextField txtStock;
    private JTextField txtQuantity;
    private JButton btnSearchProduct, btnAdd, btnCancel;

    // --- 2. DATA VARIABLES ---
    private boolean isConfirmed = false;
    private ComboItem selectedProduct = null;
    private int selectedQty = 0;

    private final Map<Integer, Integer> productStockMap = new HashMap<>();

    /**
     * Constructor to initialize the Add Invoice Detail Dialog.
     *
     * @param parent The parent frame.
     */
    public AddInvoiceDetailDialog(Frame parent) {
        super(parent, true);
        setTitle("Thêm Sản Phẩm");

        initUI();
        loadProductData();
        addEvents();

        if (cbProduct.getItemCount() > 0) {
            cbProduct.setSelectedIndex(0);
        }

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeaderLabel("CHỌN SẢN PHẨM"));
        mainPanel.add(Box.createVerticalStrut(20));

        cbProduct = new JComboBox<>();
        btnSearchProduct = createSmallButton("Tìm", Color.GRAY);
        mainPanel.add(createComboBoxWithLabel(cbProduct, "Sản Phẩm:", btnSearchProduct));
        mainPanel.add(Box.createVerticalStrut(15));

        txtStock = new JTextField();
        txtStock.setEditable(false);
        txtStock.setFocusable(false);
        mainPanel.add(createTextFieldWithLabel(txtStock, "Tồn Kho Hiện Tại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtQuantity = new JTextField("1");
        mainPanel.add(createTextFieldWithLabel(txtQuantity, "Số Lượng Mua:"));
        mainPanel.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Xác Nhận", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(btnAdd);
    }

    /**
     * Loads product data into the combo box.
     */
    private void loadProductData() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT pro_ID, pro_name, pro_count FROM Products WHERE pro_count > 0 ORDER BY pro_name ASC";
            ResultSet rs = con.createStatement().executeQuery(sql);

            cbProduct.removeAllItems();
            productStockMap.clear();

            while (rs.next()) {
                int id = rs.getInt("pro_ID");
                String name = rs.getString("pro_name");
                int count = rs.getInt("pro_count");

                cbProduct.addItem(new ComboItem(name, id));
                productStockMap.put(id, count);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        cbProduct.addActionListener(e -> {
            ComboItem selected = (ComboItem) cbProduct.getSelectedItem();
            if (selected != null) {
                int proID = selected.getValue();
                int stock = productStockMap.getOrDefault(proID, 0);
                txtStock.setText(String.valueOf(stock));
            }
        });

        btnSearchProduct.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            SearchProductDialog dialog = new SearchProductDialog(parent);
            dialog.setVisible(true);

            ComboItem selected = dialog.getSelectedProduct();
            if (selected != null) {
                setSelectedComboItem(cbProduct, selected.getValue());
            }
        });

        txtQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                String qtyText = txtQuantity.getText().trim();
                if (qtyText.isEmpty()) {
                    showError(this, "Vui lòng nhập số lượng!");
                    return;
                }

                int qty = Integer.parseInt(qtyText);
                if (qty <= 0) {
                    showError(this, "Số lượng phải > 0!");
                    return;
                }

                ComboItem item = (ComboItem) cbProduct.getSelectedItem();
                if (item == null) {
                    showError(this, "Chưa chọn sản phẩm!");
                    return;
                }

                int proID = item.getValue();
                int currentStock = productStockMap.getOrDefault(proID, 0);

                if (qty > currentStock) {
                    showError(this, "Kho chỉ còn " + currentStock + ", không đủ hàng!");
                    return;
                }

                this.selectedProduct = item;
                this.selectedQty = qty;
                this.isConfirmed = true;
                dispose();

            } catch (NumberFormatException ex) {
                showError(this, "Số lượng phải là số nguyên!");
            }
        });

        btnCancel.addActionListener(e -> dispose());
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
                return;
            }
        }
    }

    /**
     * Checks if the dialog was confirmed.
     *
     * @return true if confirmed, false otherwise.
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * Gets the selected product.
     *
     * @return The selected product as a ComboItem.
     */
    public ComboItem getSelectedProduct() {
        return selectedProduct;
    }

    /**
     * Gets the selected quantity.
     *
     * @return The selected quantity.
     */
    public int getSelectedQty() {
        return selectedQty;
    }
}
