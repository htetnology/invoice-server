public class ReceiptFilenameCreator {
    String createFilename(String[] receipt) {
        String id = "";
        for (String line : receipt) {
            if (line.startsWith("Date - ")) {
                String[] split = line.split(" - ", 2);
                id = split[1];
            }
        }

        id = id.replaceAll("/", "");
        id = id.replaceAll(":", "");
        id = id.replaceAll(" ", "");

        return id + ".txt";
    }
}