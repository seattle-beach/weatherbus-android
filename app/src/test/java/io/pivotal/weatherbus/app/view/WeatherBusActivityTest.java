package io.pivotal.weatherbus.app.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.services.StopForLocationResponse;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class WeatherBusActivityTest {
    @Inject SavedStops favoriteStops;

    WeatherBusActivity subject;
    StopForLocationResponse response;
    BusRoutesFragment busRoutesFragment;

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);

        subject = Robolectric.setupActivity(FakeWeatherBusActivity.class);

        response = new StopForLocationResponse() {{
            setStops(new ArrayList<BusStopResponse>() {{
                add(new BusStopResponse("1_1234", "STOP 0", "S", 4.2 , 4.3));
                add(new BusStopResponse("1_2234", "STOP 1", "NW", 4.4 , 4.5));
            }});
        }};
    }

    @Test
    public void onCreate_toolbarShouldShowCorrectly() {
        assertThat(subject.findViewById(R.id.progress_bar).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(subject.findViewById(R.id.bus_info).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onCreate_shouldLoadMapStopsFragment() {
        verify(FakeWeatherBusActivity.fragmentTransaction).add(eq(R.id.fragment_container), any(MapStopsFragment.class), eq("mapFragment"));
        verify(FakeWeatherBusActivity.fragmentTransaction).commit();
    }

    @Test
    public void onStopsLoaded_shouldUpdateToolBar() {
        subject.onStopsLoaded();
        assertThat(subject.findViewById(R.id.progress_bar).getVisibility()).isEqualTo(View.GONE);
        assertThat(subject.findViewById(R.id.bus_info).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(subject.findViewById(R.id.toolbar_favorite_button).getVisibility()).isEqualTo(View.GONE);

        TextView textView = ButterKnife.findById(subject, R.id.toolbar_title);

        assertThat(textView.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(textView.getText().toString()).isEqualTo("SELECT A BUS STOP");
    }

    @Test
    public void onStopSelected_shouldDisplayNameInToolbar() {
        subject.onStopSelected(new BusStop(response.getStops().get(0)));

        TextView textView = ButterKnife.findById(subject, R.id.toolbar_title);
        assertThat(textView.getText().toString()).isEqualTo("STOP 0 (S)");
        assertThat(textView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onStopSelected_shouldDisplayFavoriteIconWithCorrectColor() {
        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});

        subject.onStopSelected(new BusStop(response.getStops().get(0)));

        ImageButton icon = ButterKnife.findById(subject, R.id.toolbar_favorite_button);

        assertThat(icon.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(icon.getColorFilter()).isNotNull();

        subject.onStopSelected(new BusStop(response.getStops().get(1)));
        assertThat(icon.getColorFilter()).isNull();
    }

    @Test
    public void onFavoriteClick_shouldToggleFavorite() {
        subject.onStopSelected(new BusStop(response.getStops().get(0)));
        ImageButton icon = ButterKnife.findById(subject, R.id.toolbar_favorite_button);

        icon.performClick();
        verify(favoriteStops).addSavedStop("1_1234");
        assertThat(icon.getColorFilter()).isNotNull();
        verify(FakeWeatherBusActivity.fragment).setSelectedFavorite(true);

        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});
        icon.performClick();
        verify(favoriteStops).deleteSavedStop("1_1234");
        assertThat(icon.getColorFilter()).isNull();
        verify(FakeWeatherBusActivity.fragment).setSelectedFavorite(false);
    }

    @Test
    public void onToolbarTitleClick_shouldOpenBusStopActivity() {
        subject.onStopSelected(new BusStop(response.getStops().get(1)));
        TextView title = ButterKnife.findById(subject, R.id.toolbar_title);

        when(FakeWeatherBusActivity.fragmentTransaction.replace(eq(R.id.fragment_container), any(BusRoutesFragment.class)))
                .then(new Answer<FragmentTransaction>() {
                    @Override
                    public FragmentTransaction answer(InvocationOnMock invocation) throws Throwable {
                        BusRoutesFragment fragment = (BusRoutesFragment) invocation.getArguments()[1];
                        busRoutesFragment = fragment;
                        return FakeWeatherBusActivity.fragmentTransaction;
                    }
                });

        title.performClick();

        verify(FakeWeatherBusActivity.fragmentTransaction).replace(eq(R.id.fragment_container), any(BusRoutesFragment.class));
        verify(FakeWeatherBusActivity.fragmentTransaction, times(2)).commit();
        assertThat(busRoutesFragment.getArguments().getString("stopId")).isEqualTo("1_2234");
        assertThat(busRoutesFragment.getArguments().getString("stopName")).isEqualTo("STOP 1");
    }

    @Test
    public void onToolbarTitleClick_shouldNotCrashIfClickedWithoutASelectedStop() {
        try {
            TextView title = ButterKnife.findById(subject, R.id.toolbar_title);
            title.performClick();
        } catch(Throwable e) {
            assertThat(e.toString()).isEqualTo(""); //CRASHED
        }
    }

    public static class FakeWeatherBusActivity extends WeatherBusActivity {

        static FragmentManager fragmentManager = mock(FragmentManager.class);
        static FragmentTransaction fragmentTransaction = mock(FragmentTransaction.class);
        static MapStopsFragment fragment = mock(MapStopsFragment.class);

        public FakeWeatherBusActivity() {
            reset(fragmentManager);
            reset(fragmentTransaction);
            when(fragmentManager.beginTransaction()).thenReturn(fragmentTransaction);
            when(fragmentManager.findFragmentByTag("mapFragment")).thenReturn(fragment);
            when(fragmentTransaction.add(anyInt(), any(Fragment.class), anyString())).thenReturn(fragmentTransaction);
            when(fragmentTransaction.replace(anyInt(), any(Fragment.class))).thenReturn(fragmentTransaction);
            when(fragmentTransaction.addToBackStack(anyString())).thenReturn(fragmentTransaction);
        }

        @NonNull
        @Override
        public FragmentManager getFragmentManager() {
            return fragmentManager;
        }
    }
}