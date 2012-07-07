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
	CacheManager mCacheManager;
	EvernoteSession mSession;

	@Override
	public void onCreate() {
		super.onCreate();
		mCacheManager = new CacheManager(this);
		mSession = EvernoteUtil.getSession(getBaseContext());
	}

	public EvernoteSession getSession() {
		return mSession;
	}

	public CacheManager getCacheManager() {
		return mCacheManager;
	}

}
