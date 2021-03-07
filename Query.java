import java.sql.ResultSet;
import java.sql.SQLException;

public class Query {
    private static DatabaseHandler db = new DatabaseHandler();

    private static String nameRow =     "       Supermarket   Product name                                                   Price                          Link";
    private static String emptyRow =    "| -- | ----------- | ------------------------------------------------------------ | ------- | ------------------ | ----";
    private static String formattableRow = "| %s | %s | %s | %s | %s |  %s";

    public static void main(String[] args) {
        try {
            ResultSet queryResult = db.getQuery("king prawn", 15); // lentil, pancake, coconut, king prawn

            System.out.println(nameRow);
            System.out.println(emptyRow);

            int resultNum = 0;
            while (queryResult.next()) {
                resultNum++;

                String supermarket;
                switch (queryResult.getInt("supermarket")) {
                    case 0:
                        supermarket = "Aldi";
                        break;
                    case 1:
                        supermarket = "Sainsburys";
                        break;
                    default:
                        supermarket = "Unexpected";
                }

                String name = queryResult.getString("name");

                String link = queryResult.getString("link");

                String price = "£"+String.format("%.2f", queryResult.getFloat("priceUnit"));

                String priceMeasure = "£"+String.format("%.2f", queryResult.getFloat("priceMeasure"))+" per "+queryResult.getString("measure");

                //Now add to table
                System.out.printf(
                        formattableRow,
                        String.format("%02d", resultNum),
                        String.format("%11s", supermarket),
                        String.format("%1$60s", name),
                        String.format("%7s", price),
                        String.format("%18s", priceMeasure),
                        link
                );
                System.out.println();
            }
            System.out.println(emptyRow);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
