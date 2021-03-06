package kodman.timesheetapp.Database.ExportDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by samik on 11/3/2017.
 */

public class BaseDataHelper extends SQLiteOpenHelper {

    // DataBase store name
    public static final String DB_NAME = "timeSheetApp_sendEmailDB";
    public static final int DB_VERSION = 1;

    //Create a model for storing user data
    public static class User implements BaseColumns {
        public static final String TABLE_NAME = "latest_data";
        public static final String EMAIL = "email";
        public static final String SUBJECT = "subject";
        public static final String MESSAGE = "message";
    }

    // Script to create a data table
    static String SCRIPT_CREATE_TBL_MAIN = " CREATE TABLE " +
            User.TABLE_NAME + " ( " +
            User.EMAIL + " TEXT, " +
            User.SUBJECT + " TEXT, " +
            User.MESSAGE + " TEXT " +
            " );";

    public BaseDataHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCRIPT_CREATE_TBL_MAIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + User.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
