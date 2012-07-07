package com.android.mydm;

import com.android.mydm.util.BitmapUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class CacheManager {
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	LruCache<String, Bitmap> mMemCache;
	DiskLruCache mDiskCache;

	public CacheManager(Context context) {
		mDiskCache = DiskLruCache.openCache(context, context.getCacheDir(),
				DISK_CACHE_SIZE);
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
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

	public void put(String key, Bitmap bitmap) {
		mMemCache.put(key, bitmap);
		mDiskCache.put(key, bitmap);

	}

	public Bitmap get(String key) {
		Bitmap bitmap = mMemCache.get(key);
		Log.d("AAAA", "get " + key + " " + (bitmap != null));
		if (bitmap == null || bitmap.isRecycled()) {
			bitmap = mDiskCache.get(key);
			if (bitmap != null) {
				mMemCache.put(key, bitmap);
			}
		}
		return bitmap;
	}

	public void evictAll() {
		mMemCache.evictAll();
		mDiskCache.clearCache();

	}
}
