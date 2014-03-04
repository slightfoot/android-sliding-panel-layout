package com.demondevelopers.slidingpanelexample.util;

import java.lang.reflect.Field;

import android.util.Log;

import com.google.gson.FieldNamingStrategy;


public final class GsonFieldStrategy implements FieldNamingStrategy
{
	private static final String TAG = GsonFieldStrategy.class.getSimpleName();
	
	private StringBuilder mBuffer = new StringBuilder(32);
	
	
	@Override
	public String translateName(Field f)
	{
		String name = f.getName();
		mBuffer.setLength(0);
		mBuffer.append(Character.toLowerCase(name.charAt(1)));
		if(name.length() > 2){
			mBuffer.append(name.substring(2));
		}
		String translated = mBuffer.toString();
		Log.v(TAG, String.format("GsonFieldStrategy: '%s' -> '%s'", name, translated));
		return translated;
	}
}
