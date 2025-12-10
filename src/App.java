import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class App extends JFrame {
    private JButton loginButton;
    private JPanel appTime;
    private JLabel labelMain;

    public App(){
        super();
        this.setContentPane(appTime);
        this.setTitle("Quản Lý Bán Hàng");
        this.setSize(1000,800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
    }

    public void login() {
        LoginForm loginForm = new LoginForm();
        loginForm.setVisible(true);

        if (loginForm.isLoginSuccess()) {
            labelMain.setText("Đăng Nhập Thành Công");
        } else {
            labelMain.setText("Đăng Nhập Thất Bại");
        }
    }


    public static void main(String[] args) {
        new App().setVisible(true);
    }

}
