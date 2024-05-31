package com.costco.eeterm.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExcludeAuditData implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {

		boolean skip = false;

		switch (f.getName()) {
		case "createdByUser":
		case "createdDate":
		case "lastChangedByUser":
		case "lastChangedDate":
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
