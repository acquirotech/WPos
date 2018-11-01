package com.acquiro.wpos.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.AppConstants;
import com.acquiro.wpos.constants.Constants;
import com.acquiro.wpos.models.TransactionObject;
import com.acquiro.wpos.printer.PrinterException;
import com.acquiro.wpos.printer.PrinterHelper;
import com.acquiro.wpos.utils.AppUtil;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransactionCompleteActivity extends AppCompatActivity {

    private static final String TAG = TransactionCompleteActivity.class.getSimpleName();
    ProgressBar pbMain;
    ImageView ivSuccess;
    TextView tvMessage;
    String mAmount;
    private String REGISTER_URL =  "http://18.218.17.153:8080/Communicator/transaction/complete/version1";
    TransactionObject transactionObject;
    boolean sdkMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_complete_activity);
        mAmount = getIntent().getStringExtra(Constants.ENTERED_AMOUNT);
        sdkMode = getIntent().getBooleanExtra("sdkMode",false);
        transactionObject = (TransactionObject) getIntent().getSerializableExtra(Constants.TEMP_TXN_OBJECT);

        pbMain = findViewById(R.id.pbMain);
        ivSuccess = findViewById(R.id.ivSuccess);
        tvMessage = findViewById(R.id.tvMessage);

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pbMain.setVisibility(View.GONE);
                ivSuccess.setVisibility(View.VISIBLE);
                playAlertSound();
                tvMessage.setText("Approved");
                //showHomeActivity();
            }
        },4000);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkService();
            }
        },2000);
    }

    void checkService() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: " + response);

                        pbMain.setVisibility(View.GONE);
                        ivSuccess.setVisibility(View.VISIBLE);
                        playAlertSound();
                        tvMessage.setText("Approved");
                        if(sdkMode){
                            printTestReceipt();
                            Intent data1 = new Intent();
                            setResult(RESULT_OK,data1);
                            finish();
                        }else{
                            printTestReceipt();
                            showHomeActivity();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                        Toast.makeText(TransactionCompleteActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        Intent data1 = new Intent();
                        setResult(RESULT_CANCELED,data1);
                        finish();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sharedPreferences  = getSharedPreferences(AppConstants.PREF_NAME,MODE_PRIVATE);
                String sessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");
                params.put("sessionId", sessionId);
                params.put("transactionId",getIntent().getStringExtra(Constants.TEMP_TXN_ID) );
                params.put("statusDescription", "Approved");
                params.put("statusCode", "0");
                params.put("stan", "23432");
                params.put("walletId", "1122112211");
                params.put("cardPanNo", transactionObject.getMaskedPan());
                params.put("cardHolderName",transactionObject.getCardHolderName());
                params.put("cardType", "VISA");
                params.put("rrNo", getRandomNumber());
                params.put("authCode", getRandomNumber());
                params.put("batchNo", "24234");
                params.put("pointUsed", "0");
                if(sdkMode){
                    params.put("actualAmount", mAmount);
                }else{
                    params.put("actualAmount", AppUtil.formatAmount(Long.valueOf(getIntent().getStringExtra(Constants.ENTERED_AMOUNT))));
                }
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    String getRandomNumber(){
        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(900000);
        return String.valueOf(n);
    }


    void playAlertSound(){
        MediaPlayer m = new MediaPlayer();
        try{
            AssetFileDescriptor descriptor = TransactionCompleteActivity.this.getAssets().openFd("alert_success.mp3");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength() );
            descriptor.close();
            m.prepare();
            m.start();
        } catch(Exception e){
            // handle error here..
        }
    }

    void showHomeActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(TransactionCompleteActivity.this,HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        },2000);

    }

    public void printReceipt(View view) {
        printTestReceipt();
    }

    void printTestReceipt(){
        try {
            int ret = PrinterHelper.getInstance().printTestReceipt(this);
            if(ret==0){
                Toast.makeText(this, "Printing Completed", Toast.LENGTH_SHORT).show();
            }else if(ret==-1){
                Toast.makeText(this, "Out Of Paper", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Return : " +ret, Toast.LENGTH_SHORT).show();
            }
        } catch (PrinterException e) {
            Log.d(TAG, "printReceipt: " +e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
