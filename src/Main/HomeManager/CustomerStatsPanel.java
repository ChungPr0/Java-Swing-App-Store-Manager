package Main.HomeManager;

import Utils.DBConnection;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;

import static Utils.Export.exportToExcel;
import static Utils.Style.*;

public class CustomerStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JPanel pTableWrapper;
    private String currentPeriod = "7 ngày qua"; // Mặc định

    public CustomerStatsPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        String[] columns = {"Hạng", "Mã KH", "Tên Khách Hàng", "Số Điện Thoại", "Số Đơn Hàng", "Tổng Chi Tiêu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMinWidth(60);

        if (Utils.Session.isAdmin()) {
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

        addEvents();
    }

    private String getSqlDateFilter(String period) {
        return switch (period) {
            case "Hôm nay" -> "DATE(i.inv_date) = DATE('now', 'localtime')";
            case "Tháng này" -> "strftime('%Y-%m', i.inv_date) = strftime('%Y-%m', 'now', 'localtime')";
            case "Quý này" -> "(CAST(strftime('%m', i.inv_date) AS INTEGER) + 2) / 3 = (CAST(strftime('%m', 'now', 'localtime') AS INTEGER) + 2) / 3 AND strftime('%Y', i.inv_date) = strftime('%Y', 'now', 'localtime')";
            case "Năm nay" -> "strftime('%Y', i.inv_date) = strftime('%Y', 'now', 'localtime')";
            default -> "i.inv_date >= date('now', '-6 days', 'localtime')";
        };
    }

    public void loadData(String period) {
        this.currentPeriod = period; // Cập nhật biến toàn cục
        setTableTitle("DANH SÁCH KHÁCH HÀNG (" + period.toUpperCase() + ")");
        tableModel.setRowCount(0);
        String dateFilter = getSqlDateFilter(period);

        String sql = "SELECT c.cus_ID, c.cus_name, c.cus_phone, COUNT(i.inv_ID) as orders, SUM(i.inv_price) as total " +
                "FROM Invoices i " +
                "JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "WHERE " + dateFilter + " " +
                "GROUP BY c.cus_ID, c.cus_name, c.cus_phone " +
                "ORDER BY total DESC";

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(sql);
            int rank = 1;
            while (rs.next()) {
                double total = rs.getDouble("total");
                String moneyStr = (total >= 1000000)
                        ? String.format("%.1f Tr", total/1000000)
                        : String.format("%.0f K", total/1000);

                tableModel.addRow(new Object[]{
                        "#" + rank++,
                        rs.getInt("cus_ID"),
                        rs.getString("cus_name"),
                        rs.getString("cus_phone"),
                        rs.getInt("orders"),
                        moneyStr
                });
            }
        } catch (Exception e) {
            showError(CustomerStatsPanel.this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row == -1) return;
                    try {
                        int cusID = Integer.parseInt(table.getValueAt(row, 1).toString());

                        Window win = SwingUtilities.getWindowAncestor(CustomerStatsPanel.this);
                        if (win instanceof DashBoard dashboard) {
                            dashboard.showCustomerAndLoad(cusID);
                        }
                    } catch (Exception ex) {
                        showError(CustomerStatsPanel.this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    private void setTableTitle(String text) {
        try {
            BorderLayout layout = (BorderLayout) pTableWrapper.getLayout();
            Component headerComp = layout.getLayoutComponent(BorderLayout.NORTH);

            if (headerComp instanceof JPanel headerPanel) {
                BorderLayout headerLayout = (BorderLayout) headerPanel.getLayout();
                JLabel lbl = (JLabel) headerLayout.getLayoutComponent(BorderLayout.CENTER);
                if (lbl != null) lbl.setText(text.toUpperCase());
            } else if (headerComp instanceof JLabel) {
                ((JLabel) headerComp).setText(text.toUpperCase());
            }
        } catch (Exception e) {
             showError(this, "Lỗi set title: " + e.getMessage());
        }
    }
}