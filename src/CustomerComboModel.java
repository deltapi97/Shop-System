import java.sql.*;
import javax.swing.DefaultComboBoxModel;

public class CustomerComboModel extends DefaultComboBoxModel<String> {

    // Constructor to initialize the CustomerComboModel with a database connection
    public CustomerComboModel(Connection con) {
        try {
            // Create a statement to execute the SQL query
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Execute the query to retrieve customer data
            rs = stmt.executeQuery("SELECT * FROM customer");
        } catch (SQLException e) {
            System.err.println("Error initializing CustomerComboModel: " + e.getMessage());
        }
    }

    // Override the getElementAt method to return the customer data at a specific index
    @Override
    public String getElementAt(int index) {
        try {
            // Move the cursor to the specified row
            rs.absolute(index + 1);
            // Return the formatted customer string
            return formatCustomer(rs);
        } catch (SQLException e) {
            System.err.println("Error getting element at index " + index + ": " + e.getMessage());
            return null;
        }
    }

    // Override the getSize method to return the number of rows in the result set
    @Override
    public int getSize() {
        try {
            // Move the cursor to the last row and return the row number
            rs.last();
            return rs.getRow();
        } catch (SQLException e) {
            System.err.println("Error getting size of ResultSet: " + e.getMessage());
            return 0;
        }
    }

    // Method to get the customer ID at a specific index
    public int getIdCustomerAt(int index) {
        try {
            // Move the cursor to the specified row
            rs.absolute(index + 1);
            // Return the customer ID
            return rs.getInt("idcustomer");
        } catch (SQLException e) {
            System.err.println("Error getting customer ID at index " + index + ": " + e.getMessage());
            return -1; // Return a sentinel value to indicate failure
        }
    }

    // Helper method to format the customer data as a string
    private String formatCustomer(ResultSet rs) throws SQLException {
        return rs.getString("lastname") + ", " + rs.getString("firstname") + ", " + rs.getInt("idcustomer");
    }

    // Method to close the ResultSet and Statement resources
    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet or Statement: " + e.getMessage());
        }
    }

    private ResultSet rs;
    private Statement stmt;
}
