package kodman.timesheetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import kodman.timesheetapp.Cnst;

public class DBHandler {
    private Context mContext;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private final String TAG="Data Base";

    public DBHandler(Context context) {
        mContext = context;
    }
    //write one event to local database
    public void writeOneEventToDB(String eventName, String calendarId, String eventId,
                                  String endTime, String startTime, int color, int deleted, int synced) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("calendarId", calendarId);
        cv.put("eventName", eventName);
        cv.put("dateTimeStart", startTime);
        cv.put("dateTimeEnd", endTime);
        cv.put("color", color);
        cv.put("deleted", deleted);
        cv.put("synced", synced);

        db.insert("calendarTable", null, cv);
        dbHelper.close();
    }
    //write one event to local database
    public void writeEventWithSubToDB(String eventName, String calendarId, String eventId,String endTime,
                                      String startTime, int color, int deleted, int synced,String subName,
                                      int subColor,String notes) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("calendarId", calendarId);
        cv.put("eventName", eventName);
        cv.put("dateTimeStart", startTime);
        cv.put("dateTimeEnd", endTime);
        cv.put("color", color);
        cv.put("deleted", deleted);
        cv.put("synced", synced);
        cv.put("subName", subName);
        cv.put("subColor", subColor);
        cv.put("notes", notes);
        db.insert("calendarTable", null, cv);
        dbHelper.close();
    }




    //read all events where event id like not_synced or deleted to sync with google calendar
    public Cursor readUnsyncedEventFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable WHERE deleted LIKE '1' OR synced LIKE '0'", null);

    }
    //read one event from db with needed start time
    public ArrayList<String> readOneEventFromDB(String dateTimeStart) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM calendarTable WHERE dateTimeStart LIKE '" + dateTimeStart + "'", null);
        cursor.moveToFirst();
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("calendarId")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("eventId")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("eventName")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("dateTimeEnd")));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        dbHelper.close();
        return arrayList;
    }
    //read one event from local db with needed end time(to change end time of event, when we change start time of next event)
    public ArrayList<String> readPreviousEventFromDB(String dateTimeEnd) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM calendarTable WHERE dateTimeEnd LIKE '" + dateTimeEnd + "'", null);
        cursor.moveToFirst();
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("calendarId")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("eventId")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("eventName")));
            arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow("dateTimeStart")));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        dbHelper.close();
        return arrayList;
    }
    //update end time of event with needed start time. We use it when selecting next activity
    public void updateEvent(String dateTimeStart, String dateTimeEnd, String eventId, int synced) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeEnd = '" + dateTimeEnd + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET synced = '" + synced + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }

public void updateLastEventEndTime(String endTime)
{
    Log.d("TAG"," time="+endTime);
    dbHelper = new DBHelper(mContext);
    db = dbHelper.getWritableDatabase();
    //String query = "SELECT dateTimeStart  FROM calendarTable  ORDER BY dateTimeStart  LIMIT 1";
    String query = "SELECT *  FROM calendarTable  ORDER BY dateTimeStart DESC LIMIT 1";

    // Log.d(TAG, query);

    Cursor cursor = db.rawQuery(query, null);
    if(cursor.moveToFirst() )
    {
        String startTime=cursor.getString(cursor.getColumnIndex("dateTimeStart"));
        String name=cursor.getString(cursor.getColumnIndex("eventName"));
        Log.d("DataBase = ","Name = :"+name+" || startTime = "+startTime);
        ContentValues values = new ContentValues();
        values.put("dateTimeEnd", endTime);

        int res= db.update("calendarTable",values,
                " dateTimeStart = '"+startTime+"'",null);
        Log.d("TAG","DATABase = "+"---------------Update = "+res);
    }
    cursor.close();
    db.close();
    dbHelper.close();
}


    public boolean updateTimeEvents(String time,String newTime)
    {
        Log.d("TAG"," time=");
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        String query = "SELECT dateTimeStart  FROM calendarTable  WHERE dateTimeEnd ='"+time+"'";
        Log.d(TAG,"Update time ="+time+" newTime = "+newTime);
        // Log.d(TAG, query);

        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst() )
        {
            long startTime=Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));
           // long endTime=Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));

            if(startTime< Long.parseLong(newTime))
            {
                cursor.close();
                db.close();
                dbHelper.close();
                return false;
            }
            else
            {
                db.execSQL("UPDATE calendarTable SET dateTimeEnd = '" + newTime + "' WHERE dateTimeStart = '" + startTime + "'");
                db.execSQL("UPDATE calendarTable SET dateTimeStart = '" + newTime + "' WHERE dateTimeStart = '" + time + "'");
                db.close();
                dbHelper.close();
           Log.d(TAG,"Update all");
                return true;
            }

        }
        db.execSQL("UPDATE calendarTable SET dateTimeStart = '" + newTime + "' WHERE dateTimeStart = '" + time + "'");

        db.close();
        dbHelper.close();
        return true;
    }


    //change eventId to "deleted" for deleting from google calendar in future
    public void updateEventDelete(String dateTimeStart, int deleted) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET deleted = '" + deleted + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }

    //update start time of event with needed start time
    public void updateEventStartTime(String dateTimeStart, String newStartTime, String eventId, int synced) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeStart = '" + newStartTime + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET synced = '" + synced + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }
    //update end time of previous event, when we change time of current event
    public void updateEventEndTime(String dateTimeEnd, String newEndTime, String eventId, int synced) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeEnd = '" + newEndTime + "' WHERE dateTimeEnd = '" + dateTimeEnd + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeEnd = '" + dateTimeEnd + "'");

        db.close();
        dbHelper.close();
    }
    //updating event name or color in local database
    public void updateEventNameColor(String dateTimeStart, String newSummary, int newColor, String eventId, int synced) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET eventName = '" + newSummary + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET color = '" + newColor + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }
    //updating event name or color for SubActivity in local database
    public void updateEventSubNameColor(String dateTimeStart, String newSubName, int newColor) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET subName = '" + newSubName + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET subColor = '" + newColor + "' WHERE dateTimeStart = '" + dateTimeStart + "'");

        db.close();
        dbHelper.close();
    }
    //updating event name or color for SubActivity in local database
    public void updateEventNotes(String dateTimeStart, String notes) {

        Log.d("TAG"," time="+dateTimeStart+" /Notes="+notes);
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
         values.put("notes", notes);

       int res= db.update("calendarTable",values," dateTimeStart = '"+dateTimeStart+"'",null);
        //db.execSQL("UPDATE calendarTable SET notes = '" + notes + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        Log.d("TEG","DATABase = "+"---------------Update = "+res);

         db.close();
        dbHelper.close();
    }


    //read all events from database to fill activity log
    public Cursor readAllEventsFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable ORDER BY dateTimeStart ASC", null);
    }

    //clear table of local database
    public void clearEventsTable() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable");
        db.close();
        dbHelper.close();
    }

    //delete all events marked "deleted" or "not_synced"(when it successfully synced)
    public void deleteUnsyncedEventFromDb(String startTime) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND deleted LIKE '1'");
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND synced LIKE '0'");
        db.close();
        dbHelper.close();
    }
    //delete one event from local database
    public void deleteEventFromDb(String startTime) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "'");
        db.close();
        dbHelper.close();
    }

    //getCursor for GPS List
    public Cursor getGPSEvents(long startTime,long finishTime)
    {

        Log.d(Cnst.TAG,"Start = "+startTime+" endTime = "+finishTime);
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT * FROM coordinatesTable WHERE "+Cnst.DATE_TIME_START+
                ">"+startTime+" AND "+Cnst.DATE_TIME_START+" < "+finishTime+
                " ORDER BY dateTimeStart ASC ", null);

       // db.close();
       // dbHelper.close();
        return cursor;
    }

    public long writeToGPS(long start,long finish,double latitude,double longitude)
    {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Cnst.DATE_TIME_START, start);
        //cv.put(Cnst.DATE_TIME_END, finish);
        cv.put(Cnst.LATITUDE, latitude);
        cv.put(Cnst.LONGITUDE, longitude);
        long res=db.insert(Cnst.GPS_TABLE, null, cv);
        db.close();
         return res;
    }

    public void showBase(String table)
    {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+table, null);
        Log.d(Cnst.TAG,"-------------------Cursor count = "+cursor.getCount());
        if(cursor.moveToFirst())
            do{
            Log.d(Cnst.TAG,"-----------------Color DataBASE = "+cursor.getString(cursor.getColumnIndex("color")));


            }while(cursor.moveToNext());
    }

    //close database
    public void closeDB() {
        db.close();
        dbHelper.close();
    }
}

