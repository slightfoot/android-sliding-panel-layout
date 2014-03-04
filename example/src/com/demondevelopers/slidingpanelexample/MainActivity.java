package com.demondevelopers.slidingpanelexample;

import java.io.IOException;

import com.demondevelopers.slidingpanelexample.frags.MostRecentFragment;
import com.demondevelopers.slidingpanelexample.frags.MostRecentFragment.OnTrackSelected;
import com.demondevelopers.slidingpanelexample.model.Track;
import com.demondeveloprs.slidingpanellayout.SlideUpPanelLayout;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


public class MainActivity extends FragmentActivity
	implements OnTrackSelected
{
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private SlideUpPanelLayout mPanelLayout;
	
	private OnProgressUpdate mProgressUpdate;
	private MediaPlayer mMediaPlayer;
	private Track mTrack;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(R.string.most_recent_tracks);
		setContentView(R.layout.activity_main);
		
		mPanelLayout = (SlideUpPanelLayout)findViewById(R.id.slide_layout);
		
		/*findViewById(R.id.content_frame).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(layout.getPanelState() == SlideUpPanelLayout.PanelState.HIDDEN){
					layout.showHandle();
				}
				else{
					layout.hideHandle();
				}
			}
		});*/
		mPanelLayout.showHandle();
		
		if(savedInstanceState == null){
			MostRecentFragment mostRecent = new MostRecentFragment();
			mProgressUpdate = mostRecent;
			getSupportFragmentManager().beginTransaction()
				.add(R.id.content_frame, mostRecent)
				.commit();
		}
		else{
			mProgressUpdate = (OnProgressUpdate)getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		}
	}
	
	@Override
	public void onTrackSelected(Track track)
	{
		mTrack = track;
		
		mPanelLayout.showHandle();
		
		mHandler.removeCallbacks(mUpdateProgressRunnable);
		
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}else{
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}
		try{
			mMediaPlayer.setDataSource(this, Uri.parse(track.getStreamUrl() + 
				"?client_id=54647ab8dc2cd5e57c6163c7fb6e683d"));
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}
		catch(IOException e){
			Log.e(TAG, "onTrackSelected", e);
		}
		
		mHandler.post(mUpdateProgressRunnable);
	}
	
	private Runnable mUpdateProgressRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			MediaPlayer mp = mMediaPlayer;
			if(mp != null && mp.isPlaying()){
				int duration = mp.getDuration();
				if(duration > -1){
					float progress = (float)mp.getCurrentPosition() / (float)duration;
					Log.d(TAG, "progress: " + progress);
					mProgressUpdate.onProgressUpdate(mTrack, progress);
				}
				mHandler.postDelayed(mUpdateProgressRunnable, 1000);
			}
		}
	};
	
	@Override
	protected void onStop()
	{
		super.onStop();
		mHandler.removeCallbacks(mUpdateProgressRunnable);
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	
	public static interface OnProgressUpdate
	{
		public void onProgressUpdate(Track track, float progress);
	}
}
