package com.demondevelopers.slidingpanelexample;

import android.app.Application;
import android.content.Context;

import com.demondevelopers.slidingpanelexample.util.GsonFieldStrategy;
import com.demondevelopers.slidingpanelexample.util.RemoteJsonLoader.GsonRetriever;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ExampleApp extends Application implements GsonRetriever
{
	private AudioPlayer mAudioPlayer;
	
	
	public static ExampleApp from(Context context)
	{
		if(context == null){
			throw new IllegalStateException("context cannot be null");
		}
		return (ExampleApp)context.getApplicationContext();
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mAudioPlayer = new AudioPlayer();
	}
	
	@Override
	public Gson getGson()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setFieldNamingStrategy(new GsonFieldStrategy());
		return builder.create();
	}
	
	public AudioPlayer getAudioPlayer()
	{
		return mAudioPlayer;
	}
}
