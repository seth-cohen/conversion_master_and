package com.forwardthinking.conversionmasterfree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class MapXMLParser {
	public static final int INITIAL_CAPACITY = 30;
	public static final float LOAD_FACTOR = 0.75f;
	
	public static Map<String, Double> getHashMapResource(Context c, int hashMapResourceId) {
		Map<String, Double> map = null;
		XmlResourceParser parser = c.getResources().getXml(hashMapResourceId);
		
		String key = null;
		Double value = null;
		
		try {
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// do nothing
					Log.d("utils", "Start of XML Map Document");
				}
				else if (eventType == XmlPullParser.START_TAG ) {
					if (parser.getName().equals("map")) {
						map = new LinkedHashMap<String, Double>(INITIAL_CAPACITY, LOAD_FACTOR, false);
					}
					else if (parser.getName().equals("entry")) {
						key = parser.getAttributeValue(null, "key");
						if (null == key) {
							parser.close();
							return null;
						}
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if (parser.getName().equals("entry")) {
						map.put(key, value);
						key = null;
						value = null;
					}
				}
				else if (eventType == XmlPullParser.TEXT) {
					if (null != key) {
						value = Double.parseDouble(parser.getText());
					}
				}
				eventType = parser.next();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
	}
	
	public static Map<String, String> getHashMapStringResource(Context c, int hashMapResourceId) {
		Map<String, String> map = null;
		XmlResourceParser parser = c.getResources().getXml(hashMapResourceId);
		
		String key = null;
		String value = null;
		
		try {
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// do nothing
					Log.d("utils", "Start of XML Map Document");
				}
				else if (eventType == XmlPullParser.START_TAG ) {
					if (parser.getName().equals("map")) {
						map = new LinkedHashMap<String, String>(INITIAL_CAPACITY, LOAD_FACTOR, false);
					}
					else if (parser.getName().equals("entry")) {
						key = parser.getAttributeValue(null, "key");
						if (null == key) {
							parser.close();
							return null;
						}
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if (parser.getName().equals("entry")) {
						map.put(key, value);
						key = null;
						value = null;
					}
				}
				else if (eventType == XmlPullParser.TEXT) {
					if (null != key) {
						value = parser.getText();
					}
				}
				eventType = parser.next();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
	}
	
	public static String getMapBase(Context c, int hashMapResourceId) {
		XmlResourceParser parser = c.getResources().getXml(hashMapResourceId);
		
		String base = null;
		
		try {
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// do nothing
					Log.d("utils", "Start of XML Map Document");
				}
				else if (eventType == XmlPullParser.START_TAG ) {
					if (parser.getName().equals("map")) {
						base = parser.getAttributeValue(null, "base");
						return base;
					}
				}
				eventType = parser.next();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return base;
	}
	
}
