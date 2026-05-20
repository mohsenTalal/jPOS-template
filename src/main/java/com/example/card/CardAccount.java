package com.example.card;

public class CardAccount {
    private String cardId;
    private String customerId;
    private String pan;           // Real PAN (encrypted in production)
    private String maskedPan;     // e.g. 411111******1111
    private String expiryMonth;
    private String expiryYear;
    private String cardholderName;
    private String status;        // ACTIVE, INACTIVE, BLOCKED
    private String token;         // VTS token (replaces PAN)
    private String tokenRequestorId;
    private String createdAt;

    // Getters and setters
    public String getCardId()             { return cardId; }
    public void setCardId(String v)       { cardId = v; }
    public String getCustomerId()         { return customerId; }
    public void setCustomerId(String v)   { customerId = v; }
    public String getPan()                { return pan; }
    public void setPan(String v)          { pan = v; }
    public String getMaskedPan()          { return maskedPan; }
    public void setMaskedPan(String v)    { maskedPan = v; }
    public String getExpiryMonth()        { return expiryMonth; }
    public void setExpiryMonth(String v)  { expiryMonth = v; }
    public String getExpiryYear()         { return expiryYear; }
    public void setExpiryYear(String v)   { expiryYear = v; }
    public String getCardholderName()     { return cardholderName; }
    public void setCardholderName(String v){ cardholderName = v; }
    public String getStatus()             { return status; }
    public void setStatus(String v)       { status = v; }
    public String getToken()              { return token; }
    public void setToken(String v)        { token = v; }
    public String getCreatedAt()          { return createdAt; }
    public void setCreatedAt(String v)    { createdAt = v; }
}