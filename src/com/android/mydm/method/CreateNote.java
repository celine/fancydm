package com.android.mydm.method;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.util.Log;

import com.evernote.client.conn.mobile.FileData;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import com.evernote.edam.util.EDAMUtil;

public class CreateNote extends Method {

	public static class CreateNoteParams extends Params {
		public String title;
		public String description;
		public String mimeType;
		public String f;
		public List<String> tagNames;
		public String notebookId;
	}

	public CreateNote(EvernoteSession session) {
		super(session);
	}

	public Object execute(Params params) throws IOException,
			TTransportException, EDAMUserException, EDAMSystemException,
			EDAMNotFoundException, TException {
		// Create a new Resource
		Resource resource = new Resource();
		CreateNoteParams cp = (CreateNoteParams) params;

		// Create a new Note
		Note note = new Note();
		note.setTitle(cp.title);
		Log.d("Notebook Create", "notebookId " + cp.notebookId);
		note.setNotebookGuid(cp.notebookId);
		NoteAttributes attrs = new NoteAttributes();
		attrs.setContentClass(CONTENT_CLASS);
		note.setAttributes(attrs);
		InputStream in;
		String resource_data = "";
		if (cp.f != null) {
			in = new BufferedInputStream(new FileInputStream(cp.f));
			FileData data = new FileData(EDAMUtil.hash(in), new File(cp.f));
			in.close();
			resource.setData(data);
			resource.setMime(cp.mimeType);
			resource_data = "<en-media type=\"" + cp.mimeType + "\" hash=\""
					+ EDAMUtil.bytesToHex(resource.getData().getBodyHash())
					+ "\"/>";

			note.addToResources(resource);
		}
		// note.setTagNames(cp.tagNames);
		String todo = "<en-todo/>Item completed<br/>";
		// Set the note's ENML content. Learn about ENML at
		// http://dev.evernote.com/documentation/cloud/chapters/ENML.php

		String content = NOTE_PREFIX + "<h1>" + cp.title + "</h1><p>"
				+ cp.description + "</p>" + resource_data + todo + NOTE_SUFFIX;
		Log.d("Evernote", "content " + content);
		note.setContent(content);

		// Create the note on the server. The returned Note object
		// will contain server-generated attributes such as the note's
		// unique ID (GUID), the Resource's GUID, and the creation and
		// update time.
		Note createdNote = mSession.createNoteStore().createNote(
				mSession.getAuthToken(), note);
		return createdNote;

	}
}
