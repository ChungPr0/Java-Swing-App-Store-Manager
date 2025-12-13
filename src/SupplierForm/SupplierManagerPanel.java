package SupplierForm;

import JDBCUntils.DBConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

import static JDBCUntils.Functions.*;

public class SupplierManagerPanel extends JPanel {
    private JList<String> listSupplier;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JButton btnAdd, btnSave, btnDelete;

    private String originalName;
    private boolean isDataLoading = false;

    public SupplierManagerPanel() {
        initUI();           //Vẽ giao diện
        loadListData();     //Tải danh sách
        addEvents();        //Gán sự kiện
        addChangeListeners(); //Gán sự kiện hiện nút Lưu
    }

    // --- PHẦN GIAO DIỆN (UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Trái: Tìm kiếm + Danh sách
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        JPanel searchPanel = new JPanel(new BorderLayout());
        txtSearch = new JTextField(); addPlaceholder(txtSearch);
        btnAdd = createButton("+", Color.ORANGE);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnAdd, BorderLayout.EAST);

        listSupplier = new JList<>();

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(listSupplier), BorderLayout.CENTER);

        // 2. Panel Phải: Form thông tin
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));

        txtName = createPrettyField(rightPanel, "Họ và Tên:");
        txtPhone = createPrettyField(rightPanel, "Số điện thoại:");
        txtAddress = createPrettyField(rightPanel, "Địa chỉ:");

        // Panel Nút bấm (Lưu / Xóa)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = createButton("Lưu thay đổi", Color.GREEN);
        btnDelete = createButton("Xóa Nhà Cung Cấp", Color.RED);

        // Mặc định ẩn
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        rightPanel.add(buttonPanel);

        // Ghép vào Panel chính
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);
    }

    // --- PHẦN LOGIC DỮ LIỆU ---
    private void loadListData() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT sup_name FROM Suppliers";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sup_name"));
            }
            listSupplier.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadDetail(String name) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Suppliers WHERE sup_name = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("sup_name"));
                txtPhone.setText(rs.getString("sup_phone"));
                txtAddress.setText(rs.getString("sup_address"));

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
        // 1. Chọn list
        listSupplier.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listSupplier.getSelectedValue();
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
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddSupplierForm addSupplierForm = new AddSupplierForm(parentFrame);
            addSupplierForm.setVisible(true);

            if (addSupplierForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // 4. Nút Lưu
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                String sql = "UPDATE Suppliers SET sup_name=?, sup_phone=?, sup_address=? WHERE sup_name=?";
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
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Suppliers WHERE sup_name=?");
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
            String sql = "SELECT sup_name FROM Suppliers WHERE sup_name LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sup_name"));
            }
            listSupplier.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());;
        }
    }

    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
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
        isDataLoading = false;
    }


    @FunctionalInterface
    interface DocumentUpdateListener { void update(DocumentEvent e); }

    static class SimpleDocumentListener implements DocumentListener {
        private final DocumentUpdateListener listener;
        public SimpleDocumentListener(DocumentUpdateListener listener) { this.listener = listener; }
        public void insertUpdate(DocumentEvent e) { listener.update(e); }
        public void removeUpdate(DocumentEvent e) { listener.update(e); }
        public void changedUpdate(DocumentEvent e) { listener.update(e); }
    }
}