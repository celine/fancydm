package com.android.mydm.method;

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

public class SaveNotebook extends Method {
	public static class SaveNotebookParams extends Params {

	}

	public SaveNotebook(EvernoteSession session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException {
		// TODO Auto-generated method stub
		return null;
	}

}
