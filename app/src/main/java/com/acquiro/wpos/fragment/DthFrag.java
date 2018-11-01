package com.acquiro.wpos.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.R;
import com.acquiro.wpos.activity.RechargeActivity;
import com.acquiro.wpos.constants.AppConstants;
import com.acquiro.wpos.models.OperatorObject;
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
public class DthFrag extends Fragment {

    private static final String TAG = "Recharge";
    View rootView;
    AppCompatSpinner sOperator;
    SharedPreferences sharedPreferences;
    OperatorObject operatorObject;
    String selectedOperator;
    EditText etAmount;
    EditText etSubscriberId;
    String mSubId;
    String mAmount;
    String mSessionId;
    ProgressDialog progressDialog;


    public DthFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_dth, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        sOperator = rootView.findViewById(R.id.sOperator);
        etAmount = rootView.findViewById(R.id.etAmount);
        etSubscriberId = rootView.findViewById(R.id.etSubscriberId);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");

        sharedPreferences = getActivity().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        operatorObject = new OperatorObject(sharedPreferences.getString(AppConstants.PREF_OPERATORS,""));
        mSessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");

        selectedOperator = operatorObject.getDthId(0);
        ArrayAdapter<String> dthAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, operatorObject.getDthNameList());
        dthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sOperator.setAdapter(dthAdapter);
        sOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedOperator = operatorObject.getDthId(i);
                /*String companyId = companies_list.get(i).getCompanyId();
                String companyName = companies_list.get(i).getCompanyName();
                Toast.makeText(MainActivity.this, "Company Name: " + companyName, Toast.LENGTH_SHORT).show();*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        rootView.findViewById(R.id.bRecharge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubId = etSubscriberId.getText().toString();
                mAmount = etAmount.getText().toString();
                if(mSubId.length()>0){
                    if(mAmount.length()>0){
                        progressDialog.show();
                        rechargeAPI();
                    }else{
                        Toast.makeText(getActivity(), "Enter Amount", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "Enter Sub ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void rechargeAPI() {
        String api = "doRecharge/version2";
        JSONObject json = new JSONObject();
        try {
            json.put("amount",mAmount);
            json.put("subscriberId",/*mSubId*/"3003984028");
            json.put("operator",selectedOperator);
            json.put("circle","");
            json.put("rechargeType","TOPUP");
            json.put("paymentMode","CASH");
            json.put("cardTransactionId","");
            json.put("serviceType","DTH");
            json.put("sessionId",mSessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "rechargeAPI: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL +api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d("rechargeAPI", "onResponse: " + response);
                        progressDialog.dismiss();
                        try {
                            //JSONObject responseObject  = new JSONObject(response);
                            int statusCode = response.getInt("statusCode");
                            if(statusCode==0){
                                JSONObject body = new JSONObject(response.getString("body"));
                                String rechargeStatus = body.getString("rechargeStatus");
                                String rechargeMessage = body.getString("rechargeMessage");
                                if(rechargeStatus.equals("-1")){
                                    showPendingDialog();
                                }else if(rechargeStatus.equals("0")){
                                    showSuccessDialog();
                                }else{
                                    showFailedDialog(rechargeMessage);
                                }
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
                        progressDialog.dismiss();
                        Log.d("rechargeAPI", "onErrorResponse: " + error.toString());

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //adding the string request to request queue
        requestQueue.add(jsonObjectRequest);
    }

    void showPendingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_pending, null);
        builder.setView(view);
        TextView tvClose = view.findViewById(R.id.tvClose);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                Intent intent = new Intent(getActivity(), RechargeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }
    private void showFailedDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_failed, null);
        builder.setView(view);
        TextView tvClose = view.findViewById(R.id.tvClose);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), RechargeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_success, null);
        builder.setView(view);
        TextView tvClose = view.findViewById(R.id.tvClose);
        TextView tvRechargeText = view.findViewById(R.id.tvRechargeText);
        tvRechargeText.setText("Recharge Successful for mobile number ".concat(mSubId));
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                Intent intent = new Intent(getActivity(), RechargeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }

}
