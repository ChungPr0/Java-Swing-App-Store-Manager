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
import java.text.SimpleDateFormat;

import static Utils.Export.exportToExcel;
import static Utils.Style.*;

public class InvoiceStatsPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JPanel pTableWrapper;

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

        if (Utils.Session.isAdmin()) {
            JButton btnExport = createSmallButton("Xuất Excel", Color.decode("#1D6F42"));
            btnExport.setPreferredSize(new Dimension(100, 35));
            btnExport.addActionListener(e -> exportToExcel(table, "Danh_sach_top_hoa_don"));
            pTableWrapper = createTableWithLabel(table, "TOP HÓA ĐƠN TỔNG TIỀN NHIỀU NHẤT", btnExport);
        } else {
            pTableWrapper = createTableWithLabel(table, "TOP HÓA ĐƠN TỔNG TIỀN NHIỀU NHẤT");
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
        setTableTitle("TOP HÓA ĐƠN TỔNG TIỀN NHIỀU NHẤT (" + period.toUpperCase() + ")");
        tableModel.setRowCount(0);
        String dateFilter = getSqlDateFilter(period);

        String sql = "SELECT i.inv_ID, c.cus_name, s.sta_name, i.inv_date, i.inv_price " +
                "FROM Invoices i " +
                "LEFT JOIN Customers c ON i.cus_ID = c.cus_ID " +
                "LEFT JOIN Staffs s ON i.sta_ID = s.sta_ID " +
                "WHERE " + dateFilter + " " +
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

                String dateStr = rs.getString("inv_date");
                String formattedDate = "";
                if (dateStr != null) {
                    try {
                        java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
                        formattedDate = sdf.format(parsedDate);
                    } catch (java.text.ParseException e) {
                        formattedDate = dateStr; // Fallback
                        System.err.println("Could not parse date in InvoiceStatsPanel: " + dateStr);
                    }
                }

                tableModel.addRow(new Object[]{
                        "#" + rank++,
                        rs.getInt("inv_ID"),
                        cusName,
                        staName,
                        formattedDate,
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