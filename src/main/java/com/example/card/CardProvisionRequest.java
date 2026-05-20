package com.example.card;

public class CardProvisionRequest {
    private String customerId;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardholderName;
    private String billingAddress;
    private String billingCity;
    private String postalCode;
    private String country;
    private String email;

    // Getters and setters
    public String getCustomerId()          { return customerId; }
    public void setCustomerId(String v)    { customerId = v; }
    public String getCardNumber()          { return cardNumber; }
    public void setCardNumber(String v)    { cardNumber = v; }
    public String getExpiryMonth()         { return expiryMonth; }
    public void setExpiryMonth(String v)   { expiryMonth = v; }
    public String getExpiryYear()          { return expiryYear; }
    public void setExpiryYear(String v)    { expiryYear = v; }
    public String getCvv()                 { return cvv; }
    public void setCvv(String v)           { cvv = v; }
    public String getCardholderName()      { return cardholderName; }
    public void setCardholderName(String v){ cardholderName = v; }
    public String getBillingAddress()      { return billingAddress; }
    public void setBillingAddress(String v){ billingAddress = v; }
    public String getBillingCity()         { return billingCity; }
    public void setBillingCity(String v)   { billingCity = v; }
    public String getPostalCode()          { return postalCode; }
    public void setPostalCode(String v)    { postalCode = v; }
    public String getCountry()             { return country; }
    public void setCountry(String v)       { country = v; }
    public String getEmail()               { return email; }
    public void setEmail(String v)         { email = v; }
}