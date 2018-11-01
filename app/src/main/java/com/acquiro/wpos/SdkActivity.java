package com.acquiro.wpos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.acquiro.wpos.activity.HomeActivity;
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

public class SdkActivity extends AppCompatActivity {

    private static final String TAG = "SdkActivity";
    boolean sdkMode = true;
    String mAmount;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk);
        mAmount = getIntent().getStringExtra("amount");
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait");
        if(!mAmount.contains(".")){
            mAmount = mAmount.concat(".00");
        }

        if(AppUtil.isConnected(this)){
            progressDialog.show();
            checkService();
        }else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }


    }

    String REGISTER_URL = AppConstants.API_URL + "transaction/initiate/version1";

    String mTxnId;
    void checkService() {
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
                                mTxnId = responseObject.getJSONObject("result").getJSONObject("txnResponse").getString("TxnId");
                                Intent intent = new Intent(SdkActivity.this, MainActivity.class);
                                intent.putExtra(Constants.ENTERED_AMOUNT, mAmount);
                                intent.putExtra(Constants.TEMP_TXN_ID,mTxnId);
                                intent.putExtra("sdkMode",true);
                                startActivityForResult(intent,1235);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SdkActivity.this, "Server Timeout", Toast.LENGTH_SHORT).show();
                            Intent data1 = new Intent();
                            setResult(RESULT_CANCELED,data1);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                        Toast.makeText(SdkActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        Intent data1 = new Intent();
                        setResult(RESULT_CANCELED,data1);
                        finish();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", sessionId);
                params.put("totalAmount", mAmount);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1235:
                if(resultCode == RESULT_OK){
                    Intent data1 = new Intent();
                    data1.putExtra("txnId",mTxnId);
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
