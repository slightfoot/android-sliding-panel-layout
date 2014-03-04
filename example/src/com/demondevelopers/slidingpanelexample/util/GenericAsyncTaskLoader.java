package com.demondevelopers.slidingpanelexample.util;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


public abstract class GenericAsyncTaskLoader<D> extends AsyncTaskLoader<D>
{
	private D mData;
	
	
	public GenericAsyncTaskLoader(Context context)
	{
		super(context);
	}
	
	public D getData()
	{
		return mData;
	}
	
	@Override
	protected void onStartLoading()
	{
		if(mData != null){
			deliverResult(mData);
		}
		if(takeContentChanged() || mData == null){
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading()
	{
		cancelLoad();
	}
	
	@Override
	protected void onReset()
	{
		super.onReset();
		onStopLoading();
		if(mData != null){
			mData = null;
		}
	}
	
	@Override
	public void deliverResult(D data)
	{
		if(!isAbandoned()){
			mData = data;
			if(isStarted()){
				super.deliverResult(data);
			}
		}
	}
}
