package StaffForm;

import JDBCUtils.ComboItem;
import JDBCUtils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

import static JDBCUtils.Style.*;

public class StaffManagerPanel extends JPanel {
    // --- 1. KHAI BÁO BIẾN GIAO DIỆN (UI COMPONENTS) ---
    private JList<ComboItem> listStaff;
    private JTextField txtSearch, txtName, txtPhone, txtAddress, txtUsername, txtPassword;
    private JCheckBox chkIsAdmin;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JButton btnAdd, btnSave, btnDelete;
    private JButton btnSort;

    // --- 2. KHAI BÁO BIẾN TRẠNG THÁI (STATE VARIABLES) ---
    private int currentSortIndex = 0;
    private final String[] sortModes = {"A-Z", "Z-A", "NEW", "OLD"};
    private int selectedStaffID = -1;      // ID nhân viên đang chọn
    private boolean isDataLoading = false; // Cờ chặn sự kiện khi đang đổ dữ liệu

    public StaffManagerPanel() {
        initUI();               // Khởi tạo giao diện
        initComboBoxData();     // Nạp dữ liệu ngày tháng năm
        loadListData();         // Tải danh sách nhân viên
        addEvents();            // Gán sự kiện cho các nút
        addChangeListeners();   // Gán sự kiện theo dõi thay đổi để hiện nút Lưu
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // A. PANEL TRÁI (Tìm kiếm & Danh sách)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        txtSearch = new JTextField();
        btnSort = new JButton("A-Z");
        btnSort.setToolTipText("Đang xếp: Tên A-Z");
        btnSort.setFocusable(false);

        JPanel searchPanel = createSearchWithButtonPanel(txtSearch, btnSort, "Tìm kiếm");
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        listStaff = new JList<>();
        listStaff.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listStaff.setFixedCellHeight(30);
        leftPanel.add(new JScrollPane(listStaff), BorderLayout.CENTER);

        // B. PANEL PHẢI (Form thông tin chi tiết)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        rightPanel.add(createHeaderLabel("THÔNG TIN NHÂN VIÊN"));
        rightPanel.add(Box.createVerticalStrut(20));

        txtName = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtName, "Tên Nhân Viên:"));
        rightPanel.add(Box.createVerticalStrut(15));

        // Khu vực Ngày sinh và Vai trò (nằm cùng 1 hàng)
        cbDay = new JComboBox<>();
        cbMonth = new JComboBox<>();
        cbYear = new JComboBox<>();
        JPanel datePanel = createDatePanel("Ngày sinh:", cbDay, cbMonth, cbYear);

        chkIsAdmin = new JCheckBox();
        JPanel pRoleWrapper = createCheckBoxWithLabel(chkIsAdmin, "Vai trò:", "QUẢN TRỊ VIÊN");

        JPanel rowDateAndRole = new JPanel(new GridLayout(1, 2, 15, 0));
        rowDateAndRole.setBackground(Color.WHITE);
        rowDateAndRole.add(datePanel);
        rowDateAndRole.add(pRoleWrapper);
        rowDateAndRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        rightPanel.add(rowDateAndRole);
        rightPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtUsername = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtUsername, "Tài khoản đăng nhập:"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtPassword = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPassword, "Mật khẩu:"));
        rightPanel.add(Box.createVerticalStrut(15));

        // C. KHU VỰC BUTTON (Thêm, Lưu, Xóa)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = createButton("Thêm nhân viên", Color.decode("#3498db"));
        btnSave = createButton("Lưu thay đổi", new Color(46, 204, 113));
        btnDelete = createButton("Xóa nhân viên", new Color(231, 76, 60));

        btnSave.setVisible(false);
        btnDelete.setVisible(false);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);

        rightPanel.add(buttonPanel);

        // Tạo Scroll cho Panel phải để tránh bị tràn màn hình nếu màn hình nhỏ
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setBorder(null);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightScrollPane, BorderLayout.CENTER);

        enableForm(false); // Mặc định khóa form
    }

    // --- 4. TẢI DỮ LIỆU DANH SÁCH (LOAD LIST) ---
    private void loadListData() {
        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String keyword = txtSearch.getText().trim();
        boolean isSearching = !keyword.isEmpty() && !keyword.equals("Tìm kiếm...");

        try (Connection con = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT sta_id, sta_name FROM Staffs");

            if (isSearching) {
                sql.append(" WHERE sta_name LIKE ?");
            }

            // Xử lý sắp xếp
            switch (currentSortIndex) {
                case 0: sql.append(" ORDER BY sta_name ASC"); break;  // A-Z
                case 1: sql.append(" ORDER BY sta_name DESC"); break; // Z-A
                case 2: sql.append(" ORDER BY sta_id DESC"); break;   // Mới nhất
                case 3: sql.append(" ORDER BY sta_id ASC"); break;    // Cũ nhất
                default: sql.append(" ORDER BY sta_name ASC");
            }

            PreparedStatement ps = con.prepareStatement(sql.toString());

            if (isSearching) {
                ps.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("sta_id");
                String name = rs.getString("sta_name");
                model.addElement(new ComboItem(name, id));
            }
            listStaff.setModel(model);
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        }
    }

    // --- 5. TẢI CHI TIẾT NHÂN VIÊN (LOAD DETAIL) ---
    private void loadDetail(int id) {
        isDataLoading = true; // Chặn sự kiện text change
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Đổ dữ liệu vào TextFields
                txtName.setText(rs.getString("sta_name"));
                txtPhone.setText(rs.getString("sta_phone"));
                txtAddress.setText(rs.getString("sta_address"));
                txtUsername.setText(rs.getString("sta_username"));
                txtPassword.setText(rs.getString("sta_password"));

                // Xử lý Checkbox Admin
                String role = rs.getString("sta_role");
                chkIsAdmin.setSelected(role != null && role.equalsIgnoreCase("Admin"));

                // Xử lý ComboBox Ngày sinh
                Date sqlDate = rs.getDate("sta_date_of_birth");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    cbYear.setSelectedItem(parts[0]);
                    cbMonth.setSelectedItem(parts[1]);
                    cbDay.setSelectedItem(parts[2]);
                }

                enableForm(true);
                btnDelete.setVisible(true);
                btnSave.setVisible(false); // Ẩn nút lưu khi mới load lên
                btnAdd.setVisible(true);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false; // Mở lại sự kiện
        }
    }

    // --- 6. XỬ LÝ CÁC SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // Sự kiện chọn nhân viên trong danh sách
        listStaff.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listStaff.getSelectedValue();
                if (selected != null) {
                    selectedStaffID = selected.getValue();
                    loadDetail(selectedStaffID);
                }
            }
        });

        // Sự kiện tìm kiếm
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        // --- ĐOẠN NÀY ĐỂ CHẶN NHẬP CHỮ VÀO SỐ ĐIỆN THOẠI ---
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
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
                case 2: btnSort.setToolTipText("Đang xếp: Nhân viên mới vào"); break;
                case 3: btnSort.setToolTipText("Đang xếp: Nhân viên lâu năm"); break;
            }

            loadListData();
        });

        // Sự kiện nút Thêm Mới
        btnAdd.addActionListener(_ -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddStaffForm addStaffForm = new AddStaffForm(parentFrame);
            addStaffForm.setVisible(true);

            if (addStaffForm.isAddedSuccess()) {
                loadListData();
            }
        });

        // Sự kiện nút Lưu Thay Đổi (Update)
        btnSave.addActionListener(_ -> {
            // 1. Kiểm tra rỗng trước
            if (txtUsername.getText().trim().isEmpty()) {
                showError(this, "Tên đăng nhập không được để trống!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // --- BƯỚC 1: KIỂM TRA TRÙNG TÊN ĐĂNG NHẬP ---
                // Logic: Tìm xem có ai KHÁC (sta_id != currentID) đang dùng username này không
                String checkSql = "SELECT COUNT(*) FROM Staffs WHERE sta_username = ? AND sta_id != ?";
                PreparedStatement psCheck = con.prepareStatement(checkSql);
                psCheck.setString(1, txtUsername.getText().trim());
                psCheck.setInt(2, selectedStaffID); // Loại trừ chính nhân viên này ra

                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    showError(this, "Tài khoản '" + txtUsername.getText() + "' đã được sử dụng bởi người khác!");
                    return; // Dừng lại, không Lưu nữa
                }

                // --- BƯỚC 2: NẾU KHÔNG TRÙNG THÌ MỚI CẬP NHẬT ---
                String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();

                String sql = "UPDATE Staffs SET sta_name=?, sta_date_of_birth=?, sta_phone=?, sta_address=?, sta_username=?, sta_password=?, sta_role=? WHERE sta_id=?";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, strDate);
                ps.setString(3, txtPhone.getText().trim());
                ps.setString(4, txtAddress.getText().trim());
                ps.setString(5, txtUsername.getText().trim());
                ps.setString(6, txtPassword.getText().trim());
                ps.setString(7, chkIsAdmin.isSelected() ? "Admin" : "Staff");

                ps.setInt(8, selectedStaffID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();
                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        // Sự kiện nút Xóa (Delete)
        btnDelete.addActionListener(_ -> {
            if(showConfirm(this, "Xóa nhân viên này?")){
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM Staffs WHERE sta_id=?");
                    ps.setInt(1, selectedStaffID);
                    if (ps.executeUpdate() > 0) {
                        loadListData();
                        clearForm();
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("foreign key")) {
                        showError(this, "Không thể xóa nhân viên này vì họ đã từng lập hóa đơn bán hàng!");
                    } else {
                        showError(this, "Lỗi: " + ex.getMessage());
                    }
                }
            }
        });
    }

    // --- 7. SỰ KIỆN THEO DÕI THAY ĐỔI FORM ---
    private void addChangeListeners() {
        SimpleDocumentListener docListener = new SimpleDocumentListener(_ -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        // Lắng nghe tất cả các trường text
        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
        txtUsername.getDocument().addDocumentListener(docListener);
        txtPassword.getDocument().addDocumentListener(docListener);

        // Lắng nghe Checkbox và ComboBox
        chkIsAdmin.addActionListener(_ -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        java.awt.event.ActionListener actionListener = _ -> {
            if (!isDataLoading) btnSave.setVisible(true);
        };
        cbDay.addActionListener(actionListener);
        cbMonth.addActionListener(actionListener);
        cbYear.addActionListener(actionListener);
    }

    // --- CÁC HÀM TIỆN ÍCH KHÁC ---

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");
        txtUsername.setText(""); txtPassword.setText(""); chkIsAdmin.setSelected(false);

        btnSave.setVisible(false);
        btnDelete.setVisible(false);
        enableForm(false);
        isDataLoading = false;
    }

    private void enableForm(boolean enable) {
        txtName.setEnabled(enable);
        txtPhone.setEnabled(enable);
        txtAddress.setEnabled(enable);
        cbDay.setEnabled(enable);
        cbMonth.setEnabled(enable);
        cbYear.setEnabled(enable);
        txtUsername.setEnabled(enable);
        txtPassword.setEnabled(enable);
        chkIsAdmin.setEnabled(enable);
    }

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.format("%02d", i));
        for (int i = 1; i <= 12; i++) cbMonth.addItem(String.format("%02d", i));
        for (int i = 2025; i >= 1960; i--) cbYear.addItem(String.valueOf(i));
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