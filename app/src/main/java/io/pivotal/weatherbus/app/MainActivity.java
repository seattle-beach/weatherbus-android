package io.pivotal.weatherbus.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity {
    @InjectView(R.id.submitButton) Button button;

    WeatherBusService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("hi","hello");
            }
        });
    }
}
