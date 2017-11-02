package kodman.timesheetapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler {

    public void writeOneEventToDB(String eventName, String calendarId, String eventId, long startTime, long endTime, Context context) {
        DBHelper dbHelper = new DBHelper(context);
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

    public Cursor readOneEventFromDB(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable WHERE eventId LIKE not_synced", null);
    }

    public Cursor readAllEventsFromDB(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM calendarTable", null);
    }

    public Cursor readActivitiesFromDB(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM activityTable", null);
    }

    public void deleteEventFromDb(String eventId, Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM calendarTable WHERE startTime LIKE " + eventId);
        db.close();
        dbHelper.close();
    }


}

