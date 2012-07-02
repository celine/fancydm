package com.android.mydm.method;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import android.util.Log;

import com.android.mydm.method.CreateNote.NoteParams;
import com.evernote.client.conn.mobile.FileData;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.LazyMap;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import com.evernote.edam.util.EDAMUtil;

public class EditNote extends Method {

	public EditNote(EvernoteSession session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Note execute(Params params) throws IOException, TTransportException,
			EDAMUserException, EDAMSystemException, EDAMNotFoundException,
			TException {
		Resource resource = new Resource();
		NoteParams cp = (NoteParams) params;

		// Create a new Note
		Note note = new Note();
		note.setGuid(cp.noteId);
		note.setTitle(cp.title);

		NoteAttributes attrs = new NoteAttributes();
		attrs.setContentClass(CONTENT_CLASS);
		LazyMap lmap = new LazyMap();
		lmap.putToFullMap("checked", cp.checked ? "1" : "0");
		lmap.putToFullMap("description", cp.description);
		attrs.setApplicationData(lmap);
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

		String content = NOTE_PREFIX + "<h1>" + cp.title + "</h1>"
				+ resource_data + "<p>" + cp.description + "</p>" + todo
				+ NOTE_SUFFIX;
		Log.d("Evernote", "content " + content);
		note.setContent(content);
		return mSession.createNoteStore().updateNote(mSession.getAuthToken(),
				note);
	}

}
