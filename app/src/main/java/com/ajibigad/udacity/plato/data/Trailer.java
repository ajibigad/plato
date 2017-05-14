package com.ajibigad.udacity.plato.data;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by Julius on 14/05/2017.
 */
@Parcel
public class Trailer {

    private String name;
    private String source;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
