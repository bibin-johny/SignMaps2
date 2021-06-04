package com.signmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;



public class Aboutus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element adsElement = new Element();
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription("Sign Maps is a Traffic Signs Recognition Based Navigation System.This provides accurate and timely way to manage traffic-sign inventory with a minimal human effort.")
                .addItem(new Element().setTitle("Version 1.0.0.0"))
                .addGroup("DRIVE SAFE WITH US!")
                .addEmail("Your mail id ")
                .addWebsite("Your website/")
                .addPlayStore("com.example.yourprojectname")   //Replace all this with your package name//Your instagram id
                .addGitHub("github")
                .addItem(createCopyright())
                .create();
        setContentView(aboutPage);
    }
    private Element createCopyright()
    {
        Element copyright = new Element();
        @SuppressLint("DefaultLocale") final String copyrightString = String.format("Copyright %d ", Calendar.getInstance().get(Calendar.YEAR));
        copyright.setTitle(copyrightString);
        // copyright.setIcon(R.mipmap.ic_launcher);
        copyright.setGravity(Gravity.CENTER);
        copyright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Aboutus.this,copyrightString,Toast.LENGTH_SHORT).show();
            }
        });
        return copyright;

    }
}