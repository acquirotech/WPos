package com.acquiro.wpos.constants;

public class Constants {
    public static String COUNTRY_CODE = "0356";
    public static String TID = "99990002";
    public static String MID = "205910000010003";
    public static String TERMINAL_CAPABILITY = "E0F0C8";
    public static String TERMINAL_TYPE = "22";
    public static byte CURRENCY_EXPONENT = 2;
    public static String ADDITIONAL_TERMINAL_CAPABILITY = "F000F0A001";
    public static String MERCHANT_NAME = "Test Merchant 1";
    public static byte DEFAULT_TTQ = 0x36;
    public static byte    STATUS_CHECK_SUPPORT = 0;
    public static int EC_LIMIT = 0;
    public static int CONTACT_LESS_LIMIT = 0;
    public static int CONTACTLESS_FLOOR_LIMIT = 0;
    public static int CVM_LIMIT = 0;
    public static byte FORCE_ONLINE_PIN = 0;
    public static final int    DEFAULT_KEY_INDEX = 1;


    public static byte PBOC_KERNAL   = 1;

    // Key Type
    public static final int SINGLE_KEY = 0;
    public static final int DOUBLE_KEY = 1;

    // EMV STATUS
    public static final byte STATUS_ERROR    		= 0;
    public static final byte STATUS_CONTINUE    	= 1;
    public static final byte STATUS_COMPLETION 	= 2;

    // EMV Return Code
    public static final byte EMV_START                    = 0;  // EMV Transaction Started
    public static final byte EMV_CANDIDATE_LIST           = 1;  //
    public static final byte EMV_APP_SELECTED             = 2;  // Application Select Completed
    public static final byte EMV_READ_APP_DATA            = 3;  // Read Application Data Completed
    public static final byte EMV_DATA_AUTH                = 4;  // Data Authentication Completed
    public static final byte EMV_OFFLINE_PIN              = 5;
    public static final byte EMV_ONLINE_ENC_PIN           = 6;  // notify Application prompt Caldholder enter Online PIN
    public static final byte EMV_PIN_BYPASS_CONFIRM       = 7;  // notify Application confirm to Accepted PIN Bypass or not
    public static final byte EMV_PROCESS_ONLINE           = 8;  // notify Application to Process Online


    // EMV TRANS
    public static byte EMV_TRANS_GOODS_SERVICE = 0x00;
    public static byte EMV_TRANS_CASH          = 0x01;
    public static byte EMV_TRANS_PRE_AUTH      = 0x03;
    public static byte EMV_TRANS_INQUIRY       = 0x04;
    public static byte EMV_TRANS_TRANSFER      = 0x05;
    public static byte EMV_TRANS_PAYMENT       = 0x06;
    public static byte EMV_TRANS_ADMIN         = 0x07;
    public static byte EMV_TRANS_CASHBACK      = 0x09;
    public static byte EMV_TRANS_CARD_RECORD   = 0x0A;
    public static byte EMV_TRANS_EC_BALANCE    = 0x0B;
    public static byte EMV_TRANS_LOAD_RECORD   = 0x0C;

    public static int SMART_CARD_EVENT_INSERT_CARD = 0;
    public static int SMART_CARD_EVENT_REMOVE_CARD = 1;

    // IC Card Type
    public static int CARD_CONTACT     = 1;
    public static int CARD_CONTACTLESS = 2;



    //Intent
    public static String ENTERED_AMOUNT = "enteredAmount";
    public static String TEMP_TXN_ID = "txnId";
    public static String TEMP_TXN_OBJECT = "txnObject";


}
