package com.peter.countdowndemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.peter.countdownview.CountdownView;

/**
 * @author Peter
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CountdownView countdownView = findViewById(R.id.id_cv);
        countdownView.setCountdownSecond(30 * 60);
    }
}
