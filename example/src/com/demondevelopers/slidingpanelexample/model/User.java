package com.demondevelopers.slidingpanelexample.model;

import java.io.Serializable;


public class User implements Serializable
{
	private static final long serialVersionUID = -1066018220851539064L;
	
	private String mId;
	private String mUsername;
	private String mUri;
	private String mPermalink_url;
	private String mAvatar_url;
	
	
	public String getId()
	{
		return mId;
	}
	
	public String getUsername()
	{
		return mUsername;
	}
	
	public String getUri()
	{
		return mUri;
	}
	
	public String getPermalinkUrl()
	{
		return mPermalink_url;
	}
	
	public String getAvatarUrl()
	{
		return mAvatar_url;
	}
}
