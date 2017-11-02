package kodman.timesheetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler {
    private Context mContext;

    public DBHandler(Context context) {
        mContext = context;
    }

    public void writeOneEventToDB(String eventName, String calendarId, String eventId, String startTime, String endTime) {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable WHERE eventId LIKE 'not_synced'", null);
    }

    public Cursor readAllEventsFromDB() {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db.rawQuery("SELECT * FROM calendarTable", null);
    }

    public Cursor readActivitiesFromDB() {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM activityTable", null);
    }

    public void clearEventsTable() {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable");
        db.close();
        dbHelper.close();
    }

    public void deleteUnsyncedEventFromDb(String startTime) {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "' AND eventId LIKE 'not_synced'");
        db.close();
        dbHelper.close();
    }

    public void deleteEventFromDb(String startTime) {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE dateTimeStart LIKE '" + startTime + "'");
        db.close();
        dbHelper.close();
    }
}

