package com.acquiro.wpos.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.R;
import com.acquiro.wpos.constants.AppConstants;
import com.acquiro.wpos.fingerprint.OpVerifyAll;
import com.acquiro.wpos.test.IsoTest;
import com.acquiro.wpos.utils.AppUtil;
import com.acquiro.wpos.utils.StringUtil;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.upek.android.ptapi.PtConnectionAdvancedI;
import com.upek.android.ptapi.PtConstants;
import com.upek.android.ptapi.PtException;
import com.upek.android.ptapi.PtGlobal;
import com.upek.android.ptapi.struct.PtInfo;
import com.upek.android.ptapi.struct.PtSessionCfgV5;
import com.upek.android.ptapi.usb.PtUsbHost;

import org.jpos.iso.ISOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    ProgressBar pbMain;
    ImageView ivFingerprint;
    TextView tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        pbMain = findViewById(R.id.pbMain);
        ivFingerprint = findViewById(R.id.ivFingerprint);
        tvSkip = findViewById(R.id.tvSkip);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        if (AppUtil.isConnected(this)) {
            authenticateApi();
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
        //initFingerprint();
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        },3000);*/
       //Log.d("dsfsdfsdfsdf", "onCreate: " +StringUtil.encryptPassword("Test"));

        try {
            String iso = "0210703800000AEFBE80EFBE800019EFBF8BEFBEA4EFBEAB6D09EFBEBD17EFBE8AEFBF98EFBFA869EFBF9FEFBE825768EFBEBBEFBEBA1C100000000000000005550002551219250904383237373131303030323535303531333133313331330840";
            IsoTest.generateIso(this,iso);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ISOException e) {
            e.printStackTrace();
        }

    }


    void logIso(){
    }

    void authenticateApi() {
        final String api = "tpos/authenticate/version2";

        JSONObject json = new JSONObject();
        try {
            json.put("loginId","1111111112");
            json.put("password",/*StringUtil.encryptPassword(*/"Acquiro123"/*)*/);
            json.put("deviceSerialNo",AppConstants.DEVICE_ID);
            json.put("deviceName","Wizarpos Q1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "authenticateApi: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL + api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        try {
                            Log.d("getOperatorsApi", "onResponse: " + response);
                            int statusCode = response.getInt("statusCode");
                            if(statusCode==0){
                                JSONObject body = new JSONObject(response.getString("body"));
                                String sessionID = body.getString("sessionId");
                                SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREF_NAME,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(AppConstants.PREF_SESSION_ID,sessionID);
                                editor.apply();
                                getOperatorsApi(sessionID);
                            }else{
                                Toast.makeText(SplashActivity.this, response.getString("statusMessage"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getOperatorsApi", "onErrorResponse: " + error.toString());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(jsonObjectRequest);
    }


    void getOperatorsApi(String sessionId) {
        String api = "getOperators/version2";
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId",sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getOperatorsApi: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL + api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject  response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        try {
                            //JSONObject responseObject  = new JSONObject(response);
                            int statusCode = response.getInt("statusCode");
                            if(statusCode==0){
                                JSONObject body = new JSONObject(response.getString("body"));

                                SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(AppConstants.PREF_OPERATORS, body.toString());
                                editor.apply();

                                pbMain.setVisibility(View.GONE);
                                ivFingerprint.setVisibility(View.VISIBLE);
                                tvSkip.setVisibility(View.VISIBLE);
                                //Toast.makeText(SplashActivity.this, "Use Fingerprint to Unlock", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                //initFingerprint();
                                Log.d("getOperatorsApi", "onResponse: " + response);
                            }else{
                                Toast.makeText(SplashActivity.this, response.getString("statusMessage"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getOperatorsApi", "onErrorResponse: " + error.toString());

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);

        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }


    //-------------Fingerprint------------------------

    private PtGlobal mPtGlobal = null;
    private PtConnectionAdvancedI mConn = null;
    private PtInfo mSensorInfo = null;
    private Thread mRunningOp = null;
    private final Object mCond = new Object();
    public static final int miRunningOnRealHardware = 1;
    private String msNvmPath = null;


    public void matchFingerprint() {
        synchronized (mCond) {
            if (mRunningOp == null) {
                mRunningOp = new OpVerifyAll(mConn) {
                    @Override
                    protected void onDisplayMessage(String message) {
                        //Toast.makeText(FPActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.d("fp", "onDisplayMessage: " + message);
                        if (message.equals("No match found.") || message.equals("Right Thumb finger matched")) {
                            Intent intent = new Intent(SplashActivity.this, SelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    protected void onFinished() {
                        synchronized (mCond) {
                            mRunningOp = null;
                            mCond.notifyAll();  //notify onDestroy that operation has finished
                        }
                    }
                };
                mRunningOp.start();
            }
        }
    }

    private void initFingerprint() {
        File aDir = getDir("tcstore", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        if (aDir != null) {
            msNvmPath = aDir.getAbsolutePath();
        }
        if (initializePtapi()) {
            openPtapiSession();
            matchFingerprint();
        } else {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean initializePtapi() {
        // Load PTAPI library
        //Context aContext = getApplicationContext();
        mPtGlobal = new PtGlobal(this);

        try {
            // Initialize PTAPI interface
            // Note: If PtBridge development technology is in place and a network
            // connection cannot be established, this call hangs forever.
            mPtGlobal.initialize();
            return true;
        } catch (java.lang.UnsatisfiedLinkError ule) {
            // Library wasn't loaded properly during PtGlobal object construction
            //dislayMessage("libjniPtapi.so not loaded");
            mPtGlobal = null;
            return false;

        } catch (PtException e) {
            //dislayMessage(e.getMessage());
            return false;
        }
    }

    private void openPtapiSession() {
        PtException openException = null;

        try {
            // Try to open session
            openPtapiSessionInternal("");

            // Device successfully opened
            return;
        } catch (PtException e) {
            // Remember error and try remaining devices
            openException = e;
        }


        // No device has been opened
        if (openException == null) {
            //dislayMessage("No device found");
        } else {
            //dislayMessage("Error during device opening - " + openException.getMessage());
        }
    }

    private void openPtapiSessionInternal(String dsn) throws PtException {
        // Try to open device with given DSN string
        //Context aContext = getApplicationContext();
        try {
            PtUsbHost.PtUsbCheckDevice(this, 0);
        } catch (PtException e) {
            throw e;
        }

        mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);

        try {
            // Verify that emulated NVM is initialized and accessible
            mSensorInfo = mConn.info();
        } catch (PtException e) {

            if ((e.getCode() == PtException.PT_STATUS_EMULATED_NVM_INVALID_FORMAT)
                    || (e.getCode() == PtException.PT_STATUS_NVM_INVALID_FORMAT)
                    || (e.getCode() == PtException.PT_STATUS_NVM_ERROR)) {
                if (miRunningOnRealHardware != 0) {
                    //try add storage configuration and reopen the device
                    dsn += ",nvmprefix=" + msNvmPath + '/';
                    // Reopen session
                    mConn.close();
                    mConn = null;

                    mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);
                    try {
                        // Verify that emulated NVM is initialized and accessible
                        mSensorInfo = mConn.info();
                        configureOpenedDevice();
                        return;
                    } catch (PtException e2) {
                        //ignore errors and continue
                    }
                }


                // We have found the device, but it seems to be either opened for the first time
                // or its emulated NVM was corrupted.
                // Perform the manufacturing procedure.
                //To properly initialize it, we have to:
                // 1. Format its emulated NVM storage
                // 2. Calibrate the sensor

                // Format internal NVM
                mConn.formatInternalNVM(0, null, null);

                // Reopen session
                mConn.close();
                mConn = null;

                mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);

                // Verify that emulated NVM is initialized and accessible
                mSensorInfo = mConn.info();
                //check if sensor is calibrated
                if ((mSensorInfo.sensorType & PtConstants.PT_SENSORBIT_CALIBRATED) == 0) {
                    // No, so calibrate it
                    mConn.calibrate(PtConstants.PT_CALIB_TYPE_TURBOMODE_CALIBRATION);
                    // Update mSensorInfo
                    mSensorInfo = mConn.info();
                }

                // Device successfully opened
            } else {
                throw e;
            }
        }

        configureOpenedDevice();
    }

    private void configureOpenedDevice() throws PtException {
        PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG);
        sessionCfg.sensorSecurityMode = PtConstants.PT_SSM_DISABLED;
        sessionCfg.callbackLevel |= PtConstants.CALLBACKSBIT_NO_REPEATING_MSG;
        mConn.setSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG, sessionCfg);
    }

    @Override
    protected void onDestroy() {
        // Cancel running operation
        synchronized (mCond) {
            while (mRunningOp != null) {
                mRunningOp.interrupt();
                try {
                    mCond.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // Close PTAPI session
        closeSession();

        // Terminate PTAPI library
        terminatePtapi();

        super.onDestroy();
    }

    private void closeSession() {
        if (mConn != null) {
            try {
                mConn.close();
            } catch (PtException e) {
                // Ignore errors
            }
            mConn = null;
        }
    }

    private void terminatePtapi() {
        try {
            if (mPtGlobal != null) {
                mPtGlobal.terminate();
            }
        } catch (PtException e) {
            //ignore errors
        }
        mPtGlobal = null;
    }

}
