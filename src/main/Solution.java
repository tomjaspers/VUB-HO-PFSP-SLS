package main;

import java.util.List;

/**
 * Solution class serving as a container to hold the jobsOrder, the
 * resulting completionTimes, and the weightedTardiness
 * 
 * @author Tom Jaspers
 * 
 */
public class Solution {
	public List<Integer> jobsOrder;
	public int[][] completionTimes;
	public int weightedTardiness;
	
	public int iterations;
	public long runtime;
	public List<SolutionQualityTrace> qualityTraces;

	public Solution() {
	}
	
	public int getSize(){
		return jobsOrder.size();
	}
	
	@Override
	public String toString() {
		return "Total WT = " + weightedTardiness;
	}
}