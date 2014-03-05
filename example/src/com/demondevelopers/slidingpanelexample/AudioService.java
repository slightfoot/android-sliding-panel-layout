package com.demondevelopers.slidingpanelexample;

import com.demondevelopers.slidingpanelexample.AudioPlayer.AudioState;
import com.demondevelopers.slidingpanelexample.model.Track;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


public class AudioService extends Service
{
	private static final String TAG = AudioService.class.getSimpleName();
	
	private static final int NOTIFICATION_ID = 1;
	
	private static final String ACTION_START = "actionStart";
	private static final String ACTION_STOP  = "actionStop";
	private static final String EXTRA_TRACK  = "extraTrack";
	
	private Track        mTrack;
	private AudioPlayer  mAudioPlayer;
	
	
	public static final Intent createStartIntent(Context context, Track track)
	{
		return new Intent(context, AudioService.class)
			.setAction(ACTION_START)
			.putExtra(EXTRA_TRACK, track);
	}
	
	public static final Intent createStopIntent(Context context)
	{
		return new Intent(context, AudioService.class)
			.setAction(ACTION_STOP);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mAudioPlayer = ExampleApp.from(this).getAudioPlayer();
		mAudioPlayer.registerObserver(mAudioObserver);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.w(TAG, "onStartCommand: " + intent);
		if(intent == null){
			stopSelf();
			return 0;
		}
		
		if(intent.getAction().equals(ACTION_START)){
			mTrack = (Track)intent.getSerializableExtra(EXTRA_TRACK);
			startForeground(NOTIFICATION_ID, updateNotification(0, 0));
		}
		else if(intent != null && intent.getAction().equals(ACTION_STOP)){
			stopSelf();
		}
		
		return START_STICKY;
	}
	
	private AudioPlayer.AudioObserver mAudioObserver = new AudioPlayer.SimpleAudioObserver()
	{
		@Override
		public void onAudioPlayerStateChange(AudioPlayer player, AudioState state)
		{
			if(state == AudioState.FINISHED){
				stopSelf();
			}
		}
		public void onAudioPlayerProgress(AudioPlayer player, int progress, int duration)
		{
			if(mTrack != null){
				((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
					.notify(NOTIFICATION_ID, updateNotification(progress, duration));
			}
		};
	};
	
	@Override
	public void onDestroy()
	{
		mAudioPlayer.unregisterObserver(mAudioObserver);
		stopForeground(true);
	}
	
	private Notification updateNotification(int progress, int max)
	{
		String title = mTrack != null ? mTrack.getTitle() : "Playing in background";
		String desc  = mTrack != null ? mTrack.getDescription() : "Some description";
		//Bitmap bmp   = mTrack != null ? mTrack.getArtworkUrl() : null;
		Notification.Builder builder = new Notification.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(title)
			.setContentText(desc)
			.setContentIntent(PendingIntent.getActivity(this, 0, 
				MainActivity.createIntent(this, mTrack), 0))
			.setOngoing(true)
			.setTicker("Audio playing in background");
		if(mTrack != null){
			builder.setProgress(max, progress, false);
		}
		addNotificationPriority(builder);
		return buildNotification(builder);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void addNotificationPriority(Notification.Builder builder)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			builder.setPriority(Notification.PRIORITY_HIGH);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	private Notification buildNotification(Notification.Builder builder)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			return builder.build();
		}
		else{
			return builder.getNotification();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
