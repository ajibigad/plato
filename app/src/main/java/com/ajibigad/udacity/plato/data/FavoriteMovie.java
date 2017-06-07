package com.ajibigad.udacity.plato.data;

import org.parceler.Parcel;

/**
 * Created by Julius on 13/05/2017.
 */
@Parcel
public class FavoriteMovie extends Movie {

    private final String FILE = "file:"; //prepended to file path for picasso

    private String posterImageFileUri;

    private String backdropImageFileUri;

    public String getPosterImageFileUri() {
        return posterImageFileUri;
    }

    public void setPosterImageFileUri(String posterImageFileUri) {
        this.posterImageFileUri = FILE + posterImageFileUri;
    }

    public String getBackdropImageFileUri() {
        return backdropImageFileUri;
    }

    public void setBackdropImageFileUri(String backdropImageFileUri) {
        this.backdropImageFileUri = FILE + backdropImageFileUri;
    }
}
