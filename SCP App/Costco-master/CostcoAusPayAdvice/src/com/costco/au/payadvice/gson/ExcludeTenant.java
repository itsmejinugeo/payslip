package com.costco.au.payadvice.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExcludeTenant implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {

		boolean skip = false;
		
		switch (f.getName()) {
		case "tenant":
			skip = true;
			break;

		default:
			break;
		}
		
		
		return skip;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}
