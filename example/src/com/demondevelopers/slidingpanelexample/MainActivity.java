package com.demondevelopers.slidingpanelexample;

import com.demondeveloprs.slidingpanellayout.SlideUpPanelLayout;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


public class MainActivity extends FragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final SlideUpPanelLayout layout = (SlideUpPanelLayout)findViewById(R.id.slide_layout);
		findViewById(R.id.content).setOnClickListener(new View.OnClickListener()
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
		});
	}
}
