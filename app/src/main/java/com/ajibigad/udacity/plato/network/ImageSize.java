package com.ajibigad.udacity.plato.network;

/**
 * Created by Julius on 15/04/2017.
 */
public enum ImageSize {
    W92("w92"),
    W154("w154"),
    W185("w185"),
    W342("w342"),
    W500("w500"),
    W750("w780"),
    ORIGINAL("original");

    private String size;

    ImageSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }
}
