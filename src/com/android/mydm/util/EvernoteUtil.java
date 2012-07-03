package com.android.mydm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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

	private static final String AUTH_TOKEN = "auth_token";
	private static final String NOTE_STORE_URL = "note_store_url";
	private static final String WEB_API_URL_PREFIX = "web_api_url_prefix";
	private static final String USER_ID = "user_id";

	public static void logout(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("session",
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove(AUTH_TOKEN);
		editor.remove(NOTE_STORE_URL);
		editor.remove(WEB_API_URL_PREFIX);
		editor.remove(USER_ID);
		editor.commit();
	}
}
