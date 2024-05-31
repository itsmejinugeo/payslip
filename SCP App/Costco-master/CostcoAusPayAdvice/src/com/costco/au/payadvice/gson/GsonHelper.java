package com.costco.au.payadvice.gson;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHelper {

	public static final Gson adaptedGson = new GsonBuilder().registerTypeAdapter(Date.class, new UTCDateTypeAdapter())
			.registerTypeAdapter(byte[].class, new Base64Adapter()).setExclusionStrategies(new ExcludeTenant())
			.create();

	public static final Gson adaptedExcludingAuditGson = new GsonBuilder()
			.registerTypeAdapter(Date.class, new UTCDateTypeAdapter())
			.registerTypeAdapter(byte[].class, new Base64Adapter())
			.setExclusionStrategies(new ExcludeTenant(), new ExcludeAuditData()).create();
	
}
