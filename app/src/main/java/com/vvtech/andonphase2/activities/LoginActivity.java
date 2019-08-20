package com.vvtech.andonphase2.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.apiresponses.login.UserLoginResponse;
import com.vvtech.andonphase2.utils.AndonUtils;
import com.vvtech.andonphase2.utils.OnResponseListener;
import com.vvtech.andonphase2.utils.WebServices;

import static com.vvtech.andonphase2.utils.Utilities.getBaseURL;
import static com.vvtech.andonphase2.utils.Utilities.getIPAddress;
import static com.vvtech.andonphase2.utils.Utilities.getIsLoggedIn;
import static com.vvtech.andonphase2.utils.Utilities.saveIPAddress;
import static com.vvtech.andonphase2.utils.Utilities.saveLogInPreference;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener<UserLoginResponse> {

    LinearLayout mEditIpLayout;
    TextView mTitle;
    Button mLogin;
    EditText mNtUserID,mEmployeeID,mPassword,mIPAddressEditText;
    TextView mVersionName;
    ProgressBar mProgressbar;

    Snackbar snackbar;

    String NTID,versionName;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    public static String DEVICE_UNIQUE_NUMBER="",DEVICE_IPADDRESS="";

    @Override
    protected void onStart() {
        super.onStart();

        checkPowerOptimizationPermission();

        String ip=getIPAddress(this);


        if (ip == null) {
            showSnackBar(this, "IP address is empty, please enter IP");
        }


        loadIMEI();

        //DEVICE_IPADDRESS=getLocalIpAddress();

        Log.d("devive details", "IMEI/UNIQUEID=>" + DEVICE_UNIQUE_NUMBER + "IP address=>" + DEVICE_IPADDRESS);
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            // int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
            mVersionName.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLoggedIn=getIsLoggedIn(this);

        if(isLoggedIn)
        {
            gotoNextActivity(NotificationsActivity.class);
        }
        else {
            setContentView(R.layout.activity_login);
            initializeViews();
        }

    }

    private void initializeViews() {
        AssetManager assetManager = getAssets();

        mVersionName=findViewById(R.id.vT_version_name);

        mTitle=findViewById(R.id.vT_title);
        Typeface dacasaTypeface = Typeface.createFromAsset( assetManager, "Fonts/Midnight_Drive.otf");
        mTitle.setTypeface(dacasaTypeface);


        mLogin=findViewById(R.id.vB_al_login);
        Typeface asparagusTypeface=Typeface.createFromAsset(assetManager,"Fonts/AsparagusSprouts.ttf");
        mLogin.setTypeface(asparagusTypeface);

        mEditIpLayout=findViewById(R.id.vL_edit_ipaddress_layout);

        mNtUserID=findViewById(R.id.vE_ntid);
        mEmployeeID=findViewById(R.id.vE_empid);

        mProgressbar = findViewById(R.id.vP_progressbar);
        mProgressbar.setVisibility(View.GONE);

        mLogin.setOnClickListener(this);
        mEditIpLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.vB_al_login:
                hideKeyBoard();
                if(!mProgressbar.isShown())
                {
                    validateFields();
                }
                else {
                    showSnackBar(this,"Please let the current process to complete");
                }

                break;

            case R.id.vL_edit_ipaddress_layout:
                showPasswordLayout();
                break;

            case R.id.vT_password_ok:
                hideKeyBoard();
                if(mPassword.getText().toString().equalsIgnoreCase("123456"))
                {
                    alertDialog.dismiss();
                    showIPAddressLayout();
                }
                else {
                    showSnackBar(this,"Password incorrect");
                }

                break;

           case R.id.vT_password_cancel:
               hideKeyBoard();
               alertDialog.dismiss();
            break;

            case R.id.vT_ipaddress_cancel:
                hideKeyBoard();
                alertDialog.dismiss();
                break;

            case R.id.vT_ipaddress_ok:
                hideKeyBoard();
                saveIpAddress();
                break;

        }

    }

    private void validateFields() {
        if (!validateNTUserID()) {
            return;
        } else if (!validateEmployeeID()) {
            return;
        }
        callLoginAPI();

    }

    private boolean validateNTUserID() {

        NTID = mNtUserID.getText().toString().trim();

        if (NTID.isEmpty() || NTID.length() <= 5 /*|| !isValidUserName(employeeName)*/) {
            //Snackbar.make(mLoginButton,"Please enter a valid user name", Snackbar.LENGTH_SHORT).show();
            showSnackBar(this, "Please enter a valid NT-ID");
            return false;
        }

        return true;
    }

    private boolean validateEmployeeID() {
        if (mEmployeeID.getText().toString().trim().isEmpty()) {
            //Snackbar.make(mLoginButton,"Password Field should not be empty", Snackbar.LENGTH_SHORT).show();
            showSnackBar(this, "Employee id should not be empty");
            return false;
        } else if (mEmployeeID.getText().toString().trim().length() <=5) {
            //Snackbar.make(mLoginButton,"Invalid password", Snackbar.LENGTH_SHORT).show();
            showSnackBar(this, "Invalid Employee id");
            return false;
        }

        return true;
    }

   /* private void callLoginAPI() {
        gotoNextActivity(MainActivity.class);
    }*/

    private void callLoginAPI() {

        String employeeName = mNtUserID.getText().toString();
        String employeeID = mEmployeeID.getText().toString();

        if (AndonUtils.isConnectedToInternet(getApplicationContext())) {
            mProgressbar.setVisibility(View.VISIBLE);
            WebServices<UserLoginResponse> webServices = new WebServices<UserLoginResponse>(LoginActivity.this);
            webServices.userLogIn(getBaseURL(this), WebServices.ApiType.userlogin, employeeName, employeeID);

        } else {
            //Snackbar.make(mSignup,R.string.err_msg_nointernet, Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, getResources().getString(R.string.err_msg_nointernet) + "", Toast.LENGTH_SHORT).show();
        }
    }
    private void showSnackBar(Context context, String message) {
        Activity activity = (Activity) context;
        if (snackbar != null) {
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        snackbar.show();
    }



    public void showPasswordLayout()
    {
        CoordinatorLayout rootlayout=findViewById(R.id.vC_al_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.password_layout, rootlayout,false);
         mPassword=dialogView.findViewById(R.id.vE_password);
        TextView cancel = dialogView.findViewById(R.id.vT_password_cancel);
        TextView ok = dialogView.findViewById(R.id.vT_password_ok);

        builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(LoginActivity.this);
        ok.setOnClickListener(LoginActivity.this);

    }
    private void showIPAddressLayout()
    {
        String currentIP=getIPAddress(this);

        CoordinatorLayout rootlayout=findViewById(R.id.vC_al_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.ipaddress_layout, rootlayout,false);
         mIPAddressEditText=dialogView.findViewById(R.id.vE_ipaddress);
        if(currentIP!=null)
        {
            mIPAddressEditText.setText(currentIP);
        }
        TextView cancel = dialogView.findViewById(R.id.vT_ipaddress_cancel);
        TextView save = dialogView.findViewById(R.id.vT_ipaddress_ok);

       // builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(LoginActivity.this);
        save.setOnClickListener(LoginActivity.this);

    }

    private void saveIpAddress() {
        String ip = mIPAddressEditText.getText().toString();
        if (TextUtils.isEmpty(ip) || ip.length() < 12 || !ip.contains(".")) {
            showSnackBar(this,"enter a valid IP address");
            hideKeyBoard();

        } else {
            alertDialog.dismiss();
            saveIPAddress(this,ip);
            hideKeyBoard();

        }

    }

    private void hideKeyBoard() {
        try {
            //InputMethodManager is used to hide the virtual keyboard from the user after finishing the user input
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (NullPointerException e) {
            Log.e("Exception", e.getMessage() + ">>");
        }

    }


    @Override
    public void onResponse(UserLoginResponse response, WebServices.ApiType URL, boolean isSucces) {

        switch (URL) {
            case userlogin:
                if (mProgressbar.isShown()) {
                    mProgressbar.setVisibility(View.GONE);
                }
                UserLoginResponse userLoginResponse = (UserLoginResponse) response;
                if (isSucces) {
                    if (userLoginResponse != null) {
                        if (userLoginResponse.getEmployeeId() != null && userLoginResponse.getEmployeeName() != null && userLoginResponse.getError() == null) {
                            saveLogInSession(userLoginResponse);
                        } else {
                            showSnackBar(this, userLoginResponse.getError() + "");
                        }

                    } else {
                        Toast.makeText(this, "No response fron server", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    //API call failed
                    Toast.makeText(this, "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }

    private void saveLogInSession(UserLoginResponse userLoginResponse) {
        String employeeName = userLoginResponse.getEmployeeName();
        String employeeID = userLoginResponse.getEmployeeId();
        String employeeDepartment = userLoginResponse.getDeptName();
        String employeeValueStream = userLoginResponse.getValueStream();
        String employeelineID = userLoginResponse.getLineId();
        String employeeDesignation = userLoginResponse.getDesignation();
        String ntUserID=userLoginResponse.getNtuserId();

        saveLogInPreference(this,true,employeeName,employeeID,employeeDepartment,
                employeeValueStream,employeelineID,employeeDesignation,ntUserID);

        SharedPreferences devicePreferences=getSharedPreferences("DEVICE_PREFERENCES",MODE_PRIVATE);
        SharedPreferences.Editor editor1=devicePreferences.edit();
        if(devicePreferences.contains("IMEI_NUMBER"))
        {
            Log.d("savingvalues","inside devicePreferences if block");
            editor1.clear();
            editor1.apply();
        }

        Log.d("savingvalues","imei=>"+DEVICE_UNIQUE_NUMBER+" ip=>"+DEVICE_IPADDRESS+" ntuid=>"+ntUserID+" empname=>"+employeeName);

        editor1.putString("IMEI_NUMBER",DEVICE_UNIQUE_NUMBER);
        editor1.putString("IP_ADDRESS",DEVICE_IPADDRESS);
        editor1.putString("NT_USERID",ntUserID);
        editor1.putString("USER_NAME",employeeName);
        editor1.apply();

        mNtUserID.setText("");

        gotoNextActivity(NotificationsActivity.class);


    }

    @SuppressLint("HardwareIds")
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
//                get_imei_data();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {

            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                DEVICE_UNIQUE_NUMBER = tm.getDeviceId();
            }
            if (null == DEVICE_UNIQUE_NUMBER || 0 == DEVICE_UNIQUE_NUMBER.length()) {
                DEVICE_UNIQUE_NUMBER = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            // Log.d("devive details","IMEI/UNIQUEID=>"+DEVICE_UNIQUE_NUMBER+"IP address=>"+DEVICE_IPADDRESS);
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                if (null != tm) {
                    DEVICE_UNIQUE_NUMBER = tm.getDeviceId();
                }
                if (null == DEVICE_UNIQUE_NUMBER || 0 == DEVICE_UNIQUE_NUMBER.length()) {
                    DEVICE_UNIQUE_NUMBER = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                }

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void gotoNextActivity(Class<?> target)
    {
        finish();
        startActivity(new Intent(this,target));

    }

    @SuppressLint("BatteryLife")
    private void checkPowerOptimizationPermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
            else {
                //Permissions granted do whatever you want to do
            }
        }
    }
}
