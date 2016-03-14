package io.pivotal.weatherbus.app.repositories;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStopsRepository {
    SharedPreferences settings;

    public FavoriteStopsRepository(SharedPreferences settings) {
        this.settings = settings;
    }

    public List<String> getSavedStops() {
        List<String> result = new ArrayList<String>();

        String savedStops = settings.getString("saved_stops", "");
        if (savedStops.equals("")) {
            return result;
        }

        for (String stop : savedStops.split(",")) {
            result.add(stop);
        }
        return result;
    }

    public void addSavedStop(String stop) {
        String result = "";
        List<String> savedStops = getSavedStops();

        for (String savedStop : savedStops) {
            result += savedStop + ",";
        }
        result += stop;

        Editor editor = settings.edit();
        editor.putString("saved_stops", result);
        editor.apply();
    }

    public void deleteSavedStop(String stop) {
        String result = "";
        List<String> savedStops = getSavedStops();

        for (String savedStop : savedStops) {
            if (savedStop.equals(stop)) {
                continue;
            }
            result += savedStop + ",";
        }
        Editor editor = settings.edit();
        if (result.isEmpty()) {
            editor.putString("saved_stops","");
        } else {
            result = result.substring(0,result.length()-1);
            editor.putString("saved_stops", result);
        }
        editor.apply();
    }
}
