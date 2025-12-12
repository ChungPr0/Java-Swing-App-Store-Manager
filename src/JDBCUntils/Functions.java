package JDBCUntils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Functions {
    public static void createMenuButton(JLabel label) {

        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(Color.decode("#2c3e50"));

        label.setBorder(new EmptyBorder(10, 15, 10, 15));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- HIỆU ỨNG DI CHUỘT (HOVER) ---
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(Color.decode("#1abc9c"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(Color.decode("#2c3e50"));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Xử lý sự kiện click ở đây hoặc gọi hàm riêng
                // Ví dụ: resetActiveColor(); label.setBackground(Color.RED);
            }
        });
    }

    public static void addPlaceholderStyle(JTextField textField) {

        Font originalFont = textField.getFont();
        Font italicFont = originalFont.deriveFont(Font.ITALIC);

        textField.setText("Tìm kiếm...");
        textField.setForeground(Color.GRAY); // Màu xám mờ
        textField.setFont(italicFont);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals("Tìm kiếm...")) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    textField.setFont(originalFont);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText("Tìm kiếm...");
                    textField.setForeground(Color.GRAY);
                    textField.setFont(italicFont);
                }
            }
        });
    }
}
