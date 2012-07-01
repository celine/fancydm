/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mydm.provider;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.net.Uri;
import android.text.TextUtils;
import android.app.SearchManager;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NoteBooksProvider extends ContentProvider {
	private static final String LOG_TAG = "NoteBooksProvider";

	private static final String DATABASE_NAME = "notebooks.db";
	private static final int DATABASE_VERSION = 1;

	private static final int SEARCH = 1;
	private static final int BOOKS = 2;
	private static final int BOOK_ID = 3;

	private static final String AUTHORITY = "notebooks";

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH);
		URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY
				+ "/*", SEARCH);
		URI_MATCHER.addURI(AUTHORITY, Notebook.TABLE, BOOKS);
		URI_MATCHER.addURI(AUTHORITY, Notebook.TABLE + "/#", BOOK_ID);
	}

	private static final HashMap<String, String> SUGGESTION_PROJECTION_MAP;
	static {
		SUGGESTION_PROJECTION_MAP = new HashMap<String, String>();
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
				Notebook.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2,
				Notebook.TAGS + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
		SUGGESTION_PROJECTION_MAP.put(
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, Notebook._ID
						+ " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		SUGGESTION_PROJECTION_MAP.put(Notebook._ID, Notebook._ID);
	}

	private SQLiteOpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (URI_MATCHER.match(uri)) {
		case SEARCH:
			qb.setTables("books");
			String query = uri.getLastPathSegment();
			if (!TextUtils.isEmpty(query)) {
				qb.appendWhere(Notebook.TITLE + " LIKE ");
				qb.appendWhereEscapeString('%' + query + '%');
				qb.appendWhere(" OR ");
				qb.appendWhere(Notebook.TAGS + " LIKE ");
				qb.appendWhereEscapeString('%' + query + '%');
			}
			qb.setProjectionMap(SUGGESTION_PROJECTION_MAP);
			break;
		case BOOKS:
			qb.setTables("books");
			break;
		case BOOK_ID:
			qb.setTables("books");
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Notebook.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case BOOKS:
			return "vnd.android.cursor.dir/vnd.org.curiouscreature.provider.shelves";
		case BOOK_ID:
			return "vnd.android.cursor.item/vnd.org.curiouscreature.provider.shelves";
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;

		if (initialValues != null) {
			values = initialValues;
		} else {
			values = new ContentValues();
		}

		if (URI_MATCHER.match(uri) != BOOKS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert(Notebook.TABLE, Notebook.TITLE, values);
		if (rowId > 0) {
			Uri insertUri = ContentUris.withAppendedId(Notebook.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;
		switch (URI_MATCHER.match(uri)) {
		case BOOKS:
			count = db.delete("books", selection, selectionArgs);
			break;
		case BOOK_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete("books", Notebook._ID
					+ "="
					+ segment
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE books (" + Notebook._ID
					+ " INTEGER PRIMARY KEY, " + Notebook.TITLE + " TEXT, "
					+ Notebook.RESOURCE_ID + " TEXT, " + Notebook.TAGS
					+ " TEXT, " + Notebook.CHECKED + " SHORT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOG_TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS books");
			onCreate(db);
		}
	}
}
