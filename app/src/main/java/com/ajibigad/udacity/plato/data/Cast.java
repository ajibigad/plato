package com.ajibigad.udacity.plato.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by ajibigad on 23/07/2017.
 */

@Parcel
public class Cast {

    private String name;

    @SerializedName("profile_path")
    private String profileImage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
