package com.android.mydm.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.fragment.DisplayDMFragment.MyNote;
import com.android.mydm.method.DeleteNote;
import com.android.mydm.method.DeleteNote.DeleteNoteParams;
import com.android.mydm.method.ShareNote;
import com.android.mydm.method.ShareNote.ShareNoteParams;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;

public class DMGalleryFragment extends Fragment {
	DMPageAdapter mAdapter;
	private static final String LOG_TAG = "DMGalleryFragment";
	Gallery mGallery;
	ShareActionProvider mShareActionProvider;
	public SparseArray<String> mUrlArray;

	private boolean mShareNote = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mGallery = (Gallery) inflater.inflate(R.layout.gallery, container,
				false);
		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				getActivity().invalidateOptionsMenu();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		return mGallery;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.photo, menu);
		MenuItem item = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		Log.d(LOG_TAG, "onCreateOptionsMenu");

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(LOG_TAG, "onPrepareOptionsMenu");
		MenuItem item = menu.findItem(R.id.action_share);
		if (item == null) {
			return;
		}
		int currentItem = mGallery.getSelectedItemPosition();
		if (mUrlArray != null) {
			String url = mUrlArray.get(currentItem);
			Log.d(LOG_TAG, "url " + url);
			if (url != null) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, url);
				mShareActionProvider.setShareIntent(intent);
				item.setVisible(true);
			} else {
				item.setVisible(false);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(LOG_TAG, "item id " + item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			return true;
		case R.id.action_confirm_delete:
			new DeleteNoteTask(mSession, getActivity(), mGallery).execute();
			break;
		case R.id.action_edit:
			MyNote note = (MyNote) mGallery.getSelectedItem();
			Fragment fragment = CreateNoteFragment.newInstance(note);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.panel1, fragment, "edit");
			ft.addToBackStack(null);
			ft.commit();
			return true;
		}
		return false;
	}

	public static class DeleteNoteTask extends AsyncTask<Void, Void, Integer> {
		Context mContext;
		DMPageAdapter mAdapter;
		String noteId;
		EvernoteSession mSession;
		Gallery mGallery;

		public DeleteNoteTask(EvernoteSession session, Context context,
				Gallery gallery) {
			mContext = context;
			MyNote note = (MyNote) gallery.getSelectedItem();
			this.noteId = note.noteId;
			mGallery = gallery;
			mAdapter = (DMPageAdapter) gallery.getAdapter();
			mSession = session;
		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(mContext,
					mContext.getString(R.string.start_delete_note),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			DeleteNoteParams dParams = new DeleteNoteParams();
			dParams.noteId = noteId;
			try {
				return new DeleteNote(mSession).execute(dParams);
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

		@Override
		protected void onPostExecute(Integer result) {
			String message = mContext
					.getString(result != null ? R.string.delete_success
							: R.string.delete_fail);
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			if (result != null) {
				List<MyNote> notes = mAdapter.getData();
				List<MyNote> newNotes = new ArrayList<MyNote>();
				int position = 0;
				for (int i = 0; i < notes.size(); i++) {
					MyNote note = notes.get(i);
					if (!note.noteId.equals(noteId)) {
						newNotes.add(note);
					} else {
						position = i;
					}
				}
				if (position > newNotes.size()) {
					position = newNotes.size() - 1;
				}
				mAdapter.updateData(newNotes);
				if (newNotes.size() > 0) {
					mGallery.setSelection(position);
				}
			}
		}
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		this.setHasOptionsMenu(true);
		mSession = app.getSession();
		memCache = app.getMemCache();
		List<MyNote> notes = getArguments().getParcelableArrayList("notes");
		mUrlArray = new SparseArray<String>(notes.size());

		mShareNote = getArguments().getBoolean("share_note", false);

		mAdapter = new DMPageAdapter(getActivity(), mSession, memCache,
				mUrlArray, mShareNote);
		mGallery.setAdapter(mAdapter);
		mAdapter.updateData(notes);
		int currentItem = getArguments().getInt("position");
		mGallery.setSelection(currentItem);

		super.onActivityCreated(savedInstanceState);
	}

	String mNotebookId;
	EvernoteSession mSession;
	LruCache<String, Bitmap> memCache;

	public static class DMPageAdapter extends BaseAdapter {
		private List<MyNote> mNotes;
		LayoutInflater mInflater;
		Activity mActivity;
		EvernoteSession mSession;
		LruCache<String, Bitmap> memCache;
		SparseArray<String> mUrlArray;
		boolean mShareNote = false;

		public DMPageAdapter(Activity context, EvernoteSession session,
				LruCache<String, Bitmap> cache, SparseArray<String> urlArray,
				boolean shareNote) {
			mActivity = context;
			mInflater = LayoutInflater.from(context);
			mSession = session;
			memCache = cache;
			mUrlArray = urlArray;
			mShareNote = shareNote;
		}

		public void updateData(List<MyNote> notes) {
			this.mNotes = notes;
			this.notifyDataSetChanged();
		}

		public List<MyNote> getData() {
			return this.mNotes;
		}

		@Override
		public int getCount() {
			if (mNotes == null) {
				return 0;
			}
			return mNotes.size();
		}

		@Override
		public MyNote getItem(int page) {
			return mNotes.get(page);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dm_detail, parent,
						false);
			}
			Bitmap bitmap;
			final MyNote note = getItem(position);
			CheckBox check = (CheckBox) convertView.findViewById(R.id.checked);
			check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					note.checked = isChecked;
				}
			});
			check.setChecked(note.checked);
			if (note.resIds != null && note.resIds.size() > 0) {
				convertView.setTag(note.resIds.get(0));
				String resId = note.resIds.get(0);
				bitmap = memCache.get(resId);
			} else {
				bitmap = BitmapFactory.decodeResource(mActivity.getResources(),
						R.drawable.ic_missing_thumbnail_picture);
			}
			TextView mTitle = (TextView) convertView
					.findViewById(R.id.detail_title);
			final View progress = convertView.findViewById(R.id.progress);

			final Button mButton = (Button) convertView
					.findViewById(R.id.create_share_url);
			boolean urlExist = mUrlArray.get(position) != null;
			mButton.setVisibility(urlExist || mShareNote ? View.GONE
					: View.VISIBLE);
			mButton.setTag(position);
			mButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new ShareNoteTask(mActivity, mSession, note.noteId,
							mButton, progress).execute(mUrlArray);
				}
			});
			if (bitmap == null) {
				new GetNoteResourceTask(mActivity, mSession, memCache,
						convertView, note).execute();
			} else {
				ImageView mImage = (ImageView) convertView
						.findViewById(R.id.img);

				mImage.setImageBitmap(bitmap);
			}
			mTitle.setText(note.title);
			return convertView;
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

	public static class ShareNoteTask extends
			AsyncTask<SparseArray<String>, Void, String> {
		String noteGuid;
		EvernoteSession mSession;
		Activity mActivity;
		// MyDialogFragment fragment;
		Button mButton;
		View mProgress;
		int mKey;

		public ShareNoteTask(Activity activity, EvernoteSession session,
				String guid, Button button, View progress) {
			mActivity = activity;
			mSession = session;
			noteGuid = guid;
			mButton = button;
			mKey = (Integer) button.getTag();
			mProgress = progress;
		}

		@Override
		protected void onPreExecute() {
			// fragment = new MyDialogFragment();
			// fragment.show(mActivity.getSupportFragmentManager(), "progress");
			Drawable pdrawable = mActivity.getResources().getDrawable(
					R.drawable.evernotelogo);
			mButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					android.R.drawable.progress_horizontal, 0);
			mProgress.setVisibility(View.VISIBLE);
			mButton.setEnabled(false);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(LOG_TAG, "result " + result);
			mProgress.setVisibility(View.GONE);
			if (result != null) {
				mButton.setVisibility(View.GONE);
				mActivity.invalidateOptionsMenu();

			} else {
				mButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				mButton.setEnabled(true);
			}
		}

		@Override
		protected String doInBackground(SparseArray<String>... params) {
			ShareNoteParams sParams = new ShareNoteParams();
			sParams.guid = noteGuid;
			try {
				String url = new ShareNote(mSession).execute(sParams);
				Log.d(LOG_TAG, "url " + url);
				params[0].put(mKey, url);
				return url;
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

	}

	public static class GetNoteResourceTask extends
			AsyncTask<Void, Void, Bitmap> {
		MyNote mNote;
		EvernoteSession mSession;
		LruCache<String, Bitmap> memCache;
		View mView;

		@Override
		protected void onPreExecute() {
			if (mNote.small_thumb != null) {
				Bitmap bitmap = memCache.get(mNote.small_thumb);
				if (bitmap != null) {
					ImageView mImage = (ImageView) mView.findViewById(R.id.img);
					mImage.setImageBitmap(bitmap);
				}
			}
		}

		public GetNoteResourceTask(Context context, EvernoteSession session,
				LruCache<String, Bitmap> cache, View view, MyNote note) {
			mSession = session;
			memCache = cache;
			mView = view;
			mNote = note;
		}

		public static Resource getResource(EvernoteSession session, String resId)
				throws TTransportException, EDAMUserException,
				EDAMSystemException, EDAMNotFoundException, TException {
			Resource res = session.createNoteStore().getResource(
					session.getAuthToken(), resId, true, false, false, false);
			return res;
		}

		@Override
		public Bitmap doInBackground(Void... params) {
			try {
				String resId = mNote.resIds.get(0);
				Resource res = getResource(mSession, resId);
				Data data = res.getData();
				Bitmap bitmap = BitmapFactory.decodeByteArray(data.getBody(),
						0, data.getSize());
				memCache.put(resId, bitmap);
				String token = mNote.token == null ? mSession.getAuthToken()
						: mNote.token;
				Note note = mSession.createNoteStore().getNote(token,
						mNote.noteId, true, false, false, false);
				mNote.content = note.getContent();
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

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}

			String resId = mNote.resIds.get(0);
			if (resId.equals(mView.getTag())) {
				ImageView mImage = (ImageView) ((mView instanceof ImageView) ? mView
						: mView.findViewById(R.id.img));
				mImage.setImageBitmap(result);
			}

			if (mNote.content != null) {
				TextView mDesc = (TextView) mView
						.findViewById(R.id.detail_description);
				Pattern contentPattern = Pattern.compile("<p>(.*)<\\/p>");
				Matcher m = contentPattern.matcher(mNote.content);

				if (m.find() && mDesc!=null) {
					String desc = m.group(1);
					if(desc == null) {
						desc = "";
					}
					mDesc.setText(desc);
					
				}

			}
		}
	}
}
