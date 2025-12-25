package Main.HomeManager;

import Utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static Utils.Style.showError;

public class RevenueChartPanel extends JPanel {
    private final List<String> dates = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();
    private double maxValue = 0;
    private final JLabel lblTitle;

    public RevenueChartPanel() {
        this.setBackground(Color.WHITE);
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        lblTitle = new JLabel("BIỂU ĐỒ DOANH THU", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#2c3e50"));
        lblTitle.setBorder(new EmptyBorder(10, 0, 20, 0));
        this.add(lblTitle, BorderLayout.NORTH);
    }

    public void loadChartData(String period) {
        dates.clear();
        values.clear();
        maxValue = 0;
        lblTitle.setText("BIỂU ĐỒ DOANH THU " + period.toUpperCase());

        String groupBy;
        String dateSelect;
        String dateFilter;
        String orderBy;

        switch (period) {
            case "Hôm nay":
                dateSelect = "strftime('%H:00', inv_date)";
                groupBy = "strftime('%Y-%m-%d %H', inv_date)";
                dateFilter = "DATE(inv_date) = DATE('now', 'localtime')";
                orderBy = "strftime('%Y-%m-%d %H', inv_date)";
                break;
            case "Tháng này":
                dateSelect = "strftime('%d', inv_date)";
                groupBy = "DATE(inv_date)";
                dateFilter = "strftime('%Y-%m', inv_date) = strftime('%Y-%m', 'now', 'localtime')";
                orderBy = "DATE(inv_date)";
                break;
            case "Quý này":
                dateSelect = "strftime('%m/%Y', inv_date)";
                groupBy = "strftime('%Y-%m', inv_date)";
                dateFilter = "(CAST(strftime('%m', inv_date) AS INTEGER) + 2) / 3 = (CAST(strftime('%m', 'now', 'localtime') AS INTEGER) + 2) / 3 AND strftime('%Y', inv_date) = strftime('%Y', 'now', 'localtime')";
                orderBy = "strftime('%Y-%m', inv_date)";
                break;
            case "Năm nay":
                dateSelect = "strftime('%m', inv_date)";
                groupBy = "strftime('%Y-%m', inv_date)";
                dateFilter = "strftime('%Y', inv_date) = strftime('%Y', 'now', 'localtime')";
                orderBy = "strftime('%Y-%m', inv_date)";
                break;
            default: // "7 ngày qua"
                dateSelect = "strftime('%d/%m', inv_date)";
                groupBy = "DATE(inv_date)";
                dateFilter = "inv_date >= date('now', '-6 days', 'localtime')";
                orderBy = "DATE(inv_date)";
                break;
        }

        String sql = "SELECT " + dateSelect + " as d, SUM(inv_price) as total " +
                "FROM Invoices " +
                "WHERE " + dateFilter + " " +
                "GROUP BY " + groupBy + " " +
                "ORDER BY " + orderBy + " ASC";

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                dates.add(rs.getString("d"));
                double val = rs.getDouble("total");
                values.add(val);
                if (val > maxValue) maxValue = val;
            }
            repaint();
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font fontValue = new Font("Segoe UI", Font.BOLD, 13);
        Font fontDate = new Font("Segoe UI", Font.PLAIN, 12);

        if (values.isEmpty()) {
            g2.setFont(fontValue);
            g2.drawString("Chưa có dữ liệu cho khoảng thời gian này", getWidth()/2 - 120, getHeight()/2);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int padding = 50;
        int graphHeight = height - 2 * padding;

        int numberOfBars = values.size();
        int slotWidth = (width - 2 * padding) / numberOfBars;
        int maxBarWidth = 80;
        int barWidth = Math.min(slotWidth - 20, maxBarWidth);
        if (barWidth < 10) barWidth = 10;

        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
        g2.drawLine(padding, height - padding, padding, padding); // Y-axis

        double scaleFactor = 0.75;

        for (int i = 0; i < numberOfBars; i++) {
            double val = values.get(i);
            int barHeight = (maxValue > 0) ? (int) ((val / maxValue) * graphHeight * scaleFactor) : 0;
            if (barHeight < 2 && val > 0) barHeight = 2;

            int x = padding + (i * slotWidth) + (slotWidth - barWidth) / 2;
            int y = height - padding - barHeight;

            g2.setColor(new Color(46, 204, 113));
            g2.fillRect(x, y, barWidth, barHeight);
            g2.setColor(new Color(39, 174, 96));
            g2.drawRect(x, y, barWidth, barHeight);

            String priceStr;
            if (val >= 1000000) {
                double tr = val / 1000000.0;
                priceStr = (tr == (long) tr) ? String.format("%d Tr", (long)tr) : String.format("%.1f Tr", tr);
            } else {
                priceStr = String.format("%d K", (long)(val / 1000));
            }
            g2.setFont(fontValue);
            g2.setColor(Color.DARK_GRAY);
            int strWidth = g2.getFontMetrics().stringWidth(priceStr);
            g2.drawString(priceStr, x + (barWidth - strWidth) / 2, y - 5);

            g2.setFont(fontDate);
            g2.setColor(Color.DARK_GRAY);
            String dateStr = dates.get(i);
            int dateWidth = g2.getFontMetrics().stringWidth(dateStr);
            g2.drawString(dateStr, x + (barWidth - dateWidth) / 2, height - padding + 20);
        }
    }
}