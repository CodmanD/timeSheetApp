package kodman.timesheetapp;
import android.content.res.Resources;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import kodman.timesheetapp.R;

/**
 * Created by dd on 07.11.2017.
 */

public class ButtonActivity {

   private String TAG="--------ButtonAcivity SAY: ";
    private Resources res=MainActivity.res;
    String name;
    int color;
    String time;
    String date;
    long ms;
    public ButtonActivity(String name) {
        this.name = name;
        this.color = getColor(this.name);
        this.ms = System.currentTimeMillis();
        setDatetime();
    }

    public ButtonActivity(String name, int color) {
        this.name = name;
        this.color = color;
        this.ms = System.currentTimeMillis();

        setDatetime();
    }
    public ButtonActivity(String name, int color, long ms) {
        this.name = name;
        this.color = color;
        this.ms = ms;


        setDatetime();
    }

    public void setDatetime() {
        Date startDate = new Date(this.ms);
        this.date = new SimpleDateFormat("dd.MM.yyyy").format(startDate);
        this.time = new SimpleDateFormat("HH:mm:ss").format(startDate);
    }

    public int getColor(String name) {
        Log.d(TAG,res.getString(R.string.nothing));
        Log.d(TAG,name);
        if (name.equals(res.getString(R.string.nothing)))
            this.color = res.getColor(R.color.colorNothing);

        if (name.equals(res.getString(R.string.relaxing)))
            this.color = res.getColor(R.color.colorRelaxing);
        if (name.equals(res.getString(R.string.sleeping)))
            this.color = res.getColor(R.color.colorSleeping);
        if (name.equals(res.getString(R.string.working)))
            this.color = res.getColor(R.color.colorWorking);
        if (name.equals(res.getString(R.string.exercising)))
            this.color = res.getColor(R.color.colorExercising);
        if (name.equals(res.getString(R.string.reading)))
            this.color = res.getColor(R.color.colorReading);
        if (name.equals(res.getString(R.string.travelling)))
            this.color = res.getColor(R.color.colorTravelling);
        if (name.equals(res.getString(R.string.eating)))
            this.color = res.getColor(R.color.colorEating);
        if (name.equals(res.getString(R.string.washing)))
            this.color = res.getColor(R.color.colorWashing);
        if (name.equals(res.getString(R.string.newButton)))
            this.color = res.getColor(R.color.colorText);

        return this.color;
    }

}