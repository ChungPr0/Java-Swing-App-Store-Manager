package Main.SupplierManager;

import Utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static Utils.Style.*;

/**
 * Dialog for adding a new supplier.
 */
public class AddSupplierDialog extends JDialog {

    // --- UI VARIABLES ---
    private JTextField txtName, txtPhone, txtAddress;
    private JTextArea txtDescription; // Description field
    private JComboBox<String> cbDay, cbMonth, cbYear; // Date selection
    private JButton btnSave, btnCancel;

    // --- STATE VARIABLES ---
    private boolean isAdded = false;
    private int newSupplierID = -1;

    /**
     * Constructor to initialize the Add Supplier Dialog.
     *
     * @param parent The parent frame.
     */
    public AddSupplierDialog(Frame parent) {
        super(parent, true); // Modal
        this.setTitle("Thêm Nhà Cung Cấp Mới");

        initUI();
        initComboBoxData(); // Load date data
        addEvents();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
    }

    // --- 1. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Title
        JLabel lblTitle = createHeaderLabel("NHẬP THÔNG TIN");
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Input Fields
        txtName = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên Nhà Cung Cấp:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // Partnership Start Date
        cbDay = new JComboBox<>();
        cbMonth = new JComboBox<>();
        cbYear = new JComboBox<>();
        JPanel pDate = createDatePanel("Ngày bắt đầu hợp tác:", cbDay, cbMonth, cbYear);
        mainPanel.add(pDate);
        mainPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
        mainPanel.add(Box.createVerticalStrut(15));

        // Description field (Using createTextAreaWithLabel)
        txtDescription = new JTextArea(4, 20);
        JPanel pDesc = createTextAreaWithLabel(txtDescription, "Mô tả / Ghi chú:");
        mainPanel.add(pDesc);
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Button Area
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Lưu Lại", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);

        getRootPane().setDefaultButton(btnSave);
    }

    // --- 2. EVENT HANDLING ---

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {

        // Block non-digit input for phone number
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Save button event
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty() || txtDescription.getText().trim().isEmpty()) {
                showError(AddSupplierDialog.this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Update SQL to include new columns
                String sql = "INSERT INTO Suppliers (sup_name, sup_phone, sup_address, sup_start_date, sup_description) VALUES (?, ?, ?, ?, ?)";

                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                // Create YYYY-MM-DD date string
                String strDate = cbYear.getSelectedItem() + "-" + cbMonth.getSelectedItem() + "-" + cbDay.getSelectedItem();

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtPhone.getText().trim());
                ps.setString(3, txtAddress.getText().trim());
                ps.setString(4, strDate); // Partnership date
                ps.setString(5, txtDescription.getText().trim()); // Description

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Get the newly generated ID
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.newSupplierID = rs.getInt(1);
                    }

                    showSuccess(AddSupplierDialog.this, "Thêm thành công!");
                    isAdded = true;
                    dispose();
                }
            } catch (Exception ex) {
                showError(AddSupplierDialog.this, "Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    // --- 3. HELPER METHODS ---

    /**
     * Initializes data for date combo boxes.
     */
    private void initComboBoxData() {
        for (int i = 1; i <= 31; i++) {
            cbDay.addItem(String.format("%02d", i));
        }
        for (int i = 1; i <= 12; i++) {
            cbMonth.addItem(String.format("%02d", i));
        }
        // Current year back to 1990
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear; i >= 1990; i--) {
            cbYear.addItem(String.valueOf(i));
        }
    }

    // --- Getters for result ---

    /**
     * Checks if the supplier was added successfully.
     *
     * @return true if added, false otherwise.
     */
    public boolean isAddedSuccess() {
        return isAdded;
    }

    /**
     * Gets the ID of the newly added supplier.
     *
     * @return The new supplier ID.
     */
    public int getNewSupplierID() {
        return newSupplierID;
    }
}
