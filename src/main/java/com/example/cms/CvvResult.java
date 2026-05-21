package com.example.cms;

public class CvvResult {
    private String cvv;    // Magnetic stripe
    private String icvv;   // Chip
    private String cvv2;   // E-commerce / CNP

    public String getCvv()          { return cvv; }
    public void setCvv(String v)    { cvv = v; }
    public String getIcvv()         { return icvv; }
    public void setIcvv(String v)   { icvv = v; }
    public String getCvv2()         { return cvv2; }
    public void setCvv2(String v)   { cvv2 = v; }
}