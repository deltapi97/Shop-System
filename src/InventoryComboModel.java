import java.sql.*;
import javax.swing.DefaultComboBoxModel;

public class InventoryComboModel extends DefaultComboBoxModel<String> {

    // Constructor to initialize the InventoryComboModel with a database connection
    public InventoryComboModel(Connection con) {
        try {
            // Create a statement to execute the SQL query
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            // Execute the query to retrieve inventory data
            rs = stmt.executeQuery("SELECT * FROM inventory");
        } catch (SQLException e) {
            System.err.println("Error initializing InventoryComboModel: " + e.getMessage());
        }
    }

    // Override the getElementAt method to return the inventory data at a specific index
    @Override
    public String getElementAt(int index) {
        try {
            // Move the cursor to the specified row
            rs.absolute(index + 1);
            // Return the formatted inventory string
            return formatInventory(rs);
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

    // Method to get the inventory ID at a specific index
    public int getIdInventoryAt(int index) {
        try {
            // Move the cursor to the specified row
            rs.absolute(index + 1);
            // Return the inventory ID
            return rs.getInt("idinv");
        } catch (SQLException e) {
            System.err.println("Error getting inventory ID at index " + index + ": " + e.getMessage());
            return -1; // Return a sentinel value to indicate failure
        }
    }

    // Helper method to format the inventory data as a string
    private String formatInventory(ResultSet rs) throws SQLException {
        return rs.getString("category") + ", " + rs.getString("description") + ", " + rs.getInt("price") + ", " + rs.getInt("idinv");
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
