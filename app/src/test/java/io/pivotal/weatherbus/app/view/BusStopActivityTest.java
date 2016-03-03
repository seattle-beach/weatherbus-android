package io.pivotal.weatherbus.app.view;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.BusRoute;
import io.pivotal.weatherbus.app.services.StopResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class BusStopActivityTest {

    @Inject
    WeatherBusService service;

    BusStopActivity subject;

    String stopId = "1_1234";
    String stopName = "STOP 0";

    PublishSubject<StopResponse> stopResponseEmitter;

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);
        DateTimeZone.setDefault(DateTimeZone.UTC);

        stopResponseEmitter = PublishSubject.create();
        when(service.getStopInformation(stopId)).thenReturn(stopResponseEmitter);
        Intent intent = new Intent(ShadowApplication.getInstance().getApplicationContext(),
                BusStopActivity.class);
        intent.putExtra("stopId", stopId);
        intent.putExtra("stopName", stopName);
        subject = Robolectric.buildActivity(BusStopActivity.class).withIntent(intent).create().get();
    }

    @Test
    public void onCreate_shouldHaveStopNameAsTitle() {
        assertThat(subject.getTitle()).isEqualTo("STOP 0");
        assertThat(subject.findViewById(R.id.emptyRouteMessage).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onNextBusInfo_shouldFillListView() {
        final int millisInMinute = 60 * 1000;
        stopResponseEmitter.onNext(new StopResponse(
                new StopResponse.StopData(
                        new ArrayList<StopResponse.StopData.Departure>() {{
                            add(new StopResponse.StopData.Departure("0", "BUS 0", "S", 5 * millisInMinute, 6 * millisInMinute, 76));
                            add(new StopResponse.StopData.Departure("1", "BUS 1", "SW", 0, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("2", "BUS 2", "N", 4 * millisInMinute, 17 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("3", "BUS 3", "NE", 8 * millisInMinute, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("4", "BUS 4", "W", 19 * millisInMinute, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("5", "BUS 5", "E", 7 * millisInMinute, 7 * millisInMinute, 78.1));
                        }}
                )));
        ListView busList = (ListView) subject.findViewById(R.id.busList);
        assertThat(busList.getCount()).isEqualTo(6);

        shadowOf(busList).populateItems();
        View routeView = busList.getChildAt(0);
        TextView tv = (TextView) routeView.findViewById(R.id.routeNumber);
        assertThat(tv.getText().toString()).isEqualTo("0");
        tv = (TextView) routeView.findViewById(R.id.name);
        assertThat(tv.getText().toString()).isEqualTo("BUS 0");
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("early (1min)");
        tv = (TextView) routeView.findViewById(R.id.time);
        assertThat(tv.getText().toString()).isEqualTo("00:05");
        tv = (TextView) routeView.findViewById(R.id.temperature);
        assertThat(tv.getText().toString()).isEqualTo("76.0");

        routeView = busList.getChildAt(1);
        tv = (TextView) routeView.findViewById(R.id.routeNumber);
        assertThat(tv.getText().toString()).isEqualTo("1");
        tv = (TextView) routeView.findViewById(R.id.name);
        assertThat(tv.getText().toString()).isEqualTo("BUS 1");
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("scheduled time");
        tv = (TextView) routeView.findViewById(R.id.time);
        assertThat(tv.getText().toString()).isEqualTo("00:07");
        tv = (TextView) routeView.findViewById(R.id.temperature);
        assertThat(tv.getText().toString()).isEqualTo("78.1");

        routeView = busList.getChildAt(2);
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("very early (13min)");

        routeView = busList.getChildAt(3);
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("late (1min)");

        routeView = busList.getChildAt(4);
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("very late (12min)");

        routeView = busList.getChildAt(5);
        tv = (TextView) routeView.findViewById(R.id.status);
        assertThat(tv.getText().toString()).isEqualTo("on time");
    }

    @Test
    public void onNextBusInfo_shouldFillListViewWithLocalTimes() {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(-8));

        stopResponseEmitter.onNext(new StopResponse(
                new StopResponse.StopData(
                        new ArrayList<StopResponse.StopData.Departure>() {{
                            add(new StopResponse.StopData.Departure("0", "BUS 0", "SW", 5 * 60 * 1000, 6 * 60 * 1000, 76));
                        }}
                )));
        ListView busList = (ListView) subject.findViewById(R.id.busList);
        BusRoute expected = new BusRoute("0", "BUS 0", 5 * 60 * 1000, 6 * 60 * 1000, 76);
        BusRoute busRoute = (BusRoute) busList.getAdapter().getItem(0);
        assertThat(busRoute).isEqualTo(expected);

        shadowOf(busList).populateItems();
        View routeView = busList.getChildAt(0);
        TextView tv = (TextView) routeView.findViewById(R.id.time);
        assertThat(tv.getText().toString()).isEqualTo("16:05");
    }

    @Test
    public void onNoBusRoutes_shouldToastAnAlert() {
        stopResponseEmitter.onNext(new StopResponse(
                new StopResponse.StopData(new ArrayList<StopResponse.StopData.Departure>())));
        TextView message = (TextView) subject.findViewById(R.id.emptyRouteMessage);
        assertThat(message.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onErrorRetrievingBusRoutes_shouldToastAnError() {
        stopResponseEmitter.onError(new Throwable("everything's broken"));
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Failed to get bus routes");
    }
}