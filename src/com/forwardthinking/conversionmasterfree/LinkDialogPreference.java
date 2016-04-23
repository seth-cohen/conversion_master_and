package com.forwardthinking.conversionmasterfree;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class LinkDialogPreference extends DialogPreference {
	Context myContext;
	public LinkDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        myContext = context;
    }
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			goToUrl( myContext.getResources().getString(R.string.pro_app_link));
		}
		super.onDialogClosed(positiveResult);
	}
	
	private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        myContext.startActivity(launchBrowser);
    }
}
