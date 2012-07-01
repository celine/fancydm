package com.android.mydm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.mydm.fragment.DMGalleryFragment;
import com.android.mydm.fragment.DisplayDMFragment.MyNote;
import com.android.mydm.util.BitmapUtils;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;

public class SwitchCheckList extends FragmentActivity {
	private static final String LOG_TAG = "EvernoteViewCheckList";
	private String mUrl = null;
	static Pattern urlPattern = Pattern
			.compile("^https://sandbox.evernote.com/shard/(\\w+)/sh/(\\S+)/(.*)");
	
	MyNote mNote = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.switch_dm);

		Log.d("AAAA", "shared");
	}

	String mNotebookId;
	EvernoteSession mSession;
	LruCache memCache;

	@Override
	protected void onResume() {
		super.onResume();
		CheckListApplication app = (CheckListApplication) getApplication();
		mSession = app.getSession();
		memCache = app.getMemCache();

		if (!mSession.completeAuthentication(this) || !mSession.isLoggedIn()) {
			mSession.authenticate(this);
			// show dialog
		} else {
			// List<MyNote> notes = this.getIntent().getExtras()
			// .getParcelableArrayList("notes");
			mUrl = this.getIntent().getData().toString();
			GetShareNoteTask task = new GetShareNoteTask(mSession, memCache,
					this.getResources(), new NoteListener() {

						@Override
						public void onLoaded(MyNote note) {
							if(note != null) {
								ArrayList<MyNote> mNotes = new ArrayList<MyNote>();
								mNotes.add(note);
								
								mNote = note;
								
								FragmentTransaction ft = getSupportFragmentManager()
										.beginTransaction();
								DMGalleryFragment fragment = new DMGalleryFragment();
								Bundle args = new Bundle();
								args.putParcelableArrayList("notes", mNotes);
								args.putInt("position", 0);
								args.putBoolean("share_note", true);
								fragment.setArguments(args);
								ft.add(R.id.DMGalleryContainer, fragment);
								ft.addToBackStack(null);
								ft.commit();
							}
						}

					});

			task.execute(mUrl);

		}
	}

	static interface NoteListener {
		void onLoaded(MyNote note);
	}

	static class GetShareNoteTask extends AsyncTask<String, Void, MyNote> {

		EvernoteSession mSession = null;
		Client noteStore = null;
		NoteListener mListener;
		LruCache memCache;
		
		int margin;
		int dm_width;
		int dm_height;

		GetShareNoteTask(EvernoteSession session, LruCache memCache, Resources res,
				NoteListener listener) {
			this.mSession = session;
			this.mListener = listener;
			this.memCache = memCache;
			
			margin = res.getDimensionPixelSize(R.dimen.dm_margin);
			dm_width = res.getDimensionPixelSize(R.dimen.dm_width);
			dm_height = res.getDimensionPixelSize(R.dimen.dm_height);
		}

		@Override
		protected MyNote doInBackground(String... urls) {
			try {
				noteStore = mSession.createNoteStore();
				String url = urls[0];
				Matcher matcher = urlPattern.matcher(url);
				if (matcher.find()) {
					String guuid = matcher.group(2);
					String noteKey = matcher.group(3);

					com.evernote.edam.userstore.AuthenticationResult shareResult = noteStore
							.authenticateToSharedNote(guuid, noteKey);

					Note note = noteStore.getNote(
							shareResult.getAuthenticationToken(), guuid, true,
							true, true, true);

					MyNote myNote = new MyNote();

					ArrayList<String> resIds = new ArrayList<String>();
					List<Resource> resources = note.getResources();
					if (resources != null) {
						for (Resource res : resources) {
							resIds.add(res.getGuid());
						}
					}

					myNote.content = note.getContent();
					myNote.noteId = note.getGuid();
					myNote.resIds = resIds;
					myNote.title = note.getTitle();

					if (myNote.resIds.size() > 0) {
						String resId = myNote.resIds.get(0);
						Resource res = mSession.createNoteStore().getResource(
								shareResult.getAuthenticationToken(), resId, true, false,
								false, false);
						Data data = res.getData();
						Bitmap bitmap = BitmapFactory.decodeByteArray(
								data.getBody(), 0, data.getSize());
						Bitmap mBitmap = BitmapUtils.resizeAndCrop(bitmap,
								dm_width - 2 * margin, dm_height);
						String size = dm_width - 2 * margin + "x" + dm_height;
						myNote.small_thumb = resId + "_" + size;

						memCache.put(myNote.small_thumb, mBitmap);
					}

					return myNote;
				}
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMUserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(MyNote result) {
			mListener.onLoaded(result);
		}

	}
}
