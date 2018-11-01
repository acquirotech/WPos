package com.acquiro.wpos.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.acquiro.wpos.R;
import com.acquiro.wpos.activity.RechargeActivity;
import com.acquiro.wpos.activity.SplashActivity;
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
public class MobileFrag extends Fragment {

    private static final String TAG = "Recharge";
    View rootView;
    ProgressDialog progressDialog;
    String mMobileNumber;
    String mAmount;

    EditText etMobileNumber;
    EditText etAmount;

    RadioButton rbPrepaid,rbPostpaid;
    SharedPreferences sharedPreferences;
    OperatorObject operatorObject;

    AppCompatSpinner sOperator;
    AppCompatSpinner sCircle;
    String mSessionId;

    public MobileFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mobile, container, false);
        rbPrepaid = rootView.findViewById(R.id.rbPrepaid);
        rbPostpaid = rootView.findViewById(R.id.rbPostpaid);
        sOperator = rootView.findViewById(R.id.sOperator);
        sCircle = rootView.findViewById(R.id.sCircle);
        sharedPreferences = getActivity().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        operatorObject = new OperatorObject(sharedPreferences.getString(AppConstants.PREF_OPERATORS,""));
        mSessionId = sharedPreferences.getString(AppConstants.PREF_SESSION_ID,"");


        setView();


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);

        rootView.findViewById(R.id.bRecharge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rechargeClicked();
                mMobileNumber = etMobileNumber.getText().toString();
                mAmount = etAmount.getText().toString();
                if (mMobileNumber.length() == 10) {
                    if (mAmount.length() > 0) {
                        showRechargeOptions();
                    } else {
                        Toast.makeText(getActivity(), "Enter Amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Enter Mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        etMobileNumber = rootView.findViewById(R.id.etMobileNumber);
        etAmount = rootView.findViewById(R.id.etAmount);
        return rootView;
    }

    String selectedMobileOperator="";
    String selectedCircle="";
    String selectedRechargeType="TOPUP";
    private void setView() {

        rbPrepaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(true){
                    selectedRechargeType = "TOPUP";
                }
            }
        });
        rbPostpaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedRechargeType = "POSTPAID";
            }
        });

        //Default Value
        selectedMobileOperator = operatorObject.getMobileOperatorId(0);
        selectedCircle = operatorObject.getCircleId(0);

        ArrayAdapter<String> mobileAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, operatorObject.getMobileOperatorNameList());
        mobileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sOperator.setAdapter(mobileAdapter);
        sOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMobileOperator = operatorObject.getMobileOperatorId(i);
                /*String companyId = companies_list.get(i).getCompanyId();
                String companyName = companies_list.get(i).getCompanyName();
                Toast.makeText(MainActivity.this, "Company Name: " + companyName, Toast.LENGTH_SHORT).show();*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> circleAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, operatorObject.getCircleNameList());
        circleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCircle.setAdapter(circleAdapter);
        sCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCircle = operatorObject.getCircleId(i);
                /*String companyId = companies_list.get(i).getCompanyId();
                String companyName = companies_list.get(i).getCompanyName();
                Toast.makeText(MainActivity.this, "Company Name: " + companyName, Toast.LENGTH_SHORT).show();*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




    }

    void showRechargeOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_payment_type, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView tvClose = view.findViewById(R.id.tvClose);

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        (view.findViewById(R.id.tvCash)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                progressDialog.show();
                progressDialog.setMessage("Recharging, Please wait");




                rechargeAPI("CASH","");
               /* new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        showSuccessDialog();
                    }
                }, 2000);*/
            }
        });

        (view.findViewById(R.id.tvCard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                rechargeClicked();
            }
        });
    }

    private void rechargeClicked() {
        //progressDialog.show();
        //Intent intent = new Intent("com.telpo.tps900emvtest.activity.SplashActivity");//telpo
        Intent intent = new Intent("com.acquiro.wpos.SdkActivity"); //wizarpos
        intent.putExtra("amount", mAmount);
        startActivityForResult(intent, 1234);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1234:
                if (resultCode == Activity.RESULT_OK) {
                    progressDialog.setMessage("Recharging, Please wait");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //showSuccessDialog();
                            progressDialog.show();
                            progressDialog.setMessage("Recharging, Please wait");
                            rechargeAPI("CARD",data.getStringExtra("txnId"));

                        }
                    }, 2000);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    showFailedDialog("Transaction Failed");
                }
                break;
        }
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
        tvRechargeText.setText("Recharge Successful for mobile number ".concat(mMobileNumber));
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

    void rechargeAPI(final String payMode, final String txnId) {
        String api = "doRecharge/version2";

        JSONObject json = new JSONObject();
        try {
            json.put("amount",mAmount);
            json.put("subscriberId",mMobileNumber);
            json.put("operator",selectedMobileOperator);
            json.put("circle",selectedCircle);
            json.put("rechargeType",selectedRechargeType);
            json.put("paymentMode",payMode);
            json.put("cardTransactionId",txnId);
            json.put("serviceType","MOBILE");
            json.put("sessionId",mSessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "rechargeAPI: " +json.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConstants.API_URL +api,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        //Toast.makeText(HomeActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.d("rechargeAPI", "onResponse: " + response);
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


}
