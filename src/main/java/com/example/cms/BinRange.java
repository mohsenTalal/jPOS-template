package com.example.cms;

public class BinRange {
    private String scheme;        // VISA
    private String bin;           // 8-digit BIN (e.g. 45569300)
    private int    panLength;     // 16
    private String cardType;      // CREDIT, DEBIT, PREPAID
    private String cardCategory;  // PLATINUM, GOLD, etc.
    private String productCode;   // VC003
    private String country;       // SA
    private String currency;      // SAR
    private long   nextSequence;  // current sequence counter
    private long   minSequence;   // 1000000
    private long   maxSequence;   // 9999999
    private String status;        // ACTIVE

    // Getters and setters
    public String getScheme()           { return scheme; }
    public void setScheme(String v)     { scheme = v; }
    public String getBin()              { return bin; }
    public void setBin(String v)        { bin = v; }
    public int getPanLength()           { return panLength; }
    public void setPanLength(int v)     { panLength = v; }
    public String getCardType()         { return cardType; }
    public void setCardType(String v)   { cardType = v; }
    public String getCardCategory()     { return cardCategory; }
    public void setCardCategory(String v){ cardCategory = v; }
    public String getProductCode()      { return productCode; }
    public void setProductCode(String v){ productCode = v; }
    public String getCountry()          { return country; }
    public void setCountry(String v)    { country = v; }
    public String getCurrency()         { return currency; }
    public void setCurrency(String v)   { currency = v; }
    public long getNextSequence()       { return nextSequence; }
    public void setNextSequence(long v) { nextSequence = v; }
    public long getMinSequence()        { return minSequence; }
    public void setMinSequence(long v)  { minSequence = v; }
    public long getMaxSequence()        { return maxSequence; }
    public void setMaxSequence(long v)  { maxSequence = v; }
    public String getStatus()           { return status; }
    public void setStatus(String v)     { status = v; }
}