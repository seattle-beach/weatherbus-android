package io.pivotal.weatherbus.app.view;

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
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MapActivityTest {
    @Inject SavedStops favoriteStops;

    @Mock MapStopsFragment fragment;
    @Mock FragmentTransaction fragmentTransaction;

    MapActivity subject;
    StopForLocationResponse response;

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);

        when(FakeMapActivity.fragmentManager.findFragmentById(eq(R.id.fragment_container))).thenReturn(fragment);
        when(FakeMapActivity.fragmentManager.beginTransaction()).thenReturn(fragmentTransaction);
        when(fragmentTransaction.add(eq(R.id.fragment_container), any(MapStopsFragment.class))).thenReturn(fragmentTransaction);
        subject = Robolectric.setupActivity(FakeMapActivity.class);

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
        verify(fragmentTransaction).add(eq(R.id.fragment_container), any(MapStopsFragment.class));
        verify(fragmentTransaction).commit();
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
        verify(fragment).setSelectedFavorite(true);

        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});
        icon.performClick();
        verify(favoriteStops).deleteSavedStop("1_1234");
        assertThat(icon.getColorFilter()).isNull();
        verify(fragment).setSelectedFavorite(false);
    }

    public static class FakeMapActivity extends MapActivity {

        static FragmentManager fragmentManager = mock(FragmentManager.class);

        @NonNull
        @Override
        public FragmentManager getFragmentManager() {
            return fragmentManager;
        }
    }
}