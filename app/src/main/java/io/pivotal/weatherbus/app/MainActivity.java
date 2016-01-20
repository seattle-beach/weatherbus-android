package io.pivotal.weatherbus.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.List;

public class MainActivity extends RoboActivity {
    @InjectView(R.id.submitButton) Button button;

    @Inject
    WeatherBusService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.username);
                String userId = textView.getText().toString();

                List<String> stopIds = service.getStopIds(userId);
            }
        });
    }
}
