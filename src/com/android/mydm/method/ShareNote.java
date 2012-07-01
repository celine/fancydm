package com.android.mydm.method;

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.util.Log;

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

public class ShareNote extends Method {
	public static final String SHARD_ID = "sh1";
	private static final String LOG_TAG = "ShareNote";
	public static class ShareNoteParams extends Params {
		public String guid;
	}

	public ShareNote(EvernoteSession session) {
		super(session);
	}

	@Override
	public String execute(Params params) throws IOException, TTransportException,
			EDAMUserException, EDAMSystemException, EDAMNotFoundException,
			TException {
		ShareNoteParams sparams = (ShareNoteParams) params;
		String shareKey = mSession.createNoteStore().shareNote(
				mSession.getAuthToken(), sparams.guid);
		Log.d(LOG_TAG,"shareKey " + shareKey);
		StringBuilder builder = new StringBuilder(mSession.getWebApiUrlPrefix());
		builder.append("sh/").append(sparams.guid).append("/").append(shareKey);
		return builder.toString();
	}

}
