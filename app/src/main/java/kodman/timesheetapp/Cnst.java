package kodman.timesheetapp;

import com.google.api.services.calendar.CalendarScopes;

/**
 * Created by DI1 on 07.03.2018.
 */

public final class Cnst {
    public static final String USER_NAME_PREFERENCES = "user_name_sp";
    public static final String USER_NAME = "name";
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_PERMISSION_LOCATION = 1005;

    public static final String PREF_ACCOUNT_NAME = "is.karpus@gmail.com";
    public static final String[] SCOPES = {CalendarScopes.CALENDAR};
}
