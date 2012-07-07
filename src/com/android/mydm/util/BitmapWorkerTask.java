package com.android.mydm.util;

import java.io.FileNotFoundException;

import com.android.mydm.CacheManager;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public abstract class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

	CacheManager cacheManager;
	ImageView mImg;
	String mKey;

	public BitmapWorkerTask(CacheManager cache, ImageView img) {
		cacheManager = cache;
		mImg = img;
		mKey = img.getTag().toString();
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		try {
			final Bitmap bitmap = decodeBitmap(params[0]);
			cacheManager.put(mKey, bitmap);
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	protected abstract Bitmap decodeBitmap(String uri)
			throws FileNotFoundException;

	private static final String LOG_TAG = "BitmapWorkerTask";

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap != null) {
			if (mImg != null && mImg.getTag().equals(mKey)) {
				Log.d(LOG_TAG, "setImageBitmap");
				mImg.setImageBitmap(bitmap);
			}
		}
	}

}