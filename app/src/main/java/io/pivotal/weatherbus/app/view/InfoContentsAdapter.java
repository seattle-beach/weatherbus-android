package io.pivotal.weatherbus.app.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.map.WeatherBusMap;
import io.pivotal.weatherbus.app.map.WeatherBusMarker;

public class InfoContentsAdapter implements WeatherBusMap.InfoWindowAdapter {

    private Context context;

    @Override
    public View getInfoWindow(WeatherBusMarker marker) {
        return null;
    }

    @Override
    public View getInfoContents(WeatherBusMarker marker) {
        if(context == null) {
            return null;
        }
        View contentView = View.inflate(context, R.layout.view_info_window, null);
        TextView titleView = ButterKnife.findById(contentView, R.id.title);
        titleView.setText(marker.getTitle());
        TextView routesView = ButterKnife.findById(contentView, R.id.routes);
        routesView.setText(marker.getSnippet());
        return contentView;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
