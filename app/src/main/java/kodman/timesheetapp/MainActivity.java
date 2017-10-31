package kodman.timesheetapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import	android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ArrayList<ButtonActivity> listActivity= new ArrayList<>();//All buttons activity for current  time
    ArrayList<ButtonActivity> listLogActivity= new ArrayList<>();
    ArrayAdapter<ButtonActivity> adapterListLogActivity;
    ArrayList<ButtonActivity> listSetActivity= new ArrayList<>();

    Resources res;
    ListView lvActivity;
    Time startTime=new Time();
    Date startDate= new Date();
    DateFormat df= new DateFormat();
    class ButtonActivity
    {
       String name;
        int color;
        String time=new SimpleDateFormat("HH:mm:ss").format(startDate);
        String date=new SimpleDateFormat("dd.MM.yyyy").format(startDate);;

        public ButtonActivity()
        {

        }
        public ButtonActivity(String name)
        {

            this.name=name;
            this.color=getColor(this.name);

        }
        public ButtonActivity(String name,int color)
        {

            this.name=name;
            this.color=color;

        }
        private int getColor(String name)
        {
           // Resources res= MainActivity.this.getResources();
            if(name.equals(res.getString(R.string.nothing)))
                return res.getColor(R.color.colorNothing);
            if(name.equals(res.getString(R.string.relaxing)))
                return res.getColor(R.color.colorRelaxing);
            if(name.equals(res.getString(R.string.sleeping)))
                return res.getColor(R.color.colorSleeping);
            if(name.equals(res.getString(R.string.working)))
                return res.getColor(R.color.colorWorking);
            if(name.equals(res.getString(R.string.exercising)))
                return res.getColor(R.color.colorExercising);
            if(name.equals(res.getString(R.string.reading)))
                return res.getColor(R.color.colorReading);
            if(name.equals(res.getString(R.string.travelling)))
                return res.getColor(R.color.colorTravelling);
            if(name.equals(res.getString(R.string.eating)))
                return res.getColor(R.color.colorEating);
            if(name.equals(res.getString(R.string.washing)))
            return res.getColor(R.color.colorWashing);
            if(name.equals(res.getString(R.string.newButton)))
                return res.getColor(R.color.colorText);


            return res.getColor(R.color.colorButton);
        }

    }

    private void createList()
    {
        this.listActivity.add(new ButtonActivity(res.getString(R.string.nothing)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.relaxing)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.sleeping)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.working)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.exercising)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.reading)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.travelling)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.eating)) );
        this.listActivity.add(new ButtonActivity(res.getString(R.string.washing)) );
        //this.listActivity.add(new ButtonActivity(res.getString(R.string.newButton)) );
    }


    public void undoClick(View view)
    {
        ButtonActivity ba=MainActivity.this.listLogActivity.remove(0);
        MainActivity.this.adapterListLogActivity.remove(ba);
       createActivityLog();
        // MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
        getListViewSize(MainActivity.this.lvActivity);
        MainActivity.this.removeGoogleDiary(ba);
    }
    //for work with Google Diary
private void addGoogleDiary(ButtonActivity ba)
{
    Toast.makeText(MainActivity.this,
            "Add To Google Diary "+ba.name+" "+ba.date+"/"+ba.time, Toast.LENGTH_SHORT).show();
 Log.d(TAG,"Add to Google Diary");
}
private void removeGoogleDiary(ButtonActivity ba)
{
    Log.d(TAG,"Remove from Google Diary");
}
    //add  widgets To Layout for Current Activity
    private void addToGridViewButtonsActivity()
    {
        GridView gv=(GridView)this.findViewById(R.id.gridView);

        ArrayAdapter<ButtonActivity> adapter =
                new ArrayAdapter<ButtonActivity>(this,R.layout.gridview_item,
                R.id.btnItem, this.listActivity)
        {
            @Override
            public View getView(int position,
            View convertView, ViewGroup parent)
            {
                View view = super.getView(
                        position, convertView, parent);

               final ButtonActivity ba = this.getItem(position);

                Button btn=(Button)view.findViewById(R.id.btnItem);
                btn.setBackgroundColor(ba.getColor(ba.name));
                btn.setText(ba.name);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Date date= new Date();
                        ba.date=new SimpleDateFormat("dd.MM.yyyy").format(date);
                        ba.time=new SimpleDateFormat("HH:mm:ss").format(date);
                        MainActivity.this.listLogActivity.add(0,ba);

                        createActivityLog();
                       // MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                        getListViewSize(MainActivity.this.lvActivity);
                        MainActivity.this.addGoogleDiary(ba);

                        Toast.makeText(MainActivity.this,
                                "Выбран : " +
                                        ba.name+"lisLog Size="+adapterListLogActivity.getCount(), Toast.LENGTH_SHORT).show();
                    }
                });
              // Log.d(TAG,"getItem For GridView");
                return view;
            }
        };
         gv.setAdapter(adapter);
    }

    private static final String TAG="------Activity Say";

    private Toolbar toolbar;
    private Menu menu;
    private int status=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        System.out.println(TAG+"onCreate");
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       this.res= this.getResources();
      //  setContentView(R.layout.screen_settings);
      toolbar= (Toolbar)this.findViewById(R.id.toolBar_MainActivity);

      //if(toolbar==null)
      //    Log.d(TAG,"NULL");
        this.setSupportActionBar(toolbar);

       toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
       toolbar.setSubtitle("Time");

        //toolbar.setNavigationIcon(R.mipmap.ic_add_circle_white_36dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("333333333333333", "Icon Click");
            }
        });

        this.createList();
        this.addToGridViewButtonsActivity();
        this.createActivityLog();

       // this.lvActivity.
    }


    private void createActivityLog()
    {
        this.lvActivity=(ListView)this.findViewById(R.id.lvActivity);
       adapterListLogActivity =new ArrayAdapter<ButtonActivity>(this,
                        R.layout.list_item, R.id.tvForDate, listLogActivity)
                {
                    @Override
                    public View getView(int position,
                                        View convertView, ViewGroup parent)
                    {

                        if (convertView == null)
                        {
                            convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                        }

                        View view = super.getView( position,
                                convertView, parent);

                        final ButtonActivity  ba= this.getItem(position);

                        TextView tvDate = (TextView) view.
                                findViewById(R.id.tvForDate);

                        TextView tvStartTime = (TextView) view.
                                findViewById(R.id.tvForStartTime);

                        LinearLayout llForBA= (LinearLayout) view.
                                findViewById(R.id.llForBA);


                        String date=ba.date;
                        String time=ba.time;
                        tvDate.setText(ba.date);
                        tvStartTime.setText(ba.time);
                        final String name=ba.name;
                        final Button btnA=new Button(MainActivity.this);
                        btnA.setText(ba.name);
                        btnA.setBackgroundColor(ba.getColor(ba.name));
                        btnA.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                createDialogForLogActivity(ba,btnA);
                            }
                        });
                        if(llForBA.getChildCount()==0)
                            llForBA.addView(btnA);

                        Log.d(TAG,"Layout size = "+llForBA.getChildCount()+"  "+view.toString());
                        //tvYear.setText(String.valueOf(F.year));
                        return view;
                    }
                };
        //-- Назначение Адаптера Данных списку
//-- android.widget.ListView ---------------------
        this.lvActivity.setAdapter(adapterListLogActivity);
        //this. adapterListLogActivity.
        //this.lvActivity.
        Log.d(TAG," start setListHeigth");
       getListViewSize(lvActivity);
        //setListViewHeightBasedOnChildren(lvActivity);
       // this.lvActivity.
    }

    //Метод для изменения Activity Log
    private void createDialogForLogActivity(final ButtonActivity ba,final Button btn)
    {

       // Toast.makeText(MainActivity.this,"Click "+btnA.getText(),Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this,"Click "+ba.name,Toast.LENGTH_SHORT).show();

    AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialogLogActivityTitle);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog, null);
        final EditText editText=(EditText)view.findViewById(R.id.editTextDialog);
        editText.setText(ba.name);
        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ba.name=editText.getText().toString();
                btn.setText(ba.name);
                btn.setBackgroundColor(ba.getColor(ba.name));
                MainActivity.this.adapterListLogActivity.notifyDataSetChanged();
                MainActivity.this.getListViewSize(MainActivity.this.lvActivity);
                dialog.dismiss();
                Toast.makeText(MainActivity.this,ba.name,Toast.LENGTH_SHORT).show();
            }
        })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.listLogActivity.remove(ba);
                        MainActivity.this.adapterListLogActivity.remove(ba);
                        MainActivity.this.getListViewSize(MainActivity.this.lvActivity);
                        dialog.dismiss();
                    }
                });;
        AlertDialog dialog=builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    //МЕтод растягивает ListView
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


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        this.menu=menu;

        //Change colour for selected icon
        if (Build.VERSION.SDK_INT >= 21)
        {
        for(int i=0;i<menu.size();i++)
        {
            MenuItem mItem=this.menu.getItem(i);
            Drawable icon=mItem.getIcon();
            if(this.status==i)
                icon.setTint(getResources().getColor(R.color.colorActiveIcon));
            else
                icon.setTint(getResources().getColor(R.color.colorNoActiveIcon));
        }
        }
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id= item.getItemId();
        switch(id)
        {
            case R.id.action_home:
                this.status=0;
                this.setContentView(R.layout.activity_main);
                toolbar= (Toolbar)this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);
                this.addToGridViewButtonsActivity();

                this.createActivityLog();
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                this.status=1;
                this.setContentView(R.layout.screen_settings);
                toolbar= (Toolbar)this.findViewById(R.id.toolBar_Setting);
                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);
                this.setSupportActionBar(toolbar);

                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_export:
                this.status=2;
                this.setContentView(R.layout.screen_email);
                toolbar= (Toolbar)this.findViewById(R.id.toolBar_MainActivity);

                toolbar.setTitleTextColor(Color.WHITE);
                toolbar.setSubtitleTextColor(Color.WHITE);

                this.setSupportActionBar(toolbar);

                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                this.status=3;
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
