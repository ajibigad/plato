package com.ajibigad.udacity.plato.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

public class TrailerDeserializer implements JsonDeserializer<List<Trailer>> {

    @Override
    public List<Trailer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray trailers = json.getAsJsonObject().get("trailers").getAsJsonObject().get("youtube").getAsJsonArray();
        return new Gson().fromJson(trailers, new TypeToken<List<Trailer>>() {
        }.getType());
    }
}
