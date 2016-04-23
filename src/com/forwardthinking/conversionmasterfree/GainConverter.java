/**
 * 
 */
package com.forwardthinking.conversionmasterfree;

import java.util.HashMap;

/**
 * @author Seth
 *
 */
public class GainConverter implements Converter{
private HashMap<String, Double> map;
	
	public GainConverter(HashMap<String, Double> map) {
		this.map = map;
	}
	
	public Double convert(String from, String to, Double value) {
		Double result = null;
		Double factor;
		
		try {
			// convert to the base
			factor = map.get(from);
			result = value/factor;
			
			// convert from base to final
			factor = map.get(to);
			result = result*factor;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
}
