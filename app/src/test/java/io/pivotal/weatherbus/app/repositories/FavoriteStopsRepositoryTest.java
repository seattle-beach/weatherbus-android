package io.pivotal.weatherbus.app.repositories;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class FavoriteStopsRepositoryTest {
    @Mock
    SharedPreferences settings;
    @Mock
    Editor editor;

    FavoriteStopsRepository subject;

    @Before
    public void setUp() {
        subject = new FavoriteStopsRepository(settings);
        when(settings.edit()).thenReturn(editor);
    }

    @Test
    public void onNoSavedStops_shouldHaveEmptyList() {
        when(settings.getString("saved_stops","")).thenReturn("");

        assertThat(subject.getSavedStops().size()).isEqualTo(0);
    }

    @Test
    public void onSomeSavedStops_shouldHaveSavedStops() {
        when(settings.getString("saved_stops","")).thenReturn("1_123,1_456,1_789");

        assertThat(subject.getSavedStops()).isEqualTo(new ArrayList<String>() {{
            add("1_123");
            add("1_456");
            add("1_789");
        }});
    }

    @Test
    public void onAddingNewSavedStop_shouldSaveNewStops() {
        when(settings.getString("saved_stops","")).thenReturn("");

        subject.addSavedStop("1_589");

        verify(editor,times(1)).putString("saved_stops","1_589");
        verify(editor,times(1)).apply();
    }

    @Test
    public void onAddingSavedStops_shouldSaveAllStops() {
        when(settings.getString("saved_stops","")).thenReturn("1_123,1_456,1_789");

        subject.addSavedStop("1_589");

        verify(editor,times(1)).putString("saved_stops","1_123,1_456,1_789,1_589");
        verify(editor,times(1)).apply();
    }

    @Test
    public void whenDeletingAnExistingStop_itShouldDeleteFromSettings() {
        when(settings.getString("saved_stops","")).thenReturn("1_123,1_456,1_789");
        subject.deleteSavedStop("1_456");
        verify(editor).putString("saved_stops","1_123,1_789");
        verify(editor).apply();
    }

    @Test
    public void whenDeletingLastSavedStop_itShouldCommitEmptyString() {
        when(settings.getString("saved_stops","")).thenReturn("1_456");
        subject.deleteSavedStop("1_456");
        verify(editor).putString("saved_stops","");
        verify(editor).apply();
    }

    @Test
    public void whenDeletingANotSavedStop_itShouldDoNothing() {
        when(settings.getString("saved_stops","")).thenReturn("1_123,1_456,1_789");
        subject.deleteSavedStop("1_589");
        verify(editor,times(1)).putString("saved_stops","1_123,1_456,1_789");
        verify(editor,times(1)).apply();
    }
}