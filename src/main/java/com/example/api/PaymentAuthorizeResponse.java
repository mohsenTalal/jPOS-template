package com.example.api;

public class PaymentAuthorizeResponse {

    private String mti;
    private String responseCode;
    private String responseMessage;
    private String stan;
    private String rrn;

    public PaymentAuthorizeResponse() {
    }

    public PaymentAuthorizeResponse(String mti, String responseCode, String responseMessage, String stan, String rrn) {
        this.mti = mti;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.stan = stan;
        this.rrn = rrn;
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
}