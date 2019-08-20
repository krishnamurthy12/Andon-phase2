package com.vvtech.andonphase2.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.login.UserLoginResponse;
import com.vvtech.andonphase2.apiresponses.logout.LogOutResponse;
import com.vvtech.andonphase2.services.MyJobService;
import com.vvtech.andonphase2.utils.AndonUtils;
import com.vvtech.andonphase2.utils.OnResponseListener;
import com.vvtech.andonphase2.utils.Utilities;
import com.vvtech.andonphase2.utils.WebServices;

import java.io.File;

import static com.vvtech.andonphase2.utils.Utilities.getAllLoginDetails;
import static com.vvtech.andonphase2.utils.Utilities.getBaseURL;
import static com.vvtech.andonphase2.utils.Utilities.getIPAddress;
import static com.vvtech.andonphase2.utils.Utilities.getIsLoggedIn;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    LinearLayout mLogOutLayout;
    TextView mEmpName,mEmpNTID;

    public static String employeeName,employeeID,employeeDepartment,employeValueStream,employeeLineID,employeeDesignamtion;
    public static String ipAddress;
    boolean isLoggedIn=false;
    public String BASE_URL="";

    JobScheduler jobScheduler;
    JobInfo jobInfo;
    public static final int JOB_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*to preventing from taking screen shots*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_base);
        getLogInSharedPreferenceData();
        initializeViews();
        constructJob();
    }

    public void getLogInSharedPreferenceData() {

        ipAddress= getIPAddress(this);

        String[] loginDetails=getAllLoginDetails(this);
        isLoggedIn=getIsLoggedIn(this);

        employeeName=loginDetails[0];
        employeeID= loginDetails[1];
        employeeDepartment=loginDetails[2];
        employeValueStream=loginDetails[3];
        employeeLineID=loginDetails[4];
        employeeDesignamtion=loginDetails[5];

        BASE_URL=getBaseURL(this);

    }

    private void initializeViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //getting reference to header layout of navigation drawer
        View header = navigationView.getHeaderView(0);
        mEmpName = (TextView) header.findViewById(R.id.vT_nh_username);
        mEmpNTID = (TextView) header.findViewById(R.id.vT_nh_ntid);

        mEmpName.setText(employeeName);
        mEmpNTID.setText(employeeDesignamtion);

        navigationView.setNavigationItemSelectedListener(this);

        mLogOutLayout=findViewById(R.id.nav_logout_layout);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void constructJob()
    {
        JobInfo.Builder builder=new JobInfo.Builder(JOB_ID,new ComponentName(this, MyJobService.class));
      /*  //if we want to send data through bundle
       PersistableBundle persistableBundle=new PersistableBundle();
       persistableBundle.putString("KEY","value");*/

        // builder.setPeriodic(15*60 * 1000); /* Repeat job for every 15 minutes*/
        builder.setPeriodic(60 * 1000); /* Repeat job for every 1 minutes*/

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // WIFI or ethernet network
        builder.setPersisted(true);

        jobInfo=builder.build();
        jobScheduler= (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }

    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
       // item.setChecked(true);

        NavigationView nv= (NavigationView) findViewById(R.id.nav_view);
        Menu m=nv.getMenu();
        int id = item.getItemId();

        /*if(id==R.id.nav_home)
        {
            //Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,NotificationsActivity.class));
            finish();
        }*/

         if(id==R.id.nav_topfailures)
        {

            boolean b=!m.findItem(R.id.nav_topfailures_vs).isVisible();
            //setting submenus visible state
            m.findItem(R.id.nav_topfailures_vs).setVisible(b);
            m.findItem(R.id.nav_topfailures_line).setVisible(b);
            m.findItem(R.id.nav_topfailures_machine).setVisible(b);
            return true;

        }
        else if(id==R.id.nav_topfailures_vs)
        {
            Toast.makeText(this, "topfailures vs", Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.nav_topfailures_line)
        {
            Toast.makeText(this, "topfailures line", Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.nav_topfailures_machine)
        {
            Toast.makeText(this, "topfailures machine", Toast.LENGTH_SHORT).show();

        }
        else if(id==R.id.nav_manuals)
        {
            Toast.makeText(this, "Mannuals", Toast.LENGTH_SHORT).show();
            /*// Toast.makeText(this, "Manuals", Toast.LENGTH_SHORT).show();
            boolean b=!m.findItem(R.id.nav_manuals_valuestream).isVisible();
            //setting submenus visible state
            m.findItem(R.id.nav_manuals_valuestream).setVisible(b);
            m.findItem(R.id.nav_manuals_line).setVisible(b);
            m.findItem(R.id.nav_manuals_machine).setVisible(b);
            return true;*/

        }
        else if (id == R.id.nav_contactus)
        {
            Toast.makeText(this, "contactus", Toast.LENGTH_SHORT).show();

        }
        else if (id == R.id.nav_report_issue)
        {
            Toast.makeText(this, "report issue", Toast.LENGTH_SHORT).show();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public static void trimCache(Context context) {
        try {

            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                //deleteDir(dir);

                if(deleteDir(dir))
                {
                    Log.d("clearcache","cache cleared");

                }
                else {
                    Log.d("clearcache","can not clear cache");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }

            Log.d("cachedfile",dir.getAbsolutePath());

            return dir.delete();
        }
        else if(dir.isFile())
        {
            return dir.delete();
        }
        else {
            return false;
        }
    }

    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    /*public void showSnackBar(Context context,String message)
    {
        Activity activity = (Activity) context;
        if(snackbar!=null)
        {
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message,Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView)view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        snackbar.show();
    }*/
}
