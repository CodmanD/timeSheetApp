package kodman.timesheetapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

import butterknife.BindView;
import kodman.timesheetapp.Database.DBHandler;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    private static boolean mIsCreateAvailable = true;
    private static final String TAG = "------Activity Say";
    private String mNewSummary;
    private int mNewColor;
    public Toolbar toolbar;
    private Menu menu;
    private int status = 0;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mSharedEditor;
    private ArrayList<ButtonActivity> listActivity = new ArrayList<ButtonActivity>();
    private ArrayList<ButtonActivity> listSubactivity = new ArrayList<>();

    private ArrayList<ButtonActivity> listLogActivity = new ArrayList<>();
    private ArrayAdapter<ButtonActivity> adapterListLogActivity;
    private String mDeleteTime;
    private String mUpdateTime;
    private String mNewStartTime;
    public static Resources res;
    private ListView lvActivity;
    private int mColor;
    private static String nameCalendar = "";
    private static String myName = "";
    private Long ms;
    static String actualTime;
    SharedPreferences sPref;

    private DBHandler dbHandler;

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    //create service geting coordinates
    private void restartGetGPS() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Cnst.REQUEST_PERMISSION_LOCATION);
        } else {
            Intent gpsIntent = new Intent(this, GPSReceiver.class);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, gpsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 4000, 300000, pendingIntent);
        }
    }

    //method UNDO
    public void undoClick(View view) {


        if (MainActivity.this.listLogActivity.size() == 0) return;
        if (!mIsCreateAvailable) {
            Toast.makeText(this, "Please wait.Previous operation is performed", Toast.LENGTH_SHORT).show();
            return;
        }
        ButtonActivity ba = MainActivity.this.listLogActivity.get(0);
        try {
            mDeleteTime = String.valueOf(ba.ms);
            dbHandler.deleteEventFromDb(String.valueOf(ba.ms));
            //  MainActivity.this.removeGoogleDiary();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MainActivity.this.listLogActivity.remove(ba);
        MainActivity.this.adapterListLogActivity.notifyDataSetChanged();


        if (listLogActivity.size() == 0) {
            sPref = getSharedPreferences("tempData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sPref.edit();
            editor.putBoolean("temp", true);
            editor.commit();
        }
    }


    //create List Buttons Activities for Screen Settingns
    private void addActivities() {
        LinearLayout LLActivities = this.findViewById(R.id.LLActivities);
        if (LLActivities.getChildCount() > 0)
            LLActivities.removeAllViews();

        LinearLayout LLSettings = this.findViewById(R.id.LLSettings);

        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linLayoutParam.setMargins(40, 10, 40, 10);
        final Button btnNew = new Button(this);

        //assing listener for the Button
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.clickNewButton(v, true);
            }
        });
        //add Last Button with title "+New"
        btnNew.setText(R.string.newButton);
        btnNew.setLayoutParams(linLayoutParam);
        LLActivities.addView(btnNew);

        for (int i = 0; i < this.listActivity.size(); i++) {
            final ButtonActivity ba = this.listActivity.get(i);
            final Button btn = new Button(this);

            //assing listeners for Buttons
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!ba.name.toLowerCase().equals("nothing"))
                        changeButtonAcivity(ba, btn, true);
                    else {
                        Toast.makeText(MainActivity.this, "Button cannot be changed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            btn.setText(ba.name);
            btn.setBackgroundColor(ba.getColor(ba.name));
            btn.setTextColor(getContrastColor(ba.color));
            btn.setLayoutParams(linLayoutParam);
            LLActivities.addView(btn);
        }
        LLSettings.invalidate();

    }

    //create List Buttons SubActivities for Screen Settingns
    private void addSubactivities() {
        LinearLayout LLSubactivities = this.findViewById(R.id.LLSubactivities);
        if (LLSubactivities.getChildCount() > 0)
            LLSubactivities.removeAllViews();

        LinearLayout LLSettings = this.findViewById(R.id.LLSettings);
        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linLayoutParam.setMargins(40, 10, 40, 10);
        final Button btnNew = new Button(this);

        //assing listener for the Button
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.clickNewButton(v, false);
            }
        });

        //add Last Button with title "+New"
        btnNew.setText(R.string.newButton);
        btnNew.setLayoutParams(linLayoutParam);
        LLSubactivities.addView(btnNew);


        for (int i = 0; i < this.listSubactivity.size(); i++) {
            final ButtonActivity ba = this.listSubactivity.get(i);
            final Button btn = new Button(this);

            //assing listeners for Buttons
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeButtonAcivity(ba, btn, false);

                }
            });
            btn.setText(ba.name);
            btn.setBackgroundColor(ba.getColor(ba.name));
            btn.setTextColor(getContrastColor(ba.color));
            btn.setLayoutParams(linLayoutParam);
            LLSubactivities.addView(btn);
        }
        LLSettings.invalidate();

    }

    //Create DIALOG For change or remove Activity
    private void changeButtonAcivity(final ButtonActivity ba, final Button btn, final boolean act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog, null);
        final EditText editText = view.findViewById(R.id.editTextDialog);
        editText.setText(ba.name);

        builder.setView(view)
                .setPositiveButton("Change this acivity", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String nameButton = editText.getText().toString();
                        //Checking max characters
                        //checking the button for uniqueness
                        if (!MainActivity.this.uniqueButtonActivity(nameButton, btn.getText().toString(), false, act)) {
                            Toast.makeText(MainActivity.this, "Activity with this name exists",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            changeButtonAcivity(ba, btn, act);
                        } else {
                            String oldName = ba.name;
                            ba.name = nameButton;
                            //add and set color for the button
                            setColorFromDialog(ba, false, act, oldName);
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton(res.getText(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (act) {
                            MainActivity.this.listActivity.remove(ba);
                            addActivities();
                        } else {
                            MainActivity.this.listSubactivity.remove(ba);
                            addSubactivities();
                        }

                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    //------For click on button +New  .variable act  for distinction Activity from Subactivity
    private void clickNewButton(final View v, final boolean act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog, null);
        final EditText editText = view.findViewById(R.id.editTextDialog);
        if (act) {
            editText.setText(R.string.newActivity);
            builder.setMessage(R.string.dialogNewButton);
        } else {
            editText.setText(R.string.newSubactivity);
            builder.setMessage(R.string.dialogNewButtonForSubactivities);
        }

        builder.setView(view)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        EditText editText = view.findViewById(R.id.editTextDialog);
                        String nameButton = editText.getText().toString();

                        //checking the button for uniqueness
                        if (!MainActivity.this.uniqueButtonActivity(nameButton, ((Button) v).getText().toString(), true, act)) {
                            Toast.makeText(MainActivity.this, "Activity with this name exists",
                                    Toast.LENGTH_SHORT).show();
                            clickNewButton(v, act);
                        } else {
                            final ButtonActivity ba = new ButtonActivity(nameButton);
                            String oldName = ba.name;

                            ba.name = nameButton;
                            //add and set color for the button
                            setColorFromDialog(ba, true, act, oldName);
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    //--------------------for color---------
    private void setColorFromDialog(final ButtonActivity ba, final boolean add, final boolean act, final String oldName) {
        //create Alert for the set colour
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Choise the color");
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.color_blender, null);
        builder.setView(view);

        final SeekBar fSeekBar = view.findViewById(R.id.fSeekBar);
        final SeekBar sSeekBar = view.findViewById(R.id.sSeekBar);

        final ImageView sColor = view.findViewById(R.id.imageView4);
        final ImageView fColor = view.findViewById(R.id.imageView3);
        final ImageView rColor = view.findViewById(R.id.imageView5);
        final ButtonActivity btnTmp = new ButtonActivity("", Color.RED);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
                //set colors for  widgets
                int color1[] = calcColor(fSeekBar.getProgress());
                int color2[] = calcColor(sSeekBar.getProgress());
                int colorR[] = calcColor(color1, color2);
                switch (seekBar.getId()) {
                    case R.id.fSeekBar: {
                        fColor.setBackgroundColor(Color.rgb(color1[0], color1[1], color1[2]));
                        break;
                    }
                    case R.id.sSeekBar: {
                        sColor.setBackgroundColor(Color.rgb(color2[0], color2[1], color2[2]));
                        break;
                    }
                }
                fColor.invalidate();
                rColor.setBackgroundColor(Color.rgb(colorR[0], colorR[1], colorR[2]));
                rColor.invalidate();
                // ba.color = Color.rgb(colorR[0], colorR[1], colorR[2]);
                btnTmp.color = Color.rgb(colorR[0], colorR[1], colorR[2]);
                //Toast.makeText(MainActivity.this, "returnColor =", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        };

        fSeekBar.setOnSeekBarChangeListener(listener);
        sSeekBar.setOnSeekBarChangeListener(listener);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //if new Activity to add in list
                //else only changing her
                ba.color = btnTmp.color;
                if (add) {
                    if (act) {
                        MainActivity.this.listActivity.add(ba);

                    } else {
                        MainActivity.this.listSubactivity.add(ba);
                    }
                }
                if (act) {
                    // Log.d(Cnst.TAG,"Change color"+ba.color);
                    dbHandler.updateEventColor(ba.color, ba.name, oldName);
                    //dbHandler.updateEventColorInLog(ba.color, ba.name,);
                    addActivities();
                } else

                {
                    //Log.d(Cnst.TAG,"Change color"+ba.name);
                    dbHandler.updateSubactivityColor(ba.color, ba.name, oldName);

                    addSubactivities();
                }

                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    int Red, Green, Blue;


    //calculate the color values
    public int[] calcColor(int progress) {
        if (progress == 0) {
            Red = 255;
            Green = 0;
            Blue = 0;
        }
        if (progress > 0 & progress <= 42) {
            Red = 255;
            Green = progress * 6 - 1;
            Blue = 0;
        } else if (progress > 42 & progress <= 84) {
            Red = 256 - (progress - 42) * 6 - 1;
            Green = 255;
            Blue = 0;
        } else if (progress > 84 & progress <= 126) {
            Red = 0;
            Green = 255;
            Blue = (progress - 84) * 6 - 1;
        } else if (progress > 126 & progress <= 168) {
            Red = 0;
            Green = 256 - (progress - 126) * 6 - 1;
            Blue = 255;
        } else if (progress > 168 & progress <= 210) {
            Red = (progress - 168) * 6 - 1;
            Green = 0;
            Blue = 255;
        } else if (progress > 210 & progress <= 252) {
            Red = 255;
            Green = 0;
            Blue = 256 - (progress - 210) * 6 - 1;
        }
        int[] color = {Red, Green, Blue};
        return color;
    }

    //mix colors
    public int[] calcColor(int[] color1, int[] color2) {
        int[] color = {(color1[0] + color2[0]) / 2, (color1[1] + color2[1]) / 2, (color1[2] + color2[2]) / 2};
        return color;
    }
    /////------End color----------


    // learn the uniqueness of the name
    private boolean uniqueButtonActivity(String nameActivity, String nameButton, boolean newButton, boolean act) {
        int count = 0;
        for (ButtonActivity ba : act ? MainActivity.this.listActivity : MainActivity.this.listSubactivity) {
            if (ba.name.toUpperCase().equals(nameActivity.toUpperCase())) {
                if (count == 1)
                    return false;
                if (newButton)
                    return false;
                else if (nameActivity.toUpperCase().equals(nameActivity.toUpperCase()))
                    count++;

            }
        }
        return true;
    }

    //add  widgets with available activities to Home Screen
    // and assing listeners for their
    private void addButtonsActivityToHome() {
        GridView gv = this.findViewById(R.id.gridView);

        sortActivitiesHome();
        ArrayAdapter<ButtonActivity> adapter =
                new ArrayAdapter<ButtonActivity>(this, R.layout.gridview_item,
                        R.id.btnItem, this.listActivity) {
                    @Override
                    public View getView(int position,
                                        View convertView, ViewGroup parent) {
                        final View view = super.getView(
                                position, convertView, parent);
                        final ButtonActivity ba = this.getItem(position);
                        Button btn = view.findViewById(R.id.btnItem);
                        btn.setBackgroundColor(ba.getColor(ba.name));
                        btn.setText(ba.name);
                        btn.setLines(1);
                        btn.setTextColor(getContrastColor(ba.color));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!mIsCreateAvailable) {
                                    Toast.makeText(MainActivity.this, "Please wait.Previous operation is performed",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!ba.name.toLowerCase().equals("nothing"))
                                    createDialogSubactivity(ba);
                                else {

                                    ba.setSubName(ba.name);
                                    ba.setSubColor(ba.color);
                                    MainActivity.this.dbHandler.writeEventWithSubToDB(ba.name, "", ""
                                            , String.valueOf(ba.ms), String.valueOf(ba.ms), ba.color,
                                            0, 0, ba.name, ba.color, "");

//                                //add formed activity
                                    MainActivity.this.listLogActivity.add(0, ba);
                                    MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                                    getListViewSize(MainActivity.this.lvActivity);

                                }
                            }
                        });

                        return view;
                    }
                };
        gv.setAdapter(adapter);
    }

    //For sorting buttonActivity on Screen Home
    private void sortActivitiesHome() {

        Collections.sort(listActivity, new Comparator<ButtonActivity>() {
            @Override
            public int compare(ButtonActivity b1, ButtonActivity b2) {

                if (b1.name.toLowerCase().equals("nothing"))
                    return -1;
                if (b2.name.toLowerCase().equals("nothing"))
                    return 1;
                return (int) (getTimeLastActivity(b2) - getTimeLastActivity(b1));
            }
        });
    }

    //getting time last activity
    private long getTimeLastActivity(ButtonActivity ba) {
        for (ButtonActivity b : listLogActivity) {
            if (b.name.equals(ba.name))
                return b.ms;
        }
        return -1;
    }


    //Method for add Activity to Log
    private void createDialogSubactivity(final ButtonActivity ba) {
        Log.d(TAG, "Create dialog with subactivities =" + listSubactivity.size());
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_subactivities, null);
        adb.setView(view);
        GridView gVSub = view.findViewById(R.id.gVSub);
        TextView tvAct = view.findViewById(R.id.tvCurAct2);
        tvAct.setText(ba.name);

        adb.setCancelable(true);
        final AlertDialog dialog = adb.create();

        ArrayAdapter<ButtonActivity> adapter =
                new ArrayAdapter<ButtonActivity>(this, R.layout.gridview_item,
                        R.id.btnItem, this.listSubactivity) {
                    @Override
                    public View getView(int position,
                                        View convertView, ViewGroup parent) {

                        View view = super.getView(position, convertView, parent);
                        final ButtonActivity SA = this.getItem(position);
                        Button btn = view.findViewById(R.id.btnItem);
                        btn.setBackgroundColor(SA.color);
                        btn.setText(SA.name);
                        btn.setLines(1);
                        btn.setTextColor(getContrastColor(SA.color));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                if (!mIsCreateAvailable) {
//                                    Toast.makeText(MainActivity.this, "Please wait.Previous operation is performed", Toast.LENGTH_SHORT).show();
//                                    return;
//                                }
                                dialog.dismiss();
                                ba.ms = System.currentTimeMillis();
                                ba.subName = SA.name;
                                ba.setSubColor(SA.color);

                                //Change endTime for previous activity in DataBase
                                if (listLogActivity.size() > 0)
                                    dbHandler.updateLastEventEndTime(String.valueOf(ba.ms));


                                //Add to DB
                                MainActivity.this.dbHandler.writeEventWithSubToDB(ba.name, "", ""
                                        , String.valueOf(ba.ms), String.valueOf(ba.ms), ba.color,
                                        0, 0, ba.getSubName(), ba.getSubColor(), "");

//                                //add formed activity
                                MainActivity.this.listLogActivity.add(0, ba);
                                MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
//                                MainActivity.this.lvActivity.setAdapter(MainActivity.this.adapterListLogActivity);
                                getListViewSize(MainActivity.this.lvActivity);

                                //AddtoGoogleDiary
                                //MainActivity.this.addGoogleDiary(ba);
                                dialog.dismiss();
                            }
                        });

                        return view;
                    }
                };

        gVSub.setAdapter(adapter);

        dialog.show();

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Cnst.TAG, "CANCEL");
                dialog.dismiss();
            }
        });
    }


    //Create dialog for adding notes
    private void createDialogNotes(final ButtonActivity ba) {

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_notes, null);
        adb.setView(view);
        adb.setCancelable(true);
        final AlertDialog dialog = adb.create();

        TextView tvTitles = view.findViewById(R.id.tvTitles);
        TextView tvTimes = view.findViewById(R.id.tvTimes);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        final EditText etNote = view.findViewById(R.id.etNote);


        tvTimes.setText(ba.getStartTime() + "-" + ba.getEndTime());
        tvTitles.setText(ba.name + "/" + ba.getSubName());

        etNote.setText(ba.getNotes());
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ba.setNotes(etNote.getText().toString());
                //Add to DB
                MainActivity.this.dbHandler.updateEventNotes(String.valueOf(ba.ms), ba.getNotes());
                //Update data for ListView
                MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //get list activities from Log
    public ArrayList<ButtonActivity> getListLogActivity() {
        return MainActivity.this.listLogActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //For working with DataBase
        dbHandler = new DBHandler(getApplicationContext());

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        res = this.getResources();
        toolbar = this.findViewById(R.id.toolBar_MainActivity);
        this.setSupportActionBar(toolbar);

        //---Read from SharedPreferences ------------
        mShared = getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);
        mSharedEditor = mShared.edit();
        sPref = this.getPreferences(MODE_PRIVATE);
        // Name & Calendar User
        myName = sPref.getString(Cnst.NAME, "");
        nameCalendar = sPref.getString(Cnst.NAME_CALENDAR, "");


        Log.d(TAG, "OnCREATE Initialzed list Activities");
        //Initialized list Activities
        if (sPref.contains(Cnst.SIZE_LIST_ACTIVITY) && this.listActivity.size() == 0) {
            int size = sPref.getInt(Cnst.SIZE_LIST_ACTIVITY, 0);
            //Log.d(TAG, " Initialzed list Activities From shareds =" + size);

            for (int i = 0; i < size; i++) {
                this.listActivity.add(new ButtonActivity
                        (sPref.getString(Cnst.NAME_ACTIVITY + i, ""),
                                sPref.getInt(Cnst.COLOR_ACTIVITY + i, res.getColor(R.color.colorText))));
            }
        } else {
            this.createList();

        }
        if (this.listActivity.size() == 0) {
            this.createList();
            //  Log.d(TAG,"============= DEFAULT listActivities");
        }

        //Initialized list Subactivities
        if (sPref.contains(Cnst.SIZE_LIST_SUBACTIVITY)) {
            int size = sPref.getInt(Cnst.SIZE_LIST_SUBACTIVITY, 0);
            for (int i = 0; i < size; i++) {
                this.listSubactivity.add(new ButtonActivity
                        (sPref.getString(Cnst.NAME_SUBACTIVITY + i, ""),
                                sPref.getInt(Cnst.COLOR_SUBACTIVITY + i, res.getColor(R.color.colorText))));
            }

        }

        //add  widgets with available ativities to Home Screen
        // and assing listeners for their
        readAcivitiesFromDB();
        this.createLog();
        this.addButtonsActivityToHome();

        //Start service for get coordinates
        restartGetGPS();
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Cnst.SCOPES))
                .setBackOff(new ExponentialBackOff());
        callCalendarApi(3);

//Read From DataBase
        // dbHandler.showBase(Cnst.CALENDAR_TABLE);




/*
For actual time, update every 1000 ms
 */
        new java.util.Timer().schedule
                (
                        new TimerTask() {
                            public void run() {
                                if (toolbar == null) return;
                                toolbar.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.actualTime = new SimpleDateFormat("HH:mm").format(new Date());
                                        toolbar.setTitle(MainActivity.actualTime);
                                        if (MainActivity.this.listLogActivity.size() > 0 && MainActivity.this.status == 0) {
                                            long timeDiff = System.currentTimeMillis() - MainActivity.this.listLogActivity.get(0).ms;
                                            MainActivity.actualTime = timeDiff / 60000 + ":" + (timeDiff % 60000) / 1000 + " mm:ss";
                                            ((TextView) MainActivity.this.findViewById(R.id.tvLastLap)).setText(MainActivity.actualTime);
                                        }

                                    }
                                });
                                // Log.d(TAG,"Timer Tick");
                            }
                        },
                        0, 1000);
    }


    //create Log Activity
    //and assing listeners for widgets
    private void createLog() {
        readAcivitiesFromDB();
        if (listActivity.size() == 0) return;

        this.lvActivity = this.findViewById(R.id.lvActivity);

        adapterListLogActivity = new ArrayAdapter<ButtonActivity>(this,
                R.layout.list_item, R.id.tvDate, listLogActivity) {
            @Override
            public View getView(final int position,
                                View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }

                View view = super.getView(position, convertView, parent);
                TextView tvDate = view.findViewById(R.id.tvDate);
                TextView tvStartTime = view.findViewById(R.id.tvStartTime);
                ImageView iv = view.findViewById(R.id.ivNotes);
                final Button btnA = view.findViewById(R.id.btnBA);
                final Button btnSA = view.findViewById(R.id.btnSA);
                ;
                final ButtonActivity ba = this.getItem(position);

                //   final ButtonActivity prevBa = this.getItem(position-1);
                tvDate.setText(ba.getStartDate());
                tvStartTime.setText(ba.getStartTime());

                btnA.setText(ba.name);
                btnA.setBackgroundColor(ba.color);
                btnA.setTextColor(getContrastColor(ba.color));

                btnSA.setText(ba.getSubName());
                btnSA.setBackgroundColor(ba.getSubColor());
                btnSA.setTextColor(getContrastColor(ba.getSubColor()));

                //Added Listeners for Views

                tvStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        //Change start time for activity
                        if (position < adapterListLogActivity.getCount() - 1)
                            changeTimeActivity(adapterListLogActivity.getItem(position + 1), ba);
                        else
                            changeTimeActivity(null, ba);
                    }
                });

                btnA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //method for change activity
                        changeNameActivity(ba, btnA);
                    }
                });

                btnSA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //method for change activity
                        if (!ba.name.toLowerCase().equals("nothing"))
                            changeNameSubActivity(ba, btnSA);
                    }
                });

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(MainActivity.this,ba.getNotes(),Toast.LENGTH_SHORT).show();
                        showEventInBase();
                        Log.d(TAG, "  =" + ba.ms);
                        createDialogNotes(ba);
                    }
                });


                //Add Icon for ImageView
                if (!ba.getNotes().equals("")) {
                    iv.setImageResource(R.drawable.ic_description_white_24dp);
                } else {
                    iv.setImageResource(R.drawable.ic_insert_drive_file_white_24dp);
                }


                return view;
            }
        };

        this.adapterListLogActivity.setNotifyOnChange(true);
        this.lvActivity.setAdapter(adapterListLogActivity);
        Log.d(TAG, " start setListHeigth");
        getListViewSize(lvActivity);

    }

    // changing the time for activity
    private void changeTimeActivity(final ButtonActivity prevBa, final ButtonActivity curBa) {

        final Date date = new Date(curBa.ms);

        final TimePickerDialog TPD = new TimePickerDialog(this, android.R.style.Theme_Holo_Dialog,
                null, date.getHours(), date.getMinutes(), true) {
            @Override
            public void onTimeChanged(TimePicker view,
                                      int hour, int minute) {

                date.setHours(hour);
                date.setMinutes(minute);
            }
        };
        TPD.show();
        TPD.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (prevBa != null) {
                    Log.d(TAG, "newTime=" + date.getTime() + "|" + prevBa.ms + "|" + curBa.endTime);
                    if (date.getTime() > prevBa.ms && (date.getTime() < curBa.endTime || curBa.endTime == 0)) {
                        //Toast.makeText(MainActivity.this, "Correct time", Toast.LENGTH_SHORT).show();
                        dbHandler.updateEventEndTime(String.valueOf(prevBa.ms), String.valueOf(date.getTime()), "", 0);
                        dbHandler.updateTimeEvents(String.valueOf(curBa.ms), String.valueOf(date.getTime()));
                        prevBa.endTime = date.getTime();
                        curBa.ms = date.getTime();
                    } else
                        Toast.makeText(MainActivity.this, "The selected time is not valid for selection", Toast.LENGTH_SHORT).show();
                } else {
                    if (date.getTime() < curBa.endTime || curBa.endTime == 0) {
                        //Toast.makeText(MainActivity.this, "Correct time", Toast.LENGTH_SHORT).show();
                        dbHandler.updateTimeEvents(String.valueOf(curBa.ms), String.valueOf(date.getTime()));
                        curBa.ms = date.getTime();
                    } else
                        Toast.makeText(MainActivity.this, "The selected time is not valid for selection", Toast.LENGTH_SHORT).show();

                }
                TPD.dismiss();

//                    //Update Time in GoogleDiary
//                    // updateGoogleDiary();
//                    //Change LogAcivity
                MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                MainActivity.this.lvActivity.setAdapter(MainActivity.this.adapterListLogActivity);
//
                createLog();

//                }
            }

        });


    }

    //initialize dialog for Buttons from Activity Log
    private void changeNameActivity(final ButtonActivity ba, final Button btn) {


        int size = MainActivity.this.listActivity.size();
        final String[] itemsAcivities = new String[size];
        for (int i = 0; i < size; i++) {
            itemsAcivities[i] = MainActivity.this.listActivity.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.MakeChoice);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_with_list, null);

        final Spinner spinner = view.findViewById(R.id.spinnerAcivityLog);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                        itemsAcivities);

        spinner.setAdapter(adapter);


        //helper class for data transfer
        class Swap {
            int flag = 0;
            int color;
        }
        final Swap s = new Swap();

        adapter.setDropDownViewResource(android.R.layout.
                simple_spinner_dropdown_item);

//assign listeners
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {

                if (s.flag == 0) {
                    for (int i = 0; i < parent.getCount(); i++) {
                        if (itemsAcivities[i].toUpperCase().equals(ba.name.toUpperCase())) {
                            parent.setSelection(i);
                            break;
                        }

                    }
                    s.flag = 1;
                } else {
                    ba.name = itemsAcivities[position];
                    s.color = listActivity.get(position).color;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?>
                                                  parent) {

            }
        });


        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // ba.name=tvButtonAcivity.getText().toString();
                        btn.setText(ba.name);
                        ba.color = s.color;
                        btn.setBackgroundColor(ba.color);
                        mNewColor = ba.color;
                        mNewSummary = ba.name;
                        mUpdateTime = String.valueOf(ba.ms);
                        //callCalendarApi(4);

                        dbHandler.updateEventNameColor(mUpdateTime, mNewSummary, mNewColor, "", 0);
                        btn.setTextColor(getContrastColor(ba.color));

                        createLog();
                        Toast.makeText(MainActivity.this, "Change" + spinner.getItemAtPosition(0).toString() + "now Name=" + ba.name, Toast.LENGTH_SHORT).show();

                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ba.name = btn.getText().toString();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder bldr = new AlertDialog.Builder(MainActivity.this);
                        bldr.setMessage(res.getString(R.string.delete) + "?")
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                mDeleteTime = String.valueOf(ba.ms);
                                                // callCalendarApi(0);
                                                dbHandler.deleteEventFromDb(mDeleteTime);
                                                MainActivity.this.listLogActivity.remove(ba);
                                                MainActivity.this.createLog();
                                                dlg.cancel();
                                            }
                                        })

                                .setNegativeButton(R.string.no,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                dlg.cancel();
                                            }
                                        });

                        bldr.create().show();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    //initialize dialog for Buttons from Activity Log
    private void changeNameSubActivity(final ButtonActivity ba
            , final Button btn) {

        final ButtonActivity bTmp = new ButtonActivity("");
        //Log.d(Cnst.TAG, "btn Name =" + btn.getText());
        int size = MainActivity.this.listSubactivity.size();
        final String[] itemsAcivities = new String[size];
        for (int i = 0; i < size; i++) {
            itemsAcivities[i] = MainActivity.this.listSubactivity.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.MakeChoice);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_with_list, null);

        final Spinner spinner = view.findViewById(R.id.spinnerAcivityLog);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                        itemsAcivities);

        spinner.setAdapter(adapter);


        //helper class for data transfer
        class Swap {
            int flag = 0;
            int color;
        }
        final Swap s = new Swap();

        adapter.setDropDownViewResource(android.R.layout.
                simple_spinner_dropdown_item);

//assign listeners
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {

                if (s.flag == 0) {
                    for (int i = 0; i < parent.getCount(); i++) {
                        // if (itemsAcivities[i].toUpperCase().equals(btn.getText().toString().toUpperCase()))
                        {
                            parent.setSelection(i);
                            bTmp.name = itemsAcivities[position];
                            s.color = listSubactivity.get(position).color;
                            // Log.d(Cnst.TAG, "Spinner Else  Color =ba=" + s.color);
                            break;
                        }

                    }
                    s.flag = 1;
                } else {

                    // ba.name = itemsAcivities[position];
                    bTmp.name = itemsAcivities[position];
                    s.color = listSubactivity.get(position).color;
                    //  Log.d(Cnst.TAG, "Spinner Else  Color =ba=" + s.color);
                }
                //  Log.d(Cnst.TAG, "Spinner ba=" + ba.name);
            }

            @Override
            public void onNothingSelected(AdapterView<?>
                                                  parent) {

            }
        });


        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // ba.name=tvButtonAcivity.getText().toString();
                        btn.setText(bTmp.name);
                        //ba.color = s.color;

                        btn.setBackgroundColor(s.color);
                        /**
                         * Fom update CalendarAPI
                         *  mNewColor = ba.color;
                         * mNewSummary = ba.name;
                         *mUpdateTime = String.valueOf(ba.ms);
                         *callCalendarApi(4);
                         */

                        dbHandler.updateEventSubNameColor(String.valueOf(ba.ms), bTmp.name, s.color);
                        btn.setTextColor(getContrastColor(ba.color));

                        createLog();
                        Toast.makeText(MainActivity.this, "Change" + spinner.getItemAtPosition(0).toString() +
                                " now Name=" + bTmp.name, Toast.LENGTH_SHORT).show();

                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ba.name = btn.getText().toString();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder bldr = new AlertDialog.Builder(MainActivity.this);
                        bldr.setMessage(res.getString(R.string.delete) + "?")
                                .setCancelable(false)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                mDeleteTime = String.valueOf(ba.ms);
                                                // callCalendarApi(0);
                                                dbHandler.deleteEventFromDb(mDeleteTime);
                                                //MainActivity.this.listLogSubactivity.remove(ba);
                                                MainActivity.this.createLog();
                                                dlg.cancel();
                                            }
                                        })

                                .setNegativeButton(R.string.no,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dlg,
                                                                int id) {
                                                dlg.cancel();
                                            }
                                        });

                        bldr.create().show();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }


    //initialize widgets for Settings screen
    private void createScreenSettings() {
        this.setContentView(R.layout.screen_settings);
        toolbar = this.findViewById(R.id.toolBar_Setting);
        toolbar.setTitle(MainActivity.actualTime);
        this.setSupportActionBar(toolbar);

        //initialized layout  from available activities
        // this.createGridLayoutSettings();
        addActivities();
        addSubactivities();
        String name = sPref.getString("myCalendar", "");
        EditText editTextCalendar = this.findViewById(R.id.editTextCalendar);
        editTextCalendar.setText(MainActivity.nameCalendar);

        EditText editTextName = this.findViewById(R.id.editTextName);
        editTextName.setText(MainActivity.myName);

    }


    // Saved Data From Field MyName & My Calendar
    public void clickSaveSettings(View view) {
        sPref = this.getPreferences(MODE_PRIVATE);

        EditText editTextName = this.findViewById(R.id.editTextName);
        EditText editTextCalendar = this.findViewById(R.id.editTextCalendar);
        Toast.makeText(this, "Click Save:" + nameCalendar + " MYName" + myName, Toast.LENGTH_SHORT).show();
        nameCalendar = editTextCalendar.getText().toString();
        myName = editTextName.getText().toString();
        //validate fields
        if (nameCalendar.equals("") || myName.equals("")) {
            Toast.makeText(this, "Fill in all the fields ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (myName.length() > 40) {
            nameCalendar = "";
            Toast.makeText(this, "Maximum 40 characters", Toast.LENGTH_SHORT).show();
            return;
        }


//Saved the data in SharedPreferences
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("myName", myName);
        ed.putString("myCalendar", nameCalendar);
        ed.commit();
        Toast.makeText(this, "Click Save:" + nameCalendar + " Name :" + myName, Toast.LENGTH_SHORT).show();
    }


    //Create menu for the toolbar
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        this.menu = menu;

        //Change colour for selected icon
        if (Build.VERSION.SDK_INT >= 21) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem mItem = this.menu.getItem(i);
                Drawable icon = mItem.getIcon();
                if (this.status == i)
                    icon.setTint(getResources().getColor(R.color.colorActiveIcon));
                else
                    icon.setTint(getResources().getColor(R.color.colorNoActiveIcon));
            }
        }
        return true;
    }


    //Action for menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_home:
                this.status = 0;
                this.setContentView(R.layout.activity_main);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                toolbar = this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitle(MainActivity.actualTime);
                this.setSupportActionBar(toolbar);
                this.addButtonsActivityToHome();
                this.createLog();
                return true;
            case R.id.action_edit_page:
                this.status = 1;

                Intent intent = new Intent(MainActivity.this, ActivityEditPage.class);

                String accountName = getPreferences(Context.MODE_PRIVATE)
                        .getString(Cnst.PREF_ACCOUNT_NAME, null);

                //Log.d(Cnst.TAG, "Main Cal_ID= " + mCalendarId + "|" + accountName);
                intent.putExtra(Cnst.PREF_ACCOUNT_NAME, accountName);
                intent.putExtra(Cnst.CALENDAR_ID, mCalendarId);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                this.status = 2;
                createScreenSettings();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            case R.id.action_export:
                this.status = 3;
                //action for Screen Email
                openFragmentExport();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;


            case R.id.action_share:
                this.status = 4;
                this.setContentView(R.layout.screen_share);
                toolbar = this.findViewById(R.id.toolBar_MainActivity);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                toolbar.setTitle(MainActivity.actualTime);
                this.setSupportActionBar(toolbar);
                String appPackageName = "kodman.timesheetapp";

                //Action for Share
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                //   Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //action for Screen Email
    private void openFragmentExport() {
        final String USER_NAME_PREFERENCES = "user_name_sp";
        final String USER_NAME = "name";

        // save user name, to set name from *.csv file
        String userName = mCredential.getSelectedAccountName();
        SharedPreferences sPrefUserName;

        try {
            sPrefUserName = getSharedPreferences(USER_NAME_PREFERENCES, Context.MODE_PRIVATE);
            if (sPrefUserName.contains(USER_NAME)) {
                if (!sPrefUserName.getString(USER_NAME, "").equals(userName)) {
                    SharedPreferences.Editor editor = sPrefUserName.edit();
                    editor.clear();
                    editor.putString(USER_NAME, userName);
                    editor.apply();
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // open FragmentExport
        this.setContentView(R.layout.activity_main_fragment);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, new FragmentExport()).commit();

    }


    //------For event onClick button Clean Log --
    public void clickCleanLog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogCleanLog)
                .setCancelable(false)
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MainActivity.this.listLogActivity.clear();
                        clearLogActivityFromDB();
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showEventInBase() {


        Cursor cursor = dbHandler.readAllEventsFromDB();
        // int count = cursor.getColumnCount();

        long startTime;
        long endTime;
        int color;
        String id;
        String name;
        String subName;
        String notes;
        int subColor;

        while (cursor.moveToNext()) {

            id = cursor.getString(cursor.getColumnIndex("_id"));
            name = cursor.getString(cursor.getColumnIndex("eventName"));
            subName = cursor.getString(cursor.getColumnIndex("subName"));
            notes = cursor.getString(cursor.getColumnIndex("notes"));

            startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));

            try {
                endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));
            } catch (Exception ex) {
                endTime = 0;
            }
            if (startTime == endTime) {
                endTime = 0;
            }
            color = Integer.parseInt(cursor.getString(cursor.getColumnIndex("color")));
            subColor = Integer.parseInt(cursor.getString(cursor.getColumnIndex("subColor")));

            //Log.d(TAG, "Id:" + id + "Name = " + name + "Color = " + color + "start = " + startTime + "end = " + endTime);

            ButtonActivity ba = new ButtonActivity(name, color);
            ba.ms = startTime;
            ba.endTime = endTime;
            ba.setSubName(subName);
            ba.setSubColor(subColor);
            ba.setNotes(notes);

            Log.d(TAG, "Base ----= " + ba.name + "/" + subName + "/" + startTime + "/" + notes);
        }
        cursor.close();

    }

    //Add from DB Activities in AcivityLog
    private void readAcivitiesFromDB() {
        this.listLogActivity.clear();
        DBHandler mDbHandler = new DBHandler(getApplicationContext());

        Cursor cursor = mDbHandler.readAllEventsFromDB();
        int count = cursor.getColumnCount();
       /*
        String msg = "CalendarTable= ";
        for (int i = 0; i < count; i++) {
            msg += " | " + cursor.getColumnName(i);
        }
        Log.d(TAG, "Count = " + count + "   : " + msg);
*/

        long startTime;
        long endTime;
        int color = Color.WHITE;
        String id;
        String name;
        String subName;
        String notes;
        int subColor;
        while (cursor.moveToNext()) {

            id = cursor.getString(cursor.getColumnIndex("_id"));
            name = cursor.getString(cursor.getColumnIndex("eventName"));
            subName = cursor.getString(cursor.getColumnIndex("subName"));
            notes = cursor.getString(cursor.getColumnIndex("notes"));

            startTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeStart")));

            try {
                endTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("dateTimeEnd")));
            } catch (Exception ex) {
                Log.d(TAG, "Exception endTime= " + ex.getMessage());
                endTime = 0;
            }
            if (startTime == endTime) {
                endTime = 0;
            }
            try {
                color = Integer.parseInt(cursor.getString(cursor.getColumnIndex("color")));
            } catch (NumberFormatException e) {
                color = Color.WHITE;
            }
            subColor = Integer.parseInt(cursor.getString(cursor.getColumnIndex("subColor")));

            //Log.d(TAG, "Id:" + id + "Name = " + name + "start = " + startTime + "end = " + endTime);

            ButtonActivity ba = new ButtonActivity(name, color, subName, subColor, notes, startTime, endTime);
            this.listLogActivity.add(0, ba);
        }
        cursor.close();
        mDbHandler.closeDB();

    }

    //Clear all from  DataBase
    private void clearLogActivityFromDB() {
        DBHandler mDbHandler = new DBHandler(getApplicationContext());
        mDbHandler.clearEventsTable();
        Log.d(TAG, "Clean DB");
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("temp", true);
        editor.commit();
        mDbHandler.closeDB();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor ed = sPref.edit();

        //Log.d(TAG, "DESTROY SIZE=" + size);


        /**
         * Add in shareds allowable activities
         */
        ed.putInt("sizeListActivity", this.listActivity.size());
        for (int i = 0; i < this.listActivity.size(); i++) {
            ed.putString("buttonAcivityName" + i, this.listActivity.get(i).name);
            ed.putInt("buttonAcivityColor" + i, this.listActivity.get(i).color);
            //  Log.d(TAG, "SAVES " + listActivity.get(i).name);
        }

        ed.putInt("sizeListSubactivity", this.listSubactivity.size());
        for (int i = 0; i < this.listSubactivity.size(); i++) {
            ed.putString("buttonSubacivityName" + i, this.listSubactivity.get(i).name);
            ed.putInt("buttonSubacivityColor" + i, this.listSubactivity.get(i).color);
//            Log.d(TAG, "SAVES " + listActivity.get(i).name);
        }
        ed.commit();


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");


        super.onDestroy();
        this.listActivity.clear();
    }
//+++++++++++++++++++++++++++++++++++++UTIlits===========================================================
    //###########################################################################################

    //change  heigth  ListView
    public static void getListViewSize(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }
        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        myListView.setLayoutParams(params);
        // print height of adapter on log
        Log.i("height of listItem:", String.valueOf(totalHeight));
    }

    //  for create default List Activities
    private void createList() {
        this.listActivity.add(new ButtonActivity(res.getString(R.string.nothing)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.relaxing)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.sleeping)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.working)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.exercising)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.reading)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.travelling)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.eating)));
        this.listActivity.add(new ButtonActivity(res.getString(R.string.washing)));
    }

    //  for create default List Subactivities
    private void createListSubactivities() {

        for (int i = 1; i < 10; i++) {
            this.listSubactivity.add(new ButtonActivity("Sub " + i, Color.BLUE));

        }


    }

    public static int getContrastColor(int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? Color.BLACK : Color.WHITE;
    }

    //------for work with Google Diary--------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //#################################################################################################################
    GoogleAccountCredential mCredential;
    String mCalendarId;
    String mCalendarData[] = {null, null};
    String mStartTime;
    String mEndTime;
    boolean mIdFlag = false;

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    //TODO: change input parameters
    private void callCalendarApi(int action) {
        sPref = this.getPreferences(MODE_PRIVATE);
        mCalendarId = sPref.getString("myCalendar", "primary");
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d(Cnst.TAG, "ChooseAccount");
            chooseAccount();

        } else {
            new MakeRequestTask(mCredential, action).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(Cnst.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(Cnst.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                Log.d(Cnst.TAG, "AccountName =" + accountName);
                mCredential.setSelectedAccountName(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        Cnst.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    Cnst.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Cnst.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    callCalendarApi(3);
                }
                break;
            case Cnst.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Cnst.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        editor.commit();
                        Log.d(Cnst.TAG, "Activity Result back Account Name " + accountName);
                        mCredential.setSelectedAccountName(accountName);
                        callCalendarApi(3);
                        // need to change name from *.csv file
                        saveUserNameToSharedPref(accountName);
                    }
                }
                break;
            case Cnst.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    callCalendarApi(1);
                }
                break;
        }

    }

    private void saveUserNameToSharedPref(String accountName) {
        SharedPreferences userNameSp = getSharedPreferences(Cnst.USER_NAME_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userNameSp.edit();
        editor.putString(Cnst.USER_NAME, accountName);
        editor.apply();
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                Cnst.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private com.google.api.services.calendar.Calendar mService = null;

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<String[], Void, Void> {

        private int mAction;
        private String mSummary;
        private DBHandler mDbHandler = new DBHandler(getApplicationContext());
        private SharedPreferences sPref = getSharedPreferences("tempData", MODE_PRIVATE);

        MakeRequestTask(GoogleAccountCredential credential, int action) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("timeSheetApp")
                    .build();
            mAction = action;
        }


        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(String[]... params) {
            try {
                //Receiving action code to select action
                switch (mAction) {
                    case 4:
                        updateEventNameColor(mUpdateTime);
                        break;
                    case 3:
                        //nothing
                        break;
                    case 2:
                        updateEventTime();
                        break;
                    case 1:
                        //adding new event and synchronize offline events if device online
                        if (isDeviceOnline()) {
                            addEventToCalendar();
                            addUnsyncedEventsToCalendar();//
                        } else {
                            addEventToCalendar();
                        }
                        //put temp flag to sharedprefs to prevent flag resetting when app closes
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putBoolean("temp", false);
                        editor.commit();
                        break;
                    case 0:
                        deleteEventFromCalendar(mDeleteTime);
                        break;
                }
            } catch (UserRecoverableAuthIOException e) {
                //request permissions to access google calendar
                startActivityForResult(e.getIntent(), Cnst.REQUEST_AUTHORIZATION);
                mIsCreateAvailable = true;
            } catch (IOException e) {
                e.printStackTrace();
                mIsCreateAvailable = true;
            }

            return null;
        }

        private void deleteEventFromCalendar(String startTime) throws IOException {
            //set mIsCreateAvailable flag to prevent multiple function call of deleting
            mIsCreateAvailable = false;
            //if device online, get eventId to delete event from calendar, if not online set
            //"deleted" to eventId, to synchronize in future
            if (isDeviceOnline()) {
                try {
                    ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
                    mDbHandler.deleteEventFromDb(startTime);
                    mService.events().delete(mCalendarId, arrayList.get(1)).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mDbHandler.updateEventDelete(mStartTime, 1);
            }
            mDbHandler.closeDB();
            //set mIsCreateAvailable flag, to allow next actions
            mIsCreateAvailable = true;
        }

        private void updateEventTime() throws IOException {
            updateEventStartTime(mUpdateTime);
        }

        private void addEvent() throws IOException {
            mIsCreateAvailable = false;
            //creating new event, set start and end time, event name(summary)
            Event event = new Event().setSummary(mSummary);
            Date start = new Date(Long.parseLong(mStartTime));
            Date end = new Date(Long.parseLong(mEndTime));
            DateTime startDateTime = new DateTime(start, TimeZone.getTimeZone("UTC"));
            DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
            EventDateTime startTime = new EventDateTime()
                    .setDateTime(startDateTime);
            EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
            event.setStart(startTime);
            event.setEnd(endTime);
            String eventId = "";
            //get list of users calendars and set it id if available. If not available, set id as primary
            if (isDeviceOnline()) {
                String pageToken = null;
                do {
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();
                    for (CalendarListEntry calendarListEntry : items) {
                        if (mCalendarId.equals(calendarListEntry.getSummary()) || mCalendarId.equals(calendarListEntry.getId())) {
                            mCalendarId = calendarListEntry.getId();
                            mIdFlag = true;
                            break;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
                if (!mIdFlag) {
                    mCalendarId = "primary";
                }
                //insert created event to google calendar
                event = mService.events().insert(mCalendarId, event).execute();
                System.out.printf("Event created: %s\n", event.getHtmlLink());
                //get eventId and write event to local database
                eventId = event.getId();
                mDbHandler.writeOneEventToDB(mSummary, mCalendarId, eventId, mStartTime, mEndTime, mColor, 0, 1);
                mDbHandler.closeDB();
            } else {
                //if device not online set eventId "not_synced" and write in local database to sync
                //it when device become online
                mDbHandler.writeOneEventToDB(mSummary, mCalendarId, eventId, mStartTime, mEndTime, mColor, 0, 0);
                mDbHandler.closeDB();
            }
            mIsCreateAvailable = true;
        }

        private void addEventToCalendar() throws IOException {
            //when we creating event by first time("temp" flag was true by default), we set temporary parameters and "temp" flag
            //because we don't know end time of event
            if (sPref.getBoolean("temp", true)) {
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
                mEndTime = mStartTime;
                addEvent();
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("temp", false);
                editor.commit();
                //if "temp" flag was false, we get endTime and update previous event to set end time
                //and create new event
            } else {
                mEndTime = mCalendarData[1];
                updateEvent();
                mSummary = mCalendarData[0];
                mStartTime = mCalendarData[1];
                mEndTime = mStartTime;
                addEvent();
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("temp", true);
                editor.commit();
            }
        }

        private void updateEvent() throws IOException {
            mIsCreateAvailable = false;
            Cursor cursor = mDbHandler.readAllEventsFromDB();
            //get last event from database to get eventId of event and update it in calendar
            if (cursor.moveToLast()) {
                String eventId = cursor.getString(cursor.getColumnIndexOrThrow("eventId"));
                String calendarId = cursor.getString(cursor.getColumnIndexOrThrow("calendarId"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("dateTimeStart"));
                int synced = cursor.getInt(cursor.getColumnIndexOrThrow("synced"));
                // Retrieve the event from the API
                if (isDeviceOnline()) {
                    try {
                        if (synced != 1) {
                            //if event not synced, they have not eventId, updating event in local database to sync it in future
                            mDbHandler.updateEvent(startTime, mEndTime, eventId, 0);
                            mDbHandler.closeDB();
                            mIsCreateAvailable = true;
                            return;
                        }
                        Event event = mService.events().get(calendarId, eventId).execute();
                        // Make a change
                        Date end = new Date(Long.parseLong(mEndTime));
                        DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
                        EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
                        event.setEnd(endTime);
                        // Update the event
                        event = mService.events().update(calendarId, event.getId(), event).execute();
                        System.out.printf("Event end time updated: %s\n", event.getHtmlLink());
                        mDbHandler.updateEvent(startTime, mEndTime, eventId, 1);
                        mDbHandler.closeDB();
                    } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
                        //if bad id, change id to not synced to sync it in future and update
                        // event in local database
                        e.printStackTrace();
                        mDbHandler.updateEvent(startTime, mEndTime, eventId, 0);
                        mDbHandler.closeDB();
                    }
                } else {
                    //if device offline updating event in local database to sync it in future
                    mDbHandler.updateEvent(startTime, mEndTime, eventId, 0);
                    mDbHandler.closeDB();
                }
            }
            mDbHandler.closeDB();
            mIsCreateAvailable = true;
        }

        private void updateEventStartTime(String startTime) throws IOException {
            mIsCreateAvailable = false;
            //get needed event from database
            String newStartTime = mNewStartTime;
            String eventId = "";
            String calendarId = "";
            String endDb = "";
            String startDb = "";
            try {
                ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
                eventId = arrayList.get(1);
                calendarId = arrayList.get(0);
                endDb = arrayList.get(3);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return;
            }
            // Retrieve the event from the API by eventId
            Event event = mService.events().get(calendarId, eventId).execute();
            // Make a change
            Date start = new Date(Long.parseLong(newStartTime));
            Date end = new Date(Long.parseLong(endDb));
            DateTime startDateTime = new DateTime(start, TimeZone.getTimeZone("UTC"));
            DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
            EventDateTime eventStartTime = new EventDateTime().setDateTime(startDateTime);
            EventDateTime eventEndTime = new EventDateTime().setDateTime(endDateTime);
            event.setStart(eventStartTime);
            event.setEnd(eventEndTime);
            // Update the event
            if (isDeviceOnline()) {
                try {
                    event = mService.events().update(calendarId, event.getId(), event).execute();
                } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
                    e.printStackTrace();
                }
                System.out.printf("Event time update: %s\n", event.getHtmlLink());
                mDbHandler.updateEventStartTime(startTime, mNewStartTime, eventId, 1);
                mDbHandler.closeDB();
            } else {
                //if device offline, set eventId "not_synced" to update in future
                mDbHandler.updateEventStartTime(startTime, mNewStartTime, eventId, 0);
                mDbHandler.closeDB();
            }
            //when we change start time, we need to change end time of previous event
            try {
                ArrayList<String> previousArrayList = mDbHandler.readPreviousEventFromDB(startTime);
                eventId = previousArrayList.get(1);
                calendarId = previousArrayList.get(0);
                startDb = previousArrayList.get(3);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return;
            }
            // Retrieve the event from the API by eventId
            Event previousEvent = mService.events().get(calendarId, eventId).execute();
            // Make a change
            Date previousStart = new Date(Long.parseLong(startDb));
            Date previousEnd = new Date(Long.parseLong(newStartTime));
            DateTime previousStartDateTime = new DateTime(previousStart, TimeZone.getTimeZone("UTC"));
            DateTime previousEndDateTime = new DateTime(previousEnd, TimeZone.getTimeZone("UTC"));
            EventDateTime previousStartTime = new EventDateTime().setDateTime(previousStartDateTime);
            EventDateTime previousEndTime = new EventDateTime().setDateTime(previousEndDateTime);
            previousEvent.setStart(previousStartTime);
            previousEvent.setEnd(previousEndTime);
            // Update the event in calendar and local database
            if (isDeviceOnline()) {
                previousEvent = mService.events().update(calendarId, previousEvent.getId(), previousEvent).execute();
                System.out.printf("Event time update: %s\n", previousEvent.getHtmlLink());
                mDbHandler.updateEventEndTime(startTime, mNewStartTime, eventId, 1);
                mDbHandler.closeDB();
            } else {
                //if device offline, set eventId "not_synced" to update in future and update in local database
                eventId = "not_synced";
                mDbHandler.updateEventEndTime(startTime, mNewStartTime, eventId, 0);
                mDbHandler.closeDB();
            }
            mIsCreateAvailable = true;
        }

        private void updateEventNameColor(String startTime) throws IOException {
            mIsCreateAvailable = false;
            String newSummary = mNewSummary;
            int newColor = mNewColor;
            //get needed event from database
            ArrayList<String> arrayList = mDbHandler.readOneEventFromDB(startTime);
            String eventId = arrayList.get(1);
            String calendarId = arrayList.get(0);
            // Retrieve the event from the API
            Event event = mService.events().get(calendarId, eventId).execute();
            // Make a change
            event.setSummary(newSummary);
            // Update the event
            if (isDeviceOnline()) {
                event = mService.events().update(calendarId, event.getId(), event).execute();
                System.out.printf("Event name/color update: %s\n", event.getHtmlLink());
                mDbHandler.updateEventNameColor(startTime, newSummary, newColor, eventId, 1);
                mDbHandler.closeDB();
            } else {
                //if device offline, set eventId "not_synced" to update in future
                eventId = "not_synced";
                mDbHandler.updateEventNameColor(startTime, newSummary, newColor, eventId, 0);
                mDbHandler.closeDB();
            }
            mIsCreateAvailable = true;
        }

        private void addUnsyncedEventsToCalendar() throws IOException {
            mIsCreateAvailable = false;
            Log.e("start unsynced", "start unsynced");
            Cursor unsyncedEvents = mDbHandler.readUnsyncedEventFromDB();
            String startTimeDb;
            String eventNameDb;
            String endTimeDb;
            String tempEventId;
            int deleted;
            int synced;
            //get events marked "not_synced" and "deleted" from local database to sync it with google calendar
            while (unsyncedEvents.moveToNext()) {
                eventNameDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("eventName"));
                startTimeDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("dateTimeStart"));
                endTimeDb = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("dateTimeEnd"));
                tempEventId = unsyncedEvents.getString(unsyncedEvents.getColumnIndexOrThrow("eventId"));
                deleted = unsyncedEvents.getInt(unsyncedEvents.getColumnIndexOrThrow("deleted"));
                String pageToken = null;
                do {
                    //check if calendar id is available
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();
                    for (CalendarListEntry calendarListEntry : items) {
                        if (mCalendarId.equals(calendarListEntry.getSummary()) || mCalendarId.equals(calendarListEntry.getId())) {
                            mCalendarId = calendarListEntry.getId();
                            mIdFlag = true;
                            break;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);
                //if calendar id wrong, set calendar id "primary"
                if (!mIdFlag) {
                    mCalendarId = "primary";
                }
                //delete events marked "deleted" from google calendar and local database
                if (deleted == 1) {
                    mService.events().delete(mCalendarId, tempEventId).execute();
                    mDbHandler.deleteEventFromDb(startTimeDb);
                    System.out.printf("Event deleted: %s\n", startTimeDb);
                } else {
                    //if event has another mark, push it to google calendar
                    Event event = new Event().setSummary(eventNameDb);
                    Date start = new Date(Long.parseLong(startTimeDb));
                    Date end = new Date(Long.parseLong(endTimeDb));
                    DateTime startDateTime = new DateTime(start, TimeZone.getTimeZone("UTC"));
                    DateTime endDateTime = new DateTime(end, TimeZone.getTimeZone("UTC"));
                    EventDateTime startTime = new EventDateTime()
                            .setDateTime(startDateTime);
                    EventDateTime endTime = new EventDateTime().setDateTime(endDateTime);
                    event.setStart(startTime);
                    event.setEnd(endTime);
                    event = mService.events().insert(mCalendarId, event).execute();
                    String eventId = event.getId();
                    mDbHandler.deleteUnsyncedEventFromDb(startTimeDb);
                    System.out.printf("Event synced: %s\n", event.getHtmlLink());
                    mDbHandler.writeOneEventToDB(eventNameDb, mCalendarId, eventId, startTimeDb, endTimeDb, mColor, 0, 1);
                }

            }
            mDbHandler.closeDB();
            mIsCreateAvailable = true;
        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected void onCancelled() {
        }

    }

    //Add to Google diary
    private void addGoogleDiary(ButtonActivity ba) {
        //set event data to array and call calendarApi with needed action
        mCalendarData[0] = ba.name;
        mCalendarData[1] = String.valueOf(ms);
        mColor = ba.getColor(ba.name);
        callCalendarApi(1);
    }

    //Update google dairy
    private void updateGoogleDiary() {
        callCalendarApi(2);
    }

    //Delete From Google Diary
    private void removeGoogleDiary() {
        callCalendarApi(0);
    }

    //----------------End Block For Google Service------------------------------------------------------------




}
