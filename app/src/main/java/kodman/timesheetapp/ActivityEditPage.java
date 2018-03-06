package kodman.timesheetapp;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    final String TAG="TimeSheet";
    List listActivity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_edit_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       Toolbar toolbar = this.findViewById(R.id.toolBarEditPage);
        this.setSupportActionBar(toolbar);
       // res = this.getResources();
       // toolbar = this.findViewById(R.id.toolBar_MainActivity);
       // this.setSupportActionBar(toolbar);

        findViewById(R.id.ivLeft).setOnClickListener(this);
        findViewById(R.id.ivRight).setOnClickListener(this);

        Calendar calendar=Calendar.getInstance();

        ((TextView) findViewById(R.id.tvDay)).setText(""+calendar.get(Calendar.DAY_OF_WEEK));
        ((TextView) findViewById(R.id.tvDate)).setText(calendar.get(Calendar.DAY_OF_MONTH)+"/"
                +(calendar.get(Calendar.MONTH)+1)
                +"/"+calendar.get(Calendar.YEAR));
        createListActivity();
    }


    private void readAcivitiesFromDB() {

        DBHandler mDbHandler = new DBHandler(getApplicationContext());
        listActivity=new ArrayList<ButtonActivity>();
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
        while (cursor.moveToNext()) {

            id = cursor.getString(cursor.getColumnIndex("_id"));
            name = cursor.getString(cursor.getColumnIndex("eventName"));
            subName = cursor.getString(cursor.getColumnIndex("subName"));
            notes= cursor.getString(cursor.getColumnIndex("notes"));

            startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));

            try {
                endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));
            } catch (Exception ex) {
                Log.d(TAG,"Exception endTime= "+ex.getMessage());
                endTime = 0;
            }
            if (startTime == endTime)
            {
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
        }
        cursor.close();
        mDbHandler.closeDB();
    }


    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.ivLeft:
                Toast.makeText(this,"Left",Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivRight:
                Toast.makeText(this,"Right",Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void createListActivity()
    {

        readAcivitiesFromDB();



       ListView lvActivity = this.findViewById(R.id.LVStopwatch);

        ArrayAdapter<ButtonActivity>  adapterListActivity = new ArrayAdapter<ButtonActivity>(this,
                R.layout.list_item_stopwatch, R.id.tvActivity, listActivity) {
            @Override
            public View getView(final int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_stopwatch, parent, false);
                }

                View view = super.getView(position,convertView, parent);
                TextView tvStartTime = view. findViewById(R.id.tvStartTime);
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
        Log.d(TAG, " start setListHeigth");
        getListViewSize(lvActivity);
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
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);
        // print height of adapter on log
        //Log.i("-------------------height of listItem:", String.valueOf(totalHeight));
    }

/*
    private String getDay(int n)
    {
        switch(n)
        {
            case 1: "" break;
            case 1:break;
            case 1:break;
            case 1:break;
            case 1:break;
            case 1:break;
            case 1:break;
        }
        return "";
    }
    */
}
