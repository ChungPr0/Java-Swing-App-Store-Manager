package Main.StaffManager;

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
    private JTextField txtSearch, txtName, txtSalary, txtPhone, txtAddress, txtUsername, txtPassword;
    private JCheckBox chkIsAdmin;
    private JComboBox<String> cbDay, cbMonth, cbYear;
    private JComboBox<String> cbStartDay, cbStartMonth, cbStartYear;
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

        // Khu vực Ngày sinh và Vai trò
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

        // Khu vực Lương và Ngày vào làm
        JPanel rowSalaryAndStart = new JPanel(new GridLayout(1, 2, 15, 0));
        rowSalaryAndStart.setBackground(Color.WHITE);
        rowSalaryAndStart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        txtSalary = new JTextField();
        rowSalaryAndStart.add(createTextFieldWithLabel(txtSalary, "Lương cơ bản (VNĐ):"));

        cbStartDay = new JComboBox<>();
        cbStartMonth = new JComboBox<>();
        cbStartYear = new JComboBox<>();
        JPanel pStartDate = createDatePanel("Ngày vào làm:", cbStartDay, cbStartMonth, cbStartYear);
        rowSalaryAndStart.add(pStartDate);

        rightPanel.add(rowSalaryAndStart);
        rightPanel.add(Box.createVerticalStrut(15));

        txtUsername = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtUsername, "Tài khoản đăng nhập (Có thể để trống):"));
        rightPanel.add(Box.createVerticalStrut(15));

        txtPassword = new JTextField();
        rightPanel.add(createTextFieldWithLabel(txtPassword, "Mật khẩu (Có thể để trống):"));
        rightPanel.add(Box.createVerticalStrut(15));

        // C. KHU VỰC BUTTON
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

        // Tạo Scroll cho Panel phải
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setBorder(null);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        this.add(leftPanel, BorderLayout.WEST);
        this.add(rightScrollPane, BorderLayout.CENTER);

        enableForm(false);
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

            // Đổi logic sắp xếp theo Ngày vào làm
            switch (currentSortIndex) {
                case 0: sql.append(" ORDER BY sta_name ASC"); break;        // A-Z
                case 1: sql.append(" ORDER BY sta_name DESC"); break;       // Z-A
                case 2: sql.append(" ORDER BY sta_start_date DESC"); break; // Mới nhất (Ngày gần nhất lên đầu)
                case 3: sql.append(" ORDER BY sta_start_date ASC"); break;  // Cũ nhất (Ngày xa nhất lên đầu)
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
        isDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedStaffID = rs.getInt("sta_id"); // Gán ID

                txtName.setText(rs.getString("sta_name"));
                txtPhone.setText(rs.getString("sta_phone"));
                txtAddress.setText(rs.getString("sta_address"));

                double salary = rs.getDouble("sta_salary");
                txtSalary.setText(String.format("%.0f", salary));

                txtUsername.setText(rs.getString("sta_username"));
                txtPassword.setText(rs.getString("sta_password"));

                String role = rs.getString("sta_role");
                chkIsAdmin.setSelected(role != null && role.equalsIgnoreCase("Admin"));

                Date sqlDate = rs.getDate("sta_date_of_birth");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    cbYear.setSelectedItem(parts[0]);
                    cbMonth.setSelectedItem(parts[1]);
                    cbDay.setSelectedItem(parts[2]);
                }

                Date sqlStartDate = rs.getDate("sta_start_date");
                if (sqlStartDate != null) {
                    String[] parts = sqlStartDate.toString().split("-");
                    cbStartYear.setSelectedItem(parts[0]);
                    cbStartMonth.setSelectedItem(parts[1]);
                    cbStartDay.setSelectedItem(parts[2]);
                }

                enableForm(true);
                btnDelete.setVisible(true);
                btnSave.setVisible(false);
                btnAdd.setVisible(true);
            }
        } catch (Exception e) {
            showError(this, "Lỗi: " + e.getMessage());
        } finally {
            isDataLoading = false;
        }
    }

    // --- 6. XỬ LÝ CÁC SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        listStaff.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComboItem selected = listStaff.getSelectedValue();
                if (selected != null) {
                    selectedStaffID = selected.getValue();
                    loadDetail(selectedStaffID);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadListData(); }
            public void removeUpdate(DocumentEvent e) { loadListData(); }
            public void changedUpdate(DocumentEvent e) { loadListData(); }
        });

        java.awt.event.KeyAdapter digitOnly = new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        };
        txtPhone.addKeyListener(digitOnly);
        txtSalary.addKeyListener(digitOnly);

        // Sự kiện nút Sắp xếp
        btnSort.addActionListener(e -> {
            currentSortIndex++;
            if (currentSortIndex >= sortModes.length) {
                currentSortIndex = 0;
            }
            btnSort.setText(sortModes[currentSortIndex]);

            switch (currentSortIndex) {
                case 0: btnSort.setToolTipText("Đang xếp: Tên A -> Z"); break;
                case 1: btnSort.setToolTipText("Đang xếp: Tên Z -> A"); break;
                case 2: btnSort.setToolTipText("Đang xếp: Nhân viên mới vào làm (Gần đây nhất)"); break;
                case 3: btnSort.setToolTipText("Đang xếp: Nhân viên làm lâu năm (Thâm niên)"); break;
            }

            loadListData();
        });

        // Nút Thêm Mới: Thêm xong tự chọn nhân viên mới
        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddStaffDialog addStaffDialog = new AddStaffDialog(parentFrame);
            addStaffDialog.setVisible(true);

            if (addStaffDialog.isAddedSuccess()) {
                // 1. Xóa tìm kiếm
                txtSearch.setText("");

                // 2. Tải lại danh sách
                loadListData();

                // 3. Lấy ID mới và chọn
                int newID = addStaffDialog.getNewStaffID();
                if (newID != -1) {
                    selectStaffByID(newID);
                }
            }
        });

        // Nút Lưu Thay Đổi: Lưu xong giữ nguyên lựa chọn
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() ||
                    txtPhone.getText().trim().isEmpty() ||
                    txtAddress.getText().trim().isEmpty() ||
                    txtSalary.getText().trim().isEmpty()) {

                showError(this, "Vui lòng nhập đầy đủ thông tin: Tên, SĐT, Địa chỉ và Lương!");
                return;
            }

            if (txtUsername.getText().trim().isEmpty() && !txtPassword.getText().trim().isEmpty()) {
                showError(this, "Vui lòng nhập tài khoản nếu có mật khẩu!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String user = txtUsername.getText().trim();
                String pass = txtPassword.getText().trim();

                if (!user.isEmpty()) {
                    String checkSql = "SELECT COUNT(*) FROM Staffs WHERE sta_username = ? AND sta_id != ?";
                    PreparedStatement psCheck = con.prepareStatement(checkSql);
                    psCheck.setString(1, user);
                    psCheck.setInt(2, selectedStaffID);

                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        showError(this, "Tài khoản '" + user + "' đã được sử dụng bởi người khác!");
                        return;
                    }
                }

                String dob = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();
                String startDate = cbStartYear.getSelectedItem() + "-" + cbStartMonth.getSelectedItem() + "-" + cbStartDay.getSelectedItem();

                double salary;
                try {
                    salary = Double.parseDouble(txtSalary.getText().trim());
                } catch (Exception ex) {
                    showError(this, "Lương phải là số!");
                    return;
                }

                String sql = "UPDATE Staffs SET sta_name=?, sta_date_of_birth=?, sta_phone=?, sta_address=?, sta_salary=?, sta_start_date=?, sta_username=?, sta_password=?, sta_role=? WHERE sta_id=?";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, dob);
                ps.setString(3, txtPhone.getText().trim());
                ps.setString(4, txtAddress.getText().trim());
                ps.setDouble(5, salary);
                ps.setString(6, startDate);

                if (user.isEmpty()) ps.setNull(7, java.sql.Types.VARCHAR);
                else ps.setString(7, user);

                if (pass.isEmpty()) ps.setNull(8, java.sql.Types.VARCHAR);
                else ps.setString(8, pass);

                ps.setString(9, chkIsAdmin.isSelected() ? "Admin" : "Staff");
                ps.setInt(10, selectedStaffID);

                if (ps.executeUpdate() > 0) {
                    showSuccess(this, "Cập nhật thành công!");
                    loadListData();

                    // Giữ nguyên dòng đang chọn
                    selectStaffByID(selectedStaffID);

                    btnSave.setVisible(false);
                }
            } catch (Exception ex) {
                showError(this, "Lỗi: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
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
        SimpleDocumentListener docListener = new SimpleDocumentListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        txtName.getDocument().addDocumentListener(docListener);
        txtPhone.getDocument().addDocumentListener(docListener);
        txtAddress.getDocument().addDocumentListener(docListener);
        txtSalary.getDocument().addDocumentListener(docListener);

        txtUsername.getDocument().addDocumentListener(docListener);
        txtPassword.getDocument().addDocumentListener(docListener);

        chkIsAdmin.addActionListener(e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        });

        java.awt.event.ActionListener actionListener = e -> {
            if (!isDataLoading) btnSave.setVisible(true);
        };
        cbDay.addActionListener(actionListener);
        cbMonth.addActionListener(actionListener);
        cbYear.addActionListener(actionListener);

        cbStartDay.addActionListener(actionListener);
        cbStartMonth.addActionListener(actionListener);
        cbStartYear.addActionListener(actionListener);
    }

    private void clearForm() {
        isDataLoading = true;
        txtName.setText(""); txtPhone.setText(""); txtAddress.setText("");
        txtSalary.setText("");
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
        txtSalary.setEnabled(enable);

        cbStartDay.setEnabled(enable);
        cbStartMonth.setEnabled(enable);
        cbStartYear.setEnabled(enable);

        cbDay.setEnabled(enable);
        cbMonth.setEnabled(enable);
        cbYear.setEnabled(enable);
        txtUsername.setEnabled(enable);
        txtPassword.setEnabled(enable);
        chkIsAdmin.setEnabled(enable);
    }

    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            String val = String.format("%02d", i);
            cbDay.addItem(val);
            cbStartDay.addItem(val);
        }
        for (int i = 1; i <= 12; i++) {
            String val = String.format("%02d", i);
            cbMonth.addItem(val);
            cbStartMonth.addItem(val);
        }
        for (int i = 2025; i >= 1960; i--) {
            String val = String.valueOf(i);
            cbYear.addItem(val);
            cbStartYear.addItem(val);
        }
    }

    // Hàm chọn nhân viên theo ID trong danh sách
    private void selectStaffByID(int id) {
        ListModel<ComboItem> model = listStaff.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = model.getElementAt(i);
            if (item.getValue() == id) {
                listStaff.setSelectedIndex(i);
                listStaff.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public void refreshData() {
        loadListData();
        selectStaffByID(selectedStaffID);
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