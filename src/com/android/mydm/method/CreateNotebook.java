package com.android.mydm.method;

import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

public class CreateNotebook extends Method {

	public CreateNotebook(EvernoteSession session) {
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
