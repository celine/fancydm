package com.android.mydm.method;

import java.io.IOException;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.util.Log;

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;

public class FindNote extends Method {
	public static class FindNoteParams extends Params {
		public String notebookId;

	}

	public FindNote(EvernoteSession session) {
		super(session);
	}

	@Override
	public List<Note> execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException {

		FindNoteParams fParams = (FindNoteParams) params;
		NoteFilter fileter = new NoteFilter();
		Log.d("AAAA", "load in background " + fParams.notebookId);
		fileter.setNotebookGuid(fParams.notebookId);
		fileter.setWords("contentClass:" + CONTENT_CLASS);
		NoteList notes = mSession.createNoteStore().findNotes(
				mSession.getAuthToken(), fileter, 0, 20);
		return notes.getNotes();
	}

}
