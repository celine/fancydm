package com.android.mydm.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.mydm.CacheManager;
import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.R.drawable;
import com.android.mydm.R.id;
import com.android.mydm.R.layout;
import com.android.mydm.R.string;
import com.android.mydm.fragment.DMGalleryFragment.getContentTask;
import com.android.mydm.fragment.DisplayDMFragment.MyNote;
import com.android.mydm.remote.BackgroundService;
import com.android.mydm.util.BitmapUtils;
import com.evernote.client.oauth.android.EvernoteSession;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class CreateNoteFragment extends Fragment {
	ImageView mImg;
	Uri mUri;
	String bitmapKey;
	private static final String LOG_TAG = "CreateNoteFragment";
	EditText mTitle;
	EditText mDescription;
	MyNote mNote;
	CacheManager cacheManager;
	EvernoteSession mSession;

	public CreateNoteFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onViewCreated");
		View view = inflater.inflate(R.layout.create_note_view, container,
				false);
		mTitle = (EditText) view.findViewById(R.id.title);
		mDescription = (EditText) view.findViewById(R.id.description);
		mImg = (ImageView) view.findViewById(R.id.img);
		return view;
	}

	private static final int ACTIVITY_PICK = 1;
	private static final int ACTIVITY_CAPTURE = 2;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		Uri uri;
		switch (requestCode) {
		case ACTIVITY_CAPTURE:
			Log.d(LOG_TAG, "action " + data.getDataString());
			BitmapFactory.Options options = new BitmapFactory.Options();

			options.inJustDecodeBounds = false;
			uri = data.getData();
			setImage(uri);
			break;
		case ACTIVITY_PICK:
			Uri selectedImage = data.getData();
			String[] queryColumns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE,
					MediaStore.Images.Media.DISPLAY_NAME };
			Cursor cursor = getActivity().getContentResolver().query(
					selectedImage, queryColumns, null, null, null);
			cursor.moveToFirst();
			String filePath = cursor.getString(cursor
					.getColumnIndex(queryColumns[0]));
			File file = new File(filePath);
			String mimeType = cursor.getString(cursor
					.getColumnIndex(queryColumns[1]));
			String fileName = cursor.getString(cursor
					.getColumnIndex(queryColumns[2]));
			cursor.close();
			uri = Uri.fromFile(file);
			setImage(uri);
			break;
		}
	}

	public void launchGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(intent, ACTIVITY_PICK);
	}

	public void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, ACTIVITY_CAPTURE);
	}

	boolean modified = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onActivityCreate");
		CheckListApplication application = (CheckListApplication) getActivity()
				.getApplication();
		cacheManager = application.getCacheManager();
		mSession = application.getSession();
		this.setHasOptionsMenu(true);
		getActivity().getActionBar().setTitle(R.string.create_dm);
		mNote = getArguments().getParcelable("note");
		if (mNote != null) {
			modified = true;
			mTitle.setText(mNote.title);
			mDescription.setText(mNote.description);
			if (mNote.resIds != null && mNote.resIds.size() > 0) {
				String resId = mNote.resIds.get(0);
				Bitmap bitmap = cacheManager.get(resId);
				if (bitmap != null) {
					mImg.setImageBitmap(bitmap);
				} else {
					mImg.setTag(mNote.noteId);
					new getContentTask(getActivity(), mSession, cacheManager,
							mImg, mNote).execute();
				}
			}
		} else {
			mNote = new MyNote();
			modified = false;
			mNote.notebookId = getArguments().getString("notebookId");
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.create_note, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(LOG_TAG, "select " + item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			return true;
		case R.id.camera:

			launchCamera();
			return true;
		case R.id.photo:
			launchGallery();
			return true;
		case R.id.save:
			saveNote();
			return true;
		}
		return false;
	}

	private void setEmptyImage() {
		mImg.setImageResource(R.drawable.ic_missing_thumbnail_picture);

	}

	public void saveNote() {

		Intent intent = new Intent(
				modified ? BackgroundService.ACTION_EDIT_NOTE
						: BackgroundService.ACTION_CREATE_NOTE);
		intent.setClass(getActivity(), BackgroundService.class);
		mNote.title = mTitle.getText().toString();
		mNote.description = mDescription.getText().toString();
		intent.putExtra("note", mNote);
		String tag = getActivity().getIntent().getStringExtra("tag");
		if (tag == null) {
		}
		intent.putExtra("tag", tag);
		if (bitmapKey != null) {
			Bitmap bitmap = cacheManager.get(bitmapKey);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
			File file = new File(getActivity().getCacheDir(), bitmapKey
					+ ".jpg");

			try {
				file.createNewFile();
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(bytes.toByteArray());
				intent.setData(Uri.fromFile(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getActivity().startService(intent);
		Toast.makeText(
				getActivity(),
				getActivity().getString(
						modified ? R.string.update_dm : R.string.create_dm),
				Toast.LENGTH_SHORT).show();
		setEmptyImage();
		getActivity().onBackPressed();

	}

	public static CreateNoteFragment newInstance(Uri uri) {
		CreateNoteFragment fragment = new CreateNoteFragment();
		Bundle args = new Bundle();
		args.putParcelable("uri", uri);
		fragment.setArguments(args);
		return fragment;
	}

	public void setImage(Uri uri) {
		if (uri == null) {
			return;
		}
		mUri = uri;
		String key = uri.getLastPathSegment() + "_o";
		bitmapKey = key;
		try {
			Bitmap bitmap = cacheManager.get(key);
			if (bitmap == null) {
				bitmap = BitmapUtils
						.decodeSampledBitmapFromUriWithMaxEdgeAndRatio(
								getActivity().getContentResolver(), uri, 480,
								0.75f);
				cacheManager.put(key, bitmap);
			}
			mImg.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class EditDescriptionDialog extends DialogFragment implements
			OnEditorActionListener {

		private EditText mEditText;

		public EditDescriptionDialog() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.edit_dm, container);
			mEditText = (EditText) view.findViewById(R.id.content);
			getDialog().setTitle(R.string.edit_title);
			mEditText.requestFocus();
			getDialog().getWindow().setSoftInputMode(
					LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			mEditText.setOnEditorActionListener(this);

			return view;
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (EditorInfo.IME_ACTION_DONE == actionId) {
				// Return input text to activity
				this.dismiss();
				return true;
			}
			return false;
		}

	}

	public static CreateNoteFragment newInstance(String id) {
		CreateNoteFragment fragment = new CreateNoteFragment();
		Bundle args = new Bundle();
		args.putString("notebookId", id);
		fragment.setArguments(args);
		return fragment;
	}

	public static CreateNoteFragment newInstance(MyNote note) {
		CreateNoteFragment fragment = new CreateNoteFragment();
		Bundle args = new Bundle();
		args.putString("notebookId", note.notebookId);
		args.putParcelable("note", note);

		fragment.setArguments(args);
		return fragment;
	}
}
