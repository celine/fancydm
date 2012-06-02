package com.android.text.capture.adapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.text.capture.R;
import com.android.text.capture.util.BitmapUtils;

public class ImageAdapter extends BaseAdapter {
	LruCache<String, Bitmap> mMemCache;
	ArrayList<Uri> mUris = new ArrayList<Uri>();
	LayoutInflater mInflater;
	ContentResolver mResolver;
	private static int PIC_SIZE = 150;

	public ImageAdapter(Context context, LruCache<String, Bitmap> memCache) {
		PIC_SIZE = context.getResources().getDimensionPixelSize(
				R.dimen.preview_size);
		mMemCache = memCache;
		mInflater = LayoutInflater.from(context);
		mResolver = context.getContentResolver();
	}

	@Override
	public int getCount() {
		return mUris.size();
	}

	@Override
	public Uri getItem(int position) {
		return mUris.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static final String LOG_TAG = "ImageAdapter";

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.image, parent, false);
		}
		Uri uri = getItem(position);
		String key = generateKeyFromUri(uri);
		Bitmap bitmap = mMemCache.get(key);
		ImageView img = (ImageView) convertView;
		img.setTag(key);
		if (bitmap != null) {
			img.setImageBitmap(bitmap);
		} else {
			img.setImageResource(R.drawable.preview);
			new BitmapWorkerTask(mResolver, mMemCache, img).execute(uri);
		}
		return convertView;
	}

	public void updateData(ArrayList<Uri> data) {
		mUris = data;
		notifyDataSetChanged();
	}

	private static String generateKeyFromUri(Uri uri) {
		String key = uri.getLastPathSegment() + "_s";
		return key;
	}

	public static class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
		ContentResolver mResolver;
		LruCache<String, Bitmap> mMemCache;
		ImageView mImg;
		String mKey;

		public BitmapWorkerTask(ContentResolver resolver,
				LruCache<String, Bitmap> cache, ImageView img) {
			mResolver = resolver;
			mMemCache = cache;
			mImg = img;
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(Uri... params) {
			Uri uri = params[0];
			try {
				final Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromUri(
						mResolver, uri, PIC_SIZE, PIC_SIZE);
				mKey = generateKeyFromUri(uri);
				mMemCache.put(mKey, bitmap);
				return bitmap;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				if (mImg != null && mImg.getTag().equals(mKey)) {
					mImg.setImageBitmap(bitmap);
				}
			}
		}

	}
}
