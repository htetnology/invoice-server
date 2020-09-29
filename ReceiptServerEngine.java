public class ReceiptServerEngine {
    private ReceiptFilenameCreator receiptFilenameCreator;
    private boolean currentlyCreatingInvoice;
    private ReceiptCreationEngine receiptCreator;
    private ReceiptStorageEngine storageEngine;

    public ReceiptServerEngine() {
        currentlyCreatingInvoice = false;
        storageEngine = new ReceiptStorageEngine();
        receiptFilenameCreator = new ReceiptFilenameCreator();
        receiptCreator = new ReceiptCreationEngine();
    }

    public String getAvailableCommands() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%n", "CREATE RECEIPT       create a new receipt"));
        sb.append(String.format("%s%n", "LOAD <receipt>       display the receipt specified by <receipt>"));
        sb.append(String.format("%s%n", "SHOW RECEIPTS        show all receipts saved on the server"));
        sb.append(String.format("%s%n", "SHOW HELP            show this help"));
        return sb.toString();
    }

    public String parseCommand(String command) {
        if (currentlyCreatingInvoice) {
            return parseReceiptLine(command);
        } else {
            return parseServerCommand(command);
        }
    }

    private String parseServerCommand(String command) {
        //each command has an instruction and an argument. split the incoming string on the first whitespace character (or whitespace characterS if they are contiguous)
        String[] words = command.split("\\s+", 2);
        if (words.length < 2) {
            return "Syntax: <command> <argument>.";
        }

        //make both strings lower case and trim any excess whitespace to make comparisons easier
        String instruction = words[0].toLowerCase().trim();
        String argument = words[1].toLowerCase().trim();

        switch (instruction) {
            case "show":
                return show(argument);
            case "create":
                return create(argument);
            case "load":
                return load(argument);
            default: //everything that isn't a known command
                return "I don't understand '" + instruction + "'.";
        }
    }

    private String show(String command) {
        switch (command.toLowerCase()) {
            case "help":	    	return getAvailableCommands();
            case "receipts":		return getStoredReceipts();

            default: 			return "I don't know how to show that!";
        }
    }

    private String getStoredReceipts() {
        String[] files =  storageEngine.listFiles();
        if (files.length > 0) {
            return formatFileList(files);
        } else {
            return "No files found.";
        }
    }

    private String formatFileList(String[] files) {
        StringBuffer sb = new StringBuffer();
        for(String file : files) {
            sb.append(file);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String create(String argument) {
        if (argument.trim().equalsIgnoreCase("receipt")) return createReceipt();
        else return "I don't know how to create that!";
    }

    private String createReceipt() {
        currentlyCreatingInvoice = true;
        return receiptCreator.start();
    }

    private String parseReceiptLine(String line) {
        String output = receiptCreator.parseUserInput(line);
        if (output.equals(receiptCreator.RECEIPT_CREATED)) {
            currentlyCreatingInvoice = false;
            output = getReceipt() + output;
            saveReceipt();
        }
        return output;
    }

    private String getReceipt() {
        String output = String.format("--------------begin receipt------------%n");
        output += String.join(System.lineSeparator(), receiptCreator.getReceipt());
        output += String.format("%n--------------end receipt------------%n");
        return output;
    }

    private void saveReceipt() {
        String[] receipt = receiptCreator.getReceipt();
        storageEngine.save(receipt, receiptFilenameCreator.createFilename(receipt));
    }

    private String load(String receiptFilename) {
        String[] receipt = storageEngine.fetch(receiptFilename);
        if (receipt == null) {
            return "File not found.";
        } else {
            return formatFileList(receipt);
        }
    }
}
