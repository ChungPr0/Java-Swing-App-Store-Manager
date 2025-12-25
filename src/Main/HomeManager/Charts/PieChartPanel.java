package Main.HomeManager.Charts;

import Utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static Utils.Style.showError;

public class PieChartPanel extends JPanel {
    private final List<Slice> slices = new ArrayList<>();
    private final Color[] colors = {
            new Color(46, 204, 113), new Color(52, 152, 219), new Color(155, 89, 182),
            new Color(241, 196, 15), new Color(230, 126, 34), new Color(231, 76, 60), new Color(52, 73, 94)
    };

    private final ChartCanvas canvas;
    private final JPanel pLegend;
    private String currentPeriod;

    private final BiConsumer<String, String> onSliceClick;

    public PieChartPanel(BiConsumer<String, String> onSliceClick) {
        this.onSliceClick = onSliceClick;

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        canvas = new ChartCanvas();

        pLegend = new JPanel();
        pLegend.setLayout(new BoxLayout(pLegend, BoxLayout.Y_AXIS));
        pLegend.setBackground(Color.WHITE);
        pLegend.setBorder(new EmptyBorder(20, 10, 20, 10));

        this.add(canvas, BorderLayout.CENTER);
        this.add(pLegend, BorderLayout.EAST);
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

    public void loadPieData(String period) {
        this.currentPeriod = period;
        slices.clear();
        String dateFilter = getSqlDateFilter(period);

        String sql = "SELECT t.type_name, SUM(d.ind_count) as total " +
                "FROM Invoice_details d " +
                "JOIN Products p ON d.pro_ID = p.pro_ID " +
                "JOIN ProductTypes t ON p.type_ID = t.type_ID " +
                "JOIN Invoices i ON d.inv_ID = i.inv_ID " +
                "WHERE " + dateFilter + " " +
                "GROUP BY t.type_name ORDER BY total DESC LIMIT 7";

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(sql);
            int idx = 0;
            while (rs.next()) {
                String name = rs.getString("type_name");
                double val = rs.getDouble("total");
                slices.add(new Slice(name, val, colors[idx % colors.length]));
                idx++;
            }
            canvas.repaint();
            buildLegend();
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    private void buildLegend() {
        pLegend.removeAll();
        addLegendItem(null, "Tất cả", Color.GRAY);
        pLegend.add(Box.createVerticalStrut(10));

        for (Slice s : slices) {
            addLegendItem(s, s.name, s.color);
            pLegend.add(Box.createVerticalStrut(8));
        }
        pLegend.revalidate();
        pLegend.repaint();
    }

    private void addLegendItem(Slice s, String text, Color c) {
        JPanel pItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pItem.setBackground(Color.WHITE);
        pItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pColor = new JPanel();
        pColor.setPreferredSize(new Dimension(15, 15));
        pColor.setBackground(c);

        JLabel lblName = new JLabel(text);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pItem.add(pColor);
        pItem.add(lblName);

        pItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSliceClick != null) {
                    onSliceClick.accept(s == null ? "ALL" : s.name, currentPeriod);
                }
            }
        });
        pLegend.add(pItem);
    }

    class ChartCanvas extends JPanel {
        public ChartCanvas() {
            this.setBackground(Color.WHITE);
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    boolean clicked = false;
                    for (Slice s : slices) {
                        if (s.shape != null && s.shape.contains(e.getPoint())) {
                            if (onSliceClick != null) onSliceClick.accept(s.name, currentPeriod);
                            clicked = true;
                            break;
                        }
                    }
                    if (!clicked && onSliceClick != null) onSliceClick.accept("ALL", currentPeriod);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (slices.isEmpty()) {
                g2.drawString("Không có dữ liệu", getWidth()/2 - 40, getHeight()/2);
                return;
            }

            double totalValue = 0;
            for (Slice s : slices) totalValue += s.value;

            int minDim = Math.min(getWidth(), getHeight());
            int size = minDim - 20;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            double startAngle = 90;

            for (Slice s : slices) {
                double arcAngle = (s.value / totalValue) * 360;
                s.shape = new Arc2D.Double(x, y, size, size, startAngle, -arcAngle, Arc2D.PIE);

                g2.setColor(s.color);
                g2.fill(s.shape);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(s.shape);

                startAngle -= arcAngle;
            }

            g2.setColor(Color.WHITE);
            int innerSize = size / 2;
            int innerX = x + (size - innerSize) / 2;
            int innerY = y + (size - innerSize) / 2;
            g2.fillOval(innerX, innerY, innerSize, innerSize);
        }
    }
}