import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReceiptCreationEngine {
    private static final String[] USER_PROMPTS = {
            "%s%nEnter the number that corresponds to the shop location: ",
            "%s%nEnter the number that corresponds to the currency to use in this receipt: ",
            "Enter the name of the employee who served the customer: ",
            "Enter Item or service the customer is being charged for: ",
            "Enter the unit price (without VAT) for %s: ",
            "Enter the quantity of %s sold: ",
            "Enter another item or service? Yes/No: ",
            "Enter payment method: "
    };
    public static final String RECEIPT_CREATED = "Receipt created."; //the response the user is waiting for to know that a call to getReceipt() will succeed
    private static final int ADD_ITEM_STEP = 3;
    private boolean inProgress;
    private boolean addMoreItems;
    private int step;
    private String inputErrorMessage;
    private ReceiptFormatter formatter;
    private String[] currencyChoices;
    private String[] shopLocationChoices;
    private int shopLocationChoice;
    private int currencyChoice;
    private String staffName;
    private String paymentMethod;
    private List<ReceiptItem> receiptItems;
    private String currentItemDescription;
    private int currentItemQuantity;
    private BigDecimal currentItemPrice;
    private String[] formattedReceipt;

    public String[] getReceipt() {
        if (inProgress) throw new RuntimeException("Cannot get receipt while it is still being created");
        return formattedReceipt;
    }

    public String start() {
        if (inProgress) throw new RuntimeException("Cannot start a new receipt while creating one");

        init();
        return getNextLineForUser("");
    }

    private void init() {
        inProgress = true;
        staffName = "";
        paymentMethod = "";
        inputErrorMessage = "";
        step = 0;
        receiptItems = new ArrayList<>();
        formatter = new ReceiptFormatter();
        currencyChoices = formatter.getCurrencies();
        shopLocationChoices = formatter.getShopLocations();
    }

    private String getNextLineForUser(String userInput) {
        switch (step) {
            case 0:
                return String.format(USER_PROMPTS[step], createNumberedOptions(shopLocationChoices));
            case 1:
                return String.format(USER_PROMPTS[step], createNumberedOptions(currencyChoices));
            case 2: case 3: case 6: case 7:
                return USER_PROMPTS[step];
            case 4: case 5:
                return String.format(USER_PROMPTS[step], currentItemDescription);
            case 8:
                return RECEIPT_CREATED;
            default:
                return "I don't know how we got here!";
        }
    }

    public String parseUserInput(String userInput) {
        if (processUserInput(userInput)) {
            nextStep();
            return getNextLineForUser(userInput);
        } else {
            return getErrorMessageAndRepeatedUserPrompt(userInput);
        }
    }

    private boolean processUserInput(String userInput) {
        switch (step) {
            case 0:
                return setShopLocationIfValid(userInput);
            case 1:
                return setCurrencyIfValid(userInput);
            case 2:
                staffName = userInput;
                return true;
            case 3:
                currentItemDescription = userInput;
                return true;
            case 4:
                return setItemPriceIfValid(userInput);
            case 5:
                if (setItemQuantityIfValid(userInput)) {
                    receiptItems.add(new ReceiptItem(currentItemDescription, currentItemPrice, currentItemQuantity));
                    return true;
                } else {
                    return false;
                }
            case 6:
                addMoreItems = parseYesNo(userInput);
                return true;
            case 7:
                paymentMethod = userInput;
                formattedReceipt = formatter.createReceipt(shopLocationChoice, staffName, paymentMethod, receiptItems, currencyChoice);
                inProgress = false;
                return true;
            default:
                return false; //shouldn't get here
        }
    }

    private void nextStep() {
        if (addMoreItems) {
            step = ADD_ITEM_STEP;
            addMoreItems = false;
        } else {
            step++;
        }
    }

    private String getErrorMessageAndRepeatedUserPrompt(String userInput) {
        return String.format("*** Error: %s%n%s", inputErrorMessage, getNextLineForUser(userInput));
    }

    private String createNumberedOptions(String[] options) {
        int i = 1;
        String s = "";
        for (String option : options) {
            s += String.format("%d) %s%n", i, option);
            i++;
        }
        return s;
    }

    private boolean setShopLocationIfValid(String userInput) {
        if (!isValidInt(userInput)) return false;

        int i = parseInt(userInput);
        //menus shown to the user start at 1. Array indices start at 0.
        i--; //convert from menu number to array index
        if (isValidShopLocation(i)) {
            shopLocationChoice = i;
            return true;
        } else {
            return false;
        }
    }

    private boolean setCurrencyIfValid(String userInput) {
        if (!isValidInt(userInput)) return false;

        int i = parseInt(userInput);
        i--; //convert from menu number to array index
        if (isValidCurrency(i)) {
            currencyChoice = i;
            return true;
        } else {
            return false;
        }
    }

    private boolean setItemPriceIfValid(String userInput) {
        if (!isValidDecimal(userInput)) return false;

        BigDecimal d = new BigDecimal(userInput);
        if (isValidPrice(d)) {
            currentItemPrice = d;
            return true;
        } else {
            return false;
        }
    }

    private boolean setItemQuantityIfValid(String userInput) {
        if (!isValidInt(userInput)) return false;

        int i = parseInt(userInput);
        if (isValidQuantity(i)) {
            currentItemQuantity = i;
            return true;
        } else {
            return false;
        }
    }

    private boolean parseYesNo(String line) {
        //anything that doesn't start with 'Y' or 'y' is false
        return line.trim().toLowerCase().startsWith("y");
    }

    private boolean isValidInt(String line) {
        int i;
        try {
            i = Integer.parseInt(line);
            return true;
        } catch (NumberFormatException e) {
            setErrorMessage("not a valid integer.");
            return false;
        }
    }

    private int parseInt(String line) {
        return Integer.parseInt(line);
    }

    private boolean isValidShopLocation(int i) {
        boolean valid = isValidArrayIndex(i, shopLocationChoices);
        if (!valid) {
            setErrorMessage("not a valid shop location choice");
        }
        return valid;
    }

    private boolean isValidCurrency(int i) {
        boolean valid = isValidArrayIndex(i, currencyChoices);
        if (!valid) {
            setErrorMessage("not a valid currency choice");
        }
        return valid;
    }

    private boolean isValidDecimal(String line) {
        double d;
        try {
            d = Double.parseDouble(line);
            return true;
        } catch (NumberFormatException e) {
            setErrorMessage("not a valid decimal number.");
            return false;
        }
    }

    private boolean isValidPrice(BigDecimal price) {
        boolean valid = price.compareTo(new BigDecimal(0)) >= 0; //things can be free but no cheaper
        if (!valid) {
            setErrorMessage("items cannot have a negative price.");
        }
        return valid;
    }

    private boolean isValidQuantity(int quantity) {
        boolean valid = quantity > 0;
        if (!valid) {
            setErrorMessage("there must be at least 1 of an item to add it to a receipt.");
        }
        return valid;
    }

    private void setErrorMessage(String errorMessage) {
        inputErrorMessage = errorMessage;
    }

    private boolean isValidArrayIndex(int index, String[] array) {
        return index >= 0 && index < array.length;
    }
}


