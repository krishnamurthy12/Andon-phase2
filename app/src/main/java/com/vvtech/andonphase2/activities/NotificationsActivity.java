package com.vvtech.andonphase2.activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.vvtech.andonphase2.R;
import com.vvtech.andonphase2.adapters.EmployeesAdapter;
import com.vvtech.andonphase2.adapters.NotificationsAdapter;
import com.vvtech.andonphase2.apiresponses.allnotifications.AllNotificationsResponse;
import com.vvtech.andonphase2.apiresponses.allnotifications.NotificationList;
import com.vvtech.andonphase2.apiresponses.allusers.AllAvailableUsersResponse;
import com.vvtech.andonphase2.apiresponses.allusers.EmployeeStatusList;
import com.vvtech.andonphase2.apiresponses.general.InteractionResponse;
import com.vvtech.andonphase2.apiresponses.logout.LogOutResponse;
import com.vvtech.andonphase2.events.NotificationEvent;
import com.vvtech.andonphase2.services.MQTTService1;
import com.vvtech.andonphase2.services.MyJobService;
import com.vvtech.andonphase2.utils.AndonUtils;
import com.vvtech.andonphase2.utils.NotificationClass;
import com.vvtech.andonphase2.utils.OnResponseListener;
import com.vvtech.andonphase2.utils.WebServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vvtech.andonphase2.utils.Utilities.isAppIsInBackground;
import static com.vvtech.andonphase2.utils.Utilities.saveLogInPreference;
import static com.vvtech.andonphase2.utils.Utilities.showSnackBar;
import static com.vvtech.andonphase2.utils.Utilities.showToast;

public class NotificationsActivity extends BaseActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,NotificationsAdapter.ItemClickInterface,OnResponseListener{

    String TAG="NotificationsActivity";
    
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;

    RecyclerView mNotificationsRecyclerView;
    RecyclerView mUsersRecyclerView;
    NotificationsAdapter notificationAdapter;

    SwipeRefreshLayout mSwipeRefreshLayout;
    ProgressBar mProgressBar;

    List<EmployeeStatusList> employeeStatusList=new ArrayList<>();
    List<NotificationList> notificationList=new ArrayList<>();

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    ProgressDialog progressDialog;

    public boolean IS_USER_INTERACTING=false;

    JobScheduler jobScheduler;
    JobInfo jobInfo;
    private static final int JOB_ID = 101;

    Handler refreshHandler;

    private Timer mTimer1;
    private Handler mTimerHandler = new Handler();


    String[] actionTypeArray = {"Containment action", "Preventive Action","Corrective action"};
    String selectedAction;
    String selectionPosition;
    String actionType="machine";

    private SearchView searchView;


    @Override
    protected void onStart() {
        super.onStart();
        NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notif != null) {
            notif.cancelAll();
        }

        EventBus.getDefault().register(this);

        callAllNotificationsAPI();
        callAllAvailableUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("flowcheck","inside onResume()");

        MQTTService1 mqttService1=new MQTTService1();

        if (!isMyServiceRunning(mqttService1.getClass())) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("versionchecck","Build.VERSION_CODES>=M");
                Intent serviceIntent = new Intent(this, MQTTService1.class);
                ContextCompat.startForegroundService(this, serviceIntent );
            }
            else {
                Log.d("versionchecck","Build.VERSION_CODES<M");
                startService(new Intent(this,MQTTService1.class));

            }
            // startService(new Intent(this,MQTTService1.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_notifications);
        //This is the FrameLayout area within your activity_base.xml
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame); //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_notifications, contentFrameLayout);
        getLogInSharedPreferenceData();
        initializeViews();
    }


    private void initializeViews() {

        // toolbar fancy stuff
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Error list");

        layoutBottomSheet=findViewById(R.id.bottom_sheet);

        mSwipeRefreshLayout=findViewById(R.id.vS_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mProgressBar=findViewById(R.id.vP_an_progressBar);
        mProgressBar.setVisibility(View.GONE);

        mNotificationsRecyclerView=findViewById(R.id.vR_notifications_recycler_view);
        mNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNotificationsRecyclerView.setHasFixedSize(true);
        //mNotificationsRecyclerView.setAdapter(new SimpleAdapter(mNotificationsRecyclerView));
       /* mNotificationsRecyclerView.setAdapter(new NotificationsAdapter(mDialList,this,mNotificationsRecyclerView));*/

        mUsersRecyclerView=findViewById(R.id.vR_employee_list_recyclerview);
        mUsersRecyclerView.setLayoutManager(new GridLayoutManager(this,4));

        mUsersRecyclerView.setHasFixedSize(true);
       /* mUsersRecyclerView.setAdapter(new EmployeesAdapter(this,mEmpList));*/


        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setHideable(false);

        layoutBottomSheet.setOnClickListener(this);

        mLogOutLayout.setOnClickListener(this);
    }


    private void startTimer(){
        if(mTimer1!=null)
        {
            stopTimer();
        }
        mTimer1 = new Timer();
        TimerTask mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run() {
                        //TODO
                        Log.d(TAG, "inside startTimer run method");
                        MQTTService1 mqttService1 = new MQTTService1();
                        if(!isAppIsInBackground(NotificationsActivity.this))
                        {
                            if(!IS_USER_INTERACTING)
                            {
                                Log.d(TAG, "inside inner if block");
                                if (!isMyServiceRunning(mqttService1.getClass())) {
                                    Log.d(TAG,"MyService not Running");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        Intent serviceIntent = new Intent(NotificationsActivity.this, MQTTService1.class);
                                        ContextCompat.startForegroundService(NotificationsActivity.this, serviceIntent );
                                    }
                                    else {
                                        startService(new Intent(NotificationsActivity.this,MQTTService1.class));

                                    }
                                    // startService(new Intent(this,MQTTService1.class));
                                }
                                else {
                                    Log.d(TAG,"MyService Running");
                                }
                                /*if (!isMyServiceRunning(mqttService1.getClass())) {

                                    //startService(new Intent(MQTTService1.this, MQTTService1.class));
                                }
*/
                            }

                        }

                        //doConnect();
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 1, 60*1000); //1 minutes
    }

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    /*
    *APIs Section
    * * --------------------------------------------------------------------------------------------------------------------------
    * */
    public void callLogoutCurrentUser() {
        //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();

        if (AndonUtils.isConnectedToInternet(getApplicationContext())) {
            WebServices<LogOutResponse> webServices = new WebServices<LogOutResponse>(this);
            webServices.logOut(BASE_URL, WebServices.ApiType.logOut,  employeeID);

        } else {
            //Snackbar.make(mSignup,R.string.err_msg_nointernet, Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, getResources().getString(R.string.err_msg_nointernet) + "", Toast.LENGTH_SHORT).show();
        }
    }

    private void callAllNotificationsAPI()
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            // mProgressBar.setVisibility(View.VISIBLE);
            WebServices<AllNotificationsResponse> webServices = new WebServices<AllNotificationsResponse>(this);
            webServices.getAllNotifications(BASE_URL, WebServices.ApiType.allNotifications,employeeDepartment,employeValueStream);


        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callAllAvailableUsers()
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            WebServices<AllAvailableUsersResponse> webServices = new WebServices<AllAvailableUsersResponse>(this);
            webServices.getAllUsers(BASE_URL, WebServices.ApiType.allAvailableUsers,employeeDepartment,employeValueStream);


        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callAcceptMeaasgeAPI(String notificationId,String employeeId,String team)
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            progressDialog=new ProgressDialog(NotificationsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Accepting...");
            progressDialog.show();

            WebServices<InteractionResponse> webServices = new WebServices<InteractionResponse>(this);
            webServices.acceptError(BASE_URL, WebServices.ApiType.acceptError,notificationId,employeeId,team);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callgiveCAAPI(String notificationId,String enteredMessage,String selectionPosition,String employeeId,String team)
    {
        Log.d("entereddetails","entered msg=>"+enteredMessage+" selected pos=>"+selectionPosition);
        if(AndonUtils.isConnectedToInternet(this))
        {
            progressDialog=new ProgressDialog(NotificationsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Updating...");
            progressDialog.show();

            WebServices<InteractionResponse> webServices = new WebServices<InteractionResponse>(this);
            webServices.giveCA(BASE_URL, WebServices.ApiType.giveCA,notificationId,enteredMessage,selectionPosition,employeeId,team);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callgiveMOECommentAPI(String notificationId,String action,String employeeId,String team)
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            progressDialog=new ProgressDialog(NotificationsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Updating...");
            progressDialog.show();

            WebServices<InteractionResponse> webServices = new WebServices<InteractionResponse>(this);
            webServices.giveMOEComment(BASE_URL, WebServices.ApiType.giveMOEComment,notificationId,action,employeeId,team);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callgetCaGivenAPI(String notificationId,String team)
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            progressDialog=new ProgressDialog(NotificationsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Retriving details...");
            progressDialog.show();

            WebServices<InteractionResponse> webServices = new WebServices<InteractionResponse>(this);
            webServices.getCAGiven(BASE_URL, WebServices.ApiType.getCAGiven,notificationId,team);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callCheckListAPI(String notificationId,String response,String employeeId)
    {
        if(AndonUtils.isConnectedToInternet(this))
        {
            progressDialog=new ProgressDialog(NotificationsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Updating...");
            progressDialog.show();

            WebServices<InteractionResponse> webServices = new WebServices<InteractionResponse>(this);
            webServices.checkList(BASE_URL, WebServices.ApiType.checklistConfirm,notificationId,response,employeeId);

        }
        else {
            showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
        }
    }
    /*
    * ----------------------------------------------------------------------------------------------------------------------------
    * */

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.bottom_sheet:
                openBottomSheet();
                break;

            case R.id.vT_dl_submit:

                if(alertDialog.isShowing())
                {
                    alertDialog.dismiss();
                }
                break;

            case R.id.vT_dl_cancel:

                if(alertDialog.isShowing())
                {
                    alertDialog.dismiss();
                }
                break;


           /* case R.id.fab:
                if (checkPermissions()) {
                    //  permissions  granted.
                    takePhoto();
                }
                break;*/

            case R.id.nav_logout_layout:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                callLogoutCurrentUser();
                break;
        }
    }



    public void openBottomSheet()
    {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if(notificationAdapter!=null)
                {
                    notificationAdapter.getFilter().filter(query);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                //notificationAdapter.getFilter().filter(query);
                if(notificationAdapter!=null)
                {
                    notificationAdapter.getFilter().filter(query);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   /* private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }*/

    @Override
    public void onRefresh() {

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.greendark));
        refreshContent();

    }

    private void refreshContent() {

       callAllNotificationsAPI();
    }

    @Override
    public void itemSelected(String item, int position,String notificationId,String team) {

        String notificationStatus = notificationList.get(position).getNotificationStatus().trim().replaceAll("^\"|\"$", "");

        String department = employeeDepartment;


        if(item.equalsIgnoreCase("accept"))
        {

            if (notificationStatus.equalsIgnoreCase("Open")) {

                //Interface method when Notification is in open state
                //showSnackBar(this,item+" "+position);
                callAcceptMeaasgeAPI(notificationId, employeeID, team);

            }

        }

        else if(item.equalsIgnoreCase("action"))
        {

            Snackbar.make(mNotificationsRecyclerView, item+" "+position,Snackbar.LENGTH_SHORT).show();


            if (notificationStatus.equalsIgnoreCase("CA Pending") &&
                    ( department.contains("TEF") || department.equals("LOM") || department.contains("FCM") ) )
            {

                showActionPopup(notificationId,employeeID,team);
                //Interface method when TEF team completes work and try to give to CA


            }
            else if (notificationStatus.equalsIgnoreCase("CheckList Pending") &&
                    ( department.contains("TEF") || department.equals("LOM") || department.contains("FCM") )) {

                //Interface method when repair time takes morethan 30 mins
                //showSnackBar(this,"CheckList Pending "+team);
                showCheckListPopup(notificationId,employeeID);

            } else if (notificationStatus.equalsIgnoreCase("MOE Comment Pending") && (department.contains("MOE"))) {

                //Interface method when TEF team completes their work
             //   showSnackBar(this,"MOE Comment Pending "+team);
                callgetCaGivenAPI(notificationId,team);
            }
            else if (notificationStatus.equalsIgnoreCase("Ack Pending")) {

                showSnackBar(this,"You can't acknowledge here, Please do acknowledged in the line");
            }
            else {
                showSnackBar(this,"You can't perform any actions for this ticket");
            }

        }
        else if(item.equalsIgnoreCase("history"))
        {
            //Snackbar.make(mNotificationsRecyclerView, item+" "+position,Snackbar.LENGTH_SHORT).show();
            Intent intent=new Intent(this,ErrorsListActivity.class);
            intent.putExtra("ERROR_ID",String.valueOf(notificationList.get(position).getErrorId()));
            startActivity(intent);


        }





       /* if (notificationStatus.equalsIgnoreCase("CA Pending") && department.equals("LOM")) {

            //Interface method when LOM team completes work and try to give to CA

        }

        if (notificationStatus.equalsIgnoreCase("CA Pending") && department.contains("FCM")) {

            //Interface method when FCM team completes work and try to give to CA


        }*/

    }

    private void showDeclineDialog(int position) {


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.decline_layout, null);
        EditText description=dialogView.findViewById(R.id.vE_dl_description);
        TextView cancel = dialogView.findViewById(R.id.vT_dl_cancel);
        TextView submit = dialogView.findViewById(R.id.vT_dl_submit);

        builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();


        cancel.setOnClickListener(NotificationsActivity.this);
        submit.setOnClickListener(NotificationsActivity.this);

    }

    public void refreshActivity()
    {
        finish();
        overridePendingTransition( 0, 0);
        startActivity(getIntent());
        overridePendingTransition( 0, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {

            /*This is called when all your layouts or UI have been successfully loaded or created properly*/

        }
        else {
            /*this will called when user navigates out from the current activity*/
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces) {

        switch (URL)
        {
            case allNotifications:
                /*if(mProgressBar.isShown())
                {
                    mProgressBar.setVisibility(View.GONE);
                }*/
                if(mSwipeRefreshLayout!=null)
                {
                    if(mSwipeRefreshLayout.isRefreshing())
                    {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }

                AllNotificationsResponse notificationsResponse= (AllNotificationsResponse) response;
                if (isSucces) {

                    if (notificationsResponse != null) {
                        notificationList = notificationsResponse.getNotificationList();
                        setNotificationAdapter(notificationList);

                    } else {
                        showSnackBar(this, "allNotificationsResponse null");

                    }
                    //showSnackBar(this," outside allNotificationsResponse block");


                } else {
                    //API call failed
                    showSnackBar(this, "API call failed");
                }
               
               
              
                break;
            case allAvailableUsers:
                if (isSucces) {
                    AllAvailableUsersResponse allAvailableUsersResponse = (AllAvailableUsersResponse) response;


                    if (allAvailableUsersResponse != null) {
                        //subscribeToMQTT();
                        employeeStatusList = allAvailableUsersResponse.getEmployeeStatusList();
                        setAllUsersAdapter(employeeStatusList);

                    }

                } else {
                    //API call failed
                    showSnackBar(this, "API call failed");

                }
                break;

            case acceptError:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }

                InteractionResponse acceptErrorResponse = (InteractionResponse) response;
                if (isSucces) {

                    if (acceptErrorResponse != null) {
                        if (acceptErrorResponse.getMessage().equalsIgnoreCase("true")) {
                            //success
                            refreshContent();
                        } else {
                            showToast(this,acceptErrorResponse.getMessage());
                        }


                    } else {
                        showSnackBar(this, "No response from server");
                    }
                } else{
                //API call failed
                showSnackBar(this, "Server is busy");
            }
                break;

            case giveCA:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }

                InteractionResponse giveCAResponse = (InteractionResponse) response;
                if (isSucces) {

                        if (giveCAResponse != null) {

                            if (giveCAResponse.getMessage().equalsIgnoreCase("true")) {
                                //success
                                showToast(this,"Message saved");
                                refreshContent();
                            } else {
                                showToast(this,giveCAResponse.getMessage());
                            }


                        } else {
                            showSnackBar(this, "No response from server");
                        }
                } else {
                    //API call failed
                    showSnackBar(this, "Server is busy");
                }
                break;


            case checklistConfirm:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }

                InteractionResponse checklistConfirmResponse = (InteractionResponse) response;
                if (isSucces) {

                        if (checklistConfirmResponse != null) {

                            if (checklistConfirmResponse.getMessage().equalsIgnoreCase("true")) {
                                //success
                                refreshContent();
                                showToast(this,"Need to Fill the checklist in Line");
                            } else  if (checklistConfirmResponse.getMessage().equalsIgnoreCase("false")){
                                refreshContent();
                            }
                            else {
                                showToast(this,checklistConfirmResponse.getMessage());
                            }

                        } else {
                            showSnackBar(this, "No response from server");
                        }

                } else {
                    //API call failed
                    showSnackBar(this, "Server is busy");
                }
                break;

            case getCAGiven:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }
                InteractionResponse getCAGivenResponse = (InteractionResponse) response;
                if (isSucces) {

                        if (getCAGivenResponse != null) {

                            //success
                            String[] msg = getCAGivenResponse.getMessage().split("/");
                            if (msg.length > 2) {
                                String msgID = msg[1].replace("\"", "");
                                String resolvedMessage = "Resolver Error" + ": " + msg[2].replace("\"", "");
                                String team = msg[3].replace("\"", "");

                                showMOEpoPup(resolvedMessage, msgID, team);
                            } else {
                                showToast(this,getCAGivenResponse.getMessage());
                            }



                        } else {
                            showSnackBar(this, "No response from server");
                        }
                } else {
                    //API call failed
                    showSnackBar(this, "Server is busy");
                }
                break;

            case giveMOEComment:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }
                InteractionResponse giveMOECommentResponse = (InteractionResponse) response;
                if (isSucces) {

                        if (giveMOECommentResponse != null) {
                            if (giveMOECommentResponse.getMessage().equalsIgnoreCase("true")) {
                                //success
                                showToast(this,"Message saved");

                                //To recreate activity
                                refreshContent();
                            } else {
                                showToast(this,giveMOECommentResponse.getMessage());
                            }

                        } else {
                            showSnackBar(this, "No response from server");
                        }


                } else {
                    //API call failed
                    showSnackBar(this, "Server is busy");
                }
                break;

            case logOut:
                if(progressDialog!=null)
                {
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }
                LogOutResponse logOutResponse = (LogOutResponse) response;
                if (isSucces) {
                    if (logOutResponse != null) {
                        if (logOutResponse.getMessage() != null) {
                            if (logOutResponse.getMessage().equalsIgnoreCase("success")) {

                                MQTTService1.unsubscribeMQTT();
                                stopService(new Intent(NotificationsActivity.this, MQTTService1.class));


                                saveLogInPreference(this,false);

                                //jobScheduler.cancel(JOB_ID);

                                Intent logOutIntent = new Intent(NotificationsActivity.this, LoginActivity.class);
                                logOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                logOutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(logOutIntent);
                                finish();
                                trimCache(NotificationsActivity.this);

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

    private void setAllUsersAdapter(List<EmployeeStatusList> employeeStatusList) {
        if(employeeStatusList!=null)
        {
            EmployeesAdapter employeesAdapter=new EmployeesAdapter(this,employeeStatusList);
            mUsersRecyclerView.setAdapter(employeesAdapter);
            employeesAdapter.notifyDataSetChanged();
        }

    }

    private void setNotificationAdapter(List<NotificationList> notificationList) {
        if(notificationList!=null)
        {

            //Log.d("notificationdetails","error id=>"+notificationList.get(0).getNotificationId()+"error=>"+notificationList.get(0).getError());
            //Toast.makeText(this, "notificationList not null", Toast.LENGTH_SHORT).show();
            if(!notificationList.isEmpty())
            {
                 notificationAdapter=new NotificationsAdapter(notificationList,this,mNotificationsRecyclerView);
                //notificationAdapter=new NotificationAdapter(this,notificationList,mErrorId);
                 mNotificationsRecyclerView.setAdapter(notificationAdapter);
                notificationAdapter.notifyDataSetChanged();

            }
            else {
                showSnackBar(this,"Currently there are no issues");

            }

            //showSnackBar(this,"size=>"+mNotificationsRecyclerView.getAdapter()+"");

        }
        else {
            showSnackBar(this,"List is empty");
        }

    }

    /*is to identify specified service is running or not*/
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i ("isMyServiceRunning?", true+"");
                    return true;
                }
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    private void showMOEpoPup(String resolvedMessage, final String notificationID,final String employeeTeam) {

        IS_USER_INTERACTING=true;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.moe_popup_layout, null);

        final EditText mComment = dialogView.findViewById(R.id.vE_mpl_entered_text);
        TextView mYes = dialogView.findViewById(R.id.vT_mpl_ok);
        TextView mNo = dialogView.findViewById(R.id.vT_mpl_cancel);
        TextView mMessage=dialogView.findViewById(R.id.vT_mpl_messagebody);

        mMessage.setText(resolvedMessage);

        builder = new AlertDialog.Builder(NotificationsActivity.this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //replaceAll(System.getProperty("line.separator"), "") is used to remove new line characters from entered text
                String enteredText = mComment.getText().toString().trim().replaceAll(System.getProperty("line.separator"), "");

                if(TextUtils.isEmpty(enteredText) || enteredText.length()<5)
                {
                    //Toast.makeText(context, "Closing comment length should be atleast 5 characters", Toast.LENGTH_SHORT).show();
                    showSnackBar(NotificationsActivity.this,"Closing comment length should be atleast 5 characters");
                }
                else {
                    alertDialog.dismiss();
                    callgiveMOECommentAPI(notificationID,enteredText,employeeID,employeeTeam);
                    /* callMOEClosingAPI(notificationID,enteredText,employeeID,employeeTeam);*/
                    IS_USER_INTERACTING=false;
                }
                hideKeyBoard();

                /* refreshList();*/
            }
        });

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                hideKeyBoard();
                IS_USER_INTERACTING=false;
                refreshContent();
            }
        });

    }

    private void showActionPopup(final String notificationID, final String employeeID, final String employeeTeam) {
        IS_USER_INTERACTING=true;

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.containment_action_layout, null);

        final EditText mComment = dialogView.findViewById(R.id.vMLT_entered_text);
        TextView mYes = dialogView.findViewById(R.id.vT_cal_ok);
        TextView mNo = dialogView.findViewById(R.id.vT_cal_cancel);
        final AppCompatSpinner spinner=dialogView.findViewById(R.id.vS_action_type);

        final RadioGroup mRadioGroup=(RadioGroup)dialogView.findViewById(R.id.vR_radiogroup);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.vRB_machine:
                        spinner.setVisibility(View.VISIBLE);
                        actionType="machine";
                        break;
                    case R.id.vRB_process:
                        spinner.setVisibility(View.VISIBLE);
                        actionType="process";
                        break;
                    case R.id.vRB_organization:
                        spinner.setVisibility(View.GONE);
                        selectionPosition= String.valueOf(7);
                        actionType="organization";
                        break;
                }

            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,actionTypeArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAction=actionTypeArray[position];
                selectionPosition= String.valueOf(position);
                if(actionType.equals("machine"))
                {
                    selectionPosition= String.valueOf(position+1);
                }
                else if(actionType.equalsIgnoreCase("process"))
                {
                    selectionPosition= String.valueOf(position+4);
                }
                else
                {
                    selectionPosition= String.valueOf(7);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder = new AlertDialog.Builder(NotificationsActivity.this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //replaceAll(System.getProperty("line.separator"), "") is used to remove new line characters from entered text

                // showToast("Selected position is =>"+selectionPosition);
               /* String enteredMessage = mComment.getText().toString().trim().replaceAll(System.getProperty("line.separator"), "");
                if (TextUtils.isEmpty(enteredMessage) || enteredMessage.length() < 5) {
                    //Toast.makeText(context, "Closing action should be atleast of 5 characters", Toast.LENGTH_SHORT).show();
                    showToast("Closing action should be atleast of 5 characters");
                } else {
                    alertDialog.dismiss();
                    callgiveCAAPI(notificationID,enteredMessage,employeeID,employeeTeam);
                    //callContainmentActionAPI(notificationID,enteredMessage,employeeID,employeeTeam);

                }*/

                String enteredMessage = mComment.getText().toString().trim().replaceAll(System.getProperty("line.separator"), "");

                // showToast("Selected position is =>"+selectionPosition);
                if (TextUtils.isEmpty(enteredMessage) || enteredMessage.length() < 5) {
                    //Toast.makeText(context, "Closing action should be atleast of 5 characters", Toast.LENGTH_SHORT).show();

                    showSnackBar(NotificationsActivity.this,"Closing action should be atleast of 5 characters");
                } else {
                    alertDialog.dismiss();
                    callgiveCAAPI(notificationID,enteredMessage,selectionPosition,employeeID,employeeTeam);
                    //callContainmentActionAPI(notificationID,enteredMessage,employeeID,employeeTeam);

                }

                hideKeyBoard();
                IS_USER_INTERACTING=false;

            }
        });

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IS_USER_INTERACTING=false;
                refreshContent();
                alertDialog.dismiss();
                hideKeyBoard();

            }
        });


    }

    private void showCheckListPopup(final String notificationID, final String employeeID)
    {
        IS_USER_INTERACTING=true;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.check_list_layout, null);

        TextView mYes = dialogView.findViewById(R.id.vT_cll_yes);
        TextView mNo = dialogView.findViewById(R.id.vT_cll_no);

        builder = new AlertDialog.Builder(NotificationsActivity.this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                alertDialog.dismiss();
                callCheckListAPI(notificationID,"Yes",employeeID);
                IS_USER_INTERACTING=false;

            }
        });

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                alertDialog.dismiss();
                callCheckListAPI(notificationID,"No",employeeID);
                IS_USER_INTERACTING=false;

            }
        });

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
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        showExitDialog();
        //super.onBackPressed();
    }

    private void showExitDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.exit_dialog, null);
        Button mYes = dialogView.findViewById(R.id.vB_ed_yes);
        Button mNo = dialogView.findViewById(R.id.vB_ed_no);

        builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();

        mYes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                //vibrator.vibrate(200);
                alertDialog.dismiss();
                //freeMemory();
                NotificationsActivity.this.finish();
                finishAffinity();
            }
        });
        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("flowcheck","inside onPause()");
        if(alertDialog!=null)
        {
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        }

        if(progressDialog!=null)
        {
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
        }

        if (mSwipeRefreshLayout != null) {
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        stopTimer();
        Log.d("flowcheck","inside onStop()");
        EventBus.getDefault().unregister(this);
        hideKeyBoard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("flowcheck","inside onDestroy()");

        hideKeyBoard();

        if(alertDialog!=null)
        {
            if(alertDialog.isShowing())
            {
                alertDialog.dismiss();
            }
        }

        refreshHandler.removeCallbacksAndMessages(null);
        /*If the user is still logedin send broadcast to start service*/
        if(isLoggedIn)
        {
            Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
            sendBroadcast(broadcastIntent);
        }
        //unregisterReceiver(mybroadcast);

        freeMemory();
        // trimCache(this);
    }

    /* To Receive Event from background service*/

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(NotificationEvent event) {
        /* Do something */
        final String message=event.getMessage();
        AudioManager manager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        Log.d("receivedevent","received event=>"+message);
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

       /* //To bring back to ringing mode
        if (manager != null) {
            int valuess = 15;//range(0-15)
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, manager.getStreamMaxVolume(valuess), 0);
            manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, manager.getStreamMaxVolume(valuess), 0);
            manager.setStreamVolume(AudioManager.STREAM_ALARM, manager.getStreamMaxVolume(valuess), 0);
        }*/

        if(message.startsWith("Alert from"))
        {

            /*if (manager != null) {
               int streamMaxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                //Toast.makeText(this, Integer.toString(streamMaxVolume), Toast.LENGTH_LONG).show(); //I got 7
                manager.setStreamVolume(AudioManager.STREAM_RING, streamMaxVolume, AudioManager.FLAG_ALLOW_RINGER_MODES|AudioManager.FLAG_PLAY_SOUND);
            }*/


            this.refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!IS_USER_INTERACTING)
                    {
                        //refreshContent();
                        refreshContent();
                    }
                    refreshHandler.removeCallbacksAndMessages(null);
                    /* pushUserDetailsToServer();*/
                }
            },2000);

            NotificationClass.showNotificationToUser(NotificationsActivity.this,message);
            // pushUserDetailsToServer();

        }
        else if(message.contains("MOE"))
        {
           /* if (manager != null) {
                int streamMaxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
                //Toast.makeText(this, Integer.toString(streamMaxVolume), Toast.LENGTH_LONG).show(); //I got 7
                manager.setStreamVolume(AudioManager.STREAM_RING, streamMaxVolume, AudioManager.FLAG_ALLOW_RINGER_MODES|AudioManager.FLAG_PLAY_SOUND);
            }*/
            this.refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!IS_USER_INTERACTING)
                    {
                        // refreshContent();
                        refreshContent();
                    }

                    /*  showNotificationToUser("Containment Action done");*/
                    refreshHandler.removeCallbacksAndMessages(null);
                }
            },2000);
            //refreshContent();
            NotificationClass.showNotificationToMOE(NotificationsActivity.this,"Containment Action done");


        }
        else {
           /* if (manager != null) {
                int streamMaxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
               // Toast.makeText(this, Integer.toString(streamMaxVolume), Toast.LENGTH_LONG).show(); //I got 7
                manager.setStreamVolume(AudioManager.STREAM_RING, streamMaxVolume, AudioManager.FLAG_ALLOW_RINGER_MODES|AudioManager.FLAG_PLAY_SOUND);
            }*/

            this.refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // refreshContent();
                    if (!IS_USER_INTERACTING) {
                        // refreshContent();
                        refreshContent();
                        refreshHandler.removeCallbacksAndMessages(null);
                    }
                }
            }, 1000);

            NotificationClass.showNotificationToUser(NotificationsActivity.this);

            //To mute ringing
            if (manager != null) {
                manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }

            //To bring back to ringing mode
            if (manager != null) {
                manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }

    }


}
