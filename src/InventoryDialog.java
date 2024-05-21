import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InventoryDialog extends JDialog {

    // Constructor
    public InventoryDialog(JFrame parentFrame, Connection con) {
        super(parentFrame, "Inventory", true);
        this.con = con;

        // Initialize components
        initComponents();

        prepareForm();

        // Set position to center of the screen
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Initialize toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Initialize buttons
        firstButton = new JButton("First");
        firstButton.addActionListener(e -> navigateFirst());

        previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> navigatePrevious());

        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> navigateNext());

        lastButton = new JButton("Last");
        lastButton.addActionListener(e -> navigateLast());

        okButton = new JButton("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(e -> performOk());

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> performCancel());

        modifyButton = new JButton("Modify");
        modifyButton.addActionListener(e -> enableModify());

        addButton = new JButton("Add");
        addButton.addActionListener(e -> enableAdd());

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> performDelete());

        // Add buttons to toolbar
        toolBar.add(firstButton);
        toolBar.add(previousButton);
        toolBar.add(nextButton);
        toolBar.add(lastButton);
        toolBar.add(addButton);
        toolBar.add(modifyButton);
        toolBar.add(deleteButton);
        toolBar.add(okButton);
        toolBar.add(cancelButton);

        // Initialize panels
        mainPanel = new JPanel(new BorderLayout());
        formPanel = new JPanel(new GridLayout(5, 2, 5, 5)); // Adjusted layout for better spacing
        buttonPanel = new JPanel(new BorderLayout());
        closePanel = new JPanel();

        // Initialize close button
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        // Initialize form labels and fields
        idLabel = new JLabel("ID:", JLabel.RIGHT);
        categoryLabel = new JLabel("Category:", JLabel.RIGHT);
        descriptionLabel = new JLabel("Description:", JLabel.RIGHT);
        priceLabel = new JLabel("Price:", JLabel.RIGHT);
        quantityLabel = new JLabel("Quantity:", JLabel.RIGHT);

        idField = new JTextField();
        idField.setEditable(false);
        categoryField = new JTextField();
        descriptionField = new JTextField();
        priceField = new JTextField();
        quantityField = new JTextField();

        // Add labels and fields to form panel
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(categoryLabel);
        formPanel.add(categoryField);
        formPanel.add(descriptionLabel);
        formPanel.add(descriptionField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(quantityLabel);
        formPanel.add(quantityField);

        // Add components to close panel and button panel
        closePanel.add(closeButton);
        buttonPanel.add(closePanel, BorderLayout.SOUTH);

        // Add form panel and button panel to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add toolbar and main panel to dialog
        add(toolBar, BorderLayout.NORTH);
        add(mainPanel);

        pack(); // Adjusts size to fit components
    }

    private void prepareForm() {
        try {
            // Prepare the statement and execute query to fetch inventory data
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM inventory");

            if (rs.first()) {
                populateForm();
                currentRecord = 1;
            }
            disableEditing();
        } catch (SQLException e) {
            handleSQLException("prepareForm", e);
        }
    }

    private void navigateFirst() {
        try {
            if (rs.first()) {
                populateForm();
            }
        } catch (SQLException e) {
            handleSQLException("navigateFirst", e);
        }
    }

    private void navigateLast() {
        try {
            if (rs.last()) {
                populateForm();
            }
        } catch (SQLException e) {
            handleSQLException("navigateLast", e);
        }
    }

    private void navigatePrevious() {
        try {
            if (!rs.isFirst() && rs.previous()) {
                populateForm();
            }
        } catch (SQLException e) {
            handleSQLException("navigatePrevious", e);
        }
    }

    private void navigateNext() {
        try {
            if (!rs.isLast() && rs.next()) {
                populateForm();
            }
        } catch (SQLException e) {
            handleSQLException("navigateNext", e);
        }
    }

    private void performOk() {
        saveFormToDatabase();
        if (mode == 0) {
            navigateLast();
        }
        currentRecord++;
        disableEditing();
    }

    private void performCancel() {
        if (currentRecord > 0) {
            try {
                navigateFirst();
                while (currentRecord != rs.getInt(1)) {
                    navigateNext();
                }
            } catch (SQLException e) {
                handleSQLException("performCancel", e);
            }
        }
        disableEditing();
    }

    private void enableModify() {
        mode = 1; // Modify mode
        enableEditing();
    }

    private void enableAdd() {
        mode = 0; // Add mode
        enableEditing();
        clearForm();
    }

    private void performDelete() {
        try {
            if (rs.getRow() != 0) {
                currentRecord = rs.getInt(1);
                rs.deleteRow();
                stmt.executeUpdate("SET @num := 0;");
                stmt.executeUpdate("UPDATE inventory SET idinv = @num := (@num+1);");
                stmt.executeUpdate("ALTER TABLE inventory AUTO_INCREMENT = 1;");
                prepareForm();
            }
        } catch (SQLException e) {
            handleSQLException("performDelete", e);
        }
    }

    private void enableEditing() {
        // Enable editing for text fields and OK, Cancel buttons
        categoryField.setEditable(true);
        descriptionField.setEditable(true);
        priceField.setEditable(true);
        quantityField.setEditable(true);
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    private void disableEditing() {
        // Disable editing for text fields and OK, Cancel buttons
        idField.setEditable(false);
        categoryField.setEditable(false);
        descriptionField.setEditable(false);
        priceField.setEditable(false);
        quantityField.setEditable(false);
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    private void handleSQLException(String methodName, SQLException e) {
        JOptionPane.showMessageDialog(this, "Error in " + methodName + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        // Log the exception to a file or system log for debugging (not implemented here)
    }

    private void populateForm() {
        try {
            idField.setText(String.valueOf(rs.getInt("idinv")));
            categoryField.setText(rs.getString("category"));
            descriptionField.setText(rs.getString("description"));
            priceField.setText(String.valueOf(rs.getInt("price")));
            quantityField.setText(String.valueOf(rs.getInt("quantity")));
        } catch (SQLException e) {
            handleSQLException("populateForm", e);
        }
    }

    private void clearForm() {
        // Clear all text fields
        idField.setText("");
        categoryField.setText("");
        descriptionField.setText("");
        priceField.setText("");
        quantityField.setText("");
    }

    private void saveFormToDatabase() {
        if (categoryField.getText().isEmpty() || descriptionField.getText().isEmpty() || priceField.getText().isEmpty() || quantityField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields.");
        } else {
            try {
                if (mode == 0) { // Register new entry
                    rs.moveToInsertRow();
                }

                rs.updateString("category", categoryField.getText());
                rs.updateString("description", descriptionField.getText());
                rs.updateString("price", priceField.getText());
                rs.updateString("quantity", quantityField.getText());

                if (mode == 0) {
                    rs.insertRow();
                } else { // Modify existing entry
                    rs.updateRow();
                }
            } catch (SQLException e) {
                handleSQLException("saveFormToDatabase", e);
            }
        }
    }

    // Instance variables
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private JToolBar toolBar;
    private JPanel mainPanel, formPanel, buttonPanel, closePanel;
    private JLabel idLabel, categoryLabel, descriptionLabel, priceLabel, quantityLabel;
    private JTextField idField, categoryField, descriptionField, priceField, quantityField;
    private JButton firstButton, previousButton, nextButton, lastButton, addButton, modifyButton, deleteButton, okButton, cancelButton, closeButton;
    private int currentRecord = 0;
    private int mode = 0; // 0 for add mode, 1 for modify mode
}
