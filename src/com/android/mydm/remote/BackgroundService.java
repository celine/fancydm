package com.android.mydm.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.R;
import com.android.mydm.method.CreateNote;
import com.android.mydm.method.CreateNote.NoteParams;
import com.android.mydm.method.EditNote;
import com.android.mydm.util.EvernoteUtil;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends IntentService {
	public static final String ACTION_CREATE_NOTE = "com.android.text.ACTION_CREATE_NOTE";
	public static final String ACTION_EDIT_NOTE = "com.android.text.ACTION_EDIT_NOTE";
	private static final String LOG_TAG = "EvernoteBackgroundService";

	public BackgroundService() {
		super("EvernoteBackgroundService");
	}

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "onCreate");
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		EvernoteSession session = EvernoteUtil.getSession(getBaseContext());
		Log.d(LOG_TAG, "action " + intent.getAction());
		if (session == null) {
			return;
		}
		try {
			if (ACTION_CREATE_NOTE.equals(intent.getAction())) {
				CreateNote create = new CreateNote(session);
				NoteParams params = getNoteParams(intent);
				create.execute(params);
				if (intent.getData() != null) {
					File file = new File(intent.getData().getPath());
					if (file.exists()) {
						file.delete();
					}
				}
				Toast.makeText(getBaseContext(),
						getString(R.string.create_success), Toast.LENGTH_LONG)
						.show();
			} else if (ACTION_EDIT_NOTE.equals(intent.getAction())) {
				NoteParams params = getNoteParams(intent);
				EditNote editNote = new EditNote(session);
				editNote.execute(params);
				if (intent.getData() != null) {
					File file = new File(intent.getData().getPath());
					if (file.exists()) {
						file.delete();
					}
				}
				Toast.makeText(getBaseContext(),
						getString(R.string.edit_success), Toast.LENGTH_LONG)
						.show();
			}
		} catch (TTransportException e) {
			Log.e(LOG_TAG, "error", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "error", e);
		} catch (EDAMUserException e) {
			Log.e(LOG_TAG, "error", e);
		} catch (EDAMSystemException e) {
			Log.e(LOG_TAG, "error", e);
		} catch (EDAMNotFoundException e) {
			Log.e(LOG_TAG, "error", e);
		} catch (TException e) {
			Log.e(LOG_TAG, "error", e);
		}
	}

	public NoteParams getNoteParams(Intent intent) {
		Bundle extras = intent.getExtras();
		NoteParams params = new NoteParams();
		params.noteId = extras.getString("noteId");
		params.title = extras.getString("title");
		params.description = extras.getString("description");
		params.mimeType = extras.getString("mimeType");
		params.notebookId = extras.getString("notebookId");
		File file = null;
		if (intent.getData() != null) {
			file = new File(intent.getData().getPath());
			Log.d(LOG_TAG, "file exists " + file.exists());
			params.f = file.getAbsolutePath();
			if (params.f.toLowerCase().endsWith("jpg")) {
				params.mimeType = "image/jpeg";
			} else {
				params.mimeType = "image/png";
			}
		}
		String tag = intent.getStringExtra("tag");
		params.tagNames = new ArrayList<String>();
		params.tagNames.add(tag);
		return params;
	}
}
