package com.nightmaredevs.cameralag;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        hideNavigation();
        verifyPermission();
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        hideNavigation();
        // Get the Camera instance as the activity achieves full user focus

    }
    public void hideNavigation(){
        this.getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                );

   // View decorView = getWindow().getDecorView();
    //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      //      | View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);

    }





    private void verifyPermission(){
        String [] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0])!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[1])!= PackageManager.PERMISSION_GRANTED ) {

                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);

        }
    }

    public void showCamera(){

    }

}
