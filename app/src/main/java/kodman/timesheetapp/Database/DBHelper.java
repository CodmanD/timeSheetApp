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
                + "subName text,"
                + "subColor integer,"
                + "notes text,"
                + "eventId text,"
                + "dateTimeStart text,"
                + "dateTimeEnd text,"
                + "color integer,"
                + "latitude real,"
                + "longitude real,"
                + "deleted integer,"
                + "synced integer"
                + ");");


        db.execSQL("create table coordinatesTable ("
                + "_id integer primary key autoincrement,"
                + "dateTimeStart integer,"
                + "dateTimeEnd integer,"
                + "latitude real,"
                + "longitude real"
                + ");");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
