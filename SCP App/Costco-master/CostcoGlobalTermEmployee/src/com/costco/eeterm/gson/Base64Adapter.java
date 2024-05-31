package com.costco.eeterm.gson;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Base64Adapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
	public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return Base64.getDecoder().decode(json.getAsString().getBytes(StandardCharsets.UTF_8));
	}

	public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
	}
}