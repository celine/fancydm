package com.android.text.capture.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

public class BitmapUtils {
	private static final String LOG_TAG = "ImageUtils";

	public static int[] calculateTargetSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		float factor = Math.max((float) reqWidth / options.outWidth,
				(float) reqHeight / options.outHeight);
		return new int[] { (int) (options.outWidth * factor),
				(int) (options.outHeight * factor) };
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static Bitmap resizeAndCrop(Bitmap source, int newWidth,
			int newHeight) {
		Matrix matrix = new Matrix();
		int originalWidth = source.getWidth();
		int originalHeight = source.getHeight();
		float scale = Math.max((float) newWidth / originalWidth,
				(float) newHeight / originalHeight);
		matrix.setScale(scale, scale);
		// RectF rect = new RectF(0, 0, originalWidth, originalHeight);
		// matrix.mapRect(rect);
		Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight,
				source.getConfig());
		Canvas canvas = new Canvas();
		canvas.concat(matrix);
		canvas.setBitmap(bitmap);
		bitmap.setDensity(source.getDensity());
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		if (!matrix.rectStaysRect()) {
			paint.setAntiAlias(true);
		}

		int tmpWidth = Math.round(newWidth / scale);
		int tmpHeight = Math.round(newHeight / scale);
		int marginLeft = (originalWidth - tmpWidth) / 2;
		int marginTop = (originalHeight - tmpHeight) / 2;
		Rect srcBound = new Rect(marginLeft, marginTop, originalWidth
				- marginLeft, originalHeight - marginTop);
		Log.d(LOG_TAG, "marginLeft " + marginLeft + " marginTop " + marginTop);
		RectF targetBound = new RectF(0, 0, tmpWidth, tmpHeight);
		canvas.drawBitmap(source, srcBound, targetBound, paint);
		source.recycle();
		return bitmap;
	}

	public static Bitmap cropCenter(Bitmap source, int targetWidth,
			int targetHeight) {
		int marginLeft = (source.getWidth() - targetWidth) / 2;
		int marginTop = (source.getHeight() - targetHeight) / 2;
		Bitmap newBitmap = Bitmap.createBitmap(source, marginLeft, marginTop,
				targetWidth, targetHeight);
		return newBitmap;
	}

	public static Bitmap decodeSampledBitmapFromUriWithMaxWidth(
			ContentResolver resolver, Uri uri, int maxWidth)
			throws FileNotFoundException {
		BitmapFactory.Options options = getBitmapSize(resolver, uri);

		float ratio = (float) maxWidth / options.outWidth;
		return decodeSampledBitmap(resolver, uri, options,
				Math.round(options.outWidth * ratio),
				Math.round(options.outHeight * ratio));
	}

	public static Bitmap decodeSampledBitmapFromUri(ContentResolver resolver,
			Uri uri, int targetWidth, int targetHeight)
			throws FileNotFoundException {
		return decodeSampledBitmap(resolver, uri, getBitmapSize(resolver, uri),
				targetWidth, targetHeight);
	}

	public static BitmapFactory.Options getBitmapSize(ContentResolver resolver,
			Uri uri) throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		InputStream input = resolver.openInputStream(uri);
		BitmapFactory.decodeStream(input, null, options);

		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return options;
	}

	public static Bitmap decodeSampledBitmap(ContentResolver resolver, Uri uri,
			BitmapFactory.Options options, int targetWidth, int targetHeight)
			throws FileNotFoundException {
		options.inSampleSize = BitmapUtils.calculateInSampleSize(options,
				targetWidth, targetHeight);
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		InputStream input = resolver.openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "bitmap " + bitmap);
		bitmap = resizeAndCrop(bitmap, targetWidth, targetHeight);
		return bitmap;
	}

	public static final int getBitmapByteCount(Bitmap bitmap) {
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
}
