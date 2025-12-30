import Main.LoginManager.LoginForm;
import Utils.DBConnection;
import Utils.Style;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.util.Locale;

/**
 * Main entry point for the application.
 */
public class Run {

    /**
     * Main method to start the application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // --- STEP 1: CHECK DATABASE BEFORE STARTING SWING ---
        File dbFile = new File("data/storedatabase.db");
        if (!dbFile.exists()) {
            Style.showError(null, "Không tìm thấy file cơ sở dữ liệu tại:\n" + dbFile.getAbsolutePath() + "\nVui lòng cung cấp file database và khởi động lại.");
            System.exit(1);
        }

        // --- STEP 2: START SWING UI AFTER DATABASE IS READY ---
        Locale.setDefault(Locale.US);
        SwingUtilities.invokeLater(Run::createAndShowGUI);
    }

    /**
     * Creates and shows the GUI.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setupUIDefaults();

            // Check connection one last time to ensure file is not corrupted
            if (checkDatabaseConnection()) {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                loginForm.setLocationRelativeTo(null);
            } else {
                // This error occurs if DB file is corrupted or unreadable
                System.exit(1);
            }

        } catch (Exception e) {
            System.exit(1);
        }
    }

    /**
     * Sets up UI defaults.
     */
    private static void setupUIDefaults() {
        Color black = Color.BLACK;
        Color white = Color.WHITE;
        UIManager.put("TextField.inactiveForeground", black);
        UIManager.put("TextField.disabledTextColor", black);
        UIManager.put("FormattedTextField.inactiveForeground", black);
        UIManager.put("PasswordField.inactiveForeground", black);
        UIManager.put("TextArea.inactiveForeground", black);
        UIManager.put("ComboBox.disabledForeground", black);
        UIManager.put("TextField.disabledBackground", white);
        UIManager.put("FormattedTextField.disabledBackground", white);
        UIManager.put("PasswordField.disabledBackground", white);
        UIManager.put("TextArea.disabledBackground", white);
        UIManager.put("ComboBox.disabledBackground", white);
        UIManager.put("ScrollPane.disabledBackground", white);
        UIManager.put("EditorPane.disabledBackground", white);
        UIManager.put("TextField.inactiveBackground", white);
        UIManager.put("TextArea.inactiveBackground", white);
        UIManager.put("Button.disabledText", Color.GRAY);
    }

    /**
     * Checks the database connection.
     *
     * @return true if connection is successful, false otherwise.
     */
    private static boolean checkDatabaseConnection() {
        try (Connection con = DBConnection.getConnection()) {
            return con != null;
        } catch (Exception e) {
            Style.showError(null, "Lỗi kết nối CSDL: " + e.getMessage());
        }
        return false;
    }
}
