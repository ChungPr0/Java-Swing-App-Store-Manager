package CustomerForm;

import JDBCUntils.DBConnection;
//import CustomerForm.AddCustomerForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

import static JDBCUntils.Style.*;

public class CustomerManagerPanel extends JPanel {
    // --- KHAI BÁO BIẾN ---
    private JList<String> listCustomer;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JButton btnAdd, btnSave, btnDelete;

    private String originalName;
    private boolean isDataLoading = false;

    public CustomerManagerPanel() {
        initUI();           // Vẽ giao diện
        loadListData();     // Tải danh sách
        addEvents();        // Gán sự kiện
        addChangeListeners(); // Gán sự kiện hiện nút Lưu
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Trái: Tìm kiếm + Danh sách
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        txtSearch = new JTextField();
        JPanel searchPanel = createTextFieldWithPlaceholder(txtSearch,"Tìm kiếm");
        btnAdd = createButton("Thêm", Color.GRAY);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnAdd, BorderLayout.EAST);

        listCustomer = new JList<>();
        listCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listCustomer.setFixedCellHeight(30);

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(listCustomer), BorderLayout.CENTER);

        // 2. Panel Phải: Form thông tin
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        rightPanel.add(createHeaderLabel("THÔNG TIN KHÁCH HÀNG"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        JPanel pName = createTextFieldWithLabel(txtName, "Tên Khách Hàng:");
        rightPanel.add(pName);
        rightPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        JPanel pPhone = createTextFieldWithLabel(txtPhone, "Số điện thoại:");
        rightPanel.add(pPhone);
        rightPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        JPanel pAddress = createTextFieldWithLabel(txtAddress, "Địa chỉ:");
        rightPanel.add(pAddress);
        rightPanel.add(Box.createVerticalStrut(15));


        // Panel Nút bấm (Lưu / Xóa)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = createButton("Lưu thay đổi", Color.GREEN);
        btnDelete = createButton("Xóa khách hàng", Color.RED);

        // Mặc định ẩn
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        rightPanel.add(buttonPanel);

        // Ghép vào Panel chính
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);

        enableForm(false);
    }

    // --- PHẦN LOGIC DỮ LIỆU ---
    private void loadListData() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT cus_name FROM Customers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("cus_name"));
            }
            listCustomer.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadDetail(String name) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Customers WHERE cus_name = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("cus_name"));
                txtPhone.setText(rs.getString("cus_phone"));
                txtAddress.setText(rs.getString("cus_address"));

                enableForm(true);
                btnDelete.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
        finally {
            btnSave.setVisible(false);
            isDataLoading = false;
        }
    }

    // --- PHẦN SỰ KIỆN ---
    private void addEvents() {
        listCustomer.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listCustomer.getSelectedValue();
                if (selected != null) {
                    originalName = selected;
                    loadDetail(selected);
                }
            }
        });

        // 2. Tìm kiếm
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }

            private void doSearch() {
                String key = txtSearch.getText().trim();
                if (key.isEmpty() || key.equals("Tìm kiếm...")) {
                    loadListData();
                } else {
                    search(key);
                }
            }
        });

        // 3. Nút Thêm
        btnAdd.addActionListener(e -> {
//            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
//            AddCustomerForm addCustomerForm = new AddCustomerForm(parentFrame);
//            addCustomerForm.setVisible(true);
//
//            if (addCustomerForm.isAddedSuccess()) {
//                loadListData();
//            }
        });

        // 4. Nút Lưu
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                String sql = "UPDATE Customers SET cus_name=?, cus_phone=?, cus_address=? WHERE cus_name=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, txtPhone.getText());
                ps.setString(3, txtAddress.getText());
                ps.setString(4, originalName);

                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    originalName = txtName.getText();
                    loadListData();
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        // 5. Nút Xóa
        btnDelete.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Xóa " + originalName + "?") == JOptionPane.YES_OPTION){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Customers WHERE cus_name=?");
                    ps.setString(1, originalName);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                }
            }
        });
    }

    private void search(String keyword) {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT cus_name FROM Customers WHERE cus_name LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("cus_name"));
            }
            listCustomer.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addChangeListeners() {
        CustomerForm.CustomerManagerPanel.SimpleDocumentListener docListener = new CustomerForm.CustomerManagerPanel.SimpleDocumentListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
    }

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");
        btnSave.setVisible(false); btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPhone.setEnabled(enable);
        txtAddress.setEnabled(enable);
    }


    @FunctionalInterface
    interface DocumentUpdateListener { void update(DocumentEvent e); }

    static class SimpleDocumentListener implements DocumentListener {
        private final CustomerForm.CustomerManagerPanel.DocumentUpdateListener listener;
        public SimpleDocumentListener(CustomerForm.CustomerManagerPanel.DocumentUpdateListener listener) { this.listener = listener; }
        public void insertUpdate(DocumentEvent e) { listener.update(e); }
        public void removeUpdate(DocumentEvent e) { listener.update(e); }
        public void changedUpdate(DocumentEvent e) { listener.update(e); }
    }
}
