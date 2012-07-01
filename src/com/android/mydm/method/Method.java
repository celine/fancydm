package com.android.mydm.method;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.app.Activity;

import com.android.mydm.config.Config;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;

public abstract class Method implements Config {
	EvernoteSession mSession;

	public Method(EvernoteSession session) {
		mSession = session;
	}

	public abstract static class Params {

	}

	public abstract Object execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException;
}
