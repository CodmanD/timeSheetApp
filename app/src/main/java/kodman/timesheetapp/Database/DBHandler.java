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

    public void writeOneEventToDB(String eventName, String calendarId, String eventId, String endTime, String startTime) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("calendarId", calendarId);
        cv.put("eventName", eventName);
        cv.put("dateTimeStart ", startTime);
        cv.put("dateTimeEnd", endTime);
        db.insert("calendarTable", null, cv);
        dbHelper.close();
    }

    public Cursor readUnsyncedEventFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable WHERE eventId LIKE 'not_synced'", null);
    }

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
        db.close();
        dbHelper.close();
        return arrayList;
    }

    public Cursor readAllEventsFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();

        return db.rawQuery("SELECT * FROM calendarTable", null);
    }

    public Cursor readActivitiesFromDB() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM activityTable", null);
    }

    public void clearEventsTable() {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable");
        db.close();
        dbHelper.close();
    }

    public void deleteUnsyncedEventFromDb(String startTime) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND eventId LIKE 'not_synced'");
        db.close();
        dbHelper.close();
    }

    public void deleteEventFromDb(String startTime) {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "'");
        db.close();
        dbHelper.close();
    }

    public void closeDB() {
        db.close();
        dbHelper.close();
    }
}

