package io.pivotal.weatherbus.app.model;

import lombok.Data;

@Data
public class MarkerImageOptions {
    String direction;
    boolean isFavorite;

    public MarkerImageOptions(String direction, boolean isFavorite) {
        this.direction = direction;
        this.isFavorite = isFavorite;
    }
}
