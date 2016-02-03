package io.pivotal.weatherbus.app.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

public class BusStopAdapter extends ArrayAdapter<BusStop> {
    public BusStopAdapter(Context context, int resource) {
        super(context,resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

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
                        busStop.getResponse().getName(),
                        busStop.getResponse().getLatitude(),
                        busStop.getResponse().getLongitude());
                if (busStop.isFavorite()) {
                    text += " *";
                }
                tt1.setText(text);
            }
        }

        return view;
    }
}
