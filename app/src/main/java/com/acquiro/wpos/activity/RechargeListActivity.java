package com.acquiro.wpos.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.MainActivity;
import com.acquiro.wpos.R;
import com.acquiro.wpos.adapter.RechargeAdapter;
import com.acquiro.wpos.constants.AppConstants;
import com.acquiro.wpos.models.RechargeListObject;
import com.acquiro.wpos.ui.ClickListener;
import com.acquiro.wpos.ui.RecyclerTouchListener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RechargeListActivity extends AppCompatActivity {

    private static final String TAG = RechargeListActivity.class.getSimpleName();
    private ArrayList<RechargeListObject>  rechargeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RechargeAdapter mAdapter;
    String mSessionId;
    int selectedPosition;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_list);
        getSupportActionBar().setTitle("Recharge List");
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        mSessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        setRecycler();
        rechargeLIstApi();
    }

    private void setRecycler() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new RechargeAdapter(rechargeList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                progressDialog.show();
                selectedPosition = position;
                RechargeListObject rechargeListObject = rechargeList.get(position);
                getStatusApi(rechargeListObject.getRechargeId(),rechargeListObject.getSubscriberId());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mAdapter.notifyDataSetChanged();

    }

    void showRechargeDetails(int selectedPosition,String status){
        RechargeListObject rechargeListObject = rechargeList.get(selectedPosition);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_recharge_details,null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ((TextView)view.findViewById(R.id.tvAmount)).setText("₹ " +rechargeListObject.getRechargeAmount());
        ((TextView)view.findViewById(R.id.tvSubId)).setText("Mobile No. :" +rechargeListObject.getSubscriberId());
        ((TextView)view.findViewById(R.id.tvStatus)).setText("Status :" +status);
        //((TextView)view.findViewById(R.id.tvOperator)).setText("");
        //((TextView)view.findViewById(R.id.tvCircle)).setText("");
        ((TextView)view.findViewById(R.id.tvPayMode)).setText("Payment Mode :" +rechargeListObject.getTransactionType());
        (view.findViewById(R.id.bClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    void getStatusApi(String rechargeId, String subId) {
        String api = "getRechargeStatus/version2";
        JSONObject json = new JSONObject();
        try {
            json.put("rechargeId",rechargeId);
            json.put("sessionId",mSessionId);
            json.put("subscriberId",subId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getStatusApi: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL + api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d("accountBalanceAPI", "onResponse: " + response);
                        try {
                            //JSONObject responseObject  = new JSONObject(response);

                            int statusCode = response.getInt("statusCode");
                            if(statusCode==0){
                                JSONObject body = new JSONObject(response.getString("body"));
                                String rechargeMessage = body.getString("rechargeMessage");
                                progressDialog.dismiss();
                                showRechargeDetails(selectedPosition,rechargeMessage);

                            }else{
                                Toast.makeText(RechargeListActivity.this, response.getString("statusMessage"), Toast.LENGTH_SHORT).show();

                            }
                            //JSONObject jsonObject = new JSONObject(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("accountBalanceAPI", "onErrorResponse: " + error.toString());
                        Toast.makeText(RechargeListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(RechargeListActivity.this);
        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }

    void rechargeLIstApi() {
        String api = "getRechargeList/version2";
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId",mSessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "rechargeLIstApi: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL + api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d("accountBalanceAPI", "onResponse: " + response);
                        try {
                            //JSONObject responseObject  = new JSONObject(response);

                            int statusCode = response.getInt("statusCode");
                            if(statusCode==0){
                                JSONObject body = new JSONObject(response.getString("body"));
                                JSONArray rechargeListArray = body.getJSONArray("rechargeList");
                                Log.d("fdgdfgdfg", "onResponse: " +rechargeListArray.toString());
                                for(int i=0;i<rechargeListArray.length();i++){
                                    JSONObject jsonObject1 = rechargeListArray.getJSONObject(i);

                                    RechargeListObject rechargeListObject = new RechargeListObject(jsonObject1.getString("subscriberId"),
                                            jsonObject1.getString("rechargeId"),
                                            jsonObject1.getString("rechargeStatusCode"),
                                            jsonObject1.getString("rechargeStatusMessage"),
                                            jsonObject1.getString("rechargeAmount"),
                                            jsonObject1.getString("operator"),
                                            jsonObject1.getString("circle"),
                                            jsonObject1.getString("rechargeType"),
                                            jsonObject1.getString("serviceType"),
                                            /*jsonObject1.getString("txnType")*/"CASH",
                                            jsonObject1.getString("cardTransactionId"));
                                    rechargeList.add(rechargeListObject);
                                }
                                mAdapter.notifyDataSetChanged();

                            }else{
                                Toast.makeText(RechargeListActivity.this, response.getString("statusMessage"), Toast.LENGTH_SHORT).show();

                            }
                            //JSONObject jsonObject = new JSONObject(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("accountBalanceAPI", "onErrorResponse: " + error.toString());
                        Toast.makeText(RechargeListActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });/* {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sessionId",mSessionId);
                return params;
            }
        };*/
        RequestQueue requestQueue = Volley.newRequestQueue(RechargeListActivity.this);
        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }

}
