package io.pivotal.weatherbus.app.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BusImage {
    final private Bitmap busBitmap;
    final private Paint backgroundPaint;
    final private int padding = 16;

    public BusImage(Bitmap busBitmap, Paint backgroundPaint) {
        this.busBitmap = busBitmap;
        this.backgroundPaint = backgroundPaint;
    }

    public void drawOnto(Canvas canvas, int left, int top) {
        canvas.drawRoundRect(left, top, left + getWidth(), top + getHeight(),
                getHeight() / 6, getWidth() / 6, backgroundPaint);
        canvas.drawBitmap(busBitmap, left + padding / 2, top + padding / 2, null);
    }

    public int getHeight() {
        return busBitmap.getHeight() + padding;
    }

    public int getWidth() {
        return busBitmap.getWidth() + padding;
    }
}
