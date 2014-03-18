package com.brew.brewdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OnOffIndicator extends View {

	private SWITCH_STATE switchState;

	private int fillColor;
	private Paint fillPaint;
	private RectF fillRect;

	public OnOffIndicator(Context context) {
		super(context);
		switchState = SWITCH_STATE.OFF;
		init();
	}

	public OnOffIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		switchState = SWITCH_STATE.OFF;
		init();
	}

	public OnOffIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		switchState = SWITCH_STATE.OFF;
		fillPaint = new Paint();

		fillRect = new RectF();
	}

	public void setSwitchState(SWITCH_STATE switchState) {
		this.switchState = switchState;
		setColor();
		invalidate();
	}

	public SWITCH_STATE getSwitchState() {
		return switchState;
	}

	private void setColor() {

		switch (switchState) {
		case OFF:
			fillColor = Color.RED;
			break;
		case ON:
			fillColor = Color.GREEN;
			break;
		case PENDING_OFF:
			fillColor = 0xFF8C0000;
			break;
		case PENDING_ON:
			fillColor = 0xFF00660E;
			break;
		default:
			break;
		}

		fillPaint.setColor(fillColor);

		// setBackgroundColor(color);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		fillRect.set(0, 0, right, bottom);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawRoundRect(fillRect, 0f, 0f, fillPaint);

		super.onDraw(canvas);
	}

	public enum SWITCH_STATE {

		ON, OFF, PENDING_ON, PENDING_OFF
	}

}
