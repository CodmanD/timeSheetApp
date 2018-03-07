package kodman.timesheetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kodman.timesheetapp.Database.DBHandler;

/**
 * Created by DI1 on 02.03.2018.
 */

public class ActivityEditPage extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "TimeSheet";
    List listActivity;
    List listCalls;

    static final int REQUEST_PERMISSION_GET_CALL = 1004;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_edit_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Toolbar toolbar = this.findViewById(R.id.toolBarEditPage);

        Log.d(TAG, "Toolbar" + toolbar);
        this.setSupportActionBar(toolbar);



        Calendar calendar = Calendar.getInstance();

        String day = getResources().getStringArray(R.array.days)[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        Log.d(TAG, "DAY =" + day);
        TextView tvDay = ((TextView) this.findViewById(R.id.tvDay));

        //Log.d(TAG,"DAY ="+tvDay);
        tvDay.setText(day);
        TextView tvDate = ((TextView) this.findViewById(R.id.tvDate));

        tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.YEAR));

        this.findViewById(R.id.ivLeft).setOnClickListener(this);
        this.findViewById(R.id.ivRight).setOnClickListener(this);
        createListActivity();


        createListPhone();
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
                break;
            case R.id.ivRight:
                Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show();
                break;
        }

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
        Log.d(TAG, " start setListHeigth countAdapter= " + adapterListActivity.getCount());


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
                    String contact = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));


                    listCalls.add(new mCall(String.valueOf(date), String.valueOf(duration), contact));
                    Log.d(TAG, "------------CALL:" + number + "///" + date);
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

                    Log.d(TAG, "add Call" + call.duration + "|" + call.getTime());
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
