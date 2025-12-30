package Main;

import Main.HomeManager.HomeManagerPanel;
import Main.StaffManager.StaffManagerPanel;
import Main.SupplierManager.SupplierManagerPanel;
import Main.CustomerManager.CustomerManagerPanel;
import Main.ProductManager.ProductManagerPanel;
import Main.InvoiceManager.InvoiceManagerPanel;
import Main.DiscountManager.DiscountManagerPanel;
import Main.LoginManager.LoginForm;
import Main.LoginManager.ChangePasswordDialog;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static Utils.Style.*;

/**
 * Main Dashboard class for the application.
 * Manages navigation between different functional panels.
 */
public class DashBoard extends JFrame {

    // --- 1. VARIABLES ---
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Menu Buttons
    private JButton btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnDiscount, btnInvoice;
    private JButton currentActiveButton;

    // Functional Panels
    private HomeManagerPanel homePanel;
    private StaffManagerPanel staffPanel;
    private SupplierManagerPanel supplierPanel;
    private CustomerManagerPanel customerPanel;
    private ProductManagerPanel productPanel;
    private DiscountManagerPanel discountPanel;
    private InvoiceManagerPanel invoicePanel;

    /**
     * Constructor to initialize the Dashboard.
     */
    public DashBoard() {
        super("Quản Lý Cửa Hàng");
        this.setSize(1050, 680); // Slightly increased width to fit buttons
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        initUI();
        addEvents();
    }

    // --- 2. UI INITIALIZATION ---

    /**
     * Initializes the User Interface components.
     */
    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());

        // A. MENU BAR
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        menuPanel.setBackground(Color.decode("#2c3e50"));
        menuPanel.setPreferredSize(new Dimension(0, 50));

        // 1. Create buttons
        btnHome = createMenuButton("TRANG CHỦ");
        btnStaff = createMenuButton("NHÂN VIÊN");
        btnSupplier = createMenuButton("NHÀ CUNG CẤP");
        btnCustomer = createMenuButton("KHÁCH HÀNG");
        btnProduct = createMenuButton("SẢN PHẨM");
        btnDiscount = createMenuButton("KHUYẾN MÃI");
        btnInvoice = createMenuButton("HÓA ĐƠN");

        // 2. Add to Menu Bar
        menuPanel.add(btnHome);
        menuPanel.add(btnStaff);
        menuPanel.add(btnSupplier);
        menuPanel.add(btnCustomer);
        menuPanel.add(btnProduct);
        menuPanel.add(btnDiscount);
        menuPanel.add(btnInvoice);

        // Add "Glue" in the middle
        menuPanel.add(Box.createHorizontalGlue());

        JButton btnInfo = createMenuButton("TÀI KHOẢN");
        setupUserPopup(btnInfo);

        menuPanel.add(btnInfo);

        // B. CONTENT AREA
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Initialize sub-screens
        homePanel = new HomeManagerPanel();
        staffPanel = new StaffManagerPanel();
        supplierPanel = new SupplierManagerPanel();
        customerPanel = new CustomerManagerPanel();
        productPanel = new ProductManagerPanel();
        discountPanel = new DiscountManagerPanel();
        invoicePanel = new InvoiceManagerPanel();

        // Add to CardLayout with identifiers
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(staffPanel, "STAFF");
        contentPanel.add(supplierPanel, "SUPPLIER");
        contentPanel.add(customerPanel, "CUSTOMER");
        contentPanel.add(productPanel, "PRODUCT");
        contentPanel.add(discountPanel, "DISCOUNT");
        contentPanel.add(invoicePanel, "INVOICE");

        mainContainer.add(menuPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        this.setContentPane(mainContainer);

        // C. PERMISSIONS ON MENU (Hide buttons if not Admin)
        applyPermissions();
    }

    /**
     * Applies user permissions to show/hide menu buttons.
     */
    private void applyPermissions() {
        // Default show all
        btnHome.setVisible(true);
        btnStaff.setVisible(true);
        btnSupplier.setVisible(true);
        btnCustomer.setVisible(true);
        btnProduct.setVisible(true);
        btnDiscount.setVisible(true);
        btnInvoice.setVisible(true);

        // Admin: Full access (already shown all)

        // SaleStaff: only add/edit/delete customers, create invoices, print invoices.
        // => Hide: Staff, Supplier, Product, Discount (view only?), Home (stats?)
        // Requirement: "management tabs if authorized in that tab"
        // SaleStaff has rights in Customer, Invoice.
        // Manager inherits SaleStaff and StorageStaff + edit/delete invoices, view stats, export excel, add/edit/delete types, add/edit/delete discounts.
        // => Manager has rights in Home (stats), Customer, Invoice, Supplier, Product, Discount.
        // StorageStaff: only add/edit suppliers, add/edit products.
        // => StorageStaff has rights in Supplier, Product.

        if (Session.userRole.equalsIgnoreCase("Manager")) {
            btnStaff.setVisible(false); // Manager does not manage staff (Admin only)
        } else if (Session.userRole.equalsIgnoreCase("SaleStaff")) {
            btnStaff.setVisible(false);
            btnSupplier.setVisible(false);
            btnProduct.setVisible(false);
            btnDiscount.setVisible(false);
            btnHome.setVisible(false); // No stats view
        } else if (Session.userRole.equalsIgnoreCase("StorageStaff")) {
            btnStaff.setVisible(false);
            btnCustomer.setVisible(false);
            btnInvoice.setVisible(false);
            btnDiscount.setVisible(false);
            btnHome.setVisible(false); // No stats view
        }
    }

    // --- 3. EVENT HANDLING ---

    /**
     * Adds event listeners to components.
     */
    private void addEvents() {
        // Assign Tab switching events
        btnHome.addActionListener(e -> switchTab("HOME", homePanel, btnHome));
        btnStaff.addActionListener(e -> switchTab("STAFF", staffPanel, btnStaff));
        btnSupplier.addActionListener(e -> switchTab("SUPPLIER", supplierPanel, btnSupplier));
        btnCustomer.addActionListener(e -> switchTab("CUSTOMER", customerPanel, btnCustomer));
        btnProduct.addActionListener(e -> switchTab("PRODUCT", productPanel, btnProduct));
        btnDiscount.addActionListener(e -> switchTab("DISCOUNT", discountPanel, btnDiscount));
        btnInvoice.addActionListener(e -> switchTab("INVOICE", invoicePanel, btnInvoice));

        // Default select Home
        // If home is hidden, select the first visible tab
        if (btnHome.isVisible()) {
            updateActiveButton(btnHome);
        } else if (btnCustomer.isVisible()) {
            switchTab("CUSTOMER", customerPanel, btnCustomer);
        } else if (btnSupplier.isVisible()) {
            switchTab("SUPPLIER", supplierPanel, btnSupplier);
        } else if (btnProduct.isVisible()) {
            switchTab("PRODUCT", productPanel, btnProduct);
        } else if (btnInvoice.isVisible()) {
            switchTab("INVOICE", invoicePanel, btnInvoice);
        }
    }

    /**
     * Helper method to switch Tab and refresh data.
     *
     * @param cardName The name of the card to show.
     * @param panel    The panel object to refresh.
     * @param btn      The button that was clicked.
     */
    private void switchTab(String cardName, Object panel, JButton btn) {
        cardLayout.show(contentPanel, cardName);

        // Call corresponding refreshData method
        if (panel instanceof HomeManagerPanel) ((HomeManagerPanel) panel).refreshData();
        else if (panel instanceof StaffManagerPanel) ((StaffManagerPanel) panel).refreshData();
        else if (panel instanceof SupplierManagerPanel) ((SupplierManagerPanel) panel).refreshData();
        else if (panel instanceof CustomerManagerPanel) ((CustomerManagerPanel) panel).refreshData();
        else if (panel instanceof ProductManagerPanel) ((ProductManagerPanel) panel).refreshData();
        else if (panel instanceof DiscountManagerPanel) ((DiscountManagerPanel) panel).refreshData();
        else if (panel instanceof InvoiceManagerPanel) ((InvoiceManagerPanel) panel).refreshData();

        updateActiveButton(btn);
    }

    /**
     * Updates the color of the active button.
     *
     * @param activeBtn The button to set as active.
     */
    private void updateActiveButton(JButton activeBtn) {
        currentActiveButton = activeBtn;

        JButton[] btns = {btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnDiscount, btnInvoice};

        for (JButton btn : btns) {
            if (btn == activeBtn) {
                btn.setBackground(Color.decode("#3498db")); // Blue (Active)
            } else {
                btn.setBackground(Color.decode("#2c3e50")); // Dark (Inactive)
            }
        }
    }

    /**
     * Creates a menu button with standard styling.
     *
     * @param text The button text.
     * @return A styled JButton.
     */
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.decode("#2c3e50"));

        btn.setPreferredSize(new Dimension(130, 50));
        btn.setMaximumSize(new Dimension(130, 51));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Color.decode("#3498db"));
            }

            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(Color.decode("#2c3e50"));
                }
            }
        });
        return btn;
    }

    // --- PUBLIC HELPER METHODS ---

    /**
     * Switches to Product tab and loads specific product details.
     *
     * @param proID The product ID to load.
     */
    public void showProductAndLoad(int proID) {
        cardLayout.show(contentPanel, "PRODUCT");
        updateActiveButton(btnProduct);
        if (productPanel != null) productPanel.loadDetail(proID);
    }

    /**
     * Switches to Customer tab and loads specific customer details.
     *
     * @param cusID The customer ID to load.
     */
    public void showCustomerAndLoad(int cusID) {
        cardLayout.show(contentPanel, "CUSTOMER");
        updateActiveButton(btnCustomer);
        if (customerPanel != null) customerPanel.loadDetail(cusID);
    }

    /**
     * Switches to Invoice tab and loads specific invoice details.
     *
     * @param invID The invoice ID to load.
     */
    public void showInvoiceAndLoad(int invID) {
        cardLayout.show(contentPanel, "INVOICE");
        updateActiveButton(btnInvoice);
        if (invoicePanel != null) invoicePanel.loadDetail(invID);
    }

    /**
     * Sets up the user profile popup menu.
     *
     * @param btnTarget The button that triggers the popup.
     */
    private void setupUserPopup(JButton btnTarget) {
        JPopupMenu popupProfile = new JPopupMenu();
        popupProfile.setBackground(Color.WHITE);
        popupProfile.setBorder(BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1));
        popupProfile.setPreferredSize(new Dimension(250, 240));

        JPanel pContent = new JPanel();
        pContent.setLayout(new BoxLayout(pContent, BoxLayout.Y_AXIS));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Thông tin tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#2c3e50"));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel("Tài khoản: " + Utils.Session.loggedInStaffName);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRole = getRoles();

        JButton btnChangePass = createButton("Đổi mật khẩu", Color.decode("#3498db"));
        btnChangePass.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnChangePass.setMaximumSize(new Dimension(220, 35));

        btnChangePass.addActionListener(e -> {
            popupProfile.setVisible(false);
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ChangePasswordDialog(parent).setVisible(true);
        });

        JButton btnLogout = createButton("Đăng Xuất", Color.decode("#e74c3c"));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(220, 35));

        btnLogout.addActionListener(e -> {
            popupProfile.setVisible(false);
            if (showConfirm(this, "Bạn có chắc muốn đăng xuất?")) {
                Session.clear();
                this.dispose();
                new LoginForm().setVisible(true);
            }
        });

        pContent.add(lblTitle);
        pContent.add(Box.createVerticalStrut(10));
        pContent.add(new JSeparator());
        pContent.add(Box.createVerticalStrut(10));
        pContent.add(lblUser);
        pContent.add(Box.createVerticalStrut(5));
        pContent.add(lblRole);
        pContent.add(Box.createVerticalStrut(15));
        pContent.add(btnChangePass);
        pContent.add(Box.createVerticalStrut(10));
        pContent.add(btnLogout);

        popupProfile.add(pContent);

        btnTarget.addActionListener(e -> {
            if (!popupProfile.isVisible()) {
                popupProfile.show(btnTarget, btnTarget.getWidth() - popupProfile.getPreferredSize().width, btnTarget.getHeight());
            } else {
                popupProfile.setVisible(false);
            }
        });
    }

    /**
     * Gets the display role string for the current user.
     *
     * @return A JLabel containing the role description.
     */
    private static JLabel getRoles() {
        String roleDisplay = Session.userRole;
        if (roleDisplay.equalsIgnoreCase("Admin")) roleDisplay = "Quản trị viên";
        else if (roleDisplay.equalsIgnoreCase("Manager")) roleDisplay = "Quản lý";
        else if (roleDisplay.equalsIgnoreCase("SaleStaff")) roleDisplay = "Nhân viên bán hàng";
        else if (roleDisplay.equalsIgnoreCase("StorageStaff")) roleDisplay = "Nhân viên kho";
        else roleDisplay = "Nhân viên";

        JLabel lblRole = new JLabel("Vai trò: " + roleDisplay);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(Color.GRAY);
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lblRole;
    }
}
