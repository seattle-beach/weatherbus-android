package io.pivotal.weatherbus.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.StopForUserResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.List;

public class MainActivity extends RoboActivity {
    @InjectView(R.id.submitButton) Button button;
    @InjectView(R.id.username) EditText userName;
    @InjectView(R.id.stopList) ListView stopList;

    @Inject
    WeatherBusService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        stopList.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userName.getText().toString();
                if(userId.isEmpty()) {
                    return;
                }
                service.getStopIds(userId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<StopForUserResponse>>() {
                            @Override
                            public void call(List<StopForUserResponse> stopForUserResponses) {
                                adapter.clear();
                                for (StopForUserResponse stop : stopForUserResponses) {
                                    adapter.add(stop.getName());
                                }
                                if (adapter.getCount() == 0) {
                                    Toast.makeText(MainActivity.this.getApplicationContext(), R.string.no_stops_for_user, Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Toast.makeText(MainActivity.this.getApplicationContext(), "Error retrieving stops for this user", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }
        });
    }
}
