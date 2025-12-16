package Main;

import CustomerForm.CustomerManagerPanel;
import HomeForm.HomeManagerPanel;
import InvoiceForm.InvoiceManagerPanel;
import JDBCUtils.Session;
import Login.ChangePasswordDialog;
import Login.LoginForm;
import ProductForm.ProductManagerPanel;
import StaffForm.StaffManagerPanel;
import SupplierForm.SupplierManagerPanel;

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

        // A. MENU BAR (Thanh menu trên cùng)
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(Color.decode("#2c3e50"));
        menuPanel.setPreferredSize(new Dimension(0, 50));

        // A.1 Menu Trái (Các nút chuyển Tab)
        JPanel leftMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftMenuPanel.setBackground(Color.decode("#2c3e50"));

        btnHome = createMenuButton("TRANG CHỦ");
        btnStaff = createMenuButton("NHÂN VIÊN");
        btnSupplier = createMenuButton("NHÀ CUNG CẤP");
        btnCustomer = createMenuButton("KHÁCH HÀNG");
        btnProduct = createMenuButton("SẢN PHẨM");
        btnInvoice = createMenuButton("HÓA ĐƠN");

        leftMenuPanel.add(btnHome);
        leftMenuPanel.add(btnStaff);
        leftMenuPanel.add(btnSupplier);
        leftMenuPanel.add(btnCustomer);
        leftMenuPanel.add(btnProduct);
        leftMenuPanel.add(btnInvoice);

        // A.2 Menu Phải (Nút Tài khoản)
        JPanel rightMenuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightMenuPanel.setBackground(Color.decode("#2c3e50"));

        JButton btnInfo = createMenuButton("TÀI KHOẢN");
        setupUserPopup(btnInfo); // Cài đặt logic Popup cho nút này
        rightMenuPanel.add(btnInfo);

        menuPanel.add(leftMenuPanel, BorderLayout.WEST);
        menuPanel.add(rightMenuPanel, BorderLayout.EAST);

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
        btnHome.addActionListener(_ -> switchTab("HOME", homePanel, btnHome));
        btnStaff.addActionListener(_ -> switchTab("STAFF", staffPanel, btnStaff));
        btnSupplier.addActionListener(_ -> switchTab("SUPPLIER", supplierPanel, btnSupplier));
        btnCustomer.addActionListener(_ -> switchTab("CUSTOMER", customerPanel, btnCustomer));
        btnProduct.addActionListener(_ -> switchTab("PRODUCT", productPanel, btnProduct));
        btnInvoice.addActionListener(_ -> switchTab("INVOICE", invoicePanel, btnInvoice));

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

        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setPreferredSize(new Dimension(130, 50));
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
     * Hàm Public để gọi từ các form con (ví dụ: Mở hóa đơn từ trang chủ)
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

        btnChangePass.addActionListener(_ -> {
            popupProfile.setVisible(false);
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ChangePasswordDialog(parent).setVisible(true);
        });

        JButton btnLogout = createButton("Đăng Xuất", Color.decode("#e74c3c"));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(220, 35));

        btnLogout.addActionListener(_ -> {
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

        //--- Phần 12: Sự kiện Click: Hiện Popup --
        btnTarget.addActionListener(_ -> {
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