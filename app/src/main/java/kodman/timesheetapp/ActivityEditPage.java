package kodman.timesheetapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by DI1 on 02.03.2018.
 */

public class ActivityEditPage extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_edit_page);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       Toolbar toolbar = this.findViewById(R.id.toolBarEditPage);
        this.setSupportActionBar(toolbar);
       // res = this.getResources();
       // toolbar = this.findViewById(R.id.toolBar_MainActivity);
       // this.setSupportActionBar(toolbar);
    }
}
