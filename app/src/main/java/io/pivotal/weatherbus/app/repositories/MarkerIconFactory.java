package io.pivotal.weatherbus.app.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.model.IconOptions;

public class MarkerIconFactory {
    Context context;

    public MarkerIconFactory(Context context) {
        this.context = context;
    }

    public MarkerIcon create(IconOptions options) {
        return new MarkerIcon(options);
    }

    public class MarkerIcon {

        Bitmap bitmap;

        public MarkerIcon(IconOptions options) {
            bitmap = getVectorBitMap(R.drawable.vector_ic_bus);
        }

        public BitmapDescriptor draw() {
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        private Bitmap getVectorBitMap(@DrawableRes int id) {
            VectorDrawable drawable = (VectorDrawable) context.getResources().getDrawable(id, context.getTheme());
            Canvas canvas = new Canvas();
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
