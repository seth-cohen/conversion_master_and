package com.forwardthinking.conversionmasterfree;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.forwardthinking.conversionmasterfree.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ConverterActivity extends ActionBarActivity {
	private ConverterFragment fragment = null;
	private String selectedTypeString;
	private int resourceMapId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_converter);

		// find reference to the ad and create it.
	    AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest adRequest = new AdRequest.Builder().addTestDevice("719B63AE6F8DED7AFB9B7D38EEFAF5A8").build();
	    if (adView != null) adView.loadAd(adRequest);
	    
		if (savedInstanceState == null) {
			// create a new converter fragment and pass the appropriate
			// Id for the unit string-array
			fragment = new ConverterFragment();

			// Extras from the unit select activity needed to pass to the
			Bundle bundle = new Bundle();
			// the id of the XML defined map for the conversion factors
			resourceMapId = getIntent().getIntExtra(
					UnitSelectActivity.RESOURCE_MAP_ID, R.xml.acceleration_map);
			bundle.putInt(UnitSelectActivity.MAP_NAME, resourceMapId);

			// the current value being converted
			bundle.putString(UnitSelectActivity.CURRENT_VALUE, getIntent()
					.getStringExtra(UnitSelectActivity.CURRENT_VALUE));

			// the index of the current unit basis
			bundle.putInt(UnitSelectActivity.SELECTED_TYPE_INDEX, getIntent()
					.getIntExtra(UnitSelectActivity.SELECTED_TYPE_INDEX, 0));

			// string representing the UNIT category that is being converted
			selectedTypeString = getIntent().getStringExtra(
					UnitSelectActivity.SELECTED_UNIT);
			bundle.putString(UnitSelectActivity.SELECTED_UNIT,
					selectedTypeString);
			
			setTitle("Convert - " + selectedTypeString);

			// create the fragments passing the bundle with all of the information
			// needed
			// properly configure it.
			fragment.setArguments(bundle);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment, "dynamicFragConverter")
					.commit();
		} else {
			// we shouldn't get here but, for some reason on api level 8
			// onconfigurationchanged is not called so we don't approriately end
			// this activity and restart the main activity in 2 pain mode.
			// the id of the XML defined map for the conversion factors
			finish();

		}

	}

	@Override
	public void onPause() {
		super.onPause();

		// setActivityResult();
		// finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			fragment.setDirty(true); // preference may change, so reconvert in
										// the fragment
			Intent intent = new Intent(this, UserSettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setActivityResult();
			finish();
		}
	}

	private void setActivityResult() {
		// start the main activity but let it know what unit to default to
		// showing
		Intent intent = new Intent(this, UnitSelectActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(UnitSelectActivity.SELECTED_UNIT, selectedTypeString);
		bundle.putString(UnitSelectActivity.CURRENT_VALUE,
				fragment.getCurrentValue());
		bundle.putInt(UnitSelectActivity.SELECTED_TYPE_INDEX,
				fragment.getSelectedIndex());
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
	}

}