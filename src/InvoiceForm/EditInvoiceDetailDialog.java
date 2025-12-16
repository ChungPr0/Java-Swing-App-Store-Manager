package InvoiceForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static JDBCUtils.Style.*;

/**
 * Dialog dùng để chỉnh sửa số lượng sản phẩm trong hóa đơn.
 * <br>
 * Nhiệm vụ:
 * 1. Hiển thị tên sản phẩm và giới hạn số lượng tối đa (Tồn kho + Số lượng hiện tại).
 * 2. Cho phép nhập số lượng mới.
 * 3. Kiểm tra tính hợp lệ (phải > 0 và <= giới hạn) trước khi lưu.
 */
public class EditInvoiceDetailDialog extends JDialog {

    // --- 1. KHAI BÁO BIẾN GIAO DIỆN ---
    private JTextField txtQuantity;
    private JButton btnSave, btnCancel;

    // --- 2. BIẾN DỮ LIỆU ---
    private boolean isConfirmed = false; // Cờ xác nhận người dùng đã bấm Lưu
    private int newQuantity = 0;         // Giá trị số lượng mới sau khi sửa
    private final int limit;             // Giới hạn tồn kho tối đa (Max stock)

    public EditInvoiceDetailDialog(Frame parent, String productName, int currentQty, int limit) {
        super(parent, true); // Modal = true (Chặn cửa sổ cha)
        setTitle("Chỉnh Sửa Số Lượng");

        this.limit = limit;

        initUI(productName, currentQty, limit); // Dựng giao diện
        addEvents();                            // Gán sự kiện

        pack();
        setLocationRelativeTo(parent);          // Căn giữa màn hình
        setResizable(false);
    }

    // --- 3. KHỞI TẠO GIAO DIỆN (INIT UI) ---
    private void initUI(String productName, int currentQty, int limit) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // A. Tiêu đề form
        mainPanel.add(createHeaderLabel("SỬA SỐ LƯỢNG"));
        mainPanel.add(Box.createVerticalStrut(20));

        // B. Hiển thị Tên sản phẩm (Read-only)
        JTextField lblName = new JTextField();
        lblName.setText(productName);
        lblName.setEditable(false);
        lblName.setFocusable(false);
        mainPanel.add(createTextFieldWithLabel(lblName, "Sản phẩm: "));
        mainPanel.add(Box.createVerticalStrut(15));

        // C. Hiển thị Giới hạn tối đa (Read-only)
        JTextField pCount = new JTextField();
        pCount.setText(String.valueOf(limit));
        pCount.setEditable(false);
        pCount.setFocusable(false);
        mainPanel.add(createTextFieldWithLabel(pCount, "Tối đa có thể nhập: "));
        mainPanel.add(Box.createVerticalStrut(15));

        // D. Ô nhập Số lượng mới (Editable)
        txtQuantity = new JTextField(String.valueOf(currentQty));
        mainPanel.add(createTextFieldWithLabel(txtQuantity, "Số Lượng Mới:"));
        mainPanel.add(Box.createVerticalStrut(25));

        // E. Khu vực nút bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnSave = createButton("Xác Nhận", new Color(46, 204, 113));
        btnCancel = createButton("Hủy Bỏ", new Color(231, 76, 60));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel);
        setContentPane(mainPanel);

        // Bấm Enter để Lưu luôn
        getRootPane().setDefaultButton(btnSave);
    }

    // --- 4. XỬ LÝ SỰ KIỆN (EVENTS) ---
    private void addEvents() {
        // --- CHẶN NHẬP CHỮ ---
        txtQuantity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                // Nếu ký tự gõ vào không phải số -> Hủy bỏ (không cho nhập)
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Sự kiện nút Lưu
        btnSave.addActionListener(_ -> {
            try {
                // Parse số lượng từ ô nhập liệu
                int qty = Integer.parseInt(txtQuantity.getText().trim());

                // Validate 1: Số lượng phải dương
                if (qty <= 0) {
                    showError(this, "Số lượng phải lớn hơn 0!");
                    return;
                }

                // Validate 2: Số lượng không được vượt quá tồn kho
                if (qty > limit) {
                    showError(this,
                            "Kho không đủ hàng!\n" +
                                    "Bạn chỉ có thể nhập tối đa: " + limit + "\n" +
                                    "(Do trong kho và đơn hàng cộng lại chỉ có bấy nhiêu)");
                    return;
                }

                // Nếu hợp lệ -> Lưu dữ liệu và đóng form
                this.newQuantity = qty;
                this.isConfirmed = true;
                dispose();

            } catch (NumberFormatException ex) {
                showError(this, "Vui lòng nhập số hợp lệ!");
            }
        });

        // Sự kiện nút Hủy
        btnCancel.addActionListener(_ -> dispose());
    }

    // --- 5. GETTER TRẢ VỀ KẾT QUẢ ---
    public boolean isConfirmed() { return isConfirmed; }
    public int getNewQuantity() { return newQuantity; }
}