import JDBCUntils.DBConnection;
import StaffForm.AddStaffForm;
import JDBCUntils.Functions;
import SupplierForm.AddSupplierForm;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashBoard extends JFrame {
    private JPanel dashBoardTime;
    private JList<String> listStaff;
    private JButton staffButtonAdd;
    private JTextField staffSearchBox;
    private JPanel StaffPanel;
    private JLabel staffLabel;
    private JTextField supplierSearchBox;
    private JList<String> listSupplier;
    private JButton saveSupplierButton;
    private JButton supplierButtonAdd;
    private JPanel SupplierPanel;
    private JLabel supplierLabel;
    private JPanel menuPanel;
    private JTextField staffName;
    private JTextField staffAddress;
    private JTextField staffPhone;
    private JComboBox<String> staffDay;
    private JComboBox<String> staffMonth;
    private JComboBox<String> staffYear;
    private JButton saveStaffButton;
    private JButton deleteStaffButton;
    private JTextField supplierName;
    private JTextField supplierAddress;
    private JTextField supplierPhone;
    private JButton deleteSupplierButton;

    private String originalStaffName;
    private String originalSupplierName;

    private boolean isStaffDataLoading = false;
    private boolean isSupplierDataLoading = false;

    public DashBoard(){
        super();
        this.setContentPane(dashBoardTime);
        this.setTitle("Quản Lý Cửa Hàng");
        this.setSize(700,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        addEvents();
        loadStaffListData();
        loadSupplierListData();


        addChangeListeners();

        initMenuUI();
        initComboBoxData();

        saveStaffButton.setVisible(false);
        saveSupplierButton.setVisible(false);
        deleteStaffButton.setVisible(false);
        deleteSupplierButton.setVisible(false);

        StaffPanel.setVisible(false);
        SupplierPanel.setVisible(false);

    }

    void loadStaffListData() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try (Connection con = DBConnection.getConnection()) {

            String sql = "SELECT sta_name FROM staffs"; // MySQL
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sta_name"));
            }

            listStaff.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadSupplierListData() {
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
            e.printStackTrace();
        }
    }

    void loadSelectedStaffData(String selectedName) {
        isStaffDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Staffs WHERE sta_name = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, selectedName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                deleteStaffButton.setVisible(true);

                staffName.setText(rs.getString("sta_name"));
                staffPhone.setText(rs.getString("sta_phone"));
                staffAddress.setText(rs.getString("sta_address"));

                Date sqlDate = rs.getDate("sta_date_of_birth");
                if (sqlDate != null) {
                    String[] parts = sqlDate.toString().split("-");
                    staffYear.setSelectedItem(parts[0]);
                    staffMonth.setSelectedItem(parts[1]);
                    staffDay.setSelectedItem(parts[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        } finally {
            saveStaffButton.setVisible(false);
            isStaffDataLoading = false;
        }
    }

    void loadSelectedSupplierData(String selectedName) {
        isSupplierDataLoading = true;
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Suppliers WHERE sup_name = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, selectedName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                deleteSupplierButton.setVisible(true);

                supplierName.setText(rs.getString("sup_name"));
                supplierPhone.setText(rs.getString("sup_phone"));
                supplierAddress.setText(rs.getString("sup_address"));

            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        } finally {
            saveSupplierButton.setVisible(false);
            isSupplierDataLoading = false;
        }
    }

    void searchStaff(String keyword) {
        DefaultListModel<String> model = new DefaultListModel<>();

        try (Connection con = DBConnection.getConnection()) {

            String sql = "SELECT sta_name FROM staffs WHERE sta_name LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(rs.getString("sta_name"));
            }

            listStaff.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void searchSupplier(String keyword) {
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
            e.printStackTrace();
        }
    }

    void addEvents() {
        staffLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (StaffPanel.isVisible()) {
                    StaffPanel.setVisible(false);
                } else {
                    StaffPanel.setVisible(true);
                    SupplierPanel.setVisible(false);
                    loadStaffListData();
                }
            }
        });

        supplierLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SupplierPanel.isVisible()) {
                    SupplierPanel.setVisible(false);
                } else {
                    SupplierPanel.setVisible(true);
                    StaffPanel.setVisible(false);
                    loadStaffListData();
                }
            }
        });

        staffSearchBox.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }

            private void doSearch() {
                String key = staffSearchBox.getText().trim();
                if (key.isEmpty() || key.equals("Tìm kiếm...")) {
                    loadStaffListData();
                } else {
                    searchStaff(key);
                }
            }
        });

        supplierSearchBox.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }
            private void doSearch() {
                String key = supplierSearchBox.getText().trim();
                if (key.isEmpty() || key.equals("Tìm kiếm...")) loadSupplierListData();
                else searchSupplier(key);
            }
        });

        supplierButtonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddSupplierForm form = new AddSupplierForm(DashBoard.this);
                form.setVisible(true);
                if (form.isAddedSuccess()) {
                    loadSupplierListData();
                }
            }
        });

        staffButtonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddStaffForm addStaffForm = new AddStaffForm(DashBoard.this);
                addStaffForm.setVisible(true);
                if (addStaffForm.isAddedSuccess()) {
                    loadStaffListData();
                }
            }
        });

        listStaff.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = listStaff.getSelectedValue();
                if (selectedName != null) {
                    this.originalStaffName = selectedName;
                    loadSelectedStaffData(selectedName);
                }
            }
        });

        listSupplier.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = listSupplier.getSelectedValue();
                if (selectedName != null) {
                    this.originalSupplierName = selectedName;
                    loadSelectedSupplierData(selectedName);
                }
            }
        });

        saveStaffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Connection con = DBConnection.getConnection()) {
                    String strDate = staffYear.getSelectedItem() + "-" +
                            staffMonth.getSelectedItem() + "-" +
                            staffDay.getSelectedItem();

                    String sql = "UPDATE Staffs SET sta_name=?, sta_date_of_birth=?, sta_phone=?, sta_address=? WHERE sta_name=?";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, staffName.getText());
                    ps.setString(2, strDate);
                    ps.setString(3, staffPhone.getText());
                    ps.setString(4, staffAddress.getText());

                    ps.setString(5, originalStaffName);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        loadStaffListData();
                        saveStaffButton.setVisible(false);
                        JOptionPane.showMessageDialog(DashBoard.this, "Cập nhật thành công!");
                    } else {
                        JOptionPane.showMessageDialog(DashBoard.this, "Không tìm thấy nhân viên để sửa!");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DashBoard.this, "Lỗi cập nhật: " + ex.getMessage());
                }
            }
        });

        saveSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Connection con = DBConnection.getConnection()) {
                    String sql = "UPDATE Suppliers SET sup_name=?, sup_address=?, sup_phone=? WHERE sup_name=?";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, supplierName.getText());
                    ps.setString(2, supplierAddress.getText());
                    ps.setString(3, supplierPhone.getText());
                    ps.setString(4, originalSupplierName);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        loadSupplierListData();
                        saveSupplierButton.setVisible(false);
                        JOptionPane.showMessageDialog(DashBoard.this, "Cập nhật thành công!");
                    } else {
                        JOptionPane.showMessageDialog(DashBoard.this, "Không tìm thấy nhà cung cấp để sửa!");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DashBoard.this, "Lỗi cập nhật: " + ex.getMessage());
                }

            }
        });

        deleteStaffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        DashBoard.this,
                        "Bạn có chắc muốn xóa: " + originalStaffName + "?"
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection con = DBConnection.getConnection()) {
                        String sql = "DELETE FROM Staffs WHERE sta_name = ?";

                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, originalStaffName);

                        int rows = ps.executeUpdate();
                        if (rows > 0) {
                            loadStaffListData();
                            JOptionPane.showMessageDialog(DashBoard.this, "Đã xóa thành công: " + originalStaffName);
                            clearStaffForm();
                        } else {
                            JOptionPane.showMessageDialog(DashBoard.this, "Không tìm thấy nhân viên này trong CSDL!");
                        }

                    } catch (SQLException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(DashBoard.this, "Lỗi kết nối CSDL: " + ex.getMessage());
                    }
                }

            }
        });

        deleteSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        DashBoard.this,
                        "Bạn có chắc muốn xóa: " + originalSupplierName + "?"
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection con = DBConnection.getConnection()) {
                        String sql = "DELETE FROM Suppliers WHERE sup_name = ?";

                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, originalSupplierName);

                        int rows = ps.executeUpdate();
                        if (rows > 0) {
                            loadSupplierListData();
                            JOptionPane.showMessageDialog(DashBoard.this, "Đã xóa thành công: " + originalSupplierName);
                            clearSupplierForm();
                        } else {
                            JOptionPane.showMessageDialog(DashBoard.this, "Không tìm thấy nhà cung cấp này trong CSDL!");
                        }

                    } catch (SQLException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(DashBoard.this, "Lỗi kết nối CSDL: " + ex.getMessage());
                    }
                }

            }
        });
    }

    private void initMenuUI() {
        menuPanel.setBackground(Color.decode("#2c3e50"));
//        menuPanel.setPreferredSize(new Dimension(1000, 50));

        Functions.createMenuButton(staffLabel);
        Functions.createMenuButton(supplierLabel);
        Functions.addPlaceholderStyle(staffSearchBox);
        Functions.addPlaceholderStyle(supplierSearchBox);


//        this.getContentPane().add(menuPanel, BorderLayout.NORTH);
    }

    private void addChangeListeners() {

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkStaffChange();
            }

            public void removeUpdate(DocumentEvent e) {
                checkStaffChange();
            }

            public void changedUpdate(DocumentEvent e) {
                checkStaffChange();
            }
        };

        DocumentListener ds = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkSupplierChange();
            }

            public void removeUpdate(DocumentEvent e) {
                checkSupplierChange();
            }

            public void changedUpdate(DocumentEvent e) {
                checkSupplierChange();
            }
        };

        staffName.getDocument().addDocumentListener(dl);
        staffPhone.getDocument().addDocumentListener(dl);
        staffAddress.getDocument().addDocumentListener(dl);

        supplierName.getDocument().addDocumentListener(ds);
        supplierPhone.getDocument().addDocumentListener(ds);
        supplierAddress.getDocument().addDocumentListener(ds);

        ActionListener al = e -> checkStaffChange();

        staffDay.addActionListener(al);
        staffMonth.addActionListener(al);
        staffYear.addActionListener(al);

    }
        private void checkStaffChange() {
            if (!isStaffDataLoading) {
                saveStaffButton.setVisible(true);
            }
        }

        private void checkSupplierChange() {
            if (!isSupplierDataLoading) {
                saveSupplierButton.setVisible(true);
            }
        }



    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            staffDay.addItem(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            staffMonth.addItem(String.format("%02d", i));
        }
        for (int i = 2025; i >= 1960; i--) {
            staffYear.addItem(String.valueOf(i));
        }
    }

    private void clearStaffForm() {
        isStaffDataLoading = true;
        staffName.setText("");
        staffPhone.setText("");
        staffAddress.setText("");
        isStaffDataLoading = false;
        saveStaffButton.setVisible(false);
        deleteStaffButton.setVisible(false);
        originalStaffName = null;
    }

    private void clearSupplierForm() {
        isSupplierDataLoading = true;
        supplierName.setText("");
        supplierPhone.setText("");
        supplierAddress.setText("");
        isSupplierDataLoading = false;
        saveSupplierButton.setVisible(false);
        deleteSupplierButton.setVisible(false);
        originalSupplierName = null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashBoard().setVisible(true));
    }
}
