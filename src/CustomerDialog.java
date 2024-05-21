import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerDialog extends JDialog {

    //Constructor
    public CustomerDialog(JFrame parentFrame, Connection con) {
        super(parentFrame, "Customer", true);
        this.con = con;

        // Initialize components
        initComponents();

        prepareForm();

        // Set position to center of the screen
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Initialize toolbar and set it non-floatable
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        customerComboModel = new CustomerComboModel(con);

        // Initialize navigation buttons with action listeners
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

        // Add buttons to the toolbar
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
        formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        buttonPanel = new JPanel(new BorderLayout());
        closePanel = new JPanel();

        // Initialize close button with action listener
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        // Initialize labels and text fields
        idLabel = new JLabel("ID:", JLabel.RIGHT);
        lastnameLabel = new JLabel("Lastname:", JLabel.RIGHT);
        firstnameLabel = new JLabel("Firstname:", JLabel.RIGHT);
        afmLabel = new JLabel("AFM:", JLabel.RIGHT);
        telephoneLabel = new JLabel("Telephone:", JLabel.RIGHT);

        idField = new JTextField();
        idField.setEditable(false);
        lastnameField = new JTextField();
        firstnameField = new JTextField();
        afmField = new JTextField();
        telephoneField = new JTextField();

        // Add labels and text fields to the form panel
        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(lastnameLabel);
        formPanel.add(lastnameField);
        formPanel.add(firstnameLabel);
        formPanel.add(firstnameField);
        formPanel.add(afmLabel);
        formPanel.add(afmField);
        formPanel.add(telephoneLabel);
        formPanel.add(telephoneField);

        // Add close button to the close panel
        closePanel.add(closeButton);
        buttonPanel.add(closePanel, BorderLayout.SOUTH);

        // Add panels to the main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add toolbar and main panel to the dialog
        add(toolBar, BorderLayout.NORTH);
        add(mainPanel);

        pack(); // Adjusts size to fit components
    }

    private void prepareForm() {
        try {
            // Prepare the statement and execute query to fetch customer data
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM customer");

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
                stmt.executeUpdate("UPDATE customer SET idcustomer = @num := (@num+1);");
                stmt.executeUpdate("ALTER TABLE customer AUTO_INCREMENT = 1;");
                prepareForm();
            }
        } catch (SQLException e) {
            handleSQLException("performDelete", e);
        }
    }

    private void enableEditing() {
        // Enable editing for text fields and OK, Cancel buttons
        lastnameField.setEditable(true);
        firstnameField.setEditable(true);
        afmField.setEditable(true);
        telephoneField.setEditable(true);
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    private void disableEditing() {
        // Disable editing for text fields and OK, Cancel buttons
        lastnameField.setEditable(false);
        firstnameField.setEditable(false);
        afmField.setEditable(false);
        telephoneField.setEditable(false);
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    private void handleSQLException(String methodName, SQLException e) {
        JOptionPane.showMessageDialog(this, "Error in " + methodName + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        // Log the exception to a file or system log for debugging (not implemented here)
    }

    private void populateForm() {
        try {
            idField.setText(String.valueOf(rs.getInt("idcustomer")));
            lastnameField.setText(rs.getString("lastname"));
            firstnameField.setText(rs.getString("firstname"));
            afmField.setText(String.valueOf(rs.getInt("afm")));
            telephoneField.setText(String.valueOf(rs.getInt("telephone")));
        } catch (SQLException e) {
            handleSQLException("populateForm", e);
        }
    }

    private void clearForm() {
        // Clear all text fields
        idField.setText("");
        lastnameField.setText("");
        firstnameField.setText("");
        afmField.setText("");
        telephoneField.setText("");
    }

    private void saveFormToDatabase() {
        if (lastnameField.getText().isEmpty() || firstnameField.getText().isEmpty() || afmField.getText().isEmpty() || telephoneField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields.");
        } else {
            try {
                if (mode == 0) {
                    rs.moveToInsertRow();
                }

                rs.updateString("lastname", lastnameField.getText());
                rs.updateString("firstname", firstnameField.getText());
                rs.updateString("afm", afmField.getText());
                rs.updateString("telephone", telephoneField.getText());

                if (mode == 0) {
                    rs.insertRow();
                } else {
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
    private JLabel idLabel, lastnameLabel, firstnameLabel, afmLabel, telephoneLabel;
    private JTextField idField, lastnameField, firstnameField, afmField, telephoneField;
    private JButton firstButton, previousButton, nextButton, lastButton, addButton, modifyButton, deleteButton, okButton, cancelButton, closeButton;
    private int currentRecord = 0;
    private int mode = 0;
    private CustomerComboModel customerComboModel;
}
