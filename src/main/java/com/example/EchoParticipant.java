package com.example;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class EchoParticipant implements TransactionParticipant, Configurable {

    private String sourceKey = "REQUEST";
    private String destinationKey = "DESTINATION";

    // Simple in-memory duplicate check for local testing
    private static final Set<String> processedTransactions = new HashSet<>();

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.sourceKey = cfg.get("source", "REQUEST");
        this.destinationKey = cfg.get("destination", "DESTINATION");
    }

    @Override
    public int prepare(long id, Serializable context) {
        try {
            Context ctx = (Context) context;

            ISOMsg request = (ISOMsg) ctx.get(sourceKey);
            ISOSource source = (ISOSource) ctx.get(destinationKey);

            if (request == null) {
                System.out.println("No ISO request found in context using key: " + sourceKey);
                return ABORTED;
            }

            if (source == null) {
                System.out.println("No ISO source found in context using key: " + destinationKey);
                return ABORTED;
            }

            System.out.println("Received ISO request:");
            request.dump(System.out, "");

            ISOMsg response = (ISOMsg) request.clone();

            String mti = request.getMTI();

            if ("0800".equals(mti)) {
                response.setMTI("0810");
                response.set(39, "00");
            } else if ("0200".equals(mti)) {
                response.setMTI("0210");
                String responseCode = authorize(request);
                response.set(39, responseCode);
            } else {
                response.set(39, "12"); // Invalid transaction
            }

            System.out.println("Sending ISO response:");
            response.dump(System.out, "");

            source.send(response);

            return PREPARED | NO_JOIN | READONLY;

        } catch (Exception e) {
            e.printStackTrace();
            return ABORTED;
        }
    }

    private String authorize(ISOMsg request) {
        try {
            String pan = trim(request.getString(2));
            String processingCode = trim(request.getString(3));
            String amount = trim(request.getString(4));
            String transmissionDateTime = trim(request.getString(7));
            String stan = trim(request.getString(11));
            String posEntryMode = trim(request.getString(22));
            String rrn = trim(request.getString(37));
            String terminalId = trim(request.getString(41));
            String merchantId = trim(request.getString(42));
            String currency = trim(request.getString(49));

            // 1. Mandatory field validation
            if (isBlank(pan)) return "14";              // Invalid card number
            if (isBlank(processingCode)) return "12";   // Invalid transaction
            if (isBlank(amount)) return "13";           // Invalid amount
            if (isBlank(transmissionDateTime)) return "12";
            if (isBlank(stan)) return "12";
            if (isBlank(posEntryMode)) return "12";
            if (isBlank(rrn)) return "12";
            if (isBlank(terminalId)) return "58";       // Transaction not permitted at terminal
            if (isBlank(merchantId)) return "03";       // Invalid merchant
            if (isBlank(currency)) return "12";

            // 2. PAN validation
            if (!isValidPan(pan)) {
                return "14"; // Invalid card number
            }

            // 3. Blocked / declined PAN simulation
            if (pan.startsWith("555555")) {
                return "05"; // Do not honor
            }

            // 4. Restricted card simulation
            if (pan.startsWith("400000")) {
                return "62"; // Restricted card
            }

            // 5. Processing code validation
            if (!"000000".equals(processingCode)) {
                return "12"; // Invalid transaction
            }

            // 6. Amount validation
            long amountInHalalah = parseAmount(amount);

            if (amountInHalalah <= 0) {
                return "13"; // Invalid amount
            }

            // 7. Currency validation
            if (!"682".equals(currency)) {
                return "12"; // Invalid transaction / unsupported currency
            }

            // 8. Terminal validation
            if (!terminalId.startsWith("TERM")) {
                return "58"; // Transaction not permitted at terminal
            }

            // 9. Merchant validation
            if (!merchantId.startsWith("MERCHANT")) {
                return "03"; // Invalid merchant
            }

            // 10. Duplicate transaction check
            String duplicateKey = stan + "-" + rrn + "-" + amount;
            if (processedTransactions.contains(duplicateKey)) {
                return "94"; // Duplicate transaction
            }

            // 11. Simulate issuer unavailable
            if ("999999".equals(stan)) {
                return "91"; // Issuer unavailable
            }

            // 12. Simulate security violation
            if ("999".equals(posEntryMode)) {
                return "63"; // Security violation
            }

            // 13. Exceeds transaction limit
            // Example: more than 500 SAR
            if (amountInHalalah > 50000) {
                return "61"; // Exceeds withdrawal amount limit
            }

            // 14. Insufficient funds
            // Example: approve only up to 100 SAR for test card
            if (amountInHalalah > 10000) {
                return "51"; // Insufficient funds
            }

            // Mark as processed only when approved
            processedTransactions.add(duplicateKey);

            return "00"; // Approved

        } catch (Exception e) {
            e.printStackTrace();
            return "96"; // System malfunction
        }
    }

    private boolean isValidPan(String pan) {
        // Simple simulator validation
        // Real card validation may use Luhn check and BIN rules
        return pan != null && pan.matches("\\d{13,19}");
    }

    private long parseAmount(String amount) {
        // ISO amount field is usually 12 digits, minor unit
        // Example: 000000010000 = 100.00 SAR
        if (amount == null || !amount.matches("\\d{12}")) {
            return -1;
        }
        return Long.parseLong(amount);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void commit(long id, Serializable context) {
    }

    @Override
    public void abort(long id, Serializable context) {
    }
}