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
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
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
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kodman.timesheetapp.Database.DBHandler;
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
    static String nameCalendar = "";
    static String myName = "";

    class ButtonActivity {
        String name;
        int color;
        String time = new SimpleDateFormat("HH:mm:ss").format(startDate);
        String date = new SimpleDateFormat("dd.MM.yyyy").format(startDate);
        Long ms = Calendar.getInstance().getTimeInMillis();

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


    // temporary method for List Activities
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

    }


    public void undoClick(View view)
    {
        if (MainActivity.this.listLogActivity.size() == 0) return;
        ButtonActivity ba = MainActivity.this.listLogActivity.remove(0);
        MainActivity.this.adapterListLogActivity.remove(ba);
        createActivityLog();
        // MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
        MainActivity.this.removeGoogleDiary(ba);
    }

    //------for work with Google Diary---------------------------------------------------------------
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "korovin.dmitry.1977@gmail.com";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private static boolean mTempData = true;

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    //TODO: change input parameters
    private void callCalendarApi(int action, String[] calendarData) {

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            new MakeRequestTask(mCredential, action, calendarData).execute();
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
                callCalendarApi(3, null);
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
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    callCalendarApi(3, null);
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
                        callCalendarApi(3, null);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    callCalendarApi(3, null);
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

    String mStartTime;
    String mEndTime;
    String[] mCalendarData;

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<String[], Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private int mAction;

        DBHandler mDbHandler = new DBHandler(getApplicationContext());

        MakeRequestTask(GoogleAccountCredential credential, int action, String[] calendarData) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
            mAction = action;
            mCalendarData = calendarData;
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(String[]... params) {
            try {
                switch (mAction) {
                    case 1:

                        if (isDeviceOnline()) {
                            addEventToCalendar();
                            addUnsyncedEventsToCalendar();
                        } else {
                            addEventToCalendar();
                        }
                        mTempData = false;
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
            //TODO: delete events from calendar
            mService.events().delete("primary", "eventId").execute();
            mDbHandler.deleteEventFromDb("startTime");
        }

        String mCalendarId;
        String mSummary;

        private void addEvent() throws IOException{
            mCalendarId = "mCalendarId";
            Event event = new Event().setSummary(mCalendarData[0]);
            String start = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(Long.parseLong(mStartTime));
            String end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(Long.parseLong(mEndTime));
            DateTime startDateTime = new DateTime(start);
            //TODO: make end time
            DateTime endDateTime = new DateTime(end);
            EventDateTime startTime = new EventDateTime()
                    .setDateTime(startDateTime);
            EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
            event.setStart(startTime);
            event.setEnd(endTime);
            String calendarId = "primary";
            String eventId;
            try {
                event = mService.events().insert(calendarId, event).execute();
                eventId = event.getId();
            } catch (UnknownHostException e) {
                eventId = "not_synced";
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            mDbHandler.writeOneEventToDB(mCalendarData[0], mCalendarId, eventId, mStartTime, mEndTime);
            mDbHandler.closeDB();
        }

        private void addEventToCalendar() throws IOException {
            if (mTempData) {
                //temp data
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
                mEndTime = mStartTime;
                addEvent();
            } else {
                mEndTime = mCalendarData[1];
                addEvent();
            }
            mTempData = true;
        }

        private void addUnsyncedEventsToCalendar() throws IOException {
            Cursor unsyncedEvents = mDbHandler.readUnsyncedEventFromDB();
            String startTimeDb;
            String eventNameDb;
            String endTimeDb;
            while (unsyncedEvents.moveToNext()) {
                eventNameDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("eventName"));
                startTimeDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("dateTimeStart"));
                endTimeDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("dateTimeEnd"));
                Event event = new Event().setSummary(eventNameDb);
                String start = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Long.parseLong(startTimeDb));
                DateTime startDateTime = new DateTime(start);
                //TODO: make end time
                DateTime endDateTime = new DateTime(start);
                EventDateTime startTime = new EventDateTime()
                        .setDateTime(startDateTime);
                EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
                event.setStart(startTime);
                event.setEnd(endTime);
                String calendarId = "primary";
                event = mService.events().insert(calendarId, event).execute();
                String eventId = event.getId();
                mDbHandler.deleteUnsyncedEventFromDb(startTimeDb);
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                mDbHandler.writeOneEventToDB(eventNameDb, "mCalendarId", eventId, startTimeDb, endTimeDb);
            }
            mDbHandler.closeDB();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {
        }
    }

    private void addGoogleDiary(ButtonActivity ba) {
       //Toast.makeText(MainActivity.this,
        //        "Add To Google Diary " + ba.name + " " + ba.date + "/" + ba.time + "/" + ba.ms, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Add to Google Diary");
        String calendarData[] = {ba.name, String.valueOf(ba.ms)};
        callCalendarApi(1, calendarData);
    }

    private void removeGoogleDiary(ButtonActivity ba) {
      //  Log.d(TAG, "Remove from Google Diary");
        String calendarData[] = {ba.name, ba.time, ba.date};
        callCalendarApi(2, calendarData);
    }

    //----------------End Block For Google Service------------------------------------------------------------


    //add widgets to GridLayoutSetting
    private void addToGridLayoutSettings() {
        GridLayout GL = (GridLayout) this.findViewById(R.id.gridLayoutSettings);
        GL.setColumnCount(3);
        GL.setRowCount(7);
        int rowIndex = 0, columnIndex = 0;
        for (int i = 0; i < this.listActivity.size(); i++, rowIndex++) {
            if (rowIndex >= 7) {
                columnIndex++;
                rowIndex = 0;
            }
            ButtonActivity ba = this.listActivity.get(i);
            Button btn = new Button(this);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
                }
            });
            btn.setText(ba.name);
            btn.setBackgroundColor(ba.getColor(ba.name));


            GridLayout.Spec row = GridLayout.spec(rowIndex, 1);

            GridLayout.Spec column = GridLayout.spec(columnIndex, 1);

            GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(row, column);
            GL.addView(btn, gridLayoutParam);
            GridLayout.LayoutParams lParams = (GridLayout.LayoutParams) btn.getLayoutParams();
            lParams.setMargins(3, 0, 3, 10);
        }
        Button btn = new Button(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click New Button",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btn.setText(R.string.newButton);


        GridLayout.Spec row = GridLayout.spec(rowIndex, 1);

        GridLayout.Spec column = GridLayout.spec(columnIndex, 1);

        GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(row, column);
        GL.addView(btn, gridLayoutParam);

    }


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
                                                ba.name , Toast.LENGTH_SHORT).show();
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
        callCalendarApi(3, null);
        ///-------------
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


               // String date = ba.date;
               // String time = ba.time;
                tvDate.setText(ba.date);
                tvStartTime.setText(ba.time);
                final String name = ba.name;
                final Button btnA = new Button(MainActivity.this);
                btnA.setWidth(120);
                btnA.setText(ba.name);
                btnA.setBackgroundColor(ba.getColor(ba.name));
                btnA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        createDialogForLogActivity(ba, btnA);
                    }
                });
                if (llForBA.getChildCount() == 0)
                    llForBA.addView(btnA);

                //Log.d(TAG, "Layout size = " + llForBA.getChildCount() + "  " + view.toString());

                return view;
            }
        };
        this.adapterListLogActivity.setNotifyOnChange(true);
        this.lvActivity.setAdapter(adapterListLogActivity);
        Log.d(TAG, " start setListHeigth");
        getListViewSize(lvActivity);

    }

    //initialize dialog for Buttons from Activity Log
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
                        bldr.setMessage(res.getString(R.string.delete) + "?")
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                MainActivity.this.listLogActivity.remove(ba);
                                                MainActivity.this.createActivityLog();
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
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    //change  heigth  ListView
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

    private void createScreenSettings() {
        this.setContentView(R.layout.screen_settings);
        toolbar = (Toolbar) this.findViewById(R.id.toolBar_Setting);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        this.setSupportActionBar(toolbar);
        this.addToGridLayoutSettings();
        if (!nameCalendar.equals("")) {
            EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextCalendar);
            editTextCalendar.setText(this.nameCalendar);
        }
        if (!myName.equals("")) {
            EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextName);
            editTextCalendar.setText(this.myName);
        }
    }

    public void clickSaveSettings(View view) {
        EditText editTextName = (EditText) this.findViewById(R.id.editTextName);
        EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextCalendar);
        this.nameCalendar = editTextCalendar.getText().toString();
        this.myName = editTextName.getText().toString();
        Toast.makeText(this, "Click Save:" + nameCalendar + " MYName" + myName, Toast.LENGTH_SHORT).show();
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
            //   Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                this.status = 1;
                createScreenSettings();
             //   Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_export:
                this.status = 2;
                this.setContentView(R.layout.screen_email);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);

             //   Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                this.status = 3;
                this.setContentView(R.layout.screen_share);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);
             //   Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
