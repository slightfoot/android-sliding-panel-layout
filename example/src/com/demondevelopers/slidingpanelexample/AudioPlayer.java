package com.demondevelopers.slidingpanelexample;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Observable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.RESET;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.PREPARING;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.PREPARED;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.PLAYING;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.PAUSED;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.STOPPED;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState.FINISHED;
import static com.demondevelopers.slidingpanelexample.AudioPlayer.AudioObserver;


public class AudioPlayer extends Observable<AudioObserver>
{
	private static final String TAG  = AudioPlayer.class.getSimpleName();
	
	private Handler      mHandler    = new Handler(Looper.getMainLooper());
	private final Object mLock       = new Object();
	private WakeLock     mWakeLock   = null;
	private MediaPlayer  mMediaPlayer;
	private AudioState   mState;
	private int          mStreamType = AudioManager.STREAM_MUSIC;
	private boolean      mSeekable;
	private boolean      mBuffering;
	private int          mBuffered;
	private int          mProgress;
	private int          mDuration;
	
	
	public AudioPlayer()
	{
		mMediaPlayer = new MediaPlayer();
		mState       = RESET;
	}
	
	@Override
	protected void finalize() 
		throws Throwable
	{
		try{
			Log.d(TAG, "finalize");
			if(mWakeLock.isHeld()){
				mWakeLock.release();
			}
			mMediaPlayer.release();
		}
		finally{
			super.finalize();
		}
	}
	
	public AudioState getAudioState()
	{
		return mState;
	}
	
	public int getAudioSessionId()
	{
		return mMediaPlayer.getAudioSessionId();
	}
	
	public void setStreamType(int streamType)
	{
		mStreamType = streamType;
	}
	
	public int getStreamType()
	{
		return mStreamType;
	}
	
	public boolean isSeekable()
	{
		return mSeekable;
	}
	
	public boolean isBuffering()
	{
		return mBuffering;
	}
	
	public int getBuffered()
	{
		return mBuffered;
	}
	
	public int getProgress()
	{
		return mProgress;
	}
	
	public int getDuration()
	{
		return mDuration;
	}
	
	public boolean isPlaying()
	{
		return (mState == PLAYING);
	}
	
	public boolean isPaused()
	{
		return (mState == PAUSED);
	}
	
	public boolean play(Context context, String streamUrl)
	{
		Log.d(TAG, "play(" + streamUrl + ")");
		synchronized(mLock){
			if(mWakeLock == null){
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			}
			if(mState != RESET){
				mMediaPlayer.reset();
			}
			try{
				reset();
				mMediaPlayer.setOnPreparedListener       (mOnPrepared);
				mMediaPlayer.setOnBufferingUpdateListener(mOnBuffering);
				mMediaPlayer.setOnSeekCompleteListener   (mOnSeekComplete);
				mMediaPlayer.setOnCompletionListener     (mOnCompletion);
				mMediaPlayer.setOnErrorListener          (mOnError);
				mMediaPlayer.setOnInfoListener           (mOnInfo);
				mMediaPlayer.setAudioStreamType(mStreamType);
				mMediaPlayer.setDataSource(context, Uri.parse(streamUrl));
				mMediaPlayer.prepareAsync();
				setAudioState(PREPARING);
				return true;
			}
			catch(IllegalArgumentException e){
				Log.e(TAG, "play", e);
				return false;
			}
			catch(SecurityException e){
				Log.e(TAG, "play", e);
				return false;
			}
			catch(IOException e){
				Log.e(TAG, "play", e);
				return false;
			}
		}
	}
	
	private void reset()
	{
		mSeekable  = true;
		mBuffering = true;
		mBuffered  = 0;
		mProgress  = 0;
		mDuration  = 0;
		dispatchBufferingUpdate();
		dispatchProgressUpdate();
	}
	
	public synchronized boolean pauseToggle()
	{
		if(mState == PAUSED){
			return start();
		}
		else if(mState == PLAYING){
			return pause();
		}
		return false;
	}
	
	public boolean start()
	{
		synchronized(mLock){
			if(mState != RESET && mState != PREPARING){
				mMediaPlayer.start();
				setAudioState(PLAYING);
				return true;
			}
			Log.d(TAG, "start() false");
			return false;
		}
	}
	
	public boolean seek(int position)
	{
		synchronized(mLock){
			if(mState != RESET && mState != PREPARING){
				mMediaPlayer.seekTo(position);
				return true;
			}
			Log.d(TAG, "seek() false");
			return false;
		}
	}
	
	public boolean pause()
	{
		synchronized(mLock){
			if(mState == PLAYING){
				mMediaPlayer.pause();
				setAudioState(PAUSED);
				return true;
			}
			Log.d(TAG, "pause() false");
			return false;
		}
	}
	
	public boolean stop()
	{
		synchronized(mLock){
			if(mState == PLAYING){
				mMediaPlayer.stop();
				setAudioState(STOPPED);
				return true;
			}
			Log.d(TAG, "stop() false");
			return false;
		}
	}
	
	private void setAudioState(AudioState state)
	{
		mState = state;
		updateWakeLock();
		startStopUpdates();
		dispatchStateChange();
	}
	
	private void dispatchStateChange()
	{
		Log.d(TAG, "dispatchStateChange: " + mState.toString());
		for(int i = mObservers.size() - 1; i >= 0; i--){
			mObservers.get(i).onAudioPlayerStateChange(this, mState);
		}
	}
	
	private void dispatchProgressUpdate()
	{
		Log.d(TAG, "dispatchProgressUpdate: " + mProgress + " of " + mDuration);
		for(int i = mObservers.size() - 1; i >= 0; i--){
			mObservers.get(i).onAudioPlayerProgress(this, mProgress, mDuration);
		}
	}
	
	private void dispatchBufferingUpdate()
	{
		Log.d(TAG, "dispatchBufferingUpdate: " + mBuffering + " > " + mBuffered);
		for(int i = mObservers.size() - 1; i >= 0; i--){
			mObservers.get(i).onAudioPlayerBuffering(this, mBuffering, mBuffered);
		}
	}
	
	@SuppressLint("Wakelock")
	private void updateWakeLock()
	{
		if(mState == PREPARING || mState == PLAYING){
			int timeout = mDuration - mProgress;
			if(mWakeLock.isHeld()){
				mWakeLock.release();
			}
			Log.d(TAG, "Acquring WakeLock for " + timeout);
			mWakeLock.acquire(timeout != 0 ? timeout : 10000);
		}
		else if(mWakeLock.isHeld()){
			Log.d(TAG, "Releasing WakeLock");
			mWakeLock.release();
		}
	}
	
	private void startStopUpdates()
	{
		mHandler.removeCallbacks(mUpdateProgress);
		if(mState == PLAYING){
			Log.d(TAG, "startingUpdates");
			mHandler.post(mUpdateProgress);
		}
		else{
			Log.d(TAG, "stoppingUpdates");
		}
	}
	
	private MediaPlayer.OnPreparedListener mOnPrepared = new MediaPlayer.OnPreparedListener()
	{
		@Override
		public void onPrepared(MediaPlayer mp)
		{
			Log.d(TAG, "onPrepared");
			mProgress = 0;
			mDuration = mp.getDuration();
			setAudioState(PREPARED);
			start();
		}
	};
	
	private Runnable mUpdateProgress = new Runnable()
	{
		@Override
		public void run()
		{
			synchronized(mLock){
				mProgress = mMediaPlayer.getCurrentPosition();
				mDuration = mMediaPlayer.getDuration();
			}
			dispatchProgressUpdate();
			if(mState == PLAYING){
				mHandler.postDelayed(this, 1000);
			}
		}
	};
	
	private MediaPlayer.OnBufferingUpdateListener mOnBuffering = new MediaPlayer.OnBufferingUpdateListener()
	{
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent)
		{
			if(mBuffered != percent){
				Log.d(TAG, "onBufferingUpdate: " + percent);
				mBuffering = (percent == 100);
				mBuffered = percent;
				dispatchBufferingUpdate();
			}
		}
	};
	
	private MediaPlayer.OnSeekCompleteListener mOnSeekComplete = new MediaPlayer.OnSeekCompleteListener()
	{
		@Override
		public void onSeekComplete(MediaPlayer mp)
		{
			Log.d(TAG, "onSeekComplete");
			synchronized(mLock){
				mProgress = mp.getCurrentPosition();
				mDuration = mp.getDuration();
			}
			dispatchProgressUpdate();
		}
	};
	
	private MediaPlayer.OnCompletionListener mOnCompletion = new MediaPlayer.OnCompletionListener()
	{
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			Log.d(TAG, "onCompletion");
			synchronized(mLock){
				setAudioState(FINISHED);
			}
		}
	};
	
	private MediaPlayer.OnErrorListener mOnError = new MediaPlayer.OnErrorListener()
	{
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra)
		{
			Log.e(TAG, "onError: " + what + "(" + extra + ")");
			synchronized(mLock){
				mMediaPlayer.reset();
				setAudioState(RESET);
			}
			return true;
		}
	};
	
	private MediaPlayer.OnInfoListener mOnInfo = new MediaPlayer.OnInfoListener()
	{
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra)
		{
			Log.e(TAG, "onInfo: " + what + "(" + extra + ")");
			synchronized(mLock){
				if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
					mBuffering = true;
					dispatchBufferingUpdate();
				}
				else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
					mBuffering = false;
					dispatchBufferingUpdate();
				}
				else if(what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE){
					mSeekable = false;
					dispatchProgressUpdate();
				}
			}
			return true;
		}
	};
	
	public static enum AudioState
	{
		RESET, PREPARING, PREPARED, PLAYING, PAUSED, STOPPED, FINISHED;
	}
	
	public static interface AudioObserver
	{
		public void onAudioPlayerStateChange(AudioPlayer player, AudioState state);
		public void onAudioPlayerBuffering  (AudioPlayer player, boolean buffering, int progress);
		public void onAudioPlayerProgress   (AudioPlayer player, int progress, int duration);
	}
	
	public static class SimpleAudioObserver implements AudioObserver
	{
		@Override
		public void onAudioPlayerStateChange(AudioPlayer player, AudioState state)
		{
			// does nothing by default
		}
		
		@Override
		public void onAudioPlayerBuffering(AudioPlayer player, boolean buffering, int progress)
		{
			// does nothing by default
		}
		
		@Override
		public void onAudioPlayerProgress(AudioPlayer player, int progress, int duration)
		{
			// does nothing by default
		}
	}
}
