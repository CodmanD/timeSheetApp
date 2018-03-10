package kodman.timesheetapp;

import com.google.api.services.calendar.CalendarScopes;

/**
 * Created by DI1 on 07.03.2018.
 */

public final class Cnst {
    public static final String TAG="TimeSheet";
    public static final String USER_NAME_PREFERENCES = "user_name_sp";
    public static final String USER_NAME = "name";
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_PERMISSION_LOCATION = 1005;

    public static final String CALENDAR_ID="calendarId";
    public static final String NAME="myName";
    public static final String NAME_CALENDAR="myCalendar";
    public static final String SIZE_LIST_ACTIVITY="sizeListActivity";
    public static final String NAME_ACTIVITY="buttonAcivityName";
    public static final String COLOR_ACTIVITY="buttonAcivityColor";
    public static final String SIZE_LIST_SUBACTIVITY="sizeListSubactivity";
    public static final String NAME_SUBACTIVITY="buttonSubacivityName";
    public static final String COLOR_SUBACTIVITY="buttonSubacivityColor";

    public static final String PREF_ACCOUNT_NAME = "is.karpus@gmail.com";
    public static final String[] SCOPES = {CalendarScopes.CALENDAR};

    //Collumns DataBase Table GPS
    public static final String GPS_TABLE = "coordinatesTable";
    public static final String DATE_TIME_START = "dateTimeStart";
    public static final String DATE_TIME_END = "dateTimeEnd";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    //Collumns DataBase Table Activities
    public static final String CALENDAR_TABLE = "calendarTable";
   // public static final String DATE_TIME_START = "dateTimeStart";
   // public static final String DATE_TIME_END = "dateTimeEnd";
   // public static final String LATITUDE = "latitude";
   // public static final String LONGITUDE = "longitude";

}
