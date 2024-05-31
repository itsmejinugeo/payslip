package com.costco.au.payadvice.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCDateTypeAdapter extends TypeAdapter<Date> {
	private final DateFormat dateFormat;
	private final DateFormat dateFormat2;

	public UTCDateTypeAdapter() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void write(JsonWriter out, Date value) throws IOException {
		if (value != null) {
			String dateFormatAsString = dateFormat.format(value);
			out.value(dateFormatAsString);
		} else {
			out.nullValue();
		}

	}

	@Override
	public Date read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String dateToParse = in.nextString();
		try {
			return dateFormat.parse(dateToParse);
		} catch (ParseException e) {
			try {
				return dateFormat2.parse(dateToParse);
			} catch (ParseException e1) {
				return null;
			}

		}

	}

}
