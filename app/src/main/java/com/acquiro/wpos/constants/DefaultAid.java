package com.acquiro.wpos.constants;

import java.util.ArrayList;

public class DefaultAid {

    public static ArrayList<AIDTable> createDefaultAID() {
        ArrayList<AIDTable> aidTables = new ArrayList<>();

        AIDTable aidTable = new AIDTable();
        aidTable.setAid("A0000000031010");//
        aidTable.setAppLabel("VISA CREDIT");//
        aidTable.setAppPreferredName("VISA CREDIT");//
        aidTable.setAppPriority((byte)0);//
        aidTable.setTermFloorLimit(0);//
        aidTable.setTACDefault("D84000A800");//
        aidTable.setTACDenial("0010000000");//
        aidTable.setTACOnline("D84004F800");//
        aidTable.setThresholdValue(0);//
        aidTable.setMaxTargetPercentage((byte)0);//
        aidTable.setTargetPercentage((byte)0);//
        aidTable.setAcquirerId("000000123456");//
        aidTable.setPOSEntryMode((byte)0x80);
        aidTable.setMCC("3333");
        aidTable.setMID("12345678");
        aidTable.setAppVersionNumber("008C");//
        aidTable.setTransReferCurrencyCode("0356");//
        aidTable.setTransReferCurrencyExponent((byte)2);//
        aidTable.setDefaultTDOL("0F9F02065F2A029A039C0195059F3704");//
        aidTable.setDefaultDDOL("039F3704");//
        aidTable.setNeedCompleteMatching((byte)0);//
        aidTable.setSupportOnlinePin((byte)1);//
        aidTables.add(aidTable);


        return aidTables;
    }
}
