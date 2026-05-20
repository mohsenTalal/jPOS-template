package com.example.api;

import Api.PaymentsApi;
import Invokers.ApiClient;
import Invokers.ApiException;
import Model.*;
import com.cybersource.authsdk.core.MerchantConfig;

public class CyberSourceTest {

    public static void main(String[] args) throws Exception {

        MerchantConfig config = CyberSourceConfig.getConfig();
        ApiClient client = new ApiClient(config);
        PaymentsApi api  = new PaymentsApi(client);

        // --- Amount ---
        Ptsv2paymentsOrderInformationAmountDetails amount =
                new Ptsv2paymentsOrderInformationAmountDetails();
        amount.setTotalAmount("10.00");
        amount.setCurrency("SAR");

        // --- Billing address (required by CyberSource) ---
        Ptsv2paymentsOrderInformationBillTo billTo =
                new Ptsv2paymentsOrderInformationBillTo();
        billTo.setFirstName("Abdul");
        billTo.setLastName("Mohsen");
        billTo.setAddress1("123 Test Street");
        billTo.setLocality("Riyadh");
        billTo.setAdministrativeArea("01");
        billTo.setPostalCode("12345");
        billTo.setCountry("SA");
        billTo.setEmail("talal1mohsen@outlook.com");
        billTo.setPhoneNumber("0500000000");

        Ptsv2paymentsOrderInformation orderInfo =
                new Ptsv2paymentsOrderInformation();
        orderInfo.setAmountDetails(amount);
        orderInfo.setBillTo(billTo);

        // --- Card (Visa sandbox test PAN) ---
        Ptsv2paymentsPaymentInformationCard card =
                new Ptsv2paymentsPaymentInformationCard();
        card.setNumber("4111111111111111");
        card.setExpirationMonth("12");
        card.setExpirationYear("2031");
        card.setSecurityCode("123");

        Ptsv2paymentsPaymentInformation paymentInfo =
                new Ptsv2paymentsPaymentInformation();
        paymentInfo.setCard(card);

        // --- Build and send ---
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderInformation(orderInfo);
        request.setPaymentInformation(paymentInfo);

        try {
            PtsV2PaymentsPost201Response response = api.createPayment(request);
            System.out.println("========================================");
            System.out.println("Status   : " + response.getStatus());
            System.out.println("Trans ID : " + response.getId());
            System.out.println("Time     : " + response.getSubmitTimeUtc());
            System.out.println("========================================");
        } catch (ApiException e) {
            System.err.println("HTTP Status : " + e.getCode());
            System.err.println("Response    : " + e.getResponseBody());
        }
    }
}