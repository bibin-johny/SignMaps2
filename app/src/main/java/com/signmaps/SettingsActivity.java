package com.signmaps;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.signmaps.R;


public class SettingsActivity extends AppCompatActivity {
    static boolean vstate;
    static boolean pstate;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        preferences = getSharedPreferences("PREFS",0);
        vstate=preferences.getBoolean("switch1",false);
        pstate=preferences.getBoolean("switch2",false);
        final Switch v = (Switch) findViewById(R.id.switch1);
        v.setChecked(vstate);
        final Switch p = (Switch) findViewById(R.id.switch2);
        p.setChecked(pstate);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                vstate =!vstate;
                v.setChecked(vstate);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("switch1",vstate);
                editor.apply();

            }
        });
        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                pstate =!pstate;
                p.setChecked(pstate);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("switch2",pstate);
                editor.apply();

            }
        });


    }


}
