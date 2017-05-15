package com.ajibigad.udacity.plato.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.ajibigad.udacity.plato.data.Movie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

;

/**
 * Created by Julius on 11/05/2017.
 */
public class ImageHelper {

    public static final String IMAGE_EXT = ".jpeg";
    public static final String IMAGE_DIR = "imageDir";
    private static final String TAG = ImageHelper.class.getSimpleName();

    public static String saveImagePosterToFile(Context context, Movie movie, Bitmap bitmapImage) {
        String imagePath = getMovieImagePosterAbsolutePath(context, movie);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save image poster for " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imagePath;
    }

    public static boolean deleteImagePoster(Context context, Movie movie) {
        return deleteImagePoster(getMovieImagePosterAbsolutePath(context, movie));
    }

    public static boolean deleteImagePoster(String imagePath) {
        File file = new File(imagePath);
        if (file.delete()) {
            Log.w(TAG, "Failed to delete image at path : " + imagePath);
            return false;
        } else {
            return true;
        }
    }

    private static String buildImagePath(Movie movie) {
        return new StringBuilder().append(movie.getTitle()).append(movie.getId()).append(IMAGE_EXT).toString();
    }

    public static String getMovieImagePosterAbsolutePath(Context context, Movie movie) {
        ContextWrapper cw = new ContextWrapper(context);
        File imageDirectory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        // Create imageDir
        File imagePath = new File(imageDirectory, buildImagePath(movie));
        return imagePath.getAbsolutePath();
    }
}
