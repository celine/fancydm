package com.android.mydm.method;

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

public class DeleteNote extends Method {
	public static class DeleteNoteParams extends Params {
		public String noteId;
	}

	public DeleteNote(EvernoteSession session) {
		super(session);
	}

	@Override
	public Integer execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException {
		DeleteNoteParams dParams = (DeleteNoteParams) params;
		return mSession.createNoteStore().deleteNote(mSession.getAuthToken(),
				dParams.noteId);

	}

}
