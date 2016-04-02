package io.pivotal.weatherbus.app.map;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.model.MarkerImageOptions;

public class MarkerImageFactory {
    private final Context context;
    final Bitmap busBitmap;
    final Bitmap arrowBitmap;

    public MarkerImageFactory(Context context) {
        this.context = context;
        this.busBitmap = getVectorBitMap(R.drawable.vector_ic_bus);
        this.arrowBitmap = getVectorBitMap(R.drawable.vector_ic_arrow_up);
    }

    public MarkerImage create(MarkerImageOptions options) {
        return new MarkerImage(options);
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

    public class MarkerImage {
        BusImage busImage = null;
        Bitmap busIcon = null;
        Bitmap arrowIcon = null;
        Canvas canvas = null;

        private MarkerImageOptions options;

        public MarkerImage(MarkerImageOptions options) {
            this.busIcon = MarkerImageFactory.this.busBitmap;
            int colorId = options.isFavorite() ? android.R.color.holo_red_dark : R.color.background_material_light;
            Paint background = new Paint();
            background.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, colorId), PorterDuff.Mode.SRC_ATOP));
            busImage = new BusImage(busIcon, background);
            this.options = options;
            if (!this.options.getDirection().isEmpty()) {
                this.arrowIcon = rotate(MarkerImageFactory.this.arrowBitmap, options.getDirection());
            }
    }

        public BitmapDescriptor draw() {
            Bitmap finalIcon = createBlankBitmap(options.getDirection());
            canvas = new Canvas(finalIcon);
            busImage.drawOnto(canvas, 0, 0);
            if(arrowIcon != null) {
                canvas.drawBitmap(arrowIcon, 0, 0, null);
            }
            return BitmapDescriptorFactory.fromBitmap(finalIcon);
        }

        private Bitmap createBlankBitmap(String direction) {
            int arrowHeight = 0, arrowWidth = 0;

            if (arrowIcon != null) {
                arrowHeight = arrowIcon.getHeight();
                arrowWidth = arrowIcon.getWidth();
            }

            int width = arrowWidth > busImage.getWidth() ? arrowWidth : busImage.getWidth();
            int height = arrowHeight > busImage.getHeight() ? arrowHeight : busImage.getHeight();

            switch(direction) {
                case "N":
                case "S":
                    height = arrowHeight + busImage.getHeight();
                    break;
                case "E":
                case "W":
                    width = arrowWidth + busImage.getWidth();
                    break;
                case "NE":
                case "SE":
                case "SW":
                case "NW":
                    height = arrowHeight / 2 + busImage.getHeight();
                    width = arrowWidth / 2 + busImage.getWidth();
                    break;
            }
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        private Bitmap rotate(Bitmap icon, String direction) {
            float degrees = 0;
            switch(direction) {
                case "NW": degrees += 45;
                case "W": degrees += 45;
                case "SW": degrees += 45;
                case "S": degrees += 45;
                case "SE": degrees += 45;
                case "E":  degrees += 45;
                case "NE": degrees += 45;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(icon , 0, 0, icon .getWidth(), icon .getHeight(), matrix, true);
        }
    }
}
