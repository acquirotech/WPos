package com.acquiro.wpos.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.activity.TransactionActivity;
import com.acquiro.wpos.constants.AIDTable;
import com.acquiro.wpos.constants.CAPKTable;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.constants.DefaultAid;
import com.acquiro.wpos.constants.DefaultCapks;
import com.acquiro.wpos.jni.MsrInterface;
import com.acquiro.wpos.utils.NumberUtil;
import com.acquiro.wpos.utils.StringUtil;

import java.util.ArrayList;

public class TransactionInitFragment extends Fragment {
    View rootView;
    static String TAG = TransactionInitFragment.class.getSimpleName();


    public TransactionInitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_transaction_init, container, false);
        loadEmvKernel();
        loadCapk();
        loadAid();
        setTermInfo();
        startEmvTransaction();
        return rootView;
    }

    public void loadEmvKernel() {
        byte b = MainActivity.loadEMVKernel();
        Log.d(TAG, "loadEmvKernel: " +b);
        MainActivity.emv_kernel_initialize();
        MainActivity.emv_set_kernel_attr(new byte[]{0x20}, 1);
    }

    public void loadCapk() {
        MainActivity.emv_capkparam_clear();
        ArrayList<CAPKTable> capkTables = DefaultCapks.createDefaultCAPK();
        for (int i = 0; i < capkTables.size(); i++) {
            CAPKTable capkTable = capkTables.get(i);
            byte[] capkInfo = capkTable.getDataBuffer();
            int ret = MainActivity.emv_capkparam_add(capkInfo,capkInfo.length);
            if(ret<0){
                Log.d(TAG, "loadCapk: " +ret +" | " +capkTable.getRID() +" | " +capkTable.getCapki());
            }
        }
        Log.d(TAG, "loadCapk: Completed");
    }

    public void loadAid() {
        MainActivity.emv_aidparam_clear();
        ArrayList<AIDTable> aidTables = DefaultAid.createDefaultAID();
        byte[] aidInfo = null;
        for(int i = 0;i<aidTables.size();i++){
            AIDTable aidTable = aidTables.get(i);
            aidInfo = aidTable.getDataBuffer();
            int ret = MainActivity.emv_aidparam_add(aidInfo, aidInfo.length);
            if(ret<0){
                Log.d(TAG, "loadAid: Failed " +ret);
            }
        }
        Log.d(TAG, "loadAid: Completed");
    }

    public void setTermInfo() {
        byte[] termInfo = new byte[98];
        System.arraycopy(StringUtil.hexString2bytes(Constants.COUNTRY_CODE), 0, termInfo, 0, 2); //Country Code
        System.arraycopy(Constants.TID.getBytes(), 0, termInfo, 2, 8); //TID
        System.arraycopy("123".getBytes(), 0, termInfo, 10, "123".length());  // 8 //IFD
        System.arraycopy(StringUtil.hexString2bytes(Constants.COUNTRY_CODE), 0, termInfo, 18, 2);  //Currency Code
        System.arraycopy(StringUtil.hexString2bytes(Constants.TERMINAL_CAPABILITY), 0, termInfo, 20, 3);
        termInfo[23] = StringUtil.hexString2bytes(Constants.TERMINAL_TYPE)[0];
        termInfo[24] = Constants.CURRENCY_EXPONENT;
        System.arraycopy(StringUtil.hexString2bytes(Constants.ADDITIONAL_TERMINAL_CAPABILITY), 0, termInfo, 25, 5);
        termInfo[30] = (byte) Constants.MERCHANT_NAME.length();
        System.arraycopy(Constants.MERCHANT_NAME.getBytes(), 0, termInfo, 31, termInfo[30]); // 20
        termInfo[51] =Constants.DEFAULT_TTQ;
        termInfo[52] = Constants.STATUS_CHECK_SUPPORT;
        System.arraycopy(NumberUtil.intToBcd(Constants.EC_LIMIT, 6), 0, termInfo, 53, 6);
        System.arraycopy(NumberUtil.intToBcd(Constants.CONTACT_LESS_LIMIT, 6), 0, termInfo, 59, 6);
        System.arraycopy(NumberUtil.intToBcd(Constants.CONTACTLESS_FLOOR_LIMIT, 6), 0, termInfo, 65, 6);
        System.arraycopy(NumberUtil.intToBcd(Constants.CVM_LIMIT, 6), 0, termInfo, 71, 6);

        termInfo[77] = 1;
        termInfo[78] = 1;
        termInfo[79] = 1;
        termInfo[80] = 1;
        System.arraycopy(NumberUtil.intToBcd(999999, 4), 0, termInfo, 81, 4);
        termInfo[85] = 1;

        System.arraycopy(NumberUtil.intToBcd(999999, 6), 0, termInfo, 86, 6);
        System.arraycopy(NumberUtil.intToBcd(999999, 6), 0, termInfo, 92, 6);
        int r = MainActivity.emv_terminal_param_set2(termInfo, 98);
        Log.d(TAG, "setTermInfo: " +r);
    }

    public void startEmvTransaction() {
        MainActivity.emv_trans_initialize();
        int r = -100;
        r = MainActivity.emv_set_kernel_type(Constants.PBOC_KERNAL);
        Log.d(TAG, "emv_set_kernel_type: " +r);

        //Set Transaction Amount
        String amount = "100";
        byte[] amt = new byte[amount.length() + 1];
        System.arraycopy(amount.getBytes(), 0, amt, 0, amount.length());
        r = MainActivity.emv_set_trans_amount(amt);
        Log.d(TAG, "emv_set_trans_amount: " +r);

        //Set Other Amount
        r = MainActivity.emv_set_other_amount(new byte[]{'0', 0x00});
        Log.d(TAG, "emv_set_other_amount: " +r);

        //Set Transaction Type
        r = MainActivity.emv_set_trans_type(Constants.EMV_TRANS_GOODS_SERVICE);
        Log.d(TAG, "emv_set_trans_type: " +r);

        r = MainActivity.emv_set_force_online(Constants.FORCE_ONLINE_PIN);
        Log.d(TAG, "emv_set_force_online: " +r);

        r = MainActivity.open_reader(1);
        Log.d(TAG, "open_reader(1): " +r);
    }





}
