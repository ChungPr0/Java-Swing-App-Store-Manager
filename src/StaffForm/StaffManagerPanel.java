package StaffForm;

import JDBCUntils.DBConnection;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

import static JDBCUntils.Functions.*;

public class StaffManagerPanel extends JPanel {
    // --- KHAI BÁO BIẾN ---
    private JList<String> listStaff;
    private DefaultListModel<String> listModel;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnAdd, btnSave, btnDelete;

    private String originalName;
    private boolean isDataLoading = false;

    public StaffManagerPanel() {
        initUI();           // Vẽ giao diện
        initComboBoxData(); // Nạp ngày tháng
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

        JPanel searchPanel = new JPanel(new BorderLayout());
        txtSearch = new JTextField(); addPlaceholder(txtSearch);
        btnAdd = createButton("+", Color.CYAN);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnAdd, BorderLayout.EAST);

        listStaff = new JList<>();

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(listStaff), BorderLayout.CENTER);

        // 2. Panel Phải: Form thông tin
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));

        txtName = createPrettyField(rightPanel, "Họ và Tên:");

        // --- PHẦN NGÀY SINH---
        // Tạo Panel chứa (Container)
        JPanel dobContainer = new JPanel(new BorderLayout(5, 5));
        dobContainer.setBackground(Color.WHITE);
        dobContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Tạo Label (Tiêu đề)
        JLabel lblDob = createTitleLabel("Ngày sinh:");

        // Tạo Panel chứa 3 cái ComboBox (Xếp ngang)
        JPanel pickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pickerPanel.setBackground(Color.WHITE);

        // 5. Thêm vào Panel (Kèm dấu gạch chéo / cho đẹp)
        cbDay = createStyleComboBox();
        cbMonth = createStyleComboBox();
        cbYear = createStyleComboBox();

        pickerPanel.add(cbDay);
        pickerPanel.add(createSeparator());
        pickerPanel.add(cbMonth);
        pickerPanel.add(createSeparator());
        pickerPanel.add(cbYear);

        // 6. Ghép lại
        dobContainer.add(lblDob, BorderLayout.NORTH);
        dobContainer.add(pickerPanel, BorderLayout.CENTER);

        // 7. Thêm vào Panel chính
        rightPanel.add(dobContainer);
        rightPanel.add(Box.createVerticalStrut(15));

        txtPhone = createPrettyField(rightPanel, "Số điện thoại:");
        txtAddress = createPrettyField(rightPanel, "Địa chỉ:");

        // Panel Nút bấm (Lưu / Xóa)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = createButton("Lưu thay đổi", Color.GREEN);
        btnDelete = createButton("Xóa nhân viên", Color.RED);

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
            String sql = "SELECT sta_name FROM staffs";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sta_name"));
            }
            listStaff.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void loadDetail(String name) {
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_name = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("sta_name"));
                txtPhone.setText(rs.getString("sta_phone"));
                txtAddress.setText(rs.getString("sta_address"));

                Date sqlDate = rs.getDate("sta_date_of_birth");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    cbYear.setSelectedItem(parts[0]);
                    cbMonth.setSelectedItem(parts[1]);
                    cbDay.setSelectedItem(parts[2]);
                }

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
        // 1. load nhân viên khi chọn
        listStaff.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listStaff.getSelectedValue();
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
            AddStaffForm addStaffForm = new AddStaffForm(parentFrame);
            addStaffForm.setVisible(true);

            if (addStaffForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // 4. Nút Lưu
        btnSave.addActionListener(e -> {
            try (Connection con = DBConnection.getConnection()) {
                String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
                String sql = "UPDATE Staffs SET sta_name=?, sta_date_of_birth=?, sta_phone=?, sta_address=? WHERE sta_name=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, strDate);
                ps.setString(3, txtPhone.getText());
                ps.setString(4, txtAddress.getText());
                ps.setString(5, originalName);

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
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Staffs WHERE sta_name=?");
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
            String sql = "SELECT sta_name FROM Staffs WHERE sta_name LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sta_name"));
            }
            listStaff.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);

        java.awt.event.ActionListener actionListener = e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        };
        cbDay.addActionListener(actionListener);
        cbMonth.addActionListener(actionListener);
        cbYear.addActionListener(actionListener);
    }

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");
        btnSave.setVisible(false); btnDelete.setVisible(false);
        isDataLoading = false;
    }

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) cbMonth.addItem(String.format("%02d", i));
        for (int i = 2025; i >= 1960; i--) cbYear.addItem(String.valueOf(i));
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