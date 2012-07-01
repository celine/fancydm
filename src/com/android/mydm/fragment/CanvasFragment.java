package com.android.mydm.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.R.drawable;
import com.android.mydm.R.id;
import com.android.mydm.R.layout;
import com.android.mydm.R.string;
import com.android.mydm.remote.BackgroundService;
import com.android.mydm.util.BitmapUtils;
import com.evernote.client.oauth.android.EvernoteSession;

import android.app.Activity;
import android.content.Intent;
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

public class CanvasFragment extends Fragment {
	ImageView mImg;
	Uri mUri;
	ImageButton editButton;
	ImageButton saveButton;
	String bitmapKey;
	private static final String LOG_TAG = "CanvasFragment";

	public CanvasFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Uri uri = getArguments().getParcelable("uri");
		if (uri != null) {
			setImage(uri);
		} else {
			setEmptyImage();
			editButton.setVisibility(View.GONE);
			saveButton.setVisibility(View.GONE);
		}
		super.onActivityCreated(savedInstanceState);
	}

	private void setEmptyImage() {
		mImg.setImageResource(R.drawable.ic_gallery_empty2);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.edit, container, false);
		saveButton = (ImageButton) view.findViewById(R.id.save);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CheckListApplication application = (CheckListApplication) getActivity()
						.getApplication();
				EvernoteSession session = application.getSession();

				if (!session.completeAuthentication(getActivity())
						|| !session.isLoggedIn()) {
					session.authenticate(getActivity());
					// show dialog
				} else {
					Intent intent = new Intent(
							BackgroundService.ACTION_CREATE_NOTE);
					intent.setClass(getActivity(), BackgroundService.class);

					intent.putExtra("title", "test title");
					String tag = getActivity().getIntent()
							.getStringExtra("tag");
					if (tag == null) {
						tag = "dog";
					}
					intent.putExtra("tag", tag);
					Log.d(LOG_TAG, "start Evernote Service");
					LruCache<String, Bitmap> memCache = application
							.getMemCache();
					Bitmap bitmap = memCache.get(bitmapKey);
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
					getActivity().startService(intent);
					Toast.makeText(getActivity(),
							getActivity().getString(R.string.create_dm),
							Toast.LENGTH_LONG).show();
					setEmptyImage();
				}

			}
		});
		mImg = (ImageView) view.findViewById(R.id.img);
		editButton = (ImageButton) view.findViewById(R.id.edit);
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				FragmentManager fm = getFragmentManager();
				EditDescriptionDialog dialog = new EditDescriptionDialog();
				dialog.show(fm, "add description");
			}
		});
		return view;
	}

	public static CanvasFragment newInstance(Uri uri) {
		CanvasFragment fragment = new CanvasFragment();
		Bundle args = new Bundle();
		args.putParcelable("uri", uri);
		fragment.setArguments(args);
		return fragment;
	}

	public void setImage(Uri uri) {
		mUri = uri;
		editButton.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);
		CheckListApplication application = (CheckListApplication) getActivity()
				.getApplication();
		LruCache<String, Bitmap> memCache = application.getMemCache();
		String key = uri.getLastPathSegment() + "_o";
		bitmapKey = key;
		try {
			Bitmap bitmap = memCache.get(key);
			if (bitmap == null) {
				bitmap = BitmapUtils
						.decodeSampledBitmapFromUriWithMaxEdgeAndRatio(
								getActivity().getContentResolver(), uri, 480,
								0.75f);
				memCache.put(key, bitmap);
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
}
