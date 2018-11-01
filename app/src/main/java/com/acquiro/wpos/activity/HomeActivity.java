package com.acquiro.wpos.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.AppConstants;
import com.acquiro.wpos.constants.Constants;
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

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btn00, btnBack, btnEnter;
    String TAG = HomeActivity.class.getSimpleName();
    TextView tvAmount;
    private int amount = 0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        tvAmount = findViewById(R.id.tvAmount);
        tvAmount.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    HomeActivity.this.onKeyDown(keyCode, event);
                return true;
            }
        });
        tvAmount.requestFocus();
        setListener();

        //txnInitiateApi();
    }



    private void setListener() {
        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn00 = findViewById(R.id.btn00);
        btnBack = findViewById(R.id.btnBack);
        btnEnter = findViewById(R.id.btnEnter);

        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btn00.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnEnter.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "onKeyDown: KEYCODE_BACK");
                finish();
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
                //onKeyCode('.');
                break;
            default:
                Log.d(TAG, "onKeyDown: " + (char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                onKeyCode((char) ('0' + (keyCode - KeyEvent.KEYCODE_0)));
                break;
        }
        return true;
    }

    protected void onKeyCode(char key) {
        if (key != '.') {
            int keyCode = key - '0';
            setTextAmount(keyCode);
        }
    }

    protected void onDel() {
        amount = amount / 10;
        tvAmount.setText("₹" + AppUtil.formatAmount(amount));
    }

    protected void onEnter() {
        if (amount > 0 && amount <= 999999999) {
            Log.d(TAG, "Entered Amount " + amount);
            //Toast.makeText(this, "Rs." +AppUtil.formatAmount(amount), Toast.LENGTH_SHORT).show();

            /*Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra(Constants.ENTERED_AMOUNT,String.valueOf(amount));
            startActivity(intent);*/

            if(AppUtil.isConnected(this)){
                progressDialog.show();
                txnInitiateApi();
            }else{
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Amount should be greater than 1", Toast.LENGTH_SHORT).show();
        }
    }

    String REGISTER_URL = AppConstants.API_URL +"transaction/initiate/version1";

    void txnInitiateApi() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREF_NAME,MODE_PRIVATE);
        final String sessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: " + response);
                        progressDialog.dismiss();
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            int status = responseObject.getInt("status");
                            if (status == 0) {
                                String txnId = responseObject.getJSONObject("result").getJSONObject("txnResponse").getString("TxnId");
                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                intent.putExtra(Constants.ENTERED_AMOUNT, String.valueOf(amount));
                                intent.putExtra(Constants.TEMP_TXN_ID,txnId);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                        Toast.makeText(HomeActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", sessionId);
                params.put("totalAmount", AppUtil.formatAmount(amount));
                params.put("latitude", "28.5263998");
                params.put("logitude", "77.2232597");
                params.put("imeiNo", "1234567890");
                params.put("deviceId", AppConstants.DEVICE_ID);
                params.put("cardTxnType", "CARD");
                params.put("ipAddress", "192.68.1.0");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }


    protected void onCancel() {

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.getTag());
        switch (v.getId()) {
            case R.id.btn0:
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
                setTextAmount(Integer.valueOf((String) v.getTag()));
                break;
            case R.id.btn00:
                //amount = 0;
                setTextAmount(-1);
                break;
            case R.id.btnBack:
                onDel();
                break;
            case R.id.btnEnter:
                onEnter();
                break;
        }
    }

    private void setTextAmount(int digital) {
        if (digital == -1) {
            amount = 0;
            tvAmount.setText("₹0.00");
        } else {
            if (amount < 100000000) {
                amount = amount * 10 + digital;
                tvAmount.setText("₹".concat(AppUtil.formatAmount(amount)));
            }
        }

    }
}
