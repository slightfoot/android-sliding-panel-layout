package com.demondevelopers.slidingpanelexample.model;

import java.io.Serializable;


public class Track implements Serializable
{
	private static final long serialVersionUID = -7659723541823175369L;
	
	private String  mId;
	private String  mCreated_at;
	private Long    mDuration;
	private Boolean mStreamable;
	private String  mGenre;
	private String  mTitle;
	private String  mDescription;
	private String  mUri;
	private String  mPermaLink_url;
	private String  mArtwork_url;
	private String  mWaveform_url;
	private String  mStream_url;
	private User    mUser;
	
	private transient TrackState mTrackState = new TrackState();
	
	
	public String getId()
	{
		return mId;
	}
	
	public String getCreatedAt()
	{
		return mCreated_at;
	}
	
	public Long getDuration()
	{
		return mDuration;
	}
	
	public Boolean getStreamable()
	{
		return mStreamable;
	}
	
	public String getGenre()
	{
		return mGenre;
	}
	
	public String getTitle()
	{
		return mTitle;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public String getUri()
	{
		return mUri;
	}
	
	public String getPermaLinkUrl()
	{
		return mPermaLink_url;
	}
	
	public String getArtworkUrl()
	{
		return mArtwork_url;
	}
	
	public String getWaveformUrl()
	{
		return mWaveform_url;
	}
	
	public String getStreamUrl()
	{
		return mStream_url;
	}
	
	public User getUser()
	{
		return mUser;
	}
	
	public TrackState getTrackState()
	{
		if(mTrackState == null){
			mTrackState = new TrackState();
		}
		return mTrackState;
	}
}
