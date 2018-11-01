package com.acquiro.wpos.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.jni.PinPadCallbackHandler;
import com.acquiro.wpos.jni.PinPadInterface;
import com.acquiro.wpos.utils.AppUtil;
import com.acquiro.wpos.utils.StringUtil;

public class EnterPinFragment extends Fragment implements PinPadCallbackHandler {

    View rootView;
    private Thread mReadPINThread;
    boolean pinpadOpened = false;
    String TAG = MainActivity.class.getSimpleName();
    Integer enteredAmount = 100;
    private final int PINPAD_CANCEL  = -65792;
    private final int PINPAD_TIMEOUT = -65538;

    protected char[] stars = "●●●●●●●●●●●●●●●●".toCharArray();
    public static final int PIN_AMOUNT_SHOW  = 0x10000;
    public static final int PIN_KEY_CALLBACK = 0x10001;
    private Handler commHandler = createCommHandler();
    TextView tvAmount;
    TextView tvEnteredPin;

    public EnterPinFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_enter_pin, container, false);
        tvAmount = rootView.findViewById(R.id.tvAmount);
        tvEnteredPin = rootView.findViewById(R.id.tvEnteredPin);
        mReadPINThread = new ReadPINThread();
        mReadPINThread.start();
        return rootView;
    }

    @Override
    public void processCallback(byte[] data) {
        Log.i("processCallback", "" + data);
        if(data != null)
            commHandler.obtainMessage(PIN_KEY_CALLBACK, data[0], data[1]).sendToTarget();

    }

    @SuppressLint("HandlerLeak")
    protected Handler createCommHandler()
    {	// 无 Pinpad时跳过. DuanCS@[20141001]
        return new Handler()
        {
            public void handleMessage(Message msg)
            { /* 这里是处理信息的方法 */
                switch (msg.what)
                {
                    case PIN_AMOUNT_SHOW:	// 其值已通过onFlush显示. DuanCS@[20150907]
//					setTextById(R.id.amount, msg.obj.toString());
                        //textPin.setText(msg.obj.toString());	// 这一行也不会执行, 因为 Pinpad.showText() 不会触发回调... DuanCS@[20150912]
                        tvAmount.setText(msg.obj.toString());
                        break;
                    case PIN_KEY_CALLBACK:
                        tvEnteredPin.setText(stars, 0, msg.arg1 & 0x0F);
                        //textPin.setText(stars, 0, msg.arg1 & 0x0F);
                        break;
                }
            }
        };
    }


    class ReadPINThread extends Thread {
        public void run() {
            MainActivity mainActivity = (MainActivity) getActivity();
            byte[] pinBlock = new byte[8];
            byte[] zeroPAN = new byte[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

            // masterKey is new byte[]{'1','1','1','1','1','1','1','1' }
            //Q1上不支持单倍长PINKEY
            byte[] defaultPINKey = new byte[]{'2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'};

            int ret = -1;
            if (!pinpadOpened) {
                if (PinPadInterface.open() < 0) {
                    //notifyPinError();
                    Log.d(TAG, "PinPadInterface.open() error");
                    return;
                }
                pinpadOpened = true;

                PinPadInterface.setupCallbackHandler(EnterPinFragment.this);
            }

            ret = PinPadInterface.updateUserKey(Constants.DEFAULT_KEY_INDEX,
                    0,
                    defaultPINKey,
                    defaultPINKey.length);
            if (ret < 0) {
                Log.d(TAG, "Pinpad open Error");
                //notifyPinError();
                PinPadInterface.close();
                pinpadOpened = false;
                return;
            }
            //Q1上不支持单倍长PINKEY
            PinPadInterface.selectKey(2, Constants.DEFAULT_KEY_INDEX, 0, Constants.DOUBLE_KEY);
//    		PinPadInterface.setKey(2, appState.terminalConfig.getKeyIndex(), 0, appState.terminalConfig.getKeyAlgorithm());
            if (enteredAmount > 0) {
                byte[] text = (AppUtil.formatAmount(enteredAmount)).getBytes();
                //PinPadInterface.setText(0, text, text.length, 0);
            }
            ret = PinPadInterface.calculatePINBlock(zeroPAN, zeroPAN.length, pinBlock, 60000, 0);
            if (ret < 0) {
                if (ret == PINPAD_CANCEL) {
                    Log.d(TAG, "run: PINPAD_CANCEL");
                    //notifyPinCancel();
                } else if (ret == PINPAD_TIMEOUT) {
                    Log.d(TAG, "run: PINPAD_TIMEOUT");
                    //notifyPinTimeout();
                } else {
                    Log.d(TAG, "run: Pin Error");
                    //notifyPinError();
                }
                PinPadInterface.close();
                pinpadOpened = false;
                return;
            }
            if (ret == 0) {
                Log.d(TAG, "setPinEntryMode: CANNOT_PIN : 1" );
                //appState.trans.setPinEntryMode(CANNOT_PIN);
            } else {
                mainActivity.transactionObject.setPinblock(StringUtil.toHexString(pinBlock,false));
                Log.d(TAG, "PinBlock : " +StringUtil.toHexString(pinBlock,false));
                //appState.trans.setPinBlock(pinBlock);
                //appState.trans.setPinEntryMode(CAN_PIN);
            }
            Log.d(TAG, "Pin Success");
            //notifyPinSuccess();

            PinPadInterface.close();
            pinpadOpened = false;
            mainActivity.processTransaction();
        }
    }
}
