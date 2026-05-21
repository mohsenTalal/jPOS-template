package com.example.cms;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CardProductService {

    // Product codes mapped to card categories per your document
    private static final Map<String, CardProduct> PRODUCTS = new HashMap<>();

    static {
        PRODUCTS.put("CLASSIC",   new CardProduct("VC001", "4532", 1000,  3));
        PRODUCTS.put("GOLD",      new CardProduct("VC002", "4539", 5000,  3));
        PRODUCTS.put("PLATINUM",  new CardProduct("VC003", "4556", 10000, 3));
        PRODUCTS.put("SIGNATURE", new CardProduct("VC004", "4929", 25000, 5));
        PRODUCTS.put("INFINITE",  new CardProduct("VC005", "4916", 50000, 5));
        PRODUCTS.put("VIRTUAL",   new CardProduct("VC006", "4024", 5000,  1));
        PRODUCTS.put("DEBIT",     new CardProduct("VD001", "4111", 10000, 5));
        PRODUCTS.put("PREPAID",   new CardProduct("VP001", "4000", 2000,  3));
    }

    public CardProduct getProduct(String cardCategory) {
        CardProduct p = PRODUCTS.get(cardCategory.toUpperCase());
        if (p == null) throw new IllegalArgumentException(
                "Unknown card category: " + cardCategory);
        return p;
    }

    // PAN generation per your document:
    // BIN range + product code + sequence logic + Luhn check digit
    public String generatePAN(CardProduct product) {
        String bin      = product.getBinRange();
        String sequence = String.format("%09d",
                (long)(Math.random() * 1_000_000_000));
        String partial  = bin + sequence;
        String luhn     = partial + computeLuhn(partial);
        return luhn;
    }

    // Luhn algorithm for check digit
    private int computeLuhn(String partialPan) {
        int sum = 0;
        boolean alternate = true;
        for (int i = partialPan.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(String.valueOf(partialPan.charAt(i)));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    // Expiry date per your document rules
    public String[] generateExpiry(String cardCategory) {
        java.time.LocalDate now = java.time.LocalDate.now();
        int years = cardCategory.equals("VIRTUAL") ? 1 : 3;
        java.time.LocalDate expiry = now.plusYears(years);
        return new String[]{
                String.format("%02d", expiry.getMonthValue()),
                String.valueOf(expiry.getYear())
        };
    }
}