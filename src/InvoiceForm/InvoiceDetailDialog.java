package InvoiceForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import static JDBCUntils.Style.*;

public class InvoiceDetailDialog extends JDialog {
    private int invID;
    private JTable tableDetails;
    private DefaultTableModel model;
    private JLabel lblTitle, lblInfo, lblTotal;

    public InvoiceDetailDialog(Frame parent, int invID) {
        super(parent, true); // Modal = true
        this.invID = invID;

        setTitle("Chi Tiết Hóa Đơn #" + invID);
        initUI();
        loadData();

        setSize(600, 500); // Kích thước cửa sổ
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Header (Tiêu đề + Thông tin chung)
        JPanel pHeader = new JPanel(new BorderLayout(5, 5));
        pHeader.setBackground(Color.WHITE);

        lblTitle = createHeaderLabel("HÓA ĐƠN #" + invID);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        lblInfo = new JLabel("Đang tải thông tin...");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

        pHeader.add(lblTitle, BorderLayout.NORTH);
        pHeader.add(lblInfo, BorderLayout.CENTER);

        mainPanel.add(pHeader, BorderLayout.NORTH);

        // 2. Bảng chi tiết sản phẩm
        String[] columns = {"Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Thành Tiền"};
        model = new DefaultTableModel(columns, 0);
        tableDetails = new JTable(model);
        tableDetails.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableDetails.setRowHeight(30);

        // Căn phải cho các cột số tiền
        // (Phần này có thể làm kỹ hơn bằng Renderer nếu cần)

        mainPanel.add(new JScrollPane(tableDetails), BorderLayout.CENTER);

        // 3. Footer (Tổng tiền & Nút đóng)
        JPanel pFooter = new JPanel(new BorderLayout());
        pFooter.setBackground(Color.WHITE);
        pFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTotal = new JLabel("Tổng cộng: 0 VND");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(Color.decode("#c0392b")); // Màu đỏ đậm
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(Color.decode("#95a5a6"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        pFooter.add(lblTotal, BorderLayout.CENTER);
        pFooter.add(btnClose, BorderLayout.EAST);

        mainPanel.add(pFooter, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadData() {
        try (Connection con = DBConnection.getConnection()) {
            DecimalFormat df = new DecimalFormat("#,###");

            // 1. Lấy thông tin chung (Khách hàng, Nhân viên)
            String sqlInfo = "SELECT c.cus_name, s.sta_name, i.inv_price " +
                    "FROM Invoices i " +
                    "LEFT JOIN Customers c ON i.cus_ID = c.cus_ID " +
                    "LEFT JOIN Staffs s ON i.sta_ID = s.sta_ID " +
                    "WHERE i.inv_ID = ?";
            PreparedStatement psInfo = con.prepareStatement(sqlInfo);
            psInfo.setInt(1, invID);
            ResultSet rsInfo = psInfo.executeQuery();

            if (rsInfo.next()) {
                String cus = rsInfo.getString("cus_name");
                String sta = rsInfo.getString("sta_name");
                double total = rsInfo.getDouble("inv_price");

                lblInfo.setText("<html>Khách hàng: <b>" + cus + "</b> &nbsp;|&nbsp; Nhân viên: <b>" + sta + "</b></html>");
                lblTotal.setText("Tổng tiền: " + df.format(total) + " VND");
            }

            // 2. Lấy danh sách sản phẩm (JOIN 2 bảng: Invoice_details & Products)
            String sqlDetails = "SELECT p.pro_name, p.pro_price, d.ind_count " +
                    "FROM Invoice_details d " +
                    "JOIN Products p ON d.pro_ID = p.pro_ID " +
                    "WHERE d.inv_ID = ?";
            PreparedStatement psDetail = con.prepareStatement(sqlDetails);
            psDetail.setInt(1, invID);
            ResultSet rsDetail = psDetail.executeQuery();

            model.setRowCount(0);
            while (rsDetail.next()) {
                String name = rsDetail.getString("pro_name");
                double price = rsDetail.getDouble("pro_price");
                int count = rsDetail.getInt("ind_count");
                double subTotal = price * count;

                model.addRow(new Object[]{
                        name,
                        df.format(price),
                        count,
                        df.format(subTotal)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + e.getMessage());
        }
    }
}