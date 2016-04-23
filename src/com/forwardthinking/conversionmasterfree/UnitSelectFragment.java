package com.forwardthinking.conversionmasterfree;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UnitSelectFragment extends Fragment{
	OnUnitTypeSelectedListener typeSelectedListener;
	
	// interface that the parent activity must implement to handle when an
	// item in the list or grid view was clicked
	public interface OnUnitTypeSelectedListener {
		public void onUnitTypeSelected(AdapterView<?> parent, int position);
	}
	
	public UnitSelectFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// make sure the activity implemented the listener
		try {
			typeSelectedListener = (OnUnitTypeSelectedListener) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() 
					+ "must implement OnUnitTypeSelectedListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_unit_select,
				container, false);
	    
		//get reference to the Gridview/listview dynamically at runtime to 
		// populate with the data in the string array resource
		AbsListView gridView = (AbsListView) rootView.findViewById(R.id.gridViewUnits);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), 
					R.array.unit_types, R.layout.gridview_item);
		
		// this casting is needed to support older versions of the API [level 8] because the 
		// AbsListView version of the function wasn't added until level 11
		if (gridView instanceof GridView){
			((GridView)gridView).setAdapter(adapter);
		} 
		else { // in landscape mode this will be a list view
			((ListView)gridView).setAdapter(adapter);
			((ListView)gridView).setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			((ListView)gridView).setItemChecked(0, true);
		}
		
		// callback to the parent activity because we don't know if we are going to be in single
		// or multipane and the parent may need to update content in another fragment
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(getActivity(), ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
				
				// uses selectors and drawables linked to the listview item view layout
				// to set the background state of the selected item
				if (parent instanceof ListView) {
					((ListView)parent).setItemChecked(position, true);
				}
				
				typeSelectedListener.onUnitTypeSelected(parent, position);
			}
		});
		return rootView;
	}
	
	public void highlightSelected(final String unitType) {
		final AdapterView<?> adapterView = (AdapterView<?>) getActivity().findViewById(R.id.gridViewUnits);

		adapterView.post(new Runnable() {

			//TODO investigate WHY this is the only way I could get this to work...
			// for some rreason in touch mode listview.setselection does not set the item as selected.
			@Override
			public void run() {
				// use the adapter to find the item that matches the selected unit type
				Adapter adapter = adapterView.getAdapter();
				for (int i = 0; i < adapter.getCount(); i++) {
					String type = (String) adapterView.getItemAtPosition(i);
					if (type.equals(unitType)) {
						// scroll to the adapter position
						((ListView)adapterView).smoothScrollToPosition(i);
						((ListView)adapterView).setItemChecked(i, true);
					}
				}

			}
		});

	}
}
