package Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Class Style cung cấp các phương thức tiện ích (Utility methods) để chuẩn hóa giao diện người dùng (UI).
 * Bao gồm các thành phần: Buttons, Labels, Input Fields, ComboBoxes, Tables, và Dialogs.
 * Sử dụng phong cách thiết kế phẳng (Flat Design) kết hợp hiệu ứng 3D nhẹ và Animation.
 */
public class Style {

    // =================================================================================================================
    // GROUP 1: CORE UTILITIES & ANIMATION (CÁC HÀM XỬ LÝ MÀU SẮC VÀ HIỆU ỨNG CỐT LÕI)
    // =================================================================================================================

    /**
     * Trộn hai màu sắc lại với nhau dựa trên tỷ lệ phần trăm.
     * Thường dùng để tạo màu Hover (sáng hơn) hoặc Shadow (tối hơn).
     *
     * @param main Màu gốc.
     * @param mix Màu muốn trộn vào (thường là TRẮNG hoặc ĐEN).
     * @param ratio Tỷ lệ trộn (0.0 đến 1.0).
     * @return Đối tượng Color mới sau khi trộn.
     */
    private static Color mixColors(Color main, Color mix, double ratio) {
        int r = (int) (mix.getRed() * ratio + main.getRed() * (1 - ratio));
        int g = (int) (mix.getGreen() * ratio + main.getGreen() * (1 - ratio));
        int b = (int) (mix.getBlue() * ratio + main.getBlue() * (1 - ratio));
        return new Color(r, g, b);
    }

    /**
     * Trộn màu tịnh tiến giữa 2 màu (Linear Interpolation) dùng cho Animation.
     *
     * @param c1 Màu bắt đầu.
     * @param c2 Màu kết thúc.
     * @param ratio Tỷ lệ chuyển đổi (0.0 đến 1.0).
     * @return Màu tại thời điểm ratio.
     */
    private static Color blendColors(Color c1, Color c2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    /**
     * Cài đặt hiệu ứng chuyển màu viền (Glow Effect) cho cả TextField và TextArea.
     * Tự động phát hiện JScrollPane để tô viền đúng vị trí.
     *
     * @param comp Component cần cài đặt hiệu ứng (JTextField hoặc JTextArea).
     */
    public static void installFocusAnimation(JComponent comp) {
        final Color normalColor = Color.decode("#bdc3c7");
        final Color focusColor = Color.decode("#3498db");

        comp.addFocusListener(new java.awt.event.FocusAdapter() {
            private javax.swing.Timer timer;
            private float progress = 0f;

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                runAnimation(true);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                runAnimation(false);
            }

            private void runAnimation(boolean isEnter) {
                if (timer != null && timer.isRunning()) timer.stop();
                timer = new javax.swing.Timer(10, e -> {
                    if (isEnter) {
                        progress += 0.1f;
                        if (progress >= 1f) { progress = 1f; timer.stop(); }
                    } else {
                        progress -= 0.1f;
                        if (progress <= 0f) { progress = 0f; timer.stop(); }
                    }

                    Color newColor = blendColors(normalColor, focusColor, progress);

                    JComponent target = comp;
                    boolean isInsideScrollPane = false;

                    if (comp.getParent() instanceof JViewport && comp.getParent().getParent() instanceof JScrollPane) {
                        target = (JComponent) comp.getParent().getParent();
                        isInsideScrollPane = true;
                    }

                    if (isInsideScrollPane) {
                        target.setBorder(new javax.swing.border.LineBorder(newColor, 1));
                    } else {
                        target.setBorder(new javax.swing.border.CompoundBorder(
                                new javax.swing.border.LineBorder(newColor, 1),
                                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
                        ));
                    }

                    target.repaint();
                });
                timer.start();
            }
        });
    }

    // =================================================================================================================
    // GROUP 2: BASIC COMPONENTS (LABELS & SEPARATORS)
    // =================================================================================================================

    /**
     * Tạo nhãn tiêu đề lớn (Header) cho Form.
     *
     * @param text Nội dung tiêu đề.
     * @return JLabel được định dạng font to, đậm, căn giữa.
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.decode("#2c3e50"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Tạo nhãn tiêu đề nhỏ cho các trường nhập liệu.
     *
     * @param text Nội dung nhãn.
     * @return JLabel định dạng tiêu chuẩn.
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.decode("#34495e"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Tạo nhãn phân cách (dấu gạch chéo) dùng trong bộ chọn ngày tháng.
     *
     * @return JLabel chứa ký tự " / ".
     */
    public static JLabel createSeparator() {
        JLabel lbl = new JLabel(" / ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.GRAY);
        return lbl;
    }

    // =================================================================================================================
    // GROUP 3: BUTTONS (CÁC NÚT BẤM CUSTOM 3D)
    // =================================================================================================================

    /**
     * Tạo Button kích thước tiêu chuẩn với hiệu ứng 3D và bo góc.
     *
     * @param text Nội dung nút.
     * @param bgColor Màu nền chủ đạo.
     * @return JButton đã được custom.
     */
    public static JButton createButton(String text, Color bgColor) {
        return createCustom3DButton(text, bgColor, 0, 0, 14);
    }

    /**
     * Tạo Button kích thước nhỏ (thường dùng cho các thao tác phụ).
     *
     * @param text Nội dung nút.
     * @param bg Màu nền.
     * @return JButton kích thước 80x35.
     */
    public static JButton createSmallButton(String text, Color bg) {
        return createCustom3DButton(text, bg, 80, 35, 12);
    }

    /**
     * Hàm nội bộ tạo Button 3D tùy chỉnh (Core Button Logic).
     * Vẽ thủ công (Custom Painting) để tạo hiệu ứng bóng đổ và lún xuống khi nhấn.
     *
     * @param text Nội dung text.
     * @param mainColor Màu chủ đạo.
     * @param width Chiều rộng (0 nếu muốn tự động).
     * @param height Chiều cao (0 nếu muốn tự động).
     * @param fontSize Kích thước font chữ.
     * @return JButton hoàn chỉnh.
     */
    private static JButton createCustom3DButton(String text, Color mainColor, int width, int height, int fontSize) {
        Color shadowColor = mixColors(mainColor, Color.BLACK, 0.3); // Màu bóng tối hơn 30%
        Color hoverColor = mixColors(mainColor, Color.WHITE, 0.15); // Màu hover sáng hơn 15%

        JButton btn = new JButton(text) {
            @SuppressWarnings("FieldMayBeFinal")
            boolean isPressed = false;

            @SuppressWarnings("FieldMayBeFinal")
            boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int h = getHeight();
                int w = getWidth();
                int arc = 15;
                int shadowSize = 4;
                int yOffset = isPressed ? shadowSize : 0;

                // 1. Vẽ bóng (đế)
                if (!isPressed) {
                    g2.setColor(shadowColor);
                    g2.fill(new RoundRectangle2D.Float(0, shadowSize, w, h - shadowSize, arc, arc));
                }

                // 2. Vẽ mặt nút
                g2.setColor(isHovered ? hoverColor : mainColor);
                g2.fill(new RoundRectangle2D.Float(0, yOffset, w, h - shadowSize, arc, arc));

                // 3. Vẽ chữ căn giữa
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int stringWidth = fm.stringWidth(getText());
                int stringHeight = fm.getAscent();
                g2.drawString(getText(), (w - stringWidth) / 2, (h - shadowSize + stringHeight) / 2 - 2 + yOffset);

                g2.dispose();
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(Color.WHITE);
        btn.setFocusable(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (width > 0 && height > 0) {
            btn.setPreferredSize(new Dimension(width, height));
        } else {
            btn.setBorder(new EmptyBorder(10, 25, 14, 25));
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { setField(btn, "isPressed", true); }
            public void mouseReleased(MouseEvent e) { setField(btn, "isPressed", false); }
            public void mouseEntered(MouseEvent e) { setField(btn, "isHovered", true); }
            public void mouseExited(MouseEvent e) { setField(btn, "isHovered", false); }
        });

        return btn;
    }

    /**
     * Helper cập nhật trạng thái button thông qua Reflection (cho gọn code).
     */
    private static void setField(JButton btn, String fieldName, boolean value) {
        try {
            var field = btn.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(btn, value);
            btn.repaint();
        } catch (Exception ignored) {}
    }

    // =================================================================================================================
    // GROUP 4: INPUT (CÁC Ô NHẬP LIỆU)
    // =================================================================================================================

    /**
     * Tạo Panel chứa TextArea (Ghi chú) kèm nhãn tiêu đề.
     * Giống createTextFieldWithLabel nhưng có thanh cuộn và cao hơn.
     *
     * @param ta JTextArea cần bọc.
     * @param labelText Tiêu đề nhãn.
     * @return JPanel chứa Label và TextArea có Scroll.
     */
    public static JPanel createTextAreaWithLabel(JTextArea ta, String labelText) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        p.add(createTitleLabel(labelText), BorderLayout.NORTH);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new javax.swing.border.EmptyBorder(5, 10, 5, 10));
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setBorder(new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        installFocusAnimation(ta);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    /**
     * Tạo Panel chứa TextField kèm nhãn tiêu đề.
     *
     * @param tf JTextField cần bọc.
     * @param labelText Tiêu đề nhãn.
     * @return JPanel chứa Label và TextField.
     */
    public static JPanel createTextFieldWithLabel(JTextField tf, String labelText) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(0, 35));
        tf.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        installFocusAnimation(tf);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    /**
     * Tạo Panel chứa PasswordField kèm nhãn tiêu đề và Checkbox ẩn/hiện mật khẩu.
     *
     * @param pf JPasswordField cần bọc.
     * @param labelText Tiêu đề nhãn.
     * @param chkShowPass JCheckBox để bật tắt xem mật khẩu (có thể null nếu không cần).
     * @return JPanel chứa Label, PasswordField và Checkbox bên dưới.
     */
    public static JPanel createPasswordFieldWithLabel(JPasswordField pf, String labelText, JCheckBox chkShowPass) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);

        // Tăng chiều cao tối đa lên 90 để chứa đủ cả checkbox (nếu có)
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setPreferredSize(new Dimension(0, 35));
        pf.setEchoChar('•'); // Mặc định là ẩn
        pf.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        installFocusAnimation(pf);
        p.add(pf, BorderLayout.CENTER);

        // Xử lý Checkbox nếu được truyền vào

        if (chkShowPass != null) {
            chkShowPass.setBackground(Color.WHITE);
            chkShowPass.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Font nhỏ, nghiêng
            chkShowPass.setText("Hiển thị mật khẩu");
            chkShowPass.setForeground(Color.GRAY);
            chkShowPass.setFocusable(false); // Bỏ viền focus khi click cho đẹp

            // Sự kiện: Click vào checkbox để ẩn/hiện
            chkShowPass.addActionListener(e -> {
                if (chkShowPass.isSelected()) {
                    pf.setEchoChar((char) 0); // Hiện mật khẩu
                } else {
                    pf.setEchoChar('•'); // Ẩn mật khẩu
                }
            });

            p.add(chkShowPass, BorderLayout.SOUTH);
        }

        return p;
    }

    /**
     * Tạo thanh tìm kiếm kèm nút chức năng (Sắp xếp/Lọc) bên phải.
     * Có xử lý placeholder text "Tìm kiếm...".
     *
     * @param textField Ô nhập liệu tìm kiếm.
     * @param btnSort Nút chức năng bên cạnh.
     * @param labelText Tiêu đề thanh tìm kiếm.
     * @return JPanel hoàn chỉnh.
     */
    public static JPanel createSearchWithButtonPanel(JTextField textField, JButton btnSort, String labelText) {
        JPanel pRoot = new JPanel(new BorderLayout(5, 5));
        pRoot.setBackground(Color.WHITE);
        pRoot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        pRoot.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel pInputContainer = new JPanel(new BorderLayout(5, 0));
        pInputContainer.setBackground(Color.WHITE);

        textField.setText("Tìm kiếm...");
        textField.setForeground(Color.GRAY);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(0, 35));
        textField.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        installFocusAnimation(textField);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals("Tìm kiếm...")) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText("Tìm kiếm...");
                }
            }
        });

        btnSort.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSort.setBackground(Color.LIGHT_GRAY);
        btnSort.setForeground(Color.WHITE);
        btnSort.setPreferredSize(new Dimension(60, 30));
        btnSort.setFocusPainted(false);
        btnSort.setBorderPainted(false);
        btnSort.setOpaque(true);
        btnSort.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pInputContainer.add(textField, BorderLayout.CENTER);
        pInputContainer.add(btnSort, BorderLayout.EAST);
        pRoot.add(pInputContainer, BorderLayout.CENTER);

        return pRoot;
    }

    // =================================================================================================================
    // GROUP 5: SELECTION & CHECKBOX (COMBOBOX, DATE PICKER, CHECKBOX)
    // =================================================================================================================

    /**
     * Tạo Panel ComboBox nâng cao với các nút chức năng bên cạnh (Sử dụng GridBagLayout).
     *
     * @param box ComboBox chính.
     * @param labelText Tiêu đề nhãn.
     * @param btn1 Nút chức năng 1 (có thể null).
     * @param btn2 Nút chức năng 2 (có thể null).
     * @return JPanel chứa ComboBox và các nút.
     */
    public static JPanel createComboBoxWithLabel(JComboBox<?> box, String labelText, JButton btn1, JButton btn2) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // Cấu hình ComboBox
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.setPreferredSize(new Dimension(0, 35));
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        rowPanel.add(box, gbc);

        // Cấu hình Buttons
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        if (btn1 != null) {
            btn1.setPreferredSize(new Dimension(80, 35));
            gbc.gridx++;
            gbc.insets = new Insets(0, 10, 0, 0);
            rowPanel.add(btn1, gbc);
        }

        if (btn2 != null) {
            btn2.setPreferredSize(new Dimension(80, 35));
            gbc.gridx++;
            gbc.insets = new Insets(0, 10, 0, 0);
            rowPanel.add(btn2, gbc);
        }

        p.add(rowPanel, BorderLayout.CENTER);
        return p;
    }

    public static JPanel createComboBoxWithLabel(JComboBox<?> box, String labelText) {
        return createComboBoxWithLabel(box, labelText, null, null);
    }

    public static JPanel createComboBoxWithLabel(JComboBox<?> box, String labelText, JButton btn) {
        return createComboBoxWithLabel(box, labelText, btn, null);
    }

    /**
     * Tạo bộ chọn ngày tháng năm gồm 3 ComboBox riêng biệt.
     *
     * @param labelText Tiêu đề.
     * @param day ComboBox ngày.
     * @param month ComboBox tháng.
     * @param year ComboBox năm.
     * @return Panel chứa bộ chọn ngày.
     */
    public static JPanel createDatePanel(String labelText, JComboBox<String> day, JComboBox<String> month, JComboBox<String> year) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);

        createStyleComboBox(day);
        createStyleComboBox(month);
        createStyleComboBox(year);

        row.add(day);
        row.add(createSeparator());
        row.add(month);
        row.add(createSeparator());
        row.add(year);

        p.add(createTitleLabel(labelText), BorderLayout.NORTH);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    /**
     * Định dạng style chuẩn cho ComboBox con (dùng trong DatePanel).
     */
    private static void createStyleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(80, 33));
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Tạo CheckBox kèm nhãn tiêu đề phía trên.
     *
     * @param chk JCheckBox cần hiển thị.
     * @param labelText Tiêu đề nhóm.
     * @param textContent Nội dung bên cạnh CheckBox.
     * @return JPanel chứa CheckBox.
     */
    public static JPanel createCheckBoxWithLabel(JCheckBox chk, String labelText, String textContent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel pContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pContent.setBackground(Color.WHITE);
        pContent.setPreferredSize(new Dimension(0, 35));

        chk.setText(textContent);
        chk.setBackground(Color.WHITE);
        chk.setFocusable(false);
        chk.setFocusPainted(false);
        chk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chk.setForeground(Color.decode("#666666"));
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pContent.add(chk);
        p.add(pContent, BorderLayout.CENTER);
        return p;
    }

    // =================================================================================================================
    // GROUP 6: DATA DISPLAY (TABLES & DASHBOARD CARDS)
    // =================================================================================================================

    /**
     * Tạo JTable được bọc trong ScrollPane, có tiêu đề và danh sách nút chức năng.
     *
     * @param table JTable chứa dữ liệu.
     * @param titleText Tiêu đề bảng.
     * @param buttons Danh sách các nút chức năng (Thêm, Sửa, Xóa...) (Varargs).
     * @return JPanel hoàn chỉnh chứa bảng.
     */
    public static JPanel createTableWithLabel(JTable table, String titleText, JButton... buttons) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#ecf0f1"));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.decode("#ecf0f1"));
        table.getTableHeader().setForeground(Color.decode("#2c3e50"));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#bdc3c7")));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 45));
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 0));

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#2c3e50"));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        if (buttons != null && buttons.length > 0) {
            JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            btnWrapper.setBackground(Color.WHITE);
            for (JButton btn : buttons) {
                btnWrapper.add(btn);
            }
            headerPanel.add(btnWrapper, BorderLayout.EAST);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 150));

        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#bdc3c7")));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo thẻ Dashboard thống kê số liệu với hiệu ứng Hover đổi màu nền.
     * Khi di chuột vào: Nền sáng hơn một chút để tạo điểm nhấn, kích thước giữ nguyên.
     *
     * @param title Tiêu đề thẻ (ví dụ: Doanh thu).
     * @param lblValue JLabel hiển thị giá trị số liệu.
     * @param color Màu nền chủ đạo của thẻ.
     * @param iconPath Đường dẫn icon minh họa.
     * @return JPanel thẻ thống kê hoàn chỉnh.
     */
    public static JPanel createCard(String title, JLabel lblValue, Color color, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Phần Text (Tiêu đề + Số liệu)
        JPanel pText = new JPanel(new GridLayout(2, 1));
        pText.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);

        pText.add(lblTitle);
        pText.add(lblValue);

        // Phần Icon (Cố định kích thước 40x40)
        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.RIGHT);
        lblIcon.setIcon(new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));

        card.add(pText, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        // Viền mỏng tạo khối
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // XỬ LÝ SỰ KIỆN HOVER (CHỈ ĐỔI MÀU NỀN)
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Pha thêm 10% màu trắng vào nền -> Sáng hơn
                card.setBackground(mixColors(color, Color.WHITE, 0.1));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Trả về màu gốc
                card.setBackground(color);
            }
        });

        return card;
    }

    // =================================================================================================================
    // GROUP 7: DIALOGS & NOTIFICATIONS (HỘP THOẠI VÀ THÔNG BÁO)
    // =================================================================================================================

    /**
     * Hiển thị thông báo Thành công (Màu xanh lá).
     *
     * @param parent Component cha.
     * @param msg Nội dung thông báo.
     */
    public static void showSuccess(Component parent, String msg) {
        showCustomAlert(parent, msg, true);
    }

    /**
     * Hiển thị thông báo Lỗi (Màu đỏ).
     *
     * @param parent Component cha.
     * @param msg Nội dung lỗi.
     */
    public static void showError(Component parent, String msg) {
        showCustomAlert(parent, msg, false);
    }

    /**
     * Hiển thị hộp thoại Xác nhận (Yes/No).
     *
     * @param parent Component cha.
     * @param msg Câu hỏi xác nhận.
     * @return true nếu chọn "Xác nhận", false nếu chọn "Hủy bỏ".
     */
    public static boolean showConfirm(Component parent, String msg) {
        final boolean[] result = {false};
        Color mainColor = new Color(230, 126, 34);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JPanel pHeader = getJPanel(mainColor, result, dialog);

        // Content
        JPanel pContent = new JPanel(new BorderLayout());
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextPane txtMsg = new JTextPane();
        txtMsg.setText(msg);
        txtMsg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMsg.setForeground(Color.decode("#2c3e50"));
        txtMsg.setEnabled(false);
        txtMsg.setDisabledTextColor(Color.decode("#2c3e50"));
        txtMsg.setOpaque(false);

        javax.swing.text.StyledDocument doc = txtMsg.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        txtMsg.setSize(new Dimension(400, Short.MAX_VALUE));
        txtMsg.setPreferredSize(new Dimension(400, txtMsg.getPreferredSize().height));
        pContent.add(txtMsg, BorderLayout.CENTER);

        // Buttons
        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pButton.setBackground(Color.WHITE);
        pButton.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton btnYes = createButton("Xác Nhận", mainColor);
        JButton btnNo = createButton("Hủy Bỏ", Color.GRAY);
        btnYes.setPreferredSize(new Dimension(110, 35));
        btnNo.setPreferredSize(new Dimension(110, 35));
        btnNo.setBorder(BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1));

        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        btnNo.addActionListener(e -> { result[0] = false; dialog.dispose(); });

        btnYes.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btnYes.setBackground(mainColor.darker()); }
            public void mouseExited(MouseEvent evt) { btnYes.setBackground(mainColor); }
        });
        btnNo.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btnNo.setBackground(Color.GRAY.darker()); }
            public void mouseExited(MouseEvent evt) { btnNo.setBackground(Color.GRAY); }
        });

        pButton.add(btnYes);
        pButton.add(btnNo);

        dialog.add(pHeader, BorderLayout.NORTH);
        dialog.add(pContent, BorderLayout.CENTER);
        dialog.add(pButton, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnYes);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JPanel getJPanel(Color mainColor, boolean[] result, JDialog dialog) {
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(mainColor);
        pHeader.setPreferredSize(new Dimension(0, 40));
        pHeader.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel lblTitle = new JLabel("XÁC NHẬN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        pHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblClose = new JLabel("×");
        lblClose.setFont(new Font("Arial", Font.BOLD, 28));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { result[0] = false; dialog.dispose(); }
        });
        pHeader.add(lblClose, BorderLayout.EAST);
        return pHeader;
    }

    /**
     * Hàm nội bộ hiển thị Alert Dialog (Dùng chung cho Success/Error).
     */
    private static void showCustomAlert(Component parent, String msg, boolean isSuccess) {
        Color mainColor = isSuccess ? new Color(46, 204, 113) : new Color(231, 76, 60);
        String title = isSuccess ? "THÀNH CÔNG" : "THẤT BẠI";

        Window owner = null;
        if (parent instanceof Window) owner = (Window) parent;
        else if (parent != null) owner = SwingUtilities.getWindowAncestor(parent);

        JDialog dialog = new JDialog(owner);
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JPanel pHeader = getJPanel(mainColor, title, dialog);

        JPanel pContent = new JPanel(new BorderLayout());
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextPane txtMsg = new JTextPane();
        txtMsg.setText(msg);
        txtMsg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtMsg.setForeground(Color.decode("#2c3e50"));
        txtMsg.setEnabled(false);
        txtMsg.setDisabledTextColor(Color.decode("#2c3e50"));
        txtMsg.setOpaque(false);

        javax.swing.text.StyledDocument doc = txtMsg.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        txtMsg.setSize(new Dimension(400, Short.MAX_VALUE));
        txtMsg.setPreferredSize(new Dimension(400, txtMsg.getPreferredSize().height));
        pContent.add(txtMsg, BorderLayout.CENTER);

        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pButton.setBackground(Color.WHITE);
        pButton.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton btnOK = createButton("Đồng ý", mainColor);
        btnOK.setPreferredSize(new Dimension(110, 35));
        btnOK.addActionListener(e -> dialog.dispose());
        btnOK.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btnOK.setBackground(mainColor.darker()); }
            public void mouseExited(MouseEvent evt) { btnOK.setBackground(mainColor); }
        });

        pButton.add(btnOK);

        dialog.add(pHeader, BorderLayout.NORTH);
        dialog.add(pContent, BorderLayout.CENTER);
        dialog.add(pButton, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnOK);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * Helper tạo Header Panel cho Dialog.
     */
    private static JPanel getJPanel(Color mainColor, String title, JDialog dialog) {
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(mainColor);
        pHeader.setPreferredSize(new Dimension(0, 40));
        pHeader.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        pHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblClose = new JLabel("×");
        lblClose.setFont(new Font("Arial", Font.BOLD, 28));
        lblClose.setForeground(Color.WHITE);
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dialog.dispose(); }
        });
        pHeader.add(lblClose, BorderLayout.EAST);
        return pHeader;
    }
}