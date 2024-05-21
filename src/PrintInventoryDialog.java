import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PrintInventoryDialog extends JDialog {

    // Constructor
    public PrintInventoryDialog(JFrame parentFrame, Connection con) {
        super(parentFrame, "Print Inventory", true);
        this.con = con;

        // Initialize components
        initComponents();

        // Set dialog size and position
        setSize(200, 150);
        setLocationRelativeTo(null);
    }

    // Initialize UI components
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Create and configure the print button
        JButton printButton = new JButton("Print Inventory");
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPrint(); // Print inventory data when button is clicked
            }
        });

        // Center the button within the panel using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(printButton, gbc);

        // Add some padding to the panel for better appearance
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        // Add panel to the dialog
        add(panel);
        pack();
    }

    // Print inventory data
    private void doPrint() {
        // Check if database connection is available
        if (con == null || isConnectionClosed(con)) {
            System.err.println("Database connection is not available.");
            return;
        }

        // Retrieve and print inventory data
        try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            rs = stmt.executeQuery("SELECT * FROM inventory");

            StringBuilder sb = new StringBuilder();
            sb.append("INVENTORY REPORT\n\n");
            sb.append(String.format("%-10s %-15s %-15s %-15s %-15s\n", "Code", "Category", "Description", "Price", "Quantity"));
            sb.append("========================================================================\n");

            if (rs != null && rs.next()) {
                // Iterate through the result set and build the inventory report
                do {
                    int id = rs.getInt("idinv");
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    int price = rs.getInt("price");
                    int quantity = rs.getInt("quantity");

                    sb.append(String.format("%-10d %-15s %-15s %-15d %-15d\n", id, category, description, price, quantity));
                } while (rs.next());
            } else {
                sb.append("No inventory data found.\n");
            }

            sb.append("========================================================================\n");
            System.out.println(sb.toString());

        } catch (SQLException ex) {
            System.err.println("Error while printing inventory: " + ex.getMessage());
        } finally {
            closeResultSet(rs);
        }
    }

    // Utility method to check if the connection is closed
    private boolean isConnectionClosed(Connection con) {
        try {
            return con == null || con.isClosed();
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return true;
        }
    }

    // Utility method to close the ResultSet
    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                System.err.println("Error closing ResultSet: " + ex.getMessage());
            }
        }
    }

    // Database connection and result set variables
    private Connection con;
    private ResultSet rs;
}
