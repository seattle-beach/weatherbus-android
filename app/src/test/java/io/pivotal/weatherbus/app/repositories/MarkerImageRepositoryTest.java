package io.pivotal.weatherbus.app.repositories;

import com.google.android.gms.dynamic.zzd;
import com.google.android.gms.maps.model.BitmapDescriptor;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.map.MarkerImageFactory;
import io.pivotal.weatherbus.app.model.MarkerImageOptions;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class)
public class MarkerImageRepositoryTest {
    @Inject
    MarkerImageFactory markerImageFactory;

    @Mock
    MarkerImageFactory.MarkerImage markerImage;

    MarkerIconRepository subject;
    MarkerImageOptions options;
    BitmapDescriptor descriptorZero = new BitmapDescriptor(mock(zzd.class));
    BitmapDescriptor descriptorOne = new BitmapDescriptor(mock(zzd.class));

    @Before
    public void setUp() throws Exception {
        WeatherBusApplication.inject(this);
        options = new MarkerImageOptions("S", true);
        subject = new MarkerIconRepository();

        when(markerImageFactory.create(any(MarkerImageOptions.class))).thenReturn(markerImage);
        when(markerImage.draw()).thenReturn(descriptorZero, descriptorOne);
    }

    @Test
    public void getDescriptor_shouldReturnNewIconWhenEmpty() {
        assertThat(subject.get(options)).isNotNull();
        verify(markerImageFactory, times(1)).create(any(MarkerImageOptions.class));
    }

    @Test
    public void getDescriptor_whenCalledWithTheSameOptions_shouldNotCreateNewBitmap() {
        assertThat(subject.get(options)).isNotNull();
        assertThat(subject.get(options)).isNotNull();
        verify(markerImageFactory, times(1)).create(any(MarkerImageOptions.class));
    }

    @Test
    public void getDescriptor_whenCalledWithDifferentOptions_shouldCreateNewBitmap() {
        assertThat(subject.get(options)).isNotNull();
        assertThat(subject.get(new MarkerImageOptions("W", false))).isNotNull();
        verify(markerImageFactory, times(2)).create(any(MarkerImageOptions.class));
    }
}