package com.demondevelopers.slidingpanelexample;

import android.app.Application;

import com.demondevelopers.slidingpanelexample.util.GsonFieldStrategy;
import com.demondevelopers.slidingpanelexample.util.RemoteJsonLoader.GsonRetriever;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ExampleApp extends Application implements GsonRetriever
{
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public Gson getGson()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setFieldNamingStrategy(new GsonFieldStrategy());
		return builder.create();
	}
}
