package com.ajibigad.udacity.plato.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.ajibigad.udacity.plato.R;
import com.ajibigad.udacity.plato.data.Movie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Julius on 11/05/2017.
 */
public class ImageHelper {

    public static final String IMAGE_EXT = ".jpeg";
    public static final String IMAGE_DIR = "imageDir";
    private static final String TAG = ImageHelper.class.getSimpleName();

    public static String saveImageToFile(Context context, Movie movie, Bitmap bitmapImage) {
        String imagePath = getMovieImageAbsolutePath(context, movie);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            Toast.makeText(context, String.format(context.getString(R.string.failed_to_save_poster_image), movie.getTitle()), Toast.LENGTH_SHORT).show();
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

    public static boolean deleteMovieImage(String imagePath) {
        File file = new File(imagePath);
        if (file.delete()) {
            Log.w(TAG, "Failed to delete image at path : " + imagePath);
            return false;
        } else {
            return true;
        }
    }

    private static String buildImagePath(Movie movie) {
        return movie.getTitle() + UUID.randomUUID() + IMAGE_EXT;
    }

    public static String getMovieImageAbsolutePath(Context context, Movie movie) {
        ContextWrapper cw = new ContextWrapper(context);
        File imageDirectory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        // Create imageDir
        File imagePath = new File(imageDirectory, buildImagePath(movie));
        return imagePath.getAbsolutePath();
    }
}
