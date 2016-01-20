package io.pivotal.weatherbus.app;

import android.widget.Button;
import android.widget.EditText;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Ignore
    @Test
    public void onButtonClick_shouldCallService() {
        String userId = "bob";
        when(service.getStopIds(userId)).thenReturn(new ArrayList<String>() {{
            add("123");
            add("124");
            add("125");
        }});

        EditText editText = (EditText) subject.findViewById(R.id.username);
        editText.setText(userId);

        Button button = (Button) subject.findViewById(R.id.submitButton);
        button.performClick();

        verify(service, times(1)).getStopIds(userId);
    }
}