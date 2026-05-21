package com.example.cms;

public class CardProduct {
    private String productCode;
    private String binRange;
    private double defaultLimit;
    private int validityYears;

    public CardProduct(String productCode, String binRange,
                       double defaultLimit, int validityYears) {
        this.productCode   = productCode;
        this.binRange      = binRange;
        this.defaultLimit  = defaultLimit;
        this.validityYears = validityYears;
    }

    public String getProductCode()    { return productCode; }
    public String getBinRange()       { return binRange; }
    public double getDefaultLimit()   { return defaultLimit; }
    public int getValidityYears()     { return validityYears; }
}