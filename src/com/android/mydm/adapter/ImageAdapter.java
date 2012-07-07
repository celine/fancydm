package com.android.mydm.adapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.mydm.CacheManager;
import com.android.mydm.R;
import com.android.mydm.util.BitmapUtils;
import com.android.mydm.util.BitmapWorkerTask;

public class ImageAdapter extends BaseAdapter {
	CacheManager cacheManager;
	ArrayList<Uri> mUris = new ArrayList<Uri>();
	LayoutInflater mInflater;
	ContentResolver mResolver;
	private static int PIC_SIZE = 150;

	public ImageAdapter(Context context, CacheManager cache) {
		PIC_SIZE = context.getResources().getDimensionPixelSize(
				R.dimen.preview_size);
		cacheManager = cache;
		mInflater = LayoutInflater.from(context);
		mResolver = context.getContentResolver();
	}

	@Override
	public int getCount() {
		return mUris.size() + 1;
	}

	@Override
	public Uri getItem(int position) {
		return mUris.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static final String LOG_TAG = "ImageAdapter";

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.image, parent, false);
		}
		ImageView img = (ImageView) convertView;
		if (position == getCount() - 1) {
			img.setImageResource(R.drawable.ic_input_add);
		} else {
			Uri uri = getItem(position);
			String key = generateKeyFromUri(uri);
			Bitmap bitmap = cacheManager.get(key);
			img.setTag(key);
			if (bitmap != null) {
				img.setImageBitmap(bitmap);
			} else {
				img.setImageResource(R.drawable.preview);
				new ImageBitmapWorkerTask(mResolver, cacheManager, img).execute(
						uri.toString(), generateKeyFromUri(uri));
			}
		}
		return convertView;
	}

	public static class ImageBitmapWorkerTask extends BitmapWorkerTask {
		ContentResolver mResolver;

		public ImageBitmapWorkerTask(ContentResolver resolver,
				CacheManager cache, ImageView img) {
			super(cache, img);
			mResolver = resolver;
		}

		@Override
		protected Bitmap decodeBitmap(String uri) throws FileNotFoundException {
			// TODO Auto-generated method stub
			return BitmapUtils.decodeSampledBitmapFromUri(mResolver,
					Uri.parse(uri), PIC_SIZE, PIC_SIZE);
		}
	}

	public void updateData(ArrayList<Uri> data) {
		mUris = data;
		notifyDataSetChanged();
	}

	private static String generateKeyFromUri(Uri uri) {
		String key = uri.getLastPathSegment() + "_s";
		return key;
	}

}
