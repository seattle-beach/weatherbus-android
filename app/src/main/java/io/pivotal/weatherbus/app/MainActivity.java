package io.pivotal.weatherbus.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.List;

public class MainActivity extends RoboActivity {
    @InjectView(R.id.submitButton) Button button;
    @InjectView(R.id.username) EditText userName;

    @Inject
    WeatherBusService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userName.getText().toString();
                if(userId.isEmpty()) {
                    return;
                }
                List<String> stopIds = service.getStopIds(userId);
                if (stopIds.isEmpty()) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "No stops found for this user", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }
}
