package kodman.timesheetapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kodman.timesheetapp.Database.DBHandler;
import kodman.timesheetapp.Database.ExportDB.BaseDataHelper;
import kodman.timesheetapp.Database.ExportDB.BaseDataMaster;
import kodman.timesheetapp.Database.ExportDB.CSVWriter;

public class FragmentExport extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = "FragmentExport";
    private static final int DIALOG_DATE = 1;

    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String END = "end";
    private static final String START = "start";
    private String dataKey;
    // Stores the last data entered by the user in the corresponding fields
    private String latestEmail;
    private String latestSubject;
    private String latestMessage;

    private Context fContext;

    // Stores the data of the selected period for sending user data
    private HashMap<String, Integer> userDataMap;
    // Stores the last data entered by the user in the fields
    private HashMap<String, String> lastUserEmailData;
    // View elements
    private EditText emailEt;
    private EditText subjectEt;
    private EditText messageEt;
    private TextView startDataTv;
    private TextView endDataTv;
    private ImageButton startDCalendar;
    private ImageButton endDCalendar;
    private Button sendEmail;
    private ListView includeLv;
    private Toolbar toolbar;
    private View thisView;
    Resources res;
    ArrayList<ButtonActivity> listActivity;
    private ArrayList<String[]> allUserEventList;
    private ArrayList<String[]> filterEventUserList;
    private ArrayList<String> notSendEventName;
    private MainActivity mainActivity;


    public Toolbar getToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) thisView.findViewById(R.id.toolBar_screen_email);

        }
        return toolbar;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create fragment and initialise him
        View view = inflater.inflate(R.layout.screen_email, container, false);
        fContext = view.getContext();
        toolbar = (Toolbar) view.findViewById(R.id.toolBar_screen_email);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(toolbar);
      //  activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////
        this.mainActivity=(MainActivity)activity;
        mainActivity.toolbar=this.toolbar;
        toolbar.setTitle(mainActivity.actualTime);
        ///////////////


        res = view.getResources();
        thisView = view;

        setupUI(view);
        return view;
    }

    // Setup and initialize all view elements
    private void setupUI(View container) {
        emailEt = (EditText) container.findViewById(R.id.fe_email_et);
        subjectEt = (EditText) container.findViewById(R.id.fe_subject_et);
        messageEt = (EditText) container.findViewById(R.id.fe_message_et5);
        startDataTv = (TextView) container.findViewById(R.id.fe_calendar_startd_tv);
        endDataTv = (TextView) container.findViewById(R.id.fe_calendar_endd_tv);
        startDCalendar = (ImageButton) container.findViewById(R.id.fe_calendar_startd_ib);
        endDCalendar = (ImageButton) container.findViewById(R.id.fe_calendar_endd_ib);
        sendEmail = (Button) container.findViewById(R.id.fe_send_email_bt);
        includeLv = (ListView) container.findViewById(R.id.include_lv);

        startDataTv.setOnClickListener(this);
        endDataTv.setOnClickListener(this);
        startDCalendar.setOnClickListener(this);
        endDCalendar.setOnClickListener(this);
        sendEmail.setOnClickListener(this);

        updateUI();
    }


    private void updateUI() {
        BaseDataMaster baseDataMaster = BaseDataMaster.getDataMaster(fContext);
        if (lastUserEmailData == null) {
            lastUserEmailData = baseDataMaster.getEmailData();
        }

        // Еif the user has already entered data, automatically fill in the fields
        if (!baseDataMaster.getEmailData().isEmpty()) {
            lastUserEmailData = baseDataMaster.getEmailData();
            emailEt.setText(lastUserEmailData.get(BaseDataHelper.User.EMAIL));
            subjectEt.setText(lastUserEmailData.get(BaseDataHelper.User.SUBJECT));
            messageEt.setText(lastUserEmailData.get(BaseDataHelper.User.MESSAGE));
        }

        // We set the first and last day of the previous month in textView
        Calendar calendarlast = Calendar.getInstance();
        calendarlast.add(Calendar.MONTH, 0);
        calendarlast.set(Calendar.DAY_OF_MONTH, 1);
        calendarlast.add(Calendar.DATE, -1);
        Date lastDayOfMonth = calendarlast.getTime();

        Calendar calendarFirst = Calendar.getInstance();
        calendarFirst.add(Calendar.MONTH, 0);
        calendarlast.set(Calendar.DAY_OF_MONTH, 1);
        calendarlast.add(Calendar.DATE, -1);
        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        startDataTv.setText("01/" + calendarFirst.get(Calendar.MONTH) + "/" + calendarFirst.get(Calendar.YEAR));
        endDataTv.setText(sdf.format(lastDayOfMonth));

        showIncludeItems();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
        //Change colour for selected icon
        if (Build.VERSION.SDK_INT >= 21) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem mItem = menu.getItem(i);
                Drawable icon = mItem.getIcon();
                if (i == 2)
                    icon.setTint(getResources().getColor(R.color.colorActiveIcon));
                else
                    icon.setTint(getResources().getColor(R.color.colorNoActiveIcon));
            }
        }

    }

    @Override
    public void onClick(View view) {
        // get id called view element
        int view_id = view.getId();
        switch (view_id) {
            case R.id.fe_calendar_endd_ib:
                Log.d(LOG_TAG, "endDate_et");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_endd_tv:
                Log.d(LOG_TAG, "endDate_ib");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_startd_tv:
                Log.d(LOG_TAG, "startDate_et");
                openCalendar(view_id);
                break;
            case R.id.fe_calendar_startd_ib:
                Log.d(LOG_TAG, "startDate_ib");
                openCalendar(view_id);
                break;
            case R.id.fe_send_email_bt:
                sendUserData();
                break;
        }
    }

    private void sendUserData() {
        BaseDataMaster baseDataMaster = BaseDataMaster.getDataMaster(fContext);
        if (lastUserEmailData == null || lastUserEmailData.size() == 0) {
            lastUserEmailData = baseDataMaster.getEmailData();
        }

        // Save data entered by the user in variables
        latestEmail = emailEt.getText().toString();
        latestSubject = subjectEt.getText().toString();
        latestMessage = messageEt.getText().toString();

        // If the user changed the last saved data, we will update them
        if (!latestEmail.equals(lastUserEmailData.get(BaseDataHelper.User.EMAIL))
                || !latestSubject.equals(lastUserEmailData.get(BaseDataHelper.User.SUBJECT))
                || !latestMessage.equals(lastUserEmailData.get(BaseDataHelper.User.MESSAGE))) {

            //Save the new data to the local database
            lastUserEmailData.put(BaseDataHelper.User.EMAIL, emailEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.SUBJECT, subjectEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.MESSAGE, messageEt.getText().toString());

            baseDataMaster.insertEmailData(lastUserEmailData);
        }
        // Get all user event from sqlDB to allUserEventLit
        allUserEventList = new ArrayList<>();
        filterEventUserList = new ArrayList<>();

        DBHandler dbHandler = new DBHandler(fContext);
        Cursor curCSV = dbHandler.readAllEventsFromDB();
        while (curCSV.moveToNext()) {
            //Which column you want to exprort
            String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                    curCSV.getString(3), curCSV.getString(4), curCSV.getString(5)};
            allUserEventList.add(arrStr);
        }
        curCSV.close();

        // Do filter to send data
        String startData = startDataTv.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        long startDataMillisec = 0;
        String endData = endDataTv.getText().toString();
        long endDataMilisec = 0;

        try {
            Date start = dateFormat.parse(startData);
            startDataMillisec = start.getTime();
            Date end = dateFormat.parse(endData);
            endDataMilisec = end.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }


        for (String[] temp : allUserEventList) {
            long start = Long.parseLong(temp[4]);
            long end = Long.parseLong(temp[5]);

            if (start > startDataMillisec && end < endDataMilisec) {
                filterEventUserList.add(temp);
            }
        }

        for (int i = 0; filterEventUserList.size() > i; i++) {
            String[] temp = filterEventUserList.get(i);
            for (String name : notSendEventName) {
                if (temp[2].contains(name)) {
                    filterEventUserList.remove(i);
                }
            }
        }


        // extract the * .csv file to send
        File csvFile = createCSVFIle(filterEventUserList);
        String authoritets = fContext.getApplicationContext().getPackageName();
        Uri fileUri = FileProvider.getUriForFile(fContext, authoritets, csvFile);

        // Create content with action to send
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // We fill in the data: type of text, address, subject and the actual text of the letter
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{latestEmail});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, latestSubject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, latestMessage);
        emailIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        /* go to change*/
        fContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public File createCSVFIle(ArrayList<String[]> listToSend) {
        final String USER_NAME_PREFERENCES = "user_name_sp";
        final String USER_NAME = "name";
        String username = "default";

        SharedPreferences sharedPreferences = fContext.getSharedPreferences(USER_NAME_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(USER_NAME)) {
            if (!sharedPreferences.getString(USER_NAME, "").isEmpty()) {
                username = sharedPreferences.getString(USER_NAME, "");
            }
        }


        String fileName = username + "-timeSheetApp.csv";
        File exportDir = new File(Environment.getExternalStorageDirectory(), "csv_patch");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, fileName);

        try {
            file.createNewFile();
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            for (String[] temp : listToSend) {
                writer.writeNext(temp);
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * ГWe'll go to the calendar widget's widget display and data processing
     *
     * @param view_id - id view of the element by which the user touched.
     *                  depending on it, we process the information according to our
     */
    private void openCalendar(int view_id) {
        // Проверяем userDataMap на null.
        if (userDataMap == null) {
            userDataMap = new HashMap<>();
        }

        DialogFragment dialogFragment = new FragmentExport.DatePicker();
        if (view_id == R.id.fe_calendar_endd_tv || view_id == R.id.fe_calendar_endd_ib) {
            dataKey = END;
        } else if (view_id == R.id.fe_calendar_startd_tv || view_id == R.id.fe_calendar_startd_ib) {
            dataKey = START;
        }
        dialogFragment.show(getFragmentManager(), "dataPicker");
    }

    /**
     * This class is used to show the user a DataPicker dialog
     * It implements the logic of storing a start and end date in HashMap
     */
    @SuppressLint("ValidFragment")
    protected class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        // The key is responsible for what date we are reading, start data or end data;
        // key can have 2 values: start or end;
        private String key = null;

        /**
         * Show User DataPiker dialog
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // determine the current date
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            Dialog picker = new DatePickerDialog(getActivity(), this,
                    year, month, day);
            return picker;
        }

        /**
         * The method stores the data received by the dataPicker from the user
         *            *
         *   * @param datePicker - calendar widget
         *           * @param year is the year chosen by the user
         *           * @param month - the month chosen by the user
         *           * @param day - day selected by the user
         *          
         */
        @Override
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int month, int day) {
            // Declare the EditText variable, which we will later assign a link to
            // startData || endDataTv (EditText)
            // This is necessary in order to specify the selected date in the correct EditText
            TextView varEditText = startDataTv;
            key = dataKey;
            // Depending on the key, we assign varEditText the link we need
            if (key != null) {
                if (key.equals(START)) {
                    varEditText = startDataTv;
                } else varEditText = endDataTv;
            }
            // Save the data in the HashMap
            userDataMap.put(key + DAY, day);
            // Do increment ++month because
            // the months count in the datePicker starts with 0 and not with 1
            userDataMap.put(key + MONTH, ++month);
            userDataMap.put(key + YEAR, year);
            // set new var in EditText
            varEditText.setText(day + " / " + month + " / " + year);
        }
    }

    // We show the list of user activities
    private void showIncludeItems() {

       // listActivity = new ArrayList<>();
        this.listActivity=mainActivity.getListLogActivity();
        if(this.listActivity.size()<=0)
        {

        DBHandler dbHandler = new DBHandler(fContext);
        Cursor cursor = dbHandler.readAllEventsFromDB();


        while (cursor.moveToNext()) {
            String name= cursor.getString(cursor.getColumnIndex("eventName"));
            int color;
            try {
                color = Integer.parseInt(cursor.getString(cursor.getColumnIndex("color")));
            } catch (Exception ex) {
                color = Color.WHITE;
                Log.d("EXCEPTION", "");
            }
            listActivity.add(new ButtonActivity(name,color));

            Log.d(LOG_TAG, "getActivities");
        }
        cursor.close();
        }

        CustomArrayAdapter arrayListArrayAdapter = new CustomArrayAdapter(fContext, listActivity);

        includeLv.setAdapter(arrayListArrayAdapter);
    }

    /**
     * -----------------------------------------------------------------------------
     */

    public class CustomArrayAdapter extends ArrayAdapter<ArrayList<ButtonActivity>> {

        public CustomArrayAdapter(Context context, ArrayList values) {
            super(context, R.layout.list_item, values);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.include_activity_item, null);
            }
          TextView textView = (TextView) v.findViewById(R.id.ia_action_name);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.ia_checkbox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (notSendEventName == null) {
                        notSendEventName = new ArrayList<>();
                    }
                    // If the user unchecked, we delete the activity from the list to send

                    if (!compoundButton.isChecked()) {
                        for (ButtonActivity bt : listActivity) {
                            if (bt.name.equals(listActivity.get(position).name)) {
                                notSendEventName.add(listActivity.get(position).name);
                            }
                        }
                    } else {
                        for (int i = 0; notSendEventName.size() > i; i++) {
                            if (notSendEventName.get(i).contains(listActivity.get(position).name)) {
                                notSendEventName.remove(i);
                            }
                        }
                    }

                }
            });
            ButtonActivity activity = listActivity.get(position);
            textView.setText(activity.name);
            textView.setBackgroundColor(activity.color);
            return v;
        }
    }


}
