package com.android.text.capture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.android.text.capture.util.BitmapUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CanvasFragment extends Fragment {
	ImageView mImg;
	private static final String LOG_TAG = "CanvasFragment";

	public CanvasFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		CheckListApplication application = (CheckListApplication) getActivity()
				.getApplication();
		LruCache<String, Bitmap> memCache = application.getMemCache();
		Uri uri = getArguments().getParcelable("uri");
		if (uri != null) {

			String key = uri.getLastPathSegment() + "_o";
			try {
				Bitmap bitmap = memCache.get(key);
				if (bitmap == null) {
					bitmap = BitmapUtils
							.decodeSampledBitmapFromUriWithMaxWidth(
									getActivity().getContentResolver(), uri,
									640);
					memCache.put(key, bitmap);
				}
				mImg.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchCamera();

			}
		});
		super.onActivityCreated(savedInstanceState);
	}

	private void getFromGallery() {
		Intent intent = new Intent();
	}

	private void launchCamera() {
		File dir = new File(Environment.getExternalStorageDirectory(), "tmp");
		if (!dir.canWrite()) {
			return;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String state = Environment.getExternalStorageState();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(dir, String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		Uri mImageCaptureUri;
		try {
			file.createNewFile();
			mImageCaptureUri = Uri.fromFile(file);

		} catch (IOException e) {
			Log.e(LOG_TAG, "error", e);
			return;
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		// intent.putExtra("crop", "true");
		// intent.putExtra("aspectX", 1);
		// intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", 640);
		// intent.putExtra("outputY", 480);

		// intent.putExtra("return-data", true);
		startActivityForResult(intent, ACTIVITY_CAPTURE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.edit, container, false);
		mImg = (ImageView) view.findViewById(R.id.img);

		return view;
	}

	private static final int ACTIVITY_CAPTURE = 1;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case ACTIVITY_CAPTURE:
			Log.d(LOG_TAG, "action " + data.getAction());
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;

			File file = new File(Uri.parse(data.getAction()).getPath());
			Log.d(LOG_TAG, "file exists " + file.exists());
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			mImg.setImageBitmap(bitmap);
			mImg.setClickable(false);
		}
	}

	public static CanvasFragment newInstance(Uri uri) {
		CanvasFragment fragment = new CanvasFragment();
		Bundle args = new Bundle();
		args.putParcelable("uri", uri);
		fragment.setArguments(args);
		return fragment;
	}
}
