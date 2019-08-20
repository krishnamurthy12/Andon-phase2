package com.vvtech.andonphase2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.adapters.ErrorListAdapter;
import com.vvtech.andonphase2.apiresponses.allerrors.AllErrorsResponse;
import com.vvtech.andonphase2.apiresponses.allerrors.ErrorList;
import com.vvtech.andonphase2.apiresponses.allnotifications.AllNotificationsResponse;
import com.vvtech.andonphase2.apiresponses.allusers.AllAvailableUsersResponse;
import com.vvtech.andonphase2.apiresponses.login.UserLoginResponse;
import com.vvtech.andonphase2.apiresponses.logout.LogOutResponse;
import com.vvtech.andonphase2.services.MQTTService1;
import com.vvtech.andonphase2.utils.AndonUtils;
import com.vvtech.andonphase2.utils.OnResponseListener;
import com.vvtech.andonphase2.utils.WebServices;

import java.util.ArrayList;
import java.util.List;

import static com.vvtech.andonphase2.utils.Utilities.saveLogInPreference;
import static com.vvtech.andonphase2.utils.Utilities.showSnackBar;

public class ErrorsListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener,
        OnResponseListener,ErrorListAdapter.ErrorListInterface {

    String errorId;

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    LinearLayoutManager layoutManager;

    List<ErrorList> mList=new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        if(errorId!=null)
        {
            callGetErrorListAPI(errorId);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiveIntentData();
        //setContentView(R.layout.activity_errors_list);

        //This is the FrameLayout area within your activity_base.xml
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame); //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_errors_list, contentFrameLayout);

        mSwipeRefreshLayout=findViewById(R.id.vS_el_swiperefresh_layout);
        mRecyclerView=findViewById(R.id.vR_errorslist);

        layoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        ErrorListAdapter errorListAdapter=new ErrorListAdapter(this,mList);
        mRecyclerView.setAdapter(errorListAdapter);

        mLogOutLayout.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.nav_logout_layout:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                logoutCurrentUser();
                break;
        }
    }

    public void logoutCurrentUser() {
        //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();

        if (AndonUtils.isConnectedToInternet(getApplicationContext())) {
            WebServices<UserLoginResponse> webServices = new WebServices<UserLoginResponse>(this);
            webServices.logOut(BASE_URL, WebServices.ApiType.logOut,  employeeID);

        } else {
            //Snackbar.make(mSignup,R.string.err_msg_nointernet, Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, getResources().getString(R.string.err_msg_nointernet) + "", Toast.LENGTH_SHORT).show();
        }
    }


    private void receiveIntentData() {

        Intent intent = getIntent();
        errorId=intent.getStringExtra("ERROR_ID");
        Log.d("errorid",errorId);
    }



    private void callGetErrorListAPI(String errorId)
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            WebServices<AllErrorsResponse> webServices = new WebServices<AllErrorsResponse>(this);
            webServices.getAllErrors(BASE_URL, WebServices.ApiType.errorlist,errorId);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }

    }

    public boolean onNavigationItemSelected(MenuItem item) {
        NavigationView nv= (NavigationView) findViewById(R.id.nav_view);
        Menu m=nv.getMenu();
        int id = item.getItemId();

        if(id==R.id.nav_home)
        {
            //Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,NotificationsActivity.class));
            finish();
        }
        return true;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.greendark));
        refreshContent();

    }

    private void refreshContent() {
        if(errorId!=null)
        {
            callGetErrorListAPI(errorId);

        }
        else {
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces) {

        switch (URL)
        {
            case errorlist:

                if(mSwipeRefreshLayout!=null)
                {
                    if(mSwipeRefreshLayout.isRefreshing())
                    {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }

                AllErrorsResponse allErrorsResponse= (AllErrorsResponse) response;
                if (isSucces) {
                    if(allErrorsResponse!=null)
                    {
                        if(allErrorsResponse.getErrorList()!=null)
                        {
                            mList=allErrorsResponse.getErrorList();
                            setErrorsAdapter(allErrorsResponse.getErrorList());
                        }

                    }
                    else {
                        showSnackBar(this,"No response from Server");
                    }


                } else {
                    //API call failed
                    showSnackBar(this, "API call failed");
                }



                break;
            case logOut:
                LogOutResponse logOutResponse = (LogOutResponse) response;
                if (isSucces) {
                    if (logOutResponse != null) {
                        if (logOutResponse.getMessage() != null) {
                            if (logOutResponse.getMessage().equalsIgnoreCase("success")) {

                                MQTTService1.unsubscribeMQTT();
                                stopService(new Intent(ErrorsListActivity.this, MQTTService1.class));


                                saveLogInPreference(this,false);

                                jobScheduler.cancel(JOB_ID);

                                Intent logOutIntent = new Intent(ErrorsListActivity.this, LoginActivity.class);
                                startActivity(logOutIntent);
                                finish();
                                trimCache(ErrorsListActivity.this);

                            } else {
                                showSnackBar(this, "Logout failed please try again");

                            }
                        } else {
                            showSnackBar(this, "Logout failed please try again");
                        }
                    } else {
                        showSnackBar(this, "No response from server");
                    }
                } else {
                    //API call failed
                    showSnackBar(this, "Logout failed");
                }
                break;

        }

    }

    private void setErrorsAdapter(List<ErrorList> errorList) {
        if(!errorList.isEmpty())
        {
            ErrorListAdapter errorListAdapter=new ErrorListAdapter(this,errorList);
            mRecyclerView.setAdapter(errorListAdapter);
        }
        else {
            showSnackBar(this,"List is Empty");
        }

    }


    @Override
    public void getErrorId(int position) {
       /* mList.get(position).getPeople();*/
        Log.d("errorid","selected error id pos=>"+position);

        String errorName=mList.get(position).getErrorname();
        String errorId=mList.get(position).getErrorid();
        String lineName=mList.get(position).getLinename();
        String peoples=mList.get(position).getPeople();
        String repairTime=mList.get(position).getRepairtime();
        String station=mList.get(position).getStation();
        String action=mList.get(position).getAction();
        String date=mList.get(position).getCreateddate();

        Intent intent=new Intent(this,ErrorDetailsActivity.class);
        intent.putExtra("ERROR_ID",errorId);
        intent.putExtra("ERROR_NAME",errorName);
        intent.putExtra("LINE_NAME",lineName);
        intent.putExtra("PEOPLES",peoples);
        intent.putExtra("REPAIR_TIME",repairTime);
        intent.putExtra("STATION",station);
        intent.putExtra("ACTION",action);
        intent.putExtra("DATE",date);

        startActivity(intent);

    }
}
