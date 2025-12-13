package JDBCUntils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Style {

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.decode("#2c3e50"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.decode("#34495e"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

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

        p.add(tf, BorderLayout.CENTER);

        return p;
    }

    public static JPanel createComboBoxWithLabel(JComboBox<String> box, String labelText, JButton btn1, JButton btn2) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setBackground(Color.WHITE);

        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(0, 35));

        JPanel btnPanel = new JPanel(new GridLayout(1, 0, 5, 0));
        btnPanel.setBackground(Color.WHITE);

        if (btn1 != null) {
            btnPanel.add(btn1);
        }

        if (btn2 != null) {
            btnPanel.add(btn2);
        }

        centerPanel.add(box, BorderLayout.CENTER);
        centerPanel.add(btnPanel, BorderLayout.EAST);
        p.add(centerPanel, BorderLayout.CENTER);

        return p;
    }

    public static JPanel createComboBoxWithLabel(JComboBox<String> box, String labelText) {
        return createComboBoxWithLabel(box, labelText, null, null);
    }

    public static JPanel createComboBoxWithLabel(JComboBox<String> box, String labelText, JButton btn) {
        return createComboBoxWithLabel(box, labelText, btn, null);
    }

    public static JPanel createTextFieldWithPlaceholder(JTextField textField, String labelText) {


        JPanel p = new JPanel(new BorderLayout(5, 5));
        textField.setText("Tìm kiếm...");
        textField.setForeground(Color.GRAY);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(createTitleLabel(labelText), BorderLayout.NORTH);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(0, 35));

        textField.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));

        p.add(textField, BorderLayout.CENTER);

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

        return p;
    }

    public static JLabel createSeparator() {
        JLabel lbl = new JLabel(" / ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.GRAY);
        return lbl;
    }

    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding (Đệm)
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static JButton createIconButton(String iconPath) {
        JButton btn = new JButton();

        try {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image img = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btn.setText("?");
            System.err.println("Lỗi load ảnh: " + iconPath);
        }

        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    public static JPanel createDatePanel(String labelText, JComboBox<String> day, JComboBox<String> month, JComboBox<String> year) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        JLabel lbl = createTitleLabel(labelText);
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
        p.add(lbl, BorderLayout.NORTH);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    public static JPanel createCard(String title, JLabel lblValue, Color color, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pText = new JPanel(new GridLayout(2, 1));
        pText.setOpaque(false); // Trong suốt để thấy màu nền

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(255, 255, 255, 200));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE); // Trắng đậm

        pText.add(lblTitle);
        pText.add(lblValue);

        JLabel lblIcon = new JLabel();
        try {
            // lblIcon.setIcon(new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        } catch (Exception e) {}
        lblIcon.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(pText, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        return card;
    }

    private static void createStyleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(80, 35));
    }
}
