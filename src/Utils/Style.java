package Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Utility class providing methods to standardize the User Interface (UI).
 * Includes components: Buttons, Labels, Input Fields, ComboBoxes, Tables, and Dialogs.
 * Uses Flat Design style combined with light 3D effects and Animation.
 */
public class Style {

    /**
     * Mixes two colors based on a percentage ratio.
     * Often used to create Hover (lighter) or Shadow (darker) colors.
     *
     * @param main  The base color.
     * @param mix   The color to mix in (usually WHITE or BLACK).
     * @param ratio The mixing ratio (0.0 to 1.0).
     * @return A new Color object after mixing.
     */
    private static Color mixColors(Color main, Color mix, double ratio) {
        int r = (int) (mix.getRed() * ratio + main.getRed() * (1 - ratio));
        int g = (int) (mix.getGreen() * ratio + main.getGreen() * (1 - ratio));
        int b = (int) (mix.getBlue() * ratio + main.getBlue() * (1 - ratio));
        return new Color(r, g, b);
    }

    /**
     * Linearly interpolates between two colors for Animation.
     *
     * @param c1    Start color.
     * @param c2    End color.
     * @param ratio Transition ratio (0.0 to 1.0).
     * @return The color at the specific ratio.
     */
    private static Color blendColors(Color c1, Color c2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    /**
     * Installs a glow border effect for TextField and TextArea.
     * Automatically detects JScrollPane to paint the border in the correct position.
     *
     * @param comp The component to install the effect on (JTextField or JTextArea).
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
                        if (progress >= 1f) {
                            progress = 1f;
                            timer.stop();
                        }
                    } else {
                        progress -= 0.1f;
                        if (progress <= 0f) {
                            progress = 0f;
                            timer.stop();
                        }
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

    /**
     * Creates a large header label for Forms.
     *
     * @param text The title content.
     * @return A JLabel formatted with large, bold font and centered alignment.
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.decode("#2c3e50"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * Creates a small title label for input fields.
     *
     * @param text The label content.
     * @return A JLabel with standard formatting.
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.decode("#34495e"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates a separator label (slash) used in date pickers.
     *
     * @return A JLabel containing " / ".
     */
    public static JLabel createSeparator() {
        JLabel lbl = new JLabel(" / ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.GRAY);
        return lbl;
    }

    /**
     * Creates a standard sized button with 3D effect and rounded corners.
     *
     * @param text    The button text.
     * @param bgColor The main background color.
     * @return A customized JButton.
     */
    public static JButton createButton(String text, Color bgColor) {
        return createCustom3DButton(text, bgColor, 0, 0, 14);
    }

    /**
     * Creates a small button (usually for secondary actions).
     *
     * @param text The button text.
     * @param bg   The background color.
     * @return A JButton with size 80x35.
     */
    public static JButton createSmallButton(String text, Color bg) {
        return createCustom3DButton(text, bg, 80, 35, 12);
    }

    /**
     * Internal method to create a custom 3D button.
     * Uses custom painting to create shadow and pressed effects.
     *
     * @param text      The button text.
     * @param mainColor The main color.
     * @param width     Width (0 for automatic).
     * @param height    Height (0 for automatic).
     * @param fontSize  Font size.
     * @return A complete JButton.
     */
    private static JButton createCustom3DButton(String text, Color mainColor, int width, int height, int fontSize) {
        Color shadowColor = mixColors(mainColor, Color.BLACK, 0.3);
        Color hoverColor = mixColors(mainColor, Color.WHITE, 0.15);

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

                if (!isPressed) {
                    g2.setColor(shadowColor);
                    g2.fill(new RoundRectangle2D.Float(0, shadowSize, w, h - shadowSize, arc, arc));
                }

                g2.setColor(isHovered ? hoverColor : mainColor);
                g2.fill(new RoundRectangle2D.Float(0, yOffset, w, h - shadowSize, arc, arc));

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
            public void mousePressed(MouseEvent e) {
                setField(btn, "isPressed", true);
            }

            public void mouseReleased(MouseEvent e) {
                setField(btn, "isPressed", false);
            }

            public void mouseEntered(MouseEvent e) {
                setField(btn, "isHovered", true);
            }

            public void mouseExited(MouseEvent e) {
                setField(btn, "isHovered", false);
            }
        });

        return btn;
    }

    /**
     * Helper to update button state via Reflection.
     *
     * @param btn       The button to update.
     * @param fieldName The field name to set.
     * @param value     The boolean value to set.
     */
    private static void setField(JButton btn, String fieldName, boolean value) {
        try {
            var field = btn.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(btn, value);
            btn.repaint();
        } catch (Exception ignored) {
        }
    }

    /**
     * Creates a Panel containing a TextArea (Note) with a title label.
     * Similar to createTextFieldWithLabel but with scrollbar and taller height.
     *
     * @param ta        The JTextArea to wrap.
     * @param labelText The label title.
     * @return A JPanel containing Label and TextArea with ScrollPane.
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
     * Creates a Panel containing a TextField with a title label.
     *
     * @param tf        The JTextField to wrap.
     * @param labelText The label title.
     * @return A JPanel containing Label and TextField.
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
     * Creates a Panel containing a TextField with a button on the right.
     *
     * @param tf        The input JTextField.
     * @param btn       The function JButton next to it.
     * @param labelText The label title.
     * @return A complete JPanel.
     */
    public static JPanel createTextFieldWithButton(JTextField tf, JButton btn, String labelText) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel pInput = new JPanel(new BorderLayout(5, 0));
        pInput.setBackground(Color.WHITE);

        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(0, 35));
        tf.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        installFocusAnimation(tf);

        Dimension d = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(d.width > 0 ? d.width : 80, 35));

        pInput.add(tf, BorderLayout.CENTER);
        pInput.add(btn, BorderLayout.EAST);

        p.add(pInput, BorderLayout.CENTER);
        return p;
    }

    /**
     * Creates a Panel containing a PasswordField with a title label and a 'Show Password' checkbox.
     *
     * @param pf          The JPasswordField to wrap.
     * @param labelText   The label title.
     * @param chkShowPass The JCheckBox to toggle password visibility (can be null).
     * @return A JPanel containing Label, PasswordField, and Checkbox below.
     */
    public static JPanel createPasswordFieldWithLabel(JPasswordField pf, String labelText, JCheckBox chkShowPass) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);

        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        p.add(createTitleLabel(labelText), BorderLayout.NORTH);

        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setPreferredSize(new Dimension(0, 35));
        pf.setEchoChar('•');
        pf.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(Color.decode("#bdc3c7"), 1),
                new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        installFocusAnimation(pf);
        p.add(pf, BorderLayout.CENTER);

        if (chkShowPass != null) {
            chkShowPass.setBackground(Color.WHITE);
            chkShowPass.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            chkShowPass.setText("Hiển thị mật khẩu");
            chkShowPass.setForeground(Color.GRAY);
            chkShowPass.setFocusable(false);

            chkShowPass.addActionListener(e -> {
                if (chkShowPass.isSelected()) {
                    pf.setEchoChar((char) 0);
                } else {
                    pf.setEchoChar('•');
                }
            });

            p.add(chkShowPass, BorderLayout.SOUTH);
        }

        return p;
    }

    /**
     * Creates a simple search bar without buttons, with a title.
     *
     * @param textField       The search input field.
     * @param labelText       The title of the search bar.
     * @param placeholderText The placeholder text.
     * @return A JPanel containing the search field.
     */
    public static JPanel createSearchPanel(JTextField textField, String labelText, String placeholderText) {
        JPanel pRoot = new JPanel(new BorderLayout(5, 5));
        pRoot.setBackground(Color.WHITE);
        pRoot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        pRoot.add(createTitleLabel(labelText), BorderLayout.NORTH);

        textField.setText(placeholderText);
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
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholderText);
                }
            }
        });

        pRoot.add(textField, BorderLayout.CENTER);
        return pRoot;
    }

    /**
     * Creates a search bar with a function button (Sort/Filter) on the right.
     *
     * @param textField       The search input field.
     * @param btnSort         The function button next to it.
     * @param labelText       The title of the search bar.
     * @param placeholderText The placeholder text.
     * @return A complete JPanel.
     */
    public static JPanel createSearchWithButtonPanel(JTextField textField, JButton btnSort, String labelText, String placeholderText) {
        JPanel pRoot = new JPanel(new BorderLayout(5, 5));
        pRoot.setBackground(Color.WHITE);
        pRoot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        pRoot.add(createTitleLabel(labelText), BorderLayout.NORTH);

        JPanel pInputContainer = new JPanel(new BorderLayout(5, 0));
        pInputContainer.setBackground(Color.WHITE);

        textField.setText(placeholderText);
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
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholderText);
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

    /**
     * Creates an advanced ComboBox Panel with function buttons next to it.
     *
     * @param box       The main ComboBox.
     * @param labelText The label title.
     * @param btn1      Function button 1 (can be null).
     * @param btn2      Function button 2 (can be null).
     * @return A JPanel containing the ComboBox and buttons.
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

        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.setPreferredSize(new Dimension(0, 35));
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        rowPanel.add(box, gbc);

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
     * Creates a date picker consisting of 3 separate ComboBoxes (Day, Month, Year).
     *
     * @param labelText The title.
     * @param day       The Day ComboBox.
     * @param month     The Month ComboBox.
     * @param year      The Year ComboBox.
     * @return A Panel containing the date picker.
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
     * Standardizes style for child ComboBoxes (used in DatePanel).
     *
     * @param box The ComboBox to style.
     */
    private static void createStyleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(80, 33));
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Creates a CheckBox with a title label above it.
     *
     * @param chk         The JCheckBox to display.
     * @param labelText   The group title.
     * @param textContent The content next to the CheckBox.
     * @return A JPanel containing the CheckBox.
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

    /**
     * Creates a JTable wrapped in a ScrollPane, with a title and a list of function buttons.
     *
     * @param table     The JTable containing data.
     * @param titleText The table title.
     * @param buttons   List of function buttons (Add, Edit, Delete...) (Varargs).
     * @return A complete JPanel containing the table.
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
     * Creates a Section (Block) with a title and buttons, similar to Table Header.
     * Used to wrap other components like Filters, Charts...
     *
     * @param content   The content component inside.
     * @param titleText The section title.
     * @param buttons   Function buttons (if any).
     * @return A complete JPanel.
     */
    public static JPanel createSectionWithHeader(JComponent content, String titleText, JButton... buttons) {
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

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1));

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#bdc3c7")),
                new EmptyBorder(5, 10, 5, 0)
        ));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a Dashboard statistic card with Hover background color change effect.
     * When hovering: The background becomes slightly lighter to create a highlight, size remains the same.
     *
     * @param title    Card title (e.g., Revenue).
     * @param lblValue JLabel displaying the statistic value.
     * @param color    Main background color of the card.
     * @param iconPath Path to the illustration icon.
     * @return A complete statistic card JPanel.
     */
    public static JPanel createCard(String title, JLabel lblValue, Color color, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pText = new JPanel(new GridLayout(2, 1));
        pText.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);

        pText.add(lblTitle);
        pText.add(lblValue);

        JLabel lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.RIGHT);
        lblIcon.setIcon(new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));

        card.add(pText, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(mixColors(color, Color.WHITE, 0.1));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(color);
            }
        });

        return card;
    }

    /**
     * Displays a Success notification (Green).
     *
     * @param parent The parent component.
     * @param msg    The notification message.
     */
    public static void showSuccess(Component parent, String msg) {
        showCustomAlert(parent, msg, true);
    }

    /**
     * Displays an Error notification (Red).
     *
     * @param parent The parent component.
     * @param msg    The error message.
     */
    public static void showError(Component parent, String msg) {
        showCustomAlert(parent, msg, false);
    }

    /**
     * Displays a Confirmation dialog (Yes/No).
     *
     * @param parent The parent component.
     * @param msg    The confirmation question.
     * @return true if "Confirm" is selected, false if "Cancel" is selected.
     */
    public static boolean showConfirm(Component parent, String msg) {
        final boolean[] result = {false};
        Color mainColor = new Color(230, 126, 34);

        Window owner = null;
        if (parent != null) {
            if (parent instanceof Window) {
                owner = (Window) parent;
            } else {
                owner = SwingUtilities.getWindowAncestor(parent);
            }
        }
        JDialog dialog = new JDialog(owner);

        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        ((JPanel) dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JPanel pHeader = getJPanel(mainColor, result, dialog);

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

        JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pButton.setBackground(Color.WHITE);
        pButton.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton btnYes = createButton("Xác Nhận", mainColor);
        JButton btnNo = createButton("Hủy Bỏ", Color.GRAY);
        btnYes.setPreferredSize(new Dimension(110, 35));
        btnNo.setPreferredSize(new Dimension(110, 35));
        btnNo.setBorder(BorderFactory.createLineBorder(Color.decode("#bdc3c7"), 1));

        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        btnNo.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        btnYes.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnYes.setBackground(mainColor.darker());
            }

            public void mouseExited(MouseEvent evt) {
                btnYes.setBackground(mainColor);
            }
        });
        btnNo.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnNo.setBackground(Color.GRAY.darker());
            }

            public void mouseExited(MouseEvent evt) {
                btnNo.setBackground(Color.GRAY);
            }
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
            public void mouseClicked(MouseEvent e) {
                result[0] = false;
                dialog.dispose();
            }
        });
        pHeader.add(lblClose, BorderLayout.EAST);
        return pHeader;
    }

    /**
     * Internal method to display Alert Dialog (Shared for Success/Error).
     *
     * @param parent    The parent component.
     * @param msg       The message content.
     * @param isSuccess True for Success (Green), False for Error (Red).
     */
    private static void showCustomAlert(Component parent, String msg, boolean isSuccess) {
        Color mainColor = isSuccess ? new Color(46, 204, 113) : new Color(231, 76, 60);
        String title = isSuccess ? "THÀNH CÔNG" : "THẤT BẠI";

        Window owner = null;
        if (parent != null) {
            if (parent instanceof Window) {
                owner = (Window) parent;
            } else {
                owner = SwingUtilities.getWindowAncestor(parent);
            }
        }
        JDialog dialog = new JDialog(owner);

        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        ((JPanel) dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

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
            public void mouseEntered(MouseEvent evt) {
                btnOK.setBackground(mainColor.darker());
            }

            public void mouseExited(MouseEvent evt) {
                btnOK.setBackground(mainColor);
            }
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
     * Helper to create Header Panel for Dialog.
     *
     * @param mainColor The background color.
     * @param title     The title text.
     * @param dialog    The dialog instance.
     * @return A JPanel header.
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
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        });
        pHeader.add(lblClose, BorderLayout.EAST);
        return pHeader;
    }
}
