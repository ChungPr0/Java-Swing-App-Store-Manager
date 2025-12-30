package Main;

import Main.HomeManager.HomeManagerPanel;
import Main.StaffManager.StaffManagerPanel;
import Main.SupplierManager.SupplierManagerPanel;
import Main.CustomerManager.CustomerManagerPanel;
import Main.ProductManager.ProductManagerPanel;
import Main.InvoiceManager.InvoiceManagerPanel;
import Main.DiscountManager.DiscountManagerPanel; // [MỚI] Import Panel Khuyến mãi
import Main.LoginManager.LoginForm;
import Main.LoginManager.ChangePasswordDialog;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static Utils.Style.*;

public class DashBoard extends JFrame {

    // --- 1. KHAI BÁO BIẾN ---
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Các nút Menu
    private JButton btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnDiscount, btnInvoice; // [MỚI] Thêm btnDiscount
    private JButton currentActiveButton;

    // Các Panel chức năng
    private HomeManagerPanel homePanel;
    private StaffManagerPanel staffPanel;
    private SupplierManagerPanel supplierPanel;
    private CustomerManagerPanel customerPanel;
    private ProductManagerPanel productPanel;
    private DiscountManagerPanel discountPanel; // [MỚI] Khai báo Panel
    private InvoiceManagerPanel invoicePanel;

    public DashBoard() {
        super("Quản Lý Cửa Hàng");
        this.setSize(1050, 680); // Tăng kích thước chiều ngang một chút để chứa đủ nút
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        initUI();
        addEvents();
    }

    // --- 2. KHỞI TẠO GIAO DIỆN ---
    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());

        // A. MENU BAR
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        menuPanel.setBackground(Color.decode("#2c3e50"));
        menuPanel.setPreferredSize(new Dimension(0, 50));

        // 1. Tạo các nút
        btnHome = createMenuButton("TRANG CHỦ");
        btnStaff = createMenuButton("NHÂN VIÊN");
        btnSupplier = createMenuButton("NHÀ CUNG CẤP");
        btnCustomer = createMenuButton("KHÁCH HÀNG");
        btnProduct = createMenuButton("SẢN PHẨM");
        btnDiscount = createMenuButton("KHUYẾN MÃI"); // [MỚI] Tạo nút
        btnInvoice = createMenuButton("HÓA ĐƠN");

        // 2. Thêm vào thanh Menu
        menuPanel.add(btnHome);
        menuPanel.add(btnStaff);
        menuPanel.add(btnSupplier);
        menuPanel.add(btnCustomer);
        menuPanel.add(btnProduct);
        menuPanel.add(btnDiscount); // [MỚI]
        menuPanel.add(btnInvoice);

        // Thêm "Lò xo" ở giữa
        menuPanel.add(Box.createHorizontalGlue());

        JButton btnInfo = createMenuButton("TÀI KHOẢN");
        setupUserPopup(btnInfo);

        menuPanel.add(btnInfo);

        // B. CONTENT AREA
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Khởi tạo các màn hình con
        homePanel = new HomeManagerPanel();
        staffPanel = new StaffManagerPanel();
        supplierPanel = new SupplierManagerPanel();
        customerPanel = new CustomerManagerPanel();
        productPanel = new ProductManagerPanel();
        discountPanel = new DiscountManagerPanel(); // [MỚI]
        invoicePanel = new InvoiceManagerPanel();

        // Thêm vào CardLayout với tên định danh
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(staffPanel, "STAFF");
        contentPanel.add(supplierPanel, "SUPPLIER");
        contentPanel.add(customerPanel, "CUSTOMER");
        contentPanel.add(productPanel, "PRODUCT");
        contentPanel.add(discountPanel, "DISCOUNT"); // [MỚI]
        contentPanel.add(invoicePanel, "INVOICE");

        mainContainer.add(menuPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        this.setContentPane(mainContainer);

        // C. PHÂN QUYỀN TRÊN MENU (Ẩn nút nếu không phải Admin)
        if (!Utils.Session.isAdmin()) {
            btnStaff.setVisible(false);
            btnSupplier.setVisible(false);
            // Lưu ý: btnDiscount vẫn hiện với nhân viên để họ xem mã,
            // nhưng trong panel đó họ sẽ không thấy nút Sửa/Xóa (do logic bên trong panel xử lý).
        }
    }

    // --- 3. XỬ LÝ SỰ KIỆN ---
    private void addEvents() {
        // Gán sự kiện chuyển Tab
        btnHome.addActionListener(e -> switchTab("HOME", homePanel, btnHome));
        btnStaff.addActionListener(e -> switchTab("STAFF", staffPanel, btnStaff));
        btnSupplier.addActionListener(e -> switchTab("SUPPLIER", supplierPanel, btnSupplier));
        btnCustomer.addActionListener(e -> switchTab("CUSTOMER", customerPanel, btnCustomer));
        btnProduct.addActionListener(e -> switchTab("PRODUCT", productPanel, btnProduct));
        btnDiscount.addActionListener(e -> switchTab("DISCOUNT", discountPanel, btnDiscount));
        btnInvoice.addActionListener(e -> switchTab("INVOICE", invoicePanel, btnInvoice));

        // Mặc định chọn Trang chủ
        updateActiveButton(btnHome);
    }

    /**
     * Hàm hỗ trợ chuyển Tab và làm mới dữ liệu
     */
    private void switchTab(String cardName, Object panel, JButton btn) {
        cardLayout.show(contentPanel, cardName);

        // Gọi hàm refreshData tương ứng
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
     * Hàm đổi màu nút đang được chọn (Active)
     */
    private void updateActiveButton(JButton activeBtn) {
        currentActiveButton = activeBtn;

        // [MỚI] Thêm btnDiscount vào mảng
        JButton[] btns = {btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnDiscount, btnInvoice};

        for (JButton btn : btns) {
            if (btn == activeBtn) {
                btn.setBackground(Color.decode("#3498db")); // Xanh (Active)
            } else {
                btn.setBackground(Color.decode("#2c3e50")); // Đen (Inactive)
            }
        }
    }

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

    // --- CÁC HÀM PUBLIC HELPER ---

    public void showProductAndLoad(int proID) {
        cardLayout.show(contentPanel, "PRODUCT");
        updateActiveButton(btnProduct);
        if (productPanel != null) productPanel.loadDetail(proID);
    }

    public void showCustomerAndLoad(int cusID) {
        cardLayout.show(contentPanel, "CUSTOMER");
        updateActiveButton(btnCustomer);
        if (customerPanel != null) customerPanel.loadDetail(cusID);
    }

    public void showInvoiceAndLoad(int invID) {
        cardLayout.show(contentPanel, "INVOICE");
        updateActiveButton(btnInvoice);
        if (invoicePanel != null) invoicePanel.loadDetail(invID);
    }

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

        String role = Objects.equals(Utils.Session.userRole, "Admin") ? "Quản lý" : "Nhân viên";
        JLabel lblRole = new JLabel("Vai trò: " + role);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(Color.GRAY);
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

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
}