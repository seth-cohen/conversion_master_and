package com.forwardthinking.conversionmasterfree;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

//in order to support back to API level 8 it is necessary to use
// the deprecated preference activity functions
@SuppressWarnings("deprecation")
public class UserSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setupActionBar();
		addPreferencesFromResource(R.xml.preferences);
		
		// get the current precision to print it out in the summary
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// then you use it after 
		// get reference to the precision to set the summary text
		Preference pref = findPreference("pref_precision");
		pref.setSummary("Current setting is: " + prefs.getString("pref_precision", getResources().getString(R.string.pref_precision_default)));
		
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);

	    if (pref.getTitle().toString().equals(getResources().getString(R.string.pref_precision_title))) {
	        ListPreference listPref = (ListPreference) pref;
	        pref.setSummary("Current setting is: " + listPref.getEntry());
	    }
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// kind of a hack TODO this should probably use a back stack???  but hey, it works as I want
			onBackPressed();
		}
		
		return super.onOptionsItemSelected(item);
	}

}
