import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class App extends JFrame {
    private JTextArea quanLyBanHangTextArea;
    private JButton loginButton;
    private JPanel appTime;

    public App(){
        super();
        this.setContentPane(appTime);
        this.setTitle("khong");
        this.setSize(400,400);
        this.setVisible(true);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // khi nhấn vào nút login thì sẽ tắt bảng App
                App.this.setVisible(false);
                // bật bảng đăng nhập
                new LoginForm();
            }
        });
    }



    static void main() {
        new App();
    }


}
