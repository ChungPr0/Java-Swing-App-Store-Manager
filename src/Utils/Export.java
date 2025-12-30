package Utils;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static Utils.Style.showError;
import static Utils.Style.showSuccess;

/**
 * Utility class for exporting data.
 */
public class Export {

    /**
     * Exports JTable data to an Excel file (HTML format).
     *
     * @param table           The JTable containing data to export.
     * @param defaultFileName The default file name suggested in the save dialog.
     */
    public static void exportToExcel(JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".xls")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xls");
            }

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {

                bw.write("<html>");
                bw.write("<head><meta charset='UTF-8'></head>");
                bw.write("<body>");
                bw.write("<table border='1'>");

                bw.write("<thead><tr style='background-color: #FFFF00; font-weight: bold;'>");
                for (int i = 0; i < table.getColumnCount(); i++) {
                    bw.write("<th>");
                    bw.write(table.getColumnName(i));
                    bw.write("</th>");
                }
                bw.write("</tr></thead>");

                bw.write("<tbody>");
                for (int i = 0; i < table.getRowCount(); i++) {
                    bw.write("<tr>");
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object value = table.getValueAt(i, j);
                        String valStr = (value != null) ? value.toString() : "";

                        // Handle HTML special characters to avoid errors
                        valStr = valStr.replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;");

                        bw.write("<td>");
                        bw.write(valStr);
                        bw.write("</td>");
                    }
                    bw.write("</tr>");
                }
                bw.write("</tbody>");

                bw.write("</table></body></html>");

                showSuccess(null, "Xuất file thành công!");

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }

            } catch (Exception ex) {
                showError(null, "Lỗi xuất file: " + ex.getMessage());
            }
        }
    }
}
