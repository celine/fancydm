package com.android.mydm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.android.mydm.fragment.CanvasFragment;
import com.android.mydm.fragment.GalleryFragment;
import com.android.mydm.fragment.GalleryHeaderFragment;
import com.android.mydm.fragment.GallerySidebarFragment;
import com.evernote.client.oauth.android.EvernoteSession;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class EditActivity extends FragmentActivity {
	private static final String LOG_TAG = "EditActivity";
	GalleryFragment gFragment;
	ArrayList<Uri> mData = new ArrayList<Uri>();
	CanvasFragment cFragment;
	int currentGallery;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_content);
		setUpAccordingToConfiguration(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		cFragment = (CanvasFragment) getSupportFragmentManager()
				.findFragmentById(R.id.edit);
		if (cFragment == null) {
			cFragment = CanvasFragment.newInstance(null);
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.replace(R.id.edit, cFragment);
			ft.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setUpAccordingToConfiguration(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	public boolean toggleGallery() {
		// FragmentTransaction ft =
		// getSupportFragmentManager().beginTransaction();
		// ft.setCustomAnimations(android.R.anim.fade_in,
		// android.R.anim.fade_out);
		View currentView = findViewById(currentGallery);
		if (currentView.getVisibility() == View.GONE) {
			findViewById(currentGallery).setVisibility(View.VISIBLE);
			return true;
		} else {
			findViewById(currentGallery).setVisibility(View.GONE);
			return false;
		}
	}

	private void setUpAccordingToConfiguration(boolean landscape) {
		Log.d(LOG_TAG, "landscape " + landscape);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (gFragment != null) {
			ft.remove(gFragment);
		}
		Bundle args = getIntent().getExtras();
		if (args == null) {
			args = new Bundle();
		}
		updateData();
		args.putParcelableArrayList("data", mData);
		if (landscape) {
			gFragment = new GallerySidebarFragment();
			gFragment.setArguments(args);
			ft.replace(R.id.gallery_sidebar, gFragment);
			findViewById(R.id.gallery_sidebar).setVisibility(View.VISIBLE);
			currentGallery = R.id.gallery_sidebar;
			findViewById(R.id.gallery_header).setVisibility(View.GONE);
		} else {
			gFragment = new GalleryHeaderFragment();
			gFragment.setArguments(args);
			ft.replace(R.id.gallery_header, gFragment);
			findViewById(R.id.gallery_sidebar).setVisibility(View.GONE);
			findViewById(R.id.gallery_header).setVisibility(View.VISIBLE);
			currentGallery = R.id.gallery_header;
		}
		ft.commit();
	}

	private static final int ACTIVITY_CAPTURE = 1;
	private static final int ACTIVITY_PICK = 2;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		Uri uri;
		switch (requestCode) {
		case ACTIVITY_CAPTURE:
			Log.d(LOG_TAG, "action " + data.getAction());
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			uri = Uri.parse(data.getAction());
			setImage(uri);
			mData.add(uri);
		case ACTIVITY_PICK:
			Uri selectedImage = data.getData();
			String[] queryColumns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE,
					MediaStore.Images.Media.DISPLAY_NAME };
			Cursor cursor = getContentResolver().query(selectedImage,
					queryColumns, null, null, null);
			cursor.moveToFirst();
			String filePath = cursor.getString(cursor
					.getColumnIndex(queryColumns[0]));
			File file = new File(filePath);
			String mimeType = cursor.getString(cursor
					.getColumnIndex(queryColumns[1]));
			String fileName = cursor.getString(cursor
					.getColumnIndex(queryColumns[2]));
			cursor.close();
			uri = Uri.fromFile(file);
			setImage(uri);
			mData.add(uri);
		}
		gFragment.updateData(mData);

	}

	private void setImage(Uri uri) {
		cFragment.setImage(uri);
		gFragment.updateData(mData);
	}

	public void updateData() {
		String action = getIntent().getAction();
		Bundle args = getIntent().getExtras();
		if (action.equals(Intent.ACTION_SEND_MULTIPLE)
				|| action.equals(Intent.ACTION_SEND)) {
			ArrayList<Uri> data;
			if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
				data = args.getParcelableArrayList(Intent.EXTRA_STREAM);
			} else {
				data = new ArrayList<Uri>(1);
				data.add((Uri) args.getParcelable(Intent.EXTRA_STREAM));
			}
			mData.addAll(data);
		}
	}

	public void launchGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(intent, ACTIVITY_PICK);
	}

	public void launchCamera() {
		File dir = new File(Environment.getExternalStorageDirectory(), "tmp");
		if (!dir.canWrite()) {
			return;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
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
}