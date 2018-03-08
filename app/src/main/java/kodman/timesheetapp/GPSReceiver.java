package kodman.timesheetapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by DI1 on 08.03.2018.
 */

public class GPSReceiver  extends BroadcastReceiver {

    Context context;



    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;


             SharedPreferences sPref=context.getSharedPreferences("GPSReceiver", MODE_PRIVATE);

                String prevLat=sPref.getString(Cnst.LATITUDE,"0");
                String prevLong=sPref.getString(Cnst.LONGITUDE,"0");
                Log.d(Cnst.TAG,"--------------------Recieve "+ prevLat+"|"+prevLong);
                workCoordinates(Double.parseDouble(prevLat),Double.parseDouble(prevLong));


    }


    private Double[] workCoordinates(Double curLat,Double curLong)
    {
             Double[] coords=new Double[]{curLat,curLong};


            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
         //   Log.d(Cnst.TAG,"Coors = "+locationManager);
        Location location;
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            location =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
           float[] res=new float[1];
            Location.distanceBetween(location.getLatitude(),location.getLongitude(),coords[0],coords[1],res);

            Log.d(Cnst.TAG,"-------------Res = "+res[0]);
            if(res[0]>2)
            {
                Toast.makeText(context,"Cordinates changed",Toast.LENGTH_SHORT).show();
                SharedPreferences sPref=context.getSharedPreferences("GPSReceiver", MODE_PRIVATE);
                SharedPreferences.Editor editor=sPref.edit();
                editor.putString(Cnst.LATITUDE,String.valueOf(location.getLatitude()));
                editor.putString(Cnst.LONGITUDE,String.valueOf(location.getLongitude()));
                editor.commit();
            }
            else
                Toast.makeText(context,"Cordinates NOt changed",Toast.LENGTH_SHORT).show();

              coords[0]=location.getLatitude();
              coords[1]=location.getLongitude();
            return  coords;
        }

            return null;

    }
}
