package com.android.mydm.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.R;
import com.android.mydm.fragment.DisplayDMFragment.MyNote;
import com.android.mydm.method.CreateNote;
import com.android.mydm.method.CreateNote.NoteParams;
import com.android.mydm.method.EditNote;
import com.android.mydm.util.EvernoteUtil;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends IntentService {
	public static final String ACTION_CREATE_NOTE = "com.android.text.ACTION_CREATE_NOTE";
	public static final String ACTION_EDIT_NOTE = "com.android.text.ACTION_EDIT_NOTE";
	public static final String ACTION_SYNC_TO_SERVER = "com.android.text.ACTION_SYNC_TO_SERVER";
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
				MyNote note = intent.getParcelableExtra("note");
				NoteParams params = getNoteParams(note, intent.getData());
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
				Log.d(LOG_TAG, "success");
				return;
			} else if (ACTION_EDIT_NOTE.equals(intent.getAction())) {
				MyNote note = intent.getParcelableExtra("note");
				editNote(session, note, intent.getData());
				Toast.makeText(getBaseContext(),
						getString(R.string.edit_success), Toast.LENGTH_LONG)
						.show();
				return;
			} else if (ACTION_SYNC_TO_SERVER.equals(intent.getAction())) {
				ArrayList<MyNote> notes = intent
						.getParcelableArrayListExtra("notes");
				for (MyNote note : notes) {
					editNote(session, note, null);
				}
				Toast.makeText(getBaseContext(),
						getString(R.string.edit_success), Toast.LENGTH_LONG)
						.show();
				return;
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
		Toast.makeText(getBaseContext(), getString(R.string.update_fail),
				Toast.LENGTH_LONG).show();
	}

	private void editNote(EvernoteSession session, MyNote note, Uri data)
			throws TTransportException, IOException, EDAMUserException,
			EDAMSystemException, EDAMNotFoundException, TException {
		NoteParams params = getNoteParams(note, data);
		EditNote editNote = new EditNote(session);
		editNote.execute(params);
		if (data != null) {
			File file = new File(data.getPath());
			if (file.exists()) {
				file.delete();
			}
		}
	}

	private void showNotification() {
		final Resources res = getResources();
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(android.R.drawable.stat_notify_sync)
				.setAutoCancel(true)
				.setTicker(getString(R.string.update_to_server))
				.setContentIntent(
						getDialogPendingIntent("Tapped the notification entry."));
	}

	public static final String ACTION_DIALOG = "action_dialog";

	PendingIntent getDialogPendingIntent(String dialogText) {
		return PendingIntent.getActivity(
				this,
				dialogText.hashCode(), // Otherwise previous PendingIntents with
										// the same
										// requestCode may be overwritten.
				new Intent(ACTION_DIALOG).putExtra(Intent.EXTRA_TEXT,
						dialogText).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
	}

	public NoteParams getNoteParams(MyNote note, Uri data) {
		NoteParams params = new NoteParams();
		params.noteId = note.noteId;
		params.title = note.title;
		params.description = note.description;
		params.checked = note.checked;
		params.notebookId = note.notebookId;
		Log.d(LOG_TAG, "notebookId " + note.notebookId);
		File file = null;
		if (data != null) {
			file = new File(data.getPath());
			Log.d(LOG_TAG, "file exists " + file.exists());
			params.f = file.getAbsolutePath();
			if (params.f.toLowerCase().endsWith("jpg")) {
				params.mimeType = "image/jpeg";
			} else {
				params.mimeType = "image/png";
			}
		}
		return params;
	}
}
