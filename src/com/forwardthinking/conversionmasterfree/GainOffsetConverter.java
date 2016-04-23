/**
 * 
 */
package com.forwardthinking.conversionmasterfree;

import java.util.HashMap;

/**
 * @author Seth
 *
 */
public class GainOffsetConverter  implements Converter{
private HashMap<String, String> map;
	
	public GainOffsetConverter(HashMap<String, String> map) {
		this.map = map;
	}
	
	public Double convert(String from, String to, Double value) {
		Double result;
		Double factor;
		Double offset;
		
		try {
			
			// convert to the base
			// get the factor and the offset [string should be formatted "factor|offset"
			String[] splitResult = map.get(from).split("\\|", 2);
			factor = Double.parseDouble(splitResult[0]);
			offset = Double.parseDouble(splitResult[1]);
			result = (value+offset)/factor;
			
			// convert from base to final
			// get the factor and the offset [string should be formatted "factor|offset"
			splitResult = map.get(to).split("\\|", 2);
			factor = Double.parseDouble(splitResult[0]);
			offset = Double.parseDouble(splitResult[1]);
			result = result*factor-offset;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
}
