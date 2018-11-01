package com.acquiro.wpos.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.activity.HomeActivity;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.jni.PinPadCallbackHandler;
import com.acquiro.wpos.jni.PinPadInterface;
import com.acquiro.wpos.utils.AppUtil;
import com.acquiro.wpos.utils.StringUtil;

public class EnterPinTestFragment extends Fragment {

    View rootView;

    String TAG = MainActivity.class.getSimpleName();
    protected char[] stars = "●●●●●●●●●●●●●●●●".toCharArray();
    TextView tvAmount;
    TextView tvEnteredPin;


    public EnterPinTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_enter_pin_test, container, false);
        tvAmount = rootView.findViewById(R.id.tvAmount);
        tvEnteredPin = rootView.findViewById(R.id.tvEnteredPin);
        tvEnteredPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                   onKeyDown(keyCode, event);
                return true;
            }
        });
        tvEnteredPin.requestFocus();
        return rootView;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: "+ keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "onKeyDown: KEYCODE_BACK");
                //onBack();
                break;
            case KeyEvent.KEYCODE_ESCAPE:
                Log.d(TAG, "onKeyDown: KEYCODE_ESCAPE");
                //onCancel();
                break;
            case KeyEvent.KEYCODE_DEL:
                Log.d(TAG, "onKeyDown: KEYCODE_DEL");
                //onDel();
                break;
            case KeyEvent.KEYCODE_ENTER:
                Log.d(TAG, "onKeyDown: KEYCODE_ENTER");
                //onEnter();
                break;
            case 232://'.'
                Log.d(TAG, "onKeyDown: 232 .");
                //onKeyCode('.');
                break;
            default:
                Log.d(TAG, "onKeyDown: " +(char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                //onKeyCode((char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                break;
        }
        return true;
    }
}
