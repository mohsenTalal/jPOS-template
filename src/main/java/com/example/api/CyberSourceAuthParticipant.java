package com.example.api;

import Api.PaymentsApi;
import Invokers.ApiClient;
import Invokers.ApiException;
import Model.*;
import com.cybersource.authsdk.core.MerchantConfig;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class CyberSourceAuthParticipant implements TransactionParticipant {

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg  = (ISOMsg) ctx.get("REQUEST");

        try {
            // --- Read fields from ISO-8583 message ---
            String pan        = msg.getString(2);   // PAN
            String expiry     = msg.getString(14);  // YYMM
            String amount     = msg.getString(4);   // 12-digit amount
            String currency   = ctx.getString("CURRENCY") != null
                    ? ctx.getString("CURRENCY") : "SAR";

            // Format amount: ISO-8583 stores as 000000001000 = 10.00
            String formatted  = String.valueOf(Long.parseLong(amount) / 100.0);

            // Format expiry: YYMM → MM and 20YY
            String expMonth   = expiry.substring(2, 4);
            String expYear    = "20" + expiry.substring(0, 2);

            // --- Build CyberSource request ---
            MerchantConfig config = CyberSourceConfig.getConfig();
            ApiClient client      = new ApiClient(config);
            PaymentsApi api       = new PaymentsApi(client);

            // Amount
            Ptsv2paymentsOrderInformationAmountDetails amountDetails =
                    new Ptsv2paymentsOrderInformationAmountDetails();
            amountDetails.setTotalAmount(formatted);
            amountDetails.setCurrency(currency);

            // BillTo — in production pull from cardholder data
            Ptsv2paymentsOrderInformationBillTo billTo =
                    new Ptsv2paymentsOrderInformationBillTo();
            billTo.setFirstName(ctx.getString("CARDHOLDER_FIRST") != null
                    ? ctx.getString("CARDHOLDER_FIRST") : "Card");
            billTo.setLastName(ctx.getString("CARDHOLDER_LAST") != null
                    ? ctx.getString("CARDHOLDER_LAST") : "Holder");
            billTo.setAddress1(ctx.getString("BILLING_ADDRESS") != null
                    ? ctx.getString("BILLING_ADDRESS") : "123 Main St");
            billTo.setLocality(ctx.getString("BILLING_CITY") != null
                    ? ctx.getString("BILLING_CITY") : "Riyadh");
            billTo.setAdministrativeArea("01");
            billTo.setPostalCode("12345");
            billTo.setCountry("SA");
            billTo.setEmail(ctx.getString("CARDHOLDER_EMAIL") != null
                    ? ctx.getString("CARDHOLDER_EMAIL") : "test@test.com");

            Ptsv2paymentsOrderInformation orderInfo =
                    new Ptsv2paymentsOrderInformation();
            orderInfo.setAmountDetails(amountDetails);
            orderInfo.setBillTo(billTo);

            // Card
            Ptsv2paymentsPaymentInformationCard card =
                    new Ptsv2paymentsPaymentInformationCard();
            card.setNumber(pan);
            card.setExpirationMonth(expMonth);
            card.setExpirationYear(expYear);

            Ptsv2paymentsPaymentInformation paymentInfo =
                    new Ptsv2paymentsPaymentInformation();
            paymentInfo.setCard(card);

            // Client reference — use ISO field 37 (retrieval ref number)
            Ptsv2paymentsClientReferenceInformation clientRef =
                    new Ptsv2paymentsClientReferenceInformation();
            clientRef.setCode(msg.hasField(37)
                    ? msg.getString(37) : String.valueOf(id));

            // Build request
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setOrderInformation(orderInfo);
            request.setPaymentInformation(paymentInfo);
            request.setClientReferenceInformation(clientRef);

            // --- Fire authorization ---
            PtsV2PaymentsPost201Response response = api.createPayment(request);

            String status = response.getStatus();
            String transId = response.getId();

            // Store result in context for downstream participants
            ctx.put("CS_STATUS",   status);
            ctx.put("CS_TRANS_ID", transId);
            ctx.put("CS_RESPONSE", response);

            if ("AUTHORIZED".equals(status) ||
                    "PARTIAL_AUTHORIZED".equals(status)) {

                // Set ISO-8583 response code 00 = approved
                ctx.put("RESPONSE_CODE", "00");
                return PREPARED;
            } else {
                // Set ISO-8583 response code 05 = declined
                ctx.put("RESPONSE_CODE", "05");
                ctx.put("CS_DECLINE_REASON", response.getErrorInformation() != null
                        ? response.getErrorInformation().getReason() : status);
                return PREPARED;
            }

        } catch (ApiException e) {
            ctx.put("RESPONSE_CODE", "96");
            ctx.put("CS_ERROR", e.getResponseBody());
            return PREPARED;
        } catch (Exception e) {
            ctx.put("RESPONSE_CODE", "96");
            ctx.put("CS_ERROR", e.getMessage());
            return PREPARED;
        }
    }

    @Override
    public void commit(long id, Serializable context) {
        Context ctx = (Context) context;
        System.out.println("[COMMIT] Trans ID: " + ctx.get("CS_TRANS_ID"));
    }

    @Override
    public void abort(long id, Serializable context) {
        Context ctx = (Context) context;
        System.out.println("[ABORT] Reason: " + ctx.get("CS_DECLINE_REASON"));
    }
}