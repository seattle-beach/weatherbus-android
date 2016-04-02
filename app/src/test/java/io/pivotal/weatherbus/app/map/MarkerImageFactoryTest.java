package io.pivotal.weatherbus.app.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.google.android.gms.dynamic.zzd;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import io.pivotal.weatherbus.app.BuildConfig;
import io.pivotal.weatherbus.app.model.MarkerImageOptions;
import io.pivotal.weatherbus.app.testUtils.WeatherBusTestRunner;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowCanvas;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(WeatherBusTestRunner.class)
@Config(constants = BuildConfig.class,
        shadows = {
                MarkerImageFactoryTest.ShadowBitmapDescriptorFactory.class,
                MarkerImageFactoryTest.ExtendedShadowCanvas.class
        })
public class MarkerImageFactoryTest {

    MarkerImageFactory subject;

    private MarkerImageFactory.MarkerImage noDirectionMarker;
    private MarkerImageFactory.MarkerImage northMarker;
    private MarkerImageFactory.MarkerImage northEastMarker;
    private MarkerImageFactory.MarkerImage eastMarker;
    private MarkerImageFactory.MarkerImage southEastMarker;
    private MarkerImageFactory.MarkerImage southMarker;
    private MarkerImageFactory.MarkerImage southWestMarker;
    private MarkerImageFactory.MarkerImage westMarker;
    private MarkerImageFactory.MarkerImage northWestMarker;

    private MarkerImageFactory.MarkerImage favoriteMarker;
    @Before
    public void setUp() throws Exception {
        subject = new MarkerImageFactory(RuntimeEnvironment.application);
        noDirectionMarker = subject.create(new MarkerImageOptions("", false));
        northMarker = subject.create(new MarkerImageOptions("N", false));
        northEastMarker = subject.create(new MarkerImageOptions("NE", false));
        eastMarker = subject.create(new MarkerImageOptions("E", false));
        southEastMarker = subject.create(new MarkerImageOptions("SE", false));
        southMarker = subject.create(new MarkerImageOptions("S", false));
        southWestMarker = subject.create(new MarkerImageOptions("SW", false));
        westMarker = subject.create(new MarkerImageOptions("W", false));
        northWestMarker = subject.create(new MarkerImageOptions("NW", false));
        favoriteMarker = subject.create(new MarkerImageOptions("", true));
    }

    @Test
    public void arrowIcon_shouldRotateBasedOnMarkerOptions() {
        assertThat(noDirectionMarker.arrowIcon).isNull();
        assertIconRotatedTo(northMarker.arrowIcon, 0);
        assertIconRotatedTo(northEastMarker.arrowIcon, 45);
        assertIconRotatedTo(eastMarker.arrowIcon, 90);
        assertIconRotatedTo(southEastMarker.arrowIcon, 135);
        assertIconRotatedTo(southMarker.arrowIcon, 180);
        assertIconRotatedTo(southWestMarker.arrowIcon, 225);
        assertIconRotatedTo(westMarker.arrowIcon, 270);
        assertIconRotatedTo(northWestMarker.arrowIcon, 315);
    }

    @Test
    public void drawnIcon_shouldSizeItselfBasedOnPosition() {
        assertFinalSize(noDirectionMarker, false, false);
        assertFinalSize(northMarker, true, false);
        assertFinalSize(eastMarker, false, true);
        assertFinalSize(southMarker, true, false);
        assertFinalSize(westMarker, false, true);
        assertFinalSize(northEastMarker, true, true);
        assertFinalSize(northWestMarker, true, true);
        assertFinalSize(southEastMarker, true, true);
        assertFinalSize(southWestMarker, true, true);
    }

    @Test
    public void draw_shouldDrawAllShapesIntoCanvas() {
        MarkerImageFactory.MarkerImage markerImage = subject.create(new MarkerImageOptions("N", false));
        markerImage.draw();
        ExtendedShadowCanvas canvas = (ExtendedShadowCanvas) shadowOf(markerImage.canvas);
        assertThat(canvas.bitmapHistory.containsKey(subject.busBitmap)).isTrue();
        assertThat(canvas.bitmapHistory.containsKey(markerImage.arrowIcon)).isTrue();
        assertThat(shadowOf(markerImage.arrowIcon).getCreatedFromBitmap()).isEqualTo(subject.arrowBitmap);
    }

    @Test
    public void drawIcon_shouldNotDrawArrow_whenThereIsNoDirection() {
        MarkerImageFactory.MarkerImage markerImage = subject.create(new MarkerImageOptions("", false));
        markerImage.draw();
        ExtendedShadowCanvas canvas = (ExtendedShadowCanvas) shadowOf(markerImage.canvas);
        assertThat(canvas.bitmapHistory.containsKey(subject.busBitmap)).isTrue();
        assertThat(canvas.bitmapHistory.containsKey(markerImage.arrowIcon)).isFalse();
    }

    private void assertIconRotatedTo(Bitmap icon, int degrees) {
        assertThat(icon).isNotNull();
        Matrix expected = new Matrix();
        expected.postRotate(degrees);
        assertThat(shadowOf(icon).getCreatedFromMatrix().toString())
                .isEqualTo(expected.toString());
    }

    private void assertFinalSize(MarkerImageFactory.MarkerImage markerImage, boolean combineHeights, boolean combineWidths) {
        int height, width;
        int padding = 16;
        int arrowHeight = 0, arrowWidth = 0;

        if (markerImage.arrowIcon != null) {
            arrowHeight = markerImage.arrowIcon.getHeight();
            arrowWidth = markerImage.arrowIcon.getWidth();
        }

        if (combineHeights && combineWidths) {
            height = arrowHeight / 2 + markerImage.busIcon.getHeight() + padding;
            width = arrowWidth / 2 + markerImage.busIcon.getWidth() + padding;
        } else {
            if (combineHeights) {
                height = arrowHeight + markerImage.busIcon.getHeight() + padding;
            } else {
                height = arrowHeight > markerImage.busIcon.getHeight() + padding ? arrowHeight : markerImage.busIcon.getHeight() + padding;
            }
            if (combineWidths) {
                width = arrowWidth + markerImage.busIcon.getWidth() + padding;
            } else {
                width = arrowWidth > markerImage.busIcon.getWidth() + padding ? arrowWidth : markerImage.busIcon.getWidth() + padding;
            }
        }

        markerImage.draw();
        Canvas canvas = markerImage.canvas;
        assertThat(canvas.getWidth()).isEqualTo(width);
        assertThat(canvas.getHeight()).isEqualTo(height);
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

    @Implements(Canvas.class)
    public static class ExtendedShadowCanvas extends ShadowCanvas {

        Bitmap targetBitmap = (Bitmap)ReflectionHelpers.callConstructor(Bitmap.class, new ClassParameter[0]);
        Map<Bitmap, BitmapInfo> bitmapHistory = new HashMap<>();

        public void __constructor__(Bitmap bitmap) {
            this.targetBitmap = bitmap;
        }

        @Implementation
        public int getWidth() {
            return targetBitmap.getWidth();
        }

        @Implementation
        public int getHeight() {
            return targetBitmap.getHeight();
        }

        @Implementation
        public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
            bitmapHistory.put(bitmap, new BitmapInfo(left, top, paint));
        }

        @Data
        public class BitmapInfo {
            float left;
            float top;
            Paint paint;

            BitmapInfo(float left, float top, Paint paint) {
                this.left = left;
                this.top = top;
                this.paint = paint;
            }
        }
    }
}