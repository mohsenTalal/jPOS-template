package com.example.origination;

public class CardOriginationRequest {

    // Required fields from your document
    private String customerId;        // Customer ID
    private String accountNumber;     // Account number
    private String cardType;          // CREDIT, DEBIT, PREPAID
    private String cardCategory;      // CLASSIC, GOLD, PLATINUM, SIGNATURE, INFINITE, VIRTUAL
    private String cardForm;          // PHYSICAL, VIRTUAL
    private String deliveryAddress;   // For physical cards
    private double requestedLimit;    // Requested limit
    private String currency;          // SAR

    // Getters and setters
    public String getCustomerId()           { return customerId; }
    public void setCustomerId(String v)     { customerId = v; }
    public String getAccountNumber()        { return accountNumber; }
    public void setAccountNumber(String v)  { accountNumber = v; }
    public String getCardType()             { return cardType; }
    public void setCardType(String v)       { cardType = v; }
    public String getCardCategory()         { return cardCategory; }
    public void setCardCategory(String v)   { cardCategory = v; }
    public String getCardForm()             { return cardForm; }
    public void setCardForm(String v)       { cardForm = v; }
    public String getDeliveryAddress()      { return deliveryAddress; }
    public void setDeliveryAddress(String v){ deliveryAddress = v; }
    public double getRequestedLimit()       { return requestedLimit; }
    public void setRequestedLimit(double v) { requestedLimit = v; }
    public String getCurrency()             { return currency; }
    public void setCurrency(String v)       { currency = v; }
}