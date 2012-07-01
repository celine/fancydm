package com.android.mydm.config;

public interface Config {
	String CONSUMER_KEY = "wenchih-5780";
	String CONSUMER_SECRET = "3f209fd6be47308f";
	String EVERNOTE_HOST = "sandbox.evernote.com";

	String APP_NAME = "Evernote Android Sample";
	String APP_VERSION = "1.0";
	String NOTE_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
			+ "<en-note>";

	String NOTE_SUFFIX = "</en-note>";
	String CONTENT_CLASS = "evernote.dm.view";
}
