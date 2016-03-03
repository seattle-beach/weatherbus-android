package io.pivotal.weatherbus.app.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.pivotal.weatherbus.app.R;
import io.pivotal.weatherbus.app.SavedStops;
import io.pivotal.weatherbus.app.WeatherBusApplication;
import io.pivotal.weatherbus.app.model.BusStop;

import javax.inject.Inject;

public class MapActivity extends Activity implements MapStopsFragment.FragmentListener {
    @Bind(R.id.progress_bar) ProgressBar progressBar;
    @Bind(R.id.toolbar_title) TextView toolbarTitle;
    @Bind(R.id.bus_info) View toolbarInfo;
    @Bind(R.id.toolbar_favorite_button) ImageButton favoriteButton;

    @Inject SavedStops favoriteStops;

    BusStop selectedStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        WeatherBusApplication.inject(this);
        toolbarTitle.setText("SELECT A BUS STOP");

        if (savedInstanceState != null) {
            return;
        }

        MapStopsFragment mapStopsFragment = new MapStopsFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mapStopsFragment).commit();
    }

    @Override
    public void onStopsLoaded() {
        progressBar.setVisibility(View.GONE);
        toolbarInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopSelected(@NonNull BusStop selectedStop) {
        this.selectedStop = selectedStop;
        toolbarTitle.setText(selectedStop.getName() + " (" + selectedStop.getDirection() + ")");
        favoriteButton.setVisibility(View.VISIBLE);
        favoriteStops.getSavedStops().contains(selectedStop.getId());
        if (favoriteStops.getSavedStops().contains(selectedStop.getId())) {
            favoriteButton.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            favoriteButton.setColorFilter(null);
        }
    }

    @OnClick(R.id.toolbar_favorite_button)
    public void toggleFavorite() {
        if (selectedStop != null) {
            if (favoriteStops.getSavedStops().contains(selectedStop.getId())) {
                ((MapStopsFragment) getFragmentManager().findFragmentById(R.id.fragment_container)).setSelectedFavorite(false);
                favoriteStops.deleteSavedStop(selectedStop.getId());
                favoriteButton.setColorFilter(null);
            } else {
                ((MapStopsFragment) getFragmentManager().findFragmentById(R.id.fragment_container)).setSelectedFavorite(true);
                favoriteStops.addSavedStop(selectedStop.getId());
                favoriteButton.setColorFilter(ContextCompat.getColor(this,android.R.color.holo_red_dark));
            }
        }
    }
}

