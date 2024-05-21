import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class InvoiceDialog extends JDialog {

    // Constructor
    public InvoiceDialog(JFrame parentFrame, Connection con) {
        super(parentFrame, "Print Invoice", true);
        this.con = con;

        // Initialize components
        initComponents();

        // Set size and position
        setSize(400, 150);
        setLocationRelativeTo(null);
    }

    // Initialize UI components
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new GridBagLayout());
        comboPanel = new JPanel(new GridBagLayout());

        // Initialize customer combo box model and combo box
        customerComboModel = new CustomerComboModel(con);
        customerComboBox = new JComboBox<>(customerComboModel);

        // Initialize label and print button
        customerLabel = new JLabel("Invoice For Customer:", JLabel.RIGHT);
        printButton = new JButton("Print");

        // Add action listener to the print button
        printButton.addActionListener(e -> doPrint());

        // Set up layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Add components to comboPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        comboPanel.add(customerLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        comboPanel.add(customerComboBox, gbc);

        // Add print button to buttonPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        buttonPanel.add(printButton, gbc);

        // Add panels to main panel
        mainPanel.add(comboPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to dialog
        add(mainPanel);
        pack(); // Adjusts size to fit components
    }

    // Get the info from database and print the invoice of the selected customer
    private void doPrint() {
        if (customerComboBox.getSelectedItem() != null) {
            int customerId = customerComboBox.getSelectedIndex() + 1;

            try (PreparedStatement pstmt = con.prepareStatement(
                    "SELECT orders.*, customer.lastname, customer.firstname, inventory.category, inventory.description, inventory.price " +
                            "FROM orders " +
                            "LEFT JOIN customer ON orders.custid = customer.idcustomer " +
                            "LEFT JOIN inventory ON orders.invid = inventory.idinv " +
                            "WHERE custid = ?")) {

                pstmt.setInt(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                StringBuilder invoice = new StringBuilder();
                invoice.append(String.format("For Customer ID: %d\n", customerId));

                if (rs.next()) {
                    String customerName = rs.getString("lastname") + " " + rs.getString("firstname");
                    invoice.append(String.format("Customer Name: %s\n", customerName));
                    invoice.append("===================================================================\n");
                    invoice.append(String.format("%-10s %-15s %-15s %-15s %-15s\n", "Order", "Category", "Description", "Quantity", "Price"));
                    invoice.append("===================================================================\n");

                    do {
                        int orderId = rs.getInt("idOrder");
                        String category = rs.getString("category");
                        String description = rs.getString("description");
                        int quantity = rs.getInt("quantity");
                        int price = rs.getInt("price");

                        invoice.append(String.format("%-10d %-15s %-15s %-15d %-15d\n", orderId, category, description, quantity, price));
                    } while (rs.next());

                    invoice.append("===================================================================\n");
                } else {
                    invoice.append("No orders found for the selected customer.\n");
                }

                // Print the invoice to the console
                System.out.println(invoice.toString());

            } catch (SQLException ex) {
                // Display error message dialog and print stack trace for debugging
                JOptionPane.showMessageDialog(this, "Error while printing invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            // Show a message dialog if no customer is selected
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Instance variables
    private JPanel mainPanel, buttonPanel, comboPanel;
    private JComboBox<String> customerComboBox;
    private JLabel customerLabel;
    private JButton printButton;
    private Connection con;
    private CustomerComboModel customerComboModel;
}
