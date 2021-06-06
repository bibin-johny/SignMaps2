package com.signmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {


    Animation top,bottom;
    ImageView image1,image2;
    private static String TAG = SplashActivity.class.getName();
        private static long SLEEP_TIME = 600;    // Time in seconds to show the picture

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar

            setContentView(R.layout.activity_splash);  //your layout with the picture

            top= AnimationUtils.loadAnimation(this,R.anim.top);
            bottom= AnimationUtils.loadAnimation(this,R.anim.bottom);

            image1=findViewById(R.id.im);
            image2=findViewById(R.id.si);

            image1.setAnimation(top);
            image2.setAnimation(bottom);


            // Start timer and launch main activity
            IntentLauncher launcher = new IntentLauncher();
            launcher.start();
        }

        private class IntentLauncher extends Thread {
            @Override
            /**
             * Sleep for some time and than start new activity.
             */
            public void run() {
                try {
                    // Sleeping
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                // Start main activity
                Intent intent2 = new Intent(SplashActivity.this, DetectorActivity.class);
                SplashActivity.this.startActivity(intent2);
                SplashActivity.this.finish();
            }
        }}