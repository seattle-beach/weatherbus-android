package io.pivotal.weatherbus.app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.pivotal.weatherbus.app.R;
import org.joda.time.LocalTime;

public class BusRouteAdapter extends ArrayAdapter<BusRoute> {
    public BusRouteAdapter(Context context) {
        super(context, R.layout.route_information);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.route_information, parent, false);
        }

        BusRoute busRoute = getItem(position);

        TextView tv = (TextView) convertView.findViewById(R.id.routeNumber);
        tv.setText(busRoute.getRouteNumber());

        tv = (TextView) convertView.findViewById(R.id.name);
        tv.setText(busRoute.getRouteName());

        tv = (TextView) convertView.findViewById(R.id.status);
        String status;
        status = calculateStatus(busRoute);
        tv.setText(status);

        long departureTime = busRoute.getPredictedTime() == 0 ? busRoute.getScheduledTime() : busRoute.getPredictedTime();
        tv = (TextView) convertView.findViewById(R.id.time);
        tv.setText(new LocalTime(departureTime).toString("HH:mm"));

        tv = (TextView) convertView.findViewById(R.id.temperature);
        tv.setText(String.format("%.1f", busRoute.getTemperature()));
        return convertView;
    }

    private String calculateStatus(BusRoute busRoute) {
        final int millisInMinute = 60 * 1000;

        String status;
        if (busRoute.getPredictedTime() == 0) {
            status = "scheduled time";
        } else {
            long delta = (busRoute.getPredictedTime() - busRoute.getScheduledTime())/millisInMinute;
            if (delta == 0) {
                return "on time";
            }
            if (delta > 0) {
                status = "late";
            } else {
                status = "early";
                delta *= -1;
            }

            if (delta > 10) {
                status = "very " + status;
            }

            status += String.format(" (%dmin)", delta);
        }
        return status;
    }
}
