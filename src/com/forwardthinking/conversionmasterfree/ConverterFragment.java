package com.forwardthinking.conversionmasterfree;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConverterFragment extends Fragment implements OnItemClickListener {

	// private String DBL_FMT = null;
	// private DecimalFormat dfLargeExponent = null;

	private int precision = 6;
	private boolean dirty = false; // flag to keep track of whether or not the
									// preferences may have changed

	private String currentValue = null;
	private int selectedIndex = 0;
	private String selectedTypeString;

	// private int unitArrayId; - for string array of unit type names
	private int hashMapId;
	private HashMap<String, ?> resourceMap = null;

	private boolean started = false;
	private Converter converter = null;
	private ScientificKeyboard scientificKeyboard = null;

	// items for the listview
	private List<Map<String, String>> mapTypeValue;
	private SimpleAdapter adapter;
	private ListView listView;

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			currentValue = s.toString();
			convertAll(listView.getCheckedItemPosition());

		}
	};

	public ConverterFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// load the default fragment layout
		View rootView = inflater.inflate(R.layout.fragment_converters,
				container, false);

		// get the conversion map for this unit type
		if (getArguments() != null) {
			hashMapId = getArguments().getInt(UnitSelectActivity.MAP_NAME);
			currentValue = getArguments().getString(
					UnitSelectActivity.CURRENT_VALUE);
			selectedIndex = getArguments().getInt(
					UnitSelectActivity.SELECTED_TYPE_INDEX);
			selectedTypeString = getArguments().getString(
					UnitSelectActivity.SELECTED_UNIT);
		} else {
			hashMapId = R.xml.acceleration_map;
			currentValue = new String("1.0");
			selectedIndex = 0;
			selectedTypeString = new String("Acceleration");
		}

		// initialize the listview containing the values and names of each of
		// the types
		mapTypeValue = new ArrayList<Map<String, String>>();
		adapter = new SimpleAdapter(getActivity(), mapTypeValue,
				R.layout.listview_type_item, new String[] { "type", "value" },
				new int[] { R.id.textViewType, R.id.textViewValue });

		// attach the adapter to the listview.
		listView = (ListView) rootView.findViewById(R.id.listViewTypes);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemClickListener(this);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		// generate the view hierarchy (started set to true in update content
		// so if the fragment is loaded from the main activity this never gets
		// called
		// otherwise the value would get reset
		if (!started) {
			updateContent(hashMapId, currentValue, selectedIndex,
					selectedTypeString);
		}
		setupKeyboard();

		if (dirty) {
			// the preferences may have changed the precision.
			// If so we need to import new settings and perform a new conversion
			dirty = false;
			generateDoubleFormat();
			convertAll(selectedIndex);
		}
	}

	// newUnitSelect = true means that we are in two pane mode and a new
	// unit was selected so we need to generate a new view
	@SuppressWarnings("unchecked")
	// ugly hack suppress error for the cast of the map
	public void updateContent(int mapId, String defaultValue,
			int conversionIndex, String selectedType) {

		started = true;
		currentValue = defaultValue;
		selectedIndex = conversionIndex;
		selectedTypeString = selectedType;

		hashMapId = mapId;
		if (selectedTypeString.equals("Temperature")) {
			resourceMap = (HashMap<String, String>) MapXMLParser
					.getHashMapStringResource(getActivity(), hashMapId);
			converter = new GainOffsetConverter(
					(HashMap<String, String>) resourceMap);
		} else {
			resourceMap = (HashMap<String, Double>) MapXMLParser
					.getHashMapResource(getActivity(), hashMapId);
			converter = new GainConverter((HashMap<String, Double>) resourceMap);
		}

		// populate the underlying data for the adapter
		// indicate that the data set has been updated
		mapTypeValue.clear();
		Map<String, String> tempMap;
		for (String type : resourceMap.keySet()) {
			tempMap = new HashMap<String, String>();
			tempMap.put("type", ":" + type);
			tempMap.put("value", "1.0");

			mapTypeValue.add(tempMap);
		}
		adapter.notifyDataSetChanged();

		// prevent out-of-pounds errors on the selection
		selectedIndex = Math.min(selectedIndex, mapTypeValue.size() - 1);
		listView.setSelection(selectedIndex);
		listView.setItemChecked(selectedIndex, true);

		setDefaultValue(defaultValue);

		// preferences may have changed get the preferences and set the format
		generateDoubleFormat();

		convertAll(selectedIndex);
	}

	public void setDefaultValue(String defaultValue) {
		// create the converter object and force a conversion using the first
		// type
		EditText editFromValue = null;

		try {
			// set default value of 1.0 if this is the first time we are coming
			// to this fragment else recycle the original value
			editFromValue = (EditText) getActivity().findViewById(
					R.id.editFromValue);
			editFromValue.removeTextChangedListener(textWatcher);
			editFromValue.setText(defaultValue);
			editFromValue.selectAll();

			editFromValue.addTextChangedListener(textWatcher);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setupKeyboard() {
		scientificKeyboard = new ScientificKeyboard(getActivity(),
				R.id.keyboardview, R.xml.keyboard_scientific);
		// register the edittext
		scientificKeyboard.registerEditText(R.id.editFromValue);
		scientificKeyboard.hideKeyboard();
	}

	public void convertAll(int position) {
		EditText editFromValue = null;
		TextView textFromType = null;

		String from = null;
		String to = null;
		Double value = 0.0;
		MathContext mathContext = new MathContext(precision);
		BigDecimal bdTempValue;
		BigDecimal bdValue;

		try {
			// parse the text in the editText to a double and do the conversion
			textFromType = (TextView) getActivity().findViewById(
					R.id.textFromType);
			editFromValue = (EditText) getActivity().findViewById(
					R.id.editFromValue); // get the editTextView

			// if the value is empty set all values to 0.0f
			String valueString = editFromValue.getText().toString();
			if (valueString.equals("")) {
				value = 0.0d;
			} else {
				value = Double.parseDouble(valueString);
			}

			Map<String, String> selectedItem = mapTypeValue.get(position);

			from = selectedItem.get("type").replace(":", ""); // the selected
																// base unit
																// type
			textFromType.setText("Basis: " + from);

			// update the values of the listviews underlying map resource
			for (Map<String, String> map : mapTypeValue) {
				to = map.get("type").replace(":", "");

				if (to.equals(from)) {
					// map.put("value",
					// dfLargeExponent.format(value).replace("E+0",
					// "").replace("E", "e"));
					bdTempValue = BigDecimal.valueOf(value);
					bdValue = bdTempValue.round(mathContext);
					map.put("value", bdValue.toEngineeringString());
				} else {
					// valueString =
					// dfLargeExponent.format(converter.convert(from, to,
					// value));
					// map.put("value", valueString.replace("E+0",
					// "").replace("E", "e"));
					bdTempValue = BigDecimal.valueOf(converter.convert(from,
							to, value));
					bdValue = bdTempValue.round(mathContext);
					map.put("value", bdValue.toEngineeringString());
				}
			}
			adapter.notifyDataSetChanged();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void generateDoubleFormat() {
		// something weird with decimal format and the number of digits to
		// display
		// for some reason it is 3 less than the total number of digits
		// indicated to be displayed
		Context applicationContext = getActivity().getApplicationContext();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		// get the precision setting
		String precisionString = prefs.getString(
				"pref_precision",
				applicationContext.getResources().getString(
						R.string.pref_precision_default));
		precision = Integer.parseInt(precisionString);

		// DBL_FMT = "##0.";
		// for (int i = 0; i < precision - 1; i++) {
		// DBL_FMT += "#";
		// }
		// DBL_FMT += "E+0";
		//
		// // create the decimal format object
		// dfLargeExponent = new DecimalFormat(DBL_FMT);
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setDirty(boolean isDirty) {
		dirty = isDirty;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		selectedIndex = position;
		convertAll(position);

	}
}
