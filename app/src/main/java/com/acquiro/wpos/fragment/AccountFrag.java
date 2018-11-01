package com.acquiro.wpos.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.R;
import com.acquiro.wpos.activity.RechargeListActivity;
import com.acquiro.wpos.activity.SplashActivity;
import com.acquiro.wpos.constants.AppConstants;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFrag extends Fragment {

    private static final String TAG = "AccountFrag";
    TextView tvAccountBalance;
    View rootView;
    RelativeLayout rlRechargeList;
    String mSessionId;

    public AccountFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        tvAccountBalance =rootView.findViewById(R.id.tvAccountBalance);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        mSessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");

        rootView.findViewById(R.id.rlRechargeList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RechargeListActivity.class);
                startActivity(intent);
            }
        });

        accountBalanceAPI();
        return rootView;
    }

    void accountBalanceAPI() {
        String api = "getMerchantBalance/version2";
        JSONObject json = new JSONObject();
        try {
            json.put("sessionId",mSessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "accountBalanceAPI: " +json.toString());
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
                                String availableBalance = body.getString("availableBalance");
                                tvAccountBalance.setText("Rs. " +availableBalance);
                            }else{
                                Toast.makeText(getActivity(), response.getString("statusMessage"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("accountBalanceAPI", "onErrorResponse: " + error.toString());
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }


}
