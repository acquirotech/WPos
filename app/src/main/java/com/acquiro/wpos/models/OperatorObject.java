package com.acquiro.wpos.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OperatorObject {
    private ArrayList<MobileOperatorObject> mobileOperatorArray = new ArrayList<>();
    private ArrayList<CircleCodesObject> circleCodesArray = new ArrayList<>();
    private ArrayList<DthObject> dthArray = new ArrayList<>();


    public OperatorObject(String data){
        try {
            JSONArray operatorsJsonArray = new JSONObject(data).getJSONArray("Operators");
            JSONArray circleCodesJsonArray = new JSONObject(data).getJSONArray("circleCodes");
            JSONArray dthJsonArray = new JSONObject(data).getJSONArray("dth");

            for(int i =0; i<operatorsJsonArray.length();i++){
                JSONObject jsonObject = operatorsJsonArray.getJSONObject(i);
                MobileOperatorObject mobileOperatorObject = new MobileOperatorObject(jsonObject.getString("operatorId"),
                        jsonObject.getString("operatorCode"),
                        jsonObject.getString("operatorName"));
                mobileOperatorArray.add(mobileOperatorObject);
            }
            for(int i =0; i<circleCodesJsonArray.length();i++){
                JSONObject jsonObject = circleCodesJsonArray.getJSONObject(i);
                CircleCodesObject circleCodesObject = new CircleCodesObject(jsonObject.getString("circleId"),
                        jsonObject.getString("circleCode"),
                        jsonObject.getString("circleName"));
                circleCodesArray.add(circleCodesObject);
            }
            for(int i =0; i<dthJsonArray.length();i++){
                JSONObject jsonObject = dthJsonArray.getJSONObject(i);
                DthObject dthObject = new DthObject(jsonObject.getString("dthCode"),
                        jsonObject.getString("provider"));
                dthArray.add(dthObject);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<MobileOperatorObject> getMobileOperatorArray() {
        return mobileOperatorArray;
    }

    public ArrayList<CircleCodesObject> getCircleCodesArray() {
        return circleCodesArray;
    }

    public ArrayList<DthObject> getDthArray() {
        return dthArray;
    }

    public List<String> getMobileOperatorNameList(){
        List<String> strings = new ArrayList<>();
        for(int i=0;i<mobileOperatorArray.size();i++){
            strings.add(mobileOperatorArray.get(i).getOperatorName());
        }
        return strings;
    }
    public String getMobileOperatorId(int position){
        return mobileOperatorArray.get(position).getOperatorId();
    }
    public String getCircleId(int position){
        return circleCodesArray.get(position).getCircleId();
    }

    public String getDthId(int position){
        return dthArray.get(position).getDthCode();
    }

    public List<String> getCircleNameList(){
        List<String> strings = new ArrayList<>();
        for(int i=0;i<circleCodesArray.size();i++){
            strings.add(circleCodesArray.get(i).getCircleName());
        }
        return strings;
    }
    public List<String> getDthNameList(){
        List<String> strings = new ArrayList<>();
        for(int i=0;i<dthArray.size();i++){
            strings.add(dthArray.get(i).getProvider());
        }
        return strings;
    }
}
