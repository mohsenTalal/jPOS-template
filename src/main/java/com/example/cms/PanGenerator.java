package com.example.cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PanGenerator {

    @Autowired
    private BinRegistry binRegistry;

    /**
     * Generates a full 16-digit PAN:
     *
     * Example for PLATINUM:
     *   BIN prefix:        45569300        (8 digits — from BIN registry)
     *   CMS sequence:      1000001         (7 digits — auto-incremented)
     *   Luhn check digit:  6               (1 digit  — computed)
     *   Final PAN:         4556930010000016
     */
    public PanResult generate(String cardCategory) {
        BinRange binRange = binRegistry.getBin(cardCategory);
        long sequence     = binRegistry.nextSequence(cardCategory);

        // Build body: BIN(8) + sequence(7)
        String body = binRange.getBin() +
                String.format("%07d", sequence);  // 8 + 7 = 15 digits

        // Compute Luhn check digit (1 digit)
        int checkDigit = computeLuhnCheckDigit(body);

        // Final PAN = body(15) + checkDigit(1) = 16 digits
        String pan = body + checkDigit;

        PanResult result = new PanResult();
        result.setPan(pan);
        result.setBin(binRange.getBin());
        result.setSequence(sequence);
        result.setCheckDigit(checkDigit);
        result.setMaskedPan(mask(pan));
        result.setProductCode(binRange.getProductCode());
        result.setPanLength(pan.length());

        return result;
    }

    /**
     * Validates any PAN using Luhn algorithm.
     * Returns true if valid.
     */
    public boolean validateLuhn(String pan) {
        if (pan == null || pan.isEmpty()) return false;
        String body = pan.substring(0, pan.length() - 1);
        int expectedCheck = computeLuhnCheckDigit(body);
        int actualCheck   = Character.getNumericValue(
                pan.charAt(pan.length() - 1));
        return expectedCheck == actualCheck;
    }

    /**
     * Luhn check digit algorithm.
     *
     * Steps:
     * 1. Start from the rightmost digit, double every second digit
     * 2. If doubling gives > 9, subtract 9
     * 3. Sum all digits
     * 4. Check digit = (10 - sum % 10) % 10
     */
    private int computeLuhnCheckDigit(String body) {
        int sum       = 0;
        boolean doubleIt = true;  // rightmost gets doubled first

        for (int i = body.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(body.charAt(i));
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return (10 - (sum % 10)) % 10;
    }

    // Mask PAN: show first 6 and last 4 only
    private String mask(String pan) {
        if (pan.length() < 10) return "****";
        return pan.substring(0, 6)
                + "*".repeat(pan.length() - 10)
                + pan.substring(pan.length() - 4);
    }
}