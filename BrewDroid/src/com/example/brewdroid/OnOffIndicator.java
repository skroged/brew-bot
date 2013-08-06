package com.example.brewdroid;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class OnOffIndicator extends View {

	private boolean on;

	public OnOffIndicator(Context context) {
		super(context);
		setOn(false);
	}

	public OnOffIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOn(false);
	}

	public OnOffIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setOn(false);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;

		int color = on ? Color.GREEN : Color.RED;

		setBackgroundColor(color);
	}

}
