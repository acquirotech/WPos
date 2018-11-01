package com.acquiro.wpos.models;

import java.io.Serializable;

public class TransactionObject implements Serializable {
    private String cardPan;
    private String track2Data;
    private String expiryDate;
    private String tsi;
    private String tvr;
    private String pinblock;
    private String cardHolderName;
    private String serviceCode;
    private byte csn;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardPan() {
        return cardPan;
    }
    public String getMaskedPan(){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(cardPan.substring(0,6));
        int xNum = cardPan.length() - 10;
        for(int i =0;i<xNum;i++){
            stringBuilder.append("X");
        }
        stringBuilder.append(cardPan.substring(cardPan.length()-4, cardPan.length()));
        return stringBuilder.toString();
    }

    public void setCardPan(String cardPan) {
        this.cardPan = cardPan;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public byte getCsn() {
        return csn;
    }

    public void setCsn(byte csn) {
        this.csn = csn;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getPinblock() {
        return pinblock;
    }

    public void setPinblock(String pinblock) {
        this.pinblock = pinblock;
    }
}
