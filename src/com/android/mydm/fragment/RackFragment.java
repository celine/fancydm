package com.android.mydm.fragment;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.CacheManager;
import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.R.dimen;
import com.android.mydm.R.id;
import com.android.mydm.R.layout;
import com.android.mydm.R.menu;
import com.android.mydm.util.BitmapUtils;
import com.android.mydm.util.BitmapWorkerTask;
import com.android.mydm.view.RackView;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RackFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<List<Notebook>> {
	RackView mRackView;
	EvernoteSession mSession;
	NoteBookAdapter mAdapter;
	CacheManager cacheManager;
	private static final String LOG_TAG = "RackFragment";
	private int mMode;
	private static final int MODE_VIEW = 0;
	private static final int MODE_SELECT = 1;
	Bitmap evernoteLogo;

	public void updateMode(int mode) {
		ActionBar actionBAr = getActivity().getActionBar();
		switch (mode) {
		case MODE_VIEW:
			actionBAr.setTitle(R.string.app_name);
			break;
		case MODE_SELECT:
			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Uri dataUri = getActivity().getIntent().getData();
		mMode = dataUri != null ? MODE_SELECT : MODE_VIEW;
		updateMode(mMode);
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		setHasOptionsMenu(true);
		mSession = app.getSession();
		cacheManager = app.getCacheManager();
		mAdapter = new NoteBookAdapter(getActivity(), cacheManager);
		mRackView.setAdapter(mAdapter);
		mRackView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				Notebook notebook = (Notebook) adapter
						.getItemAtPosition(position);
				DisplayDMFragment fragment = DisplayDMFragment.newInstance(
						notebook.getGuid(), notebook.getName());

				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.add(R.id.panel1, fragment);
				ft.addToBackStack(null);
				ft.commit();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.notebook_holder, container,
				false);
		mRackView = (RackView) layout.findViewById(R.id.rack);
		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mSession.completeAuthentication(getActivity())
				|| !mSession.isLoggedIn()) {
			mSession.authenticate(getActivity());
			// show dialog
		} else {
			Loader<List<Notebook>> loader = getLoaderManager().getLoader(0);
			Log.d(LOG_TAG, "onResume " + loader);
			if (loader == null) {
				getLoaderManager().initLoader(0, null, this);
			}

		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.photo, menu);
		Log.d(LOG_TAG, "onCreateOptionsMenu");
	}

	public static class NotebookFragmentLoader extends
			AsyncTaskLoader<List<Notebook>> {
		EvernoteSession mSession;
		List<Notebook> mNotebooks;

		public NotebookFragmentLoader(Context context, EvernoteSession session) {
			super(context);
			mSession = session;
		}

		@Override
		protected void onStartLoading() {
			if (mNotebooks != null) {
				deliverResult(mNotebooks);
			}
			if (takeContentChanged() || mNotebooks == null) {
				forceLoad();
			}
		}

		private void releaseResources(List<Notebook> notebooks) {
			notebooks.clear();
		}

		@Override
		public void deliverResult(List<Notebook> notebooks) {
			if (isReset()) {
				if (notebooks != null) {
					releaseResources(notebooks);
				}
			}
			List<Notebook> oldNotebooks = mNotebooks;
			if (isStarted()) {
				mNotebooks = notebooks;
				super.deliverResult(mNotebooks);

			}
			if (oldNotebooks != null && !oldNotebooks.equals(notebooks)) {
				releaseResources(oldNotebooks);
			}
			super.deliverResult(notebooks);
		}

		@Override
		public List<Notebook> loadInBackground() {
			try {
				List<Notebook> notebooks = mSession.createNoteStore()
						.listNotebooks(mSession.getAuthToken());
				return notebooks;
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMUserException e) {
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

	}

	public static class NoteBookAdapter extends BaseAdapter {
		LayoutInflater mInflater;
		public List<Notebook> mNotebooks;
		CacheManager memCache;
		int coverWidth;
		int coverHeight;
		Resources mRes;

		public NoteBookAdapter(Context context, CacheManager cache) {
			mInflater = LayoutInflater.from(context);
			memCache = cache;
			coverWidth = context.getResources().getDimensionPixelSize(
					R.dimen.book_width);
			coverHeight = context.getResources().getDimensionPixelSize(
					R.dimen.book_height);
			mRes = context.getResources();
		}

		public void updateData(List<Notebook> notebooks) {
			mNotebooks = notebooks;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mNotebooks == null) {
				return 0;
			}
			return mNotebooks.size();
		}

		@Override
		public Notebook getItem(int position) {
			return mNotebooks.get(position);
		}

		@Override
		public long getItemId(int id) {
			return id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dmbook, parent, false);
			}
			Notebook notebook = getItem(position);
			ImageView img = (ImageView) convertView.findViewById(R.id.img);
			TextView text = (TextView) convertView.findViewById(R.id.title);
			text.setText(notebook.getName());
			String key = notebook.getGuid() + "_cover";
			img.setTag(key);
			Bitmap bitmap = memCache.get(key);
			if (bitmap != null) {
				img.setImageBitmap(bitmap);
			} else {
				new CoverBitmapWorkerTask(mRes, memCache, img, coverWidth,
						coverHeight, notebook.getGuid() == null ? null
								: notebook.getName()).execute(notebook
						.getName());
			}

			return convertView;
		}

	}

	@Override
	public Loader<List<Notebook>> onCreateLoader(int id, Bundle args) {
		return new NotebookFragmentLoader(getActivity(), mSession);
	}

	@Override
	public void onLoadFinished(Loader<List<Notebook>> loader,
			List<Notebook> notebooks) {
		Log.d(LOG_TAG, "data " + notebooks.size());
		Notebook notebook = new Notebook();
		notebook.setName(getString(R.string.create_notebook));
		notebooks.add(notebook);
		
		mAdapter.updateData(notebooks);
	}

	@Override
	public void onLoaderReset(Loader<List<Notebook>> arg0) {
		// TODO Auto-generated method stub

	}

	public static class CoverBitmapWorkerTask extends BitmapWorkerTask {
		int mWidth;
		int mHeight;
		Resources mRes;
		String mTitle;
		String colors[] = new String[] { "#ADD8E6", "#41A317", "#FFF380" };
		public static Random mRandom = new Random();

		public CoverBitmapWorkerTask(Resources res,
				CacheManager cache, ImageView img, int width,
				int height, String title) {
			super(cache, img);
			mWidth = width;
			mHeight = height;
			mRes = res;
			mTitle = title;
		}

		@Override
		protected Bitmap decodeBitmap(String uri) throws FileNotFoundException {
			int color = Color.WHITE;
			if (mTitle != null) {
				color = Color
						.parseColor(colors[mRandom.nextInt(colors.length)]);
			}

			return BitmapUtils.decodeDMCover(color, mTitle, mWidth, mHeight);

		}
	}
}
