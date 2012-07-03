package com.android.mydm.fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.CheckListApplication;
import com.android.mydm.R;
import com.android.mydm.method.CreateNotebook;
import com.android.mydm.method.CreateNotebook.CreateNotebookParams;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Notebook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ListNotebookFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<Notebook>> {

	EvernoteSession mSession;
	NoteBookAdapter mAdapter;
	private static final String LOG_TAG = "ListNotebookFragment";

	private OnNotebookSelectedListener listener = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getActivity().getActionBar().setTitle(R.string.app_name);
		super.onActivityCreated(savedInstanceState);
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		mSession = app.getSession();
		mAdapter = new NoteBookAdapter(getActivity());
		setListAdapter(mAdapter);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView");
		return inflater.inflate(R.layout.listview, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Notebook notebook = (Notebook) l.getItemAtPosition(position);

		if (listener != null) {
			listener.onNotebookSelected(notebook);
		} else {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (notebook.getGuid() != null) {
				DisplayDMFragment fragment = DisplayDMFragment.newInstance(
						notebook.getGuid(), notebook.getName());
				ft.add(R.id.panel1, fragment);

				ft.addToBackStack(null);
				ft.commit();
			} else {
				Fragment prev = getFragmentManager()
						.findFragmentByTag("dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				DialogFragment f = CreateNotebookDialog
						.newInstance(getString(R.string.create_notebook));
				f.show(getFragmentManager(), "dialog");

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");
		if (!mSession.completeAuthentication(getActivity())
				|| !mSession.isLoggedIn()) {
			mSession.authenticate(getActivity());
			// show dialog
		} else {
			Loader<List<Notebook>> loader = getLoaderManager().getLoader(0);
			Log.d(LOG_TAG, "onResume " + loader);
			if (loader == null) {
				getLoaderManager().initLoader(0, null, this);
			} else {
				loader.onContentChanged();
			}

		}
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

		public NoteBookAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
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
				convertView = mInflater.inflate(R.layout.notebook, parent,
						false);
			}
			Notebook notebook = getItem(position);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			text.setText(notebook.getName());
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			icon.setVisibility(notebook.getGuid() != null ? View.GONE
					: View.VISIBLE);
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
		if (notebooks == null) {
			return;
		}
		Log.d(LOG_TAG, "data " + notebooks.size());

		Bundle args = this.getArguments();
		boolean canCreate = true;
		if (args != null) {
			canCreate = args.getBoolean("can_create", true);
		}
		if (canCreate) {
			Notebook notebook = new Notebook();
			notebook.setName(getString(R.string.create_notebook));
			notebooks.add(0, notebook);
		}

		mAdapter.updateData(notebooks);
	}

	@Override
	public void onLoaderReset(Loader<List<Notebook>> arg0) {
		// TODO Auto-generated method stub

	}

	public void setOnNotebookSelectedListener(
			OnNotebookSelectedListener listener) {
		this.listener = listener;
	}

	public static class CreateNotebookDialog extends DialogFragment implements
			OnEditorActionListener {
		private EditText mEditText;

		EvernoteSession mSession;

		public static CreateNotebookDialog newInstance(String title) {
			CreateNotebookDialog dialog = new CreateNotebookDialog();
			Bundle args = new Bundle();
			args.putString("title", title);
			dialog.setArguments(args);
			return dialog;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mSession = ((CheckListApplication) getActivity().getApplication())
					.getSession();
			String title = getArguments().getString("title");
			View view = inflater.inflate(R.layout.edit_dm, container);
			mEditText = (EditText) view.findViewById(R.id.content);
			getDialog().setTitle(title);
			mEditText.requestFocus();
			getDialog().getWindow().setSoftInputMode(
					LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			mEditText.setOnEditorActionListener(this);
			Button done = (Button) view.findViewById(R.id.done);

			done.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					createNotebook();
				}
			});
			return view;
		}

		private void createNotebook() {
			new CreaeteNotebookTask(this, mSession).execute(mEditText.getText()
					.toString());

		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (EditorInfo.IME_ACTION_DONE == actionId) {
				createNotebook();
				return true;
			}
			return false;
		}

	}

	private static class CreaeteNotebookTask extends
			AsyncTask<String, Void, Notebook> {
		CreateNotebookDialog mContext;
		ProgressDialog dialog;
		EvernoteSession mSession;

		public CreaeteNotebookTask(CreateNotebookDialog context,
				EvernoteSession session) {
			mContext = context;
			mSession = session;
		}

		@Override
		protected void onPostExecute(Notebook result) {
			dialog.dismiss();
			mContext.dismiss();
			ListNotebookFragment lFragment = (ListNotebookFragment) mContext
					.getFragmentManager().findFragmentByTag("notebooks");
			Log.d(LOG_TAG, " lFragment " + lFragment);
			lFragment.onDialogDismiss(true);
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(mContext.getActivity(), "Progress",
					mContext.getString(R.string.progress));
		}

		@Override
		protected Notebook doInBackground(String... params) {
			CreateNotebook create = new CreateNotebook(mSession);
			CreateNotebookParams cParams = new CreateNotebookParams();
			cParams.notebookName = params[0];
			try {
				return create.execute(cParams);
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

	public static interface OnNotebookSelectedListener {
		public void onNotebookSelected(Notebook notebook);
	}

	public void onDialogDismiss(boolean update) {
		if (update) {
			Loader loader = getLoaderManager().getLoader(0);
			loader.onContentChanged();
		}
		Log.d(LOG_TAG, "onDismiss");
	}

}
