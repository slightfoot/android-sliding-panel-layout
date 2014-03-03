/*
	Copyright 2014 Demon Developers Ltd
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.demondeveloprs.slidingpanellayout;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * Slide Up Panel Layout
 * 
 * @version 1.0
 * @author Simon Lightfoot <simon@demondevelopers.com>
 * 
 */
public class SlideUpPanelLayout extends ViewGroup
{
	private GestureDetector mGestureDetector;
	private ObjectAnimator  mAnimator;
	
	private PanelState mState = PanelState.HIDDEN;
	
	private int   mPanelId, mHandleId;
	private View  mPanel,   mHandle;
	
	private SavedState mLastSavedState;
	private boolean    mFirstLayout = true;
	
	
	public SlideUpPanelLayout(Context context)
	{
		super(context);
		initView(context, null);
	}
	
	public SlideUpPanelLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initView(context, attrs);
	}
	
	public SlideUpPanelLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initView(context, attrs);
	}
	
	protected void initView(Context context, AttributeSet attrs)
	{
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideUpPanelLayout);
		mPanelId  = a.getResourceId(R.styleable.SlideUpPanelLayout_slidePanelId,  View.NO_ID);
		mHandleId = a.getResourceId(R.styleable.SlideUpPanelLayout_slideHandleId, View.NO_ID);
		a.recycle();
		
		setWillNotDraw(false);
		
		mGestureDetector = new GestureDetector(getContext(), mHandleGestureDetector);
		mAnimator = ObjectAnimator.ofFloat(this, "panelOffset", 0.0f);
		mAnimator.addListener(mSlidingFinished);
	}
	
	/**
	 * Shows handle when previously hidden
	 */
	public void showHandle()
	{
		if(mState == PanelState.HIDDEN){
			mPanel.animate()
				.translationY(0)
				.setListener(mShownFinished)
				.start();
		}
	}
	
	/**
	 * Hides handle from any state.
	 */
	public void hideHandle()
	{
		mPanel.animate()
			.translationY(mHandle.getHeight())
			.setListener(mHiddenFinished)
			.start();
	}
	
	/**
	 * Toggles handle's visible state.
	 */
	public void toggleHandle()
	{
		if(mState == PanelState.HIDDEN){
			showHandle();
		}
		else{
			hideHandle();
		}
	}
	
	/**
	 * Animates panel to its open state.
	 */
	public void animateOpen()
	{
		animatePanel(true);
	}
	
	/**
	 * Animates panel to its closed state.
	 */
	public void animateClose()
	{
		animatePanel(false);
	}
	
	/**
	 * Sets the panel offset to a fixed offset.
	 * 
	 * @param offset panel offset
	 */
	public void setPanelOffset(float offset)
	{
		mPanel.setTranslationY(Math.min(0, Math.max(offset, getMaxPanelOffset())));
	}
	
	/**
	 * Gets panel's current offset.
	 * 
	 * @return panel offset
	 */
	public float getPanelOffset()
	{
		return mPanel.getTranslationY();
	}
	
	/**
	 * Gets the panel's open offset.
	 * 
	 * @return panel open offset
	 */
	public float getMaxPanelOffset()
	{
		return -(mPanel.getHeight() - mHandle.getMeasuredHeight());
	}
	
	/**
	 * Get's the panel's current state.
	 * 
	 * @return current panel state
	 */
	public PanelState getPanelState()
	{
		return mState;
	}
	
	/**
	 * Sets the Panel view to be controlled.
	 * 
	 * @param panel view
	 */
	public void setPanelView(View panel)
	{
		mPanel = panel;
		if(mPanel != null){
			mPanel.bringToFront();
		}
	}
	
	/**
	 * Sets the Handle view to control the panel.
	 * 
	 * @param handle view
	 */
	public void setHandleView(View handle)
	{
		mHandle = handle;
		if(mHandle != null){
			mHandle.setOnTouchListener(mHandleTouchListener);
		}
	}
	
	/**
	 * Called when layout inflation is completed and we try to
	 * auto detect our required Panel and Handle views.
	 */
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		
		if(mPanelId != View.NO_ID){
			View panel = findViewById(mPanelId);
			if(panel == null){
				throw new IllegalStateException("Cannot find Panel view");
			}
			setPanelView(panel);
		}
		
		if(mHandleId != View.NO_ID){
			View handle = findViewById(mHandleId);
			if(handle == null){
				throw new IllegalStateException("Cannot find Handle view");
			}
			setHandleView(handle);
		}
	}
	
	/**
	 * Default measures its child views.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * Layout forces the position of the Panel view and expects the Handle view to
	 * be at the top of the Panel view.
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		final int count = getChildCount();
		for(int i = 0; i < count; i++){
			View child = getChildAt(i);
			if(child.getVisibility() == View.GONE) continue;
			if(child == mPanel && mPanel != null && mHandle != null){
				int defaultHandleTop = (bottom - top) - mHandle.getMeasuredHeight();
				child.layout(
					left,  defaultHandleTop, 
					right, defaultHandleTop + mPanel.getMeasuredHeight());
			}
			else{
				child.layout(left, top, right, bottom);
			}
		}
		if(mFirstLayout){
			if(mLastSavedState != null){
				switch(mLastSavedState.mState){
					case HIDDEN:
						mPanel.setTranslationY(mHandle.getHeight());
						break;
					case CLOSED:
						mPanel.setTranslationY(0);
						break;
					case DRAGGING:
					case SLIDING:
					case OPEN:
						mPanel.setTranslationY(getMaxPanelOffset());
						break;
				}
				mState = mLastSavedState.mState;
				mLastSavedState = null;
			}
			else{
				mPanel.setTranslationY(mHandle.getHeight());
			}
			mFirstLayout = false;
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.mState = mState;
		return ss;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		BaseSavedState base = (BaseSavedState)state;
		if(base instanceof SavedState){
			mLastSavedState = (SavedState)base;
		}
		super.onRestoreInstanceState(base.getSuperState());
	}
	
	private void setPanelState(PanelState state)
	{
		mState = state;
	}
	
	private void animatePanel(boolean openOrClosed)
	{
		setPanelState(PanelState.SLIDING);
		if(mAnimator != null){
			mAnimator.cancel();
		}
		mAnimator.setFloatValues((openOrClosed) ? getMaxPanelOffset() : 0);
		mAnimator.start();
	}
	
	private AnimatorListener mShownFinished = new AnimatorListenerAdapter()
	{
		@Override
		public void onAnimationEnd(Animator animation)
		{
			setPanelState(PanelState.CLOSED);
		}
	};
	
	private AnimatorListener mHiddenFinished = new AnimatorListenerAdapter()
	{
		@Override
		public void onAnimationEnd(Animator animation)
		{
			setPanelState(PanelState.HIDDEN);
		}
	};
	
	private AnimatorListener mSlidingFinished = new AnimatorListenerAdapter()
	{
		@Override
		public void onAnimationEnd(Animator animation)
		{
			float offset = getPanelOffset();
			if(offset == 0){
				setPanelState(PanelState.CLOSED);
			}
			else if(offset == getMaxPanelOffset()){
				setPanelState(PanelState.OPEN);
			}
		}
	};
	
	private View.OnTouchListener mHandleTouchListener = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			boolean result = mGestureDetector.onTouchEvent(event);
			final int action = event.getAction();
			if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
				result |= snapPanel();
			}
			return result;
		}
	};
	
	private boolean snapPanel()
	{
		if(mState != PanelState.SLIDING){
			if(getPanelOffset() < getMaxPanelOffset() / 2){
				animateOpen();
			}
			else{
				animateClose();
			}
			return true;
		}
		return false;
	}
	
	private GestureDetector.OnGestureListener mHandleGestureDetector 
		= new GestureDetector.SimpleOnGestureListener()
	{
		private float mStartOffset = 0;
		
		@Override
		public boolean onDown(MotionEvent e)
		{
			mStartOffset = e.getRawY() - mPanel.getTranslationY();
			if(mState == PanelState.SLIDING){
				if(mAnimator != null){
					mAnimator.cancel();
				}
				setPanelState(PanelState.DRAGGING);
			}
			return true;
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			setPanelState(PanelState.DRAGGING);
			setPanelOffset(e2.getRawY() - mStartOffset);
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			animatePanel(e1.getRawY() > e2.getRawY());
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			if(mState == PanelState.OPEN){
				animateClose();
			}
			else if(mState == PanelState.CLOSED){
				animateOpen();
			}
			return true;
		}
	};
	
	/**
	 * Panel State
	 */
	public static enum PanelState
	{
		HIDDEN, CLOSED, DRAGGING, SLIDING, OPEN
	}
	
	static class SavedState extends BaseSavedState
	{
		public PanelState mState;
		
		
		public SavedState(Parcelable superState)
		{
			super(superState);
		}
		
		public SavedState(Parcel in)
		{
			super(in);
			mState = Enum.valueOf(PanelState.class, in.readString());
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeString(mState.toString());
		}
		
		
		public static final Parcelable.Creator<SavedState> CREATOR = 
			new Parcelable.Creator<SlideUpPanelLayout.SavedState>()
		{
			@Override
			public SavedState createFromParcel(Parcel source)
			{
				return new SavedState(source);
			}
			
			@Override
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}
}
