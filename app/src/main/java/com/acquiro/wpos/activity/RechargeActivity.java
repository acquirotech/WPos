package com.acquiro.wpos.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.acquiro.wpos.R;
import com.acquiro.wpos.fragment.AccountFrag;
import com.acquiro.wpos.fragment.DthFrag;
import com.acquiro.wpos.fragment.MobileFrag;

public class RechargeActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            addFrag(item.getItemId());
            return true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        getSupportActionBar().hide();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        addFrag(R.id.navigation_mobile);
    }
    void addFrag(int itemId){
        Fragment fragment = null;
        switch (itemId){
            case R.id.navigation_mobile:
                fragment = new MobileFrag();
                break;
            case R.id.navigation_dish:
                fragment = new DthFrag();
                break;
            case R.id.navigation_account:
                fragment = new AccountFrag();
                break;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragFrame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}
