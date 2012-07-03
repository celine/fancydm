package com.android.mydm;

import com.android.mydm.fragment.ListNotebookFragment;
import com.android.mydm.util.EvernoteUtil;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle args) {
		super.onCreate(args);
		setContentView(R.layout.main);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.panel1, new ListNotebookFragment(), "notebooks");
		ft.commit();
	}

	private static final String LOG_TAG = "MainActivity";

}
