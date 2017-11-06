package kodman.timesheetapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.SeekBar;

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
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;

import kodman.timesheetapp.Database.DBHandler;
import kodman.timesheetapp.Database.ExportDB.BaseDataHelper;
import kodman.timesheetapp.Database.ExportDB.BaseDataMaster;
import kodman.timesheetapp.Database.ExportDB.CSVWriter;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,View.OnClickListener {
    private static final String TAG = "------Activity Say";
    private Toolbar toolbar;
    private Menu menu;
    private int status = 0;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mSharedEditor;

    private ArrayList<ButtonActivity> listActivity = new ArrayList<>();//All buttons activity for current  time
    private ArrayList<ButtonActivity> listLogActivity = new ArrayList<>();
    private  ArrayAdapter<ButtonActivity> adapterListLogActivity;
    private  ArrayList<ButtonActivity> listSetActivity = new ArrayList<>();
    private  String mDeleteTime;
    private  String mUpdateTime;
    private  String mNewStartTime;
    private  Resources res;
    private  ListView lvActivity;
    Time startTime = new Time();
    private  Date startDate = new Date();
    Date currentDate = new Date();
    private  DateFormat df = new DateFormat();
    private  String mColor;
    private  static String nameCalendar = "";
    private  static String myName = "";
    private  SharedPreferences sPref;
    private   Long ms;
    static String  actualTime;


    class ButtonActivity {
        String name;
        int color;
        String time ;
        String date ;
        long ms;

        public ButtonActivity() {

        }

        public ButtonActivity(String name) {
            this.name = name;
            this.color = getColor(this.name);
            this.ms=System.currentTimeMillis();
            setDatetime();
        }

        public ButtonActivity(String name, int color) {
            this.name = name;
            this.color = color;
            this.ms=System.currentTimeMillis();

            setDatetime();
        }

        public ButtonActivity(String name, int color,long ms) {
            this.name = name;
            this.color = color;
            this.ms=ms;

            setDatetime();
        }

        public void setDatetime()
        {
            Date startDate=new Date(this.ms);
            this.date= new SimpleDateFormat("dd.MM.yyyy").format(startDate);
            this.time=new SimpleDateFormat("HH:mm:ss").format(startDate);
        }
        private int getColor(String name) {

            if (name.equals(res.getString(R.string.nothing)))
                this.color = res.getColor(R.color.colorNothing);

            if (name.equals(res.getString(R.string.relaxing)))
                this.color = res.getColor(R.color.colorRelaxing);
            if (name.equals(res.getString(R.string.sleeping)))
                this.color = res.getColor(R.color.colorSleeping);
            if (name.equals(res.getString(R.string.working)))
                this.color = res.getColor(R.color.colorWorking);
            if (name.equals(res.getString(R.string.exercising)))
                this.color = res.getColor(R.color.colorExercising);
            if (name.equals(res.getString(R.string.reading)))
                this.color = res.getColor(R.color.colorReading);
            if (name.equals(res.getString(R.string.travelling)))
                this.color = res.getColor(R.color.colorTravelling);
            if (name.equals(res.getString(R.string.eating)))
                this.color = res.getColor(R.color.colorEating);
            if (name.equals(res.getString(R.string.washing)))
                this.color = res.getColor(R.color.colorWashing);
            if (name.equals(res.getString(R.string.newButton)))
                this.color = res.getColor(R.color.colorText);

            return this.color;
        }

    }


    //  for create default List Activities
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


    //
    public static int getContrastColor(int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? Color.BLACK : Color.WHITE;
    }


    public void undoClick(View view) {
        if (MainActivity.this.listLogActivity.size() == 0) return;
        ButtonActivity ba = MainActivity.this.listLogActivity.get(0);
        try {
            mDeleteTime = String.valueOf(ba.ms);
            MainActivity.this.removeGoogleDiary();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        MainActivity.this.adapterListLogActivity.remove(ba);
        createActivityLog();
    }

    //------for work with Google Diary---------------------------------------------------------------
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "is.karpus@gmail.com";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private static boolean mTempData = true;
    private String mNewSummary;
    private String mNewColor;

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    //TODO: change input parameters
    private void callCalendarApi(int action) {
        sPref = this.getPreferences(MODE_PRIVATE);
        mCalendarId = sPref.getString("myCalendar", "primary");
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
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
                callCalendarApi(3);
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
                    callCalendarApi(3);
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
                        callCalendarApi(3);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    callCalendarApi(3);
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

    String mCalendarId;
    String mCalendarData[] = {null, null, null};
    String mStartTime;
    String mEndTime;

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<String[], Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private int mAction;
        private String mSummary;
        private DBHandler mDbHandler = new DBHandler(getApplicationContext());


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
        protected Void doInBackground(String[]... params) {
            try {

                switch (mAction) {
                    case 4:
                        updateEventNameColor(mStartTime);
                        break;
                    case 3:
                        //do nothing. calendar service initialisation
                        break;
                    case 2:
                        updateEventTime();
                        break;
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
                        deleteEventFromCalendar(mDeleteTime);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void deleteEventFromCalendar(String startTime) throws IOException {
            try {
                ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
                mService.events().delete(mCalendarId, arrayList.get(1)).execute();
                mDbHandler.deleteEventFromDb(startTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateEventTime() throws IOException {
            updateEventStartTime(mUpdateTime);
        }

        boolean mIdFlag = false;

        private void addEvent() throws IOException {
            Event event = new Event().setSummary(mSummary);
            Date start = new Date(Long.parseLong(mStartTime));
            Date end = new Date(Long.parseLong(mEndTime));
            DateTime startDateTime = new DateTime(start, TimeZone.getTimeZone("UTC"));
            DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
            EventDateTime startTime = new EventDateTime()
                    .setDateTime(startDateTime);
            EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
            event.setStart(startTime);
            event.setEnd(endTime);
            String eventId;
            if (isDeviceOnline()) {
                String pageToken = null;
                do {
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();
                    for (CalendarListEntry calendarListEntry : items) {
                        if (mCalendarId.equals(calendarListEntry.getSummary()) || mCalendarId.equals(calendarListEntry.getId())) {
                            mCalendarId = calendarListEntry.getId();
                            mIdFlag = true;
                            break;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
                if (!mIdFlag) {
                    mCalendarId = "primary";
                }
                event = mService.events().insert(mCalendarId, event).execute();
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                eventId = event.getId();
                mDbHandler.writeOneEventToDB(mSummary, mCalendarId, eventId, mStartTime, mEndTime, mColor);
                mDbHandler.closeDB();
            } else {
                eventId = "not_synced";
                mDbHandler.writeOneEventToDB(mSummary, mCalendarId, eventId, mStartTime, mEndTime, mColor);
                mDbHandler.closeDB();
            }
            mTempData = false;
        }

        private void addEventToCalendar() throws IOException {
            if (mTempData) {
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
                mColor = mCalendarData[2];
                mEndTime = mStartTime;
                addEvent();
                mTempData = false;
            } else {
                mEndTime = mCalendarData[1];
                updateEvent();
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
                mEndTime = mStartTime;
                addEvent();
                mTempData = true;
            }
        }

        private void updateEvent() throws IOException {
            ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(mStartTime);
            if (arrayList.size() == 0) {
                mTempData = true;
                return;
            }

            String eventId = arrayList.get(1);
            String calendarId = arrayList.get(0);
            // Retrieve the event from the API
            if (isDeviceOnline()) {
                Event event = mService.events().get(calendarId, eventId).execute();
                // Make a change
                Date end = new Date(Long.parseLong(mEndTime));
                DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
                EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
                event.setEnd(endTime);
                // Update the event


                event = mService.events().update(calendarId, event.getId(), event).execute();
                System.out.printf("Event end time updated: %s\n", event.getHtmlLink());
                mDbHandler.updateEvent(mStartTime, mEndTime, eventId);
                mDbHandler.closeDB();
            } else {
                eventId = "not_synced";
                mDbHandler.updateEvent(mStartTime, mEndTime, eventId);
                mDbHandler.closeDB();
            }
        }

        private void updateEventStartTime(String startTime) throws IOException {
            String newStartTime = mNewStartTime;
            Log.e("startTime", startTime);
            Log.e("newstartTime", mNewStartTime);
            ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
            String eventId = arrayList.get(1);
            String calendarId = arrayList.get(0);
            // Retrieve the event from the API
            Event event = mService.events().get(calendarId, eventId).execute();
            // Make a change
            Date start = new Date(Long.parseLong(newStartTime));
            DateTime startDateTime = new DateTime(start, TimeZone.getTimeZone("UTC"));
            EventDateTime endTime = new EventDateTime().setDateTime(startDateTime);
            event.setEnd(endTime);
            // Update the event
            if (isDeviceOnline()) {
                event = mService.events().update(calendarId, event.getId(), event).execute();
                System.out.printf("Event time update: %s\n", event.getHtmlLink());
                mDbHandler.updateEventStartTime(startTime, mNewStartTime, eventId);
                mDbHandler.closeDB();
            } else {
                eventId = "not_synced";
                mDbHandler.updateEventStartTime(startTime, mNewStartTime, eventId);
                mDbHandler.closeDB();
            }
        }

        private void updateEventNameColor(String startTime) throws IOException {
            String newSummary = mNewSummary;
            String newColor = mNewColor;
            ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
            String eventId = arrayList.get(1);
            String calendarId = arrayList.get(0);
            // Retrieve the event from the API
            Event event = mService.events().get(calendarId, eventId).execute();
            // Make a change
            event.setSummary(newSummary);
            // Update the event
            if (isDeviceOnline()) {
                event = mService.events().update(calendarId, event.getId(), event).execute();
                System.out.printf("Event name/color update: %s\n", event.getHtmlLink());
                mDbHandler.updateEventNameColor(startTime, newSummary, newColor, eventId);
                mDbHandler.closeDB();
            } else {
                eventId = "not_synced";
                mDbHandler.updateEventNameColor(startTime, newSummary, newColor, eventId);
                mDbHandler.closeDB();
            }
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
                String pageToken = null;
                do {
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();
                    for (CalendarListEntry calendarListEntry : items) {
                        if (mCalendarId.equals(calendarListEntry.getSummary()) || mCalendarId.equals(calendarListEntry.getId())) {
                            mCalendarId = calendarListEntry.getId();
                            mIdFlag = true;
                            break;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
                if (!mIdFlag) {
                    mCalendarId = "primary";
                }
                Event event = new Event().setSummary(eventNameDb);
                String start = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Long.parseLong(startTimeDb));
                DateTime startDateTime = new DateTime(start);
                DateTime endDateTime = new DateTime(start);
                EventDateTime startTime = new EventDateTime()
                        .setDateTime(startDateTime);
                EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
                event.setStart(startTime);
                event.setEnd(endTime);
                event = mService.events().insert(mCalendarId, event).execute();
                String eventId = event.getId();
                mDbHandler.deleteUnsyncedEventFromDb(startTimeDb);
                System.out.printf("Event synced: %s\n", event.getHtmlLink());
                mDbHandler.writeOneEventToDB(eventNameDb, mCalendarId, eventId, startTimeDb, endTimeDb, mColor);
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

    //Add to Google diary
    private void addGoogleDiary(ButtonActivity ba) {
        //Toast.makeText(MainActivity.this,
        //        "Add To Google Diary " + ba.name + " " + ba.date + "/" + ba.time + "/" + ba.ms, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Add to Google Diary");
        mCalendarData[0] = ba.name;
        mCalendarData[1] = String.valueOf(ms);
        mCalendarData[2] = String.valueOf(ba.getColor(ba.name));
        callCalendarApi(1);
    }

    private void updateGoogleDiary() {
        //  Log.d(TAG, "Update from Google Diary");
        callCalendarApi(2);
    }

    //Delete From Google Diary
    private void removeGoogleDiary() {
        //  Log.d(TAG, "Remove from Google Diary");
        callCalendarApi(0);
    }

    //----------------End Block For Google Service------------------------------------------------------------


    //add widgets to GridLayoutSetting
    private void addToGridLayoutSettings() {
        GridLayout GL = (GridLayout) this.findViewById(R.id.gridLayoutSettings);
        if (GL.getChildCount() > 0) {
            Toast.makeText(MainActivity.this, "Count listActivity=" + this.listActivity.size(),
                    Toast.LENGTH_SHORT).show();
            GL.removeViews(0, GL.getChildCount());
            Toast.makeText(MainActivity.this, "Count after remove =" + GL.getChildCount(),
                    Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "Count =" + GL.getChildCount(),
                Toast.LENGTH_SHORT).show();
        int rowIndex = 0, columnIndex = 0;
        for (int i = 0; i < this.listActivity.size(); i++, rowIndex++) {

            if (rowIndex >= 7) {
                columnIndex++;
                rowIndex = 0;
            }
            final ButtonActivity ba = this.listActivity.get(i);
            final Button btn = new Button(this);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(res.getString(R.string.deleteActivity))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.listActivity.remove(ba);
                                    addToGridLayoutSettings();
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).setCancelable(true);

                    builder.create().show();
                    // Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
                }
            });
            btn.setText(ba.name);
            btn.setBackgroundColor(ba.getColor(ba.name));
            btn.setTextColor(MainActivity.this.getContrastColor(ba.color));

            //Insert the Button in defined position
            GridLayout.Spec row = GridLayout.spec(rowIndex, 1);
            GridLayout.Spec column = GridLayout.spec(columnIndex, 1);
            GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(row, column);
            GL.addView(btn, gridLayoutParam);

            GridLayout.LayoutParams lParams = (GridLayout.LayoutParams) btn.getLayoutParams();
            lParams.setMargins(3, 0, 3, 10);
        }

        if (GL.getChildCount() >= 21) return;

        Button btn = new Button(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.clickNewButton(v);
            }
        });

        if (rowIndex >= 7) {
            columnIndex++;
            rowIndex = 0;
        }
        btn.setText(R.string.newButton);


        GridLayout.Spec row = GridLayout.spec(rowIndex, 1);
        GridLayout.Spec column = GridLayout.spec(columnIndex, 1);
        GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(row, column);
        GL.addView(btn, gridLayoutParam);

    }


    //------For click on button +New
    private void clickNewButton(final View v) {

        Toast.makeText(MainActivity.this, "Click New Button",
                Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogNewButton);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog, null);
        builder.setView(view)
                .setPositiveButton("Create new button", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        EditText editText = (EditText) view.findViewById(R.id.editTextDialog);
                        String nameButton = editText.getText().toString();
                        if (nameButton.length() > 20) {
                            Toast.makeText(MainActivity.this, "Maximum  characters in name is 20",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            clickNewButton(v);
                        }
                        if (!MainActivity.this.uniqueButtonActivity(nameButton)) {
                            Toast.makeText(MainActivity.this, "Maximum  characters in name is 20",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            clickNewButton(v);
                        } else {
                            final ButtonActivity ba = new ButtonActivity(nameButton);
                            ba.name = nameButton;
                            setColorFromDialog(ba);
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "create",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                /*
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder bldr = new AlertDialog.Builder(MainActivity.this);
                        bldr.setMessage(res.getString(R.string.delete) + "?")
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                //  MainActivity.this.listLogActivity.remove(ba);
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
                */
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    //--------------------for color---------
    private void setColorFromDialog(final ButtonActivity ba) {
        ba.color = Color.RED;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choise the color");
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.color_blender, null);
        builder.setView(view);

        final SeekBar fSeekBar = (SeekBar) view.findViewById(R.id.fSeekBar);
        final SeekBar sSeekBar = (SeekBar) view.findViewById(R.id.sSeekBar);

        final ImageView sColor = (ImageView) view.findViewById(R.id.imageView4);
        final ImageView fColor = (ImageView) view.findViewById(R.id.imageView3);
        final ImageView rColor = (ImageView) view.findViewById(R.id.imageView5);


        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {

                int color1[] = calcColor(fSeekBar.getProgress());
                int color2[] = calcColor(sSeekBar.getProgress());
                int colorR[] = calcColor(color1, color2);
                switch (seekBar.getId()) {
                    case R.id.fSeekBar: {
                        fColor.setBackgroundColor(Color.rgb(color1[0], color1[1], color1[2]));
                        break;
                    }
                    case R.id.sSeekBar: {
                        sColor.setBackgroundColor(Color.rgb(color2[0], color2[1], color2[2]));
                        break;
                    }
                }
                fColor.invalidate();
                rColor.setBackgroundColor(Color.rgb(colorR[0], colorR[1], colorR[2]));
                rColor.invalidate();
                ba.color = Color.rgb(colorR[0], colorR[1], colorR[2]);
                Toast.makeText(MainActivity.this, "returnColor =", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        };

        fSeekBar.setOnSeekBarChangeListener(listener);
        sSeekBar.setOnSeekBarChangeListener(listener);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ba.color = Color.WHITE;
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MainActivity.this.listActivity.add(ba);
                MainActivity.this.addToGridLayoutSettings();
                //Toast.makeText(MainActivity.this, "Click Save", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    int Red, Green, Blue;

    public int[] calcColor(int progress) {
        if (progress == 0) {
            Red = 255;
            Green = 0;
            Blue = 0;
        }
        if (progress > 0 & progress <= 42) {
            Red = 255;
            Green = progress * 6 - 1;
            Blue = 0;
        } else if (progress > 42 & progress <= 84) {
            Red = 256 - (progress - 42) * 6 - 1;
            Green = 255;
            Blue = 0;
        } else if (progress > 84 & progress <= 126) {
            Red = 0;
            Green = 255;
            Blue = (progress - 84) * 6 - 1;
        } else if (progress > 126 & progress <= 168) {
            Red = 0;
            Green = 256 - (progress - 126) * 6 - 1;
            Blue = 255;
        } else if (progress > 168 & progress <= 210) {
            Red = (progress - 168) * 6 - 1;
            Green = 0;
            Blue = 255;
        } else if (progress > 210 & progress <= 252) {
            Red = 255;
            Green = 0;
            Blue = 256 - (progress - 210) * 6 - 1;
        }
        int[] color = {Red, Green, Blue};
        return color;
    }

    public int[] calcColor(int[] color1, int[] color2) {
        int[] color = {(color1[0] + color2[0]) / 2, (color1[1] + color2[1]) / 2, (color1[2] + color2[2]) / 2};
        return color;
    }
    /////------End color----------


    // learn the uniqueness of the name
    private boolean uniqueButtonActivity(String name) {
        for (ButtonActivity ba : MainActivity.this.listActivity) {
            if (ba.name.toUpperCase().equals(name.toUpperCase()))
                return false;
        }
        return true;
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
                        btn.setTextColor(MainActivity.this.getContrastColor(ba.color));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Date date = new Date();
                                ButtonActivity BA = new ButtonActivity(ba.name);
                                BA.date = new SimpleDateFormat("dd.MM.yyyy").format(date);
                                BA.time = new SimpleDateFormat("HH:mm:ss").format(date);
                                ms = System.currentTimeMillis();
                                BA.ms = ms;
                                MainActivity.this.listLogActivity.add(0, BA);
                                createActivityLog();
                                getListViewSize(MainActivity.this.lvActivity);
                                MainActivity.this.addGoogleDiary(ba);
                                Toast.makeText(MainActivity.this,
                                        ba.name, Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Log.d(TAG,"getItem For GridView");
                        return view;
                    }
                };
        gv.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        System.out.println(TAG + "onCreate");
        super.onCreate(savedInstanceState);
        mShared = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        mSharedEditor = mShared.edit();
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.res = this.getResources();
        toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);
        this.setSupportActionBar(toolbar);


        //---Read from SharedPreferences ------------
        sPref = this.getPreferences(MODE_PRIVATE);
        // Name & Calendar User
        this.myName = sPref.getString("myName", "");
        this.nameCalendar = sPref.getString("myCalendar", "");


        Log.d(TAG, "OnCREATE Initialzed list Activities");
        //Initialzed list Activities
        if (sPref.contains("sizeListActivity") && this.listActivity.size() == 0) {


            int size = sPref.getInt("sizeListActivity", 0);

            Log.d(TAG, " Initialzed list Activities From shareds =" + size);
            //   this.listActivity=new ArrayList<>();
            for (int i = 0; i < size; i++) {
                this.listActivity.add(new ButtonActivity
                        (sPref.getString("buttonAcivityName" + i, ""),
                                sPref.getInt("buttonAcivityColor" + i, res.getColor(R.color.colorText))));
            }


        } else {
            this.createList();
        }
        if(this.listActivity.size() == 0)
          this.createList();
        //------------------------------------------------------------------


        this.addToGridViewButtonsActivity();

        ///------------

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        callCalendarApi(3);
        ///-------------

//Read From DataBase
        addAcivitiesFromDB();
        this.createActivityLog();
/*
For actual time, update every 1000 ms
 */
        new java.util.Timer().schedule
                (
                        new TimerTask() {
                            public void run() {
                                if(toolbar==null) return;
                                toolbar.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Date curDate = new Date();
                                        String time = new SimpleDateFormat("HH:mm:ss").format(curDate);
                                       MainActivity.actualTime=time;
                                        toolbar.setTitle(time);
                                        if (MainActivity.this.listLogActivity.size() > 0 && MainActivity.this.status == 0) {
                                            long timeDiff = System.currentTimeMillis() - MainActivity.this.listLogActivity.get(0).ms;
                                            Date moment = new Date(timeDiff);
                                            time = new SimpleDateFormat("mm:ss").format(moment) + " min:sec";
                                            ((TextView) MainActivity.this.findViewById(R.id.tvLastLap)).setText(time);
                                        }

                                    }
                                });

                                // Log.d(TAG,"Timer Tick");
                            }
                        },
                        0, 1000);

    }

    @Override
    public void onDestroy() {
        SharedPreferences.Editor ed = sPref.edit();
        int size = this.listActivity.size();
        Log.d(TAG, "DESTROY SIZE=" + size);
        ed.putInt("sizeListActivity", size);
        for (int i = 0; i < size; i++) {
            ed.putString("buttonAcivityName" + i, this.listActivity.get(i).name);
            ed.putInt("buttonAcivityColor" + i, this.listActivity.get(i).color);
        }
        ed.commit();
        this.listActivity.clear();
        super.onDestroy();
    }

    private void createActivityLog() {
        if (listActivity.size() == 0) return;
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

                tvStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(MainActivity.this, ba.name + "ms = " + ba.ms, Toast.LENGTH_SHORT).show();
                        changeTimeActivity(ba);
                    }
                });

                LinearLayout llForBA = (LinearLayout) view.
                        findViewById(R.id.llForBA);

                tvDate.setText(ba.date);
                tvStartTime.setText(ba.time);
                final String name = ba.name;
                final Button btnA = new Button(MainActivity.this);
                btnA.setWidth(120);
                btnA.setText(ba.name);
                btnA.setBackgroundColor(ba.getColor(ba.name));
                btnA.setTextColor(MainActivity.this.getContrastColor(ba.color));
                btnA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Click name= " + ba.name, Toast.LENGTH_SHORT).show();
                        createDialogForLogActivity(ba, btnA);
                    }
                });
                if (llForBA.getChildCount() == 0)
                    llForBA.addView(btnA);

                return view;
            }
        };
        this.adapterListLogActivity.setNotifyOnChange(true);
        this.lvActivity.setAdapter(adapterListLogActivity);
        Log.d(TAG, " start setListHeigth");
        getListViewSize(lvActivity);

    }

    // changing the time for activity
    private void changeTimeActivity(final ButtonActivity ba) {
        //final Date date= new Date(ba.ms);
        final Date date = new Date();

        final TimePickerDialog TPD = new TimePickerDialog(this,
                null, date.getHours(), date.getMinutes(), true) {
            @Override
            public void onTimeChanged(TimePicker view,
                                      int hour, int minute) {
                long currentTime = System.currentTimeMillis();

                Date curDate = new Date(currentTime);
                int curHour = curDate.getHours();
                int curMinutes = curDate.getMinutes();

                Date lastDate = new Date(ba.ms);
                int lastHour = lastDate.getHours();
                int lastMinutes = lastDate.getMinutes();
                Log.d(TAG, "al=" + hour + ":" + minute + "  last=" + lastHour + ":" + lastMinutes + "  cur=" + curHour + ":" + curMinutes);
                //Toast.makeText(MainActivity.this,"curTime="+new Date(currentTime)+"  |selectedTime="+new Date(selectedTime),Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this,"curTime="+currentTime+"  |selectedTime="+selectedTime,Toast.LENGTH_SHORT).show();
                if (hour > curHour || (hour == curHour && minute > curMinutes) || hour < lastHour || (hour == lastHour && minute < lastMinutes)) {
                    Toast.makeText(MainActivity.this, "The selected time is not valid for selection", Toast.LENGTH_SHORT).show();

                    cancel();
                    changeTimeActivity(ba);
                } else {
                    date.setHours(hour);
                    date.setMinutes(minute);
                    Toast.makeText(MainActivity.this, "Ok Change time " + date.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        TPD.setButton(DialogInterface.BUTTON_POSITIVE,
                "Save", new DialogInterface.
                        OnClickListener() {
                    @Override
                    public void onClick(DialogInterface
                                                dialog, int which) {
                        Log.e("UPDATE!!", "sf");
                        ba.time = new SimpleDateFormat("HH:mm:ss").format(date);
                        ba.date = new SimpleDateFormat("dd.MM.yyyy").format(date);
                        Toast.makeText(MainActivity.this,
                                "Выбранное время чч:мм : " + date.toString(),
                                Toast.LENGTH_LONG).show();

                        mUpdateTime = String.valueOf(ba.ms);
                        ba.ms = date.getTime();
                        mNewStartTime = String.valueOf(ba.ms);
                        updateGoogleDiary();
                        createActivityLog();
                    }
                });

        TPD.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel", new DialogInterface.
                        OnClickListener() {
                    @Override
                    public void onClick(DialogInterface
                                                dialog, int which) {

                    }
                });
        TPD.show();

    }

    //initialize dialog for Buttons from Activity Log
    private void createDialogForLogActivity(final ButtonActivity ba, final Button btn) {


        int size = MainActivity.this.listActivity.size();
        final String[] itemsAcivities = new String[size];
        for (int i = 0; i < size; i++) {
            itemsAcivities[i] = MainActivity.this.listActivity.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogLogActivityTitle);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_with_list, null);
        //  final TextView tvButtonAcivity=(TextView)view.findViewById(R.id.tvNameButtonAcivity);
        // tvButtonAcivity.setText(ba.name);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinnerAcivityLog);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.
                        simple_spinner_item, itemsAcivities);
        spinner.setAdapter(adapter);


        //helper class for data transfer
        class Swap {
            int flag = 0;
            int color;
        }
        ;
        final Swap s = new Swap();

        adapter.setDropDownViewResource(android.R.layout.
                simple_spinner_dropdown_item);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {

                if (s.flag == 0) {
                    for (int i = 0; i < parent.getCount(); i++) {
                        if (itemsAcivities[i].toUpperCase().equals(ba.name.toUpperCase())) {
                            parent.setSelection(i);
                            break;
                        }

                    }
                    s.flag = 1;
                } else {
                    ba.name = itemsAcivities[position];
                    s.color = listActivity.get(position).color;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?>
                                                  parent) {

            }
        });


        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // ba.name=tvButtonAcivity.getText().toString();
                        btn.setText(ba.name);
                        ba.color = s.color;
                        btn.setBackgroundColor(ba.color);
                        mNewColor = String.valueOf(ba.color);
                        mNewSummary = ba.name;
                        mUpdateTime = String.valueOf(ba.ms);
                        callCalendarApi(4);
                        btn.setTextColor(MainActivity.this.getContrastColor(ba.color));

                        createActivityLog();
                        Toast.makeText(MainActivity.this, "Change" + spinner.getItemAtPosition(0).toString() + "now Name=" + ba.name, Toast.LENGTH_SHORT).show();

                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ba.name = btn.getText().toString();
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
                                                mDeleteTime = String.valueOf(ba.ms);
                                                callCalendarApi(0);
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
        toolbar.setTitle(MainActivity.actualTime);
        this.setSupportActionBar(toolbar);
        this.addToGridLayoutSettings();

        EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextCalendar);
        editTextCalendar.setText(sPref.getString("myCalendar", ""));

        EditText editTextName = (EditText) this.findViewById(R.id.editTextName);
        editTextName.setText(sPref.getString("myName", ""));

    }

    // Saved Data From Field MyName & My Calendar
    public void clickSaveSettings(View view) {
        sPref = this.getPreferences(MODE_PRIVATE);

        EditText editTextName = (EditText) this.findViewById(R.id.editTextName);
        EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextCalendar);
        Toast.makeText(this, "Click Save:" + nameCalendar + " MYName" + myName, Toast.LENGTH_SHORT).show();
        this.nameCalendar = editTextCalendar.getText().toString();
        this.myName = editTextName.getText().toString();
        if (nameCalendar.equals("") || myName.equals("")) {
            Toast.makeText(this, "Fill in all the fields ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nameCalendar.length() > 40) {
            this.nameCalendar = "";
            Toast.makeText(this, "Maximum 40 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (myName.length() > 20) {
            this.myName = "";
            Toast.makeText(this, "Maximum 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("myName", this.myName);
        ed.putString("myCalendar", this.nameCalendar);
        ed.commit();
        Toast.makeText(this, "Click Save:" + nameCalendar + " Name :" + myName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_home:
                this.status = 0;
                this.setContentView(R.layout.activity_main);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitle( MainActivity.actualTime);
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
               // this.setContentView(R.layout.screen_email);
               // toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

               // toolbar.setTitle(MainActivity.actualTime);
               // this.setSupportActionBar(toolbar);
                createScreenEmail();
                //   Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                this.status = 3;
                this.setContentView(R.layout.screen_share);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitle(MainActivity.actualTime);
                this.setSupportActionBar(toolbar);
                String appPackageName = "kodman.timesheetapp";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                //   Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //------For event onClick button Clean Log --
    public void clickCleanLog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogCleanLog)
                .setCancelable(false)
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MainActivity.this.listLogActivity.clear();
                        //   MainActivity.this.createActivityLog();
                        clearLogActivityFromDB();
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    //Add from DB Activities in AcivityLog
    private void addAcivitiesFromDB()
    {
       // String query = "SELECT * FROM calendarTable";
        DBHandler mDbHandler = new DBHandler(getApplicationContext());

        Cursor cursor =mDbHandler.readAllEventsFromDB();
        int count =cursor.getColumnCount();
        String msg="CalendarTable= ";
        for(int i=0;i<count;i++)
        {
            msg+=" | "+cursor.getColumnName(i);
        }
        Log.d(TAG,"Count = "+count+"   : "+msg);

        while(cursor.moveToNext())
        {

            String id=cursor.getString(cursor.getColumnIndex("_id"));
           String name=cursor.getString(cursor.getColumnIndex("eventName"));

            long startTime=Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));
            long endTime=Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));
         //   String color=cursor.getString(6);
            int color;
            try{
              color=Integer.parseInt(cursor.getString(cursor.getColumnIndex("color")));
            }
            catch(Exception ex)
            {
                 color=Color.WHITE;
                Log.d(TAG,"EXCEPTION  Id:"+id+"Name = "+name+"Color = "+color+"start = "+startTime+"end = "+endTime );
            }
            Log.d(TAG,"Id:"+id+"Name = "+name+"Color = "+color+"start = "+startTime+"end = "+endTime );

            ButtonActivity ba=new ButtonActivity(name,color);
            ba.ms=startTime;
            ba.setDatetime();
            this.listLogActivity.add(0,ba);
        }
        cursor.close();

    }

    //Clear all from  DataBase
    private void clearLogActivityFromDB() {

        DBHandler mDbHandler = new DBHandler(getApplicationContext());
        mDbHandler.clearEventsTable();
        Log.d(TAG,"Clean DB");
        // Toast.makeText(this, "Clean Log From DB", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }





    /*
    START EMAIL_SCREEN+++++++++++++++++++++++++++++++++++++++++++
     */
        private static final String LOG_TAG = "FragmentExport";
  private static final int DIALOG_DATE = 1;

    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String END = "end";
    private static final String START = "start";
    private String dataKey;
    // Хранит последние данные введенные пользователем в соответсвующие поля
    private String latestEmail;
    private String latestSubject;
    private String latestMessage;
    private EditText emailEt;
    private EditText subjectEt;
  private EditText messageEt;
    private TextView startData;
    private TextView endData;
    private ImageButton startDCalendar;
    private ImageButton endDCalendar;
    private Button sendEmail;
    private ListView includeLv;
    ArrayList<ButtonActivity> listActivityToSend;
    // Хранит данные выбранного периода отправки пользовательских данных
    private HashMap<String, Integer> userDataMap;

    // Хранит последние данные введенные польхователем в поля
    private HashMap<String, String> lastUserEmailData;

    private void createScreenEmail()
    {
        this.setContentView(R.layout.screen_email);
        toolbar = (Toolbar)this.findViewById(R.id.toolBar_screen_email);
        toolbar.setTitle(MainActivity.actualTime);
        this.setSupportActionBar(toolbar);
        setupUI();
    }


//    private Context fContext;

//
private void setupUI() {
    toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);
    emailEt = (EditText) this.findViewById(R.id.fe_email_et);
    subjectEt = (EditText) this.findViewById(R.id.fe_subject_et);
    messageEt = (EditText) this.findViewById(R.id.fe_message_et5);
    startData = (TextView) this.findViewById(R.id.fe_calendar_startd_tv);
    endData = (TextView) this.findViewById(R.id.fe_calendar_endd_tv);
    startDCalendar = (ImageButton)this.findViewById(R.id.fe_calendar_startd_ib);
    endDCalendar = (ImageButton)this.findViewById(R.id.fe_calendar_endd_ib);
    sendEmail = (Button) this.findViewById(R.id.fe_send_email_bt);
    includeLv = (ListView) this.findViewById(R.id.include_lv);

    startData.setOnClickListener(this);
    endData.setOnClickListener(this);
    startDCalendar.setOnClickListener(this);
    endDCalendar.setOnClickListener(this);
    sendEmail.setOnClickListener(this);

    updateUI();
}

    private void updateUI() {
        BaseDataMaster baseDataMaster = BaseDataMaster.getDataMaster(MainActivity.this);
        if (lastUserEmailData == null) {
            lastUserEmailData = baseDataMaster.getEmailData();
        }

        // Если пользовватель уже вводил данные, автоматически зополняем поля
        if (!baseDataMaster.getEmailData().isEmpty()) {
            lastUserEmailData = baseDataMaster.getEmailData();
            emailEt.setText(lastUserEmailData.get(BaseDataHelper.User.EMAIL));
            subjectEt.setText(lastUserEmailData.get(BaseDataHelper.User.SUBJECT));
            messageEt.setText(lastUserEmailData.get(BaseDataHelper.User.MESSAGE));
        }

        // Устанваливаем в textView первый и последний день предыдущего месяца
        Calendar calendarlast = Calendar.getInstance();
        calendarlast.add(Calendar.MONTH, 0);
        calendarlast.set(Calendar.DAY_OF_MONTH, 1);
        calendarlast.add(Calendar.DATE, -1);
        Date lastDayOfMonth = calendarlast.getTime();

        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.add(Calendar.MONTH, 0);
        calendarlast.set(Calendar.DAY_OF_MONTH, 1);
        calendarlast.add(Calendar.DATE, -1);
        java.text.DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        startData.setText("01/" + calendarFirst.get(Calendar.MONTH) + "/" + calendarFirst.get(Calendar.YEAR));
        endData.setText(sdf.format(lastDayOfMonth));

        showIncludeItems();
    }
    private void showIncludeItems() {

       // listActivity = new ArrayList<>();

//        DBHandler dbHandler = new DBHandler(MainActivity.this);
//        Cursor cursor = dbHandler.readActivitiesFromDB();
//
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            listActivity.add(new ButtonActivity(cursor.getString(1)));
//            cursor.moveToNext();
//            Log.d(LOG_TAG, "getActivities");
//        }
//        cursor.close();
        listActivityToSend = listActivity;
        CustomArrayAdapter arrayListArrayAdapter = new CustomArrayAdapter(MainActivity.this, listActivity);

        includeLv.setAdapter(arrayListArrayAdapter);
    }

    public class CustomArrayAdapter extends ArrayAdapter<ArrayList<ButtonActivity>> {

        public CustomArrayAdapter(Context context, ArrayList values) {
            super(context, R.layout.list_item, values);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.include_activity_item, null);
            }
            TextView textView = (TextView) v.findViewById(R.id.ia_action_name);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.ia_checkbox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    // Если пользоваетель снял галочку, удаляем активность из спика на отправку

                    if (!compoundButton.isChecked()) {
                        for (ButtonActivity bt : listActivityToSend) {
                            if (bt.name.equals(listActivity.get(position).name)) {
                                listActivityToSend.remove(position);
                            }
                        }
                    } else {
                        listActivityToSend.add(listActivity.get(position));
                    }

                }
            });
            ButtonActivity activity = listActivity.get(position);
            textView.setText(activity.name);
            textView.setBackgroundColor(activity.color);
            return v;
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(MainActivity.this,"Click",Toast.LENGTH_SHORT).show();
        // Получаем id вызванной view
        int view_id = view.getId();
        switch (view_id) {
            case R.id.fe_calendar_endd_ib:
                Log.d(LOG_TAG, "endDate_et");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_endd_tv:
                Log.d(LOG_TAG, "endDate_ib");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_startd_tv:
                Log.d(LOG_TAG, "startDate_et");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_startd_ib:
                Log.d(LOG_TAG, "startDate_ib");
                openCalendar(view_id);
                break;
            case R.id.fe_send_email_bt:
                sendUserData();
                break;
        }
    }

    private void sendUserData() {

        BaseDataMaster baseDataMaster = BaseDataMaster.getDataMaster(MainActivity.this);
        if (lastUserEmailData == null || lastUserEmailData.size() == 0) {
            lastUserEmailData = baseDataMaster.getEmailData();
        }

        // Сохраняем данные введенные пользователем в переменные
        latestEmail = emailEt.getText().toString();
        latestSubject = subjectEt.getText().toString();
        latestMessage = messageEt.getText().toString();

        // Если пользователь менял последние сохраненные данные, обновзяем их
        if (!latestEmail.equals(lastUserEmailData.get(BaseDataHelper.User.EMAIL))
                || !latestSubject.equals(lastUserEmailData.get(BaseDataHelper.User.SUBJECT))
                || !latestMessage.equals(lastUserEmailData.get(BaseDataHelper.User.MESSAGE))) {

            // Сохраняем новые данные в локальную базу данных
            lastUserEmailData.put(BaseDataHelper.User.EMAIL, emailEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.SUBJECT, subjectEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.MESSAGE, messageEt.getText().toString());

            baseDataMaster.insertEmailData(lastUserEmailData);

        }
        /// Готовим файл *.csv к отправке
        String fileName = createCsvFile();
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
        Uri path = Uri.fromFile(filelocation);
        // Создаем интент с экшеном на отправку
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        // Заполняем данными: тип текста, адрес, сабж и собственно текст письма
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, latestEmail);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, latestSubject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, latestMessage);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        /* Отправляем на выбор!*/
        MainActivity.this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private String createCsvFile() {
        DBHandler dbHandler = new DBHandler(MainActivity.this);
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        String username = "default";
        String fileName = username + "-timeSheetApp.csv";
        File file = new File(exportDir, fileName);
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            Cursor curCSV = dbHandler.readAllEventsFromDB();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                        curCSV.getString(3), curCSV.getString(4), curCSV.getString(5)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }

        return fileName;
    }

    private void openCalendar(int view_id) {
        // Проверяем userDataMap на null.
        if (userDataMap == null) {
            userDataMap = new HashMap<>();
        }

        DialogFragment dialogFragment = new DatePicker();
        if (view_id == R.id.fe_calendar_endd_tv || view_id == R.id.fe_calendar_endd_ib) {
            dataKey = END;
        } else if (view_id == R.id.fe_calendar_startd_tv || view_id == R.id.fe_calendar_startd_ib) {
            dataKey = START;
        }
        dialogFragment.show(getFragmentManager(), "dataPicker");
    }

    @SuppressLint("ValidFragment")
    protected class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        // Ключ, отвечает за то, какую дату мы считываем, start data or end data;
        // key может иметь 2 значения : start или end;
        private String key = null;

        /**
         * Показываем виджет календаря пользователю
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // определяем текущую дату
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // создаем DatePickerDialog и возвращаем его
            Dialog picker = new DatePickerDialog(getActivity(), this,
                    year, month, day);
            return picker;
        }

        /**
         * Метод сохраняет данные полученные dataPicker от пользователя
         *
         * @param datePicker - виджет календаря
         * @param year       - год выбранный пользователем
         * @param month      - месяц выбранный пользователем
         * @param day        - день выбранный пользователем
         */
        @Override
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int month, int day) {
            // Объявляем переменну EditText, которой позже присвоем ссылку на
            // startData || endData (EditText)
            // Это нужно для того, что бы указать выбранную дату в правильном EditText
            TextView varEditText = startData;
            key = dataKey;
            // В зависимости от ключа, присваеваем varEditText нужную нам ссылку
            if (key != null) {
                if (key.equals(START)) {
                    varEditText = startData;
                } else varEditText = endData;
            }
            // Сохраняем нащи данные в HashMap
            userDataMap.put(key + DAY, day);
            // Делепем инкремент ++month потому что
            // счет месяцев в dataPicker наченается с 0 а не с 1
            userDataMap.put(key + MONTH, ++month);
            userDataMap.put(key + YEAR, year);
            // Присваеваем новое значение в EditText
            varEditText.setText(day + " / " + month + " / " + year);
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////

//    // Показываем список пользовательских активностей
//    private void showIncludeItems() {
//
//        listActivity = new ArrayList<>();
//
//        DBHandler dbHandler = new DBHandler(fContext);
//        Cursor cursor = dbHandler.readActivitiesFromDB();
//
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            listActivity.add(new ButtonActivity(cursor.getString(1)));
//            cursor.moveToNext();
//            Log.d(LOG_TAG, "getActivities");
//        }
//        cursor.close();
//        listActivityToSend = listActivity;
//        CustomArrayAdapter arrayListArrayAdapter = new CustomArrayAdapter(fContext, listActivity);
//
//        includeLv.setAdapter(arrayListArrayAdapter);
//    }
//
//
//
//    public class CustomArrayAdapter extends ArrayAdapter<ArrayList<ButtonActivity>> {
//
//        public CustomArrayAdapter(Context context, ArrayList values) {
//            super(context, R.layout.list_item, values);
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            View v = convertView;
//
//            if (v == null) {
//                LayoutInflater vi;
//                vi = LayoutInflater.from(getContext());
//                v = vi.inflate(R.layout.include_activity_item, null);
//            }
//            TextView textView = (TextView) v.findViewById(R.id.ia_action_name);
//            CheckBox checkBox = (CheckBox) v.findViewById(R.id.ia_checkbox);
//
//            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//
//                    // Если пользоваетель снял галочку, удаляем активность из спика на отправку
//
//                    if (!compoundButton.isChecked()) {
//                        for (ButtonActivity bt : listActivityToSend) {
//                            if (bt.name.equals(listActivity.get(position).name)) {
//                                listActivityToSend.remove(position);
//                            }
//                        }
//                    } else {
//                        listActivityToSend.add(listActivity.get(position));
//                    }
//
//                }
//            });
//            ButtonActivity activity = listActivity.get(position);
//            textView.setText(activity.name);
//            textView.setBackgroundColor(activity.color);
//            return v;
//        }
//    }
//
//    class User implements BaseColumns {
//        public  final String TABLE_NAME = "latest_data";
//        public  final String EMAIL = " email ";
//        public  final String SUBJECT = " subject ";
//        public  final String MESSAGE = " message ";
//    }
//
//    class BaseDataHelper extends SQLiteOpenHelper {
//
//        public static final String DB_NAME = "timeSheetApp_sendEmailDB";
//        public static final int DB_VERSION = 1;
//
//        User user= new User();
//
//         String SCRIPT_CREATE_TBL_MAIN = " CREATE TABLE " +
//                user.TABLE_NAME + " ( " +
//                user._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//               user.EMAIL + " TEXT, " +
//                user.SUBJECT + " TEXT, " +
//                user.MESSAGE + " TEXT " +
//                " );";
//
//        public BaseDataHelper(Context context) {
//            super(context, DB_NAME, null, DB_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) {
//            db.execSQL(SCRIPT_CREATE_TBL_MAIN);
//        }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//            sqLiteDatabase.execSQL("DROP TABLE " + user.TABLE_NAME);
//            onCreate(sqLiteDatabase);
//        }
//    }
//
    /*
    +++++++++++++++++++++++++++++++++++++++++ End EMAIL SCREEN
     */
}
