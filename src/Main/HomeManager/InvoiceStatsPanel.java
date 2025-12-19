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
import java.text.SimpleDateFormat;

import static JDBCUtils.Export.exportToExcel;
import static JDBCUtils.Style.*;

public class InvoiceStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;

    public InvoiceStatsPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        String[] columns = {"Hạng", "Mã HĐ", "Khách Hàng", "Nhân Viên", "Ngày Lập", "Tổng Tiền"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMinWidth(60);

        JPanel pTable;
        if (JDBCUtils.Session.isAdmin()) {
            JButton btnExport = createSmallButton("Xuất Excel", Color.decode("#1D6F42"));
            btnExport.setPreferredSize(new Dimension(100, 35));
            btnExport.addActionListener(e -> exportToExcel(table, "Danh_sach_top_hoa_don_7_ngay_gan_nhat"));
            pTable = createTableWithLabel(table, "TOP HÓA ĐƠN TỔNG TIỀN NHIỀU NHẤT 7 NGÀY QUA", btnExport);
        } else {
            pTable = createTableWithLabel(table, "TOP HÓA ĐƠN TỔNG TIỀN NHIỀU NHẤT 7 NGÀY QUA");
        }
        this.add(pTable, BorderLayout.CENTER);

        addEvents();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT i.inv_ID, c.cus_name, s.sta_name, i.inv_date, i.inv_price " +
                "FROM Invoices i " +
                "LEFT JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "LEFT JOIN Staffs s ON i.sta_ID = s.sta_ID " +
                "WHERE i.inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "ORDER BY i.inv_price DESC LIMIT 20";

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(sql);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            int rank = 1;

            while (rs.next()) {
                String cusName = rs.getString("cus_name");
                if (cusName == null) cusName = "Khách lẻ";

                String staName = rs.getString("sta_name");
                if (staName == null) staName = "Ẩn danh";

                double price = rs.getDouble("inv_price");
                String moneyStr = (price >= 1000000)
                        ? String.format("%.1f Tr", price / 1000000)
                        : String.format("%.0f K", price / 1000);

                tableModel.addRow(new Object[]{
                        "#" + rank++,
                        rs.getInt("inv_ID"),
                        cusName,
                        staName,
                        sdf.format(rs.getTimestamp("inv_date")),
                        moneyStr
                });
            }
        } catch (Exception e) {
            showError(InvoiceStatsPanel.this, "Lỗi: " + e.getMessage());
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
}