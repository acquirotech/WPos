package com.acquiro.wpos.models;

public class CircleCodesObject {
    String circleId;
    String circleCode;
    String circleName;

    public CircleCodesObject(String circleId, String circleCode, String circleName) {
        this.circleId = circleId;
        this.circleCode = circleCode;
        this.circleName = circleName;
    }

    public String getCircleId() {
        return circleId;
    }

    public String getCircleCode() {
        return circleCode;
    }

    public String getCircleName() {
        return circleName;
    }
}
