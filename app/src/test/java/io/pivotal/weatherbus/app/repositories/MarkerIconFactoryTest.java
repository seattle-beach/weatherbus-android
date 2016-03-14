package io.pivotal.weatherbus.app.repositories;

import android.graphics.Bitmap;
import com.google.android.gms.dynamic.zzd;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.model.IconOptions;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class,
        shadows = MarkerIconFactoryTest.ShadowBitmapDescriptorFactory.class)
public class MarkerIconFactoryTest {

    MarkerIconFactory subject = new MarkerIconFactory(RuntimeEnvironment.application);
    IconOptions notFavoriteIcon = new IconOptions("", false);

    @Test
    public void initialTest() {
        assertThat(subject.create(notFavoriteIcon)).isNotNull();
    }

    @Implements(BitmapDescriptorFactory.class)
    public static class ShadowBitmapDescriptorFactory {
        
        static BitmapDescriptor descriptor = new BitmapDescriptor(mock(zzd.class));

        public ShadowBitmapDescriptorFactory() {
        }

        @Implementation
        public static BitmapDescriptor fromBitmap(Bitmap image) {
            return descriptor;
        }
    }
}