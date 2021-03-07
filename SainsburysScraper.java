import com.jaunt.*;
import java.util.ArrayList;

public class SainsburysScraper {
    private final String NAV_LIST_URL = "https://www.sainsburys.co.uk/shop/AjaxGetImmutableZDASView?requesttype=ajax&storeId=10151&langId=44&catalogId=10241";

    /** This should be formatted with an integer categoryId and beginIndex */
    private final String PRODUCT_LIST_URL = "https://www.sainsburys.co.uk/shop/AjaxApplyFilterSearchResultView?langId=44&storeId=10151&categoryId=%d&pageSize=120&orderBy=NAME_ASC&catSeeAll=true&beginIndex=%d&requesttype=ajax";

    private final DatabaseHandler db = new DatabaseHandler();

    private int[] getMainCategoryIdNums() {
        ArrayList<Integer> categoryIds = new ArrayList<>();
        try {
            UserAgent userAgent = new UserAgent();                          // Create new headless browser
            userAgent.sendGET(NAV_LIST_URL);
            JNode jsonContent = userAgent.json;

            for (JNode node : jsonContent.get("navList")) {
                int parentId = Integer.parseInt(String.valueOf(node.get("parentId")));
                if (parentId == 0) {
                    categoryIds.add(Integer.parseInt(String.valueOf(node.get("id"))));
                }
            }
        } catch(JauntException e) { System.err.println(e); }

        return categoryIds.stream().mapToInt(i -> i).toArray();
    }

    private float parsePrice(String priceUnparsed) {
        if (priceUnparsed.contains("p")) {
            return Float.parseFloat("0." + priceUnparsed.substring(0, priceUnparsed.length()-1));
        } else {
            return Float.parseFloat(priceUnparsed.substring(1, priceUnparsed.length()));
        }
    }

    private void storeData(int catId) {
        int counter = 0;        // Count the number of items stored
        int numberOfItems;
        try {
            UserAgent userAgent = new UserAgent();                          // Create new headless browser
            userAgent.sendGET(String.format(PRODUCT_LIST_URL, catId, 0));
            userAgent.openJSON("{ main : " + userAgent.getSource() + "}");
            JNode jsonContent = userAgent.json;

            // Check if product id was not valid

            // Get number of products
            JNode pageHeading = jsonContent.findFirst("pageHeading");
            String heading = pageHeading.toString();
            String clean = heading.replaceAll(".*[(]", "").replaceAll(" products available[)].*", "").replaceAll(",", "");
            numberOfItems = Integer.parseInt(clean);

            // Store the products
            for (int pageNumber = 0; pageNumber < Math.ceil((float) numberOfItems / 120); pageNumber++) {
                userAgent.sendGET(String.format(PRODUCT_LIST_URL, catId, pageNumber));
                userAgent.openJSON("{ main : " + userAgent.getSource() + "}");
                JNode productsList = userAgent.json.findFirst("products");

                for (int itemNumber = 0; (itemNumber < 120) && (counter < numberOfItems); itemNumber++) {
                    UserAgent productAgent = new UserAgent();
                    productAgent.openContent(String.valueOf(productsList.get(itemNumber).get("result")).replaceAll("\\\\/", "/").replaceAll("\\\\\"", "\""));

                    String name  = productAgent.doc.findFirst("<a>").getTextContent().replaceAll("^([\\\\r]*[\\\\n]*[\\\\t]*\\s*)", "").replaceAll("([\\\\r]*[\\\\n]*[\\\\t]*\\s*)$", "").replaceAll("([\\\\r]*[\\\\n]*[\\\\t]*\\s*)$", "");
                    if (name.equals("")) continue; // removes promotions, which have an empty name

                    String link = productAgent.doc.findFirst("<a>").getAt("href");

                    String priceUnitNotParsed = productAgent.doc.findFirst("<p class=\"pricePerUnit\">").getTextContent().replaceAll("^([\\\\r]*[\\\\n]*\\s*)", "");
                    float priceUnit = parsePrice(priceUnitNotParsed);

                    String priceMeasureNotParsed = productAgent.doc.findFirst("<p class=\"pricePerMeasure\">").getTextContent();
                    String[] priceMeasureNotParsedList = priceMeasureNotParsed.replaceAll("((\\\\r)*(\\\\n)*(\\s)*)$", "").split("/");
                    float priceMeasure = parsePrice(priceMeasureNotParsedList[0]);
                    String measure = priceMeasureNotParsedList[1];

                    // Dont do anything if (measure.equals("kg") || measure.equals("ltr") || measure.equals("ea"))
                    if (measure.equals("100g")) {
                        priceMeasure = (float) (Math.round((priceMeasure * 10) * 100.0) / 100.0);
                        measure = "kg";
                    } else if (measure.equals("100ml")) {
                        priceMeasure = (float) (Math.round((priceMeasure * 10) * 100.0) / 100.0);
                        measure = "ltr";
                    } else if (measure.equals("75cl")) {
                        priceMeasure = (float) (Math.round((priceMeasure*(((float) 4)/3)) * 100.0) / 100.0);
                        measure = "ltr";
                    }

                    db.saveProduct(1, name, link, priceUnit, priceMeasure, measure);

                    counter++;
                }
                System.out.println("counter: " + counter);
            }

        } catch(JauntException e) { System.err.println(e); }
    }

    public void main() {
        int[] categoryIdNums = getMainCategoryIdNums();

        System.out.printf("Found %d main category IDs.%n", categoryIdNums.length);
        System.out.println("Storing all items from these main categories...");

        for (int categoryId : categoryIdNums) {
            storeData(categoryId);
        }

    }
}