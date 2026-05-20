package com.example.api;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class JposClientService {

    private static final String JPOS_HOST = "localhost";
    private static final int JPOS_PORT = 8000;

    public PaymentAuthorizeResponse authorize(PaymentAuthorizeRequest request) throws Exception {

        ISO87APackager packager = new ISO87APackager();

        ASCIIChannel channel = new ASCIIChannel(JPOS_HOST, JPOS_PORT, packager);
        channel.connect();

        try {
            String stan = generateStan();
            String rrn = generateRrn();

            ISOMsg isoRequest = new ISOMsg();
            isoRequest.setMTI("0200");

            // Card details
            isoRequest.set(2, request.getCardNumber());
            isoRequest.set(3, "000000"); // Purchase
            isoRequest.set(4, convertAmountToIsoAmount(request.getAmount()));
            isoRequest.set(7, transmissionDateTime());
            isoRequest.set(11, stan);

            // Expiry date: field 14 = YYMM
            isoRequest.set(14, toIsoExpiry(request.getExpiryMonth(), request.getExpiryYear()));

            // POS / merchant details
            isoRequest.set(22, "051"); // e-commerce/manual entry simulation
            isoRequest.set(37, rrn);
            isoRequest.set(41, padRight("TERM001", 8));
            isoRequest.set(42, padRight("MERCHANT000001", 15));
            isoRequest.set(49, mapCurrency(request.getCurrency()));

            // Optional card acceptor name/location field
            isoRequest.set(43, padRight(buildCardAcceptorName(request), 40));

            System.out.println("Sending ISO request from REST API:");
            isoRequest.dump(System.out, "");

            channel.send(isoRequest);

            ISOMsg isoResponse = channel.receive();

            System.out.println("Received ISO response from jPOS:");
            isoResponse.dump(System.out, "");

            String responseCode = isoResponse.getString(39);
            String authorizationCode = isoResponse.hasField(38)
                    ? isoResponse.getString(38)
                    : null;

            return new PaymentAuthorizeResponse(
                    isoResponse.getMTI(),
                    responseCode,
                    mapResponseCode(responseCode),
                    isoResponse.getString(11),
                    isoResponse.getString(37),
                    authorizationCode,
                    null
            );

        } finally {
            channel.disconnect();
        }
    }

    private String convertAmountToIsoAmount(String amount) {
        // Example: "10.00" -> "000000001000"
        BigDecimal value = new BigDecimal(amount);
        BigDecimal cents = value.multiply(new BigDecimal("100"));
        return String.format("%012d", cents.longValue());
    }

    private String transmissionDateTime() {
        // ISO field 7 = MMDDhhmmss
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
    }

    private String generateStan() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private String generateRrn() {
        return String.format("%012d", Math.abs(new Random().nextLong()) % 1_000_000_000_000L);
    }

    private String toIsoExpiry(String expiryMonth, String expiryYear) {
        if (expiryMonth == null || expiryYear == null) {
            throw new IllegalArgumentException("expiryMonth and expiryYear are required");
        }

        String mm = expiryMonth.length() == 1 ? "0" + expiryMonth : expiryMonth;
        String yy = expiryYear.length() == 4
                ? expiryYear.substring(2, 4)
                : expiryYear;

        return yy + mm; // YYMM
    }

    private String mapCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "682";
        }

        return switch (currency.toUpperCase()) {
            case "SAR" -> "682";
            case "USD" -> "840";
            case "EUR" -> "978";
            default -> currency;
        };
    }

    private String buildCardAcceptorName(PaymentAuthorizeRequest request) {
        String city = request.getCity() != null ? request.getCity() : "Riyadh";
        String country = request.getCountry() != null ? request.getCountry() : "SA";
        return "ECOMMERCE MERCHANT " + city + " " + country;
    }

    private String padRight(String value, int length) {
        if (value == null) {
            value = "";
        }

        if (value.length() >= length) {
            return value.substring(0, length);
        }

        return String.format("%-" + length + "s", value);
    }

    private String mapResponseCode(String code) {
        return switch (code) {
            case "00" -> "Approved";
            case "03" -> "Invalid merchant";
            case "05" -> "Do not honor";
            case "12" -> "Invalid transaction";
            case "13" -> "Invalid amount";
            case "14" -> "Invalid card number";
            case "51" -> "Insufficient funds";
            case "54" -> "Expired card";
            case "58" -> "Transaction not permitted at terminal";
            case "61" -> "Exceeds withdrawal amount limit";
            case "62" -> "Restricted card";
            case "63" -> "Security violation";
            case "91" -> "Issuer or switch inoperative";
            case "94" -> "Duplicate transaction";
            case "96" -> "System malfunction";
            default -> "Unknown response";
        };
    }
}