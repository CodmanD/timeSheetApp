package kodman.timesheetapp;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

import kodman.timesheetapp.R;

/**
 * Created by dd on 07.11.2017.
 */

public class ButtonActivity {

   private String TAG="--------ButtonAcivity SAY: ";
    private Resources res=MainActivity.res;

    private static SimpleDateFormat formatDate=new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    String name;
    String subName="";
    int subColor= Color.BLACK;
    int color;
    long endTime;
    long ms;
    double longitude;
    double latitude;
    String notes="";

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ButtonActivity(String name) {
        this.name = name;
        this.color = getColor(this.name);
        this.ms = System.currentTimeMillis();
       // setDatetime();
    }

    public ButtonActivity(String name, int color) {
        this.name = name;
        this.color = color;
        this.ms = System.currentTimeMillis();

      //  setDatetime();
    }
    public ButtonActivity(String name, int color, long ms) {
        this.name = name;
        this.color = color;
        this.ms = ms;
       // setDatetime();
    }



    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public int getSubColor() {
        return subColor;
    }

    public void setSubColor(int subColor) {
        this.subColor = subColor;
    }


    public String getStartTime(){return  formatTime.format(this.ms); }
    public String getStartDate(){return  formatDate.format(this.ms); }
    public String getEndTime(){return  formatTime.format(this.endTime); }
    public String getEndDate(){return  formatDate.format(this.endTime); }

    public int getColor(String name) {
       // Log.d(TAG,res.getString(R.string.nothing));
        //Log.d(TAG,name);
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