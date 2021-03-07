import java.sql.*;

public class DatabaseHandler {
    private Connection conn;

    public DatabaseHandler() {
        try{
            conn = DriverManager.getConnection("jdbc:sqlite:products.db");
            String createStatement = "CREATE TABLE IF NOT EXISTS \"Products\" (\n" +
                    "\t\"supermarket\"\tINTEGER,\n" +
                    "\t\"name\"\tTEXT,\n" +
                    "\t\"link\"\tTEXT UNIQUE,\n" +
                    "\t\"priceUnit\"\tREAL,\n" +
                    "\t\"priceMeasure\"\tREAL,\n" +
                    "\t\"measure\"\tTEXT\n" +
                    ")";
            conn.createStatement().execute(createStatement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void saveProduct(int supermarket, String name, String link, float priceUnit, float priceMeasure, String measure) {
        String sql = "INSERT INTO \"Products\"(\"supermarket\",\"name\",\"link\",\"priceUnit\",\"priceMeasure\",\"measure\") VALUES(?,?,?,?,?,?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, supermarket);
            pstmt.setString(2, name);
            pstmt.setString(3, link);
            pstmt.setFloat(4, priceUnit);
            pstmt.setFloat(5, priceMeasure);
            pstmt.setString(6, measure);

            pstmt.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ResultSet getQuery(String query, int numberOfResults) {
        try {
            Statement stmt  = conn.createStatement();
            ResultSet rs;
            String queryParsed = "\"%" + query.replaceAll("\\s", "%%") + "%\"";
            if (numberOfResults > 0) {
                rs = stmt.executeQuery(String.format("SELECT * FROM products WHERE name LIKE %s LIMIT %d;", queryParsed, numberOfResults));
            } else {
                String temp = String.format("SELECT * FROM products WHERE name LIKE %s;", queryParsed);
                rs = stmt.executeQuery(temp);
            }

            return rs;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }
}
