package io.pivotal.weatherbus.app.view;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
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
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.FragmentTestUtil;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class BusRoutesFragmentTest {

    @Inject WeatherBusService service;

    BusRoutesFragment subject;
    PublishSubject<StopResponse> stopResponseEmitter;
    ListView busList;

    @Before
    public void setup() throws Exception {
        WeatherBusApplication.inject(this);
        DateTimeZone.setDefault(DateTimeZone.UTC);

        stopResponseEmitter = PublishSubject.create();

        String stopId = "1_1234";

        when(service.getStopInformation(stopId)).thenReturn(stopResponseEmitter);
        subject = new BusRoutesFragment();
        FragmentTestUtil.startFragment(subject);
        subject.setStopId(stopId);

        assertThat(subject.getView()).isNotNull();
        busList = ButterKnife.findById(subject.getView(), R.id.busList);
    }

    @Test
    public void onNextBusInfo_shouldFillListView() {
        final int millisInMinute = 60 * 1000;
        stopResponseEmitter.onNext(new StopResponse(
                new StopResponse.StopData(
                        new ArrayList<StopResponse.StopData.Departure>() {{
                            add(new StopResponse.StopData.Departure("0", "BUS 0", 5 * millisInMinute, 6 * millisInMinute, 76));
                            add(new StopResponse.StopData.Departure("1", "BUS 1", 0, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("2", "BUS 2", 4 * millisInMinute, 17 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("3", "BUS 3", 8 * millisInMinute, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("4", "BUS 4", 19 * millisInMinute, 7 * millisInMinute, 78.1));
                            add(new StopResponse.StopData.Departure("5", "BUS 5", 7 * millisInMinute, 7 * millisInMinute, 78.1));
                        }}
                )));
        shadowOf(busList).populateItems();

        assertThat(busList.getCount()).isEqualTo(6);
        testRouteInfo(busList.getChildAt(0), "0", "BUS 0", "early (1min)", "00:05", "76.0");
        testRouteInfo(busList.getChildAt(1), "1", "BUS 1", "scheduled time", "00:07", "78.1");
        testRouteInfo(busList.getChildAt(2), "2", "BUS 2", "very early (13min)", "00:04", "78.1");
        testRouteInfo(busList.getChildAt(3), "3", "BUS 3", "late (1min)", "00:08", "78.1");
        testRouteInfo(busList.getChildAt(4), "4", "BUS 4", "very late (12min)", "00:19", "78.1");
        testRouteInfo(busList.getChildAt(5), "5", "BUS 5", "on time", "00:07", "78.1");
    }

    @Test
    public void onNextBusInfo_shouldFillListViewWithLocalTimes() {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(-8));

        stopResponseEmitter.onNext(new StopResponse(
                new StopResponse.StopData(
                        new ArrayList<StopResponse.StopData.Departure>() {{
                            add(new StopResponse.StopData.Departure("0", "BUS 0", 5 * 60 * 1000, 6 * 60 * 1000, 76));
                        }}
                )));
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
        TextView message = ButterKnife.findById(subject.getView(), R.id.emptyRouteMessage);
        assertThat(message.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onErrorRetrievingBusRoutes_shouldToastAnError() {
        stopResponseEmitter.onError(new Throwable("everything's broken"));
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Failed to get bus routes");
    }

    private void testRouteInfo(View routeView, String expectedNumber, String expectedName, String expectedStatus, String expectedTime, String expectedTemperature) {
        ButterKnife.findById(routeView, R.id.routeNumber);
        TextView tv = ButterKnife.findById(routeView, R.id.routeNumber);
        assertThat(tv.getText().toString()).isEqualTo(expectedNumber);
        tv = ButterKnife.findById(routeView, R.id.name);;
        assertThat(tv.getText().toString()).isEqualTo(expectedName);
        tv = ButterKnife.findById(routeView, R.id.status);;
        assertThat(tv.getText().toString()).isEqualTo(expectedStatus);
        tv = ButterKnife.findById(routeView, R.id.time);
        assertThat(tv.getText().toString()).isEqualTo(expectedTime);
        tv = ButterKnife.findById(routeView, R.id.temperature);
        assertThat(tv.getText().toString()).isEqualTo(expectedTemperature);
    }
}