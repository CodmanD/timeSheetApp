package kodman.timesheetapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    // Хранит последние данные введенные пользователем в соответсвующие поля
    private String latestEmail;
    private String latestSubject;
    private String latestMessage;

    private Context fContext;

    // Хранит данные выбранного периода отправки пользовательских данных
    private HashMap<String, Integer> userDataMap;
    // Хранит последние данные введенные польхователем в поля
    private HashMap<String, String> lastUserEmailData;
    // View elements
    private EditText emailEt;
    private EditText subjectEt;
    private EditText messageEt;
    private TextView startData;
    private TextView endData;
    private ImageButton startDCalendar;
    private ImageButton endDCalendar;
    private Button sendEmail;
    private ListView includeLv;
    private Toolbar toolbar;

    Resources res;
    ArrayList<ButtonActivity> listActivity;
    ArrayList<ButtonActivity> listActivityToSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Создем наш фрашмент и инициализируем его
        View view = inflater.inflate(R.layout.screen_email, container, false);
        fContext = view.getContext();
        res = view.getResources();
        setupUI(view);
        return view;
    }

    // Настраимваем и инициализируем все view элементы
    private void setupUI(View container) {
        toolbar = (Toolbar) container.findViewById(R.id.toolBar_MainActivity);
        emailEt = (EditText) container.findViewById(R.id.fe_email_et);
        subjectEt = (EditText) container.findViewById(R.id.fe_subject_et);
        messageEt = (EditText) container.findViewById(R.id.fe_message_et5);
        startData = (TextView) container.findViewById(R.id.fe_calendar_startd_tv);
        endData = (TextView) container.findViewById(R.id.fe_calendar_endd_tv);
        startDCalendar = (ImageButton) container.findViewById(R.id.fe_calendar_startd_ib);
        endDCalendar = (ImageButton) container.findViewById(R.id.fe_calendar_endd_ib);
        sendEmail = (Button) container.findViewById(R.id.fe_send_email_bt);
        includeLv = (ListView) container.findViewById(R.id.include_lv);

        startData.setOnClickListener(this);
        endData.setOnClickListener(this);
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

        // Если пользовватель уже вводил данные, автоматически зополняем поля
        if (!baseDataMaster.getEmailData().isEmpty()) {
            lastUserEmailData = baseDataMaster.getEmailData();
            emailEt.setText(lastUserEmailData.get(BaseDataHelper.User.EMAIL));
            subjectEt.setText(lastUserEmailData.get(BaseDataHelper.User.SUBJECT));
            messageEt.setText(lastUserEmailData.get(BaseDataHelper.User.MESSAGE));
        }

        // Устанваливаем в textView первый и последний день предыдущего месяца
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
        startData.setText("01/" + calendarFirst.get(Calendar.MONTH) + "/" + calendarFirst.get(Calendar.YEAR));
        endData.setText(sdf.format(lastDayOfMonth));

        showIncludeItems();
    }

    @Override
    public void onClick(View view) {
        // Получаем id вызванной view
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

        // Сохраняем данные введенные пользователем в переменные
        latestEmail = emailEt.getText().toString();
        latestSubject = subjectEt.getText().toString();
        latestMessage = messageEt.getText().toString();

        // Если пользователь менял последние сохраненные данные, обновзяем их
        if (!latestEmail.equals(lastUserEmailData.get(BaseDataHelper.User.EMAIL))
                || !latestSubject.equals(lastUserEmailData.get(BaseDataHelper.User.SUBJECT))
                || !latestMessage.equals(lastUserEmailData.get(BaseDataHelper.User.MESSAGE))) {

            // Сохраняем новые данные в локальную базу данных
            lastUserEmailData.put(BaseDataHelper.User.EMAIL, emailEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.SUBJECT, subjectEt.getText().toString());
            lastUserEmailData.put(BaseDataHelper.User.MESSAGE, messageEt.getText().toString());

            baseDataMaster.insertEmailData(lastUserEmailData);
        }


        // Готовим файл *.csv к отправке
        String fileName = createCsvFile();
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
        Uri path = Uri.fromFile(filelocation);
        // Создаем интент с экшеном на отправку
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        // Заполняем данными: тип текста, адрес, сабж и собственно текст письма
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, latestEmail);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, latestSubject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, latestMessage);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        /* Отправляем на выбор!*/
        fContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private String createCsvFile() {
        DBHandler dbHandler = new DBHandler(fContext);
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        String username = "default";
        String fileName = username + "-timeSheetApp.csv";
        File file = new File(exportDir, fileName);
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            Cursor curCSV = dbHandler.readAllEventsFromDB();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                        curCSV.getString(3), curCSV.getString(4), curCSV.getString(5)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }

        return fileName;
    }

    /**
     * Говимся к показу виджета календаря польхователю и обрвботки данныъ
     *
     * @param view_id - id view элемента по которому тапнул пользователь.
     *                в зависимости от него обрабатываем информацию по своему
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
     * Этот класс используется для показа пользователю DataPicker dialog
     * В нем реализована логика сохранения даты типа start и end в HashMap
     */
    @SuppressLint("ValidFragment")
    protected class DatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        // Ключ, отвечает за то, какую дату мы считываем, start data or end data;
        // key может иметь 2 значения : start или end;
        private String key = null;

        /**
         * Показываем виджет календаря пользователю
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // определяем текущую дату
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // создаем DatePickerDialog и возвращаем его
            Dialog picker = new DatePickerDialog(getActivity(), this,
                    year, month, day);
            return picker;
        }

        /**
         * Метод сохраняет данные полученные dataPicker от пользователя
         *
         * @param datePicker - виджет календаря
         * @param year       - год выбранный пользователем
         * @param month      - месяц выбранный пользователем
         * @param day        - день выбранный пользователем
         */
        @Override
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int month, int day) {
            // Объявляем переменну EditText, которой позже присвоем ссылку на
            // startData || endData (EditText)
            // Это нужно для того, что бы указать выбранную дату в правильном EditText
            TextView varEditText = startData;
            key = dataKey;
            // В зависимости от ключа, присваеваем varEditText нужную нам ссылку
            if (key != null) {
                if (key.equals(START)) {
                    varEditText = startData;
                } else varEditText = endData;
            }
            // Сохраняем нащи данные в HashMap
            userDataMap.put(key + DAY, day);
            // Делепем инкремент ++month потому что
            // счет месяцев в dataPicker наченается с 0 а не с 1
            userDataMap.put(key + MONTH, ++month);
            userDataMap.put(key + YEAR, year);
            // Присваеваем новое значение в EditText
            varEditText.setText(day + " / " + month + " / " + year);
        }
    }

    // Показываем список пользовательских активностей
    private void showIncludeItems() {

        listActivity = new ArrayList<>();

        DBHandler dbHandler = new DBHandler(fContext);
        Cursor cursor = dbHandler.readActivitiesFromDB();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            listActivity.add(new ButtonActivity(cursor.getString(1)));
            cursor.moveToNext();
            Log.d(LOG_TAG, "getActivities");
        }
        cursor.close();
        listActivityToSend = listActivity;
        CustomArrayAdapter arrayListArrayAdapter = new CustomArrayAdapter(fContext, listActivity);

        includeLv.setAdapter(arrayListArrayAdapter);
    }

    /**
     * -----------------------------------------------------------------------------
     */
    class ButtonActivity {
        String name;
        int color;

        public ButtonActivity(String name) {

            this.name = name;

        }

        public ButtonActivity(String name, int color) {

            this.name = name;
            this.color = color;

        }

    }

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

                    // Если пользоваетель снял галочку, удаляем активность из спика на отправку

                    if (!compoundButton.isChecked()) {
                        for (ButtonActivity bt : listActivityToSend) {
                            if (bt.name.equals(listActivity.get(position).name)) {
                                listActivityToSend.remove(position);
                            }
                        }
                    } else {
                        listActivityToSend.add(listActivity.get(position));
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
