package com.example.card;

import Api.PaymentsApi;
import Invokers.ApiClient;
import Model.*;
import com.cybersource.authsdk.core.MerchantConfig;
import com.example.api.CyberSourceConfig;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CardProvisioningService {

    // In-memory store — replace with a database in production
    private final ConcurrentHashMap<String, CardAccount> cardStore =
            new ConcurrentHashMap<>();

    // ── 1. Provision a new card for a customer ──────────────────────────────
    public CardAccount provisionCard(CardProvisionRequest req) throws Exception {

        CardAccount card = new CardAccount();
        card.setCardId(UUID.randomUUID().toString());
        card.setCustomerId(req.getCustomerId());
        card.setPan(req.getCardNumber());
        card.setMaskedPan(maskPan(req.getCardNumber()));
        card.setExpiryMonth(req.getExpiryMonth());
        card.setExpiryYear(req.getExpiryYear());
        card.setCardholderName(req.getCardholderName());
        card.setStatus("ACTIVE");
        card.setCreatedAt(LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Tokenize via CyberSource
        String token = tokenizeCard(req);
        card.setToken(token);

        // Store the card
        cardStore.put(card.getCardId(), card);

        // Mask PAN before returning — never return real PAN
        card.setPan(null);

        return card;
    }

    // ── 2. Get card by ID ───────────────────────────────────────────────────
    public CardAccount getCard(String cardId) {
        CardAccount card = cardStore.get(cardId);
        if (card != null) {
            card.setPan(null); // Never expose PAN
        }
        return card;
    }

    // ── 3. Get all cards for a customer ────────────────────────────────────
    public java.util.List<CardAccount> getCustomerCards(String customerId) {
        return cardStore.values().stream()
                .filter(c -> customerId.equals(c.getCustomerId()))
                .peek(c -> c.setPan(null))
                .collect(java.util.stream.Collectors.toList());
    }

    // ── 4. Block a card ─────────────────────────────────────────────────────
    public CardAccount blockCard(String cardId) {
        CardAccount card = cardStore.get(cardId);
        if (card != null) {
            card.setStatus("BLOCKED");
        }
        return card;
    }

    // ── 5. Activate a card ──────────────────────────────────────────────────
    public CardAccount activateCard(String cardId) {
        CardAccount card = cardStore.get(cardId);
        if (card != null) {
            card.setStatus("ACTIVE");
        }
        return card;
    }

    // ── Internal: Tokenize via CyberSource ─────────────────────────────────
    private String tokenizeCard(CardProvisionRequest req) throws Exception {
        MerchantConfig config = CyberSourceConfig.getConfig();
        ApiClient client      = new ApiClient(config);
        PaymentsApi api       = new PaymentsApi(client);

        // Run a $0 authorization to validate and tokenize the card
        Ptsv2paymentsOrderInformationAmountDetails amount =
                new Ptsv2paymentsOrderInformationAmountDetails();
        amount.setTotalAmount("0.00");
        amount.setCurrency("SAR");

        Ptsv2paymentsOrderInformationBillTo billTo =
                new Ptsv2paymentsOrderInformationBillTo();
        billTo.setFirstName(req.getCardholderName().split(" ")[0]);
        billTo.setLastName(req.getCardholderName().contains(" ")
                ? req.getCardholderName().split(" ", 2)[1] : "N/A");
        billTo.setAddress1(req.getBillingAddress());
        billTo.setLocality(req.getBillingCity());
        billTo.setAdministrativeArea("01");
        billTo.setPostalCode(req.getPostalCode());
        billTo.setCountry(req.getCountry());
        billTo.setEmail(req.getEmail());

        Ptsv2paymentsOrderInformation orderInfo =
                new Ptsv2paymentsOrderInformation();
        orderInfo.setAmountDetails(amount);
        orderInfo.setBillTo(billTo);

        Ptsv2paymentsPaymentInformationCard card =
                new Ptsv2paymentsPaymentInformationCard();
        card.setNumber(req.getCardNumber());
        card.setExpirationMonth(req.getExpiryMonth());
        card.setExpirationYear(req.getExpiryYear());
        card.setSecurityCode(req.getCvv());

        Ptsv2paymentsPaymentInformation paymentInfo =
                new Ptsv2paymentsPaymentInformation();
        paymentInfo.setCard(card);

        // Request tokenization
        Ptsv2paymentsTokenInformation tokenInfo =
                new Ptsv2paymentsTokenInformation();
        tokenInfo.setTransientTokenJwt(null);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderInformation(orderInfo);
        request.setPaymentInformation(paymentInfo);

        Ptsv2paymentsClientReferenceInformation clientRef =
                new Ptsv2paymentsClientReferenceInformation();
        clientRef.setCode("PROVISION-" + req.getCustomerId());
        request.setClientReferenceInformation(clientRef);

        PtsV2PaymentsPost201Response response = api.createPayment(request);

        // Return the CyberSource transaction ID as the token
        return response.getId();
    }

    // ── Internal: Mask PAN ──────────────────────────────────────────────────
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 8) return "****";
        return pan.substring(0, 6) +
                "*".repeat(pan.length() - 10) +
                pan.substring(pan.length() - 4);
    }
}