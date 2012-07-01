package com.android.mydm.fragment;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.android.mydm.CheckListApplication;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Resource;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public abstract class WebFragment extends Fragment {
	private boolean mIsWebViewAvailable;

	WebView mWebView;
	EvernoteSession mSession;

	public WebFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getWebView().getSettings().setJavaScriptEnabled(true);
		getWebView().loadUrl("file:///android_asset/index.html");
		CheckListApplication app = (CheckListApplication) getActivity()
				.getApplication();
		mSession = app.getSession();
		getWebView().setWebViewClient(new WebViewClient() {

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view,
					String url) {
				Log.d("AAAA", "url:" + url);
				if (url.startsWith("res:")) {
					String resId = url.substring(4);

					Resource res;

					try {
						res = mSession.createNoteStore().getResource(
								mSession.getAuthToken(), resId, true, false,
								false, false);
						Data data = res.getData();

						ByteArrayInputStream stream = new ByteArrayInputStream(
								data.getBody());
						WebResourceResponse resp = new WebResourceResponse(
								"image/jpeg", "utf-8", stream);
						return resp;
					} catch (TTransportException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EDAMUserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EDAMSystemException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EDAMNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return super.shouldInterceptRequest(view, url);
			}

		});
		getWebView().setWebChromeClient(new WebChromeClient() {
			public void onConsoleMessage(String message, int lineNumber,
					String sourceID) {
				Log.d("WebView", message + " -- From line " + lineNumber
						+ " of " + sourceID);
			}
		});
	}

	@Override
	public void onDestroy() {
		if (mWebView != null) {
			mWebView.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		mWebView.onResume();
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mWebView != null) {
			mWebView.destroy();
		}
		mWebView = new WebView(getActivity());
		mIsWebViewAvailable = true;
		return mWebView;
	}

	@Override
	public void onDestroyView() {
		mIsWebViewAvailable = false;
		super.onDestroyView();
	}

	public WebView getWebView() {
		return mIsWebViewAvailable ? mWebView : null;
	}

}