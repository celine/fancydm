package com.android.mydm;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		setContentView(R.layout.main);
		Uri uri = getIntent().getData();
		if (uri != null) {
			Log.d(LOG_TAG, "uri " + uri.toString());
		}
	}

	private static final String LOG_TAG = "MainActivity";

}
