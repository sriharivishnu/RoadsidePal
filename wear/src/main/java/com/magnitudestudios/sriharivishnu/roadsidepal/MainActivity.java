package com.magnitudestudios.sriharivishnu.roadsidepal;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.title);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
