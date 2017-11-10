package kodman.timesheetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBHandler {
    private Context mContext;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandler(Context context) {
        mContext = context;
    }
    //write one event to local database
    public void writeOneEventToDB(String eventName, String calendarId, String eventId, String endTime, String startTime, int color) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("calendarId", calendarId);
        cv.put("eventName", eventName);
        cv.put("dateTimeStart", startTime);
        cv.put("dateTimeEnd", endTime);
        cv.put("color", color);
        db.insert("calendarTable", null, cv);
        dbHelper.close();
    }
    //read all events where event id like not_synced or deleted to sync with google calendar
    public Cursor readUnsyncedEventFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable WHERE eventId LIKE 'not_synced' OR eventId LIKE 'deleted'", null);

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
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        dbHelper.close();
        return arrayList;
    }
    //update end time of event with needed start time. We use it when selecting next activity
    public void updateEvent(String dateTimeStart, String dateTimeEnd, String eventId) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeEnd = '" + dateTimeEnd + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }
    //change eventId to "deleted" for deleting from google calendar in future
    public void updateEventDelete(String dateTimeStart, String eventId) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }

    //update start time of event with needed start time
    public void updateEventStartTime(String dateTimeStart, String newStartTime, String eventId) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeStart = '" + newStartTime + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.close();
        dbHelper.close();
    }
    //update end time of previous event, when we change time of current event
    public void updateEventEndTime(String dateTimeEnd, String newEndTime, String eventId) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET dateTimeEnd = '" + newEndTime + "' WHERE dateTimeEnd = '" + dateTimeEnd + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeEnd = '" + dateTimeEnd + "'");
        db.close();
        dbHelper.close();
    }

    //updating event name or color in local database
    public void updateEventNameColor(String dateTimeStart, String newSummary, int newColor, String eventId) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE calendarTable SET eventName = '" + newSummary + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET color = '" + newColor + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
        db.execSQL("UPDATE calendarTable SET eventId = '" + eventId + "' WHERE dateTimeStart = '" + dateTimeStart + "'");
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
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND eventId LIKE 'not_synced'");
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND eventId LIKE 'deleted'");
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
    //close database
    public void closeDB() {
        db.close();
        dbHelper.close();
    }
}

