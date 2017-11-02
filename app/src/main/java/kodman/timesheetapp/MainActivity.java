package kodman.timesheetapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    ArrayList<ButtonActivity> listActivity = new ArrayList<>();//All buttons activity for current  time
    ArrayList<ButtonActivity> listLogActivity = new ArrayList<>();
    ArrayAdapter<ButtonActivity> adapterListLogActivity;
    ArrayList<ButtonActivity> listSetActivity = new ArrayList<>();

    Resources res;
    ListView lvActivity;
    Time startTime = new Time();
    Date startDate = new Date();
    DateFormat df = new DateFormat();

    class ButtonActivity {
        String name;
        int color;
        String time = new SimpleDateFormat("HH:mm:ss").format(startDate);
        String date = new SimpleDateFormat("dd.MM.yyyy").format(startDate);


        public ButtonActivity() {

        }

        public ButtonActivity(String name) {

            this.name = name;
            this.color = getColor(this.name);

        }

        public ButtonActivity(String name, int color) {

            this.name = name;
            this.color = color;

        }

        private int getColor(String name) {
            // Resources res= MainActivity.this.getResources();
            if (name.equals(res.getString(R.string.nothing)))
                return res.getColor(R.color.colorNothing);
            if (name.equals(res.getString(R.string.relaxing)))
                return res.getColor(R.color.colorRelaxing);
            if (name.equals(res.getString(R.string.sleeping)))
                return res.getColor(R.color.colorSleeping);
            if (name.equals(res.getString(R.string.working)))
                return res.getColor(R.color.colorWorking);
            if (name.equals(res.getString(R.string.exercising)))
                return res.getColor(R.color.colorExercising);
            if (name.equals(res.getString(R.string.reading)))
                return res.getColor(R.color.colorReading);
            if (name.equals(res.getString(R.string.travelling)))
                return res.getColor(R.color.colorTravelling);
            if (name.equals(res.getString(R.string.eating)))
                return res.getColor(R.color.colorEating);
            if (name.equals(res.getString(R.string.washing)))
                return res.getColor(R.color.colorWashing);
            if (name.equals(res.getString(R.string.newButton)))
                return res.getColor(R.color.colorText);


            return res.getColor(R.color.colorButton);
        }

    }

    private void createList() {
        this.listActivity.add(new ButtonActivity(res.getString(R.string.nothing)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.relaxing)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.sleeping)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.working)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.exercising)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.reading)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.travelling)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.eating)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.washing)));
        //this.listActivity.add(new ButtonActivity(res.getString(R.string.newButton)) );
    }


    public void undoClick(View view) {
        ButtonActivity ba = MainActivity.this.listLogActivity.remove(0);
        MainActivity.this.adapterListLogActivity.remove(ba);
        createActivityLog();
        // MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
        getListViewSize(MainActivity.this.lvActivity);
        MainActivity.this.removeGoogleDiary(ba);
    }

    //------for work with Google Diary---------------------------------------------------------------
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void initCalendar(int action) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential, action).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                initCalendar(3);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT);
                } else {
                    initCalendar(3);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        initCalendar(3);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    initCalendar(3);
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private int mAction;

        MakeRequestTask(GoogleAccountCredential credential, int action) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
            mAction = action;
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                switch (mAction) {
                    case 1:
                        addEventToCalendar();
                        break;
                    case 0:
                        deleteEventFromCalendar();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void deleteEventFromCalendar() throws IOException {
            mService.events().delete("primary", "eventId").execute();
        }

        private void addEventToCalendar() throws IOException {
            Event event = new Event().setSummary("Google I/O 2015");
            DateTime startDateTime = new DateTime("2017-10-28T09:00:00-07:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);
            event.setEnd(start);
            String calendarId = "primary";
            event = mService.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {

        }
    }

    private void addGoogleDiary(ButtonActivity ba) {
        Toast.makeText(MainActivity.this,
                "Add To Google Diary " + ba.name + " " + ba.date + "/" + ba.time, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Add to Google Diary");
        initCalendar(1);
    }

    private void removeGoogleDiary(ButtonActivity ba) {
        Log.d(TAG, "Remove from Google Diary");
        initCalendar(2);
    }


    //----------------End Block For Google Service------------------------------------------------------------

    //add  widgets To Layout for Current Activity
    private void addToGridViewButtonsActivity() {
        GridView gv = (GridView) this.findViewById(R.id.gridView);

        ArrayAdapter<ButtonActivity> adapter =
                new ArrayAdapter<ButtonActivity>(this, R.layout.gridview_item,
                        R.id.btnItem, this.listActivity) {
                    @Override
                    public View getView(int position,
                                        View convertView, ViewGroup parent) {
                        View view = super.getView(
                                position, convertView, parent);

                        final ButtonActivity ba = this.getItem(position);

                        Button btn = (Button) view.findViewById(R.id.btnItem);
                        btn.setBackgroundColor(ba.getColor(ba.name));
                        btn.setText(ba.name);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Date date = new Date();
                                ButtonActivity BA = new ButtonActivity(ba.name);
                                BA.date = new SimpleDateFormat("dd.MM.yyyy").format(date);
                                BA.time = new SimpleDateFormat("HH:mm:ss").format(date);

                                MainActivity.this.listLogActivity.add(0, BA);
                                createActivityLog();
                                getListViewSize(MainActivity.this.lvActivity);
                                MainActivity.this.addGoogleDiary(ba);

                                Toast.makeText(MainActivity.this,
                                        "Выбран : " +
                                                ba.name + "lisLog Size=" + adapterListLogActivity.getCount(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Log.d(TAG,"getItem For GridView");
                        return view;
                    }
                };
        gv.setAdapter(adapter);
    }

    private static final String TAG = "------Activity Say";

    private Toolbar toolbar;
    private Menu menu;
    private int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        System.out.println(TAG + "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.res = this.getResources();
        //  setContentView(R.layout.screen_settings);
        toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

        //if(toolbar==null)
        //    Log.d(TAG,"NULL");
        this.setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setSubtitle("Time");

        //toolbar.setNavigationIcon(R.mipmap.ic_add_circle_white_36dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("333333333333333", "Icon Click");
            }
        });

        this.createList();
        this.addToGridViewButtonsActivity();
        this.createActivityLog();
        ///------------

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        //////-----------
        // this.lvActivity.
    }

    private void createActivityLog() {
        this.lvActivity = (ListView) this.findViewById(R.id.lvActivity);
        adapterListLogActivity = new ArrayAdapter<ButtonActivity>(this,
                R.layout.list_item, R.id.tvForDate, listLogActivity) {
            @Override
            public View getView(int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }

                View view = super.getView(position,
                        convertView, parent);

                final ButtonActivity ba = this.getItem(position);

                TextView tvDate = (TextView) view.
                        findViewById(R.id.tvForDate);

                TextView tvStartTime = (TextView) view.
                        findViewById(R.id.tvForStartTime);

                LinearLayout llForBA = (LinearLayout) view.
                        findViewById(R.id.llForBA);


                String date = ba.date;
                String time = ba.time;
                tvDate.setText(ba.date);
                tvStartTime.setText(ba.time);
                final String name = ba.name;
                final Button btnA = new Button(MainActivity.this);
                btnA.setWidth(120);
                btnA.setText(ba.name);
                btnA.setBackgroundColor(ba.getColor(ba.name));
                btnA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        createDialogForLogActivity(ba, btnA);
                    }
                });
                if (llForBA.getChildCount() == 0)
                    llForBA.addView(btnA);

                Log.d(TAG, "Layout size = " + llForBA.getChildCount() + "  " + view.toString());
                //tvYear.setText(String.valueOf(F.year));
                return view;
            }
        };
        this.adapterListLogActivity.setNotifyOnChange(true);
        this.lvActivity.setAdapter(adapterListLogActivity);
        Log.d(TAG, " start setListHeigth");
        getListViewSize(lvActivity);

    }

    //Метод для изменения Activity Log
    private void createDialogForLogActivity(final ButtonActivity ba, final Button btn) {

        // Toast.makeText(MainActivity.this,"Click "+btnA.getText(),Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, "Click " + ba.name, Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogLogActivityTitle);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.editTextDialog);
        editText.setText(ba.name);
        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ba.name = editText.getText().toString();
                        btn.setText(ba.name);
                        btn.setBackgroundColor(ba.getColor(ba.name));
                        MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                        MainActivity.this.getListViewSize(MainActivity.this.lvActivity);
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ba.name, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        AlertDialog.Builder bldr = new AlertDialog.Builder(MainActivity.this);
                        bldr.setMessage(R.string.delete + "?")
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                MainActivity.this.listLogActivity.remove(ba);
                                                MainActivity.this.createActivityLog();
                                                //  MainActivity.this.adapterListLogActivity.remove(ba);
                                                //  MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                                                //  MainActivity.this.getListViewSize(MainActivity.this.lvActivity);

                                                dlg.cancel();
                                            }
                                        })

                                .setNegativeButton(R.string.no,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                dlg.cancel();
                                            }
                                        });

                        bldr.create().show();


                        // MainActivity.this.listLogActivity.remove(ba);
                        //  MainActivity.this.adapterListLogActivity.remove(ba);
                        // MainActivity.this.createActivityLog();
                        //  MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                        // MainActivity.this.getListViewSize(MainActivity.this.lvActivity);
                        dialog.dismiss();
                    }
                });
        ;
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    //МЕтод растягивает ListView
    public static void getListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }
        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);
        // print height of adapter on log
        Log.i("height of listItem:", String.valueOf(totalHeight));
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        this.menu = menu;

        //Change colour for selected icon
        if (Build.VERSION.SDK_INT >= 21) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem mItem = this.menu.getItem(i);
                Drawable icon = mItem.getIcon();
                if (this.status == i)
                    icon.setTint(getResources().getColor(R.color.colorActiveIcon));
                else
                    icon.setTint(getResources().getColor(R.color.colorNoActiveIcon));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_home:
                this.status = 0;
                this.setContentView(R.layout.activity_main);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);
                this.addToGridViewButtonsActivity();
                this.createActivityLog();
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                this.status = 1;
                this.setContentView(R.layout.screen_settings);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_Setting);
                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);
                this.setSupportActionBar(toolbar);

                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_export:
                this.status = 2;
                this.setContentView(R.layout.screen_email);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);

                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                this.status = 3;
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
