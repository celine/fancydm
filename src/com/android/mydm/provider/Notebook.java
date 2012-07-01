package com.android.mydm.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Notebook implements BaseColumns {
	public static final String TITLE = "title";
	public static final String RESOURCE_ID = "resource_id";
	public static final String CHECKED = "checked";
	public static final String TAGS = "tags";

	public static final String DEFAULT_SORT_ORDER = TITLE + " asc";
	public static final String TABLE = "books";
	public static final Uri CONTENT_URI = Uri.parse("content://notebooks/"
			+ TABLE);
}
