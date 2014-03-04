package com.demondevelopers.slidingpanelexample.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class RemoteJsonLoader<T> extends GenericAsyncTaskLoader<T>
{
	private static final String TAG = RemoteJsonLoader.class.getSimpleName();
	
	private final Class<T> mClazz;
	private final String   mUrl;
	
	
	public RemoteJsonLoader(Context context, Class<T> clazz, String url)
	{
		super(context);
		mClazz = clazz;
		mUrl   = url;
	}
	
	@Override
	public T loadInBackground()
	{
		HttpURLConnection conn = null;
		InputStream is = null;
		try{
			Context appContext = getContext().getApplicationContext();
			if(!(appContext instanceof GsonRetriever)){
				throw new GsonNotProvided();
			}
			try{
				Log.d(TAG, "Loading JSON from " + mUrl);
				conn = (HttpURLConnection)new URL(mUrl).openConnection();
				conn.addRequestProperty("Accept", "application/json");
				conn.setDoInput(true);
				if(conn.getResponseCode() != 200){
					throw new IOException("Server borked: " + conn.getResponseMessage());
				}
				is = conn.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				Gson gson = ((GsonRetriever)appContext).getGson();
				return (T)gson.fromJson(isr, mClazz);
			}
			catch (IOException e) {
				Log.e(TAG, "Exception: ", e);
			}
			finally{
				if(is != null){
					is.close();
				}
				if(conn != null){
					conn.disconnect();
				}
			}
		}
		catch(GsonNotProvided e){
			Log.e(TAG, "Gson not provided by application context.", e);
		}
		catch(JsonSyntaxException e){
			Log.e(TAG, "Remote JSON syntax issues: " + e.getMessage(), e);
		}
		catch(IOException e){
			Log.e(TAG, "Failed to load data: " + e.getMessage(), e);
		}
		return null;
	}
	
	public static interface GsonRetriever
	{
		public Gson getGson();
	}
	
	public static class GsonNotProvided extends Exception
	{
		private static final long serialVersionUID = 8877763940196544330L;
	}
}
