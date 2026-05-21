package com.example.cms;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class BinRegistry {

    // Sandbox BINs — replace with real issuer BINs in production
    // Format: BIN(8) + sequence(7) + Luhn(1) = 16-digit PAN
    private final Map<String, BinRange> registry = new HashMap<>();

    public BinRegistry() {

        // Visa Classic
        registry.put("CLASSIC", build(
                "45560100", "VC001", "CLASSIC", "CREDIT", 1_000_000, 9_999_999));

        // Visa Gold
        registry.put("GOLD", build(
                "45560200", "VC002", "GOLD", "CREDIT", 1_000_000, 9_999_999));

        // Visa Platinum
        registry.put("PLATINUM", build(
                "45569300", "VC003", "PLATINUM", "CREDIT", 1_000_000, 9_999_999));

        // Visa Signature
        registry.put("SIGNATURE", build(
                "45560400", "VC004", "SIGNATURE", "CREDIT", 1_000_000, 9_999_999));

        // Visa Infinite
        registry.put("INFINITE", build(
                "45560500", "VC005", "INFINITE", "CREDIT", 1_000_000, 9_999_999));

        // Virtual Visa
        registry.put("VIRTUAL", build(
                "45560600", "VC006", "VIRTUAL", "CREDIT", 1_000_000, 9_999_999));

        // Visa Debit
        registry.put("DEBIT", build(
                "41110100", "VD001", "DEBIT", "DEBIT", 1_000_000, 9_999_999));

        // Visa Prepaid
        registry.put("PREPAID", build(
                "40000100", "VP001", "PREPAID", "PREPAID", 1_000_000, 9_999_999));
    }

    public BinRange getBin(String cardCategory) {
        BinRange bin = registry.get(cardCategory.toUpperCase());
        if (bin == null)
            throw new IllegalArgumentException(
                    "No BIN configured for category: " + cardCategory);
        return bin;
    }

    // Atomically get and increment sequence
    public synchronized long nextSequence(String cardCategory) {
        BinRange bin = getBin(cardCategory);
        long seq = bin.getNextSequence();
        if (seq > bin.getMaxSequence())
            throw new RuntimeException("BIN sequence exhausted for: "
                    + cardCategory);
        bin.setNextSequence(seq + 1);
        return seq;
    }

    private BinRange build(String bin, String productCode,
                           String category, String cardType,
                           long minSeq, long maxSeq) {
        BinRange b = new BinRange();
        b.setScheme("VISA");
        b.setBin(bin);
        b.setPanLength(16);
        b.setCardCategory(category);
        b.setCardType(cardType);
        b.setProductCode(productCode);
        b.setCountry("SA");
        b.setCurrency("SAR");
        b.setMinSequence(minSeq);
        b.setMaxSequence(maxSeq);
        b.setNextSequence(minSeq);
        b.setStatus("ACTIVE");
        return b;
    }
}