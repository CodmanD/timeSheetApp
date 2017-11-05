package kodman.timesheetapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
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

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;

import kodman.timesheetapp.Database.DBHandler;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    class ThreadForActualTime extends Thread {
        boolean isPause = false;

        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        while (this.isPause) {
                            this.wait();
                        }
                    }

                    Thread.sleep(1000);
                    synchronized (MainActivity.this) {
                        //Log.d(TAG, "Thread=================TICK");
                        toolbar.post(new Runnable() {
                            @Override
                            public void run() {

                                Date curDate = new Date();
                                String time = new SimpleDateFormat("HH:mm:ss").format(curDate);
                                toolbar.setTitle(time);
                                if (MainActivity.this.listLogActivity.size() > 0 && MainActivity.this.status == 0) {
                                    long timeDiff = System.currentTimeMillis() - MainActivity.this.listLogActivity.get(0).ms;
                                    Date moment = new Date(timeDiff);
                                    time = new SimpleDateFormat("mm:ss").format(moment) + " min:sec";
                                    ((TextView) MainActivity.this.findViewById(R.id.tvLastLap)).setText(time);
                                }

                            }
                        });
                    }
                }
            } catch (InterruptedException ex) {
                Log.d(TAG, "--Daemon Thread :" + ex.getMessage());
            }
        }

        public synchronized void pauseOn()//включаем паузу
        {
            this.isPause = true;
        }

        public synchronized void pauseOff()//выключаем паузу
        {
            this.isPause = false;
            this.notify();
        }
    }

    ArrayList<ButtonActivity> listActivity = new ArrayList<>();//All buttons activity for current  time
    ArrayList<ButtonActivity> listLogActivity = new ArrayList<>();
    ArrayAdapter<ButtonActivity> adapterListLogActivity;
    ArrayList<ButtonActivity> listSetActivity = new ArrayList<>();
    String mDeleteTime;
    String mUpdateTime;
    String mNewStartTime;
    Resources res;
    ListView lvActivity;
    Time startTime = new Time();
    Date startDate = new Date();
    Date currentDate = new Date();
    DateFormat df = new DateFormat();
    String mColor = "#aaaaaa";
    static String nameCalendar = "";
    static String myName = "";
    SharedPreferences sPref;
    Long ms;

    class ButtonActivity {
        String name;
        int color;
        String time = new SimpleDateFormat("HH:mm:ss").format(startDate);
        String date = new SimpleDateFormat("dd.MM.yyyy").format(startDate);
        long ms = System.currentTimeMillis();
        ;

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

            return this.color;
            // return res.getColor(R.color.colorButton);
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
        // MainActivity.this.adapterListLogActivity.notifyDataSetChanged();


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
    String mCalendarData[] = {null, null};
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
                        //TODO:put time for deleting from db and calendar
                        deleteEventFromCalendar(mDeleteTime);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void deleteEventFromCalendar(String startTime) throws IOException {
            ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
            mService.events().delete(mCalendarId, arrayList.get(1)).execute();
            mDbHandler.deleteEventFromDb(startTime);
        }

        private void updateEventTime() throws IOException {
            updateEventStartTime(mUpdateTime);
            mDbHandler.deleteEventFromDb(mUpdateTime);
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
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                mDbHandler.writeOneEventToDB(mSummary, mCalendarId, eventId, mStartTime, mEndTime, mColor);
                mDbHandler.closeDB();
            }
            mTempData = false;
        }

        private void addEventToCalendar() throws IOException {
            if (mTempData) {
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
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
                System.out.printf("Event created: %s\n", event.getHtmlLink());
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
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                mDbHandler.updateEvent(mStartTime, mEndTime, eventId);
                mDbHandler.closeDB();
            } else {
                eventId = "not_synced";
                mDbHandler.updateEvent(mStartTime, mEndTime, eventId);
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
                System.out.printf("Event created: %s\n", event.getHtmlLink());
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

    private static final String TAG = "------Activity Say";

    private Toolbar toolbar;
    private Menu menu;
    private int status = 0;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mSharedEditor;
    private ThreadForActualTime actualTime;

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
        //  setContentView(R.layout.screen_settings);
        toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

        //if(toolbar==null)
        //    Log.d(TAG,"NULL");
        this.setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);


        //toolbar.setNavigationIcon(R.mipmap.ic_add_circle_white_36dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Icon Click");
            }
        });

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
        //  this.createList();
        //------------------------------------------------------------------


        this.addToGridViewButtonsActivity();
        this.createActivityLog();
        ///------------

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        callCalendarApi(3);
        ///-------------


//        if (actualTime == null) {
//            actualTime = new ThreadForActualTime();
//            //    actualTime.start();
//        }

/*
For actual time, update every 1000 ms
 */
        new java.util.Timer().schedule
                (
                        new TimerTask() {
                            public void run() {
                                toolbar.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Date curDate = new Date();
                                        String time = new SimpleDateFormat("HH:mm:ss").format(curDate);
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
                        1000, 1000);

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
                        ba.time = new SimpleDateFormat("HH:mm:ss").format(date);
                        ba.date = new SimpleDateFormat("dd.MM.yyyy").format(date);
                        mUpdateTime = String.valueOf(ba.ms);
                        ba.ms = date.getTime();
                        mNewStartTime = String.valueOf(ba.ms);
                        updateGoogleDiary();

                        createActivityLog();
                        Toast.makeText(MainActivity.this,
                                "Выбранное время чч:мм : " + date.toString(),
                                Toast.LENGTH_LONG).show();

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
        toolbar.setTitle("00:00:00");
        this.setSupportActionBar(toolbar);
        this.addToGridLayoutSettings();
        // if (!nameCalendar.equals("")) {
        EditText editTextCalendar = (EditText) this.findViewById(R.id.editTextCalendar);
        editTextCalendar.setText(sPref.getString("myCalendar", ""));
        // }
        // if (!myName.equals("")) {
        EditText editTextName = (EditText) this.findViewById(R.id.editTextName);
        editTextName.setText(sPref.getString("myName", ""));
        // }
    }


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

                toolbar.setTitle("00:00:00");
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

                toolbar.setTitle("00:00:00");
                this.setSupportActionBar(toolbar);

                //   Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                this.status = 3;
                this.setContentView(R.layout.screen_share);
                toolbar = (Toolbar) this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitle("00:00:00");
                this.setSupportActionBar(toolbar);
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

    public void clearLogActivityFromDB() {
        Toast.makeText(this, "Clean Log From DB", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
//        this.myName = sPref.getString("myName", "");
//        this.nameCalendar = sPref.getString("myCalendar", "");
        super.onResume();
        if (MainActivity.this.actualTime != null) {
            MainActivity.this.actualTime.pauseOff();//Возобновляем работу потока
        }
        // Log.d("======OnResume=====","Thread if != null begin");
    }

    @Override
    public void onPause() {
//        SharedPreferences.Editor ed = sPref.edit();
//        int size=this.listActivity.size();
//        ed.putInt("sizeListActivity",size);
//        for(int i=0;i<size;i++)
//        {
//            ed.putString("buttonAcivityName"+i,this.listActivity.get(i).name);
//            ed.putInt("buttonAcivityColor"+i,this.listActivity.get(i).color);
//        }
//        ed.commit();
        //  this.listActivity.clear();
        super.onPause();


        if (MainActivity.this.actualTime != null) {
            MainActivity.this.actualTime.pauseOn();//Приостанавливаем работу потока
        }
        // Log.d("======OnPause=====","Thread if != null stop");
    }
}
