package Main.InvoiceManager;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;
import Main.CustomerManager.AddCustomerDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Objects;

import static JDBCUtils.Style.*;

/**
 * Panel quản lý Hóa đơn (Bán hàng).
 */
public class InvoiceManagerPanel extends JPanel {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN ---
    private JList<ComboItem> listInvoice;
    private JTextField txtSearch, txtTotalMoney, txtID, txtDate;
    private JComboBox<ComboItem> cbCustomer, cbStaff;

    // Các nút chức năng chính
    private JButton btnAdd, btnSave, btnDelete, btnPrint;
    private JButton btnQuickAddCustomer; // Nút thêm nhanh khách hàng
    private JButton btnSort;

    // Bảng chi tiết sản phẩm
    private JTable tableDetails;
    private DefaultTableModel detailModel;
    private JButton btnAddDetail, btnEditDetail, btnDelDetail;

    // --- 2. BIẾN TRẠNG THÁI ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"NEW", "OLD", "PUP", "PDW"};
    private int selectedInvID = -1;        // ID hóa đơn đang chọn
    private boolean isDataLoading = false; // Cờ chặn sự kiện

    public InvoiceManagerPanel() {
        initUI();
        loadComboBoxData();
        loadListData();
        addEvents();
        addChangeListeners();
    }

    // =================================================================================
    //                           PHẦN 1: KHỞI TẠO GIAO DIỆN
    // =================================================================================
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // --- A. PANEL TRÁI (DANH SÁCH HÓA ĐƠN) ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        btnSort = new JButton("NEW");
        btnSort.setToolTipText("Đang xếp: Mới nhất");
        btnSort.setFocusable(false);

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listInvoice = new JList<>();
        listInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listInvoice.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listInvoice), BorderLayout.CENTER);

        // --- B. PANEL PHẢI (CHI TIẾT HÓA ĐƠN) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN HÓA ĐƠN"));
        rightPanel.add(Box.createVerticalStrut(20));

        // Các trường thông tin chung (Header)
        txtID = new JTextField();
        txtID.setEnabled(false); // Disable nhưng nền vẫn trắng nhờ UIManager
        txtID.setFocusable(false);
        rightPanel.add(createTextFieldWithLabel(txtID, "Mã Hóa Đơn: "));
        rightPanel.add(Box.createVerticalStrut(10));

        txtDate = new JTextField();
        txtDate.setEnabled(false);
        txtDate.setFocusable(false);
        rightPanel.add(createTextFieldWithLabel(txtDate, "Ngày Lập Đơn: "));
        rightPanel.add(Box.createVerticalStrut(10));

        cbCustomer = new JComboBox<>();
        btnQuickAddCustomer = createSmallButton("Mới", Color.GRAY);
        rightPanel.add(createComboBoxWithLabel(cbCustomer, "Khách Hàng:", btnQuickAddCustomer));
        rightPanel.add(Box.createVerticalStrut(10));

        cbStaff = new JComboBox<>();
        rightPanel.add(createComboBoxWithLabel(cbStaff, "Nhân Viên Bán:"));
        rightPanel.add(Box.createVerticalStrut(10));

        txtTotalMoney = new JTextField();
        txtTotalMoney.setEnabled(false);
        txtTotalMoney.setFocusable(false);
        rightPanel.add(createTextFieldWithLabel(txtTotalMoney, "Tổng Tiền (VND):"));
        rightPanel.add(Box.createVerticalStrut(15)); // Tăng khoảng cách chút

        // --- C. BẢNG CHI TIẾT SẢN PHẨM (DETAIL TABLE) ---

        // 1. Tạo các nút chức năng nhỏ cho bảng
        btnAddDetail = createSmallButton("Thêm", Color.GRAY);
        btnEditDetail = createSmallButton("Sửa", Color.GRAY);
        btnDelDetail = createSmallButton("Xóa", Color.GRAY);
        setDetailButtonsVisible(false); // Mặc định ẩn

        // 2. Cấu hình Model và Bảng
        String[] columns = {"ID SP", "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        detailModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableDetails = new JTable(detailModel);

        // 3. Ẩn cột ID SP (Cột 0)
        tableDetails.getColumnModel().getColumn(0).setMinWidth(0);
        tableDetails.getColumnModel().getColumn(0).setMaxWidth(0);
        tableDetails.getColumnModel().getColumn(0).setWidth(0);

        // 4. Sử dụng hàm createTableWithLabel
        // Hàm này sẽ tự bọc ScrollPane, tạo Header viền xám và gắn các nút vào
        JPanel pTable = createTableWithLabel(
                tableDetails,
                "DANH SÁCH SẢN PHẨM TRONG HÓA ĐƠN",
                btnAddDetail, btnEditDetail, btnDelDetail
        );

        rightPanel.add(pTable);
        rightPanel.add(Box.createVerticalStrut(15));

        // --- D. KHU VỰC NÚT CHỨC NĂNG CHÍNH ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Tạo mới", Color.decode("#3498db"));
        btnSave = createButton("Lưu thay đổi", new Color(46, 204, 113));
        btnDelete = createButton("Xóa Hóa Đơn", new Color(231, 76, 60));
        btnPrint = createButton("In Hóa Đơn", Color.decode("#9b59b6"));

        btnPrint.setVisible(false);
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        rightPanel.add(buttonPanel);

        // Tạo thanh cuộn cho panel phải
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setBorder(null);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightScrollPane, BorderLayout.CENTER);

        enableForm(false);
    }

    // =================================================================================
    //                           PHẦN 2: TẢI DỮ LIỆU
    // =================================================================================

    private void loadComboBoxData() {
        try (Connection con = DBConnection.getConnection()) {
            isDataLoading = true;

            cbCustomer.removeAllItems();
            cbStaff.removeAllItems();

            ResultSet rsCus = con.createStatement().executeQuery("SELECT cus_ID, cus_name FROM Customers");
            while (rsCus.next()) cbCustomer.addItem(new ComboItem(rsCus.getString("cus_name"), rsCus.getInt("cus_ID")));

            ResultSet rsSta = con.createStatement().executeQuery("SELECT sta_ID, sta_name FROM Staffs");
            while (rsSta.next()) cbStaff.addItem(new ComboItem(rsSta.getString("sta_name"), rsSta.getInt("sta_ID")));

            // Auto select logged-in staff
            if (JDBCUtils.Session.isLoggedIn) {
                int myID = JDBCUtils.Session.loggedInStaffID;
                setSelectedComboItem(cbStaff, myID);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();

        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT i.inv_ID, i.inv_price, c.cus_name " +
                    "FROM Invoices i LEFT JOIN Customers c ON i.cus_ID = c.cus_ID");

            if (isSearching) {
                sql.append(" WHERE i.inv_ID LIKE ? OR c.cus_name LIKE ?");
            }

            switch (currentSortIndex) {
                case 1: sql.append(" ORDER BY i.inv_ID ASC"); break;
                case 2: sql.append(" ORDER BY i.inv_price ASC"); break;
                case 3: sql.append(" ORDER BY i.inv_price DESC"); break;
                default: sql.append(" ORDER BY i.inv_ID DESC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());

            if (isSearching) {
                String searchPattern = "%" + keyword + "%";
                ps.setString(1, searchPattern);
                ps.setString(2, searchPattern);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("inv_ID");
                String cusName = rs.getString("cus_name");
                if (cusName == null) cusName = "Khách vãng lai";

                String displayText = "HĐ #" + id + " - " + cusName;
                model.addElement(new ComboItem(displayText, id));
            }
            listInvoice.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    public void loadDetail(int invID) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            DecimalFormat df = new DecimalFormat("#,###");
            String sql = "SELECT * FROM Invoices WHERE inv_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, invID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedInvID = invID;
                txtID.setText("#" + selectedInvID);
                setSelectedComboItem(cbCustomer, rs.getInt("cus_ID"));
                setSelectedComboItem(cbStaff, rs.getInt("sta_ID"));
                java.sql.Timestamp ts = rs.getTimestamp("inv_date");
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                txtDate.setText(ts != null ? sdf.format(ts) : "");

                btnAdd.setVisible(true);
                btnSave.setVisible(false);
                btnPrint.setVisible(true);

                if (JDBCUtils.Session.isAdmin()) {
                    enableForm(true);
                    btnSave.setText("Lưu thay đổi");
                    btnDelete.setVisible(true);
                    setDetailButtonsVisible(true);
                } else {
                    enableForm(false);
                    btnDelete.setVisible(false);
                    setDetailButtonsVisible(false);
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
                        df.format(finalPrice),
                        count,
                        df.format(finalPrice * count)
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
    //                           PHẦN 3: XỬ LÝ SỰ KIỆN
    // =================================================================================
    private void addEvents() {
        listInvoice.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listInvoice.getSelectedValue();
                if (selected != null) {
                    selectedInvID = selected.getValue();
                    loadDetail(selectedInvID);
                    btnSave.setText("Lưu thay đổi");
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

        btnAdd.addActionListener(e -> {
            clearForm();
            enableForm(true);
            setDetailButtonsVisible(true);
            if (!JDBCUtils.Session.isAdmin()) cbStaff.setEnabled(false);
            selectedInvID = -1;
            btnSave.setText("Lưu hóa đơn");
            btnSave.setVisible(true);
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnPrint.setVisible(false);
        });

        btnDelDetail.addActionListener(e -> {
            int row = tableDetails.getSelectedRow();
            if (row == -1) { showError(this, "Chọn 1 sản phẩm để xóa!"); return; }
            detailModel.removeRow(row);
            calculateUITotal();
            btnSave.setVisible(true);
        });

        btnEditDetail.addActionListener(e -> {
            int row = tableDetails.getSelectedRow();
            if (row == -1) { showError(this, "Chọn 1 sản phẩm để sửa!"); return; }

            int proID = Integer.parseInt(detailModel.getValueAt(row, 0).toString());
            String name = tableDetails.getValueAt(row, 1).toString();
            int currentQtyUI = Integer.parseInt(tableDetails.getValueAt(row, 3).toString());
            int maxLimit = 0;

            try (Connection con = DBConnection.getConnection()) {
                int stockInDB = getProductStock(con, proID);
                int qtySavedInDB = 0;
                if (selectedInvID != -1) {
                    String sql = "SELECT ind_count FROM Invoice_details WHERE inv_ID=? AND pro_ID=?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, selectedInvID); ps.setInt(2, proID);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) qtySavedInDB = rs.getInt("ind_count");
                }
                maxLimit = stockInDB + qtySavedInDB;
            } catch(Exception ex) { showError(this, "Lỗi: " + ex.getMessage()); }

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
                    for(int i=0; i<detailModel.getRowCount(); i++) {
                        int idOnTable = Integer.parseInt(detailModel.getValueAt(i, 0).toString());
                        if(idOnTable == proID) {
                            currentQtyInTable = Integer.parseInt(detailModel.getValueAt(i, 3).toString());
                            rowExist = i; break;
                        }
                    }

                    if (currentQtyInTable + qtyInput > stockDB) {
                        showError(this, "Vượt quá tồn kho (" + stockDB + ")!");
                        return;
                    }

                    double price = getProductPrice(proID);
                    DecimalFormat df = new DecimalFormat("#,###");

                    if(rowExist != -1) {
                        int newTotal = currentQtyInTable + qtyInput;
                        detailModel.setValueAt(newTotal, rowExist, 3);
                        detailModel.setValueAt(df.format(price * newTotal), rowExist, 4);
                    } else {
                        String cleanName = item.toString().split(" \\(")[0];
                        detailModel.addRow(new Object[]{ proID, cleanName, df.format(price), qtyInput, df.format(price * qtyInput) });
                    }
                    calculateUITotal();
                    btnSave.setVisible(true);
                } catch (Exception ex) { showError(this, "Lỗi: " + ex.getMessage()); }
            }
        });

        btnSave.addActionListener(e -> {
            if (detailModel.getRowCount() == 0) { showError(this, "Chưa có sản phẩm nào!"); return; }
            if (selectedInvID == -1) createNewInvoice();
            else saveChangesToDatabase();
        });

        // Nút thêm nhanh khách hàng
        btnQuickAddCustomer.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddCustomerDialog dialog = new AddCustomerDialog(parent);
            dialog.setVisible(true);

            if (dialog.isAddedSuccess()) {
                loadComboBoxData(); // Tải lại danh sách
                // Lấy ID mới và chọn nó
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
    }

    // =================================================================================
    //                           PHẦN 4: LOGIC DATABASE
    // =================================================================================

    private void createNewInvoice() {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String sqlInv = "INSERT INTO Invoices (cus_ID, sta_ID, inv_price) VALUES (?, ?, ?)";
            PreparedStatement psInv = con.prepareStatement(sqlInv, Statement.RETURN_GENERATED_KEYS);
            ComboItem cus = (ComboItem) cbCustomer.getSelectedItem();
            ComboItem sta = (ComboItem) cbStaff.getSelectedItem();
            double total = parseMoney(txtTotalMoney.getText());

            psInv.setInt(1, Objects.requireNonNull(cus).getValue());
            psInv.setInt(2, Objects.requireNonNull(sta).getValue());
            psInv.setDouble(3, total);
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
                int currentStock = getProductStock(con, proID);
                if (currentStock < qty) throw new Exception("Sản phẩm ID " + proID + " không đủ hàng.");
                double currentPrice = getProductPrice(proID);

                psDetail.setInt(1, newInvID);
                psDetail.setInt(2, proID);
                psDetail.setInt(3, qty);
                psDetail.setDouble(4, currentPrice);
                psDetail.executeUpdate();

                psStock.setInt(1, qty); psStock.setInt(2, proID);
                psStock.executeUpdate();
            }

            con.commit();
            showSuccess(this, "Tạo hóa đơn thành công! Mã: #" + newInvID);
            loadListData();

            // Chọn lại hóa đơn vừa tạo
            selectInvoiceByID(newInvID);

        } catch (Exception ex) {
            try { if(con!=null) con.rollback(); } catch(Exception ignored) {}
            showError(this, "Lỗi tạo: " + ex.getMessage());
        } finally {
            try { if(con!=null) { con.setAutoCommit(true); con.close(); } } catch(Exception ignored) {}
        }
    }

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
                int currentStock = getProductStock(con, proID);
                if (currentStock < qty) throw new Exception("Sản phẩm ID " + proID + " không đủ hàng.");

                psIns.setInt(1, selectedInvID);
                psIns.setInt(2, proID);
                psIns.setInt(3, qty);
                psIns.setDouble(4, priceOnTable);
                psIns.executeUpdate();

                psDed.setInt(1, qty); psDed.setInt(2, proID);
                psDed.executeUpdate();
            }

            String sqlHead = "UPDATE Invoices SET cus_ID=?, sta_ID=?, inv_price=? WHERE inv_ID=?";
            PreparedStatement psHead = con.prepareStatement(sqlHead);
            ComboItem cus = (ComboItem) cbCustomer.getSelectedItem();
            ComboItem sta = (ComboItem) cbStaff.getSelectedItem();
            double total = parseMoney(txtTotalMoney.getText());

            psHead.setInt(1, Objects.requireNonNull(cus).getValue());
            psHead.setInt(2, Objects.requireNonNull(sta).getValue());
            psHead.setDouble(3, total);
            psHead.setInt(4, selectedInvID);
            psHead.executeUpdate();

            con.commit();
            showSuccess(this, "Cập nhật thành công!");

            // Load lại list nhưng giữ nguyên lựa chọn
            loadListData();
            selectInvoiceByID(selectedInvID);

        } catch (Exception ex) {
            try { if(con!=null) con.rollback(); } catch(Exception ignored) {}
            showError(this, "Lỗi cập nhật: " + ex.getMessage());
        } finally {
            try { if(con!=null) { con.setAutoCommit(true); con.close(); } } catch(Exception ignored){}
        }
    }

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
            try { if (con != null) con.rollback(); } catch (Exception ignored) {}
            showError(this, "Lỗi xóa: " + ex.getMessage());
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (Exception ignored) {}
        }
    }

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

            for (int i = 0; i < detailModel.getRowCount(); i++) {
                bill.append("<tr>");
                bill.append("<td>").append(detailModel.getValueAt(i, 1)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 3)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 2)).append("</td>");
                bill.append("<td class='num'>").append(detailModel.getValueAt(i, 4)).append("</td>");
                bill.append("</tr>");
            }
            bill.append("</table><div class='line'></div>");
            bill.append("<h3 class='right'>TỔNG CỘNG: ").append(txtTotalMoney.getText()).append(" VND</h3>");
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
    //                           PHẦN 5: CÁC HÀM TIỆN ÍCH
    // =================================================================================

    private void calculateUITotal() {
        double total = 0;
        for (int i = 0; i < detailModel.getRowCount(); i++) {
            total += parseMoney(detailModel.getValueAt(i, 4).toString());
        }
        txtTotalMoney.setText(String.format("%,.0f", total));
    }

    private double parseMoney(String text) {
        try { return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).parse(text).doubleValue(); } catch (Exception e) { return 0; }
    }

    private double getProductPrice(int proID) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT pro_price FROM Products WHERE pro_ID = ?");
            ps.setInt(1, proID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getDouble(1);
        } catch(Exception ignored){}
        return 0;
    }

    private int getProductStock(Connection con, int proID) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT pro_count FROM Products WHERE pro_ID = ?");
        ps.setInt(1, proID);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt("pro_count") : 0;
    }

    private void setSelectedComboItem(JComboBox<ComboItem> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).getValue() == id) { cb.setSelectedIndex(i); return; }
        }
    }

    private void enableForm(boolean enable) {
        cbCustomer.setEnabled(enable);
        cbStaff.setEnabled(enable);
        if (btnQuickAddCustomer != null) btnQuickAddCustomer.setEnabled(enable);
        setDetailButtonsVisible(enable);
    }

    private void setDetailButtonsVisible(boolean visible) {
        if (btnAddDetail != null) btnAddDetail.setVisible(visible);
        if (btnEditDetail != null) btnEditDetail.setVisible(visible);
        if (btnDelDetail != null) btnDelDetail.setVisible(visible);
    }

    private void clearForm() {
        isDataLoading = true;
        txtTotalMoney.setText("");
        txtID.setText("[Tự động]");
        txtDate.setText("[Tự động]");

        if (cbCustomer.getItemCount() > 0) cbCustomer.setSelectedIndex(0);
        if (cbStaff.getItemCount() > 0) {
            if (JDBCUtils.Session.isLoggedIn) {
                setSelectedComboItem(cbStaff, JDBCUtils.Session.loggedInStaffID);
            } else cbStaff.setSelectedIndex(0);
        }

        detailModel.setRowCount(0);
        selectedInvID = -1;
        listInvoice.clearSelection();
        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        btnPrint.setVisible(false);
        setDetailButtonsVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void selectInvoiceByID(int id) {
        ListModel<ComboItem> model = listInvoice.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = model.getElementAt(i);
            if (item.getValue() == id) {
                listInvoice.setSelectedIndex(i);
                listInvoice.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public void refreshData() {
        loadComboBoxData();
        loadListData();
        selectInvoiceByID(selectedInvID);
    }

    private void addChangeListeners() {
        cbCustomer.addActionListener(e -> checkChange());
        cbStaff.addActionListener(e -> checkChange());
    }

    private void checkChange() { if (!isDataLoading) btnSave.setVisible(true); }
}