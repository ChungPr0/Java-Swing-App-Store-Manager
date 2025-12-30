package Main.InvoiceManager;

import Utils.ComboItem;
import Utils.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static Utils.Style.*;

/**
 * Dialog for searching customers.
 */
public class SearchCustomerDialog extends JDialog {

    private JTextField txtSearch;
    private JList<ComboItem> listResults;
    private JButton btnConfirm, btnCancel;

    private ComboItem selectedCustomer = null;

    /**
     * Constructor to initialize the Search Customer Dialog.
     *
     * @param parent The parent frame.
     */
    public SearchCustomerDialog(Frame parent) {
        super(parent, "Tìm Kiếm Khách Hàng", true);
        initUI();
        addEvents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- Header ---
        JLabel lblTitle = createHeaderLabel("TÌM KIẾM KHÁCH HÀNG");
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Search Panel ---
        txtSearch = new JTextField();
        JPanel pSearch = createSearchPanel(txtSearch, "Tìm kiếm", "Nhập tên hoặc SĐT...");
        pSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        mainPanel.add(pSearch);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Results List ---
        listResults = new JList<>();
        listResults.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listResults.setFixedCellHeight(30);
        JScrollPane scrollPane = new JScrollPane(listResults);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        btnConfirm = createButton("Xác Nhận", new Color(46, 204, 113));
        btnCancel = createButton("Hủy", new Color(231, 76, 60));
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel);

        this.setContentPane(mainPanel);
        getRootPane().setDefaultButton(btnConfirm);
    }

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        // Search as you type
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
        });

        // Double-click to confirm
        listResults.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });

        btnConfirm.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());
    }

    /**
     * Performs the customer search.
     */
    private void search() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty() || keyword.equals("Nhập tên hoặc SĐT...")) {
            listResults.setModel(new DefaultListModel<>());
            return;
        }

        DefaultListModel<ComboItem> model = new DefaultListModel<>();
        String sql = "SELECT cus_ID, cus_name, cus_phone FROM Customers WHERE (cus_name LIKE ? OR cus_phone LIKE ?) AND cus_ID != 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("cus_ID");
                String name = rs.getString("cus_name");
                String phone = rs.getString("cus_phone");
                String displayText = name + " - " + phone;
                model.addElement(new ComboItem(displayText, id));
            }
            listResults.setModel(model);

        } catch (Exception e) {
            showError(this, "Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    /**
     * Confirms the selected customer.
     */
    private void confirmSelection() {
        selectedCustomer = listResults.getSelectedValue();
        if (selectedCustomer == null) {
            showError(this, "Vui lòng chọn một khách hàng!");
            return;
        }
        dispose();
    }

    /**
     * Gets the selected customer.
     *
     * @return The selected customer as a ComboItem.
     */
    public ComboItem getSelectedCustomer() {
        return selectedCustomer;
    }
}
