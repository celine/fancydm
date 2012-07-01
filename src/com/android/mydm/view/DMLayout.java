package com.android.mydm.view;

import com.android.mydm.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DMLayout extends LinearLayout {
	LinearLayout mCurrentRow;

	public DMLayout(Context context) {
		super(context);
		init();
	}

	public DMLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DMLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	int margin;

	public void init() {
		setOrientation(VERTICAL);
		margin = getContext().getResources().getDimensionPixelSize(
				R.dimen.dm_margin);
	}

	public LinearLayout newRow() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(layout);
		return layout;
	}

	public void addChildView(View addChild, int childWidth) {
		int width = getWidth();
		int rowWidth = 0;
		if (mCurrentRow != null) {
			for (int i = 0; i < mCurrentRow.getChildCount(); i++) {
				View child = mCurrentRow.getChildAt(i);
				rowWidth += child.getWidth();

				Log.d("AAAA", "child  " + child.getWidth() + " padding "
						+ child.getPaddingRight());
			}
		}
		if (mCurrentRow == null || width - rowWidth < childWidth) {
			mCurrentRow = newRow();
		}

		Log.d("AAAA", "childWidth " + childWidth);
		LinearLayout.LayoutParams lp = (LayoutParams) addChild
				.getLayoutParams();
		lp.leftMargin = margin;
		lp.topMargin = margin;
		mCurrentRow.addView(addChild, lp);
		invalidate();
	}

}
