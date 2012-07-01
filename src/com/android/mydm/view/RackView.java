package com.android.mydm.view;

import com.android.mydm.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.GridView;

public class RackView extends GridView {

	public RackView(Context context) {
		super(context);
	}

	public RackView(Context context, AttributeSet attrs) {
		super(context, attrs);
		load(context, attrs, 0);
	}

	Bitmap mBackground;
	int backgroundWidth;
	int backgroundHeight;
	private static final String LOG_TAG = "RackView";

	public void load(Context context, AttributeSet attrs, int defStyle) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RackView, defStyle, 0);
		Log.d(LOG_TAG, "a count " + a.getIndexCount());
		int resId = a.getResourceId(R.styleable.RackView_rackBackground, 0);
		if (resId != 0) {
			mBackground = BitmapFactory.decodeResource(getResources(), resId);
			backgroundWidth = mBackground.getWidth();
			backgroundHeight = mBackground.getHeight();
		}
		a.recycle();
	}

	public RackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		load(context, attrs, defStyle);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mBackground != null) {
			int width = getWidth();
			int height = getHeight();
			int top = getChildCount() == 0 ? 0 : getChildAt(0).getTop();
			for (int i = 0; i < width; i += backgroundWidth) {
				for (int j = top; j < height; j += backgroundHeight) {
					canvas.drawBitmap(mBackground, i, j, null);
				}
			}
		}
		super.dispatchDraw(canvas);
	}
}
