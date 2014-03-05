package com.demondevelopers.slidingpanelexample;

import com.demondevelopers.slidingpanelexample.frags.MostRecentFragment;
import com.demondevelopers.slidingpanelexample.frags.MostRecentFragment.OnTrackSelected;
import com.demondevelopers.slidingpanelexample.model.Track;
import com.demondeveloprs.slidingpanellayout.SlideUpPanelLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class MainActivity extends FragmentActivity
	implements OnTrackSelected
{
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String EXTRA_TRACK = "extraTrack";
	
	private SlideUpPanelLayout mPanelLayout;
	
	private AudioPlayer mAudioPlayer;
	private Track       mTrack;
	
	
	public static Intent createIntent(Context context, Track track)
	{
		return new Intent(context, MainActivity.class)
			.putExtra(EXTRA_TRACK, track);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.most_recent_tracks);
		setContentView(R.layout.activity_main);
		
		mAudioPlayer = ExampleApp.from(this).getAudioPlayer();
		mPanelLayout = (SlideUpPanelLayout)findViewById(R.id.slide_layout);
		
		if(savedInstanceState == null){
			getSupportFragmentManager().beginTransaction()
				.add(R.id.content_frame, new MostRecentFragment())
				.commit();
			if(getIntent().hasExtra(EXTRA_TRACK)){
				mTrack = (Track)getIntent().getSerializableExtra(EXTRA_TRACK);
			}
		}
		else{
			mTrack = (Track)savedInstanceState.getSerializable(EXTRA_TRACK);
		}
		if(mTrack != null){
			mPanelLayout.showHandle();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRA_TRACK, mTrack);
	}
	
	@Override
	public void onTrackSelected(Track track)
	{
		mPanelLayout.showHandle();
		mTrack = track;
		mAudioPlayer.play(this, track.getStreamUrl() + 
			"?client_id=54647ab8dc2cd5e57c6163c7fb6e683d");
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if(mAudioPlayer.isPlaying()){
			stopService(AudioService.createStopIntent(this));
		}
		mAudioPlayer.registerObserver(mAudioObserver);
	}
	
	private AudioPlayer.AudioObserver mAudioObserver = new AudioPlayer.SimpleAudioObserver()
	{
		public void onAudioPlayerProgress(AudioPlayer player, int progress, int duration)
		{
			if(mTrack != null && progress != 0 && duration != 0){
				mTrack.getTrackState().update(progress, duration);
			}
		}
	};
	
	@Override
	protected void onStop()
	{
		super.onStop();
		mAudioPlayer.unregisterObserver(mAudioObserver);
		if(mAudioPlayer.isPlaying()){
			startService(AudioService.createStartIntent(this, mTrack));
		}
	}
}
