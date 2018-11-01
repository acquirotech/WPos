package com.acquiro.wpos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.acquiro.wpos.activity.TestPinActivity;
import com.acquiro.wpos.activity.TransactionCompleteActivity;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.fragment.EnterPinFragment;
import com.acquiro.wpos.fragment.EnterPinTestFragment;
import com.acquiro.wpos.fragment.TransactionInitFragment;
import com.acquiro.wpos.jni.MsrInterface;
import com.acquiro.wpos.jni.PinPadInterface;
import com.acquiro.wpos.models.TransactionObject;
import com.acquiro.wpos.utils.AppUtil;
import com.acquiro.wpos.utils.ByteUtil;
import com.acquiro.wpos.utils.StringUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public native static byte loadEMVKernel();
    public native static byte exitEMVKernel();
    //MSR Functions
    public native static int msr_open();
    // Card Functions
    public native static int open_reader(int reader);
    public native static void close_reader(int reader);
    public native static int poweron_card();
    public native static int get_card_type();
    public native static int get_card_atr(byte[] atr);
    public native static int transmit_card(byte[] cmd, int cmdLength, byte[] respData, int respDataLength);
    public native static void emv_kernel_initialize();                                                          // 0
    public native static int emv_is_tag_present(int tag);                                                        // 1
    public native static int emv_get_tag_data(int tag, byte[] data, int dataLength);                            // 2
    public native static int emv_get_tag_list_data(int[] tagList, int tagCount, byte[] data, int dataLength);    // 3
    public native static int emv_set_tag_data(int tag, byte[] data, int dataLength);                            // 4
    public native static int emv_preprocess_qpboc();                                                            // 5
    public native static void emv_trans_initialize();                                                           // 6
    public native static int emv_get_version_string(byte[] data, int dataLength);                                // 7
    public native static int emv_set_trans_amount(byte[] amount);                                                // 8  ASC 以分为单位
    public native static int emv_set_other_amount(byte[] amount);                                                // 9
    public native static int emv_set_trans_type(byte transType);                                                //10
    public native static int emv_set_kernel_type(byte kernelType);                                                //11
    public native static int emv_process_next();                                                                //12
    public native static int emv_is_need_advice();                                                                //13
    public native static int emv_is_need_signature();                                                            //14
    public native static int emv_set_force_online(int flag);                                                    //15
    public native static int emv_get_card_record(byte[] data, int dataLength);                                    //16
    public native static int emv_get_candidate_list(byte[] data, int dataLength);                                //17
    public native static int emv_set_candidate_list_result(int index);                                            //18
    public native static int emv_set_id_check_result(int result);                                                //19
    public native static int emv_set_online_pin_entered(int result);                                            //20
    public native static int emv_set_pin_bypass_confirmed(int result);                                            //21
    public native static int emv_set_online_result(int result, byte[] respCode, byte[] issuerRespData, int issuerRespDataLength); // 22
    public native static int emv_aidparam_clear();                                                                //23
    public native static int emv_aidparam_add(byte[] data, int dataLength);                                        //24
    public native static int emv_capkparam_clear();                                                                //25
    public native static int emv_capkparam_add(byte[] data, int dataLength);                                    //26
    public native static int emv_terminal_param_set(byte[] TerminalParam);                                        //27
    public native static int emv_terminal_param_set2(byte[] TerminalParam, int paramLength);
    public native static int emv_exception_file_clear();                                                        //28
    public native static int emv_exception_file_add(byte[] exceptFile);                                            //29
    public native static int emv_revoked_cert_clear();                                                            //30
    public native static int emv_revoked_cert_add(byte[] revokedCert);                                            //31
    public native static int emv_log_file_clear();                                                              //32
    public native static int emv_set_kernel_attr(byte[] data, int dataLength);

    public TransactionObject transactionObject;
    protected static Thread msrThread = null;
    protected static boolean msrThreadActived = false;
    protected static boolean readMSRCard = false;
    protected static boolean msrClosed = true;

    FrameLayout frameLayoutContainer;
    static byte emvStatus;
    static byte emvRetCode;
    static Handler emvHandler;
    FragmentManager fragmentManager;

    static String mAmount = "";

    boolean sdkMode;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayoutContainer = findViewById(R.id.frameLayoutContainer);
        System.loadLibrary("wizarpos_emv_kernel");
        sdkMode = getIntent().getBooleanExtra("sdkMode",false);

        mAmount = getIntent().getStringExtra(Constants.ENTERED_AMOUNT);
        Log.d(TAG, "onCreate: Amount : " +mAmount);

        transactionObject = new TransactionObject();
        Fragment transactionInitFragment = new TransactionInitFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayoutContainer, transactionInitFragment);
        fragmentTransaction.commit();

        msrThread = new MSRThread();
        msrThread.start();


        emvHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                Log.d(TAG, "EMV Handler -> emvStatus : " + emvStatus + " | emvRetCode : " + emvRetCode);
                switch (emvStatus) {
                    case Constants.STATUS_CONTINUE:
                        switch (emvRetCode){
                            case Constants.EMV_CANDIDATE_LIST:
                                Log.d(TAG, "EMV_CANDIDATE_LIST: Select APP from list");
                                break;
                            case Constants.EMV_APP_SELECTED:  //1
                                Toast.makeText(MainActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "EMV_APP_SELECTED");
                                emv_process_next();
                                break;
                            case Constants.EMV_READ_APP_DATA: //2
                                Log.d(TAG, "EMV_READ_APP_DATA: ");
                                byte[] tagData;
                                int tagDataLength = 0;

                                tagData = new byte[100];
                                if (emv_is_tag_present(0x5A) >= 0) {
                                    tagDataLength = emv_get_tag_data(0x5A, tagData, tagData.length);
                                    transactionObject.setCardPan(StringUtil.toString(AppUtil.removeTailF(ByteUtil.bcdToAscii(tagData, 0, tagDataLength))));
                                    Log.d(TAG, "getCardPan: " +transactionObject.getCardPan());
                                }
                                // Track2
                                if (emv_is_tag_present(0x57) >= 0) {
                                    tagDataLength = emv_get_tag_data(0x57, tagData, tagData.length);
                                    transactionObject.setTrack2Data(StringUtil.toString(AppUtil.removeTailF(ByteUtil.bcdToAscii(tagData, 0, tagDataLength))));
                                    //Log.d(TAG, "getTrack2Data: " +transactionObject.getTrack2Data());
                                }
                                // CSN
                                if (emv_is_tag_present(0x5F34) >= 0) {
                                    tagDataLength = emv_get_tag_data(0x5F34, tagData, tagData.length);
                                    transactionObject.setCsn(tagData[0]);
                                    Log.d(TAG, "getCsn: " +transactionObject.getCsn());
                                }
                                // Expiry
                                if (emv_is_tag_present(0x5F24) >= 0) {
                                    tagDataLength = emv_get_tag_data(0x5F24, tagData, tagData.length);
                                    transactionObject.setExpiryDate(StringUtil.toHexString(tagData, 0, 3, false).substring(0, 4));
                                    Log.d(TAG, "getExpiryDate: " +transactionObject.getExpiryDate());
                                }
                                if(emv_is_tag_present(0x5F20) >0){
                                    tagDataLength = emv_get_tag_data(0x5F20, tagData, tagData.length);
                                    transactionObject.setCardHolderName(StringUtil.toString(tagData).substring(0,tagDataLength));
                                    Log.d(TAG, "CardHolderName: " +transactionObject.getCardHolderName());
                                }
                                emv_process_next();
                                break;
                            case Constants.EMV_DATA_AUTH:
                                byte[] TSI = new byte[2];
                                byte[] TVR = new byte[5];
                                byte[] CVM_LIST = new byte[100];
                                emv_get_tag_data(0x9B, TSI, 2); // TSI
                                if(emv_is_tag_present(0x95) >= 0){
                                    Log.d(TAG, "0x95: present");
                                }

                                emv_get_tag_data(0x95, TVR, 5); // TVR
                                emv_get_tag_data(0x8E,CVM_LIST,CVM_LIST.length); //CVM List
                                if ((TSI[0] & (byte) 0x80) == (byte) 0x80
                                        && (TVR[0] & (byte) 0x40) == (byte) 0x00
                                        && (TVR[0] & (byte) 0x08) == (byte) 0x00
                                        && (TVR[0] & (byte) 0x04) == (byte) 0x00
                                        ) {
                                    Log.d(TAG, "EMV_DATA_AUTH: Offline Data Auth Success");
                                }
                                Log.d(TAG, "TSI: " +StringUtil.toHexString(TSI,false));
                                Log.d(TAG, "TVR: " +StringUtil.toHexString(TVR,false));
                                Log.d(TAG, "CVM_LIST: " +StringUtil.toHexString(CVM_LIST,0,CVM_LIST.length,false));
                                transactionObject.setTsi(StringUtil.toHexString(TSI,false));
                                transactionObject.setTvr(StringUtil.toHexString(TVR,false));
                                emv_process_next();
                                break;
                            case Constants.EMV_ONLINE_ENC_PIN:
                                enterOnlinePIN();
                                break;
                        }
                        break;
                    case Constants.STATUS_COMPLETION:
                        break;
                }

            }
        };
    }

    class MSRThread extends Thread {
        public void run() {
            super.run();
            msrThreadActived = true;
            readMSRCard = false;
            if (msrClosed) {
                if (MsrInterface.open() >= 0) {
                    msrClosed = false;
                }
            }
            if (!msrClosed) {
                readMSRCard = true;
                do {
                    int nReturn = -1;
                    nReturn = MsrInterface.poll(500);
//					appState.msrPollResult = nReturn;
                    Log.d(TAG, "MsrInterface.poll, " + nReturn);
                    if (!readMSRCard) {
                        MsrInterface.close();
                        msrClosed = true;
                        Log.d(TAG, "MsrInterface.close");
                    } else if (nReturn >= 0) {
                        read_track1_data();
                        if (read_track2_data()) {
                            read_track3_data();
                            MsrInterface.close();
                            cancelContactCard();
                            readMSRCard = false;
                            msrClosed = true;
                            //TODO Call Sale Here
                            //notifyMSR();
                        } else {
                            MsrInterface.close();
                            msrClosed = true;
                            readMSRCard = false;
                            //notifyMsrReadError();
                            //TODO MSR Error
                        }

                        enterOnlinePIN();
                    }
                } while (readMSRCard);
            } else {
                //notifyMsrOpenError();
            }
            Log.d(TAG, "MSRThread.exit");
            msrThreadActived = false;
        }
    }

    protected void cancelContactCard() {
        Log.d(TAG, "cancelContactCard: ");
            close_reader(1);
    }
    protected static void cancelMSRThread() {
        if (readMSRCard) {
            readMSRCard = false;
        }
    }
    protected void read_track3_data() {
        Log.d(TAG, "read_track3_data: ");
        int trackDatalength;
        byte[] byteArry = new byte[255];
        trackDatalength = MsrInterface.getTrackData(2, byteArry, byteArry.length);  // nTrackIndex: 0-Track1; 1-track2; 2-track3

        String strDebug = "";
        for (int i = 0; i < trackDatalength; i++)
            strDebug += String.format("%02X ", byteArry[i]);
        Log.d(TAG, "track3 Data: " + strDebug);
        if (trackDatalength > 0) {
            //appState.trans.setTrack3Data(byteArry, 0, trackDatalength);
        }
    }

    protected boolean read_track2_data() {
        Log.d(TAG, "read_track2_data: ");
        int trackDatalength;
        byte[] byteArry = new byte[255];
        trackDatalength = MsrInterface.getTrackData(1, byteArry, byteArry.length);  // nTrackIndex: 0-Track1; 1-track2; 2-track3
        String strDebug = "";
        for (int i = 0; i < trackDatalength; i++)
            strDebug += String.format("%02X ", byteArry[i]);
        Log.d(TAG, "track2 Data: " + strDebug);
        if (trackDatalength > 0) {
            if (trackDatalength > 37
                    || trackDatalength < 21
                    ) {
                return false;
            }

            int panStart = -1;
            int panEnd = -1;
            for (int i = 0; i < trackDatalength; i++) {
                if (byteArry[i] >= (byte) '0' && byteArry[i] <= (byte) '9') {
                    if (panStart == -1) {
                        panStart = i;
                    }
                } else if (byteArry[i] == (byte) '=') {
                    /* Field separator */
                    panEnd = i;
                    break;
                } else {
                    panStart = -1;
                    panEnd = -1;
                    break;
                }
            }
            if (panEnd == -1 || panStart == -1) {
                return false;
            }
            Log.d(TAG, "read_track2_data PAN: " +new String(byteArry, panStart, panEnd - panStart));
            Log.d(TAG, "read_track2_data Expiry:" +new String(byteArry, panEnd + 1, 4));
            Log.d(TAG, "read_track2_data Service Code: "+new String(byteArry, panEnd + 5, 3));
            //appState.trans.setTrack2Data(byteArry, 0, trackDatalength);
            //appState.trans.setCardEntryMode(SWIPE_ENTRY);
            Log.d(TAG, "read_track2_data: " +new String(byteArry));

            transactionObject.setCardPan(new String(byteArry, panStart, panEnd - panStart));
            transactionObject.setExpiryDate(new String(byteArry, panEnd + 1, 4));
            transactionObject.setServiceCode(new String(byteArry, panEnd + 5, 3));
            transactionObject.setTrack2Data(new String(byteArry).substring(0,trackDatalength));
            return true;
        }
        return false;
    }

    protected void read_track1_data() {
        Log.d(TAG, "read_track1_data: ");
        int trackDatalength;
        byte[] byteArry = new byte[255];
        trackDatalength = MsrInterface.getTrackData(0, byteArry, byteArry.length);  // nTrackIndex: 0-Track1; 1-track2; 2-track3
        String strDebug = "";
        for (int i = 0; i < trackDatalength; i++)
            strDebug += String.format("%02X ", byteArry[i]);
        Log.d(TAG, "track1 Data: " + strDebug);
        Log.d(TAG, "read_track1_data: " +new String(byteArry));
        if (trackDatalength > 0) {
            try {
                String track1Data = new String(byteArry, "UTF-8");
                String trackArray[] = track1Data.split("\\^");
                final String cardHolderName =trackArray[1];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, cardHolderName, Toast.LENGTH_SHORT).show();
                    }
                });
                transactionObject.setCardHolderName(cardHolderName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //appState.trans.setTrack1Data(byteArry, 0, trackDatalength);

        }
    }


    private void enterOnlinePIN() {
        /*Fragment enterPinFragment = new EnterPinFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayoutContainer, enterPinFragment);
        fragmentTransaction.commit();*/

        Intent intent = new Intent(MainActivity.this, TestPinActivity.class);
        intent.putExtra(Constants.ENTERED_AMOUNT,mAmount);
        intent.putExtra(Constants.TEMP_TXN_ID,getIntent().getStringExtra(Constants.TEMP_TXN_ID));
        intent.putExtra(Constants.TEMP_TXN_OBJECT,transactionObject);
        intent.putExtra("sdkMode",sdkMode);

        if(sdkMode){
            startActivityForResult(intent,1236);
        }else{
            startActivity(intent);

        }
    }

    public void processTransaction(){

        String cardPan = transactionObject.getCardPan();
        Log.d(TAG, "processTransaction: " +cardPan);
        if (PinPadInterface.open() < 0) {
            //notifyPinError();
            Log.d(TAG, "PinPadInterface.open() error");
            return;
        }
        PinPadInterface.selectKey(1, /*Constants.DEFAULT_KEY_INDEX*/2, 0, Constants.DOUBLE_KEY);

        byte encPan[]= new byte[100];
        int r = PinPadInterface.encryptWithMode("12345678".getBytes(),encPan,0,null,0);
        //int r = PinPadInterface.encrypt("12345678".getBytes(),8,encPan);
        Log.d(TAG, "processTransaction: " +r);
        Log.d(TAG, "processTransaction: " +StringUtil.toHexString(encPan,false));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1236:
                if(resultCode == RESULT_OK){
                    Intent data1 = new Intent();
                    setResult(RESULT_OK,data1);
                    finish();
                }else if(resultCode == RESULT_CANCELED){
                    Intent data1 = new Intent();
                    setResult(RESULT_CANCELED,data1);
                    finish();
                }
                break;
        }
    }

    public static void cardEventOccured(int eventType) {
        Log.d(TAG, "cardEventOccured: " + eventType);
        cancelMSRThread();
        int cardType;
        Message msg = new Message();
        if (eventType == Constants.SMART_CARD_EVENT_INSERT_CARD) {
            cardType = MainActivity.get_card_type();
            Log.d(TAG, "cardType = " + cardType);

            if (cardType == Constants.CARD_CONTACT) {
                Log.d(TAG, "cardEventOccured: Card Inserted");

                emv_trans_initialize();
                emv_set_kernel_type(Constants.PBOC_KERNAL);
                //Set Transaction Amount
                byte[] amt = new byte[mAmount.length() + 1];
                System.arraycopy(mAmount.getBytes(), 0, amt, 0, mAmount.length());
                emv_set_trans_amount(amt);

                setEMVData();
                int r = emv_process_next();
                Log.d(TAG, "emv_process_next: " + r);
            } else if (cardType == Constants.CARD_CONTACTLESS) {
                Log.d(TAG, "cardEventOccured: Card Taped");
            } else {
                cardType = -1;
            }
        }
    }


    public static void emvProcessCallback(byte[] data) {
        emvStatus = data[0];
        emvRetCode = data[1];

        Log.d(TAG, "emvStatus: " + data[0]);
        Log.d(TAG, "emvRetCode: " + data[1]);

        Message msg = new Message();
        emvHandler.sendMessage(msg);

    }

    static Calendar mCalendar;
    static int currentYear;
    static int currentMonth;
    static int currentDay;
    static int currentHour;
    static int currentMinute;
    static int currentSecond;

    public static void setEMVData() {
        //TODO Set DateTime
        String transDate = "20180507"; // YYYYMMDD
        long time = System.currentTimeMillis();
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        currentYear = mCalendar.get(Calendar.YEAR);
        currentMonth = mCalendar.get(Calendar.MONTH) + 1;
        currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        currentHour = mCalendar.get(Calendar.HOUR);
        if (mCalendar.get(Calendar.AM_PM) == Calendar.PM) {
            currentHour += 12;
        }
        currentMinute = mCalendar.get(Calendar.MINUTE);
        currentSecond = mCalendar.get(Calendar.SECOND);

        String transTime = StringUtil.fillZero(Integer.toString(currentHour), 2)
                + StringUtil.fillZero(Integer.toString(currentMinute), 2)
                + StringUtil.fillZero(Integer.toString(currentSecond), 2);

        int r = emv_set_tag_data(0x9A, StringUtil.hexString2bytes(transDate.substring(2)), 3);
        Log.d(TAG, "setEMVData: " + r);
        r = emv_set_tag_data(0x9F21, StringUtil.hexString2bytes(transTime), 3);
        Log.d(TAG, "setEMVData: " + r);
        r = emv_set_tag_data(0x9F41, StringUtil.hexString2bytes(StringUtil.fillZero("1", 8)), 4);
        Log.d(TAG, "setEMVData: " + r);
        r = emv_set_trans_type(Constants.EMV_TRANS_GOODS_SERVICE);
        Log.d(TAG, "setEMVData: " + r);
    }
}
