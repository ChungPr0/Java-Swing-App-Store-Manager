package SupplierForm;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

import static JDBCUtils.Style.*;

public class SupplierManagerPanel extends JPanel {
    // --- KHAI BÁO BIẾN GIAO DIỆN ---
    private JList<ComboItem> listSupplier;
    private JTextField txtSearch, txtName, txtPhone, txtAddress;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnSort;

    // --- BIẾN TRẠNG THÁI ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "NEW", "OLD"};
    private int selectedSupID = -1; // ID nhà cung cấp đang chọn
    private boolean isDataLoading = false; // Cờ chặn sự kiện khi đang load dữ liệu

    public SupplierManagerPanel() {
        initUI();           // 1. Tạo giao diện
        loadListData();     // 2. Tải danh sách NCC
        addEvents();        // 3. Gán sự kiện click/search
        addChangeListeners(); // 4. Gán sự kiện gõ phím để hiện nút Lưu
    }

    // --- 1. KHỞI TẠO GIAO DIỆN (UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(Color.decode("#ecf0f1"));

        // A. PANEL TRÁI (Tìm kiếm & Danh sách)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);

        txtSearch = new JTextField();
        btnSort = new JButton("A-Z");
        btnSort.setToolTipText("Đang xếp: Tên A-Z");

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listSupplier = new JList<>();
        listSupplier.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listSupplier.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listSupplier), BorderLayout.CENTER);

        // B. PANEL PHẢI (Form nhập liệu)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN NHÀ CUNG CẤP"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtName, "Tên Nhà Cung Cấp:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        rightPanel.add(Box.createVerticalStrut(15));

        // C. KHU VỰC NÚT BẤM
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Thêm nhà cung cấp", Color.decode("#3498db"));
        btnSave = createButton("Lưu thay đổi", new Color(46, 204, 113));
        btnDelete = createButton("Xóa nhà cung cấp", new Color(231, 76, 60));

        // Mặc định ẩn nút Lưu và Xóa khi chưa chọn ai
        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        rightPanel.add(buttonPanel);

        // Thêm 2 panel vào màn hình chính
        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.CENTER);

        enableForm(false); // Khóa các ô nhập liệu ban đầu
    }

    // --- 2. TẢI DỮ LIỆU DANH SÁCH (LEFT PANEL) ---
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();

        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT sup_id, sup_name FROM Suppliers");

            if (isSearching) {
                sql.append(" WHERE sup_name LIKE ?");
            }

            // Xử lý sắp xếp
            switch (currentSortIndex) {
                case 1: sql.append(" ORDER BY sup_name DESC"); break;
                case 2: sql.append(" ORDER BY sup_id DESC"); break; // Mới nhất
                case 3: sql.append(" ORDER BY sup_id ASC"); break;  // Cũ nhất
                default: sql.append(" ORDER BY sup_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());

            if (isSearching) {
                ps.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("sup_id");
                String name = rs.getString("sup_name");
                model.addElement(new ComboItem(name, id));
            }
            listSupplier.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    // --- 3. TẢI CHI TIẾT (RIGHT PANEL) ---
    private void loadDetail(int id) {
        isDataLoading = true; // Chặn sự kiện text change
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Suppliers WHERE sup_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtName.setText(rs.getString("sup_name"));
                txtPhone.setText(rs.getString("sup_phone"));
                txtAddress.setText(rs.getString("sup_address"));

                enableForm(true);
                btnDelete.setVisible(true);
                btnSave.setVisible(false);
                btnAdd.setVisible(true);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false; // Mở lại sự kiện
        }
    }

    // --- 4. XỬ LÝ CÁC SỰ KIỆN NÚT BẤM & CLICK ---
    private void addEvents() {
        // Sự kiện chọn vào danh sách bên trái
        listSupplier.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listSupplier.getSelectedValue();
                if (selected != null) {
                    selectedSupID = selected.getValue();
                    loadDetail(selectedSupID);
                }
            }
        });

        // Sự kiện tìm kiếm (Gõ đến đâu tìm đến đó)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        // --- ĐOẠN NÀY ĐỂ CHẶN NHẬP CHỮ VÀO SỐ ĐIỆN THOẠI ---
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                // Nếu ký tự gõ vào KHÔNG phải là số -> Chặn (consume)
                if (!Character.isDigit(c)) {
                    e.consume();
                }
            }
        });

        // Sự kiện nút Sắp xếp (Sort Loop)
        btnSort.addActionListener(_ -> {
            currentSortIndex++;
            if (currentSortIndex >= sortModes.length) {
                currentSortIndex = 0;
            }
            btnSort.setText(sortModes[currentSortIndex]);

            switch (currentSortIndex) {
                case 0: btnSort.setToolTipText("Đang xếp: Tên A -> Z"); break;
                case 1: btnSort.setToolTipText("Đang xếp: Tên Z -> A"); break;
                case 2: btnSort.setToolTipText("Đang xếp: Nhà cung cấp mới hợp tác"); break;
                case 3: btnSort.setToolTipText("Đang xếp: Nhà cung cấp lâu năm"); break;
            }

            loadListData();
        });

        // Sự kiện nút Thêm Mới
        btnAdd.addActionListener(_ -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddSupplierForm addSupplierForm = new AddSupplierForm(parentFrame);
            addSupplierForm.setVisible(true);

            if (addSupplierForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // Sự kiện nút Lưu Thay Đổi
        btnSave.addActionListener(_ -> {
            try (Connection con = DBConnection.getConnection()) {
                String sql = "UPDATE Suppliers SET sup_name=?, sup_phone=?, sup_address=? WHERE sup_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, txtPhone.getText());
                ps.setString(3, txtAddress.getText());
                ps.setInt(4, selectedSupID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        // Sự kiện nút Xóa (Có bắt lỗi khóa ngoại)
        btnDelete.addActionListener(_ -> {
            if(showConfirm(this, "Xóa nhà cung cấp này?")){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Suppliers WHERE sup_id=?");
                    ps.setInt(1, selectedSupID);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("foreign key")) {
                        showError(this, "Không thể xóa nhà cung cấp này vì họ đang cung cấp các sản phẩm trong kho!");
                    } else {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    // --- 5. SỰ KIỆN THEO DÕI THAY ĐỔI FORM ---
    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(_ -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");

        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPhone.setEnabled(enable);
        txtAddress.setEnabled(enable);
    }

    public void refreshData() {
        loadListData();
    }

    // Helper Interface & Class cho DocumentListener
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