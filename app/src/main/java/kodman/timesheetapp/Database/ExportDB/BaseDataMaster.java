package kodman.timesheetapp.Database.ExportDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

public class BaseDataMaster {

    private static final String LOG_TAG = "BaseDataMaster";
    private SQLiteDatabase database;
    private BaseDataHelper dbCreator;

    private static BaseDataMaster dataMaster;

    // Create & initialize dataBase variable
    private BaseDataMaster(Context context) {
        dbCreator = new BaseDataHelper(context);
        if (database == null || !database.isOpen()) {
            database = dbCreator.getWritableDatabase();
        }
    }

    public static BaseDataMaster getDataMaster(Context context) {
        if (dataMaster == null) {
            dataMaster = new BaseDataMaster(context);
        }
        return dataMaster;
    }

    /**
     * Called when need save a new user data to Sqlite db
     *
     * @param emailData - Data for autocomplete fields for input.
     */
    public long insertEmailData(HashMap<String, String> emailData) {
        deleteTable();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BaseDataHelper.User.EMAIL, emailData.get(BaseDataHelper.User.EMAIL));
        contentValues.put(BaseDataHelper.User.SUBJECT, emailData.get(BaseDataHelper.User.SUBJECT));
        contentValues.put(BaseDataHelper.User.MESSAGE, emailData.get(BaseDataHelper.User.MESSAGE));

        Log.d(LOG_TAG, "insertData");
        return database.insert(BaseDataHelper.User.TABLE_NAME, null, contentValues);
    }

    // This method is remove table
    private void deleteTable() {
        database.delete(BaseDataHelper.User.TABLE_NAME, null, null);
    }

    /**
     * This method is called when the FragmentExport is opened to auto-complete fields for input
     *
     * @return data from it will be substituted in the input fields
     */
    public HashMap<String, String> getEmailData() {
        String query = "SELECT " + BaseDataHelper.User.EMAIL + ", " +
                BaseDataHelper.User.SUBJECT + ", " +
                BaseDataHelper.User.MESSAGE + " " +
                " FROM " + BaseDataHelper.User.TABLE_NAME;

        // Read the data from the database and write them to the list
        Cursor cursor = database.rawQuery(query, null);

        HashMap<String, String> list = new HashMap<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.put(BaseDataHelper.User.EMAIL, cursor.getString(0));
            list.put(BaseDataHelper.User.SUBJECT, cursor.getString(1));
            list.put(BaseDataHelper.User.MESSAGE, cursor.getString(2));

            cursor.moveToNext();
            Log.d("BDM", "getEmailData");
        }
        ;
        return list;
    }
}
