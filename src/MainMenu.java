import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        super("Invoice Application 2021");
        connectToDB();
        initComponents();
        createDialogs();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Initialize menu bar and menus
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu orderMenu = new JMenu("Order");
        JMenu reportsMenu = new JMenu("Reports");
        JMenu helpMenu = new JMenu("Help");

        // Initialize menu items
        JMenuItem inventoryItem = new JMenuItem("Inventory");
        JMenuItem customersItem = new JMenuItem("Customer");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem placeOrderItem = new JMenuItem("Place Order");
        JMenuItem invoiceItem = new JMenuItem("Invoice");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem customerReportsItem = new JMenuItem("Customer Reports");
        JMenuItem inventoryReportsItem = new JMenuItem("Inventory Reports");

        // Add action listeners to menu items
        inventoryItem.addActionListener(e -> inventoryDialog.setVisible(true));
        customersItem.addActionListener(e -> customerDialog.setVisible(true));
        exitItem.addActionListener(e -> doExit());
        placeOrderItem.addActionListener(e -> placeOrderDialog.setVisible(true));
        invoiceItem.addActionListener(e -> invoiceDialog.setVisible(true));
        aboutItem.addActionListener(e -> aboutDialog.setVisible(true));
        customerReportsItem.addActionListener(e -> printCustomerDialog.setVisible(true));
        inventoryReportsItem.addActionListener(e -> printInventoryDialog.setVisible(true));

        // Add items to menus
        fileMenu.add(inventoryItem);
        fileMenu.add(customersItem);
        fileMenu.add(exitItem);
        orderMenu.add(placeOrderItem);
        reportsMenu.add(customerReportsItem);
        reportsMenu.add(inventoryReportsItem);
        reportsMenu.addSeparator();
        reportsMenu.add(invoiceItem);
        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(orderMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 400);
    }

    private void createDialogs() {
        if (con != null) {
            // Initialize dialogs with database connection
            inventoryDialog = new InventoryDialog(this, con);
            customerDialog = new CustomerDialog(this, con);
            placeOrderDialog = new PlaceOrderDialog(this, con);
            invoiceDialog = new InvoiceDialog(this, con);
            aboutDialog = new AboutDialog(this);
            printCustomerDialog = new PrintCustomerDialog(this, con);
            printInventoryDialog = new PrintInventoryDialog(this, con);
        } else {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void connectToDB() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection to the database
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/inv?useSSL=false", "root", "zoot");
        } catch (ClassNotFoundException | SQLException e) {
            handleDBConnectionError(e);
        }
    }

    private void handleDBConnectionError(Exception e) {
        String errorMessage = "Error connecting to the database:\n" + e.getMessage();
        JOptionPane.showMessageDialog(this, errorMessage, getTitle(), JOptionPane.ERROR_MESSAGE);
        System.exit(1); // Exit the application if the database connection fails
    }


    private void doExit() {
        // Close database resources and exit the application
        closeJDBC(rs, stmt, con);
        System.exit(0);
    }

    private void closeJDBC(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging purposes
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new MainMenu().setVisible(true));
    }

    // Dialogs
    private InventoryDialog inventoryDialog;
    private CustomerDialog customerDialog;
    private PlaceOrderDialog placeOrderDialog;
    private InvoiceDialog invoiceDialog;
    private AboutDialog aboutDialog;
    private PrintCustomerDialog printCustomerDialog;
    private PrintInventoryDialog printInventoryDialog;

    // Database connection variables
    private Connection con;
    private ResultSet rs;
    private Statement stmt;
}
