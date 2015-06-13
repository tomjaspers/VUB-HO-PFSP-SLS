package main;

public class SolutionQualityTrace {
	
	int quality;
	int iteration;
	long time;
	
	public SolutionQualityTrace(){}

	public SolutionQualityTrace(int quality, int iteration, long time) {
		super();
		this.quality = quality;
		this.iteration = iteration;
		this.time = time;
	}

	@Override
	public String toString() {
		return quality + "," + iteration + "," + time;
		//return "quality=" + quality + ", iteration=" + iteration + ", time="				+ time + "";
	}
	
	
}
