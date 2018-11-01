package com.acquiro.wpos.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.utils.AppUtil;

public class TestPinActivity extends AppCompatActivity {

    private static final String TAG = TestPinActivity.class.getSimpleName();
    TextView tvEnteredPin;
    StringBuilder pinHint = new StringBuilder();
    static String mAmount = "";
    TextView tvAmount;
    boolean sdkMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pin);
        sdkMode = getIntent().getBooleanExtra("sdkMode",false);
        tvEnteredPin =  findViewById(R.id.tvEnteredPin);
        tvAmount =  findViewById(R.id.tvAmount);
        mAmount = getIntent().getStringExtra(Constants.ENTERED_AMOUNT);
        if(sdkMode){
            tvAmount.setText("Amount : ₹" +mAmount);
        }else{
            tvAmount.setText("Amount : ₹" +AppUtil.formatAmount(Long.valueOf(mAmount)));
        }

        tvEnteredPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    TestPinActivity.this.onKeyDown(keyCode, event);
                return true;
            }
        });
        tvEnteredPin.requestFocus();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: "+ keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "onKeyDown: KEYCODE_BACK");
                //onBack();
                break;
            case KeyEvent.KEYCODE_ESCAPE:
                Log.d(TAG, "onKeyDown: KEYCODE_ESCAPE");
                onCancel();
                break;
            case KeyEvent.KEYCODE_DEL:
                Log.d(TAG, "onKeyDown: KEYCODE_DEL");
                onDel();
                break;
            case KeyEvent.KEYCODE_ENTER:
                Log.d(TAG, "onKeyDown: KEYCODE_ENTER");
                onEnter();
                break;
            case 232://'.'
                Log.d(TAG, "onKeyDown: 232 .");
                onKeyCode('.');
                break;
            default:
                Log.d(TAG, "onKeyDown: " +(char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                onKeyCode((char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                break;
        }
        return true;
    }
    protected void onKeyCode(char key) {
        if (key != '.') {
            int keyCode = key - '0';
            pinHint.append("*");
            setText();
        }
    }
    protected void onDel() {
        if(pinHint.length()>0){
            pinHint.deleteCharAt(pinHint.length()-1);
            setText();
        }

    }

    protected void onEnter() {
        Intent intent = new Intent(TestPinActivity.this,TransactionCompleteActivity.class);
        intent.putExtra(Constants.TEMP_TXN_ID,getIntent().getStringExtra(Constants.TEMP_TXN_ID));
        intent.putExtra(Constants.ENTERED_AMOUNT,getIntent().getStringExtra(Constants.ENTERED_AMOUNT));
        intent.putExtra(Constants.TEMP_TXN_OBJECT,getIntent().getSerializableExtra(Constants.TEMP_TXN_OBJECT));
        intent.putExtra("sdkMode",sdkMode);
        if(sdkMode){
            startActivityForResult(intent,1237);
        }else {
            startActivity(intent);
        }
        //Toast.makeText(this, "Transaction Sending", Toast.LENGTH_SHORT).show();

    }

    protected void onCancel() {
        Intent intent = new Intent(TestPinActivity.this,HomeActivity.class);
        if(sdkMode){
            Intent data1 = new Intent();
            setResult(RESULT_CANCELED,data1);
            finish();
        }else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    void setText(){
        tvEnteredPin.setText(pinHint.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1237:
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
   
}
