package com.demondevelopers.slidingpanelexample.model;

import android.database.Observable;

import static com.demondevelopers.slidingpanelexample.model.TrackState.TrackStateChanged;


public class TrackState extends Observable<TrackStateChanged>
{
	private final Object mLock = new Object();
	private int mProgress;
	private int mTotal;
	
	public void update(int progress, int total)
	{
		synchronized(mLock){
			mProgress = progress;
			mTotal    = total;
		}
		notifyChange();
	}
	
	public void notifyChange()
	{
		synchronized(mLock){
			for(int i = mObservers.size() - 1; i >= 0; i--){
				mObservers.get(i).onTrackStateChange(this);
			}
		}
	}
	
	public int getProgress()
	{
		return mProgress;
	}
	
	public int getTotal()
	{
		return mTotal;
	}
	
	public static interface TrackStateChanged
	{
		void onTrackStateChange(TrackState state);
	}
}
