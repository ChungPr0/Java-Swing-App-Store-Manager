package Main.HomeManager;

import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;

import static JDBCUtils.Style.*;

public class HomeManagerPanel extends JPanel {
    private JLabel lblRevenue7Days, lblItemsSold7Days, lblActiveCustomers7Days, lblOrders7Days;
    private JButton btnRefresh;
    private JPanel pRevCard, pItemCard, pCusCard, pOrdCard;

    private JPanel bottomPanel;
    private CardLayout bottomCardLayout;

    private RevenueChartPanel chartPanel;
    private ProductStatsPanel productPanel;
    private CustomerStatsPanel customerPanel;
    private InvoiceStatsPanel invoicePanel;

    public HomeManagerPanel() {
        initUI();
        refreshData();
        addEvents();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(Color.decode("#ecf0f1"));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        JLabel lblTitle = createHeaderLabel("TỔNG QUAN 7 NGÀY QUA");

        btnRefresh = createSmallButton("Làm Mới", Color.GRAY);
        btnRefresh.setPreferredSize(new Dimension(80, 35));
        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(btnRefresh, BorderLayout.EAST);
        this.add(pHeader, BorderLayout.NORTH);

        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);

        JPanel pStats = new JPanel(new GridLayout(1, 4, 20, 0));
        pStats.setOpaque(false);
        pStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblRevenue7Days = new JLabel("0 đ");
        lblItemsSold7Days = new JLabel("0");
        lblActiveCustomers7Days = new JLabel("0");
        lblOrders7Days = new JLabel("0");

        pRevCard = JDBCUtils.Style.createCard("DOANH THU", lblRevenue7Days, new Color(46, 204, 113), "assets/icons/money.png");
        pItemCard = JDBCUtils.Style.createCard("SẢN PHẨM", lblItemsSold7Days, new Color(52, 152, 219), "assets/icons/box.png");
        pCusCard = JDBCUtils.Style.createCard("KHÁCH HÀNG", lblActiveCustomers7Days, new Color(243, 156, 18), "assets/icons/customer.png");
        pOrdCard = JDBCUtils.Style.createCard("ĐƠN HÀNG", lblOrders7Days, new Color(155, 89, 182), "assets/icons/bill.png");

        pRevCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pItemCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pCusCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pOrdCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pStats.add(pRevCard);
        pStats.add(pItemCard);
        pStats.add(pCusCard);
        pStats.add(pOrdCard);

        pCenter.add(pStats);
        pCenter.add(Box.createVerticalStrut(20));

        bottomCardLayout = new CardLayout();
        bottomPanel = new JPanel(bottomCardLayout);
        bottomPanel.setOpaque(false);

        chartPanel = new RevenueChartPanel();
        productPanel = new ProductStatsPanel();
        customerPanel = new CustomerStatsPanel();
        invoicePanel = new InvoiceStatsPanel();

        bottomPanel.add(chartPanel, "REVENUE");
        bottomPanel.add(productPanel, "PRODUCT");
        bottomPanel.add(customerPanel, "CUSTOMER");
        bottomPanel.add(invoicePanel, "INVOICE");

        bottomCardLayout.show(bottomPanel, "REVENUE");

        pCenter.add(bottomPanel);
        this.add(pCenter, BorderLayout.CENTER);
    }

    private void addEvents() {
        btnRefresh.addActionListener(e -> refreshData());

        pRevCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                chartPanel.loadChartData();
                bottomCardLayout.show(bottomPanel, "REVENUE");
            }
        });

        pItemCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                productPanel.loadData();
                bottomCardLayout.show(bottomPanel, "PRODUCT");
            }
        });

        pCusCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                customerPanel.loadData();
                bottomCardLayout.show(bottomPanel, "CUSTOMER");
            }
        });

        pOrdCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                invoicePanel.loadData();
                bottomCardLayout.show(bottomPanel, "INVOICE");
            }
        });
    }

    private String formatSmartMoney(double val) {
        if (val >= 1000000) {
            double tr = val / 1000000.0;
            return (tr == (long) tr) ? String.format("%d Tr", (long) tr) : String.format("%.1f Tr", tr);
        } else {
            return String.format("%d K", (long) (val / 1000));
        }
    }

    public void refreshData() {
        try (Connection con = DBConnection.getConnection()) {
            String sqlRev = "SELECT SUM(inv_price) FROM Invoices WHERE inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            ResultSet rsRev = con.createStatement().executeQuery(sqlRev);
            if (rsRev.next()) lblRevenue7Days.setText(formatSmartMoney(rsRev.getDouble(1)));

            String sqlItems = "SELECT SUM(d.ind_count) FROM Invoice_details d JOIN Invoices i ON d.inv_ID = i.inv_ID WHERE i.inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            ResultSet rsItems = con.createStatement().executeQuery(sqlItems);
            if (rsItems.next()) lblItemsSold7Days.setText(String.valueOf(rsItems.getInt(1)));

            String sqlCus = "SELECT COUNT(DISTINCT cus_ID) FROM Invoices WHERE inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            ResultSet rsCus = con.createStatement().executeQuery(sqlCus);
            if (rsCus.next()) lblActiveCustomers7Days.setText(String.valueOf(rsCus.getInt(1)));

            String sqlOrd = "SELECT COUNT(*) FROM Invoices WHERE inv_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
            ResultSet rsOrd = con.createStatement().executeQuery(sqlOrd);
            if (rsOrd.next()) lblOrders7Days.setText(String.valueOf(rsOrd.getInt(1)));

        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }

        if (chartPanel.isVisible()) chartPanel.loadChartData();
        if (productPanel.isVisible()) productPanel.loadData();
        if (customerPanel.isVisible()) customerPanel.loadData();
        if (invoicePanel.isVisible()) invoicePanel.loadData();
    }
}