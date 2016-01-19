package io.pivotal.weatherbus.app;

import android.widget.Button;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    @Mock
    WeatherBusService service;

    @InjectMocks
    MainActivity subject;

    @Before
    public void setUp() {
        subject = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void onCreate_shouldHaveGUI() {
        Button button = (Button) subject.findViewById(R.id.submitButton);
        assertThat(button).isNotNull();
    }
}