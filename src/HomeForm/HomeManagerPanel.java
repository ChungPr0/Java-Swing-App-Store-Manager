package HomeForm;

import Main.DashBoard;
import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import static JDBCUtils.Style.*;

public class HomeManagerPanel extends JPanel {
    private JLabel lblRevenue, lblProductCount, lblCustomerCount, lblOrderCount;
    private JTable tableRecent;
    private JButton btnRefresh;
    private DefaultTableModel tableModel;

    public HomeManagerPanel() {
        initUI();
        loadDashboardData();
        addEvents();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(Color.decode("#ecf0f1"));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);

        JLabel lblTitle = createHeaderLabel("TỔNG QUAN CỬA HÀNG");
        btnRefresh = createButton("Làm mới dữ liệu", Color.GRAY);

        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(btnRefresh, BorderLayout.EAST);

        this.add(pHeader, BorderLayout.NORTH);

        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);

        JPanel pStats = new JPanel(new GridLayout(1, 4, 20, 0));
        pStats.setOpaque(false);
        pStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblRevenue = new JLabel("0 đ");
        lblProductCount = new JLabel("0");
        lblCustomerCount = new JLabel("0");
        lblOrderCount = new JLabel("0");

        pStats.add(createCard("DOANH THU", lblRevenue, new Color(46, 204, 113), "assets/icons/money.png"));
            pStats.add(createCard("SẢN PHẨM", lblProductCount, new Color(52, 152, 219), "assets/icons/box.png"));
        pStats.add(createCard("KHÁCH HÀNG", lblCustomerCount, new Color(243, 156, 18), "assets/icons/customer.png"));
        pStats.add(createCard("HÓA ĐƠN", lblOrderCount, new Color(155, 89, 182), "assets/icons/bill.png"));

        pCenter.add(pStats);
        pCenter.add(Box.createVerticalStrut(20));

        JPanel pTableSection = new JPanel(new BorderLayout());
        pTableSection.setBackground(Color.WHITE);
        pTableSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTableTitle = createTitleLabel("Đơn hàng gần đây");

        String[] columns = {"Mã HĐ", "Khách Hàng", "Nhân Viên", "Tổng Tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableRecent = new JTable(tableModel);
        tableRecent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableRecent.setRowHeight(30);
        tableRecent.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableRecent.getTableHeader().setBackground(Color.decode("#ecf0f1"));

        pTableSection.add(lblTableTitle, BorderLayout.NORTH);
        pTableSection.add(new JScrollPane(tableRecent), BorderLayout.CENTER);

        pCenter.add(pTableSection);

        this.add(pCenter, BorderLayout.CENTER);
    }

    private void loadDashboardData() {
        try (Connection con = DBConnection.getConnection()) {
            DecimalFormat df = new DecimalFormat("#,### VND");

            String sqlRev = "SELECT SUM(inv_price) FROM Invoices";
            ResultSet rsRev = con.createStatement().executeQuery(sqlRev);
            if (rsRev.next()) {
                double rev = rsRev.getDouble(1);
                lblRevenue.setText(df.format(rev));
            }

            ResultSet rsPro = con.createStatement().executeQuery("SELECT COUNT(*) FROM Products");
            if (rsPro.next()) lblProductCount.setText(String.valueOf(rsPro.getInt(1)));

            ResultSet rsCus = con.createStatement().executeQuery("SELECT COUNT(*) FROM Customers");
            if (rsCus.next()) lblCustomerCount.setText(String.valueOf(rsCus.getInt(1)));

            ResultSet rsInv = con.createStatement().executeQuery("SELECT COUNT(*) FROM Invoices");
            if (rsInv.next()) lblOrderCount.setText(String.valueOf(rsInv.getInt(1)));

            tableModel.setRowCount(0);
            String sqlTable = "SELECT i.inv_ID, c.cus_name, s.sta_name, i.inv_price " +
                    "FROM Invoices i " +
                    "LEFT JOIN Customers c ON i.cus_ID = c.cus_ID " +
                    "LEFT JOIN Staffs s ON i.sta_ID = s.sta_ID " +
                    "ORDER BY i.inv_ID DESC LIMIT 10";

            ResultSet rsTable = con.createStatement().executeQuery(sqlTable);
            while (rsTable.next()) {
                tableModel.addRow(new Object[]{
                        rsTable.getInt("inv_ID"),
                        rsTable.getString("cus_name"),
                        rsTable.getString("sta_name"),
                        df.format(rsTable.getDouble("inv_price"))
                });
            }

        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        btnRefresh.addActionListener(_ -> loadDashboardData());

        tableRecent.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableRecent.getSelectedRow();
                    if (row == -1) return;

                    try {
                        int invID = Integer.parseInt(tableRecent.getValueAt(row, 0).toString());

                        Window win = SwingUtilities.getWindowAncestor(HomeManagerPanel.this);

                        if (win instanceof DashBoard dashboard) {
                            dashboard.showInvoiceAndLoad(invID);
                        }

                    } catch (Exception ex) {
                        showError(HomeManagerPanel.this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    public void refreshData() {
        loadDashboardData();
    }
}
