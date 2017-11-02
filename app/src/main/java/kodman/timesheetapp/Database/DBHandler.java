package kodman.timesheetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler {
    private Context mContext;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandler(Context context) {
        mContext = context;
    }

    public void writeOneEventToDB(String eventName, String calendarId, String eventId, String startTime, String endTime) {
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

