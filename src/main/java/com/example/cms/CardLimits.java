package com.example.cms;

public class CardLimits {
    private double dailyPurchaseLimit;
    private double monthlyPurchaseLimit;
    private double atmWithdrawalLimit;
    private double ecommerceLimit;
    private double contactlessLimit;
    private double internationalLimit;
    private double creditLimit;

    // Build limits from product defaults
    public static CardLimits fromProduct(CardProduct product,
                                         double requestedLimit) {
        CardLimits limits = new CardLimits();
        double base = Math.min(requestedLimit, product.getDefaultLimit());
        limits.dailyPurchaseLimit    = base;
        limits.monthlyPurchaseLimit  = base * 5;
        limits.atmWithdrawalLimit    = base * 0.3;
        limits.ecommerceLimit        = base * 0.5;
        limits.contactlessLimit      = 300;   // SAR 300 per Visa rules
        limits.internationalLimit    = base * 0.5;
        limits.creditLimit           = base;
        return limits;
    }

    public double getDailyPurchaseLimit()       { return dailyPurchaseLimit; }
    public double getMonthlyPurchaseLimit()     { return monthlyPurchaseLimit; }
    public double getAtmWithdrawalLimit()       { return atmWithdrawalLimit; }
    public double getEcommerceLimit()           { return ecommerceLimit; }
    public double getContactlessLimit()         { return contactlessLimit; }
    public double getInternationalLimit()       { return internationalLimit; }
    public double getCreditLimit()              { return creditLimit; }
}