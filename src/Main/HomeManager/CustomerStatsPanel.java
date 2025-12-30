package Main.HomeManager;

import Utils.DBConnection;
import Utils.Session;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static Utils.Export.exportToExcel;
import static Utils.Style.*;

/**
 * Panel for displaying customer statistics.
 * Includes filters for total spent, order count, and address.
 */
public class CustomerStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JPanel pTableWrapper;
    private String currentPeriod = "7 ngày qua"; // Default

    // Filter components
    private JComboBox<String> cbTotalRange;
    private JComboBox<String> cbOrderCountRange;
    private JComboBox<String> cbAddressFilter;
    private JButton btnApplyFilter;

    /**
     * Constructor to initialize the Customer Statistics Panel.
     */
    public CustomerStatsPanel() {
        this.setLayout(new BorderLayout(0, 10));
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Filter Panel ---
        JPanel filterPanel = createFilterPanel();
        this.add(filterPanel, BorderLayout.NORTH);

        // --- Table Panel ---
        String[] columns = {"Hạng", "Mã KH", "Tên Khách Hàng", "Địa Chỉ", "Số Đơn Hàng", "Tổng Chi Tiêu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        setupTableStyle();

        if (Session.canViewStats()) {
            JButton btnExport = createSmallButton("Xuất Excel", Color.decode("#1D6F42"));
            btnExport.setPreferredSize(new Dimension(100, 35));
            btnExport.addActionListener(e -> {
                String fileName = "Danh_Sách_Khách_Hàng_" + currentPeriod.replace(" ", "_");
                exportToExcel(table, fileName);
            });
            pTableWrapper = createTableWithLabel(table, "DANH SÁCH KHÁCH HÀNG", btnExport);
        } else {
            pTableWrapper = createTableWithLabel(table, "DANH SÁCH KHÁCH HÀNG");
        }
        this.add(pTableWrapper, BorderLayout.CENTER);

        loadAddressData();
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

        // 1. Total Spent Filter (3 parts)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbTotalRange = new JComboBox<>(new String[]{
                "Tất cả", "Dưới 1 triệu", "1 triệu - 5 triệu", "5 triệu - 10 triệu", "Trên 10 triệu"
        });
        JPanel pTotal = createComboBoxWithLabel(cbTotalRange, "Tổng chi tiêu:");
        pTotal.setPreferredSize(new Dimension(0, 60));
        contentPanel.add(pTotal, gbc);

        // 2. Order Count Filter (3 parts)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbOrderCountRange = new JComboBox<>(new String[]{
                "Tất cả", "Dưới 5 đơn", "5 - 10 đơn", "10 - 20 đơn", "Trên 20 đơn"
        });
        JPanel pOrder = createComboBoxWithLabel(cbOrderCountRange, "Số đơn hàng:");
        pOrder.setPreferredSize(new Dimension(0, 60));
        contentPanel.add(pOrder, gbc);

        // 3. Address Filter (3 parts)
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 3.0;
        cbAddressFilter = new JComboBox<>();
        JPanel pAddress = createComboBoxWithLabel(cbAddressFilter, "Địa chỉ:");
        pAddress.setPreferredSize(new Dimension(0, 60));
        contentPanel.add(pAddress, gbc);

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
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
    }

    /**
     * Loads unique addresses from the database into the address filter combo box.
     */
    private void loadAddressData() {
        cbAddressFilter.addItem("Tất cả");
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT DISTINCT cus_address FROM Customers WHERE cus_address IS NOT NULL AND cus_address != '' ORDER BY cus_address";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                cbAddressFilter.addItem(rs.getString("cus_address"));
            }
        } catch (Exception ignored) {
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
     * Loads customer data into the table based on filters and period.
     *
     * @param period The time period to filter by.
     */
    public void loadData(String period) {
        this.currentPeriod = period;
        setTableTitle("DANH SÁCH KHÁCH HÀNG (" + period.toUpperCase() + ")");
        tableModel.setRowCount(0);

        List<String> whereConditions = new ArrayList<>();
        List<String> havingConditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // 1. Date Filter (WHERE)
        whereConditions.add(getSqlDateFilter(period));

        // 2. Address Filter (WHERE)
        String selectedAddress = (String) cbAddressFilter.getSelectedItem();
        if (selectedAddress != null && !selectedAddress.equals("Tất cả")) {
            whereConditions.add("c.cus_address LIKE ?");
            params.add("%" + selectedAddress + "%");
        }

        // 3. Total Spent Filter (HAVING)
        String totalRange = (String) cbTotalRange.getSelectedItem();
        if (totalRange != null && !totalRange.equals("Tất cả")) {
            switch (totalRange) {
                case "Dưới 1 triệu":
                    havingConditions.add("SUM(i.inv_price) < 1000000");
                    break;
                case "1 triệu - 5 triệu":
                    havingConditions.add("SUM(i.inv_price) >= 1000000 AND SUM(i.inv_price) <= 5000000");
                    break;
                case "5 triệu - 10 triệu":
                    havingConditions.add("SUM(i.inv_price) >= 5000000 AND SUM(i.inv_price) <= 10000000");
                    break;
                case "Trên 10 triệu":
                    havingConditions.add("SUM(i.inv_price) > 10000000");
                    break;
            }
        }

        // 4. Order Count Filter (HAVING)
        String orderRange = (String) cbOrderCountRange.getSelectedItem();
        if (orderRange != null && !orderRange.equals("Tất cả")) {
            switch (orderRange) {
                case "Dưới 5 đơn":
                    havingConditions.add("COUNT(i.inv_ID) < 5");
                    break;
                case "5 - 10 đơn":
                    havingConditions.add("COUNT(i.inv_ID) >= 5 AND COUNT(i.inv_ID) <= 10");
                    break;
                case "10 - 20 đơn":
                    havingConditions.add("COUNT(i.inv_ID) >= 10 AND COUNT(i.inv_ID) <= 20");
                    break;
                case "Trên 20 đơn":
                    havingConditions.add("COUNT(i.inv_ID) > 20");
                    break;
            }
        }

        String whereClause = String.join(" AND ", whereConditions);
        String havingClause = havingConditions.isEmpty() ? "" : "HAVING " + String.join(" AND ", havingConditions);

        String sql = "SELECT c.cus_ID, c.cus_name, c.cus_address, COUNT(i.inv_ID) as orders, SUM(i.inv_price) as total " +
                "FROM Invoices i " +
                "JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "WHERE " + whereClause + " " +
                "GROUP BY c.cus_ID, c.cus_name, c.cus_address " +
                havingClause + " " +
                "ORDER BY total DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                double total = rs.getDouble("total");
                String moneyStr = String.format("%,.0f VND", total);

                tableModel.addRow(new Object[]{
                        "#" + rank++,
                        rs.getInt("cus_ID"),
                        rs.getString("cus_name"),
                        rs.getString("cus_address"),
                        rs.getInt("orders"),
                        moneyStr
                });
            }
        } catch (Exception e) {
            showError(CustomerStatsPanel.this, "Lỗi tải dữ liệu khách hàng: " + e.getMessage());
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
                        int cusID = Integer.parseInt(table.getValueAt(row, 1).toString());
                        Window win = SwingUtilities.getWindowAncestor(CustomerStatsPanel.this);
                        if (win instanceof DashBoard) {
                            ((DashBoard) win).showCustomerAndLoad(cusID);
                        }
                    } catch (Exception ex) {
                        showError(CustomerStatsPanel.this, "Lỗi: " + ex.getMessage());
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
