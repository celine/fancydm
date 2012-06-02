package com.android.text.capture;

import com.android.text.capture.util.BitmapUtils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CheckListApplication extends Application {
	LruCache<String, Bitmap> mMemCache;

	@Override
	public void onCreate() {
		super.onCreate();
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		int memClass = am.getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 8;
		mMemCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in bytes rather than number
				// of items.
				return BitmapUtils.getBitmapByteCount(bitmap);
			}
		};
	}

	public LruCache<String, Bitmap> getMemCache() {
		return mMemCache;
	}

}
