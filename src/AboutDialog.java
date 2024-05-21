import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(JFrame parentFrame) {
        super(parentFrame, "About", true);

        initComponents();
        setSize(400, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel aboutPanel = new JPanel(new BorderLayout());
        String msg = "<html><body style='margin: 10px;'>"
                + "<p align='justify'>"
                + "It is an application with which we can search for a product "
                + "from the warehouse and order the items we want "
                + "as well as issue an invoice."
                + "</p>"
                + "</body></html>";
        JLabel aboutText = new JLabel(msg);
        aboutText.setVerticalAlignment(JLabel.TOP);
        aboutPanel.add(aboutText, BorderLayout.CENTER);
        add(aboutPanel);
    }
}
