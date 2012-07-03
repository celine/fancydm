package com.android.mydm.method;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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
		String content = cp.content;
		String resource_data = "";
		Map<String, String> map = parseContent(content);
		String hash = map.get("hash");
		cp.mimeType = map.get("mimeType");
		if (cp.f != null) {
			in = new BufferedInputStream(new FileInputStream(cp.f));
			FileData data = new FileData(EDAMUtil.hash(in), new File(cp.f));
			in.close();
			resource.setData(data);
			resource.setMime(cp.mimeType);
			hash = EDAMUtil.bytesToHex(resource.getData().getBodyHash());
			
			note.addToResources(resource);
		}
		if (hash != null) {
			Log.d("Evernote", "mimeType " + cp.mimeType);
			resource_data = "<en-media type=\"" + cp.mimeType + "\" hash=\""
					+ hash + "\"/>";
		}
		// note.setTagNames(cp.tagNames);
		String todo = "<en-todo checked=\"" + (cp.checked ? "true" : "false")
				+ "\"/>This item is completed<br/>";
		// Set the note's ENML content. Learn about ENML at
		// http://dev.evernote.com/documentation/cloud/chapters/ENML.php

		content = NOTE_PREFIX + "<h1>" + cp.title + "</h1>" + resource_data
				+ "<p>" + cp.description + "</p>" + todo + NOTE_SUFFIX;
		Log.d("Evernote", "content " + content);
		note.setContent(content);
		return mSession.createNoteStore().updateNote(mSession.getAuthToken(),
				note);
	}

	public Map<String, String> parseContent(String content) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput(new StringReader(content));
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					System.out.println("Start document");
				} else if (eventType == XmlPullParser.START_TAG) {
					String tag = xpp.getName();
					if (tag.equals("en-media")) {
						int count = xpp.getAttributeCount();
						for (int i = 0; i < count; i++) {
							String name = xpp.getAttributeName(i);
							if ("type".equals(name)) {
								Log.d("Evernote","find mimeType " + xpp.getAttributeValue(i));
								map.put("mimeType", xpp.getAttributeValue(i));
							} else if ("hash".equals(name)) {
								map.put("hash", xpp.getAttributeValue(i));
							}
						}
					} else if ("en-todo".equals(xpp.getName())) {
						String checked = xpp.getAttributeValue(0);
						map.put("checked", checked);
					}

				} else if (eventType == XmlPullParser.END_TAG) {

				} else if (eventType == XmlPullParser.TEXT) {

				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

}
