package com.demondevelopers.slidingpanelexample.widget;

import com.demondevelopers.slidingpanelexample.R;
import com.demondevelopers.slidingpanelexample.model.Track;
import com.demondevelopers.slidingpanelexample.model.TrackState;
import com.demondevelopers.slidingpanelexample.model.TrackState.TrackStateChanged;
import com.squareup.picasso.Picasso;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class TrackView extends FrameLayout
	implements TrackStateChanged
{
	private static final String TAG = TrackView.class.getSimpleName();
	
	private Picasso   mPicasso;
	private Track     mTrack;
	
	private ImageView mWaveform;
	private ImageView mArtwork;
	private TextView  mUser;
	private TextView  mTitle;
	private TextView  mGenre;
	
	private Drawable       mProgressWaveform;
	private ObjectAnimator mProgressAnimator;
	
	
	public TrackView(Context context)
	{
		super(context);
		initView(context, null);
	}
	
	public TrackView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initView(context, attrs);
	}
	
	public TrackView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initView(context, attrs);
	}
	
	protected void initView(Context context, AttributeSet attrs)
	{
		mPicasso = Picasso.with(getContext());
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mWaveform = (ImageView)findViewById(R.id.track_waveform);
		mArtwork  = (ImageView)findViewById(R.id.track_artwork);
		mUser     = (TextView)findViewById(R.id.track_user);
		mTitle    = (TextView)findViewById(R.id.track_title);
		mGenre    = (TextView)findViewById(R.id.track_genre);
		
		mProgressWaveform = mWaveform.getBackground();
		mProgressAnimator = ObjectAnimator.ofInt(mProgressWaveform, "level", 0);
		mProgressAnimator.setInterpolator(new LinearInterpolator());
		mProgressAnimator.setDuration(1000);
	}
	
	public void setTrack(Track track)
	{
		if(mTrack != null){
			mTrack.getTrackState().unregisterObserver(this);
			mProgressAnimator.cancel();
			mProgressWaveform.setLevel(0);
		}
		if(track != null){
			setImageViewUrl(mWaveform, track.getWaveformUrl());
			String artwork = track.getArtworkUrl();
			if(TextUtils.isEmpty(artwork)){
				artwork = track.getUser().getAvatarUrl();
			}
			setImageViewUrl(mArtwork,  artwork);
			setTextViewText(mUser,     track.getUser().getUsername());
			setTextViewText(mTitle,    track.getTitle());
			setTextViewText(mGenre,    track.getGenre());
			
			TrackState state = track.getTrackState();
			updateProgress(false, state.getProgress(), state.getTotal());
			state.registerObserver(this);
			mTrack = track;
		}
	}
	
	public Track getTrack()
	{
		return mTrack;
	}
	
	@Override
	public void onTrackStateChange(TrackState state)
	{
		updateProgress(true, state.getProgress(), state.getTotal());
	}
	
	private void updateProgress(boolean animate, int progress, int total)
	{
		float value = (10000.0f / total);
		if(animate){
			mProgressAnimator.setIntValues((int)(value * (progress + 1000)));
			mProgressAnimator.start();
		}
		else{
			mProgressAnimator.cancel();
			mProgressWaveform.setLevel((int)(value * progress));
		}
	}
	
	private void setTextViewText(TextView tv, CharSequence text)
	{
		if(tv != null){
			if(!TextUtils.isEmpty(text)){
				tv.setVisibility(View.VISIBLE);
				tv.setText(text);
			}
			else{
				tv.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void setImageViewUrl(ImageView iv, String url)
	{
		if(iv != null){
			if(!TextUtils.isEmpty(url)){
				iv.setVisibility(View.VISIBLE);
				mPicasso
					.load(Uri.parse(url))
					.into(iv);
			}
			else{
				mPicasso.cancelRequest(iv);
				iv.setImageDrawable(null);
				iv.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	/*
	private void loadWaveform(String url)
	{
		mPicasso.load(Uri.parse(url)).into(new Target()
		{
			
			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable)
			{
			}
			
			@Override
			public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from)
			{
			}
			
			@Override
			public void onBitmapFailed(Drawable errorDrawable)
			{
			}
		});
	}
	*/
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		SavedState ss = new SavedState(super.onSaveInstanceState());
		ss.track   = mTrack;
		return ss;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		SavedState ss = (SavedState)state;
		super.onRestoreInstanceState(ss.getSuperState());
		setTrack(ss.track);
	}
	
	
	static class SavedState extends BaseSavedState
	{
		Track track;
		
		
		public SavedState(Parcelable superState)
		{
			super(superState);
		}
		
		public SavedState(Parcel in)
		{
			super(in);
			track = (Track)in.readSerializable();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeSerializable(track);
		}
		
		
		public static final Parcelable.Creator<SavedState> CREATOR 
			= new Parcelable.Creator<SavedState>()
		{
			public SavedState createFromParcel(Parcel in)
			{
				return new SavedState(in);
			}
			
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}
}
