package kodman.timesheetapp;

import java.text.SimpleDateFormat;

/**
 * Created by DI1 on 07.03.2018.
 */

public class mCall {
    String time;
    String duration;
    String number;
    String contact;
    private static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");


    public mCall(String time, String duration, String number, String contact) {
        this.time = time;
        this.duration = duration;
        this.number = number;
        this.contact = contact;
    }
    public mCall(String time, String duration,String contact) {
        this.time = time;
        this.duration = duration;
         this.contact = contact;
    }

    public String getTime() {
        return time;
    }


    public String getStartTime(){return  formatTime.format(Long.parseLong(this.time)); }
    public String getFinishTime(){return  formatTime.format(Long.parseLong(this.time)+(Long.parseLong(this.duration)*1000)); }
    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
