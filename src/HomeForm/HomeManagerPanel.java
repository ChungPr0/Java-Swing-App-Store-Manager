package HomeForm;

import InvoiceForm.InvoiceDetailDialog;
import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import static JDBCUntils.Style.*;

public class HomeManagerPanel extends JPanel {
    private JLabel lblRevenue, lblProductCount, lblCustomerCount, lblOrderCount;
    private JTable tableRecent;
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

        // --- PHẦN 1: TIÊU ĐỀ & NÚT LÀM MỚI ---
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);

        JLabel lblTitle = createHeaderLabel("TỔNG QUAN CỬA HÀNG");
        JButton btnRefresh = createButton("Làm mới dữ liệu", Color.GRAY);

        btnRefresh.addActionListener(e -> loadDashboardData());

        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(btnRefresh, BorderLayout.EAST);

        this.add(pHeader, BorderLayout.NORTH);

        // --- PHẦN 2: CENTER (CHỨA THẺ THỐNG KÊ + BẢNG) ---
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);

        // 2.1 Các thẻ thống kê (Stats Cards)
        JPanel pStats = new JPanel(new GridLayout(1, 4, 20, 0));
        pStats.setOpaque(false);
        pStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Tạo 4 thẻ
        lblRevenue = new JLabel("0 đ");
        lblProductCount = new JLabel("0");
        lblCustomerCount = new JLabel("0");
        lblOrderCount = new JLabel("0");

        // Màu sắc: Xanh lá (Tiền), Xanh dương (SP), Cam (Khách), Tím (Đơn)
        pStats.add(createCard("DOANH THU", lblRevenue, new Color(46, 204, 113), "asset/icons/money.png"));
        pStats.add(createCard("SẢN PHẨM", lblProductCount, new Color(52, 152, 219), "asset/icons/box.png"));
        pStats.add(createCard("KHÁCH HÀNG", lblCustomerCount, new Color(243, 156, 18), "asset/icons/users.png"));
        pStats.add(createCard("HÓA ĐƠN", lblOrderCount, new Color(155, 89, 182), "asset/icons/bill.png"));

        pCenter.add(pStats);
        pCenter.add(Box.createVerticalStrut(20));

        // 2.2 Bảng đơn hàng gần đây
        JPanel pTableSection = new JPanel(new BorderLayout());
        pTableSection.setBackground(Color.WHITE);
        pTableSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTableTitle = createTitleLabel("Đơn hàng gần đây");

        // Cấu hình bảng
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

    // --- LOGIC DỮ LIỆU ---
    private void loadDashboardData() {
        try (Connection con = DBConnection.getConnection()) {
            DecimalFormat df = new DecimalFormat("#,### VND");

            // 1. Load Tổng Doanh Thu
            String sqlRev = "SELECT SUM(inv_price) FROM Invoices";
            ResultSet rsRev = con.createStatement().executeQuery(sqlRev);
            if (rsRev.next()) {
                double rev = rsRev.getDouble(1);
                lblRevenue.setText(df.format(rev));
            }

            // 2. Load Tổng Sản Phẩm
            ResultSet rsPro = con.createStatement().executeQuery("SELECT COUNT(*) FROM Products");
            if (rsPro.next()) lblProductCount.setText(String.valueOf(rsPro.getInt(1)));

            // 3. Load Tổng Khách Hàng
            ResultSet rsCus = con.createStatement().executeQuery("SELECT COUNT(*) FROM Customers");
            if (rsCus.next()) lblCustomerCount.setText(String.valueOf(rsCus.getInt(1)));

            // 4. Load Tổng Hóa Đơn
            ResultSet rsInv = con.createStatement().executeQuery("SELECT COUNT(*) FROM Invoices");
            if (rsInv.next()) lblOrderCount.setText(String.valueOf(rsInv.getInt(1)));

            // 5. Load Bảng Đơn Hàng Gần Đây (Lấy 10 cái mới nhất theo inv_ID)
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
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        // Sự kiện click vào bảng
        tableRecent.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableRecent.getSelectedRow();
                    if (row == -1) return;

                    int invID = Integer.parseInt(tableRecent.getValueAt(row, 0).toString());

                    JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(HomeManagerPanel.this);
                    InvoiceDetailDialog dialog = new InvoiceDetailDialog(parent, invID);
                    dialog.setVisible(true);
                }
            }
        });
    }
}
