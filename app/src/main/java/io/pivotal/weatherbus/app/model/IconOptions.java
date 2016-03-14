package io.pivotal.weatherbus.app.model;

import lombok.Data;

@Data
public class IconOptions {
    String direction;
    boolean isFavorite;

    public IconOptions(String direction, boolean isFavorite) {
        this.direction = direction;
        this.isFavorite = isFavorite;
    }
}
