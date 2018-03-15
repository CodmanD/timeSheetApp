package kodman.timesheetapp;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kodman.timesheetapp.Database.DBHandler;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.provider.CalendarContract.CalendarCache.URI;

/**
 * Created by DI1 on 02.03.2018.
 */

public class ActivityEditPage extends AppCompatActivity implements View.OnClickListener {

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
           // CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
   // private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;



    final String TAG = "TimeSheet";
    List listActivity;
    List listCalls;
    List listGPS;
    List listEvents;

    long curTime;
    long startTime;
    long finishTime;
    String day;
    TextView tvDate;
    TextView tvDay;
    Calendar calendar;
    private static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

    static final int REQUEST_PERMISSION_GET_CALL = 1004;
    static final int REQUEST_PERMISSION_GET_CALENDAR=1010;

    private com.google.api.services.calendar.Calendar mService = null;

    GoogleAccountCredential mCredential;

    private void setTime(long curtime) {
        long[] time = new long[2];
        calendar.setTimeInMillis(curTime);
        //calendar.set(year, month, day, 0, 0, 0);
        Calendar dateStart = Calendar.getInstance();
        dateStart.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Calendar dateEnd = Calendar.getInstance();
        dateEnd.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
        startTime = dateStart.getTimeInMillis();
        finishTime = dateEnd.getTimeInMillis();
        //Log.d(Cnst.TAG, "CurTime = " + curTime + "|" + startTime + "|" + finishTime);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
        curTime = System.currentTimeMillis();
        setTime(curTime);

        setContentView(R.layout.screen_edit_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Toolbar toolbar = this.findViewById(R.id.toolBarEditPage);
        this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_36dp);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
           //Toast.makeText(ActivityEditPage.this,"Click",Toast.LENGTH_SHORT).show();
          Intent intent= new Intent(ActivityEditPage.this,MainActivity.class);
            startActivity(intent);
    }
});
      //  Log.d(TAG, "Toolbar" + toolbar);


     //   workCalendar();
/*
        Intent intent = getIntent();


        String calendarId = intent.getExtras().getString(Cnst.CALENDAR_ID);
        //mService=(com.google.api.services.calendar.Calendar)intent.getExtras().getSerializable("Service");

        Log.d(Cnst.TAG,"Calendar ID = " + calendarId);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Cnst.SCOPES))
                .setBackOff(new ExponentialBackOff());

        if(mCredential.getSelectedAccountName() == null) {
            chooseAccount();}

        Log.d(Cnst.TAG,"Credential = " + mCredential);




// Submit the query and get a Cursor object back.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                        REQUEST_PERMISSION_GET_CALENDAR);
        } else {

          HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("timeSheetApp")
                    .build();


            List<Event>
                    events = null;
            try {
                events = mService.events().list(calendarId).execute().getItems();
                if (events != null)
                    for (int i = 0; i < events.size(); i++) {
                        Log.d(Cnst.TAG, "Google Event" + events.get(i).getSummary());
                    }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(Cnst.TAG, "------------------Exception  " + e.getMessage());
            }
        }
        Log.d(Cnst.TAG, " Service = " + mService);

*/
        day = getResources().getStringArray(R.array.days)[calendar.get(Calendar.DAY_OF_WEEK) - 1];

       // Log.d(TAG, "DAY =" + day);
        tvDay = ((TextView) this.findViewById(R.id.tvDay));

        //Log.d(TAG,"DAY ="+tvDay);
        tvDay.setText(day);
        tvDate = ((TextView) this.findViewById(R.id.tvDate));

        tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.YEAR));

        this.findViewById(R.id.ivLeft).setOnClickListener(this);
        this.findViewById(R.id.ivRight).setOnClickListener(this);
     //   createListActivity();

        createListEvents();
    //    createListPhone();
     //   createListGPS();
    }



    @AfterPermissionGranted(Cnst.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(Cnst.PREF_ACCOUNT_NAME, null);
            Log.d(Cnst.TAG,"Account name = "+accountName);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        Cnst.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    Cnst.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }



    private void workCalendar()
    {

        ContentResolver contentResolver = getContentResolver();
        Calendar calendar = Calendar.getInstance();
        String dtstart = "dtstart";
        String dtend = "dtend";

        listEvents= new ArrayList<String[]>();

        SimpleDateFormat    displayFormatter = new SimpleDateFormat("MMMM dd, yyyy (EEEE)");

       String stime=displayFormatter.format(calendar.getTime());

        SimpleDateFormat startFormatter = new SimpleDateFormat("MM/dd/yy");
        String dateString = startFormatter.format(calendar.getTime());

        long after = calendar.getTimeInMillis();
        SimpleDateFormat formatterr = new SimpleDateFormat("hh:mm:ss MM/dd/yy");
        Calendar endOfDay = Calendar.getInstance();
        Date dateCCC = null;
        try {
            dateCCC = formatterr.parse("23:59:59 " + dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        endOfDay.setTime(dateCCC);




        Log.d(Cnst.TAG,"WorkCalendar ");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                        REQUEST_PERMISSION_GET_CALENDAR);
            Log.d(Cnst.TAG,"No Permission");
        } else {


            //Cursor managedCursor  = null;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                //  String accountName = getPreferences(Context.MODE_PRIVATE)
                //        .getString(Cnst.PREF_ACCOUNT_NAME, null);
                String accountName = getIntent().getStringExtra(Cnst.PREF_ACCOUNT_NAME);
                Log.d(Cnst.TAG, "Account Name = " + accountName);

                //  managedCursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"), new String[] {CalendarContract.Calendars.ACCOUNT_NAME },
                String[] mProjection =
                        {
                                // CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES,
                                CalendarContract.Calendars._ID,
                                CalendarContract.Calendars.NAME,
                                CalendarContract.Calendars.ACCOUNT_NAME,
                                // CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                                // CalendarContract.Calendars.CALENDAR_LOCATION,
                                // CalendarContract.Calendars.CALENDAR_TIME_ZONE
                        };
                Uri uri = CalendarContract.Calendars.CONTENT_URI;
                Cursor mCursor = contentResolver.query(uri, mProjection,
                        null, null, null);
                //CalendarContract cc=new CalendarContract.();
                // Log.d(Cnst.TAG,"Calendar Name"+CalendarContract.)
                //  CalendarProvider calendarProvider = new CalendarProvider(context);
                //   Uri calendars = Uri.parse("content://calendar/calendars");

                // Cursor managedCursor = this.managedQuery(calendars, new String[] { "_id", "name" }, null, null, null);


                Log.d(Cnst.TAG, "Calendars Cursor count = " + mCursor.getCount());

                ArrayList<Integer> ids=new ArrayList<>();
                String title;
                String acc;
                if (mCursor.moveToFirst())
                    do {
                        // int calendar_id = managedCursor.getInt(0);
                        title = mCursor.getString(1);
                        int id = mCursor.getInt(0);
                        acc = mCursor.getString(2);
                        if(acc.equals(accountName))
                            ids.add(id);
                        Log.d(Cnst.TAG, "Cal_ID = " + id + " : " + title + " || " + acc);
                    }
                    while (mCursor.moveToNext());

                String wherId="(";
                for(int i=0;i<ids.size();i++)
                {

                        wherId+=ids.get(i);
                        if(i!=ids.size()-1)
                            wherId+=",";
                        else
                            wherId+=")";
                }
                Log.d(Cnst.TAG," IDS = "+wherId);

String sel=CalendarContract.Events.CALENDAR_ID +
        " LIKE( " + "SELECT " + CalendarContract.Calendars._ID + " FROM " + CalendarContract.Calendars.CONTENT_URI + " WHERE " +
        CalendarContract.Calendars.ACCOUNT_NAME + " = '" + accountName + "')";

                Log.d(Cnst.TAG,sel);
                String selection = "" + dtstart + ">" + startTime + " AND " + dtend + "<" + finishTime + " AND " + CalendarContract.Events.CALENDAR_ID +
                        " IN " + wherId+ "";
                //ContentProvider calendarProvider = new ContentProvider();
                //List<Calendar> calendars = calendarProvider.getCalendars().getList();
//                Cursor cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI,
//                        (new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventTimezone", "eventLocation"}),
//                        "(" + dtstart + ">" + startTime + " and " + dtend + "<" + finishTime + ")  ",
//                        null, "dtstart ASC");
                Cursor cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI,
                        (new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventTimezone", "eventLocation"}),
                        selection,
                        null, "dtstart ASC");


        /*String[] COLS={"calendar_id", "title", "description", "dtstart", "dtend","eventTimezone", "eventLocation"};

        cursor = contentResolver.query(


                CalendarContract.Events.CONTENT_URI, COLS,null, null, null);*/
                Log.d(Cnst.TAG, "Cursor Evenbts count=" + cursor.getCount());

                try {


                    if (cursor.moveToFirst()) {


                        do {

                            int calendar_id = cursor.getInt(0);
                            Log.d(Cnst.TAG, "Cal_ID = " + calendar_id);
                             title = cursor.getString(1);

                            String description = cursor.getString(2);

                            String dtstart1 = cursor.getString(3);

                            String dtend1 = cursor.getString(4);


                            String eventTimeZone = cursor.getString(5);

                            String eventlocation = cursor.getString(6);

                            listEvents.add(0, new String[]{dtstart1, dtend1, title});
                            Log.d(Cnst.TAG, " Events =" + title);

                        } while (cursor.moveToNext());
                    }
                } catch (AssertionError ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }


    private  void createListEvents()
    {
        workCalendar();
        ListView lvGoogle = findViewById(R.id.LVGoogle);

        ArrayAdapter<mCall> adapterGoogle = new ArrayAdapter<mCall>(this,
                R.layout.list_item_phone, R.id.tvContact, listEvents) {

            @Override
            public View getView(final int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_phone, parent, false);
                }

                View view = super.getView(position, convertView, parent);
                TextView tvStartTime = view.findViewById(R.id.tvStartTime);
                TextView tvFinishTime = view.findViewById(R.id.tvFinishTime);
                TextView tvContact = view.findViewById(R.id.tvContact);



                final String[] event = (String[]) listEvents.get(position);

                // Log.d(TAG, "add Call" + call.duration + "|" + call.getTime());
                tvStartTime.setText( formatTime.format(Long.parseLong(event[0])));
                tvFinishTime.setText( formatTime.format(Long.parseLong(event[1])));
                tvContact.setText(event[2]);

                return view;
            }
        };

        if (adapterGoogle != null)
            lvGoogle.setAdapter(adapterGoogle);


}

    private void readAcivitiesFromDB() {

        DBHandler mDbHandler = new DBHandler(getApplicationContext());
        listActivity = new ArrayList<ButtonActivity>();
        Cursor cursor = mDbHandler.readAllEventsFromDB();
        int count = cursor.getColumnCount();
        String msg = "CalendarTable= ";
        for (int i = 0; i < count; i++) {
            msg += " | " + cursor.getColumnName(i);
        }
        Log.d(TAG, "Count = " + count + "   : " + msg);


        long startTime;
        long endTime;
        int color;
        String id;
        String name;
        String subName;
        String notes;
        int subColor;
        if (cursor.moveToFirst())
            do {

                id = cursor.getString(cursor.getColumnIndex("_id"));
                name = cursor.getString(cursor.getColumnIndex("eventName"));
                subName = cursor.getString(cursor.getColumnIndex("subName"));
                notes = cursor.getString(cursor.getColumnIndex("notes"));

                startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));
                if(startTime<this.startTime||startTime>finishTime)continue;
                try {
                    endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));
                } catch (Exception ex) {
                    Log.d(TAG, "Exception endTime= " + ex.getMessage());
                    endTime = 0;
                }
                if (startTime == endTime) {
                    endTime = 0;
                }
                color = Integer.parseInt(cursor.getString(cursor.getColumnIndex("color")));
                subColor = Integer.parseInt(cursor.getString(cursor.getColumnIndex("subColor")));

                // Log.d(TAG, "Id:" + id + "Name = " + name + "start = " + startTime + "end = " + endTime);

                ButtonActivity ba = new ButtonActivity(name, color);
                ba.ms = startTime;
                ba.endTime = endTime;
                ba.setSubName(subName);
                ba.setSubColor(subColor);

                ba.setNotes(notes);
                this.listActivity.add(0, ba);
            } while (cursor.moveToNext());
        cursor.close();
        mDbHandler.closeDB();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ivLeft:
                Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show();

                curTime -= 86400000;
                setTime(curTime);


                break;
            case R.id.ivRight:
                Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show();
                curTime += 86400000;
                setTime(curTime);
                break;
        }
        calendar.setTimeInMillis(curTime);
        tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.YEAR));
        day = getResources().getStringArray(R.array.days)[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        tvDay.setText(day);

        createListActivity();
        createListPhone();
        createListGPS();
        createListEvents();
    }

    private void readCoordinates() {
        listGPS = new ArrayList<String[]>();
        Cursor cursor = new DBHandler(getBaseContext()).getGPSEvents(startTime, finishTime);
        String timeEnd = "";

       // Log.d(Cnst.TAG, "Cursor GPS = " + cursor.getCount());
      // if(cursor.moveToFirst())
        if (cursor.moveToLast())
            do {
                String[] coords = new String[4];
                coords[0] = cursor.getString(cursor.getColumnIndex(Cnst.DATE_TIME_START));
                coords[1] = timeEnd;
                timeEnd = cursor.getString(cursor.getColumnIndex(Cnst.DATE_TIME_START));
                coords[2] = cursor.getString(cursor.getColumnIndex(Cnst.LATITUDE));
                coords[3] = cursor.getString(cursor.getColumnIndex(Cnst.LONGITUDE));
                listGPS.add( coords);

            }
            //while (cursor.moveToNext());
            while (cursor.moveToPrevious());
        cursor.close();
    }

    private void createListGPS() {

        readCoordinates();


        Log.d(TAG, "Count = " + listGPS.size());

        ListView lvGPS = this.findViewById(R.id.LVGPS);

        ArrayAdapter<String[]> adapterListActivity = new ArrayAdapter<String[]>(this,
                R.layout.list_item_stopwatch, R.id.tvActivity, listGPS) {
            @Override
            public View getView(final int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_stopwatch, parent, false);
                }

                View view = super.getView(position, convertView, parent);
                TextView tvStartTime = view.findViewById(R.id.tvStartTime);
                TextView tvFinishTime = view.findViewById(R.id.tvFinishTime);
                TextView tvActivity = view.findViewById(R.id.tvActivity);
                TextView tvSubactivity = view.findViewById(R.id.tvSubactivity);

                final String[] coords = (String[]) listGPS.get(position);

                tvStartTime.setText(formatTime.format(Long.parseLong(coords[0])));

                String endTime = "";
                try {
                    endTime = formatTime.format(Long.parseLong(coords[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                tvFinishTime.setText(endTime);
                tvActivity.setText(coords[2]);
                tvSubactivity.setText(coords[3]);


                return view;
            }
        };

        // this.adapterListLogActivity.setNotifyOnChange(true);
        lvGPS.setAdapter(adapterListActivity);
       // Log.d(TAG, " start setListHeigth countAdapter= " + adapterListActivity.getCount());


        // getListViewSize(lvActivity);
    }


    private void createListActivity() {

        readAcivitiesFromDB();


        Log.d(TAG, "Count = " + listActivity.size());

        ListView lvActivity = this.findViewById(R.id.LVStopwatch);

        ArrayAdapter<ButtonActivity> adapterListActivity = new ArrayAdapter<ButtonActivity>(this,
                R.layout.list_item_stopwatch, R.id.tvActivity, listActivity) {
            @Override
            public View getView(final int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_stopwatch, parent, false);
                }

                View view = super.getView(position, convertView, parent);
                TextView tvStartTime = view.findViewById(R.id.tvStartTime);
                TextView tvFinishTime = view.findViewById(R.id.tvFinishTime);
                TextView tvActivity = view.findViewById(R.id.tvActivity);
                TextView tvSubactivity = view.findViewById(R.id.tvSubactivity);

                final ButtonActivity ba = (ButtonActivity) listActivity.get(position);

                tvStartTime.setText(ba.getStartTime());
                tvFinishTime.setText(ba.getEndTime());
                tvActivity.setText(ba.name);
                tvSubactivity.setText(ba.getSubName());


                return view;
            }
        };

        // this.adapterListLogActivity.setNotifyOnChange(true);
        lvActivity.setAdapter(adapterListActivity);
        //  Log.d(TAG, " start setListHeigth countAdapter= " + adapterListActivity.getCount());


        // getListViewSize(lvActivity);
    }

    private void createListPhone() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_PERMISSION_GET_CALL);
        } else {

            String[] projection = new String[]{
                    CallLog.Calls._ID,
                    CallLog.Calls.DATE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
            };
            String where = "";


            listCalls = new ArrayList<mCall>();

            Cursor cursor = getBaseContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    where,
                    null,
                    null
            );


            if (cursor.moveToFirst()) {
                do {
                    long _id = cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                  //  Log.d(Cnst.TAG,"Number = "+number);
                    String contact = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));

                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));

                    if(date>=startTime&&date<finishTime)
                        if(contact==null||contact.equals(""))
                        {
                            Log.d(Cnst.TAG,"CONTACT NULL");
                           // contact = number;//cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                            if(number==null||number.equals(""))
                                listCalls.add(new mCall(String.valueOf(date), String.valueOf(duration), "Unknown contact"));
                          else
                            listCalls.add(new mCall(String.valueOf(date), String.valueOf(duration), number));
                            //
                        }
                        else
                                listCalls.add(new mCall(String.valueOf(date), String.valueOf(duration), contact));
                    //  Log.d(TAG, "------------CALL:" + number + "///" + date);
                } while (cursor.moveToNext());
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }

            ListView lvPhone = findViewById(R.id.LVPhone);

            ArrayAdapter<mCall> adapterPhone = new ArrayAdapter<mCall>(this,
                    R.layout.list_item_stopwatch, R.id.tvContact, listCalls) {

                @Override
                public View getView(final int position,
                                    View convertView, ViewGroup parent) {

                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.list_item_phone, parent, false);
                    }

                    View view = super.getView(position, convertView, parent);
                    TextView tvStartTime = view.findViewById(R.id.tvStartTime);
                    TextView tvFinishTime = view.findViewById(R.id.tvFinishTime);
                    TextView tvContact = view.findViewById(R.id.tvContact);


                    final mCall call = (mCall) listCalls.get(position);

                   // Log.d(TAG, "add Call" + call.duration + "|" + call.getTime());
                    tvStartTime.setText(call.getStartTime());
                    tvFinishTime.setText(call.getFinishTime());
                    tvContact.setText(call.contact);

                    return view;
                }
            };

            if (adapterPhone != null)
                lvPhone.setAdapter(adapterPhone);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_GET_CALL:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


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
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount()));
        myListView.setLayoutParams(params);
        // print height of adapter on log
        //Log.i("-------------------height of listItem:", String.valueOf(totalHeight));
    }


}
