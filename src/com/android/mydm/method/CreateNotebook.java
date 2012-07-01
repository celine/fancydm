package com.android.mydm.method;

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.method.Method.Params;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Notebook;

public class CreateNotebook extends Method {

	public static class CreateNotebookParams extends Params {
		public String notebookName;
	}

	public CreateNotebook(EvernoteSession session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Notebook execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException {
		CreateNotebookParams cParams = (CreateNotebookParams) params;
		Notebook notebook = new Notebook();
		notebook.setName(cParams.notebookName);
		
		return mSession.createNoteStore().createNotebook(mSession.getAuthToken(),
				notebook);
	}

}
