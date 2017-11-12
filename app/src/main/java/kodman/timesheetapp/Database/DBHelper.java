package kodman.timesheetapp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context) {
        super(context, "calendar.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table calendarTable ("
                + "_id integer primary key autoincrement,"
                + "calendarId text,"
                + "eventName text,"
                + "eventId text,"
                + "dateTimeStart text,"
                + "dateTimeEnd text,"
                + "color integer,"
                + "deleted integer,"
                + "synced integer"
                + ");");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
