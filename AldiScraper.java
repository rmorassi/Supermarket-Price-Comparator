import com.jaunt.JNode;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;


public class AldiScraper {
    /** This should be formatted with an integer categoryId */
    private final String PRODUCT_LIST_URL = "https://www.aldi.co.uk/api/productsearch/rr/category/groceriescategories?page=%d&firstPlacementTotalCount=0&secondPlacementTotalCount=3771";

    private final DatabaseHandler db = new DatabaseHandler();

    private float parsePrice(String priceUnparsed) {
        try {
            if (priceUnparsed.startsWith("£")) {
                return Float.parseFloat(priceUnparsed.substring(1, priceUnparsed.length()));
            } else if (priceUnparsed.endsWith("p")) {
                return 100 * Float.parseFloat(priceUnparsed.substring(0, priceUnparsed.length() - 1));
            } else {
                System.err.println("some price unexpected");
                return 0.0f;
            }
        } catch (Exception e) {
            return 0.0f;
        }
    }

    private void storeData(int pageNumber) {
        try {
            UserAgent userAgent = new UserAgent();                          // Create new headless browser
            userAgent.sendGET(String.format(PRODUCT_LIST_URL, pageNumber));
            JNode productsList = userAgent.json.findFirst("results");

            for (JNode product : productsList) {
                String name = product.findFirst("name").toString();
                String link = product.findFirst("productUrl").toString().replaceAll("\\\\/", "/");
                float priceUnit = parsePrice(product.findFirst("price").toString());

                float priceMeasure;
                String measure;
                String[] temp = product.findFirst("pricePerUnit").toString().split(" per ");
                if (temp.length != 2) {
                    priceMeasure = 0.0f;
                    measure = "";
                } else {
                    priceMeasure = parsePrice(temp[0]);
                    measure = temp[1];
                }

                db.saveProduct(0, name, link, priceUnit, priceMeasure, measure);
            }

        } catch (JauntException e) { System.err.println(e); }
    }

    public void main() {
        System.out.println("Storing all items...");

        for (int i = 0; i <= 209; i++) {
            storeData(i);
            System.out.println(i+1);
        }

    }
}