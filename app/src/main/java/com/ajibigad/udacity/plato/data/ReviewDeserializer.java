package com.ajibigad.udacity.plato.data;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Julius on 14/05/2017.
 */
public class ReviewDeserializer implements JsonDeserializer<List<Review>> {
    @Override
    public List<Review> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        JsonElement reviews = je.getAsJsonObject().get("reviews").getAsJsonObject().get("results").getAsJsonArray();

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion to this deserializer
        return new Gson().fromJson(reviews, new TypeToken<List<Review>>() {
        }.getType());

    }
}
