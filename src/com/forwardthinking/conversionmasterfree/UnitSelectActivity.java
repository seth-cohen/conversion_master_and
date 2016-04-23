package com.forwardthinking.conversionmasterfree;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class UnitSelectActivity extends ActionBarActivity implements
		UnitSelectFragment.OnUnitTypeSelectedListener {

	public final static String ARRAY_NAME = "com.forwardthinking.convert.ARRAY_NAME";
	public final static String MAP_NAME = "com.forwardthinking.convert.MAP_NAME";
	public final static String SELECTED_UNIT = "com.forwardthinking.convert.SELECTED_UNIT";
	public final static String SELECTED_TYPE_INDEX = "com.forwardthinking.convert.SELECTED_TYPE_INDEX";
	public final static String CURRENT_VALUE = "com.forwardthinking.convert.CURRENT_VALUE";
	public final static String RESOURCE_MAP_ID = "com.forwardthinking.convert.RESOURCE_MAP_ID";
	
	private final static int KEYBOARD_ROWS = 4;
	private final static int KEYBOARD_COLS = 4;
	
	private final int CONVERTER_RESULT_CODE = 1;

	private String lastSelectedType = null;
	private String currentValue = new String("1.0");
	private int selectedIndex = 0;
	
	// for doublepress of back button
	private boolean backAlreadyPressed = false;
	private final static long delay = 1500L;
	private Handler handler = new Handler();
	private Runnable runResetBackPressed = new Runnable() {
		@Override
		public void run() {
			backAlreadyPressed = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unit_select);
	    
		// find reference to the ad and create it.
	    AdView adView = (AdView)this.findViewById(R.id.adView);
	    AdRequest adRequest = new AdRequest.Builder().addTestDevice("719B63AE6F8DED7AFB9B7D38EEFAF5A8").build();
	    
		if (savedInstanceState != null) {
			lastSelectedType = savedInstanceState
					.getString(UnitSelectActivity.SELECTED_UNIT);
			currentValue = savedInstanceState
					.getString(UnitSelectActivity.CURRENT_VALUE);
			selectedIndex = savedInstanceState
					.getInt(UnitSelectActivity.SELECTED_TYPE_INDEX);
		}
		else {
			// load a new ad
			if (adView != null ) adView.loadAd(adRequest);
		}

		// we need to generate a list view
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, new UnitSelectFragment(),
						"dynamicFragList").commit();

		// this is part of a fucking hack to get the keyboad to be cool after
		// going to preferences screen
		// super hack to properly size the keyboard because something terrible
		// happens. Keys get the width of the screen
		// after returning from the preferences screen even though the
		// containers all have the proper sizes.
		final ViewGroup parent = (ViewGroup) findViewById(R.id.fragConverter);
		if (parent != null) {
			final KeyboardView keyboardView = (KeyboardView) parent
					.findViewById(R.id.keyboardview);

			keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							if (keyboardView.getKeyboard() != null) {
								int width = keyboardView.getWidth()/4;
								int height = keyboardView.getHeight()/4;
								
								Keyboard keyboard = keyboardView.getKeyboard();
								for (int y = 0; y < KEYBOARD_ROWS; ++y) {
									for (int x = 0; x < KEYBOARD_COLS; ++x ) {
										Key key = keyboard.getKeys().get(y*4 + x);
										
										key.width = width;
										key.height = height;
										key.x = x*width;
										key.y = y*height;	
									}
								}
							}
						}

					});
		}
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
			Intent intent = new Intent(this, UserSettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == CONVERTER_RESULT_CODE && resultCode == RESULT_OK) {
			// this is the unit type that we came from.
			// when in single pane mode the converter activity adds
			// the string of it's unit type as a return extra
			lastSelectedType = data.getStringExtra(SELECTED_UNIT);
			currentValue = data.getStringExtra(CURRENT_VALUE);
			selectedIndex = data.getIntExtra(SELECTED_TYPE_INDEX, 0);
		} else {
			// we came here from the back button so we should not load a defaultS
			// value or unit type
			lastSelectedType = null;
			currentValue = new String("1.0");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// if we have something still stored here then we are either already in
		// this
		// activity and moved to protrait, in which case load the proper
		// activity
		// or we just came from an activity going into landscape mode
		if (lastSelectedType != null) {
			configureConverter(lastSelectedType);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (backAlreadyPressed) {
			handler.removeCallbacks(runResetBackPressed);
			handler = null;
			super.onBackPressed();
		}
		else {
			backAlreadyPressed = true;
			Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
			handler.postDelayed(runResetBackPressed, delay);
		}
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// we are saving the last selected type and setting it to null
		// so that we know if we are returning from an action by going into
		// landscape or because they hit the back/up button
		// otherwise we will just keep on loading the last conversion activity
		// endlessly on back button
		ConverterFragment fragment = (ConverterFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragConverter);

		// we were in landscape at one point but now that list fragment
		// is not attached to an activity
		if (fragment != null) {
			currentValue = fragment.getCurrentValue();
			selectedIndex = fragment.getSelectedIndex();
		}
		
		outState.putString(SELECTED_UNIT, lastSelectedType);
		outState.putString(CURRENT_VALUE, currentValue);
		outState.putInt(SELECTED_TYPE_INDEX, selectedIndex);
	}

	@Override
	public void onUnitTypeSelected(AdapterView<?> parent, int position) {
		// set the last selected unit
		TextView v = (TextView) parent.getChildAt(position
				- parent.getFirstVisiblePosition());

		String unit = v.getText().toString();

		lastSelectedType = unit;
		configureConverter(unit);

	}

	private void configureConverter(String unit) {
		ConverterFragment fragment = (ConverterFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragConverter);

		// we were in landscape at one point but now that list fragment
		// is not attached to an activity
		if (fragment != null && fragment.getActivity() == null) {
			currentValue = fragment.getCurrentValue();
			selectedIndex = fragment.getSelectedIndex();
			fragment = null;
		}

		UnitSelectFragment selectFragment = (UnitSelectFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragList);
		if (selectFragment != null && selectFragment.getActivity() != null) {
			selectFragment.highlightSelected(lastSelectedType);
		}

		// if the converter fragment is visible then we are in two pane mode
		// and we should take the value directly from the fragment instead of
		// the stored value
		if (fragment != null && fragment.isVisible()) {
			currentValue = fragment.getCurrentValue();
			selectedIndex = fragment.getSelectedIndex();
		}

		int resourceMapId = R.xml.acceleration_map;
		
		if (unit.equals("Angle")) {
			resourceMapId = R.xml.angle_map;
		} 
		else if (unit.equals("Area")) {
			resourceMapId = R.xml.area_map;
		} 
		else if (unit.equals("Concentration")) {
			resourceMapId = R.xml.concentration_map;
		} 
		else if (unit.equals("Density")) {
			resourceMapId = R.xml.density_map;
		} 
		else if (unit.equals("Energy")) {
			resourceMapId = R.xml.energy_map;
		} 
		else if (unit.equals("Flow")) {
			resourceMapId = R.xml.flow_map;
		} 
		else if (unit.equals("Force")) {
			resourceMapId = R.xml.force_map;
		}
		else if (unit.equals("Length")) {
			resourceMapId = R.xml.length_map;
		} 
		else if (unit.equals("Light")) {
			resourceMapId = R.xml.light_map;
		} 
		else if (unit.equals("Mass")) {
			resourceMapId = R.xml.mass_map;
		} 
		else if (unit.equals("Memory")) {
			resourceMapId = R.xml.memory_map;
		}
		else if (unit.equals("Power")) {
			resourceMapId = R.xml.power_map;
		} 
		else if (unit.equals("Pressure")) {
			resourceMapId = R.xml.pressure_map;
		}
		else if (unit.equals("Speed")) {
			resourceMapId = R.xml.speed_map;
		} 
		else if (unit.equals("Temperature")) {
			resourceMapId = R.xml.temperature_map;
		}
		else if (unit.equals("Thermal Conductivity")) {
			resourceMapId = R.xml.thermalk_map;
		}
		else if (unit.equals("Time")) {
			resourceMapId = R.xml.time_map;
		} 
		else if (unit.equals("Torque")) {
			resourceMapId = R.xml.torque_map;
		}
		else if (unit.equals("Volume")) {
			resourceMapId = R.xml.volume_map;
		} 
		else if (unit.equals("Volume-dry")) {
			resourceMapId = R.xml.volume_dry_map;
		} 

		loadActivity(unit, fragment, resourceMapId);
	}
	
	private void loadActivity(String unit, ConverterFragment fragment, int resourceMapId) {
		if (fragment == null) {
			setTitle(R.string.app_name);
			
			Intent intent = new Intent(this, ConverterActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt(RESOURCE_MAP_ID, resourceMapId);
			bundle.putString(CURRENT_VALUE, currentValue);
			bundle.putInt(SELECTED_TYPE_INDEX, selectedIndex);
			bundle.putString(SELECTED_UNIT, unit);
			intent.putExtras(bundle);

			startActivityForResult(intent, CONVERTER_RESULT_CODE);
		} else {
			fragment.updateContent(resourceMapId, currentValue, selectedIndex, unit);
			getSupportActionBar().setTitle("Convert - " + unit);
		}
	}

}
