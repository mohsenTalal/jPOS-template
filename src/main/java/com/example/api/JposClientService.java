package com.example.api;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.springframework.stereotype.Service;

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
            ISOMsg isoRequest = new ISOMsg();
            isoRequest.setMTI("0200");

            isoRequest.set(2, request.getPan());
            isoRequest.set(3, "000000");
            isoRequest.set(4, convertAmountToIsoAmount(request.getAmount()));
            isoRequest.set(7, transmissionDateTime());
            isoRequest.set(11, generateStan());
            isoRequest.set(22, "051");
            isoRequest.set(37, generateRrn());
            isoRequest.set(41, padRight(request.getTerminalId(), 8));
            isoRequest.set(42, padRight(request.getMerchantId(), 15));
            isoRequest.set(49, request.getCurrency());

            System.out.println("Sending ISO request from REST API:");
            isoRequest.dump(System.out, "");

            channel.send(isoRequest);

            ISOMsg isoResponse = channel.receive();

            System.out.println("Received ISO response from jPOS:");
            isoResponse.dump(System.out, "");

            String responseCode = isoResponse.getString(39);

            return new PaymentAuthorizeResponse(
                    isoResponse.getMTI(),
                    responseCode,
                    mapResponseCode(responseCode),
                    isoResponse.getString(11),
                    isoResponse.getString(37)
            );

        } finally {
            channel.disconnect();
        }
    }

    private String convertAmountToIsoAmount(String amount) {
        // Example: "100.00" -> "000000010000"
        double value = Double.parseDouble(amount);
        long cents = Math.round(value * 100);
        return String.format("%012d", cents);
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