package com.vvtech.andonphase2.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.login.UserLoginResponse;
import com.vvtech.andonphase2.apiresponses.logout.LogOutResponse;
import com.vvtech.andonphase2.services.MQTTService1;
import com.vvtech.andonphase2.utils.AndonUtils;
import com.vvtech.andonphase2.utils.OnResponseListener;
import com.vvtech.andonphase2.utils.WebServices;

import static com.vvtech.andonphase2.utils.Utilities.saveLogInPreference;
import static com.vvtech.andonphase2.utils.Utilities.showSnackBar;

public class ErrorDetailsActivity extends AppCompatActivity {

    String errorId,errorName,lineName,peoples,repairTime,station,action,date;

    TextView mErrorId,mErrorName,mLineName,mPeoplesInvolved,mRepairTime,mStation,mAction,mDate;

    @Override
    protected void onStart() {
        super.onStart();
        setValuesToViews();
    }

    private void setValuesToViews() {

        mErrorId.setText(errorId);
        mErrorName.setText(errorName);
        mLineName.setText(lineName);
        mPeoplesInvolved.setText("1");
        mRepairTime.setText(repairTime+" minutes");
        mStation.setText(station);
        mAction.setText(action);
        mDate.setText(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame); //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_error_details, contentFrameLayout);*/
        setContentView(R.layout.activity_error_details);
        getIntentData();
        initializeViews();
    }

    private void initializeViews() {

        mErrorId=findViewById(R.id.vT_aed_error_id);
        mErrorName=findViewById(R.id.vT_aed_error_name);
        mLineName=findViewById(R.id.vT_aed_line_name);
        mPeoplesInvolved=findViewById(R.id.vT_aed_peoples);
        mRepairTime=findViewById(R.id.vT_aed_repair_time);
        mStation=findViewById(R.id.vT_aed_station);
        mAction=findViewById(R.id.vT_aed_action);
        mDate=findViewById(R.id.vT_aed_date);
    }

    private void getIntentData() {

       Intent intent=getIntent();

        errorId=intent.getStringExtra("ERROR_ID");
        errorName=intent.getStringExtra("ERROR_NAME");
        lineName=intent.getStringExtra("LINE_NAME");
        peoples=intent.getStringExtra("PEOPLES");
        repairTime=intent.getStringExtra("REPAIR_TIME");
        station=intent.getStringExtra("STATION");
        action=intent.getStringExtra("ACTION");
        date= intent.getStringExtra("DATE");

    }
}
