package com.vvtech.andonphase2.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Utilities {

    public static Snackbar snackbar;
    private static Toast mToast;
    private static String loginPreference="LOGINPREFERENCE";
    private static String ipAddressPreference="IPADDRESSPREFERENCE";

/*
* To showSnackBar message
* -----------------------------------------------------------------------------------------------------------------
* */
    public static void showSnackBar(Context context, String message) {
        Activity activity = (Activity) context;
        if (snackbar != null) {
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        snackbar.show();
    }

    /*
    * --------------------------------------------------------------------------------------------------------------
    * */


/*
* To show Toast message
* -----------------------------------------------------------------------------------------------------------------
* */
    public static void showToast(Context context, String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /*
    * ------------------------------------------------------------------------------------------------------------
    * */


/*For getting BaseURL
* -------------------------------------------------------------------------------------------------------------------
 * */
    public static String getBaseURL(Context context)
    {
        String ipAddress=getIPAddress(context);
        if(ipAddress!=null)
        {
            return "http://"+ipAddress+":8080/AndonWebservices/rest/";

        }

        return null;
    }
    /*
    * ------------------------------------------------------------------------------------------------------------------
    * */

/*For saving IP Address
* -------------------------------------------------------------------------------------------------------------------
* */
    public static void saveIPAddress(Context context, String ipAddress)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(ipAddressPreference,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

       /* String ipaddress=sharedPreferences.getString("IPADDRESS",null);
        if(ipaddress!=null)*/
        if(sharedPreferences.contains("IPADDRESS"))
        {
            editor.clear();
            editor.apply();
        }
        editor.putString("IPADDRESS",ipAddress);
        editor.apply();
    }


    public static String getIPAddress(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(ipAddressPreference,Context.MODE_PRIVATE);
        return sharedPreferences.getString("IPADDRESS",null);

    }
    /*
 * -------------------------------------------------------------------------------------------------------------------
     * */


    /*
    To saveLogInPreference
 * -------------------------------------------------------------------------------------------------------------------
     * */

    public static void saveLogInPreference(Context context, boolean isLoggedIn, String...strings)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(loginPreference,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        if(isLoggedIn)
        {
            editor.putBoolean("ISLOGGEDIN",isLoggedIn);
            editor.putString("LINENAME",strings[0]);
            editor.putString("STATIONNAME",strings[1]);
            editor.apply();

            editor.putBoolean("IS_LOGGEDIN", true);
            editor.putString("EMPLOYEE_NAME", strings[0]);
            editor.putString("EMPLOYEE_ID", strings[1]);
            editor.putString("EMPLOYEE_DEPARTMENT", strings[2]);
            editor.putString("EMPLOYEE_VALUESTREAM", strings[3]);
            editor.putString("EMPLOYEE_LINEID", strings[4]);
            editor.putString("EMPLOYEE_DESIGNATION", strings[5]);
            editor.putString("NT_USERID", strings[6]);
            editor.apply();
        }
        else {
            editor.clear();
            editor.apply();
        }

    }

    public static boolean getIsLoggedIn(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(loginPreference,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("ISLOGGEDIN",false);
    }

    public static String[] getAllLoginDetails(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(loginPreference,Context.MODE_PRIVATE);
        String[] details=new String[7];

        details[0]=sharedPreferences.getString("EMPLOYEE_NAME",null);
        details[1]=sharedPreferences.getString("EMPLOYEE_ID",null);
        details[2]=sharedPreferences.getString("EMPLOYEE_DEPARTMENT",null);
        details[3]=sharedPreferences.getString("EMPLOYEE_VALUESTREAM",null);
        details[4]=sharedPreferences.getString("EMPLOYEE_LINEID",null);
        details[5]=sharedPreferences.getString("EMPLOYEE_DESIGNATION",null);
        details[6]=sharedPreferences.getString("NT_USERID",null);

        return details;
    }


    /*
* -------------------------------------------------------------------------------------------------------------------
     * */


    /*
    To check whether the app is connected to Internet or not
 * -------------------------------------------------------------------------------------------------------------------
     * */

    public static boolean isConnectedToInternet(Context con){
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    /*
    * ------------------------------------------------------------------------------------------------------------------
    * */

    //For View(s)
    public static void toggleVisibility(boolean status,View... views) {
        for (View v : views) {
            //if status is true make view visible, else visibility gone
            if(status)
            {
                v.setVisibility(View.VISIBLE);

            }
            else {
                v.setVisibility(View.GONE);
            }
            /*if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }*/
        }
    }

    //For ViewGroup(s)
    public static void toggleVisibility(boolean status,ViewGroup... views) {
        for (View v : views) {
            //if status is true make view visible, else visibility gone
            if(status)
            {
                v.setVisibility(View.VISIBLE);

            }
            else {
                v.setVisibility(View.GONE);
            }
            /*if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }*/
        }
    }
    /*
     * ------------------------------------------------------------------------------------------------------------------
     * */

     /*
    To check whether the app is in background or foreground
 * -------------------------------------------------------------------------------------------------------------------
     * */

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
