package com.acquiro.wpos.models;

public class DthObject {
    String dthCode;
    String provider;

    public DthObject(String dthCode, String provider) {
        this.dthCode = dthCode;
        this.provider = provider;
    }

    public String getDthCode() {
        return dthCode;
    }

    public String getProvider() {
        return provider;
    }
}
