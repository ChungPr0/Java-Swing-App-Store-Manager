package InvoiceForm;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static JDBCUtils.Style.*;

public class AddInvoiceDetailDialog extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN ---
    private JComboBox<ComboItem> cbProduct;
    private JTextField txtStock; // Ô hiển thị tồn kho (Mới)
    private JTextField txtQuantity;
    private JButton btnAdd, btnCancel;

    // --- 2. BIẾN DỮ LIỆU ---
    private boolean isConfirmed = false;
    private ComboItem selectedProduct = null;
    private int selectedQty = 0;

    // Map để lưu trữ cặp: [ID Sản Phẩm] -> [Số Lượng Tồn]
    // Giúp tra cứu nhanh mà không cần gọi lại DB hay cắt chuỗi
    private Map<Integer, Integer> productStockMap = new HashMap<>();

    public AddInvoiceDetailDialog(Frame parent) {
        super(parent, true);
        setTitle("Thêm Sản Phẩm");

        initUI();
        loadProductData(); // Load xong dữ liệu
        addEvents();       // Mới gán sự kiện (để sự kiện chọn item chạy đúng)

        // Trigger chọn dòng đầu tiên để fill tồn kho ngay khi mở
        if (cbProduct.getItemCount() > 0) {
            cbProduct.setSelectedIndex(0);
        }

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeaderLabel("CHỌN SẢN PHẨM"));
        mainPanel.add(Box.createVerticalStrut(20));

        // 1. ComboBox Sản phẩm
        cbProduct = new JComboBox<>();
        mainPanel.add(createComboBoxWithLabel(cbProduct, "Sản Phẩm:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // 2. Ô Tồn kho - Read Only
        txtStock = new JTextField();
        txtStock.setEditable(false); // Không cho sửa
        txtStock.setFocusable(false); // Không cho focus vào
        mainPanel.add(createTextFieldWithLabel(txtStock, "Tồn Kho Hiện Tại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // 3. Ô Số lượng nhập
        txtQuantity = new JTextField("1");
        mainPanel.add(createTextFieldWithLabel(txtQuantity, "Số Lượng Mua:"));
        mainPanel.add(Box.createVerticalStrut(25));

        // 4. Buttons
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

    private void loadProductData() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT pro_ID, pro_name, pro_count FROM Products WHERE pro_count > 0";
            ResultSet rs = con.createStatement().executeQuery(sql);

            cbProduct.removeAllItems();
            productStockMap.clear();

            while (rs.next()) {
                int id = rs.getInt("pro_ID");
                String name = rs.getString("pro_name");
                int count = rs.getInt("pro_count");

                // 1. Thêm vào ComboBox (Chỉ hiện tên cho đẹp)
                cbProduct.addItem(new ComboItem(name, id));

                // 2. Lưu số lượng tồn vào Map (Bộ nhớ đệm)
                productStockMap.put(id, count);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        // --- SỰ KIỆN 1: KHI CHỌN SẢN PHẨM -> FILL TỒN KHO ---
        cbProduct.addActionListener(_ -> {
            ComboItem selected = (ComboItem) cbProduct.getSelectedItem();
            if (selected != null) {
                int proID = selected.getValue();
                // Lấy tồn kho từ Map ra (Nhanh và chính xác tuyệt đối)
                if (productStockMap.containsKey(proID)) {
                    int stock = productStockMap.get(proID);
                    txtStock.setText(String.valueOf(stock));
                } else {
                    txtStock.setText("0");
                }
            }
        });

        // --- SỰ KIỆN 2: CHẶN NHẬP CHỮ VÀO Ô SỐ LƯỢNG ---
        txtQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // --- SỰ KIỆN 3: NÚT XÁC NHẬN ---
        btnAdd.addActionListener(_ -> {
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

        btnCancel.addActionListener(_ -> dispose());
    }

    public boolean isConfirmed() { return isConfirmed; }
    public ComboItem getSelectedProduct() { return selectedProduct; }
    public int getSelectedQty() { return selectedQty; }
}