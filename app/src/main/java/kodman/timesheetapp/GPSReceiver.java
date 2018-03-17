package kodman.timesheetapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import kodman.timesheetapp.Database.DBHandler;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;


//Class for getting and saving coordinates
public class GPSReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        //get last coordinates
        SharedPreferences sPref = context.getSharedPreferences("GPSReceiver", MODE_PRIVATE);
        String prevLat = sPref.getString(Cnst.LATITUDE, "0");
        String prevLong = sPref.getString(Cnst.LONGITUDE, "0");
        //  Log.d(Cnst.TAG,"--------------------Recieve "+ prevLat+"|"+prevLong);
        workCoordinates(Double.parseDouble(prevLat), Double.parseDouble(prevLong));


    }

    //obtaining coordinates
    private void workCoordinates(Double curLat, Double curLong) {
        //Double[] coords=new Double[]{curLat,curLong};
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        //   Log.d(Cnst.TAG,"Coors = "+locationManager);
        Location location;
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            float[] res = new float[1];
            if (location == null)
                return;
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), curLat, curLong, res);


            if (res[0] > 100) {

                //write to Shared
                SharedPreferences sPref = context.getSharedPreferences("GPSReceiver", MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putString(Cnst.LATITUDE, String.valueOf(location.getLatitude()));
                editor.putString(Cnst.LONGITUDE, String.valueOf(location.getLongitude()));
                editor.commit();

                DBHandler dbHandler = new DBHandler(context);

                //write to DataBase
                long count = dbHandler.writeToGPS(System.currentTimeMillis(), -1, location.getLatitude(), location.getLongitude());

            }

        }
    }
}
