package Main.InvoiceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static Utils.Style.*;

/**
 * Dialog for editing the quantity of a product in an invoice.
 * <br>
 * Responsibilities:
 * 1. Display product name and maximum quantity limit (Stock + Current quantity).
 * 2. Allow entering a new quantity.
 * 3. Validate input (must be > 0 and <= limit) before saving.
 */
public class EditInvoiceDetailDialog extends JDialog {

    // --- 1. UI VARIABLES ---
    private JTextField txtQuantity;
    private JButton btnSave, btnCancel;

    // --- 2. DATA VARIABLES ---
    private boolean isConfirmed = false; // Flag to confirm user clicked Save
    private int newQuantity = 0;         // New quantity value after editing
    private final int limit;             // Maximum stock limit

    /**
     * Constructor to initialize the Edit Invoice Detail Dialog.
     *
     * @param parent       The parent frame.
     * @param productName  The name of the product.
     * @param currentQty   The current quantity.
     * @param limit        The maximum quantity limit.
     */
    public EditInvoiceDetailDialog(Frame parent, String productName, int currentQty, int limit) {
        super(parent, true); // Modal = true (Blocks parent window)
        setTitle("Chỉnh Sửa Số Lượng");

        this.limit = limit;

        initUI(productName, currentQty, limit); // Build UI
        addEvents();                            // Assign events

        pack();
        setLocationRelativeTo(parent);          // Center on screen
        setResizable(false);
    }

    // --- 3. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     *
     * @param productName The name of the product.
     * @param currentQty  The current quantity.
     * @param limit       The maximum quantity limit.
     */
    private void initUI(String productName, int currentQty, int limit) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Form Title
        mainPanel.add(createHeaderLabel("SỬA SỐ LƯỢNG"));
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Display Product Name (Read-only)
        JTextField lblName = new JTextField();
        lblName.setText(productName);
        lblName.setEditable(false);
        lblName.setFocusable(false);
        mainPanel.add(createTextFieldWithLabel(lblName, "Sản phẩm: "));
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Display Maximum Limit (Read-only)
        JTextField pCount = new JTextField();
        pCount.setText(String.valueOf(limit));
        pCount.setEditable(false);
        pCount.setFocusable(false);
        mainPanel.add(createTextFieldWithLabel(pCount, "Tối đa có thể nhập: "));
        mainPanel.add(Box.createVerticalStrut(15));

        // D. New Quantity Input (Editable)
        txtQuantity = new JTextField(String.valueOf(currentQty));
        mainPanel.add(createTextFieldWithLabel(txtQuantity, "Số Lượng Mới:"));
        mainPanel.add(Box.createVerticalStrut(25));

        // E. Button Area
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Xác Nhận", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel);
        setContentPane(mainPanel);

        // Press Enter to Save
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. EVENT HANDLING ---

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        // --- BLOCK NON-DIGIT INPUT ---
        txtQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                // If typed character is not a digit -> Cancel (don't allow input)
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Save button event
        btnSave.addActionListener(e -> {
            try {
                // Parse quantity from input field
                int qty = Integer.parseInt(txtQuantity.getText().trim());

                // Validate 1: Quantity must be positive
                if (qty <= 0) {
                    showError(this, "Số lượng phải lớn hơn 0!");
                    return;
                }

                // Validate 2: Quantity must not exceed stock limit
                if (qty > limit) {
                    showError(this,
                            "Kho không đủ hàng!\n" +
                                    "Bạn chỉ có thể nhập tối đa: " + limit + "\n" +
                                    "(Do trong kho và đơn hàng cộng lại chỉ có bấy nhiêu)");
                    return;
                }

                // If valid -> Save data and close form
                this.newQuantity = qty;
                this.isConfirmed = true;
                dispose();

            } catch (NumberFormatException ex) {
                showError(this, "Vui lòng nhập số hợp lệ!");
            }
        });

        // Cancel button event
        btnCancel.addActionListener(e -> dispose());
    }

    // --- 5. GETTERS FOR RESULT ---

    /**
     * Checks if the dialog was confirmed.
     *
     * @return true if confirmed, false otherwise.
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * Gets the new quantity.
     *
     * @return The new quantity.
     */
    public int getNewQuantity() {
        return newQuantity;
    }
}
