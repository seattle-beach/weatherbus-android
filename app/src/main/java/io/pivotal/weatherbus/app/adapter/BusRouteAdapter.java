package io.pivotal.weatherbus.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.model.BusRoute;
import org.joda.time.LocalTime;

public class BusRouteAdapter extends ArrayAdapter<BusRoute> {
    public BusRouteAdapter(Context context) {
        super(context, R.layout.route_information);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.route_information, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        BusRoute busRoute = getItem(position);
        long departureTime = busRoute.getPredictedTime() == 0 ? busRoute.getScheduledTime() : busRoute.getPredictedTime();

        holder.routeNumber.setText(busRoute.getRouteNumber());
        holder.name.setText(busRoute.getRouteName());
        holder.status.setText(calculateStatus(busRoute));
        holder.time.setText(new LocalTime(departureTime).toString("HH:mm"));
        holder.temperature.setText(String.format("%.1f", busRoute.getTemperature()));

        return view;
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

    static class ViewHolder {
        @Bind(R.id.routeNumber) TextView routeNumber;
        @Bind(R.id.status) TextView status;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.time) TextView time;
        @Bind(R.id.temperature) TextView temperature;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
