package com.example.cms;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

@Service
public class CvvService {

    // In production: call real HSM via SMAdapter
    // For sandbox: software simulation only

    public CvvResult generateCvv(String pan, String expiryMonth,
                                 String expiryYear) throws Exception {
        CvvResult result = new CvvResult();

        // CVV  — magnetic stripe (3 digits)
        result.setCvv(computeCvv(pan, expiryMonth, expiryYear, "001"));

        // iCVV — chip card (3 digits, different service code)
        result.setIcvv(computeCvv(pan, expiryMonth, expiryYear, "999"));

        // CVV2 — card not present / e-commerce (3 digits)
        result.setCvv2(computeCvv(pan, expiryMonth, expiryYear, "000"));

        return result;
    }

    private String computeCvv(String pan, String month,
                              String year, String serviceCode) throws Exception {
        // Software simulation — replace with HSM call in production
        String data = pan + year.substring(2) + month + serviceCode;
        Mac mac = Mac.getInstance("HmacSHA256");
        // TODO: Use real CVK from HSM key store
        mac.init(new SecretKeySpec(
                "sandbox-cvk-key-replace-in-prod".getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes());
        int cvv = Math.abs((hash[0] << 16 | hash[1] << 8 | hash[2]) % 1000);
        return String.format("%03d", cvv);
    }
}