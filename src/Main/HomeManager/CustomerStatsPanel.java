package Main.HomeManager;

import JDBCUtils.DBConnection;
import Main.DashBoard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;

import static JDBCUtils.Export.exportToExcel;
import static JDBCUtils.Style.*;

public class CustomerStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;

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

        JPanel pTable;
        if (JDBCUtils.Session.isAdmin()) {
            JButton btnExport = createSmallButton("Xuất Excel", Color.decode("#1D6F42"));
            btnExport.setPreferredSize(new Dimension(100, 35));
            btnExport.addActionListener(e -> exportToExcel(table, "Danh_sach_top_khach_hang_7_ngay_gan_nhat"));
            pTable = createTableWithLabel(table, "TOP KHÁCH HÀNG CHI TIÊU NHIỀU NHẤT 7 NGÀY QUA", btnExport);
        } else {
            pTable = createTableWithLabel(table, "TOP KHÁCH HÀNG CHI TIÊU NHIỀU NHẤT 7 NGÀY QUA");
        }
        this.add(pTable, BorderLayout.CENTER);

        addEvents();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT c.cus_ID, c.cus_name, c.cus_phone, COUNT(i.inv_ID) as orders, SUM(i.inv_price) as total " +
                "FROM Invoices i " +
                "JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "WHERE i.inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "GROUP BY c.cus_ID, c.cus_name, c.cus_phone " +
                "ORDER BY total DESC LIMIT 20";

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
}