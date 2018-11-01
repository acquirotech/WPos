package com.acquiro.wpos.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.Constants;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = TransactionActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        startTransaction();
    }

    private void startTransaction() {
        int r = open_reader(1);
        Log.d(TAG, "open_reader: " +r);

    }
}
