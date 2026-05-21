package com.example.cms;

public class PanResult {
    private String pan;
    private String bin;
    private long   sequence;
    private int    checkDigit;
    private String maskedPan;
    private String productCode;
    private int    panLength;

    public String getPan()              { return pan; }
    public void setPan(String v)        { pan = v; }
    public String getBin()              { return bin; }
    public void setBin(String v)        { bin = v; }
    public long getSequence()           { return sequence; }
    public void setSequence(long v)     { sequence = v; }
    public int getCheckDigit()          { return checkDigit; }
    public void setCheckDigit(int v)    { checkDigit = v; }
    public String getMaskedPan()        { return maskedPan; }
    public void setMaskedPan(String v)  { maskedPan = v; }
    public String getProductCode()      { return productCode; }
    public void setProductCode(String v){ productCode = v; }
    public int getPanLength()           { return panLength; }
    public void setPanLength(int v)     { panLength = v; }
}