import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends  JDialog {
    private JPanel dangNhaptime;
    private JButton loginButton;
    private JPasswordField passwordField1;
    private JTextField textField1;

    private boolean isSuccess = false;

    public LoginForm(){
        super();
        this.setContentPane(dangNhaptime);
        this.setSize(400,400);
        this.setTitle("Đăng Nhập");
        this.setModal(true);
        addEvents();
    }


    public boolean isLoginSuccess() {
        return isSuccess;
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quanLyUser", "root", "123456");
        return con;
    }


    private void addEvents() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField1.getText();
                String password = new String(passwordField1.getPassword());
                try {
                    if (checkLogin(name, password)) {
                        JOptionPane.showMessageDialog(LoginForm.this, "Đăng nhập thành công!"); // Sửa null thành this để hiện giữa form

                        // Đăng nập thành công
                        isSuccess = true;
                        dispose();

                    } else {
                        JOptionPane.showMessageDialog(LoginForm.this, "Sai tên hoặc mật khẩu", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginForm.this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public boolean checkLogin(String name, String password) throws Exception{
        Connection con = getConnection();

        PreparedStatement ps = con.prepareStatement("SELECT * FROM user WHERE name = ? AND password = ?");
        ps.setString(1, name);
        ps.setString(2,password);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
