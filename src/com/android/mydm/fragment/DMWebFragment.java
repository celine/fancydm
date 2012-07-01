package com.android.mydm.fragment;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.android.mydm.CheckListApplication;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;

public class DMWebFragment extends WebFragment implements
		LoaderManager.LoaderCallbacks<List<Note>> {
	String mNotebookId;
	EvernoteSession mSession;
	private static final String LOG_TAG = "DMWebFragment";

	@Override
	public void onResume() {
		super.onResume();
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		mSession = app.getSession();
		if (!mSession.completeAuthentication(getActivity())
				|| !mSession.isLoggedIn()) {
			mSession.authenticate(getActivity());
			// show dialog
		} else {
			Log.d(LOG_TAG, "session logged in " + mSession.isLoggedIn());
			Loader loader = getLoaderManager().getLoader(0);
			Bundle args = getArguments();
			Log.d("AAAA", "ngid " + args.getString("ngid"));
			if (args != null) {
				mNotebookId = args.getString("ngid");
			}
			if (loader == null) {
				getLoaderManager().initLoader(0, null, this);
			} else {
				loader.onContentChanged();
			}
		}
	}

	@Override
	public Loader<List<Note>> onCreateLoader(int id, Bundle arg1) {
		return new NoteListLoader(getActivity(), mSession, mNotebookId);
	}

	@Override
	public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
		for (int i = 0; i < data.size(); i++) {
			Note note = data.get(i);
			Log.d("WebView", "javascript: addBox('" + "res:"
					+ note.getResources().get(0).getGuid() + "', 'imgsq" + 2
					+ "'," + note.getTitle() + ")");

			mWebView.loadUrl("javascript: addBox('" + "res:"
					+ note.getResources().get(0).getGuid() + "', '1','" + note.getTitle() + "')");
		}

		Log.d(LOG_TAG, "data size " + data.size());
	}

	@Override
	public void onLoaderReset(Loader<List<Note>> arg0) {
		mWebView.loadUrl("javascript: unload()");

	}

	public static class NoteListLoader extends AsyncTaskLoader<List<Note>> {
		String notebookId;
		EvernoteSession mSession;

		public NoteListLoader(Context context, EvernoteSession session,
				String id) {
			super(context);
			mSession = session;
			notebookId = id;
		}

		List<Note> mNotes;

		@Override
		protected void onStartLoading() {
			Log.d("AAAA", "start load");
			if (mNotes != null) {
				deliverResult(mNotes);
			}
			if (takeContentChanged() || mNotes == null) {
				forceLoad();
			}
		}

		@Override
		public List<Note> loadInBackground() {
			NoteFilter fileter = new NoteFilter();
			Log.d("AAAA", "load in background " + notebookId);
			fileter.setNotebookGuid(notebookId);
			try {
				NoteList notes = mSession.createNoteStore().findNotes(
						mSession.getAuthToken(), fileter, 0, 20);
				return notes.getNotes();
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMUserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		private void releaseResources(List<Note> notes) {
			notes.clear();
		}

		@Override
		public void deliverResult(List<Note> notes) {
			if (isReset()) {
				if (notes != null) {
					releaseResources(notes);
				}
			}
			List<Note> oldNotes = mNotes;
			if (isStarted()) {
				mNotes = notes;
				super.deliverResult(mNotes);

			}
			if (oldNotes != null && !oldNotes.equals(notes)) {
				releaseResources(oldNotes);
			}
			super.deliverResult(notes);
		}

	}
}
