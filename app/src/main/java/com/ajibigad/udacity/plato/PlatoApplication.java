package com.ajibigad.udacity.plato;

import android.app.Application;

import com.ajibigad.udacity.plato.data.FavoriteMovieHelper;
import com.facebook.stetho.Stetho;

/**
 * Created by Julius on 14/04/2017.
 */
public class PlatoApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        //registers this with the EventBus
        new FavoriteMovieHelper(this);

        Stetho.initializeWithDefaults(this);
    }
}
