package util;

import gnu.trove.map.hash.TIntObjectHashMap;


public class ExpLookUpTable {
	
	TIntObjectHashMap<Double> table;
	double temperature;
	
	public ExpLookUpTable(double temperature){
		this(temperature, 100);
	}
	
	public ExpLookUpTable(double temperature, int initialCapacity){
		assert(temperature != 0);
		assert(initialCapacity > 0);
		
		this.table = new TIntObjectHashMap<>(initialCapacity);
		this.temperature = temperature;
		
	}
	
	public double getExp(int diff){
		if(table.contains(diff)){
			return table.get(diff);
		}
		double value = calculateValue(diff);
		table.put(diff, value);
		return value;
	}
	
	public void setTemperature(double newTemperature){
		assert(temperature != 0);
		
		table.clear();
		this.temperature = newTemperature;
	}
	
	private double calculateValue(int diff){
		return Math.exp(-diff/this.temperature);
	}
}
