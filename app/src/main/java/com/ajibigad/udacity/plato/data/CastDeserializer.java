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
 * Created by ajibigad on 23/07/2017.
 */

public class CastDeserializer implements JsonDeserializer<List<Cast>> {
    @Override
    public List<Cast> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonElement casts = je.getAsJsonObject().get("cast").getAsJsonArray();

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion to this deserializer
        return new Gson().fromJson(casts, new TypeToken<List<Cast>>() {
        }.getType());
    }
}
