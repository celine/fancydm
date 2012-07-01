package com.android.mydm.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.R.dimen;
import com.android.mydm.R.id;
import com.android.mydm.R.layout;
import com.android.mydm.R.menu;
import com.android.mydm.SwitchCheckList.NoteListLoader;
import com.android.mydm.method.FindNote;
import com.android.mydm.method.FindNote.FindNoteParams;
import com.android.mydm.method.ShareNote;
import com.android.mydm.method.ShareNote.ShareNoteParams;
import com.android.mydm.util.BitmapUtils;
import com.android.mydm.view.DMLayout;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;

public class DisplayDMFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<List<Note>> {
	String mNotebookId;
	EvernoteSession mSession;
	LruCache<String, Bitmap> memCache;
	DMLayout mLayout;
	private static final String LOG_TAG = "DMWebFragment";
	View mEmptyView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onViewCreated");
		View layout = inflater.inflate(R.layout.page, container, false);
		mLayout = (DMLayout) layout.findViewById(R.id.dm_layout);
		mEmptyView = layout.findViewById(R.id.empty);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Bundle args = getArguments();
		Log.d("AAAA", "ngid " + args.getString("ngid"));
		if (args != null) {
			mNotebookId = args.getString("ngid");
		}
		Activity activity = getActivity();
		activity.getActionBar().setTitle(args.getString("title"));
		activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);

		setHasOptionsMenu(true);
		CheckListApplication app = (CheckListApplication) activity
				.getApplication();
		mSession = app.getSession();
		memCache = app.getMemCache();
		getLoaderManager().initLoader(0, null, this);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(LOG_TAG, "item id " + item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			break;
		}
		return true;
	}

	@Override
	public Loader<List<Note>> onCreateLoader(int id, Bundle arg1) {
		mEmptyView.setVisibility(View.VISIBLE);
		return new NoteListLoader(getActivity(), mSession, mNotebookId);
	}

	@Override
	public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
		mLayout.removeAllViews();
		mEmptyView.setVisibility(View.GONE);
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		EvernoteSession session = app.getSession();
		ArrayList<MyNote> myNotes = new ArrayList<MyNote>();
		for (int i = 0; i < data.size(); i++) {
			Note note = data.get(i);

			MyNote mnote = new MyNote();
			mnote.content = note.getContent();
			ArrayList<String> resIds = new ArrayList<String>();
			for (Resource res : note.getResources()) {
				resIds.add(res.getGuid());
			}
			mnote.noteId = note.getGuid();
			mnote.resIds = resIds;
			mnote.title = note.getTitle();
			myNotes.add(mnote);
		}
		for (int i = 0; i < data.size(); i++) {
			new GetResourceTask(getActivity(), session, memCache, mLayout,
					myNotes).execute(i);
		}

		Log.d(LOG_TAG, "data size " + data.size());
	}

	public static class MyNote implements Parcelable {
		public String noteId;
		boolean checked = false;
		public String title;
		public ArrayList<String> resIds = new ArrayList<String>();
		public String content;
		public String small_thumb;

		public MyNote() {
		}

		public MyNote(Parcel in) {
			checked = in.readInt() == 1 ? true : false;
			title = in.readString();
			in.readStringList(resIds);
			content = in.readString();
			small_thumb = in.readString();
			noteId = in.readString();
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(checked ? 1 : 0);
			dest.writeString(title);
			dest.writeStringList(resIds);
			dest.writeString(content);
			dest.writeString(small_thumb);
			dest.writeString(noteId);
		}

		public static final Parcelable.Creator<MyNote> CREATOR = new Parcelable.Creator<MyNote>() {
			public MyNote createFromParcel(Parcel in) {
				return new MyNote(in);
			}

			public MyNote[] newArray(int size) {
				return new MyNote[size];
			}
		};
	}

	public static class GetResourceTask extends
			AsyncTask<Integer, MyNote, Integer> {
		FragmentActivity mActivity;
		DMLayout mLayout;
		EvernoteSession mSession;
		LruCache<String, Bitmap> memCache;
		ArrayList<MyNote> mNotes;
		Bitmap mBitmap;
		int margin;
		int dm_width;
		int dm_height;
		int title_height;
		int rowWidth;
		int per_row = 1;

		public GetResourceTask(FragmentActivity fragmentActivity,
				EvernoteSession session, LruCache<String, Bitmap> cache,
				DMLayout layout, ArrayList<MyNote> myNotes) {
			mActivity = fragmentActivity;
			mLayout = layout;
			mSession = session;
			memCache = cache;
			mNotes = myNotes;
			Resources res = fragmentActivity.getResources();
			margin = res.getDimensionPixelSize(R.dimen.dm_margin);
			dm_width = res.getDimensionPixelSize(R.dimen.dm_width);
			dm_height = res.getDimensionPixelSize(R.dimen.dm_height);
			title_height = res.getDimensionPixelSize(R.dimen.title_height);
			View parent = (View) mLayout.getParent();
			rowWidth = parent.getWidth();
			per_row = rowWidth / dm_width;
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			int position = params[0];
			MyNote note = mNotes.get(position);
			String resId = note.resIds.get(0);
			Resource res;
			try {
				res = mSession.createNoteStore().getResource(
						mSession.getAuthToken(), resId, true, false, false,
						false);
				Data data = res.getData();
				Bitmap bitmap = BitmapFactory.decodeByteArray(data.getBody(),
						0, data.getSize());
				mBitmap = BitmapUtils.resizeAndCrop(bitmap, dm_width - 2
						* margin, dm_height);
				String size = dm_width - 2 * margin + "x" + dm_height;
				note.small_thumb = resId + "_" + size;
				memCache.put(note.small_thumb, mBitmap);
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
			return position;
		}

		@Override
		protected void onPostExecute(Integer position) {
			MyNote note = mNotes.get(position);
			View item = LayoutInflater.from(mActivity).inflate(
					R.layout.action_dm, null, false);
			int width = (rowWidth - (2 + per_row - 1) * margin) / per_row;
			int height = Math.round((float) dm_width / width * dm_height);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,
					height + title_height);
			item.setLayoutParams(lp);
			TextView text = (TextView) item.findViewById(R.id.title);
			ImageView img = (ImageView) item.findViewById(R.id.img);
			text.setText(note.title);
			img.setImageBitmap(mBitmap);
			View detail = item.findViewById(R.id.more_detail);
			detail.setTag(position);
			detail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentTransaction ft = mActivity
							.getSupportFragmentManager().beginTransaction();
					DMGalleryFragment fragment = new DMGalleryFragment();
					Bundle args = new Bundle();
					args.putParcelableArrayList("notes", mNotes);
					args.putInt("position", (Integer) v.getTag());
					fragment.setArguments(args);
					ft.add(R.id.rack, fragment);
					ft.addToBackStack(null);
					ft.commit();
				}
			});
			mLayout.addChildView(item, dm_width);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Note>> arg0) {
		mLayout.removeAllViews();
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
			try {
				FindNoteParams fParams = new FindNoteParams();
				fParams.notebookId = notebookId;
				FindNote fNote = new FindNote(mSession);
				return fNote.execute(fParams);
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
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

	public static DisplayDMFragment newInstance(String guid, String title) {
		DisplayDMFragment fragment = new DisplayDMFragment();
		Bundle args = new Bundle();
		args.putString("ngid", guid);
		args.putString("title", title);
		fragment.setArguments(args);
		return fragment;
	}
}
