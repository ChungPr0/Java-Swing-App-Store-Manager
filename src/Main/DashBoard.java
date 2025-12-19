package Main;

import Main.HomeManager.HomeManagerPanel;
import Main.StaffManager.StaffManagerPanel;
import Main.SupplierManager.SupplierManagerPanel;
import Main.CustomerManager.CustomerManagerPanel;
import Main.ProductManager.ProductManagerPanel;
import Main.InvoiceManager.InvoiceManagerPanel;
import Main.LoginManager.LoginForm;
import Main.LoginManager.ChangePasswordDialog;
import JDBCUtils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static JDBCUtils.Style.*;

public class DashBoard extends JFrame {

    // --- 1. KHAI BÁO BIẾN ---
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Các nút Menu
    private JButton btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnInvoice;
    private JButton currentActiveButton; // Biến theo dõi nút đang được chọn

    // Các Panel chức năng (Cache để tránh new lại nhiều lần gây chậm)
    private HomeManagerPanel homePanel;
    private StaffManagerPanel staffPanel;
    private SupplierManagerPanel supplierPanel;
    private CustomerManagerPanel customerPanel;
    private ProductManagerPanel productPanel;
    private InvoiceManagerPanel invoicePanel;

    public DashBoard() {
        super("Quản Lý Cửa Hàng");
        this.setSize(950, 650);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        initUI();
        addEvents();
    }

    // --- 2. KHỞI TẠO GIAO DIỆN ---
    private void initUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());

        // A. MENU BAR (Sửa lại toàn bộ đoạn này)
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS)); // Xếp hàng ngang
        menuPanel.setBackground(Color.decode("#2c3e50"));
        menuPanel.setPreferredSize(new Dimension(0, 50));

        // 1. Tạo và thêm các nút bên Trái
        btnHome = createMenuButton("TRANG CHỦ");
        btnStaff = createMenuButton("NHÂN VIÊN");
        btnSupplier = createMenuButton("NHÀ CUNG CẤP");
        btnCustomer = createMenuButton("KHÁCH HÀNG");
        btnProduct = createMenuButton("SẢN PHẨM");
        btnInvoice = createMenuButton("HÓA ĐƠN");

        menuPanel.add(btnHome);
        menuPanel.add(btnStaff);
        menuPanel.add(btnSupplier);
        menuPanel.add(btnCustomer);
        menuPanel.add(btnProduct);
        menuPanel.add(btnInvoice);

        // Thêm "Lò xo" ở giữa (Quan trọng: Đẩy nút Tài khoản sang phải)
        menuPanel.add(Box.createHorizontalGlue());

        JButton btnInfo = createMenuButton("TÀI KHOẢN");
        setupUserPopup(btnInfo);

        menuPanel.add(btnInfo);

        // B. CONTENT AREA (Khu vực nội dung chính)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Khởi tạo các màn hình con
        homePanel = new HomeManagerPanel();
        staffPanel = new StaffManagerPanel();
        supplierPanel = new SupplierManagerPanel();
        customerPanel = new CustomerManagerPanel();
        productPanel = new ProductManagerPanel();
        invoicePanel = new InvoiceManagerPanel();

        // Thêm vào CardLayout với tên định danh
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(staffPanel, "STAFF");
        contentPanel.add(supplierPanel, "SUPPLIER");
        contentPanel.add(customerPanel, "CUSTOMER");
        contentPanel.add(productPanel, "PRODUCT");
        contentPanel.add(invoicePanel, "INVOICE");

        mainContainer.add(menuPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);

        this.setContentPane(mainContainer);

        // C. PHÂN QUYỀN (Ẩn nút nếu không phải Admin)
        if (!JDBCUtils.Session.isAdmin()) {
            btnStaff.setVisible(false);
            btnSupplier.setVisible(false);
        }
    }

    // --- 3. XỬ LÝ SỰ KIỆN ---
    private void addEvents() {
        // Gán sự kiện chuyển Tab cho từng nút
        btnHome.addActionListener(e -> switchTab("HOME", homePanel, btnHome));
        btnStaff.addActionListener(e -> switchTab("STAFF", staffPanel, btnStaff));
        btnSupplier.addActionListener(e -> switchTab("SUPPLIER", supplierPanel, btnSupplier));
        btnCustomer.addActionListener(e -> switchTab("CUSTOMER", customerPanel, btnCustomer));
        btnProduct.addActionListener(e -> switchTab("PRODUCT", productPanel, btnProduct));
        btnInvoice.addActionListener(e -> switchTab("INVOICE", invoicePanel, btnInvoice));

        // Mặc định chọn Trang chủ khi mở lên
        updateActiveButton(btnHome);
    }

    /**
     * Hàm hỗ trợ chuyển Tab và làm mới dữ liệu
     */
    private void switchTab(String cardName, Object panel, JButton btn) {
        cardLayout.show(contentPanel, cardName);

        // Gọi hàm refreshData tương ứng với từng loại Panel
        if (panel instanceof HomeManagerPanel) ((HomeManagerPanel) panel).refreshData();
        else if (panel instanceof StaffManagerPanel) ((StaffManagerPanel) panel).refreshData();
        else if (panel instanceof SupplierManagerPanel) ((SupplierManagerPanel) panel).refreshData();
        else if (panel instanceof CustomerManagerPanel) ((CustomerManagerPanel) panel).refreshData();
        else if (panel instanceof ProductManagerPanel) ((ProductManagerPanel) panel).refreshData();
        else if (panel instanceof InvoiceManagerPanel) ((InvoiceManagerPanel) panel).refreshData();

        updateActiveButton(btn);
    }

    /**
     * Hàm đổi màu nút đang được chọn (Active)
     */
    private void updateActiveButton(JButton activeBtn) {
        currentActiveButton = activeBtn;

        JButton[] btns = {btnHome, btnStaff, btnSupplier, btnCustomer, btnProduct, btnInvoice};
        for (JButton btn : btns) {
            if (btn == activeBtn) {
                btn.setBackground(Color.decode("#3498db")); // Màu Xanh (Active)
            } else {
                btn.setBackground(Color.decode("#2c3e50")); // Màu Đen (Inactive)
            }
        }
    }

    /**
     * Hàm tạo nút Menu với style chuẩn
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
                // Hover vào luôn hiện màu xanh
                btn.setBackground(Color.decode("#3498db"));
            }

            public void mouseExited(MouseEvent e) {
                // Chỉ trả về màu đen nếu nút này KHÔNG phải là nút đang chọn
                if (btn != currentActiveButton) {
                    btn.setBackground(Color.decode("#2c3e50"));
                }
            }
        });

        return btn;
    }

    /**
     * Hàm Public để gọi từ Dashboard (Click vào Sản phẩm -> Sang tab sản phẩm)
     */
    public void showProductAndLoad(int proID) {
        cardLayout.show(contentPanel, "PRODUCT");
        updateActiveButton(btnProduct);

        if (productPanel != null) {
            productPanel.loadDetail(proID);
        }
    }

    /**
     * Hàm Public để gọi từ Dashboard (Click vào Top khách hàng -> Sang tab khách hàng)
     */
    public void showCustomerAndLoad(int cusID) {
        cardLayout.show(contentPanel, "CUSTOMER");
        updateActiveButton(btnCustomer);
        if (customerPanel != null) {
            customerPanel.loadDetail(cusID);
        }
    }

    /**
     * Hàm Public để gọi từ Dashboard (Click vào Top hóa đơn -> Sang tab hóa đơn)
     */
    public void showInvoiceAndLoad(int invID) {
        cardLayout.show(contentPanel, "INVOICE");
        updateActiveButton(btnInvoice);
        if (invoicePanel != null) {
            invoicePanel.loadDetail(invID);
        }
    }

    /**
     * CẤU HÌNH POPUP TÀI KHOẢN
     */
    private void setupUserPopup(JButton btnTarget) {
        // --- Phần 1: Tạo giao diện Popup ---
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

        JLabel lblUser = new JLabel("Tài khoản: " + JDBCUtils.Session.loggedInStaffName);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        String role = "Nhân viên";
        if (Objects.equals(JDBCUtils.Session.userRole, "Admin")) {
            role = "Quản lý";
        }

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
        pContent.add(Box.createVerticalStrut(5));

        popupProfile.add(pContent);

        //--- Phần 12: Sự kiện Click: Hiện Popup --
        btnTarget.addActionListener(e -> {
            if (!popupProfile.isVisible()) {
                popupProfile.show(btnTarget,
                        btnTarget.getWidth() - popupProfile.getPreferredSize().width,
                        btnTarget.getHeight());
            } else {
                popupProfile.setVisible(false);
            }
        });
    }
}