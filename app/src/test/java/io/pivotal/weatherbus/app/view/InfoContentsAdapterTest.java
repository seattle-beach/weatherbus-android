package io.pivotal.weatherbus.app.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class InfoContentsAdapterTest {

    @Mock WeatherBusMarker marker;
    String markerTitle;
    String markerSnippet;

    InfoContentsAdapter subject;

    @Before
    public void setup() throws Exception {
        subject = new InfoContentsAdapter();
        Activity activity = Robolectric.setupActivity(MapStopsFragmentTest.MockActivity.class);
        subject.setContext(activity);
        markerTitle = "MY STOP (W)";
        markerSnippet = "Routes: a, b, c, d";
        when(marker.getTitle()).thenReturn(markerTitle);
        when(marker.getSnippet()).thenReturn(markerSnippet);
    }

    @Test
    public void getInfoWindow_shouldReturnNull() {
        assertThat(subject.getInfoWindow(marker)).isNull();
    }

    @Test
    public void getInfoContents_shouldDisplayTitleOfMarker() {
        View view = subject.getInfoContents(marker);
        assertThat(view).isNotNull();

        TextView titleView = ButterKnife.findById(view, R.id.title);
        assertThat(titleView.getText().toString()).isEqualTo("MY STOP (W)");

        TextView routesView = ButterKnife.findById(view, R.id.routes);
        assertThat(routesView.getText().toString()).isEqualTo(markerSnippet);
    }

    @Test
    public void getInfoContents_returnsNull_whenNoContextIsSet() {
        subject.setContext(null);
        assertThat(subject.getInfoContents(marker)).isNull();
    }

    public static class MockActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }
}