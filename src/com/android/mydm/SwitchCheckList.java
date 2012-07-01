package com.android.mydm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.fragment.DisplayDMFragment.MyNote;
import com.evernote.client.conn.ApplicationInfo;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SwitchCheckList extends FragmentActivity {
	DMPageAdapter mAdapter;
	private static final String LOG_TAG = "EvernoteViewCheckList";
	ViewPager viewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.switch_dm);
		viewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new DMPageAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mAdapter);
	}

	String mNotebookId;
	EvernoteSession mSession;

	@Override
	protected void onResume() {
		super.onResume();
		CheckListApplication app = (CheckListApplication) getApplication();
		mSession = app.getSession();
		if (!mSession.completeAuthentication(this) || !mSession.isLoggedIn()) {
			mSession.authenticate(this);
			// show dialog
		} else {
			List<MyNote> notes = this.getIntent().getExtras()
					.getParcelableArrayList("notes");
			mAdapter.updateData(notes);
			int currentItem = getIntent().getExtras().getInt("position");
			viewPager.setCurrentItem(currentItem);
			// Log.d(LOG_TAG, "session logged in " + mSession.isLoggedIn());
			// Loader loader = getSupportLoaderManager().getLoader(0);
			// Bundle args = getIntent().getExtras();
			// if (args != null) {
			// mNotebookId = args.getString("ngid");
			// }
			// if (loader == null) {
			// getSupportLoaderManager().initLoader(0, null, this);
			// } else {
			// loader.onContentChanged();
			// }
		}
	}

	// @Override
	// public Loader<List<Note>> onCreateLoader(int id, Bundle arg1) {
	// return new NoteListLoader(this, mSession, mNotebookId);
	// }
	//
	// @Override
	// public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
	// mAdapter.updateData(data);
	// Log.d(LOG_TAG, "data size " + data.size());
	// }
	//
	// @Override
	// public void onLoaderReset(Loader<List<Note>> arg0) {
	// mAdapter.updateData(null);
	//
	// }

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

	public static class DMPageAdapter extends FragmentStatePagerAdapter {
		private List<MyNote> mNotes;

		public DMPageAdapter(FragmentManager fm) {
			super(fm);
		}

		public void updateData(List<MyNote> notes) {
			this.mNotes = notes;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mNotes == null) {
				return 0;
			}
			return mNotes.size();
		}

		@Override
		public Fragment getItem(int page) {
			return NoteFragment.newInstance(mNotes.get(page));
		}
	}

	public static class NoteFragment extends Fragment implements
			LoaderManager.LoaderCallbacks<Bitmap> {

		public static NoteFragment newInstance(MyNote note) {
			NoteFragment fragment = new NoteFragment();
			Bundle args = new Bundle();
			args.putStringArrayList("resIds", note.resIds);
			args.putString("title", note.title);
			fragment.setArguments(args);
			return fragment;
		}

		String resId;
		EvernoteSession mSession;
		LruCache<String, Bitmap> memCache;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			CheckListApplication app = (CheckListApplication) getActivity()
					.getApplication();
			memCache = app.getMemCache();
			mSession = app.getSession();
			Bundle args = getArguments();
			List<String> resIds = args.getStringArrayList("resIds");
			resId = resIds.get(0);
			getLoaderManager().initLoader(0, null, this);

		}

		ImageView mImage;
		TextView mTitle;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.dm_detail, container, false);
			mImage = (ImageView) view.findViewById(R.id.img);
			mTitle = (TextView) view.findViewById(R.id.title);
			return view;
		}

		@Override
		public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
			return new GetNoteResourceLoader(getActivity(), mSession, memCache,
					resId);
		}

		@Override
		public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
			Bundle args = getArguments();
			mTitle.setText(args.getString("title"));
			mImage.setImageBitmap(bitmap);
			mTitle.getParent().requestLayout();

		}

		@Override
		public void onLoaderReset(Loader<Bitmap> loader) {
			mImage.setImageResource(R.drawable.ic_gallery_empty2);

		}
	}

	private static String parseDescriptoin(String content) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class GetNoteResourceLoader extends AsyncTaskLoader<Bitmap> {
		String resId;
		EvernoteSession mSession;
		LruCache<String, Bitmap> memCache;

		public GetNoteResourceLoader(Context context, EvernoteSession session,
				LruCache<String, Bitmap> cache, String resId) {
			super(context);
			mSession = session;
			memCache = cache;
			this.resId = resId;
		}

		@Override
		protected void onStartLoading() {

			Bitmap bitmap = memCache.get(resId);
			if (bitmap != null) {
				deliverResult(bitmap);
			}
			if (takeContentChanged() || bitmap == null) {
				forceLoad();
			}
		}

		public static Resource getResource(EvernoteSession session, String resId)
				throws TTransportException, EDAMUserException,
				EDAMSystemException, EDAMNotFoundException, TException {
			Resource res = session.createNoteStore().getResource(
					session.getAuthToken(), resId, true, false, false, false);
			return res;
		}

		@Override
		public Bitmap loadInBackground() {
			try {
				Resource res = getResource(mSession, resId);
				Data data = res.getData();
				Bitmap bitmap = BitmapFactory.decodeByteArray(data.getBody(),
						0, data.getSize());
				memCache.put(resId, bitmap);
				return bitmap;
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

	}
}
