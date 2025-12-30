package Main.InvoiceManager;

import Utils.ComboItem;
import Utils.DBConnection;
import Utils.Session;
import Main.CustomerManager.AddCustomerDialog;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Objects;

import static Utils.Style.*;

/**
 * Panel for managing invoices.
 * Allows creating, viewing, editing, deleting, and printing invoices.
 */
public class InvoiceManagerPanel extends JPanel {

    // --- 1. UI VARIABLES ---
    private JList<ComboItem> listInvoice;
    private JTextField txtSearch, txtID, txtDate;
    private JComboBox<ComboItem> cbCustomer, cbStaff;

    // Discount variables
    private JPanel pDiscountContainer;
    private JTextField txtDiscountCode;
    private JButton btnApplyDiscount;
    private JLabel lblDiscountAmount;

    private JLabel lblFinalTotal;

    private JButton btnAdd, btnSave, btnDelete, btnPrint;
    private JButton btnSearchCustomer, btnQuickAddCustomer;
    private JButton btnSort;
    private JTable tableDetails;
    private DefaultTableModel detailModel;
    private JButton btnAddDetail, btnEditDetail, btnDelDetail;

    // --- 2. STATE VARIABLES ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"NEW", "OLD", "PUP", "PDW"};
    private int selectedInvID = -1;
    private boolean isDataLoading = false;

    private int currentDiscountID = -1;
    private double discountValueCalculated = 0;

    /**
     * Constructor to initialize the Invoice Manager Panel.
     */
    public InvoiceManagerPanel() {
        initUI();
        loadComboBoxData();
        loadListData();
        addEvents();
        addChangeListeners();
    }

    // =================================================================================
    //                           PART 1: UI INITIALIZATION
    // =================================================================================

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // --- A. LEFT PANEL ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        btnSort = new JButton("NEW");
        btnSort.setFocusable(false);

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm", "Nhập mã HĐ, tên KH...");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listInvoice = new JList<>();
        listInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listInvoice.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listInvoice), BorderLayout.CENTER);

        // --- B. RIGHT PANEL ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(createHeaderLabel("THÔNG TIN HÓA ĐƠN"));
        contentPanel.add(Box.createVerticalStrut(20));

        txtID = new JTextField();
        txtID.setEnabled(false);
        contentPanel.add(createTextFieldWithLabel(txtID, "Mã Hóa Đơn:"));
        contentPanel.add(Box.createVerticalStrut(10));

        txtDate = new JTextField();
        txtDate.setEnabled(false);
        contentPanel.add(createTextFieldWithLabel(txtDate, "Ngày Lập Đơn:"));
        contentPanel.add(Box.createVerticalStrut(10));

        cbCustomer = new JComboBox<>();
        btnSearchCustomer = createSmallButton("Tìm", Color.GRAY);
        btnQuickAddCustomer = createSmallButton("Mới", Color.GRAY);
        contentPanel.add(createComboBoxWithLabel(cbCustomer, "Khách Hàng:", btnSearchCustomer, btnQuickAddCustomer));
        contentPanel.add(Box.createVerticalStrut(10));

        cbStaff = new JComboBox<>();
        contentPanel.add(createComboBoxWithLabel(cbStaff, "Nhân Viên Bán:"));
        contentPanel.add(Box.createVerticalStrut(10));

        // Table
        String[] columns = {"ID SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        detailModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tableDetails = new JTable(detailModel);
        tableDetails.getColumnModel().getColumn(0).setMinWidth(0);
        tableDetails.getColumnModel().getColumn(0).setMaxWidth(0);
        tableDetails.getColumnModel().getColumn(0).setWidth(0);

        btnAddDetail = createSmallButton("Thêm", Color.GRAY);
        btnEditDetail = createSmallButton("Sửa", Color.GRAY);
        btnDelDetail = createSmallButton("Xóa", Color.GRAY);
        setDetailButtonsVisible(false);

        JPanel pTable = createTableWithLabel(tableDetails, "DANH SÁCH SẢN PHẨM", btnAddDetail, btnEditDetail, btnDelDetail);
        contentPanel.add(pTable);
        contentPanel.add(Box.createVerticalStrut(15));

        // --- DISCOUNT AREA ---
        pDiscountContainer = new JPanel(new BorderLayout());
        pDiscountContainer.setBackground(Color.WHITE);
        pDiscountContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        txtDiscountCode = new JTextField();
        btnApplyDiscount = createSmallButton("Áp dụng", Color.decode("#27ae60"));
        JPanel pInput = createTextFieldWithButton(txtDiscountCode, btnApplyDiscount, "Mã giảm giá (Nếu có):");

        lblDiscountAmount = new JLabel("- 0 VND");
        lblDiscountAmount.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 13));
        lblDiscountAmount.setForeground(Color.decode("#27ae60"));
        lblDiscountAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDiscountAmount.setBorder(new EmptyBorder(5, 0, 0, 5));

        pDiscountContainer.add(pInput, BorderLayout.NORTH);
        pDiscountContainer.add(lblDiscountAmount, BorderLayout.CENTER);

        contentPanel.add(pDiscountContainer);
        contentPanel.add(Box.createVerticalStrut(5));

        // --- TOTAL AMOUNT ---
        JPanel pTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pTotal.setBackground(Color.WHITE);
        pTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblTotalTitle = new JLabel("KHÁCH CẦN TRẢ:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblFinalTotal = new JLabel("0 VND");
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFinalTotal.setForeground(Color.decode("#e74c3c"));

        pTotal.add(lblTotalTitle);
        pTotal.add(Box.createHorizontalStrut(10));
        pTotal.add(lblFinalTotal);

        contentPanel.add(pTotal);
        contentPanel.add(Box.createVerticalStrut(10));

        // Footer
        JScrollPane scrollContent = new JScrollPane(contentPanel);
        scrollContent.setBorder(null);
        scrollContent.getVerticalScrollBar().setUnitIncrement(16);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 0, 5, 0)
        ));

        btnAdd = createButton("Tạo mới", Color.decode("#3498db"));
        btnSave = createButton("Lưu", new Color(46, 204, 113));
        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        btnPrint = createButton("In Hóa Đơn", Color.decode("#9b59b6"));

        btnPrint.setVisible(false);
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        // Only SaleStaff or Manager can see Add button
        if (!Session.canCreateInvoice()) {
            btnAdd.setVisible(false);
        }

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(scrollContent, BorderLayout.CENTER);
        rightContainer.add(buttonPanel, BorderLayout.SOUTH);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightContainer, BorderLayout.CENTER);

        enableForm(false);
        pDiscountContainer.setVisible(false);
    }

    // =================================================================================
    //                           PART 2: DATA LOADING
    // =================================================================================

    /**
     * Loads data into combo boxes (Customers and Staff).
     */
    private void loadComboBoxData() {
        try (Connection con = DBConnection.getConnection()) {
            isDataLoading = true;
            cbCustomer.removeAllItems();
            cbStaff.removeAllItems();

            ResultSet rsCus = con.createStatement().executeQuery("SELECT cus_ID, cus_name FROM Customers ORDER BY cus_ID ASC");
            while (rsCus.next())
                cbCustomer.addItem(new ComboItem(rsCus.getString("cus_name"), rsCus.getInt("cus_ID")));

            ResultSet rsSta = con.createStatement().executeQuery("SELECT sta_ID, sta_name FROM Staffs");
            while (rsSta.next())
                cbStaff.addItem(new ComboItem(rsSta.getString("sta_name"), rsSta.getInt("sta_ID")));

            if (Session.isLoggedIn) {
                setSelectedComboItem(cbStaff, Session.loggedInStaffID);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    /**
     * Loads the list of invoices from the database.
     */
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Nhập mã HĐ, tên KH...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT i.inv_ID, i.inv_price, c.cus_name " +
                    "FROM Invoices i LEFT JOIN Customers c ON i.cus_ID = c.cus_ID");

            if (isSearching) {
                sql.append(" WHERE i.inv_ID LIKE ? OR c.cus_name LIKE ?");
            }

            switch (currentSortIndex) {
                case 1:
                    sql.append(" ORDER BY i.inv_ID ASC");
                    break;
                case 2:
                    sql.append(" ORDER BY i.inv_price ASC");
                    break;
                case 3:
                    sql.append(" ORDER BY i.inv_price DESC");
                    break;
                default:
                    sql.append(" ORDER BY i.inv_ID DESC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());
            if (isSearching) {
                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("inv_ID");
                String cusName = rs.getString("cus_name");
                if (cusName == null) cusName = "Khách vãng lai";
                model.addElement(new ComboItem("HĐ #" + id + " - " + cusName, id));
            }
            listInvoice.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Loads details of a specific invoice.
     *
     * @param invID The invoice ID.
     */
    public void loadDetail(int invID) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT i.*, d.dis_code, d.dis_value, d.dis_type " +
                    "FROM Invoices i LEFT JOIN Discounts d ON i.dis_ID = d.dis_ID WHERE i.inv_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, invID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedInvID = invID;
                txtID.setText("#" + selectedInvID);
                setSelectedComboItem(cbCustomer, rs.getInt("cus_ID"));
                setSelectedComboItem(cbStaff, rs.getInt("sta_ID"));
                txtDate.setText(rs.getString("inv_date"));

                int disID = rs.getInt("dis_ID");
                if (disID > 0) {
                    currentDiscountID = disID;
                    txtDiscountCode.setText(rs.getString("dis_code"));
                } else {
                    currentDiscountID = -1;
                    txtDiscountCode.setText("");
                }

                // Always show discount container (to view)
                pDiscountContainer.setVisible(true);

                // Button visibility logic
                btnAdd.setVisible(Session.canCreateInvoice());

                btnSave.setVisible(false);

                // SaleStaff can print invoices
                btnPrint.setVisible(Session.isSaleStaff());

                // Manager can edit/delete invoices
                if (Session.canEditDeleteInvoice()) {
                    enableForm(true);
                    btnSave.setText("Lưu");
                    btnDelete.setVisible(true);
                    setDetailButtonsVisible(true);
                    btnApplyDiscount.setVisible(true);
                } else {
                    enableForm(false);
                    btnDelete.setVisible(false);
                    setDetailButtonsVisible(false);
                    btnApplyDiscount.setVisible(false);
                }
            }

            detailModel.setRowCount(0);
            String sqlDetail = "SELECT p.pro_ID, p.pro_name, d.unit_price, p.pro_price, d.ind_count " +
                    "FROM Invoice_details d JOIN Products p ON d.pro_ID = p.pro_ID WHERE d.inv_ID = ?";
            PreparedStatement psDetail = con.prepareStatement(sqlDetail);
            psDetail.setInt(1, invID);
            ResultSet rsDetail = psDetail.executeQuery();

            while (rsDetail.next()) {
                double historicalPrice = rsDetail.getDouble("unit_price");
                double currentPrice = rsDetail.getDouble("pro_price");
                double finalPrice = (historicalPrice > 0) ? historicalPrice : currentPrice;

                int count = rsDetail.getInt("ind_count");
                detailModel.addRow(new Object[]{
                        rsDetail.getInt("pro_ID"),
                        rsDetail.getString("pro_name"),
                        new DecimalFormat("#,###").format(finalPrice),
                        count,
                        new DecimalFormat("#,###").format(finalPrice * count)
                });
            }
            calculateUITotal();

        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // =================================================================================
    //                           PART 3: EVENT HANDLING
    // =================================================================================

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        listInvoice.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listInvoice.getSelectedValue();
                if (selected != null) {
                    selectedInvID = selected.getValue();
                    loadDetail(selectedInvID);
                    btnSave.setText("Lưu");
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

        btnAdd.addActionListener(e -> {
            clearForm();
            enableForm(true);

            // When creating new: Show Discount Panel AND Apply Button (For Staff too)
            pDiscountContainer.setVisible(true);
            btnApplyDiscount.setVisible(true);

            setDetailButtonsVisible(true);
            if (!Session.isAdmin()) cbStaff.setEnabled(false);
            selectedInvID = -1;
            btnSave.setText("Lưu");
            btnSave.setVisible(true);
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnPrint.setVisible(false);
        });

        btnApplyDiscount.addActionListener(e -> checkAndApplyDiscount());

        btnDelDetail.addActionListener(e -> {
            int row = tableDetails.getSelectedRow();
            if (row == -1) {
                showError(this, "Chọn 1 sản phẩm để xóa!");
                return;
            }
            detailModel.removeRow(row);
            calculateUITotal();
            btnSave.setVisible(true);
        });

        btnEditDetail.addActionListener(e -> {
            int row = tableDetails.getSelectedRow();
            if (row == -1) {
                showError(this, "Chọn 1 sản phẩm để sửa!");
                return;
            }

            int proID = Integer.parseInt(detailModel.getValueAt(row, 0).toString());
            String name = tableDetails.getValueAt(row, 1).toString();
            int currentQtyUI = Integer.parseInt(tableDetails.getValueAt(row, 3).toString());
            int maxLimit = 9999;

            try (Connection con = DBConnection.getConnection()) {
                int stockInDB = getProductStock(con, proID);
                int qtySavedInDB = 0;
                if (selectedInvID != -1) {
                    String sql = "SELECT ind_count FROM Invoice_details WHERE inv_ID=? AND pro_ID=?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, selectedInvID);
                    ps.setInt(2, proID);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) qtySavedInDB = rs.getInt("ind_count");
                }
                maxLimit = stockInDB + qtySavedInDB;
            } catch (Exception ignored) {
            }

            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            EditInvoiceDetailDialog dialog = new EditInvoiceDetailDialog(parent, name, currentQtyUI, maxLimit);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int newQty = dialog.getNewQuantity();
                detailModel.setValueAt(newQty, row, 3);
                double price = parseMoney(tableDetails.getValueAt(row, 2).toString());
                DecimalFormat df = new DecimalFormat("#,###");
                detailModel.setValueAt(df.format(price * newQty), row, 4);
                calculateUITotal();
                btnSave.setVisible(true);
            }
        });

        btnAddDetail.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddInvoiceDetailDialog dialog = new AddInvoiceDetailDialog(parent);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                try (Connection con = DBConnection.getConnection()) {
                    ComboItem item = dialog.getSelectedProduct();
                    int qtyInput = dialog.getSelectedQty();
                    int proID = item.getValue();
                    int stockDB = getProductStock(con, proID);

                    int currentQtyInTable = 0;
                    int rowExist = -1;
                    for (int i = 0; i < detailModel.getRowCount(); i++) {
                        int idOnTable = Integer.parseInt(detailModel.getValueAt(i, 0).toString());
                        if (idOnTable == proID) {
                            currentQtyInTable = Integer.parseInt(detailModel.getValueAt(i, 3).toString());
                            rowExist = i;
                            break;
                        }
                    }

                    if (currentQtyInTable + qtyInput > stockDB) {
                        showError(this, "Vượt quá tồn kho (" + stockDB + ")!");
                        return;
                    }

                    double price = getProductPrice(proID);
                    DecimalFormat df = new DecimalFormat("#,###");

                    if (rowExist != -1) {
                        int newTotal = currentQtyInTable + qtyInput;
                        detailModel.setValueAt(newTotal, rowExist, 3);
                        detailModel.setValueAt(df.format(price * newTotal), rowExist, 4);
                    } else {
                        String cleanName = item.toString().split(" \\(")[0];
                        detailModel.addRow(new Object[]{proID, cleanName, df.format(price), qtyInput, df.format(price * qtyInput)});
                    }
                    calculateUITotal();
                    btnSave.setVisible(true);
                } catch (Exception ex) {
                    showError(this, "Lỗi: " + ex.getMessage());
                }
            }
        });

        btnSave.addActionListener(e -> {
            if (detailModel.getRowCount() == 0) {
                showError(this, "Chưa có sản phẩm nào!");
                return;
            }
            if (selectedInvID == -1) createNewInvoice();
            else saveChangesToDatabase();
        });

        btnSearchCustomer.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            SearchCustomerDialog dialog = new SearchCustomerDialog(parent);
            dialog.setVisible(true);
            ComboItem selected = dialog.getSelectedCustomer();
            if (selected != null) setSelectedComboItem(cbCustomer, selected.getValue());
        });

        btnQuickAddCustomer.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCustomerDialog dialog = new AddCustomerDialog(parent);
            dialog.setVisible(true);
            if (dialog.isAddedSuccess()) {
                loadComboBoxData();
                int newID = dialog.getNewCustomerID();
                if (newID != -1) {
                    setSelectedComboItem(cbCustomer, newID);
                }
                showSuccess(this, "Đã cập nhật danh sách khách hàng!");
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedInvID == -1) return;
            if (showConfirm(this, "Xóa hóa đơn #" + selectedInvID + "? Hàng sẽ được hoàn kho.")) {
                deleteInvoiceTransaction();
            }
        });

        btnPrint.addActionListener(e -> printInvoice());

        tableDetails.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableDetails.getSelectedRow();
                    if (row == -1) return;
                    try {
                        int proID = Integer.parseInt(tableDetails.getValueAt(row, 0).toString());
                        Window win = SwingUtilities.getWindowAncestor(InvoiceManagerPanel.this);
                        if (win instanceof DashBoard) {
                            ((DashBoard) win).showProductAndLoad(proID);
                        }
                    } catch (Exception ex) {
                        showError(InvoiceManagerPanel.this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    // =================================================================================
    //                           PART 4: DATABASE LOGIC & CALCULATIONS
    // =================================================================================

    /**
     * Checks and applies the discount code.
     */
    private void checkAndApplyDiscount() {
        String code = txtDiscountCode.getText().trim();
        if (code.isEmpty()) {
            currentDiscountID = -1;
            calculateUITotal();
            showSuccess(this, "Đã hủy mã giảm giá.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Discounts WHERE dis_code = ? AND date('now') BETWEEN dis_start_date AND dis_end_date";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentDiscountID = rs.getInt("dis_ID");
                calculateUITotal();
                showSuccess(this, "Áp mã " + code + " thành công!");
                btnSave.setVisible(true);
            } else {
                currentDiscountID = -1;
                calculateUITotal();
                showError(this, "Mã không tồn tại hoặc đã hết hạn!");
            }
        } catch (Exception e) {
            showError(this, "Lỗi kiểm tra mã: " + e.getMessage());
        }
    }

    /**
     * Gets the product type ID from the product ID.
     *
     * @param proID The product ID.
     * @return The product type ID.
     */
    private int getProductTypeID(int proID) {
        int typeID = -1;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_ID FROM Products WHERE pro_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, proID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                typeID = rs.getInt("type_ID");
            }
        } catch (Exception ignored) {
        }
        return typeID;
    }

    /**
     * Calculates the total amount displayed on the UI.
     */
    private void calculateUITotal() {
        double totalBill = 0;        // Total bill amount (before discount)
        double eligibleAmount = 0;   // Total amount of eligible items for discount

        // 1. Calculate total bill first
        for (int i = 0; i < detailModel.getRowCount(); i++) {
            double itemTotal = parseMoney(detailModel.getValueAt(i, 4).toString());
            totalBill += itemTotal;
        }

        discountValueCalculated = 0;

        // 2. If there is a discount code, calculate the discount amount
        if (currentDiscountID != -1) {
            try (Connection con = DBConnection.getConnection()) {
                // Get full discount info: Type, Value, Scope, Category
                String sql = "SELECT dis_type, dis_value, dis_scope, dis_category_id FROM Discounts WHERE dis_ID = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, currentDiscountID);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String type = rs.getString("dis_type");       // PERCENT or FIXED
                    double val = rs.getDouble("dis_value");
                    String scope = rs.getString("dis_scope");     // ALL or CATEGORY
                    int categoryID = rs.getInt("dis_category_id");

                    // --- PRODUCT FILTER LOGIC ---
                    if ("ALL".equals(scope)) {
                        // If applies to all -> Eligible amount is total bill
                        eligibleAmount = totalBill;
                    } else {
                        // If applies to category -> Iterate rows to filter
                        for (int i = 0; i < detailModel.getRowCount(); i++) {
                            int proID = Integer.parseInt(detailModel.getValueAt(i, 0).toString());
                            double itemTotal = parseMoney(detailModel.getValueAt(i, 4).toString());

                            // Check if this product belongs to the discounted category
                            if (getProductTypeID(proID) == categoryID) {
                                eligibleAmount += itemTotal;
                            }
                        }
                    }

                    // --- DISCOUNT CALCULATION LOGIC ---
                    if ("PERCENT".equals(type)) {
                        // Discount by % on eligible amount
                        discountValueCalculated = eligibleAmount * (val / 100.0);
                    } else {
                        // Fixed amount discount (FIXED)
                        // If fixed discount (e.g., 50k) is greater than eligible amount (e.g., 30k)
                        // Then max discount is eligible amount (30k)
                        discountValueCalculated = Math.min(val, eligibleAmount);
                    }
                }
            } catch (Exception e) {
                showError(this, "Lỗi: " + e.getMessage());
            }
        }

        // 3. Calculate final total
        double finalTotal = totalBill - discountValueCalculated;
        if (finalTotal < 0) finalTotal = 0;

        // 4. Display on UI
        DecimalFormat df = new DecimalFormat("#,###");
        if (discountValueCalculated > 0) {
            lblDiscountAmount.setText("- " + df.format(discountValueCalculated) + " VND");
            lblDiscountAmount.setForeground(Color.decode("#27ae60")); // Green
        } else {
            // If code exists but no matching products -> Discount 0
            if (currentDiscountID != -1) {
                lblDiscountAmount.setText("- 0 VND (Không có SP phù hợp)");
                lblDiscountAmount.setForeground(Color.GRAY);
            } else {
                lblDiscountAmount.setText("- 0 VND");
                lblDiscountAmount.setForeground(Color.decode("#27ae60"));
            }
        }

        lblFinalTotal.setText(df.format(finalTotal) + " VND");
    }

    /**
     * Creates a new invoice in the database.
     */
    private void createNewInvoice() {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String sqlInv = "INSERT INTO Invoices (cus_ID, sta_ID, inv_price, dis_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement psInv = con.prepareStatement(sqlInv, Statement.RETURN_GENERATED_KEYS);
            ComboItem cus = (ComboItem) cbCustomer.getSelectedItem();
            ComboItem sta = (ComboItem) cbStaff.getSelectedItem();
            double total = parseMoney(lblFinalTotal.getText().replace(" VND", ""));

            psInv.setInt(1, Objects.requireNonNull(cus).getValue());
            psInv.setInt(2, Objects.requireNonNull(sta).getValue());
            psInv.setDouble(3, total);

            if (currentDiscountID == -1) psInv.setNull(4, Types.INTEGER);
            else psInv.setInt(4, currentDiscountID);

            psInv.executeUpdate();

            ResultSet rsKeys = psInv.getGeneratedKeys();
            int newInvID = 0;
            if (rsKeys.next()) newInvID = rsKeys.getInt(1);

            String sqlDetail = "INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE Products SET pro_count = pro_count - ? WHERE pro_ID = ?";
            PreparedStatement psDetail = con.prepareStatement(sqlDetail);
            PreparedStatement psStock = con.prepareStatement(sqlStock);

            for (int i = 0; i < detailModel.getRowCount(); i++) {
                int proID = Integer.parseInt(detailModel.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(detailModel.getValueAt(i, 3).toString());
                double currentPrice = getProductPrice(proID);

                psDetail.setInt(1, newInvID);
                psDetail.setInt(2, proID);
                psDetail.setInt(3, qty);
                psDetail.setDouble(4, currentPrice);
                psDetail.executeUpdate();

                psStock.setInt(1, qty);
                psStock.setInt(2, proID);
                psStock.executeUpdate();
            }

            con.commit();
            showSuccess(this, "Tạo hóa đơn thành công! Mã: #" + newInvID);
            loadListData();
            selectInvoiceByID(newInvID);

        } catch (Exception ex) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ignored) {
            }
            showError(this, "Lỗi tạo: " + ex.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Saves changes to an existing invoice.
     */
    private void saveChangesToDatabase() {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String sqlOld = "SELECT pro_ID, ind_count FROM Invoice_details WHERE inv_ID = ?";
            PreparedStatement psOld = con.prepareStatement(sqlOld);
            psOld.setInt(1, selectedInvID);
            ResultSet rsOld = psOld.executeQuery();
            while (rsOld.next()) {
                PreparedStatement psRestock = con.prepareStatement("UPDATE Products SET pro_count = pro_count + ? WHERE pro_ID = ?");
                psRestock.setInt(1, rsOld.getInt("ind_count"));
                psRestock.setInt(2, rsOld.getInt("pro_ID"));
                psRestock.executeUpdate();
            }

            PreparedStatement psDel = con.prepareStatement("DELETE FROM Invoice_details WHERE inv_ID = ?");
            psDel.setInt(1, selectedInvID);
            psDel.executeUpdate();

            String sqlIns = "INSERT INTO Invoice_details (inv_ID, pro_ID, ind_count, unit_price) VALUES (?, ?, ?, ?)";
            String sqlDed = "UPDATE Products SET pro_count = pro_count - ? WHERE pro_ID = ?";
            PreparedStatement psIns = con.prepareStatement(sqlIns);
            PreparedStatement psDed = con.prepareStatement(sqlDed);

            for (int i = 0; i < detailModel.getRowCount(); i++) {
                int proID = Integer.parseInt(detailModel.getValueAt(i, 0).toString());
                int qty = Integer.parseInt(detailModel.getValueAt(i, 3).toString());
                double priceOnTable = parseMoney(detailModel.getValueAt(i, 2).toString());

                psIns.setInt(1, selectedInvID);
                psIns.setInt(2, proID);
                psIns.setInt(3, qty);
                psIns.setDouble(4, priceOnTable);
                psIns.executeUpdate();

                psDed.setInt(1, qty);
                psDed.setInt(2, proID);
                psDed.executeUpdate();
            }

            String sqlHead = "UPDATE Invoices SET cus_ID=?, sta_ID=?, inv_price=?, dis_ID=? WHERE inv_ID=?";
            PreparedStatement psHead = con.prepareStatement(sqlHead);
            ComboItem cus = (ComboItem) cbCustomer.getSelectedItem();
            ComboItem sta = (ComboItem) cbStaff.getSelectedItem();
            double total = parseMoney(lblFinalTotal.getText().replace(" VND", ""));

            psHead.setInt(1, Objects.requireNonNull(cus).getValue());
            psHead.setInt(2, Objects.requireNonNull(sta).getValue());
            psHead.setDouble(3, total);

            if (currentDiscountID == -1) psHead.setNull(4, Types.INTEGER);
            else psHead.setInt(4, currentDiscountID);

            psHead.setInt(5, selectedInvID);
            psHead.executeUpdate();

            con.commit();
            showSuccess(this, "Cập nhật thành công!");
            loadListData();
            selectInvoiceByID(selectedInvID);

        } catch (Exception ex) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ignored) {
            }
            showError(this, "Lỗi cập nhật: " + ex.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Deletes the selected invoice and restores stock.
     */
    private void deleteInvoiceTransaction() {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String sqlGetItems = "SELECT pro_ID, ind_count FROM Invoice_details WHERE inv_ID = ?";
            PreparedStatement psGet = con.prepareStatement(sqlGetItems);
            psGet.setInt(1, selectedInvID);
            ResultSet rs = psGet.executeQuery();
            String sqlRestock = "UPDATE Products SET pro_count = pro_count + ? WHERE pro_ID = ?";
            PreparedStatement psRestock = con.prepareStatement(sqlRestock);

            while (rs.next()) {
                psRestock.setInt(1, rs.getInt("ind_count"));
                psRestock.setInt(2, rs.getInt("pro_ID"));
                psRestock.executeUpdate();
            }

            PreparedStatement psDelDetail = con.prepareStatement("DELETE FROM Invoice_details WHERE inv_ID = ?");
            psDelDetail.setInt(1, selectedInvID);
            psDelDetail.executeUpdate();

            PreparedStatement psDelHead = con.prepareStatement("DELETE FROM Invoices WHERE inv_ID = ?");
            psDelHead.setInt(1, selectedInvID);
            psDelHead.executeUpdate();

            con.commit();
            showSuccess(this, "Đã xóa và hoàn kho!");
            loadListData();
            clearForm();
        } catch (Exception ex) {
            try {
                if (con != null) con.rollback();
            } catch (Exception ignored) {
            }
            showError(this, "Lỗi xóa: " + ex.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Prints the selected invoice.
     */
    private void printInvoice() {
        if (selectedInvID == -1) return;
        try {
            StringBuilder bill = new StringBuilder();
            bill.append("<html><head><style>body { font-family: Monospaced; font-size: 12px; } h2 { text-align: center; } .line { border-bottom: 1px dashed black; } .right { text-align: right; } table { width: 100%; border-collapse: collapse; } td, th { padding: 5px; text-align: left; } .num { text-align: right; } </style></head><body>");
            bill.append("<h2>HÓA ĐƠN BÁN HÀNG</h2>");
            bill.append("<p style='text-align:center'>Mã số: #").append(selectedInvID).append("</p>");
            bill.append("<p style='text-align:center'>Ngày: ").append(txtDate.getText()).append("</p>");
            bill.append("<div class='line'></div>");
            bill.append("<p><b>Khách hàng:</b> ").append(cbCustomer.getSelectedItem()).append("</p>");
            bill.append("<p><b>Nhân viên:</b> ").append(cbStaff.getSelectedItem()).append("</p>");
            bill.append("<br><table><tr><th>Sản phẩm</th><th class='num'>SL</th><th class='num'>Đ.Giá</th><th class='num'>T.Tiền</th></tr>");

            double subTotal = 0;
            for (int i = 0; i < detailModel.getRowCount(); i++) {
                double rowPrice = parseMoney(detailModel.getValueAt(i, 4).toString());
                subTotal += rowPrice;
                bill.append("<tr>");
                bill.append("<td>").append(detailModel.getValueAt(i, 1)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 3)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 2)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 4)).append("</td>");
                bill.append("</tr>");
            }
            bill.append("</table><div class='line'></div>");

            DecimalFormat df = new DecimalFormat("#,###");
            bill.append("<p class='right'>Tạm tính: ").append(df.format(subTotal)).append(" VND</p>");
            if (discountValueCalculated > 0) {
                bill.append("<p class='right'>Giảm giá: -").append(df.format(discountValueCalculated)).append(" VND</p>");
            }
            bill.append("<h3 class='right'>TỔNG CỘNG: ").append(lblFinalTotal.getText()).append("</h3>");
            bill.append("<br><p style='text-align:center; font-style:italic;'>Cảm ơn quý khách và hẹn gặp lại!</p></body></html>");

            JTextPane printingComponent = new JTextPane();
            printingComponent.setContentType("text/html");
            printingComponent.setText(bill.toString());
            if (printingComponent.print(null, null, true, null, null, true)) {
                showSuccess(this, "Đã gửi lệnh in thành công!");
            }
        } catch (Exception ex) {
            showError(this, "Lỗi in ấn: " + ex.getMessage());
        }
    }

    // =================================================================================
    //                           PART 5: UTILITY METHODS
    // =================================================================================

    /**
     * Parses a money string to double.
     *
     * @param text The money string.
     * @return The double value.
     */
    private double parseMoney(String text) {
        try {
            return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).parse(text).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets the price of a product.
     *
     * @param proID The product ID.
     * @return The product price.
     */
    private double getProductPrice(int proID) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT pro_price FROM Products WHERE pro_ID = ?");
            ps.setInt(1, proID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * Gets the stock quantity of a product.
     *
     * @param con   The database connection.
     * @param proID The product ID.
     * @return The stock quantity.
     * @throws SQLException If a database error occurs.
     */
    private int getProductStock(Connection con, int proID) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT pro_count FROM Products WHERE pro_ID = ?");
        ps.setInt(1, proID);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt("pro_count") : 0;
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
     * Enables or disables form fields.
     *
     * @param enable True to enable, false to disable.
     */
    private void enableForm(boolean enable) {
        cbCustomer.setEnabled(enable);
        cbStaff.setEnabled(enable);
        btnSearchCustomer.setVisible(enable);
        btnQuickAddCustomer.setVisible(enable);
        setDetailButtonsVisible(enable);

        // Always allow input/click if form is enabled (Create mode or Admin edit)
        txtDiscountCode.setEnabled(enable);
        btnApplyDiscount.setEnabled(enable);
    }

    /**
     * Sets the visibility of detail buttons (Add, Edit, Delete).
     *
     * @param visible True to show, false to hide.
     */
    private void setDetailButtonsVisible(boolean visible) {
        btnAddDetail.setVisible(visible);
        btnEditDetail.setVisible(visible);
        btnDelDetail.setVisible(visible);
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        isDataLoading = true;
        txtID.setText("[Tự động]");
        txtDate.setText("[Tự động]");

        // Reset Discount
        txtDiscountCode.setText("");
        currentDiscountID = -1;
        lblDiscountAmount.setText("- 0 VND");
        pDiscountContainer.setVisible(false);

        // --- IMPORTANT: When creating new, always show Apply button ---
        btnApplyDiscount.setVisible(true);

        setSelectedComboItem(cbCustomer, 1);
        if (cbStaff.getItemCount() > 0) {
            if (Session.isLoggedIn) {
                setSelectedComboItem(cbStaff, Session.loggedInStaffID);
            } else cbStaff.setSelectedIndex(0);
        }
        detailModel.setRowCount(0);
        calculateUITotal();
        selectedInvID = -1;
        listInvoice.clearSelection();
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        btnPrint.setVisible(false);
        setDetailButtonsVisible(false);
        enableForm(false);
        isDataLoading = false;

        // Button visibility logic
        btnAdd.setVisible(Session.canCreateInvoice());
    }

    /**
     * Selects an invoice in the list by ID.
     *
     * @param id The invoice ID.
     */
    private void selectInvoiceByID(int id) {
        for (int i = 0; i < listInvoice.getModel().getSize(); i++) {
            if (listInvoice.getModel().getElementAt(i).getValue() == id) {
                listInvoice.setSelectedIndex(i);
                listInvoice.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    /**
     * Refreshes the data in the panel.
     */
    public void refreshData() {
        loadComboBoxData();
        loadListData();
        selectInvoiceByID(selectedInvID);
    }

    /**
     * Adds change listeners to form fields.
     */
    private void addChangeListeners() {
        cbCustomer.addActionListener(e -> checkChange());
        cbStaff.addActionListener(e -> checkChange());
    }

    /**
     * Checks for changes and enables the Save button.
     */
    private void checkChange() {
        if (!isDataLoading) btnSave.setVisible(true);
    }
}
