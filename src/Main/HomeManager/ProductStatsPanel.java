package Main.HomeManager;

import Main.HomeManager.Charts.PieChartPanel;
import Utils.DBConnection;
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

import static Utils.Style.createTableWithLabel;
import static Utils.Style.showError;

public class ProductStatsPanel extends JPanel {
    private final PieChartPanel chartPanel;
    private final JTable tableProduct;
    private final DefaultTableModel tableModel;
    private final JPanel pTableWrapper;

    public ProductStatsPanel() {
        this.setLayout(new BorderLayout(20, 0));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        chartPanel = new PieChartPanel(this::loadTableData);
        JPanel pChartWrapper = createChartWrapper(chartPanel);
        pChartWrapper.setPreferredSize(new Dimension(420, 0));

        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Đã Bán", "Tồn Kho", "Doanh Thu"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableProduct = new JTable(tableModel);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableProduct.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableProduct.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableProduct.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tableProduct.getColumnModel().getColumn(0).setMaxWidth(60);
        tableProduct.getColumnModel().getColumn(2).setMaxWidth(70);
        tableProduct.getColumnModel().getColumn(3).setMaxWidth(70);
        pTableWrapper = createTableWithLabel(tableProduct, "TOP SẢN PHẨM BÁN CHẠY");

        this.add(pChartWrapper, BorderLayout.WEST);
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
        chartPanel.loadPieData(period);
        loadTableData("ALL", period);
    }

    private void loadTableData(String categoryName, String period) {
        tableModel.setRowCount(0);
        String dateFilter = getSqlDateFilter(period);
        String sql;

        String titlePeriod = period.toUpperCase();
        if (categoryName.equals("ALL")) {
            setTableTitle("TOP SẢN PHẨM BÁN CHẠY (" + titlePeriod + ")");
            sql = "SELECT p.pro_ID, p.pro_name, p.pro_count, " +
                    "SUM(d.ind_count) as qty, " +
                    "SUM(d.ind_count * d.unit_price) as total " +
                    "FROM Invoice_details d " +
                    "JOIN Products p ON d.pro_ID = p.pro_ID " +
                    "JOIN Invoices i ON d.inv_ID = i.inv_ID " +
                    "WHERE " + dateFilter + " " +
                    "GROUP BY p.pro_ID, p.pro_name, p.pro_count " +
                    "ORDER BY qty DESC LIMIT 20";
        } else {
            setTableTitle("CHI TIẾT: " + categoryName.toUpperCase() + " (" + titlePeriod + ")");
            sql = "SELECT p.pro_ID, p.pro_name, p.pro_count, " +
                    "SUM(d.ind_count) as qty, " +
                    "SUM(d.ind_count * d.unit_price) as total " +
                    "FROM Invoice_details d " +
                    "JOIN Products p ON d.pro_ID = p.pro_ID " +
                    "JOIN ProductTypes t ON p.type_ID = t.type_ID " +
                    "JOIN Invoices i ON d.inv_ID = i.inv_ID " +
                    "WHERE " + dateFilter + " AND t.type_name = ? " +
                    "GROUP BY p.pro_ID, p.pro_name, p.pro_count " +
                    "ORDER BY qty DESC";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!categoryName.equals("ALL")) ps.setString(1, categoryName);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double total = rs.getDouble("total");
                String moneyStr = (total >= 1000000) ? String.format("%.1f Tr", total/1000000) : String.format("%.0f K", total/1000);

                tableModel.addRow(new Object[]{
                        rs.getInt("pro_ID"),
                        rs.getString("pro_name"),
                        rs.getInt("qty"),
                        rs.getInt("pro_count"),
                        moneyStr
                });
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        tableProduct.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableProduct.getSelectedRow();
                    if (row == -1) return;
                    try {
                        int proID = Integer.parseInt(tableProduct.getValueAt(row, 0).toString());

                        Window win = SwingUtilities.getWindowAncestor(ProductStatsPanel.this);
                        if (win instanceof DashBoard) {
                            ((DashBoard) win).showProductAndLoad(proID);
                        }
                    } catch (Exception ex) {
                        showError(ProductStatsPanel.this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    private JPanel createChartWrapper(JPanel content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel lblTitle = new JLabel("TỈ LỆ BÁN THEO DANH MỤC", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#2c3e50"));
        lblTitle.setBorder(new EmptyBorder(5, 0, 15, 0));
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
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