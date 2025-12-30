package Main.HomeManager;

import Utils.DBConnection;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;

import static Utils.Style.*;

/**
 * Panel for the Home Manager Dashboard.
 * Displays overview statistics and charts.
 */
public class HomeManagerPanel extends JPanel {
    private JLabel lblRevenue, lblItemsSold, lblActiveCustomers, lblOrders;
    private JButton btnRefresh;
    private JPanel pRevCard, pItemCard, pCusCard, pOrdCard;
    private JComboBox<String> cbPeriod;
    private JLabel lblTitle;

    private JPanel bottomPanel;
    private CardLayout bottomCardLayout;

    private RevenueChartPanel chartPanel;
    private ProductStatsPanel productPanel;
    private CustomerStatsPanel customerPanel;
    private InvoiceStatsPanel invoicePanel;

    /**
     * Constructor to initialize the Home Manager Panel.
     */
    public HomeManagerPanel() {
        initUI();
        refreshData();
        addEvents();
    }

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(Color.decode("#ecf0f1"));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header Panel ---
        JPanel pHeader = new JPanel(new BorderLayout(10, 10));
        pHeader.setOpaque(false);

        lblTitle = createHeaderLabel("TỔNG QUAN");

        // Right side of header with ComboBox and Button
        JPanel pHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pHeaderRight.setOpaque(false);

        cbPeriod = new JComboBox<>(new String[]{"Hôm nay", "7 ngày qua", "Tháng này", "Quý này", "Năm nay"});
        cbPeriod.setSelectedIndex(0);
        cbPeriod.setFocusable(false);
        JPanel pCombo = createComboBoxWithLabel(cbPeriod, "Thống kê theo:");
        cbPeriod.setPreferredSize(new Dimension(120, 34));
        pCombo.setOpaque(false);
        pHeaderRight.add(pCombo);

        pHeaderRight.add(Box.createHorizontalStrut(10)); // Add space

        btnRefresh = createSmallButton("Làm Mới", Color.GRAY);
        btnRefresh.setPreferredSize(new Dimension(80, 35));
        pHeaderRight.add(btnRefresh);

        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(pHeaderRight, BorderLayout.EAST);
        this.add(pHeader, BorderLayout.NORTH);

        // --- Center Panel ---
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);

        JPanel pStats = new JPanel(new GridLayout(1, 4, 20, 0));
        pStats.setOpaque(false);
        pStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblRevenue = new JLabel("0 đ");
        lblItemsSold = new JLabel("0");
        lblActiveCustomers = new JLabel("0");
        lblOrders = new JLabel("0");

        pRevCard = Utils.Style.createCard("DOANH THU", lblRevenue, new Color(46, 204, 113), "assets/icons/money.png");
        pItemCard = Utils.Style.createCard("SẢN PHẨM", lblItemsSold, new Color(52, 152, 219), "assets/icons/box.png");
        pCusCard = Utils.Style.createCard("KHÁCH HÀNG", lblActiveCustomers, new Color(243, 156, 18), "assets/icons/customer.png");
        pOrdCard = Utils.Style.createCard("ĐƠN HÀNG", lblOrders, new Color(155, 89, 182), "assets/icons/bill.png");

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

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        btnRefresh.addActionListener(e -> refreshData());
        cbPeriod.addActionListener(e -> refreshData());

        pRevCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!Session.canViewStats()) return;
                String period = (String) cbPeriod.getSelectedItem();
                if (period != null) {
                    chartPanel.loadChartData(period);
                }
                bottomCardLayout.show(bottomPanel, "REVENUE");
            }
        });

        pItemCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!Session.canViewStats()) return;
                String period = (String) cbPeriod.getSelectedItem();
                productPanel.loadData(period);
                bottomCardLayout.show(bottomPanel, "PRODUCT");
            }
        });

        pCusCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!Session.canViewStats()) return;
                String period = (String) cbPeriod.getSelectedItem();
                if (period != null) {
                    customerPanel.loadData(period);
                }
                bottomCardLayout.show(bottomPanel, "CUSTOMER");
            }
        });

        pOrdCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!Session.canViewStats()) return;
                String period = (String) cbPeriod.getSelectedItem();
                if (period != null) {
                    invoicePanel.loadData(period);
                }
                bottomCardLayout.show(bottomPanel, "INVOICE");
            }
        });
    }

    /**
     * Formats money values into a shorter string (e.g., 1.5 Tr, 500 K).
     *
     * @param val The value to format.
     * @return Formatted string.
     */
    private String formatSmartMoney(double val) {
        if (val >= 1000000) {
            double tr = val / 1000000.0;
            return (tr == (long) tr) ? String.format("%d Tr", (long) tr) : String.format("%.1f Tr", tr);
        } else {
            return String.format("%d K", (long) (val / 1000));
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
            case "Hôm nay" -> "DATE(inv_date) = DATE('now', 'localtime')";
            case "Tháng này" -> "strftime('%Y-%m', inv_date) = strftime('%Y-%m', 'now', 'localtime')";
            case "Quý này" ->
                    "(CAST(strftime('%m', inv_date) AS INTEGER) + 2) / 3 = (CAST(strftime('%m', 'now', 'localtime') AS INTEGER) + 2) / 3 AND strftime('%Y', inv_date) = strftime('%Y', 'now', 'localtime')";
            case "Năm nay" -> "strftime('%Y', inv_date) = strftime('%Y', 'now', 'localtime')";
            default -> "inv_date >= date('now', '-6 days', 'localtime')"; // 7 days ago
        };
    }

    /**
     * Refreshes the data displayed on the panel.
     */
    public void refreshData() {
        // Only allow viewing statistics if authorized
        if (!Session.canViewStats()) {
            lblRevenue.setText("---");
            lblItemsSold.setText("---");
            lblActiveCustomers.setText("---");
            lblOrders.setText("---");
            return;
        }

        String period = (String) cbPeriod.getSelectedItem();
        if (period == null) return;

        lblTitle.setText("TỔNG QUAN " + period.toUpperCase());
        String dateFilter = getSqlDateFilter(period);

        try (Connection con = DBConnection.getConnection()) {
            String sqlRev = "SELECT SUM(inv_price) FROM Invoices WHERE " + dateFilter;
            ResultSet rsRev = con.createStatement().executeQuery(sqlRev);
            if (rsRev.next()) lblRevenue.setText(formatSmartMoney(rsRev.getDouble(1)));

            String sqlItems = "SELECT SUM(d.ind_count) FROM Invoice_details d JOIN Invoices i ON d.inv_ID = i.inv_ID WHERE " + dateFilter;
            ResultSet rsItems = con.createStatement().executeQuery(sqlItems);
            if (rsItems.next()) lblItemsSold.setText(String.valueOf(rsItems.getInt(1)));

            String sqlCus = "SELECT COUNT(DISTINCT cus_ID) FROM Invoices WHERE " + dateFilter;
            ResultSet rsCus = con.createStatement().executeQuery(sqlCus);
            if (rsCus.next()) lblActiveCustomers.setText(String.valueOf(rsCus.getInt(1)));

            String sqlOrd = "SELECT COUNT(*) FROM Invoices WHERE " + dateFilter;
            ResultSet rsOrd = con.createStatement().executeQuery(sqlOrd);
            if (rsOrd.next()) lblOrders.setText(String.valueOf(rsOrd.getInt(1)));

        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }

        // Refresh the visible panel
        Component visibleComp = null;
        for (Component comp : bottomPanel.getComponents()) {
            if (comp.isVisible()) {
                visibleComp = comp;
                break;
            }
        }

        if (visibleComp instanceof RevenueChartPanel) ((RevenueChartPanel) visibleComp).loadChartData(period);
        else if (visibleComp instanceof ProductStatsPanel) ((ProductStatsPanel) visibleComp).loadData(period);
        else if (visibleComp instanceof CustomerStatsPanel) ((CustomerStatsPanel) visibleComp).loadData(period);
        else if (visibleComp instanceof InvoiceStatsPanel) ((InvoiceStatsPanel) visibleComp).loadData(period);
    }
}
