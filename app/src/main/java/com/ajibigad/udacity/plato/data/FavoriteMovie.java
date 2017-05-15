package com.ajibigad.udacity.plato.data;

import org.parceler.Parcel;

/**
 * Created by Julius on 13/05/2017.
 */
@Parcel
public class FavoriteMovie extends Movie {

    private String posterImageFileUri;

    public String getPosterImageFileUri() {
        return posterImageFileUri;
    }

    public void setPosterImageFileUri(String posterImageFileUri) {
        this.posterImageFileUri = "file:" + posterImageFileUri;
    }
}
