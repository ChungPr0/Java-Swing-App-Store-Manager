package Main.HomeManager;

import Utils.ComboItem;
import Utils.DBConnection;
import Utils.Session;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static Utils.Export.exportToExcel;
import static Utils.Style.*;

/**
 * Panel for displaying invoice statistics.
 * Includes filters for customer, staff, and price range.
 */
public class InvoiceStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JPanel pTableWrapper;
    private String currentPeriod = "7 ngày qua"; // Default

    // Filter components
    private JComboBox<ComboItem> cbCustomerFilter, cbStaffFilter;
    private JComboBox<String> cbPriceRange;
    private JButton btnApplyFilter;

    /**
     * Constructor to initialize the Invoice Statistics Panel.
     */
    public InvoiceStatsPanel() {
        this.setLayout(new BorderLayout(0, 10));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Filter Panel ---
        JPanel filterPanel = createFilterPanel();
        this.add(filterPanel, BorderLayout.NORTH);

        // --- Table Panel ---
        String[] columns = {"Hạng", "Mã HĐ", "Khách Hàng", "Nhân Viên", "Ngày Lập", "Tổng Tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        setupTableStyle();

        if (Session.canViewStats()) {
            JButton btnExport = createSmallButton("Xuất Excel", Color.decode("#1D6F42"));
            btnExport.setPreferredSize(new Dimension(100, 35));
            btnExport.addActionListener(e -> {
                String fileName = "Danh_Sách_Hóa_Đơn_" + currentPeriod.replace(" ", "_");
                exportToExcel(table, fileName);
            });
            pTableWrapper = createTableWithLabel(table, "DANH SÁCH HÓA ĐƠN", btnExport);
        } else {
            pTableWrapper = createTableWithLabel(table, "DANH SÁCH HÓA ĐƠN");
        }
        this.add(pTableWrapper, BorderLayout.CENTER);

        loadFilterData();
        addEvents();
    }

    /**
     * Creates the filter panel with combo boxes and apply button.
     *
     * @return A JPanel containing filter components.
     */
    private JPanel createFilterPanel() {
        // Panel containing filter content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Customer Filter (3 parts)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbCustomerFilter = new JComboBox<>();
        JPanel pCus = createComboBoxWithLabel(cbCustomerFilter, "Khách hàng:");
        pCus.setPreferredSize(new Dimension(0, 60)); // Set width = 0 for GridBagLayout to divide by weight
        contentPanel.add(pCus, gbc);

        // 2. Staff Filter (3 parts)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbStaffFilter = new JComboBox<>();
        JPanel pStaff = createComboBoxWithLabel(cbStaffFilter, "Nhân viên:");
        pStaff.setPreferredSize(new Dimension(0, 60));
        contentPanel.add(pStaff, gbc);

        // 3. Price Range Filter (3 parts)
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbPriceRange = new JComboBox<>(new String[]{
                "Tất cả", "Dưới 1 triệu", "1 triệu - 5 triệu", "5 triệu - 10 triệu", "Trên 10 triệu"
        });
        JPanel pPrice = createComboBoxWithLabel(cbPriceRange, "Khoảng giá:");
        pPrice.setPreferredSize(new Dimension(0, 60));
        contentPanel.add(pPrice, gbc);

        // 4. Apply Button (1 part)
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(25, 5, 5, 5); // Align button with combobox
        btnApplyFilter = createSmallButton("Lọc", Color.decode("#3498db"));
        btnApplyFilter.setPreferredSize(new Dimension(100, 35));
        contentPanel.add(btnApplyFilter, gbc);

        return createSectionWithHeader(contentPanel, "BỘ LỌC CHI TIẾT");
    }

    /**
     * Sets up the style for the table columns.
     */
    private void setupTableStyle() {
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
    }

    /**
     * Loads data into the filter combo boxes (Customers and Staff).
     */
    private void loadFilterData() {
        try (Connection con = DBConnection.getConnection()) {
            // Load customers
            cbCustomerFilter.addItem(new ComboItem("Tất cả", 0));
            ResultSet rsCus = con.createStatement().executeQuery("SELECT cus_ID, cus_name FROM Customers ORDER BY cus_name");
            while (rsCus.next()) {
                cbCustomerFilter.addItem(new ComboItem(rsCus.getString("cus_name"), rsCus.getInt("cus_ID")));
            }

            // Load staffs
            cbStaffFilter.addItem(new ComboItem("Tất cả", 0));
            ResultSet rsSta = con.createStatement().executeQuery("SELECT sta_ID, sta_name FROM Staffs ORDER BY sta_name");
            while (rsSta.next()) {
                cbStaffFilter.addItem(new ComboItem(rsSta.getString("sta_name"), rsSta.getInt("sta_ID")));
            }
        } catch (Exception e) {
            showError(this, "Lỗi tải dữ liệu bộ lọc: " + e.getMessage());
        }
    }

    /**
     * Generates SQL date filter condition based on the selected period.
     *
     * @param period The selected period string.
     * @return SQL condition string.
     */
    private String getSqlDateFilter(String period) {
        return switch (period) {
            case "Hôm nay" -> "DATE(i.inv_date) = DATE('now', 'localtime')";
            case "Tháng này" -> "strftime('%Y-%m', i.inv_date) = strftime('%Y-%m', 'now', 'localtime')";
            case "Quý này" ->
                    "(CAST(strftime('%m', i.inv_date) AS INTEGER) + 2) / 3 = (CAST(strftime('%m', 'now', 'localtime') AS INTEGER) + 2) / 3 AND strftime('%Y', i.inv_date) = strftime('%Y', 'now', 'localtime')";
            case "Năm nay" -> "strftime('%Y', i.inv_date) = strftime('%Y', 'now', 'localtime')";
            default -> "i.inv_date >= date('now', '-6 days', 'localtime')";
        };
    }

    /**
     * Loads invoice data into the table based on filters and period.
     *
     * @param period The time period to filter by.
     */
    public void loadData(String period) {
        this.currentPeriod = period;
        setTableTitle("DANH SÁCH HÓA ĐƠN (" + period.toUpperCase() + ")");
        tableModel.setRowCount(0);

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // 1. Date filter
        conditions.add(getSqlDateFilter(period));

        // 2. Customer filter
        ComboItem selectedCus = (ComboItem) cbCustomerFilter.getSelectedItem();
        if (selectedCus != null && selectedCus.getValue() > 0) {
            conditions.add("i.cus_ID = ?");
            params.add(selectedCus.getValue());
        }

        // 3. Staff filter
        ComboItem selectedStaff = (ComboItem) cbStaffFilter.getSelectedItem();
        if (selectedStaff != null && selectedStaff.getValue() > 0) {
            conditions.add("i.sta_ID = ?");
            params.add(selectedStaff.getValue());
        }

        // 4. Price filter
        String priceRange = (String) cbPriceRange.getSelectedItem();
        if (priceRange != null && !priceRange.equals("Tất cả")) {
            switch (priceRange) {
                case "Dưới 1 triệu":
                    conditions.add("i.inv_price < 1000000");
                    break;
                case "1 triệu - 5 triệu":
                    conditions.add("i.inv_price >= 1000000 AND i.inv_price <= 5000000");
                    break;
                case "5 triệu - 10 triệu":
                    conditions.add("i.inv_price >= 5000000 AND i.inv_price <= 10000000");
                    break;
                case "Trên 10 triệu":
                    conditions.add("i.inv_price > 10000000");
                    break;
            }
        }

        String sql = "SELECT i.inv_ID, c.cus_name, s.sta_name, i.inv_date, i.inv_price " +
                "FROM Invoices i " +
                "LEFT JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "LEFT JOIN Staffs s ON i.sta_ID = s.sta_ID " +
                "WHERE " + String.join(" AND ", conditions) + " " +
                "ORDER BY i.inv_price DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            int rank = 1;

            while (rs.next()) {
                double price = rs.getDouble("inv_price");
                String moneyStr = String.format("%,.0f VND", price);

                tableModel.addRow(new Object[]{
                        "#" + rank++,
                        rs.getInt("inv_ID"),
                        rs.getString("cus_name") != null ? rs.getString("cus_name") : "Khách lẻ",
                        rs.getString("sta_name") != null ? rs.getString("sta_name") : "Ẩn danh",
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("inv_date"))),
                        moneyStr
                });
            }
        } catch (Exception e) {
            showError(InvoiceStatsPanel.this, "Lỗi tải dữ liệu hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        btnApplyFilter.addActionListener(e -> loadData(currentPeriod));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row == -1) return;
                    try {
                        int invID = Integer.parseInt(table.getValueAt(row, 1).toString());
                        Window win = SwingUtilities.getWindowAncestor(InvoiceStatsPanel.this);
                        if (win instanceof DashBoard) {
                            ((DashBoard) win).showInvoiceAndLoad(invID);
                        }
                    } catch (Exception ex) {
                        showError(InvoiceStatsPanel.this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Updates the title of the table wrapper.
     *
     * @param text The new title text.
     */
    private void setTableTitle(String text) {
        try {
            BorderLayout layout = (BorderLayout) pTableWrapper.getLayout();
            Component headerComp = layout.getLayoutComponent(BorderLayout.NORTH);
            if (headerComp instanceof JPanel headerPanel) {
                JLabel lbl = (JLabel) ((BorderLayout) headerPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (lbl != null) lbl.setText(text.toUpperCase());
            }
        } catch (Exception e) {
            showError(this, "Lỗi set title: " + e.getMessage());
        }
    }
}
