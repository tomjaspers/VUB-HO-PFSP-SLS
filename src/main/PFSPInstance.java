package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import util.ListUtil;

/**
 * This class serves as a representation for the actual permutation flow-shop
 * scheduling problem (PFSP). This class is solely responsible for: containing
 * all information about the specific problem instance (e.g., number of jobs,
 * number of machines, processing times, etc.), calculating measures of
 * goodness, reading and processing data from an input file.
 * 
 * @author Tom Jaspers
 * 
 */
public class PFSPInstance {
	enum InitializationMethod {
		RANDOM_PERMUTATION, SLACK_HEURISTIC
	};

	private final static Charset ENCODING = StandardCharsets.UTF_8;

	private String instanceName;

	private int numberOfJobs;
	private int numberOfMachines;

	private int[] dueDates;
	private int[] priorities;

	private int[][] processingTimesMatrix;

	private int idxJobId = 0;
	private int idxJobCompletionTime;

	public PFSPInstance() {
	}

	public PFSPInstance(String fileName) throws Exception {
		readDataFromFile(fileName);
	}

	/**
	 * Initializes all necesarry arrays to the required size
	 */
	private void initializeArrays() {
		// We prefer to work from index 1 as a starting point, so initialize
		// size+1
		dueDates = new int[numberOfJobs + 1];
		priorities = new int[numberOfJobs + 1];
		processingTimesMatrix = new int[numberOfJobs + 1][numberOfMachines + 1];
		idxJobCompletionTime = numberOfMachines;
	}
	
	/**
	 * Calculates the temperature, used for SA-like acceptance criteria
	 * 
	 * See paper: An Iterated Greedy Algorithm for the Flowshop Problem with Sequence Dependent Setup Times (Rubén and Stützle, 2005)
	 * 
	 * @param t adjustable parameter multiplied with base temperature
	 * @return
	 */
	public double getTemperature(double t){
		double temperature = 0;
		
		for (int i = 1; i <= numberOfJobs; i++) {
			for (int j = 1; j <= numberOfMachines; j++) {
				temperature+= processingTimesMatrix[i][j];
			}
		}
		
		return t * temperature / (numberOfJobs*numberOfMachines*10);
	}

	/**
	 * Reads and processes a file (given by its filename) in to the object's
	 * representation.
	 */
	public void readDataFromFile(String fileName) throws Exception {

		final Path filePath = Paths.get(fileName);

		this.instanceName = filePath.getFileName().toString();

		int currentValue;
		try (Scanner scanner = new Scanner(filePath, ENCODING.name())) {
			// First line contains number of jobs and machines
			numberOfJobs = scanner.nextInt();
			numberOfMachines = scanner.nextInt();

			// Once we got the numberOfJobs and numberOfMachines, we can
			// initialize the arrays
			initializeArrays();

			// Next set of lines contain information about the jobs (processing
			// time)
			for (int i = 1; i <= numberOfJobs; i++) {
				for (int j = 1; j <= numberOfMachines; j++) {
					scanner.nextInt(); // Machine number, don't really care
					currentValue = scanner.nextInt(); // processingTime
					processingTimesMatrix[i][j] = currentValue;
				}
			}
			// "Reldue" line in the file
			scanner.next();
			// Last set of lines contain information regarding dueDate and
			// priority
			for (int i = 1; i <= numberOfJobs; i++) {
				scanner.nextInt(); // -1
				currentValue = scanner.nextInt(); // dueDate
				dueDates[i] = currentValue;
				scanner.nextInt(); // -1
				currentValue = scanner.nextInt(); // priority
				priorities[i] = currentValue;

			}
		}

	}

	/**
	 * Creates and returns an initial solution (i.e., a permutation) for this
	 * problem instance, based on the given {@link InitializationMethod}
	 */
	public List<Integer> getInitialSolution(
			InitializationMethod initializationMethod) {
		switch (initializationMethod) {
		case RANDOM_PERMUTATION:
			return getRandomInitialSolution();
		case SLACK_HEURISTIC:
			return getConstructiveInitialSolution();
		default:
			throw new RuntimeException("Unhandled InitializationMethod: "
					+ initializationMethod);
		}
	}

	/**
	 * Generates an initial solution using a random permutation of the N JobIds
	 * 
	 * @return List<Integer> representing an initial solution as an ordering of
	 *         JobIds (0-indexed)
	 */
	public List<Integer> getRandomInitialSolution() {
		List<Integer> solution = new ArrayList<Integer>();

		for (int i = 0; i < getNumberOfJobs(); i++) {
			solution.add(i + 1);
		}
		Collections.shuffle(solution);

		return solution;
	}

	/**
	 * Generates an constructive initial solution using the SLACK heuristic
	 * 
	 * The SLACK heuristic can be done by: Construct the solution inserting one
	 * job at a time, by always selecting the one that minimizes the weighted
	 * earliness. The weighted earliness of job Ji is computed as wi * (di - Ci
	 * ). At each iteration Ci corresponds to the makespan of the partial
	 * solution.
	 * 
	 * @return List<Integer> representing an initial solution as an ordering of
	 *         JobIds (0-indexed)
	 */
	public List<Integer> getConstructiveInitialSolution() {
		List<Integer> solution = new ArrayList<Integer>();
		int[][] partialCompletionTimes = calculateCompletionTimes(solution);

		/* Create a job domain to keep track of the jobs already in our solution */
		List<Integer> jobDomain = new ArrayList<Integer>();
		for (int i = 1; i <= numberOfJobs; i++) {
			jobDomain.add(i);
		}

		/* We have to eventually select all jobs from the jobDomain */
		for (int i = 0; i < numberOfJobs; i++) {
			int idxBestJobId = -1;
			int bestJobId = -1;
			int bestWeightedEarliness = Integer.MAX_VALUE;
			/*
			 * Iterate over the remaining job domain, and find for the best
			 * match
			 */
			for (int j = 0; j < jobDomain.size(); j++) {
				int jobId = jobDomain.get(j);

				int weight = priorities[jobId];
				int dueDate = dueDates[jobId];

				/*
				 * Simulate adding this job to the solution, to retrieve the
				 * partial makespan
				 */
				List<Integer> newSolution = new ArrayList<Integer>(solution);
				newSolution.add(jobId);
				int[][] newPartialCompletionTimes = reCalculateCompletionTimes(
						newSolution, partialCompletionTimes, i);
				int partialMakespanWithThisJob = newPartialCompletionTimes[i][idxJobCompletionTime];

				int weightedEarliness = (weight * (dueDate - partialMakespanWithThisJob));

				if (weightedEarliness < bestWeightedEarliness) {
					idxBestJobId = j;
					bestJobId = jobId;
					bestWeightedEarliness = weightedEarliness;
				}
			}
			/* Add the job to the solution, and remove it from the domain */
			solution.add(bestJobId);
			partialCompletionTimes = reCalculateCompletionTimes(solution,
					partialCompletionTimes, i);
			jobDomain.remove(idxBestJobId);
		}

		return solution;
	}

	/**
	 * Calculates the completion time for a given jobs order (permutation) when
	 * no previous completion times has been provided (i.e., during the initial
	 * construction of a solution). If there has been a previously existing
	 * completion times, the function
	 * {@link #reCalculateCompletionTimes(List, int[][], int)} could be used to
	 * reduce computation.
	 * 
	 * @see #reCalculateCompletionTimes(List, int[][], int)
	 */
	public int[][] calculateCompletionTimes(List<Integer> jobsOrder) {
		int[][] previousCompletionTimes = new int[numberOfJobs][numberOfMachines + 1];

		return reCalculateCompletionTimes(jobsOrder, previousCompletionTimes, 0);
	}

	/**
	 * Recalculates the set of completion times for a jobs order, while taking
	 * in to account the previous completion times (i.e., the completion times
	 * of the previous permutation of jobs), and the index of the lowest-indexed
	 * item that is different between the new and the old permutation. This
	 * method can save unnecessary recalculation of the entire completion times
	 * matrix, and just recalculates the modified part.
	 * 
	 * E.g., We permute the list B of length N+1 to B' by swapping the last two
	 * items; to recaluclate, we can use the completion times of list B, and
	 * recaluclate starting from index N-2
	 */
	public int[][] reCalculateCompletionTimes(List<Integer> jobsOrder,
			final int[][] previousCompletionTimes, int startIndex) {
		// we need a proper copy to run the simulated move on !
		int[][] completionTimes = ListUtil.copyOf(previousCompletionTimes);

		int jobId;
		int previousMachineEndTime;
		int previousJobMachineEndTime;
		for (int i = startIndex; i < jobsOrder.size(); i++) {
			jobId = jobsOrder.get(i);
			completionTimes[i][idxJobId] = jobId;
			previousMachineEndTime = 0;
			if (i == 0) {
				for (int j = 1; j <= numberOfMachines; j++) {
					previousMachineEndTime = previousMachineEndTime
							+ processingTimesMatrix[jobId][j];
					completionTimes[i][j] = previousMachineEndTime;
				}
			} else {
				for (int j = 1; j <= numberOfMachines; j++) {
					previousJobMachineEndTime = completionTimes[i - 1][j];
					previousMachineEndTime = Math.max(previousMachineEndTime,
							previousJobMachineEndTime)
							+ processingTimesMatrix[jobId][j];
					completionTimes[i][j] = previousMachineEndTime;
				}
			}

		}
		return completionTimes;
	}

	/**
	 * Calculates the total weighted tardiness from an array of completion times
	 * 
	 * Possible enhancement: keep an array of CUMULATIVE weighted tardiness to
	 * avoid recalculating everything
	 * 
	 * @param completionTimes
	 * @return int value indicating the total weighted tardiness
	 */
	public int calculateWeightedTardiness(int[][] completionTimes) {
		int weightedTardiness = 0;

		int jobId;
		int jobCompletionTime;
		int tardiness;
		for (int i = 0; i < completionTimes.length; i++) {
			jobId = completionTimes[i][idxJobId];
			jobCompletionTime = completionTimes[i][idxJobCompletionTime];

			tardiness = Math.max(jobCompletionTime - dueDates[jobId], 0);

			weightedTardiness += tardiness * priorities[jobId];
		}
		return weightedTardiness;
	}

	/* Getters below */

	public int getNumberOfJobs() {
		return numberOfJobs;
	}

	public int getNumberOfMachines() {
		return numberOfMachines;
	}

	public int[] getDueDates() {
		return dueDates;
	}

	public int[] getPriorities() {
		return priorities;
	}

	public int[][] getProcessingTimesMatrix() {
		return processingTimesMatrix;
	}

	public String getInstanceName() {
		return instanceName;
	}
}
