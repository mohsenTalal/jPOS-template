package com.example.origination;

import com.example.api.CyberSourceConfig;
import com.example.card.CardAccount;
import com.example.cms.*;
import Api.PaymentsApi;
import Invokers.ApiClient;
import Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/origination")
public class CardOriginationController {

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private CardProductService cardProductService;

    @Autowired
    private CvvService cvvService;

    @Autowired
    private PanGenerator panGenerator;

    // In-memory store — replace with DB in production
    private final ConcurrentHashMap<String, Map<String, Object>>
            cardStore = new ConcurrentHashMap<>();

    @PostMapping("/provision")
    public ResponseEntity<?> provisionCard(
            @RequestBody CardOriginationRequest req) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();

            // STEP 3 — Eligibility validation
            EligibilityResult eligibility =
                    eligibilityService.validate(req);
            if (!eligibility.isEligible()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "REJECTED",
                        "reasons", eligibility.getFailureReasons()
                ));
            }

            // STEP 5 — Select card product
            CardProduct product =
                    cardProductService.getProduct(req.getCardCategory());

            // STEP 6 — Generate PAN from BIN range - old
            //String pan = cardProductService.generatePAN(product);

            // STEP 6 — Generate PAN from BIN range using real BIN + Luhn
            PanResult panResult = panGenerator.generate(req.getCardCategory());
            String pan          = panResult.getPan();

            if (!panGenerator.validateLuhn(pan)) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "PAN_LUHN_VALIDATION_FAILED"));
            }

            // STEP 7 — Generate expiry date
            String[] expiry = cardProductService.generateExpiry(
                    req.getCardCategory());
            String expiryMonth = expiry[0];
            String expiryYear  = expiry[1];

            // STEPS 8–9 — Request CVV/iCVV/CVV2 from HSM
            CvvResult cvvResult = cvvService.generateCvv(
                    pan, expiryMonth, expiryYear);

            // STEP 10 — Store card profile securely
            String cardId = UUID.randomUUID().toString();

            // STEP 11 — Link card to customer and account
            Map<String, Object> cardProfile = new LinkedHashMap<>();
            cardProfile.put("cardId",        cardId);
            cardProfile.put("customerId",    req.getCustomerId());
            cardProfile.put("accountNumber", req.getAccountNumber());
            cardProfile.put("cardCategory",  req.getCardCategory());
            cardProfile.put("cardType",      req.getCardType());
            cardProfile.put("cardForm",      req.getCardForm());
            cardProfile.put("maskedPan",     panResult.getMaskedPan());
            cardProfile.put("bin",           panResult.getBin());
            cardProfile.put("sequence",      panResult.getSequence());
            cardProfile.put("checkDigit",    panResult.getCheckDigit());
            cardProfile.put("productCode",   panResult.getProductCode());
            cardProfile.put("expiryMonth",   expiryMonth);
            cardProfile.put("expiryYear",    expiryYear);


            // STEP 12 — Assign card limits
            CardLimits limits = CardLimits.fromProduct(
                    product, req.getRequestedLimit());
            cardProfile.put("dailyLimit",       limits.getDailyPurchaseLimit());
            cardProfile.put("monthlyLimit",     limits.getMonthlyPurchaseLimit());
            cardProfile.put("atmLimit",         limits.getAtmWithdrawalLimit());
            cardProfile.put("ecommerceLimit",   limits.getEcommerceLimit());
            cardProfile.put("contactlessLimit", limits.getContactlessLimit());
            cardProfile.put("creditLimit",      limits.getCreditLimit());

            // STEP 13 — Register for authorization via CyberSource
            String csToken = registerWithCyberSource(
                    pan, expiryMonth, expiryYear, cvvResult, req);
            cardProfile.put("csToken", csToken);

            // STEP 15 — Activate or keep inactive pending delivery
            String initialStatus = "VIRTUAL".equals(req.getCardForm())
                    ? "ACTIVE" : "INACTIVE_PENDING_DELIVERY";
            cardProfile.put("status", initialStatus);
            cardProfile.put("createdAt", LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            cardStore.put(cardId, cardProfile);

            // STEP 16 — Return provisioning status
            // Never return PAN or CVV in response
            response.put("cardId",       cardId);
            response.put("customerId",   req.getCustomerId());
            response.put("maskedPan",    panResult.getMaskedPan());
            response.put("bin",          panResult.getBin());
            response.put("sequence",     panResult.getSequence());
            response.put("checkDigit",   panResult.getCheckDigit());
            response.put("expiryMonth",  expiryMonth);
            response.put("expiryYear",   expiryYear);
            response.put("cardCategory", req.getCardCategory());
            response.put("cardType",     req.getCardType());
            response.put("cardForm",     req.getCardForm());
            response.put("productCode",  product.getProductCode());
            response.put("status",       initialStatus);
            response.put("csToken",      csToken);
            response.put("limits", Map.of(
                    "daily",       limits.getDailyPurchaseLimit(),
                    "monthly",     limits.getMonthlyPurchaseLimit(),
                    "atm",         limits.getAtmWithdrawalLimit(),
                    "ecommerce",   limits.getEcommerceLimit(),
                    "contactless", limits.getContactlessLimit()
            ));
            response.put("createdAt", cardProfile.get("createdAt"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get card by ID
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(@PathVariable String cardId) {
        Map<String, Object> card = cardStore.get(cardId);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    // Get all cards for customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerCards(
            @PathVariable String customerId) {
        List<Map<String, Object>> cards = cardStore.values().stream()
                .filter(c -> customerId.equals(c.get("customerId")))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(cards);
    }

    // Activate card after delivery
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<?> activateCard(@PathVariable String cardId) {
        Map<String, Object> card = cardStore.get(cardId);
        if (card == null) return ResponseEntity.notFound().build();
        card.put("status", "ACTIVE");
        return ResponseEntity.ok(card);
    }

    // Block card
    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable String cardId) {
        Map<String, Object> card = cardStore.get(cardId);
        if (card == null) return ResponseEntity.notFound().build();
        card.put("status", "BLOCKED");
        return ResponseEntity.ok(card);
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private String registerWithCyberSource(String pan, String expiryMonth,
                                           String expiryYear, CvvResult cvv,
                                           CardOriginationRequest req) throws Exception {

        var config = CyberSourceConfig.getConfig();
        var client = new ApiClient(config);
        var api    = new PaymentsApi(client);

        var amount = new Ptsv2paymentsOrderInformationAmountDetails();
        amount.setTotalAmount("0.00");
        amount.setCurrency(req.getCurrency() != null
                ? req.getCurrency() : "SAR");

        var billTo = new Ptsv2paymentsOrderInformationBillTo();
        billTo.setFirstName("Card");
        billTo.setLastName("Holder");
        billTo.setAddress1(req.getDeliveryAddress() != null
                ? req.getDeliveryAddress() : "123 Main St");
        billTo.setLocality("Riyadh");
        billTo.setAdministrativeArea("01");
        billTo.setPostalCode("12345");
        billTo.setCountry("SA");
        billTo.setEmail("card@bank.sa");

        var orderInfo = new Ptsv2paymentsOrderInformation();
        orderInfo.setAmountDetails(amount);
        orderInfo.setBillTo(billTo);

        var card = new Ptsv2paymentsPaymentInformationCard();
        card.setNumber(pan);
        card.setExpirationMonth(expiryMonth);
        card.setExpirationYear(expiryYear);
        card.setSecurityCode(cvv.getCvv2());

        var paymentInfo = new Ptsv2paymentsPaymentInformation();
        paymentInfo.setCard(card);

        var clientRef = new Ptsv2paymentsClientReferenceInformation();
        clientRef.setCode("PROVISION-" + req.getCustomerId());

        var request = new CreatePaymentRequest();
        request.setOrderInformation(orderInfo);
        request.setPaymentInformation(paymentInfo);
        request.setClientReferenceInformation(clientRef);

        var res = api.createPayment(request);
        return res.getId();
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) return "****";
        return pan.substring(0, 6)
                + "*".repeat(pan.length() - 10)
                + pan.substring(pan.length() - 4);
    }
}