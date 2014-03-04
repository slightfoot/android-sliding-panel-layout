package com.demondevelopers.slidingpanelexample.frags;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.demondevelopers.slidingpanelexample.R;
import com.demondevelopers.slidingpanelexample.MainActivity.OnProgressUpdate;
import com.demondevelopers.slidingpanelexample.model.Track;
import com.demondevelopers.slidingpanelexample.model.Tracks;
import com.demondevelopers.slidingpanelexample.util.RemoteJsonLoader;
import com.demondevelopers.slidingpanelexample.widget.TrackView;


public class MostRecentFragment extends BaseFragment 
	implements LoaderCallbacks<Tracks>, OnProgressUpdate
{
	private static final String TAG = MostRecentFragment.class.getSimpleName();
	
	private static final int LOADER_TRACKS = 0;
	
	private OnTrackSelected mOnTrackSelected;
	
	private ListView mListView;
	private TracksAdapter mAdapter;
	
	private HashMap<String, ProgressListener> mUpdateListeners = new HashMap<String, ProgressListener>();
	
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try{
			mOnTrackSelected = (OnTrackSelected)activity;
		}
		catch(ClassCastException e){
			Log.w(TAG, "Your activity should probably implement OnTrackSelected");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mAdapter = new TracksAdapter(getActivity());
		
		getLoaderManager().initLoader(LOADER_TRACKS, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_mostrecent, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		mListView = (ListView)view.findViewById(R.id.list);
		mListView.setEmptyView(view.findViewById(R.id.empty));
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Track track = (Track)parent.getItemAtPosition(position);
				Log.w(TAG, "onItemClick: [" + position + "] " + track.getId() + ": " + track.getTitle());
				if(mOnTrackSelected != null){
					mOnTrackSelected.onTrackSelected(track);
				}
			}
		});
		mListView.setRecyclerListener(new ListView.RecyclerListener()
		{
			@Override
			public void onMovedToScrapHeap(View view)
			{
				if(view instanceof TrackView){
					Track track = ((TrackView)view).getTrack();
					if(track != null){
						mUpdateListeners.remove(track.getId());
					}
				}
			}
		});
	}
	
	@Override
	public void onProgressUpdate(Track track, float progress)
	{
		ProgressListener listener = mUpdateListeners.get(track.getId());
		if(listener != null){
			listener.updateProgress(progress);
		}
	}
	
	@Override
	public Loader<Tracks> onCreateLoader(int id, Bundle args)
	{
		return new RemoteJsonLoader<Tracks>(getActivity(), Tracks.class,
			"https://api.soundcloud.com/tracks.json?filter=all&order=created_at&" + 
			"client_id=54647ab8dc2cd5e57c6163c7fb6e683d");
	}
	
	@Override
	public void onLoadFinished(Loader<Tracks> loader, Tracks data)
	{
		if(data != null){
			mAdapter.setTracks(data);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Tracks> loader)
	{
		//
	}
	
	public static interface OnTrackSelected
	{
		public void onTrackSelected(Track track);
	}
	
	public static interface ProgressListener
	{
		public void updateProgress(float progress);
	}
	
	private class TracksAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;
		private Tracks mTracks;
		
		
		public TracksAdapter(Context context)
		{
			mInflater = LayoutInflater.from(context);
		}
		
		public void setTracks(Tracks tracks)
		{
			mTracks = tracks;
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.item_track, parent, false);
			}
			
			Track track = mTracks.get(position);
			TrackView trackView = (TrackView)convertView;
			trackView.setTrack(track);
			mUpdateListeners.put(track.getId(), trackView);
			return convertView;
		}
		
		@Override
		public long getItemId(int position)
		{
			return mTracks.get(position).getId().hashCode();
		}
		
		@Override
		public Track getItem(int position)
		{
			return mTracks.get(position);
		}
		
		@Override
		public int getCount()
		{
			return (mTracks != null) ? mTracks.size() : 0;
		}
	}
}
