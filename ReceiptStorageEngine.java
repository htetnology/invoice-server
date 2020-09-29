import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ReceiptStorageEngine {

    private static final String filesLocation = "." + File.separator + "invoices";

    public ReceiptStorageEngine() {
        //create dir that holds all the files if it doesn't already exist
        Path storageDir = Paths.get(filesLocation);
        if (!Files.exists(storageDir)) {
            try {
                Files.createDirectory(storageDir);
            } catch (IOException e) {
                System.err.println("Error: storage directory for receipts does not exist and cannot be created.");
            }
        }
    }

    public String[] fetch(String filename) {
        if (fileExists(filename)) {
            return readFile(filename);
        } else {
            return null;
        }
    }

    private String[] readFile(String filename) {
        String[] receipt = null;
        try {
            List<String> receiptLines = Files.readAllLines(getFilePath(filename));
            receipt = receiptLines.toArray(new String[0]);
        } catch (IOException e) {
            System.out.println("There was an error reading the file.");
        }
        return receipt;
    }

    public boolean save(String[] receipt, String filename) {
        boolean saveReceipt = false;
        try {
            List<String> receiptLines = Arrays.asList(receipt);
            Path receiptFile = getFilePath(filename);
            Files.write(receiptFile, receiptLines, StandardCharsets.UTF_8);
            saveReceipt = true;
        } catch (IOException e) {
            System.out.println("There was an error saving the receipt.");
        }

        return saveReceipt;
    }

    public String[] listFiles() {
        String[] filesList = null;
        try {
            File invoiceDir = new File(filesLocation);
            filesList = invoiceDir.list();
            Arrays.sort(filesList); //to check if works, enter:  Arrays.sort(filesList, Collections.reverseOrder());
        } catch (ClassCastException e) {
            System.out.println("There was an error sorting the receipts.");
        }
        return filesList;
    }

    private boolean fileExists(String filename) {
        return Files.exists(getFilePath(filename));
    }

    private Path getFilePath(String filename) {
        return Paths.get(filesLocation + File.separator + filename);
    }
}
