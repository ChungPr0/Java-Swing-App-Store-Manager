package Main.CustomerManager;

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
 * Dialog for adding a new customer.
 */
public class AddCustomerDialog extends JDialog {

    // --- 1. UI VARIABLES ---
    private JTextField txtName, txtPhone, txtAddress;
    private JButton btnSave, btnCancel;

    // --- 2. STATE VARIABLES ---
    private boolean isAdded = false;
    private int newCustomerID = -1; // Variable to store the ID of the newly added customer

    /**
     * Constructor to initialize the Add Customer Dialog.
     *
     * @param parent The parent frame.
     */
    public AddCustomerDialog(Frame parent) {
        super(parent, true);
        this.setTitle("Thêm Khách Hàng Mới");

        initUI();
        addEvents();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setResizable(false);
    }

    // --- 3. UI INITIALIZATION ---

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
        mainPanel.add(createTextFieldWithLabel(txtName, "Tên khách hàng:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtPhone = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtPhone, "Số điện thoại:"));
        mainPanel.add(Box.createVerticalStrut(15));

        txtAddress = new JTextField();
        mainPanel.add(createTextFieldWithLabel(txtAddress, "Địa chỉ:"));
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

        // Pressing Enter will activate the Save button
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. EVENT HANDLING ---

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        // --- BLOCK NON-DIGIT INPUT FOR PHONE NUMBER ---
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Save button event
        btnSave.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty()) {
                showError(AddCustomerDialog.this, "Vui lòng nhập Tên và Số điện thoại!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO Customers (cus_name, cus_phone, cus_address) VALUES (?, ?, ?)";

                // [MODIFIED] Add RETURN_GENERATED_KEYS parameter to get the ID
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtPhone.getText().trim());
                ps.setString(3, txtAddress.getText().trim());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Get the newly generated ID from the Database
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.newCustomerID = rs.getInt(1);
                    }

                    showSuccess(AddCustomerDialog.this, "Thêm thành công!");
                    isAdded = true;
                    dispose();
                }
            } catch (Exception ex) {
                showError(AddCustomerDialog.this, "Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    // --- 5. UTILITY METHODS ---

    /**
     * Checks if the customer was added successfully.
     *
     * @return true if added, false otherwise.
     */
    public boolean isAddedSuccess() {
        return isAdded;
    }

    /**
     * Gets the ID of the newly added customer.
     *
     * @return The new customer ID.
     */
    public int getNewCustomerID() {
        return newCustomerID;
    }
}
