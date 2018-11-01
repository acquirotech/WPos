package com.acquiro.wpos.models;

public class RechargeListObject {
    private String subscriberId;
    private String rechargeId;
    private String rechargeStatusCode;
    private String rechargeStatusMessage;
    private String rechargeAmount;
    private String operator;
    private String circle;
    private String rechargeType;
    private String serviceType;
    private String transactionType;
    private String cardTransactionId;

    public RechargeListObject(String subscriberId,
                              String rechargeId,
                              String rechargeStatusCode,
                              String rechargeStatusMessage,
                              String rechargeAmount,
                              String operator,
                              String circle,
                              String rechargeType,
                              String serviceType,
                              String transactionType,
                              String cardTransactionId) {
        this.subscriberId = subscriberId;
        this.rechargeId = rechargeId;
        this.rechargeStatusCode = rechargeStatusCode;
        this.rechargeStatusMessage = rechargeStatusMessage;
        this.rechargeAmount = rechargeAmount;
        this.operator = operator;
        this.circle = circle;
        this.rechargeType = rechargeType;
        this.serviceType = serviceType;
        this.transactionType = transactionType;
        this.cardTransactionId = cardTransactionId;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public String getRechargeId() {
        return rechargeId;
    }

    public String getRechargeStatusCode() {
        return rechargeStatusCode;
    }

    public String getRechargeStatusMessage() {
        return rechargeStatusMessage;
    }

    public String getRechargeAmount() {
        return rechargeAmount;
    }

    public String getOperator() {
        return operator;
    }

    public String getCircle() {
        return circle;
    }

    public String getRechargeType() {
        return rechargeType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getCardTransactionId() {
        return cardTransactionId;
    }
}
