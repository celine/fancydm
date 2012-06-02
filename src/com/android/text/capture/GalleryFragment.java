package com.android.text.capture;

import java.util.ArrayList;

import com.android.text.capture.adapter.ImageAdapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
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
		mAdapter = new ImageAdapter(getActivity(), application.getMemCache());
		mGallery.setAdapter(mAdapter);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				Uri uri = (Uri) adapter.getItemAtPosition(position);
				CanvasFragment canvasFragment = CanvasFragment.newInstance(uri);
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.edit, canvasFragment);
				ft.commit();
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null) {
			String action = getArguments().getString("action");
			updateData(action, args);
		}
		super.onActivityCreated(savedInstanceState);
	}

	public void updateData(String action, Bundle args) {
		if (action.equals(Intent.ACTION_SEND_MULTIPLE)
				|| action.equals(Intent.ACTION_SEND)) {
			ArrayList<Uri> data;
			if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
				data = args.getParcelableArrayList(Intent.EXTRA_STREAM);
			} else {
				data = new ArrayList<Uri>(1);
				data.add((Uri) args.getParcelable(Intent.EXTRA_STREAM));
			}
			Log.d(LOG_TAG, "data size " + data.size());
			mAdapter.updateData(data);
		}
	}
}
