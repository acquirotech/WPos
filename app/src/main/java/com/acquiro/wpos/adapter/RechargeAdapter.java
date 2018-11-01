package com.acquiro.wpos.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acquiro.wpos.R;
import com.acquiro.wpos.models.RechargeListObject;

import java.util.ArrayList;

public class RechargeAdapter extends RecyclerView.Adapter<RechargeAdapter.MyViewHolder> {
    private ArrayList<RechargeListObject> rechargeListObject;

    public class MyViewHolder extends RecyclerView.ViewHolder {
       public TextView tvAmount, tvSubId, tvRechargeStatus,tvRechargeId,tvOperator;

        public MyViewHolder(View view) {
            super(view);
            tvAmount = view.findViewById(R.id.tvAmount);
            tvSubId = view.findViewById(R.id.tvSubId);
            tvRechargeStatus =  view.findViewById(R.id.tvRechargeStatus);
            tvRechargeId =  view.findViewById(R.id.tvRechargeId);
            tvOperator =  view.findViewById(R.id.tvOperator);
        }
    }


    public RechargeAdapter(ArrayList<RechargeListObject> rechargeListObject) {
        this.rechargeListObject = rechargeListObject;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_recharge_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RechargeListObject rechargeList = rechargeListObject.get(position);
        holder.tvAmount.setText("Rs." +rechargeList.getRechargeAmount());
        holder.tvSubId.setText(rechargeList.getSubscriberId());
        holder.tvRechargeStatus.setText("Status: " +rechargeList.getRechargeStatusMessage());
        holder.tvRechargeId.setText("RechargeId: " +rechargeList.getRechargeId());
        //holder.tvOperator.setText(rechargeList.getOperator());
    }

    @Override
    public int getItemCount() {
        return rechargeListObject.size();
    }
}
