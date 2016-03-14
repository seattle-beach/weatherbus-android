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
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.BusStop;
import io.pivotal.weatherbus.app.repositories.FavoriteStopsRepository;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class WeatherBusActivityTest {
    @Inject
    FavoriteStopsRepository favoriteStops;
    @Mock MapStopsFragment mapStopsFragment;
    @Mock BusRoutesFragment busRoutesFragment;

    BusStop busStopZero;
    BusStop busStopOne;

    @InjectMocks
    FakeWeatherBusActivity subject;

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);

        subject = ActivityController.of(Robolectric.getShadowsAdapter(), subject).setup().get();

        busStopZero = new BusStop("1_1234", "STOP 0", "S", 4.2 , 4.3, new ArrayList<String>());
        busStopOne = new BusStop("1_2234", "STOP 1", "NW", 4.4 , 4.5, new ArrayList<String>());
    }

    @Test
    public void onCreate_toolbarShouldShowCorrectly() {
        assertThat(subject.findViewById(R.id.progress_bar).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(subject.findViewById(R.id.bus_info).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onCreate_shouldLoadAndHideBusRouteFragment() {
        verify(FakeWeatherBusActivity.fragmentTransaction).add(R.id.fragment_container, busRoutesFragment);
        verify(FakeWeatherBusActivity.fragmentTransaction).hide(busRoutesFragment);
        verify(FakeWeatherBusActivity.fragmentTransaction).commit();
    }

    @Test
    public void onCreate_shouldLoadMapStopsFragment() {
        verify(FakeWeatherBusActivity.fragmentTransaction).add(R.id.fragment_container, mapStopsFragment);
        verify(FakeWeatherBusActivity.fragmentTransaction, never()).hide(mapStopsFragment);
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
        subject.onStopSelected(busStopZero);

        TextView textView = ButterKnife.findById(subject, R.id.toolbar_title);
        assertThat(textView.getText().toString()).isEqualTo("STOP 0 (S)");
        assertThat(textView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onStopSelected_shouldDisplayFavoriteIconWithCorrectColor() {
        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});

        subject.onStopSelected(busStopZero);

        ImageButton icon = ButterKnife.findById(subject, R.id.toolbar_favorite_button);

        assertThat(icon.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(icon.getColorFilter()).isNotNull();

        subject.onStopSelected(busStopOne);
        assertThat(icon.getColorFilter()).isNull();
    }

    @Test
    public void onFavoriteClick_shouldToggleFavorite() {
        subject.onStopSelected(busStopZero);
        ImageButton icon = ButterKnife.findById(subject, R.id.toolbar_favorite_button);

        icon.performClick();
        verify(favoriteStops).addSavedStop("1_1234");
        assertThat(icon.getColorFilter()).isNotNull();
        verify(mapStopsFragment).setSelectedFavorite(true);

        when(favoriteStops.getSavedStops()).thenReturn(new ArrayList<String>() {{
            add("1_1234");
        }});
        icon.performClick();
        verify(favoriteStops).deleteSavedStop("1_1234");
        assertThat(icon.getColorFilter()).isNull();
        verify(mapStopsFragment).setSelectedFavorite(false);
    }

    @Test
    public void onToolbarTitleClick_shouldShowRouteFragmentAndHideMapFragment() {
        subject.onStopSelected(busStopOne);
        TextView title = ButterKnife.findById(subject, R.id.toolbar_title);

        title.performClick();

        verify(busRoutesFragment).setStopId("1_2234");
        verify(FakeWeatherBusActivity.fragmentTransaction).hide(mapStopsFragment);
        verify(FakeWeatherBusActivity.fragmentTransaction).show(busRoutesFragment);
        verify(FakeWeatherBusActivity.fragmentTransaction).
                setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
        verify(FakeWeatherBusActivity.fragmentTransaction, times(2)).commit();
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

        public FakeWeatherBusActivity() {
            reset(fragmentManager);
            reset(fragmentTransaction);
            when(fragmentManager.beginTransaction()).thenReturn(fragmentTransaction);
            when(fragmentTransaction.add(anyInt(), any(Fragment.class), anyString())).thenReturn(fragmentTransaction);
            when(fragmentTransaction.add(anyInt(), any(Fragment.class))).thenReturn(fragmentTransaction);
            when(fragmentTransaction.hide(any(Fragment.class))).thenReturn(fragmentTransaction);
            when(fragmentTransaction.show(any(Fragment.class))).thenReturn(fragmentTransaction);
            when(fragmentTransaction.replace(anyInt(), any(Fragment.class))).thenReturn(fragmentTransaction);
            when(fragmentTransaction.addToBackStack(anyString())).thenReturn(fragmentTransaction);
            when(fragmentTransaction.setCustomAnimations(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(fragmentTransaction);
        }

        @NonNull
        @Override
        public FragmentManager getFragmentManager() {
            return fragmentManager;
        }
    }
}