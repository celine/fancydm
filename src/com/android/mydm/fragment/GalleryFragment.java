package com.android.mydm.fragment;

import java.util.ArrayList;

import com.android.mydm.CheckListApplication;
import com.android.mydm.EditActivity;
import com.android.mydm.R;
import com.android.mydm.R.id;
import com.android.mydm.adapter.ImageAdapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.GridView;

public abstract class GalleryFragment extends Fragment {
	private static final String LOG_TAG = "GalleryFragment";
	ImageAdapter mAdapter;
	AdapterView<ImageAdapter> mGallery;
	int resId;
	public GalleryFragment(int resId) {
		this.resId = resId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView");
		View view = inflater.inflate(resId, container, false);
		mGallery = (AdapterView) view.findViewById(R.id.gallery);
		CheckListApplication application = (CheckListApplication) getActivity()
				.getApplication();
		mAdapter = new ImageAdapter(getActivity(), application.getCacheManager());
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				if (position == adapter.getCount() - 1) {
					EditActivity activity = (EditActivity) getActivity();
					activity.launchGallery();
				} else {
					CanvasFragment fragment = (CanvasFragment) getFragmentManager()
							.findFragmentById(R.id.edit);
					Uri uri = (Uri) adapter.getItemAtPosition(position);
					if (fragment == null) {
						fragment = CanvasFragment.newInstance(uri);
						FragmentTransaction ft = getFragmentManager()
								.beginTransaction();
						ft.replace(R.id.edit, fragment);
						ft.commit();
					} else {
						fragment.setImage(uri);
					}
				}
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null) {
			ArrayList<Uri> data = getArguments().getParcelableArrayList("data");
			updateData(data);
		}
		super.onActivityCreated(savedInstanceState);
	}

	public void updateData(ArrayList<Uri> data) {
		mAdapter.updateData(data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
	}
	
}
