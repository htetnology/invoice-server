import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class ReceiptFormatter {
    private static final String POUND_SIGN = "\u00A3";
    private static final String EURO_SIGN = "\u20AC";
    private static final String[] CURRENCIES = { POUND_SIGN, EURO_SIGN };
    private static final String[] SHOP_LOCATIONS = {
            "King's Cross",
            "Victoria",
            "Waterloo",
            "Euston",
            "Liverpool Street",
            "London Bridge",
            "Paddington",
            "Marylebone"
    };
    private static final String[] TEMPLATE = {
            "%n****************************************%n* %s *%n****************************************%n%n", // + COMPANY_SLOGAN
            "Date - %s%n%n", // + invoice reference no
            "FixAllPhones - %s%n%n", // + shop location
            "VAT No: %s%n%n", // + vat number
            "Served by: %s%n%n", // + staff member's first name
            "---------------------------------------------%nYour order:%n%n", // no additional info
            "%s%n%n", // + costs breakdown
            "Subtotal%s                   %s%.2f%n", // + padding, currency symbol, subtotal
            "Tax (VAT @ %.1f%%)%s          %s%.2f%n%n", // + vat rate, padding, currency symbol, vat amount
            "TOTAL : %d item%s             %s%.2f%n%n", // + number of items, s or no s (single or plural), currency symbol, total amount payable
            "---------------------------------------------%n%n", //no additional info
            "Paid with %s%n%n%n", // + payment type (card/cash/bitcoin/etc)
            "Exchange and returns policy - %s%n%n", // + returns policy url
            "All repairs guaranteed for 18 months%nRepair T&Cs - %s%n%n%n", // + terms & conditions url
            "Rate our Service%n%s%n%n%n", // + 3rd party review site url
            "Get 20%% off your order when you refer a friend - see our web page for details%n%s%n%n", // + referral url
            "Follow us on %s%n" // + our social media channels
    };
    private static final String RETURNS_POLICY_URL = "http://www.FixAllPhones.co.uk/returns";
    private static final String TERMS_AND_CONDITIONS_URL = "http://www.FixAllPhones.co.uk/terms";
    private static final String SERVICE_REVIEW_URL = "https//uk.trustpilot.com/review/www.FixAllPhones.co.uk";
    private static final String REFERRAL_URL = "http://www.FixAllPhones.co.uk/phonefriend";
    private static final String SOCIAL_MEDIA = "FaceBook and Twitter";
    private static final String COMPANY_SLOGAN = "FixAllPhones - repairs you can trust";
    private static final String VAT_NUMBER = "123456789GB";
    private static final BigDecimal VAT_PERCENTAGE = new BigDecimal(20);

    private String currencySymbol;
    private String referenceNumber;
    private String shopLocation;
    private String staffName;
    private String itemisedCosts;
    private String paymentMethod;
    private int numberOfItemsPurchased;
    private BigDecimal subtotal;
    private BigDecimal vat;
    private BigDecimal total;
    private List<ReceiptItem> receiptItems;
    private int longestItemDescription;

    public String[] getCurrencies() {
        return CURRENCIES;
    }

    public String[] getShopLocations() {
        return SHOP_LOCATIONS;
    }

    public String[] createReceipt(int shopLocationChoice, String staffName, String paymentMethod, List<ReceiptItem> items, int currencySymbolChoice) {
        if (indexOutOfRange(SHOP_LOCATIONS, shopLocationChoice)) throw new IllegalArgumentException("Not a valid location");
        if (indexOutOfRange(CURRENCIES, currencySymbolChoice)) throw new IllegalArgumentException("Not a valid currency");

        init();
        this.shopLocation = SHOP_LOCATIONS[shopLocationChoice];
        this.currencySymbol = CURRENCIES[currencySymbolChoice];
        this.staffName = staffName;
        this.paymentMethod = paymentMethod;
        this.receiptItems = items;
        referenceNumber = createReferenceNumber();

        createItemisedCostsAndSubtotal();
        vat = getVat(subtotal);
        total = addVat(subtotal);
        String s = "";
        for (int i = 0; i < TEMPLATE.length; i++) {
            s += String.format(TEMPLATE[i], getTemplateData(i));
        }

        return s.split(System.lineSeparator());
    }

    private boolean indexOutOfRange(String[] array, int index) {
        return index < 0 || index >= array.length;
    }

    private void init() {
        referenceNumber = "";
        itemisedCosts = "";
        numberOfItemsPurchased = 0;
        subtotal = new BigDecimal(0);
        vat = new BigDecimal(0);
        total = new BigDecimal(0);
        longestItemDescription = 0;
    }

    private String createReferenceNumber() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%1$tY/%<tm/%<td %<tH:%<tM:%<tS", now);
    }

    private void createItemisedCostsAndSubtotal() {
        calculateLongestItemDescription();
        for (ReceiptItem item : receiptItems) {
            itemisedCosts += addItemisedCostToReceipt(item, calculateItemDescriptionPadding(item));
            numberOfItemsPurchased += item.getQuantity();
            updateSubtotal(item);
        }
    }

    private void calculateLongestItemDescription() {
        for (ReceiptItem item : receiptItems) {
            String concat = getConcatenatedString(item);
            int length = concat.length();
            if (length > longestItemDescription) {
                longestItemDescription = length;
            }
        }
    }

    private String calculateItemDescriptionPadding(ReceiptItem item) {
        String concat = getConcatenatedString(item);
        int length = concat.length();
        return " ".repeat(longestItemDescription - length);
    }

    private String getConcatenatedString(ReceiptItem item) {
        String desc = item.getDescription();
        int quantity = item.getQuantity();
        BigDecimal formattedPrice = item.getPrice().setScale(2, RoundingMode.HALF_UP);
        return desc + quantity + formattedPrice;
    }

    private void updateSubtotal(ReceiptItem item) {
        BigDecimal price = item.getPrice();
        BigDecimal quantity = new BigDecimal(item.getQuantity());
        BigDecimal totalCostOfItem = price.multiply(quantity);
        subtotal = subtotal.add(totalCostOfItem);
    }

    private String addItemisedCostToReceipt(ReceiptItem item, String padding) {
        BigDecimal price = item.getPrice();
        int quantity = item.getQuantity();
        //format: numberOfUnits x itemDescription @ £unitPrice each: £(unitPrice*NumberOfUnits)
        return String.format("%d x %s @ %s%.2f each%s        %3$s%.2f%n", quantity, item.getDescription(), currencySymbol, price, padding, price.multiply(new BigDecimal(quantity)));
    }

    private String calculateCostsBreakdownPadding() {
        //if there's only 1 item, everything's aligned.
        if (numberOfItemsPurchased == 1) {
            return "";
        }
        //beyond that either "item" becoming "items" or the number of items growing beyond one digit necessitates padding
        return " ".repeat((""+numberOfItemsPurchased).length());
    }

    private BigDecimal addVat(BigDecimal amount) {
        BigDecimal vat = getVat(amount);

        return amount.add(vat);
    }

    private BigDecimal getVat(BigDecimal amount) {
        //(amount * 20) / 100
        return amount.multiply(VAT_PERCENTAGE).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    private Object[] getTemplateData(int line) {
        if (line == 0) return new Object[]{ COMPANY_SLOGAN };
        if (line == 1) return new Object[]{ referenceNumber };
        if (line == 2) return new Object[]{ shopLocation };
        if (line == 3) return new Object[]{ VAT_NUMBER };
        if (line == 4) return new Object[]{ staffName };
        if (line == 6) return new Object[]{ itemisedCosts };
        if (line == 7) return new Object[]{ calculateCostsBreakdownPadding(), currencySymbol, subtotal };
        if (line == 8) return new Object[]{ VAT_PERCENTAGE, calculateCostsBreakdownPadding(), currencySymbol, vat};
        if (line == 9) return new Object[]{ numberOfItemsPurchased, numberOfItemsPurchased > 1 ? "s" : "", currencySymbol, total };
        if (line == 11) return new Object[]{ paymentMethod };
        if (line == 12) return new Object[]{ RETURNS_POLICY_URL };
        if (line == 13) return new Object[]{ TERMS_AND_CONDITIONS_URL };
        if (line == 14) return new Object[]{ SERVICE_REVIEW_URL };
        if (line == 15) return new Object[]{ REFERRAL_URL };
        if (line == 16) return new Object[]{ SOCIAL_MEDIA };
        else return new Object[]{ "" }; //any lines that don't need extra data
    }
}


