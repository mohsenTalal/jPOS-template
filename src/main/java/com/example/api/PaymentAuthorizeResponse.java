package com.example.api;

public class PaymentAuthorizeResponse {

    private String mti;
    private String responseCode;
    private String responseMessage;
    private String stan;
    private String rrn;
    private String authorizationCode;
    private String transactionId;

    public PaymentAuthorizeResponse() {
    }

    public PaymentAuthorizeResponse(
            String mti,
            String responseCode,
            String responseMessage,
            String stan,
            String rrn,
            String authorizationCode,
            String transactionId
    ) {
        this.mti = mti;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.stan = stan;
        this.rrn = rrn;
        this.authorizationCode = authorizationCode;
        this.transactionId = transactionId;
    }

    public String getMti() {
        return mti;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getStan() {
        return stan;
    }

    public String getRrn() {
        return rrn;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getTransactionId() {
        return transactionId;
    }
}