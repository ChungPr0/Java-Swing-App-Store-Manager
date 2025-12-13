package ProductForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static JDBCUntils.Style.*;

public class AddProductForm extends JDialog {
    private JTextField txtName, txtPrice, txtCount;
    private JComboBox<String> cbType, cbSupplier;
    private JButton btnSave, btnCancel;

    private boolean isAdded = false;

    public AddProductForm(Frame parent) {
        super(parent, true); // Modal = true (chặn form cha)
        this.setTitle("Thêm Sản Phẩm Mới"); // Đổi tiêu đề
        initUI();
        loadTypeData();
        loadSupplierData();
        addEvents();

        this.pack(); // Tự động co giãn kích thước vừa với nội dung
        this.setLocationRelativeTo(parent); // Hiện giữa form cha
        this.setResizable(false); // Không cho kéo giãn lung tung
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = createHeaderLabel("NHẬP THÔNG TIN");
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        JPanel pName = createTextFieldWithLabel(txtName, "Tên Sản Phẩm:");
        mainPanel.add(pName);
        mainPanel.add(Box.createVerticalStrut(15));

        txtPrice = new JTextField();
        JPanel pPrice = createTextFieldWithLabel(txtPrice, "Giá Bán (VND):");
        mainPanel.add(pPrice);
        mainPanel.add(Box.createVerticalStrut(15));

        txtCount = new JTextField();
        JPanel pCount = createTextFieldWithLabel(txtCount, "Số Lượng Tồn:");
        mainPanel.add(pCount);
        mainPanel.add(Box.createVerticalStrut(15));

        cbType = new JComboBox<>();
        JPanel pType = createComboBoxWithLabel(cbType,"Phân Loại:");
        mainPanel.add(pType);
        mainPanel.add(Box.createVerticalStrut(15));

        cbSupplier = new JComboBox<>();
        JPanel pSupplier = createComboBoxWithLabel(cbSupplier, "Nhà Cung Cấp:");
        mainPanel.add(pSupplier);
        mainPanel.add(Box.createVerticalStrut(15));


        // 3. Các nút bấm (Lưu / Hủy)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);
    }

    // --- CÁC HÀM LOGIC ---
    private void loadTypeData() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT type_name FROM ProductTypes";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            cbType.removeAllItems();
            while (rs.next()) {
                cbType.addItem(rs.getString("type_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(AddProductForm.this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadSupplierData() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            cbSupplier.removeAllItems();
            while (rs.next()) {
                cbSupplier.addItem(rs.getString("sup_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(AddProductForm.this, "Lỗi: " + e.getMessage());
        }
    }

    private void addEvents() {
        // Nút Lưu
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (txtName.getText().trim().isEmpty() ||
                        txtPrice.getText().trim().isEmpty() ||
                        txtCount.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(AddProductForm.this, "Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                try (Connection con = DBConnection.getConnection()) {
                    String selectedTypeName = (String) cbType.getSelectedItem();
                    int typeID = 0;

                    if (selectedTypeName != null) {
                        String sqlGetTypeID = "SELECT type_ID FROM ProductTypes WHERE type_name = ?";
                        PreparedStatement psType = con.prepareStatement(sqlGetTypeID);
                        psType.setString(1, selectedTypeName);
                        ResultSet rsType = psType.executeQuery();
                        if (rsType.next()) {
                            typeID = rsType.getInt("type_ID");
                        }
                    }

                    String selectedSupName = (String) cbSupplier.getSelectedItem();
                    int supID = 0;

                    if (selectedSupName != null) {
                        String sqlGetSupplierID = "SELECT sup_ID FROM Suppliers WHERE sup_name = ?";
                        PreparedStatement psID = con.prepareStatement(sqlGetSupplierID);
                        psID.setString(1, selectedSupName);
                        ResultSet rsID = psID.executeQuery();
                        if (rsID.next()) {
                            supID = rsID.getInt("sup_ID");
                        }
                    }


                    String sql = "INSERT INTO Products (pro_name, pro_price, pro_count, type_ID, sup_ID) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, txtName.getText().trim());
                    ps.setDouble(2, Double.parseDouble(txtPrice.getText().trim()));
                    ps.setInt(3, Integer.parseInt(txtCount.getText().trim()));
                    ps.setInt(4, typeID);
                    ps.setInt(5, supID);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(AddProductForm.this, "Thêm sản phẩm thành công!");
                        isAdded = true;
                        dispose();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(AddProductForm.this, "Lỗi: Giá, Số lượng và ID phải là số hợp lệ!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddProductForm.this, "Lỗi CSDL: " + ex.getMessage());
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    public boolean isAddedSuccess() {
        return isAdded;
    }
}
