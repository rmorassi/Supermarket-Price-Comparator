public class Controller {
    public static void main(String[] args) {
        SainsburysScraper sainsburys = new SainsburysScraper();
        sainsburys.main();

        AldiScraper aldi = new AldiScraper();
        aldi.main();
    }
}
