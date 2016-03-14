package io.pivotal.weatherbus.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.pivotal.weatherbus.app.model.BusStop;

import java.util.Locale;

public class BusStopAdapter extends ArrayAdapter<BusStop> {
    String selectedStopId = "";

    public BusStopAdapter(Context context, int resource) {
        super(context,resource);
    }

    public void highlightStop(String stopId) {
        selectedStopId = stopId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            view = vi.inflate(android.R.layout.simple_list_item_1, null);
        }

        BusStop busStop = getItem(position);

        if (busStop != null) {
            TextView tt1 = (TextView) view.findViewById(android.R.id.text1);
            if (tt1 != null) {
                String text = String.format(Locale.getDefault(), "%s: (%.1f, %.1f)",
                        busStop.getName(),
                        busStop.getLatitude(),
                        busStop.getLongitude());
                if (busStop.isFavorite()) {
                    text += " *";
                }
                tt1.setText(text);
            }

            if(busStop.getId().equals(selectedStopId)) {
                view.setBackgroundColor(Color.parseColor("#ffffd8"));
            } else {
                view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.background_light));
            }
        }

        return view;
    }
}
