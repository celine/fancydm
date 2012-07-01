package com.android.mydm;

import com.android.mydm.config.Config;
import com.android.mydm.util.BitmapUtils;
import com.android.mydm.util.EvernoteUtil;
import com.evernote.client.conn.ApplicationInfo;
import com.evernote.client.oauth.android.EvernoteSession;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CheckListApplication extends Application implements Config {
	LruCache<String, Bitmap> mMemCache;
	EvernoteSession mSession;

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
		mSession = EvernoteUtil.getSession(getBaseContext());
	}

	public EvernoteSession getSession() {
		return mSession;
	}

	public LruCache<String, Bitmap> getMemCache() {
		return mMemCache;
	}

}
