import javax.swing.table.*;
import java.sql.*;

public class OrderTableComboModel extends DefaultTableModel {

    // Constructor to initialize the OrderTableComboModel with a database connection
    public OrderTableComboModel(Connection con) {
        this.con = con;
        try {
            // Create a statement to execute the SQL query
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Execute the query to retrieve order data
            rs = stmt.executeQuery("SELECT * FROM orders "
                    + "LEFT JOIN customer ON custid = idcustomer "
                    + "LEFT JOIN inventory ON invid = idinv");
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }
    }

    // Override the getRowCount method to return the number of rows in the result set
    @Override
    public int getRowCount() {
        try {
            if (rs != null) {
                rs.last();
                return rs.getRow();
            }
        } catch (SQLException e) {
            System.err.println("Error getting row count: " + e.getMessage());
        }
        return 0;
    }

    // Override the getColumnCount method to return the number of columns
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    // Override the getValueAt method to return the value at a specific row and column
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            rs.absolute(rowIndex + 1);
            switch (columnIndex) {
                case 0:
                    return rs.getInt("idOrder");
                case 1:
                    return rs.getString("lastname");
                case 2:
                    return rs.getString("category");
                case 3:
                    return rs.getString("description");
                case 4:
                    return rs.getInt("price");
                default:
                    throw new IllegalArgumentException("Invalid column index");
            }
        } catch (SQLException e) {
            System.err.println("Error getting value at row " + rowIndex + ", column " + columnIndex + ": " + e.getMessage());
        }
        return null;
    }

    // Override the getColumnName method to return the column name
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    // Method to close the ResultSet and Statement resources
    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }
    }

    private static final String[] COLUMN_NAMES = {"Order ID", "Customer", "Category", "Description", "Price"};
    private Connection con;
    private ResultSet rs;
}
