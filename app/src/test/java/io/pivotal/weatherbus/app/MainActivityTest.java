package io.pivotal.weatherbus.app;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.google.inject.Inject;
import io.pivotal.weatherbus.app.services.StopForUserResponse;
import io.pivotal.weatherbus.app.services.WeatherBusService;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    @Inject
    WeatherBusService service;

    @InjectMocks
    MainActivity subject;

    String userName;
    PublishSubject<List<StopForUserResponse>> publishSubject;
    List<StopForUserResponse> response;

    @Before
    public void setUp() {
        subject = Robolectric.setupActivity(MainActivity.class);
        userName = "bob";
        publishSubject = PublishSubject.create();
        when(service.getStopForUser(userName)).thenReturn(publishSubject);
        response = new ArrayList<StopForUserResponse>() {{
            add(new StopForUserResponse());
            get(0).setId("1_1234");
            get(0).setName("STOP 0");
            add(new StopForUserResponse());
            get(1).setId("1_2345");
            get(1).setName("STOP 1");
        }};
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
        ((EditText) subject.findViewById(R.id.username)).setText(userName);
        subject.findViewById(R.id.submitButton).performClick();

        verify(service, times(1)).getStopForUser(userName);
    }

    @Test
    public void onButtonClick_whenUsernameEmpty_shouldNotCallService() {
        ((EditText) subject.findViewById(R.id.username)).setText("");
        subject.findViewById(R.id.submitButton).performClick();

        verify(service, never()).getStopForUser(anyString());
    }

    @Test
    public void onButtonClick_whenNoStopsAreFound_shouldToast() {
        ((EditText) subject.findViewById(R.id.username)).setText(userName);
        subject.findViewById(R.id.submitButton).performClick();
        publishSubject.onNext(new ArrayList<StopForUserResponse>());
        publishSubject.onCompleted();

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("No stops found for this user");
    }

    @Test
    public void onButtonClick_whenStopsAreFound_shouldPopulateListView() {
        ((EditText) subject.findViewById(R.id.username)).setText(userName);
        subject.findViewById(R.id.submitButton).performClick();
        publishSubject.onNext(response);
        publishSubject.onCompleted();

        ListView stopList = (ListView) subject.findViewById(R.id.stopList);
        assertThat(stopList.getAdapter().getCount()).isEqualTo(2);
    }

    @Test
    public void onSecondButtonClick_onlyNewStopsShouldShow() {
        ((EditText) subject.findViewById(R.id.username)).setText(userName);
        subject.findViewById(R.id.submitButton).performClick();
        publishSubject.onNext(response);
        publishSubject.onCompleted();

        publishSubject = PublishSubject.create();
        when(service.getStopForUser("bob2")).thenReturn(publishSubject);
        ((EditText) subject.findViewById(R.id.username)).setText("bob2");
        subject.findViewById(R.id.submitButton).performClick();
        publishSubject.onNext(new ArrayList<StopForUserResponse>() {{
            add(new StopForUserResponse());
            get(0).setId("1_1236");
            get(0).setName("STOP 2");
        }});
        publishSubject.onCompleted();

        ListView stopList = (ListView) subject.findViewById(R.id.stopList);
        assertThat(stopList.getAdapter().getCount()).isEqualTo(1);
    }

    @Test
    public void onButtonClick_whenErrorRetrievingStops_shouldToast() {
        ((EditText) subject.findViewById(R.id.username)).setText(userName);
        subject.findViewById(R.id.submitButton).performClick();
        publishSubject.onError(new Throwable("nop"));

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Error retrieving stops for this user");
    }
}