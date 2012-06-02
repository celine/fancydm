package com.android.text.capture;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class EditActivity extends FragmentActivity {
	private static final String LOG_TAG = "EditActivity";
	GalleryFragment mFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_content);

		setUpAccordingToConfiguration(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setUpAccordingToConfiguration(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	private void setUpAccordingToConfiguration(boolean landscape) {
		Log.d(LOG_TAG, "landscape " + landscape);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (mFragment != null) {
			ft.remove(mFragment);
		}
		Bundle args = getIntent().getExtras();
		if (args == null) {
			args = new Bundle();
		}
		args.putString("action", getIntent().getAction());
		if (landscape) {
			mFragment = new GallerySidebarFragment();
			mFragment.setArguments(args);
			ft.replace(R.id.gallery_sidebar, mFragment);
			findViewById(R.id.gallery_sidebar).setVisibility(View.VISIBLE);
			findViewById(R.id.gallery_header).setVisibility(View.GONE);
		} else {
			mFragment = new GalleryHeaderFragment();
			mFragment.setArguments(args);
			ft.replace(R.id.gallery_header, mFragment);
			findViewById(R.id.gallery_sidebar).setVisibility(View.GONE);
			findViewById(R.id.gallery_header).setVisibility(View.VISIBLE);
		}
		ft.commit();
	}

}