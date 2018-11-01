package com.acquiro.wpos.models;

public class MobileOperatorObject {
    String operatorId;
    String operatorCode;
    String operatorName;

    public MobileOperatorObject(String operatorId, String operatorCode, String operatorName) {
        this.operatorId = operatorId;
        this.operatorCode = operatorCode;
        this.operatorName = operatorName;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public String getOperatorName() {
        return operatorName;
    }
    @Override
    public String toString() {

        return operatorName;
    }
}
