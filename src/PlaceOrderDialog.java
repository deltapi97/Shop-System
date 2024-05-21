import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PlaceOrderDialog extends JDialog {

    // Constructor
    public PlaceOrderDialog(JFrame parentFrame, Connection con) {
        super(parentFrame, "Place Order", true);
        this.con = con;

        // Initialize UI components
        initComponents();
        pack();

        // Center the dialog on the screen
        setLocationRelativeTo(null);

        try {
            // Create a statement to execute the SQL query
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Execute the query to retrieve customer data
            rs = stmt.executeQuery("select * from customer,inventory");
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
    }

    // Initialize UI components
    private void initComponents() {
        topPanel = new JPanel(new GridLayout(6, 2));
        bottomPanel = new JPanel();

        // Initialize labels
        customerLabel = new JLabel("Customer: ", JLabel.RIGHT);
        inventoryLabel = new JLabel("Inventory Item: ", JLabel.RIGHT);
        priceLabel = new JLabel("Item Price: ", JLabel.RIGHT);
        quantityLabel = new JLabel("Quantity: ", JLabel.RIGHT);
        totalPriceLabel = new JLabel("Total Price: ", JLabel.RIGHT);

        // Initialize combo boxes
        customerComboBox = new JComboBox<>();
        customerComboBox.setModel(new CustomerComboModel(con));

        inventoryComboBox = new JComboBox<>();
        inventoryComboBox.setModel(new InventoryComboModel(con));
        inventoryComboBox.addActionListener(e -> updatePriceAndQuantity());

        // Initialize text fields
        priceTextField = new JTextField();
        quantityTextField = new JTextField("1");
        totalPriceTextField = new JTextField();

        // Initialize buttons and add action listeners
        addButton = new JButton("Add Order");
        deleteButton = new JButton("Delete Order");
        exitButton = new JButton("Exit");

        addButton.addActionListener(e -> doAdd());
        deleteButton.addActionListener(e -> doDel());
        exitButton.addActionListener(e -> System.exit(0));

        // Initialize order table and scroll pane
        orderTable = new JTable(new OrderTableComboModel(con));
        tableScrollPane = new JScrollPane(orderTable);

        // Add components to the top panel
        topPanel.add(customerLabel);
        topPanel.add(customerComboBox);
        topPanel.add(inventoryLabel);
        topPanel.add(inventoryComboBox);
        topPanel.add(priceLabel);
        topPanel.add(priceTextField);
        topPanel.add(quantityLabel);
        topPanel.add(quantityTextField);
        topPanel.add(totalPriceLabel);
        topPanel.add(totalPriceTextField);

        // Add buttons to the bottom panel
        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(exitButton);

        // Set layout for the dialog and add panels
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Update price and quantity when an inventory item is selected
    private void updatePriceAndQuantity() {
        try {
            int idInv = inventoryComboBox.getSelectedIndex() + 1;
            String query = "SELECT price, quantity FROM inventory WHERE idinv = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, idInv);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                priceTextField.setText(String.valueOf(price));
                quantityTextField.setText(String.valueOf(quantity));
                totalPriceTextField.setText(String.valueOf(price));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add order to the database and update the table
    private void doAdd() {
        if (customerComboBox.getSelectedItem() != null && inventoryComboBox.getSelectedItem() != null) {
            int customerID = customerComboBox.getSelectedIndex() + 1;
            int inventoryID = inventoryComboBox.getSelectedIndex() + 1;
            double priceDouble = Double.parseDouble(priceTextField.getText());
            int price = (int) priceDouble;

            try {
                rs = stmt.executeQuery("select * from orders");
                rs.moveToInsertRow();
                rs.updateInt("custid", customerID);
                rs.updateInt("invid", inventoryID);
                rs.updateInt("orders.quantity", 1);
                rs.updateDouble("price", price);
                rs.insertRow();

                // Clear the input fields
                customerComboBox.setSelectedIndex(-1);
                inventoryComboBox.setSelectedIndex(-1);
                priceTextField.setText("");
                quantityTextField.setText("1");
                totalPriceTextField.setText("");

                // Update the table with the new order
                tableCreation();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select customer and inventory.");
        }
    }

    // Create and update the order table
    private void tableCreation() {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new Object[]{"Order ID", "Customer", "Category", "Description", "Price"});
        try {
            rs = stmt.executeQuery("SELECT orders.idOrder, customer.lastname, inventory.category, inventory.description, inventory.price " +
                    "FROM orders " +
                    "LEFT JOIN customer ON orders.custid = customer.idcustomer " +
                    "LEFT JOIN inventory ON orders.invid = inventory.idinv");
            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("idOrder"),
                        rs.getString("lastname"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getInt("price")
                };
                tableModel.addRow(row);
            }
            orderTable.setModel(tableModel);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Delete the selected order from the table and database
    private void doDel() {
        int selectedRow = orderTable.getSelectedRow();
        if (orderTable.getSelectedRowCount() == 1) {
            int orderId = (int) orderTable.getValueAt(selectedRow, 0);
            try {
                String deleteSQL = "DELETE FROM orders WHERE idOrder = ?";
                PreparedStatement pstmt = con.prepareStatement(deleteSQL);
                pstmt.setInt(1, orderId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    ((DefaultTableModel) orderTable.getModel()).removeRow(selectedRow);
                } else {
                    System.out.println("Failed to delete the order from the database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (orderTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Table is empty.");
        } else if (orderTable.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please select an order.");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a single row to delete.");
        }
    }

    // Declare UI components and database variables
    private JPanel topPanel, bottomPanel;
    private JComboBox<String> customerComboBox, inventoryComboBox;
    private JTable orderTable;
    private JScrollPane tableScrollPane;
    private JButton addButton, deleteButton, exitButton;
    private JLabel customerLabel, inventoryLabel, priceLabel, quantityLabel, totalPriceLabel;
    private JTextField priceTextField, quantityTextField, totalPriceTextField;
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
}
