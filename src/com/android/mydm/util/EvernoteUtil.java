package com.android.mydm.util;

import android.content.Context;

import com.android.mydm.config.Config;
import com.evernote.client.conn.ApplicationInfo;
import com.evernote.client.oauth.android.EvernoteSession;

public class EvernoteUtil implements Config {
	public static EvernoteSession getSession(Context context) {
		ApplicationInfo info = new ApplicationInfo(CONSUMER_KEY,
				CONSUMER_SECRET, EVERNOTE_HOST, APP_NAME, APP_VERSION);
		return EvernoteSession.getFromPreferences(info,
				context.getSharedPreferences("session", Context.MODE_PRIVATE),
				context.getCacheDir());
	}
}
