package io.pivotal.weatherbus.app;

import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    @Inject
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
        EditText editText = (EditText) subject.findViewById(R.id.username);
        assertThat(editText).isNotNull();
    }

    @Test
    public void onButtonClick_whenUserNameNotEmpty_shouldCallService() {
        String userId = "bob";
        when(service.getStopIds(anyString())).thenReturn(new ArrayList<String>());

        ((EditText) subject.findViewById(R.id.username)).setText(userId);
        subject.findViewById(R.id.submitButton).performClick();

        verify(service, times(1)).getStopIds(userId);
    }

    @Test
    public void onButtonClick_whenUsernameEmpty_shouldNotCallService() {
        ((EditText) subject.findViewById(R.id.username)).setText("");
        subject.findViewById(R.id.submitButton).performClick();

        verify(service, never()).getStopIds(anyString());
    }

    @Test
    public void onButtonClick_whenNoStopsAreFound_shouldToast() {
        String userId = "bob";
        when(service.getStopIds(userId)).thenReturn(new ArrayList<String>());

        ((EditText) subject.findViewById(R.id.username)).setText(userId);
        subject.findViewById(R.id.submitButton).performClick();

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("No stops found for this user");
    }
}